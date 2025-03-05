#!groovy
// import org.devops.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        stages  {
            stage("Checkout") {
                steps {
                    script{
                        sleep(100000000) {
                            // on interrupt do
                            echo "hello"
                        }
                    }
                }
            }
        }
    }
}