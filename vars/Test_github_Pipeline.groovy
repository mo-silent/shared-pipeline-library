#!groovy
def call(body) {
    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        stages  {
            // stage("Docker PS") {
            //     steps {
            //         // sh 'docker ps'
            //         sh 'sleep 1800'
            //     }
            // }
            stage("Build for AMD64 platform") {
              agent {
                kubernetes {
                    yamlFile 'Jenkins-kaniko-amd64.yaml'
                }
              }
              steps {
                container('kaniko') {
                    sh 'sleep 1800'
                    sh '/kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination 617482875210.dkr.ecr.us-east-1.amazonaws.com/java-demo:202310-02-amd64'
                }
              }
            }
        }
    }
}