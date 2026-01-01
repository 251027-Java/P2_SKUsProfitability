pipeline {
    agent any
    
    tools {
        jdk 'JDK21' 
    }
    
    stages {
        stage('Hey guys') {
            steps {
                echo 'Pipeline started!'
                echo "Building on: ${env.NODE_NAME}"
            }
        }
        
        stage('Checkout') {
            steps {
                echo 'Code checked out from Git'
            }
        }
        
        stage('Build') {
            steps {
                dir('P2_SKUsProfitability') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Test') {
            steps {
                dir('P2_SKUsProfitability') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Package') {
            steps {
                dir('P2_SKUsProfitability') {
                    sh 'mvn package -DskipTests'
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}