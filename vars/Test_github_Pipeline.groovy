#!groovy
def call(body) {
    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        stages  {
            stage("Docker PS") {
                steps {
                    // sh 'docker ps'
                    sh 'sleep 1800'
                }
            }
            stage("sleep") {
                steps {
                    script{
                        sleep(1800) {
                            // on interrupt do
                            echo "hello"
                        }
                    }
                }
            }
        }
    }
}