#!groovy
import org.*

def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // 实例化对象，导入共享库方法
    def utils = new Utils()
    def tools = new Tools()
    def ECR = new org.aws.ECR()


    // 获取项目参数并进行初始化处理和校验
    Map METADATA = utils.getProjectParams('CI', config)

    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        environment {
            DOCKER_REGISTRY_HOST_TOKYO = "329599658616.dkr.ecr.us-west-2.amazonaws.com"
            ROLE_ARN = "arn:aws:iam::329599658616:role/jenkins_slave"
            GIT_CREDENTIAL_ID = 'plaud_silent'
        }
        stages  {
            stage("get_evn") {
                steps {
                    script {
                        def branchName = scm.branches[0].name
                        if (branchName.contains("*/")) {
                            branchName = branchName.split("\\*/")[1]
                        }
                        echo "分支名称: ${branchName}"
                        METADATA.put("branchName", branchName)
                        
                        def changedFiles = getChangedFiles()
                        def modifiedDirs = changedFiles.collect { it.split('/')[0] }.unique()
                        echo "修改的一级目录: ${modifiedDirs}"
                        METADATA.put("modifiedDirs", modifiedDirs)
                    }
                }
            }
            stage("Build_Src") {
                steps {
                    script{
                        repo_dir = tools.checkoutSource(METADATA.GIT_REPO, "release", env.GIT_CREDENTIAL_ID)
                        dir(repo_dir) {
                            sh "pwd && ls -l"
                            tools.build(METADATA)
                            stash includes: 'web/dist/**', name: 'web-dist'
                        }
                    }
                    
                }
            }
            stage("Create ECR Repository") {
                when {
                    equals expected: "true",
                    actual: METADATA.IS_DOCKER
                }
                steps {
                    script{
                        String region = env.DOCKER_REGISTRY_HOST_TOKYO.tokenize('.')[-3].toLowerCase()
                        ECR.createRepository(region, METADATA.GROUP_NAME, null)
                    }
                }
            }
            stage("Build Docker images") {
                when {
                    equals expected: "true", 
                    actual: METADATA.IS_DOCKER
                }
                agent {
                    kubernetes {
                        yaml Kanikotemplate()
                    }
                }
                steps {
                    container('kaniko') {
                        echo 'Building the Docker image'
                        unstash 'web-dist'
                        sh 'ls -la'
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

