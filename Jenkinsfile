#!groovy
@Library('jenkins-pipeline-shared@temporary') _

pipeline {
    agent any
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '30'))
        timeout(time: 30, unit: 'MINUTES')
    }
    stages {
        stage('Checkout') {
            agent any
            steps {
                deleteDir()
                checkout scm
                stash name: 'app'
                sh "$SBT version"
                script {
                    version = '1.0.' + env.BUILD_NUMBER
                    currentBuild.displayName = version
                    env.NODE_STAGE = "Checkout"
                }
            }
        }
        stage('Build'){
            agent any
            steps {
                colourText("info", "Building ${env.BUILD_ID} on ${env.JENKINS_URL} from branch ${env.BRANCH_NAME}")
                script {
                    env.NODE_STAGE = "Build"
                }
                dir('gitlab') {
                    git(url: "$GITLAB_URL/StatBusReg/sbr-control-api.git", credentialsId: 'sbr-gitlab-id', branch: 'fix/csv-column-changes')
                }
                // Remove the fake data
                sh 'rm -rf conf/sample/sbr-2500-ent-data.csv'
                sh 'rm -rf conf/sample/sbr-2500-ent-ch-links.csv'
                sh 'rm -rf conf/sample/sbr-2500-ent-vat-links.csv'
                sh 'rm -rf conf/sample/sbr-2500-ent-paye-links.csv'
                sh 'rm -rf conf/sample/sbr-2500-ent-leu-links.csv'
                sh 'rm -rf conf/sample/sbr-2500-leu-ch-links.csv'
                sh 'rm -rf conf/sample/sbr-2500-leu-paye-links.csv'
                sh 'rm -rf conf/sample/sbr-2500-leu-vat-links.csv'

                // Copy over the real data
                sh 'cp gitlab/dev/data/sbr-2500-ent-data.csv conf/sample/sbr-2500-ent-data.csv'
                sh 'cp gitlab/dev/data/sbr-2500-ent-ch-links.csv conf/sample/sbr-2500-ent-ch-links.csv'
                sh 'cp gitlab/dev/data/sbr-2500-ent-paye-links.csv conf/sample/sbr-2500-ent-paye-links.csv'
                sh 'cp gitlab/dev/data/sbr-2500-ent-vat-links.csv conf/sample/sbr-2500-ent-vat-links.csv'
                sh 'cp gitlab/dev/data/sbr-2500-ent-leu-links.csv conf/sample/sbr-2500-ent-leu-links.csv'
                sh 'cp gitlab/dev/data/sbr-2500-leu-ch-links.csv conf/sample/sbr-2500-leu-ch-links.csv'
                sh 'cp gitlab/dev/data/sbr-2500-leu-paye-links.csv conf/sample/sbr-2500-leu-paye-links.csv'
                sh 'cp gitlab/dev/data/sbr-2500-leu-vat-links.csv conf/sample/sbr-2500-leu-vat-links.csv'

                sh '''
                $SBT clean compile "project api" universal:packageBin
                cp target/universal/sbr-control-api-*.zip dev-ons-sbr-control-api.zip
                cp target/universal/sbr-control-api-*.zip test-ons-sbr-control-api.zip
                cp target/universal/sbr-control-api-*.zip prod-ons-sbr-control-api.zip
                '''
            }
        }
        stage('Static Analysis') {
            agent any
            steps {
                parallel (
                    "Unit" :  {
                        colourText("info","Running unit tests")
                        // sh "$SBT test"
                    },
                    "Style" : {
                       colourText("info","Running style tests")
                        //sh '''
                        //$SBT scalastyleGenerateConfig
                        //$SBT scalastyle
                        //'''
                    },
                    "Additional" : {
                        colourText("info","Running additional tests")
                        //sh "$SBT scapegoat"
                    }
                )
            }
            post {
                always {
                    script {
                        env.NODE_STAGE = "Static Analysis"
                    }
                }
                success {
                    colourText("info","Generating reports for tests")
                    //   junit '**/target/test-reports/*.xml'

                    //step([$class: 'CoberturaPublisher', coberturaReportFile: '**/target/scala-2.11/coverage-report/*.xml'])
                    //step([$class: 'CheckStylePublisher', pattern: 'target/scalastyle-result.xml, target/scala-2.11/scapegoat-report/scapegoat-scalastyle.xml'])
                }
                failure {
                    colourText("warn","Failed to retrieve reports.")
                }
            }
        }
        stage ('Bundle') {
            agent any
             when {
                 anyOf {
                     branch "develop"
                     branch "release"
                     branch "master"
                 }
             }
            steps {
                script {
                    env.NODE_STAGE = "Bundle"
                }
                colourText("info", "Bundling....")
                //packageApp('dev')
                //packageApp('test')
                stash name: "zip"
            }
        }
        stage ('Release') {
            agent any
            when {
                branch "master"
            }
            steps {
                colourText("success", 'Release.')
            }
        }
        stage ('Package and Push Artifact') {
            agent any
            when {
                branch "master"
            }
            steps {
                colourText("success", 'Package.')
            }

        }
        stage('Deploy'){
            agent any
             when {
                 anyOf {
                     branch "develop"
                     branch "release"
                     branch "master"
                 }
             }
            steps {
                colourText("success", 'Deploy.')
                script {
                    env.NODE_STAGE = "Deploy"
                    if (BRANCH_NAME == "develop") {
                        env.DEPLOY_NAME = "dev"
                    }
                    else if  (BRANCH_NAME == "release") {
                        env.DEPLOY_NAME = "test"
                    }
                    else if (BRANCH_NAME == "master") {
                        env.DEPLOY_NAME = "prod"
                    }
                }
                milestone(1)
                lock('Deployment Initiated') {
                    colourText("info", 'deployment in progress')
                    deploy()
                    // unstash zip
                }
            }
        }

        stage('Integration Tests') {
            agent { label 'adrianharristesting' }
            when {
                anyOf {
                    branch "develop"
                    branch "release"
                }
            }
            steps {
                colourText("success", 'Integration Tests - For Release or Dev environment.')
            }
        }


    }
    post {
        always {
            script {
                colourText("info", 'Post steps initiated')
                deleteDir()
            }
        }
        success {
            colourText("success", "All stages complete. Build was successful.")
            sendNotifications currentBuild.result, "\$SBR_EMAIL_LIST"
        }
        unstable {
            colourText("warn", "Something went wrong, build finished with result ${currentResult}. This may be caused by failed tests, code violation or in some cases unexpected interrupt.")
            sendNotifications currentResult, "\$SBR_EMAIL_LIST", "${env.NODE_STAGE}"
        }
        failure {
            colourText("warn","Process failed at: ${env.NODE_STAGE}")
            sendNotifications currentResult, "\$SBR_EMAIL_LIST", "${env.NODE_STAGE}"
        }
    }
}

def deploy () {
    echo "Deploying Api app to ${env.DEPLOY_NAME}"
    withCredentials([string(credentialsId: "sbr-api-dev-secret-key", variable: 'APPLICATION_SECRET')]) {
        deployToCloudFoundry("cloud-foundry-sbr-${env.DEPLOY_NAME}-user", 'sbr', "${env.DEPLOY_NAME}", "${env.DEPLOY_NAME}-sbr-control-api", "${env.DEPLOY_NAME}-ons-sbr-control-api.zip", "gitlab/${env.DEPLOY_NAME}/manifest.yml")
    }
}