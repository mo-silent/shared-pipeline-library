#!groovy
package org
import java.util.regex.*

// Git代码迁出
def checkoutSource( String repoUrl, String branch, String gitCredentialsId ){
    def log = new Log()

    log.info ("Git Repo: ${repoUrl}, Branch: ${branch}")
    Map scmVars = checkout([
        $class: 'GitSCM',
        userRemoteConfigs: [[url: repoUrl, credentialsId: gitCredentialsId]],
        branches: [[name: branch]],
        extensions: [[$class: 'CheckoutOption', timeout: 30], [$class: 'CleanBeforeCheckout', deleteUntrackedNestedRepositories: true]],
        submoduleCfg: [],
        doGenerateSubmoduleConfigurations: false
        ])
    println scmVars

    // 获取GIT_PROJECT_ID（取url中路径段）
    gitUrl = scmVars.GIT_URL
    if(gitUrl.indexOf('git@') >= 0) {
        // 如果是git@格式，转换为http格式
        gitUrl = 'http://' + gitUrl.split('@')[1].replaceAll(':','/')
    }
    URL gitUrl = new URL(gitUrl)
    env.GIT_PROJECT_ID = gitUrl.getPath().replace(".git","").substring(1)

    // 获取GIT_COMMIT
    // env.GIT_COMMIT_LONG = sh ( script:'git rev-parse HEAD', returnStdout: true ).trim()
    env.GIT_COMMIT_LONG = scmVars.GIT_COMMIT
    env.GIT_COMMIT_SHORT = scmVars.GIT_COMMIT.substring(0,8)
    env.GIT_BRANCH_NAME = scmVars.GIT_BRANCH.replace("origin/","")

    println """
    ***INFO：Add checkout metadata to the global environment variable
    ***INFO：GIT_PROJECT_ID：${env.GIT_PROJECT_ID}
    ***INFO：GIT_BRANCH_NAME：${branch}
    ***INFO：GIT_COMMIT_LONG：${env.GIT_COMMIT_LONG}
    ***INFO：GIT_COMMIT_SHORT：${env.GIT_COMMIT_SHORT}
    """.stripIndent()

    // 获取当前路径与列出目录文件
    sh '''
        set +x
        echo ***INFO：Current directory path：$(pwd)
        echo ***INFO：Current directory size：$(du -hd0 | awk '{print $1}')
        echo -e ***INFO：List files in the current directory \n ls -lha --time-style=long-iso | tail -n +2
       '''
}