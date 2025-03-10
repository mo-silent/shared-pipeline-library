#!/bin/bash

METADATA=$1
path=$2
# 检查必要的参数是否存在
if [ -z "$METADATA" ] || [ -z "$path" ]; then
    echo "错误: 缺少必要的参数 METADATA 或 path"
    exit 1
fi

# 显示仓库目录内容
ls -la "${METADATA}"
echo "***INFO: The path is ${METADATA}/${path}"

# 读取package.json中的name字段作为publicPath
# 使用jq命令安全地解析package.json并获取name字段
if [ ! -f "${METADATA}/${path}/package.json" ]; then
    echo "错误: package.json 文件不存在于 ${METADATA}/${path}"
    exit 1
fi

publicPath=$(jq -r '.name' "${METADATA}/${path}/package.json")
if [ -z "$publicPath" ]; then
    echo "错误: 无法从package.json获取name字段"
    exit 1
fi
echo "${publicPath}"

# 获取当前目录并创建数据目录
dir_path=$(pwd)
mkdir -p "${dir_path}/data/${path}"

# 切换到Dockerfile目录并复制nginx模板
cd Dockerfile/plaud-web/ || exit 1
cp nginx.template "${dir_path}/data/${path}/nginx.template"

# 根据path参数选择不同的Dockerfile模板
if [ "${path}" = "editor" ]; then
    dockerfile_content=$(cat editor_Dockerfile)
else
    dockerfile_content=$(cat Dockerfile)
fi

# 替换Dockerfile中的变量并创建新的Dockerfile
echo "${dockerfile_content}" | sed -e "s/\$path/${publicPath}/g" > "${dir_path}/data/${path}/Dockerfile"