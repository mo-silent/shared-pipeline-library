#!groovy
package org
import org.*
import java.util.regex.*

// Git代码迁出
def checkoutSource( String repoUrl, String branch, String gitCredentialsId ){
    withCredentials([sshUserPrivateKey(
        credentialsId: gitCredentialsId,  // 凭证的 ID
        keyFileVariable: 'SSH_KEY_FILE',  // 私钥文件的路径
        usernameVariable: 'silent.mo'  // 用户名
    )]) {
        // 设置 Git 使用 SSH Key
        sh '''
            mkdir -p ~/.ssh
            cp $SSH_KEY_FILE ~/.ssh/id_rsa
            chmod 600 ~/.ssh/id_rsa
            ssh-keyscan github.com >> ~/.ssh/known_hosts
            echo $SSH_KEY_FILE
        '''

        checkout([
            $class: 'GitSCM',
            branches: [[name: branch]],  // 分支名称
            extensions: [],
            userRemoteConfigs: [[
                url: repoUrl,  // Git 仓库的 HTTPS URL
                credentialsId: gitCredentialsId  // 凭证的 ID
            ]]
        ])
        sh "pwd && ls -l"
    }
    // Map scmVars = checkout([
    //     $class: 'GitSCM',
    //     userRemoteConfigs: [[url: repoUrl, credentialsId: gitCredentialsId]],
    //     branches: [[name: branch]],
    //     extensions: [[$class: 'CheckoutOption', timeout: 30], [$class: 'CleanBeforeCheckout', deleteUntrackedNestedRepositories: true]],
    //     submoduleCfg: [],
    //     doGenerateSubmoduleConfigurations: false
    //     ])
    

    
}