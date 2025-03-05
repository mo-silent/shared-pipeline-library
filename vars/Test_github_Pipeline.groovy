#!groovy
def call(body) {
    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        stages {
            stage('Handle Webhook') {
                steps {
                    script {
                        // 通过 params.payload 访问变量
                        echo "GitHub Event Payload"
                    }
                }
            }
        }
    }
}