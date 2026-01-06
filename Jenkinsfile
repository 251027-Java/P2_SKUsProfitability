pipeline {
    agent any
    
    tools {
        jdk 'JDK21' 
        maven 'maven'
    }
    
    stages {
        stage('Hey guys') {
            steps {
                echo 'Pipeline started!'
                echo "Building on: ${env.NODE_NAME}"
            }
        }
        
        stage('Git Checkout') {
            steps {
                echo 'Code checked out from Git'
            }
        }

        stage('Build and Package') { // build the jar
            steps {
                dir('./backend') {
                    sh 'chmod +x ./mvnw && ./mvnw clean compile'
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/auth-service') {
                    sh 'chmod +x ./mvnw && ./mvnw clean compile'
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/calculator-service') {
                    sh 'chmod +x ./mvnw && ./mvnw clean compile'
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/product-service') {
                    sh 'chmod +x ./mvnw && ./mvnw clean compile'
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/SKUProfitability') {
                    sh 'chmod +x ./mvnw && ./mvnw clean compile'
                    sh './mvnw package -DskipTests'
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
