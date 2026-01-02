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
        
        stage('Checkout') {
            steps {
                echo 'Code checked out from Git'
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean install'
                }
            }
        }
        
        stage('Test Backend') {
            steps {
                dir('backend') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Package Backend') {
            steps {
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Build ProductService') {
            steps {
                dir('ProductService') {
                    sh 'mvn clean install'
                }
            }
        }
        
        stage('Test ProductService') {
            steps {
                dir('ProductService') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Package ProductService') {
            steps {
                dir('ProductService') {
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