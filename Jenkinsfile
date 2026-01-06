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

        stage('Maven dependencies') { // mvnw install dependencies
            steps {
                echo 'Running a clean install' 
                sh 'cd ./backend && chmod +x ./mvnw && ./mvnw clean install'
                sh 'cd ./backend/auth-service && chmod +x ./mvnw && ./mvnw clean install'
                sh 'cd ./backend/calculator-service && chmod +x ./mvnw && ./mvnw clean install'
                sh 'cd ./Backend/product-service && chmod +x ./mvnw && ./mvnw clean install'
                sh 'cd ./Backend/SKUProfitability && chmod +x ./mvnw && ./mvnw clean install'
            }
        }

        stage('Build and Package') { // build the jar
            steps {
                dir('./backend') {
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/auth-service') {
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/calculator-service') {
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/product-service') {
                    sh './mvnw package -DskipTests'
                }
                dir('./backend/SKUProfitability') {
                    sh './mvnw package -DskipTests'
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
}
