#!groovy
package org.docker

def createWebDockerfile(String path, Map METADATA){
    sh """
        cd Dockerfile/plaud-web/
        mkdir -p ${path}
        # 读取当前目录的Dockerfile内容
        // 根据path参数选择不同的Dockerfile模板
        dockerfile_content=\$(if [ "${path}" = "editor" ]; then cat editor_Dockerfile; else cat Dockerfile; fi)
        
        # 使用 sed 替换\$path为传入的 path 参数
        echo "\${dockerfile_content}" | sed "s/\\\$env/${METADATA.packegePUBLISH_PATH}/g" > ${path}/Dockerfile.tmp
        
        # 覆盖原始Dockerfile
        mv ${path}/Dockerfile.tmp ${path}/Dockerfile
    """
}