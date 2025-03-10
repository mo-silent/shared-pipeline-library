#!groovy
package org.docker

def createWebDockerfile(String path, Map METADATA){
    sh """
        cd Dockerfile/plaud-web/
        mkdir -p /data/${path}
        // 读取package.json中的name字段作为publicPath
        publicPath=\$(cat ${METADATA.repo_dir}/${path}/package.json | grep "name" | cut -d'"' -f4)
        echo "\${publicPath}"
        # 读取当前目录的Dockerfile内容
        // 根据path参数选择不同的Dockerfile模板
        dockerfile_content=\$(if [ "${path}" = "editor" ]; then cat editor_Dockerfile; else cat Dockerfile; fi)
        
        # 使用 sed 替换\$path为传入的 path 参数
        # echo "\${dockerfile_content}" | sed -e "s/\\\$env/${METADATA.packegePUBLISH_PATH}/g" -e "s/\\\$path/\${publicPath}/g" > ${path}/Dockerfile.tmp
        sed -e "s/\\\$path/\${publicPath}/g" > ${path}/Dockerfile.tmp
        
        # 覆盖原始Dockerfile
        mv /data/${path}/Dockerfile.tmp /data/${path}/Dockerfile
    """
}