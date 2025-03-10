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
        '''
        repo_dir = getRepoName(repoUrl)
        checkout([
            $class: 'GitSCM',
            branches: [[name: branch]],  // 分支名称
            extensions: [[$class: 'CheckoutOption', timeout: 30], [$class: 'CleanBeforeCheckout', deleteUntrackedNestedRepositories: true],  [$class: 'RelativeTargetDirectory', relativeTargetDir: repo_dir]],
            userRemoteConfigs: [[
                url: repoUrl,  // Git 仓库的 HTTPS URL
                credentialsId: gitCredentialsId  // 凭证的 ID
            ]]
        ])
        return repo_dir
    }
    
}

def getRepoName(String repoUrl) {
    // 去掉 URL 末尾的 .git（如果存在）
    if (repoUrl.endsWith('.git')) {
        repoUrl = repoUrl.substring(0, repoUrl.length() - 4)
    }
    // 提取最后一个 / 之后的部分
    return repoUrl.substring(repoUrl.lastIndexOf('/') + 1)
}

// 构建应用
def build(Map METADATA) {
    
    switch( METADATA.BUILD_TYPE )
      {
        case "none":
            println "***INFO：The code doesn't need compile."
            break
        case "yarn":
            println "***INFO: Yarn bulid."
            METADATA.modifiedDirs.each { dir ->
                println "***INFO: Bulid branch ${dir}"
                
                sh '''
                    export PATH=$PATH:/root/.nvm/versions/node/v23.9.0/bin
                    yarn install
                '''
                sh "/root/.nvm/versions/node/v23.9.0/bin/yarn build --scope ${dir}"
                if ( METADATA.modifiedDirs[i] == "web" && !METADATA.modifiedDirs.contains("editor")){
                    sh "/root/.nvm/versions/node/v23.9.0/bin/yarn build --scope editor"
                    stash includes: "editor/dist/**", name: "editor-dist"
                }
                if ( METADATA.modifiedDirs[i] == "editor" && !METADATA.modifiedDirs.contains("web")){
                    sh "/root/.nvm/versions/node/v23.9.0/bin/yarn build --scope web"
                    stash includes: "web/dist/**", name: "web-dist"
                }
                stash includes: "${dir}/dist/**", name: "${dir}-dist"
            }
            break
        default:
            echo "***ERROR: Not support BUILD_TYPE, exit.."
            sh "exit 1"
      }
}
