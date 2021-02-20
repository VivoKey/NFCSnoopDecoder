pipeline {
    agent any
    tools {
        maven 'DefaultMaven'
        jdk 'JDK11'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
            steps {
				sh 'mvn -Dmaven.test.failure.ignore=true clean install -U'
				
            }
            post {
                always {
					archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
	
    }
}