#!groovy
def call(body) {
    pipeline {
        agent {
            kubernetes {
                yaml podtemlateCI()
            }
        }
        stages  {
            stage("get_evn") {
                steps {
                    script {
                        // sh 'docker ps'
                        // echo "${env.all}"
                        def branchName = scm.branches[0].name
                        if (branchName.contains("*/")) {
                            branchName = branchName.split("\\*/")[1]
                        }
                        echo "分支名称: ${branchName}"
                        
                        def changedFiles = getChangedFiles()
                        def modifiedDirs = changedFiles.collect { it.split('/')[0] }.unique()
                        echo "修改的一级目录: ${modifiedDirs}"
                        sh """
                            echo ***INFO：当前目录是 `pwd` && echo ***INFO：列出target目录文件 && ls -lha
                        """
                        sh 'mkdir -p dist && echo "Build output" > dist/output.txt'
                        stash includes: 'dist/**', name: 'dist-stash'
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
                    unstash 'dist-stash'
                    sh 'cat dist/output.txt'
                    sh 'sleep 18000'
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

