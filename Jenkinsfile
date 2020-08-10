@Library('jenkins-library-acid-base-github@0.47.0') _

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
    NOTIFY_SUCCESS = true
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
          acidSendNotifications (this, [projectOs: 'web', sendNoSlackSuccessNotification: NOTIFY_SUCCESS])
        }
      }
      steps {
        script {
          def secrets = [
            [$class: 'VaultSecret', path: "mobile-engineering/tools/nexus/acid.build", engineVersion: 1, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = " deploy --batch-mode -Pci -U -Dnexususer=$DEPLOY_USERNAME -Dnexuspassword=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            acidExecuteMaven(this, [
              configFileId: '5ff62c62-4015-4854-8ab8-29bd275a1a92',
              params: mavenParams,
              suppressionsEnabled: true
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
      steps {
        script {
          echo 'Release translation module'
          def secrets = [
            [$class: 'VaultSecret', path: "mobile-engineering/tools/nexus/acid.build", engineVersion: 1, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = " release:clean release:prepare release:perform --batch-mode -Pci -Dnexususer=$DEPLOY_USERNAME -Dnexuspassword=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            acidExecuteMaven(this, [
              configFileId: '5ff62c62-4015-4854-8ab8-29bd275a1a92',
              params: mavenParams,
              suppressionsEnabled: true
            ])
          }
        }
      }
      post {
        always {
          acidSendNotifications(this, [sendNoSlackSuccessNotification: NOTIFY_SUCCESS, projectOs: 'web'])
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
