#!/usr/bin/env groovy
def artServer = Artifactory.server 'art-p-01'
def buildInfo = Artifactory.newBuildInfo()
def distDir = 'build/dist'

pipeline {
    libraries {
        lib('jenkins-pipeline-shared')
    }
     environment {
        SVC_NAME = "sbr-control-api"
        ORG = "SBR"
    }
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        timeout(time: 1, unit: 'HOURS')
        ansiColor('xterm')
    }
    agent { label 'download.jenkins.slave' }
    stages {
        stage('Checkout') {
            agent { label 'download.jenkins.slave' }
            steps {
                checkout scm        
                script {
                    buildInfo.name = "${SVC_NAME}"
                    buildInfo.number = "${BUILD_NUMBER}"
                    buildInfo.env.collect()
                }
                colourText("info", "BuildInfo: ${buildInfo.name}-${buildInfo.number}")
                stash name: 'Checkout'
            }
        }

        stage('Build'){
            agent { label 'build.sbt_0-13-13' }
            steps {
                unstash name: 'Checkout'
                sh "sbt compile"
            }
            post {
                success {
                    colourText("info","Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Validate') {
            failFast true
            parallel {
                stage('Test: Unit'){
                    agent { label 'build.sbt_0-13-13' }
                    steps {
                        unstash name: 'Checkout'
                        sh 'sbt coverage test coverageReport coverageAggregate'
                    }
                    post {
                        always {
                            junit '**/target/test-reports/*.xml'
                            cobertura autoUpdateHealth: false, 
                                autoUpdateStability: false, 
                                coberturaReportFile: 'target/**/coverage-report/cobertura.xml', 
                                conditionalCoverageTargets: '70, 0, 0', 
                                failUnhealthy: false, 
                                failUnstable: false, 
                                lineCoverageTargets: '80, 0, 0', 
                                maxNumberOfBuilds: 0, 
                                methodCoverageTargets: '80, 0, 0', 
                                onlyStable: false, 
                                zoomCoverageChart: false
                        }
                        success {
                            colourText("info","Stage: ${env.STAGE_NAME} successful!")
                        }
                        failure {
                            colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                        }
                    }
                }
                stage('Style') {
                    agent { label 'build.sbt_0-13-13' }
                    steps {
                        unstash name: 'Checkout'
                        colourText("info","Running style tests")
                        sh 'sbt scalastyleGenerateConfig scalastyle'
                    }
                    post {
                        always {
                            checkstyle canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'target/scalastyle-result.xml', unHealthy: ''   
                        }
                    }
                }
                stage('Scapegoat') {
                    agent { label 'build.sbt_0-13-13' }
                    steps {
                        unstash name: 'Checkout'
                        colourText("info","Running additional tests")
                        sh 'sbt scapegoat'
                    }
                    post {
                        always {
                            checkstyle canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'target/scala-2.11/scapegoat-report/scapegoat-scalastyle.xml', unHealthy: ''
                        }
                    }
                }
            }
            post {
                success {
                    colourText("info","Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Test: Acceptance') {
            agent { label 'build.sbt_0-13-13' }
            steps {
                unstash name: 'Checkout'
                sh "sbt it:test"
            }
            post {
                always {
                    junit '**/target/test-reports/*.xml'
                }
                success {
                    colourText("info","Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage ('Publish') {
            agent { label 'build.sbt_0-13-13' }
            when { 
                branch "feature/REG-1676-cf-manifest" 
                // evaluate the when condition before entering this stage's agent, if any
                beforeAgent true 
            }
            steps {
                colourText("info", "Building ${env.BUILD_ID} on ${env.JENKINS_URL} from branch ${env.BRANCH_NAME}")
                unstash name: 'Checkout'
                sh 'sbt universal:packageBin'
                script {
                    def uploadSpec = """{
                        "files": [
                            {
                                "pattern": "target/universal/*.zip",
                                "target": "registers-sbt-snapshots/uk/gov/ons/${buildInfo.name}/${buildInfo.number}/"
                            },
                            {
                                "pattern": "manifest.yml",
                                "target": "registers-sbt-snapshots/uk/gov/ons/${buildInfo.name}/${buildInfo.number}/"
                            }
                        ]
                    }"""
                    artServer.upload spec: uploadSpec, buildInfo: buildInfo
                }
            }
            post {
                success {
                    colourText("info","Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Deploy: Dev'){
            agent { label 'deploy.cf' }
            when { 
                branch "feature/REG-1676-cf-manifest"
                // evaluate the when condition before entering this stage's agent, if any
                beforeAgent true 
            }
            environment{
                CREDS = 's_jenkins_sbr_dev'
                SPACE = 'Dev'
            }
            steps {
                script {
                    def downloadSpec = """{
                        "files": [
                            {
                                "pattern": "registers-sbt-snapshots/uk/gov/ons/${buildInfo.name}/${buildInfo.number}/*.*",
                                "target": "${distDir}/",
                                "flat": "true"
                            }
                        ]
                    }"""
                    artServer.download spec: downloadSpec, buildInfo: buildInfo
                    sh "mv ${distDir}/*.zip ${distDir}/${env.SVC_NAME}.zip"
                }
                dir('vars') {
                    git url: "${GITLAB_URL}/StatBusReg/${env.SVC_NAME}.git", branch: 'feature/REG-1676-vars-file', credentialsId: 'JenkinsSBR__gitlab'
                }
                stash name: 'Vars', includes: 'vars/**'
                script {
                    cfDeploy {
                        credentialsId = "${this.env.CREDS}"
                        org = "${this.env.ORG}"
                        space = "${this.env.SPACE}"
                        appName = "${this.env.SPACE.toLowerCase()}-${this.env.SVC_NAME}"
                        appPath = "./${distDir}/${this.env.SVC_NAME}.zip"
                        manifestPath  = "./${distDir}/manifest.yml"
                        varsPath = "vars/${this.env.SPACE.toLowerCase()}/vars.yml"
                    }
                }
            }
            post {
                success {
                    colourText("info","Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }

        stage('Deploy: Test'){
            agent { label 'deploy.cf' }
            when { 
                branch "feature/REG-1676-cf-manifest"
                // evaluate the when condition before entering this stage's agent, if any
                beforeAgent true 
            }
            environment{
                CREDS = 's_jenkins_sbr_test'
                SPACE = 'Test'
            }
            steps {
                script {
                    def downloadSpec = """{
                        "files": [
                            {
                                "pattern": "registers-sbt-snapshots/uk/gov/ons/${buildInfo.name}/${buildInfo.number}/*.*",
                                "target": "${distDir}/",
                                "flat": "true"
                            }
                        ]
                    }"""
                    artServer.download spec: downloadSpec, buildInfo: buildInfo
                    sh "mv ${distDir}/*.zip ${distDir}/${env.SVC_NAME}.zip"
                }
                unstash name: 'Vars'
                script {
                    cfDeploy {
                        credentialsId = "${this.env.CREDS}"
                        org = "${this.env.ORG}"
                        space = "${this.env.SPACE}"
                        appName = "${this.env.SPACE.toLowerCase()}-${this.env.SVC_NAME}"
                        appPath = "./${distDir}/${this.env.SVC_NAME}.zip"
                        manifestPath  = "./${distDir}/manifest.yml"
                        varsPath = "vars/${this.env.SPACE.toLowerCase()}/vars.yml"
                    }
                }
            }
            post {
                success {
                    colourText("info","Stage: ${env.STAGE_NAME} successful!")
                }
                failure {
                    colourText("warn","Stage: ${env.STAGE_NAME} failed!")
                }
            }
        }
    }

    post {
        success {
            colourText("success", "All stages complete. Build was successful.")
            slackSend(
                color: "good",
                message: "${env.JOB_NAME} success: ${env.RUN_DISPLAY_URL}"
            )
        }
        unstable {
            colourText("warn", "Something went wrong, build finished with result ${currentResult}. This may be caused by failed tests, code violation or in some cases unexpected interrupt.")
            slackSend(
                color: "warning",
                message: "${env.JOB_NAME} unstable: ${env.RUN_DISPLAY_URL}"
            )
        }
        failure {
            colourText("warn","Process failed at: ${env.NODE_STAGE}")
            slackSend(
                color: "danger",
                message: "${env.JOB_NAME} failed at ${env.STAGE_NAME}: ${env.RUN_DISPLAY_URL}"
            )
        }
    }
}
