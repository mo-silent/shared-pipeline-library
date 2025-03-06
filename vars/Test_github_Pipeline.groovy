#!groovy
import org.*
// import org.jenkinsci.plugins.credentialsbinding.impl.SuppressCredentialsInterceptors

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    def tools = new Tools()
    def ECR = new org.aws.ECR()

    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        environment {
            DOCKER_REGISTRY_HOST_TOKYO = "329599658616.dkr.ecr.us-west-2.amazonaws.com"
            ROLE_ARN = "arn:aws:iam::329599658616:role/jenkins_slave"
            GIT_CREDENTIAL_ID = 'test_silent'
            GIT_API_TOKEN = credentials('test_silent')
        }
        stages  {
            stage("get_evn") {
                steps {
                    script {
                        // def branchName = scm.branches[0].name
                        // if (branchName.contains("*/")) {
                        //     branchName = branchName.split("\\*/")[1]
                        // }
                        // echo "分支名称: ${branchName}"
                        
                        // def changedFiles = getChangedFiles()
                        // def modifiedDirs = changedFiles.collect { it.split('/')[0] }.unique()
                        // echo "修改的一级目录: ${modifiedDirs}"
                        // sh """
                        //     echo ***INFO：当前目录是 `pwd` && echo ***INFO：列出target目录文件 && ls -lha
                        // """

                        // sh 'mkdir -p dist && echo "Build output" > dist/output.txt'
                        // stash includes: 'dist/**', name: 'dist-stash'
                        echo "当前的config 信息: ${config}"
                        // sh 'aws --version'
                        // sh 'aws ecr get-login-password --region us-west-2 > token.txt'
                        // sh 'cat token.txt'
                        if (config.GROUP_NAME) {
                            String region = env.DOCKER_REGISTRY_HOST_TOKYO.tokenize('.')[-3].toLowerCase()
                            
                            // ECR.createRepository(region, config.GROUP_NAME, null)
                        }
                        if (config.GIT_REPO) {
                            println "token: ${env.GIT_API_TOKEN}"
                            // tools.checkoutSource(config.GIT_REPO, "main", env.GIT_CREDENTIAL_ID)
                        }
                    }
                }
            }
            stage("Build for AMD64 platform") {
              agent {
                kubernetes {
                    yaml Kanikotemplate()
                }
              }
              steps {
                container('kaniko') {
                    echo 'Building the Docker image'
                    // unstash 'dist-stash'
                    // sh 'cat dist/output.txt'
                    // sh 'sleep 180'
                    // sh '/kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination 617482875210.dkr.ecr.us-east-1.amazonaws.com/java-demo:202310-02-amd64'
                }
              }
            }
        }
    }
}

@NonCPS
def getChangedFiles() {
    def changedFiles = []
    for (changeLogSet in currentBuild.changeSets) {
        for (entry in changeLogSet.items) {
            for (file in entry.affectedFiles) {
                changedFiles.add(file.path)
            }
        }
    }
    return changedFiles
}

