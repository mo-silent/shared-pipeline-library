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
    def dockerUtils = new org.docker.Utils()
    def jenkins = new Jenkins()


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
                        def branchName = env.ref.replaceFirst('refs/heads/', '')
                        METADATA.put("branchName", branchName)
                        echo "分支名称: ${branchName}"

                        if (!(METADATA.branchName == 'release' || METADATA.branchName.startsWith('feature'))) {
                            echo "当前分支 ${METADATA.branchName} 不是 release 或以 feature 开头的分支，流水线将退出"
                            currentBuild.result = 'ABORTED'
                            error("流水线已中止：不支持的分支类型")
                        }
                        // def changedFiles = env.modified
                        // 处理修改的文件目录
                        def modifiedDirs = []
                        def commits = readJSON text: env.commits
                        commits.each { commit ->
                            if (commit.modified) {
                                commit.modified.each { file ->
                                    if (file.contains('/')) {
                                        modifiedDirs << file.substring(0, file.indexOf('/'))
                                    }
                                }
                            }
                        }
                        modifiedDirs = modifiedDirs.unique()
                        METADATA.put("modifiedDirs", modifiedDirs)
                        
                        // set Job build display name
                        def BUILD_TAG = jenkins.setJobDisplayName("CI", METADATA)   
                        METADATA.put("BUILD_TAG", BUILD_TAG)

                        echo "修改的一级目录: ${modifiedDirs}"
                        echo "BUILD_TAG: ${BUILD_TAG}"
                        // sh 'exit 1'
                    }
                }
            }
            stage("Build_Src") {
                steps {
                    script{
                        repo_dir = tools.checkoutSource(METADATA.GIT_REPO, METADATA.branchName, env.GIT_CREDENTIAL_ID)
                        METADATA.put("repo_dir", repo_dir)
                        dir(repo_dir) {
                            sh "pwd && ls -l"
                            tools.build(METADATA)
                        }
                    }
                }
            }
            stage("Create Docker Files") {
                when {
                    equals expected: "true",
                    actual: METADATA.IS_DOCKER
                }
                steps {
                    script{
                        echo "repo_dir: ${METADATA.repo_dir}"
                        sh "pwd && ls -l ${METADATA.repo_dir}/"
                        def parallelSteps = [:]
                        METADATA.modifiedDirs.each { dir ->
                            parallelSteps["create-dockerfile-${dir}"] = {
                                dockerUtils.createWebDockerfile(dir, METADATA)
                            }
                        }
                        parallel parallelSteps
                        sh "pwd && ls -la"
                        stash includes: "docker-data/**", name: "docker-data-dist", allowEmpty: true, useDefaultExcludes: false
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
                        METADATA.modifiedDirs.each { dir ->
                            echo "dir: ${dir}"
                            String region = env.DOCKER_REGISTRY_HOST_TOKYO.tokenize('.')[-3].toLowerCase()
                            def repoName = "${METADATA.GROUP_NAME}/${dir}"
                            echo "repoName: ${repoName}"
                            ECR.createRepository(region, repoName, null)
                        }
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
                        script {
                            sh "pwd && ls -la"
                            unstash "plaud-web-dist"
                            unstash "docker-data-dist"
                            sh "ls -la"
                            // def parallelSteps = [:]
                            METADATA.modifiedDirs.each { dir ->
                                // parallelSteps["create-dockerfile-${dir}"] = {
                                def tags = "${env.DOCKER_REGISTRY_HOST_TOKYO}/${METADATA.GROUP_NAME}/${dir}:${METADATA.BUILD_TAG}"
                                echo "tags: ${tags}"
                                sh "ls -la docker-data/${dir}"
                                // sh "sleep 180"
                                dockerUtils.kanikoPush(tags, "docker-data/${dir}")
                                // }
                            }
                            // parallel parallelSteps
                            // sh 'sleep 180'
                        }
                        
                    }
                }
            }
            stage("Call CD Pipeline") {
                when {
                    equals expected: "false",
                    actual: METADATA.IS_DOCKER
                }
                steps {
                    script{
                        sh "echo 'Call CD Pipeline'"
                        // 调用 CD 流水线
                        // def cdPipelineUrl = 'cd-web'
                        // def buildEnv = ''
                        // // 根据分支名称设置构建环境
                        // if (METADATA.branchName.startsWith('release')) {
                        //     buildEnv = 'uat'
                        // } else if (METADATA.branchName.startsWith('feature')) {
                        //     buildEnv = 'qa'
                        // }
                        // // 调用CD流水线并传入环境参数
                        // build job: cdPipelineUrl, parameters: [
                        //     string(name: 'ENV', value: buildEnv)
                        // ]
                    }
                }
            }
        }
    }
}

