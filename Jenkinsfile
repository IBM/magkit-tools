@Library('jenkins-library-acid-base-github@latest') _

pipeline {
  agent {
    label 'apertomagkit'
  }
  tools {
    maven 'maven360'
    jdk 'openJDK11'
  }
  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(artifactNumToKeepStr: '5', numToKeepStr: '30'))
    timeout time: 1, unit: 'HOURS'
  }
  parameters {
    choice(name: 'INPUT_ENV', description: 'Choice of additional action', choices: ['Build', 'Release'])
  }
  environment {
    // notification Slack variables
    SLACK_TOKEN = 'pn1bvDADqWbd1aNeKkInOF8F'
    SLACK_BASE_URL = 'https://aperto.slack.com/services/hooks/jenkins-ci/'
    SLACK_CHANNEL = '#cop-magnolia'
  }

  stages {
    stage('Build') {
      when {
        anyOf {
          branch 'master'; branch 'develop'; branch 'PR-*'
        }
        expression {
          params.INPUT_ENV != 'Release'
        }
      }
      post {
        always {
          acidSendNotifications (this, [projectOs: 'magnolia', sendNoSlackSuccessNotification: true])
        }
      }
      steps {
        script {
          def mavenBaseCommand = "deploy"
          if (BRANCH_NAME ==~ /^PR-.*/ ) {
            mavenBaseCommand = "package"
          }
          def secrets = [
            [$class: 'VaultSecret', path: "acid/tools/nexus/live", engineVersion: 2, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = "${mavenBaseCommand} -Pci,coverage -U -Duser=$DEPLOY_USERNAME -Dpw=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            withMaven {
              sh "mvn $mavenParams"
            }
          }
        }
      }
    }
    stage('Sonar') {
      when {
        anyOf {
          branch 'master'; branch 'develop'; branch 'PR-*'
        }
        expression {
          params.INPUT_ENV != 'Release'
        }
      }
      post {
        always {
          acidSendNotifications (this, [projectOs: 'magnolia', sendNoSlackSuccessNotification: true])
        }
      }
      steps {
        withSonarQubeEnv('SonarQube') {
          sh "mvn sonar:sonar -Duser=$DEPLOY_USERNAME -Dpw=$DEPLOY_PASSWORD"
        }
      }
    }
    stage('Release') {
      when {
        branch 'master'
        expression {
          params.INPUT_ENV == 'Release'
        }
      }
      post {
        always {
          acidSendNotifications (this, [projectOs: 'magnolia', sendNoSlackSuccessNotification: false])
        }
      }
      steps {
        script {
          echo 'Release translation module'
          def secrets = [
            [$class: 'VaultSecret', path: "acid/tools/nexus/live", engineVersion: 2, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = "release:clean release:prepare release:perform -Pci -Duser=$DEPLOY_USERNAME -Dpw=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            withMaven {
              sh "mvn $mavenParams"
            }
          }
        }
      }
    } // end stage release
  }
  post {
    always {
      cleanWs ()
    }
  }
}
