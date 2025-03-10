#!groovy
package org.docker

def createWebDockerfile(String path, String buildEnv){
    sh """
        cd Dockerfile/plaud-web/
        # 读取当前目录的Dockerfile内容
        dockerfile_content=\$(cat Dockerfile)
        
        # 使用 sed 替换\$path为传入的 path 参数
        echo "\${dockerfile_content}" | sed "s/\\\$path/${path}/g; s/\\\$env/${buildEnv}/g" > Dockerfile.tmp
        
        # 覆盖原始Dockerfile
        mv Dockerfile.tmp Dockerfile
    """
}