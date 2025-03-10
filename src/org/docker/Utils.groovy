#!groovy
package org.docker

def createWebDockerfile(String path, Map METADATA){
    sh """
        ls -la ${METADATA.repo_dir}/
        echo "***INFO: The path is ${METADATA.repo_dir}/${path}"
        // 读取package.json中的name字段作为publicPath
        // 使用jq命令安全地解析package.json并获取name字段
        publicPath=\$(jq -r '.name' ${METADATA.repo_dir}/${path}/package.json)
        echo "\${publicPath}" 

        cd Dockerfile/plaud-web/
        mkdir -p ../data/${path}
        // 使用sh命令复制nginx模板文件
        sh "cp nginx.template ../data/${path}/nginx.template"
        
        # 读取当前目录的Dockerfile内容
        // 根据path参数选择不同的Dockerfile模板
        dockerfile_content=\$(if [ "${path}" = "editor" ]; then cat editor_Dockerfile; else cat Dockerfile; fi)
        
        # 使用 sed 替换\$path为传入的 path 参数
        # echo "\${dockerfile_content}" | sed -e "s/\\\$env/${METADATA.packegePUBLISH_PATH}/g" -e "s/\\\$path/\${publicPath}/g" > ../data/${path}/Dockerfile.tmp
        sed -e "s/\\\$path/\${publicPath}/g" > ../data/${path}/Dockerfile.tmp
        
        # 覆盖原始Dockerfile
        mv ../data/${path}/Dockerfile.tmp ../data/${path}/Dockerfile
    """
}