#!groovy
@Library('jenkins-pipeline-shared') _

pipeline {
    
    environment {
        GIT_TRACE=1
        GIT_TRACE_PERFORMANCE=1
        GIT_CURL_VERBOSE=1
        
        GIT_TYPE = "Github"
        GITLAB_CREDS = "ai-gitlab-creds"

        ORGANIZATION = "ons"
        TEAM = "ai"
        MODULE_NAME = "address-index-api"

   }
   
   agent any
    
    stages {
        stage("Build"){
steps {
                sh "git config --list"
                script {
                    env.NODE_STAGE = "Bundle"
                }
                dir('conf') {
                    git(url: "$GITLAB_URL/AddressIndex/${MODULE_NAME}.git", credentialsId: "$GITLAB_CREDS", branch: "develop")
                }
            }
        }
    }
}
