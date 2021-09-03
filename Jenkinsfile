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
          def secrets = [
            [$class: 'VaultSecret', path: "acid/tools/nexus/live", engineVersion: 2, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = " deploy --batch-mode -Pci -U -Duser=$DEPLOY_USERNAME -Dpw=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            acidExecuteMaven(this, [
              configFileId: '5ff62c62-4015-4854-8ab8-29bd275a1a92',
              params: mavenParams,
              executeDependencyCheck: false
            ])
          }
        }
      }
    }
    stage('Sonar') {
      when {
        branch 'master'
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
        acidExecuteSonar (this, "magnolia", [
          withCoverage: false,
          jdk: 'openJDK11',
          maven: 'maven360'
        ])
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
            def mavenParams = " release:clean release:prepare release:perform --batch-mode -Pci -Duser=$DEPLOY_USERNAME -Dpw=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            acidExecuteMaven(this, [
              configFileId: '5ff62c62-4015-4854-8ab8-29bd275a1a92',
              params: mavenParams,
              executeDependencyCheck: false
            ])
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
