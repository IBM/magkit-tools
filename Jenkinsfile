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
    choice(name: 'INPUT_ENV', description: 'Choice of additional action', choices: ['Build', 'Sonar', 'Release'])
  }
  triggers {
    parameterizedCron(env.BRANCH_NAME == 'develop' ? '''
    H 4 5 * 1-5 %INPUT_ENV=Sonar
    ''' : '')
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
        allOf {
          expression {
            params.INPUT_ENV != 'Sonar'
          }
          expression {
            params.INPUT_ENV != 'Release'
          }
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
            [$class: 'VaultSecret', path: "acid/tools/nexus/live", engineVersion: 2, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = " deploy --batch-mode -Pci -T4 -U -Dnexususer=$DEPLOY_USERNAME -Dnexuspassword=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            acidExecuteMaven(this, [
              configFileId: '43b0811f-4f12-4f37-a972-994571977dec',
              params: mavenParams,
              suppressionsEnabled: true
            ])
          }
        }
      }
    }
    stage('Sonar') {
      when {
        branch 'develop'
        expression {
          params.INPUT_ENV == 'Sonar'
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
            [$class: 'VaultSecret', path: "acid/tools/nexus/live", engineVersion: 2, secretValues: [
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_PASSWORD', vaultKey: 'password'],
              [$class: 'VaultSecretValue', envVar: 'DEPLOY_USERNAME', vaultKey: 'username']
            ]]
          ]
          wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
            def mavenParams = " release:clean release:prepare release:perform --batch-mode -Pci -Dnexususer=$DEPLOY_USERNAME -Dnexuspassword=$DEPLOY_PASSWORD -Djenkins.gitBranch=${GIT_BRANCH} -Djenkins.buildNumber=${BUILD_NUMBER}"
            acidExecuteMaven(this, [
              configFileId: '43b0811f-4f12-4f37-a972-994571977dec',
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
