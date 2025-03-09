#!groovy
package org
import groovy.json.JsonOutput

/**
* @author:  Silent.Mo
* @description: 获取项目参数并进行参数检查
* @return: Map
*/
def getProjectParams(String jobType, Map config) {

    // 将参数值统一转换为小写，避免后续因大小写匹配错误
    config.each {
        if (it.key != "BRANCH_LIST") {
            // print(it.key)
            if (it.key ==~ /^(?!DOCKER_FILE|HELM_TEMPLATE|DEPLOY_ENV).*$/) {
                config.put(it.key, it.value.toLowerCase())
            }
        }

    }

    // 进入CI参数判断流程
    if ( jobType.toUpperCase() == 'CI' ) {
        // GitUrl地址解析断言
        // 待补全
        assert (config.GIT_REPO && (config.GIT_REPO != null)): "ERROR: key is not exist or null"

        // 代码类型检查
        assert (config.CODE_TYPE && (config.CODE_TYPE != null)): "ERROR: key is not exist or null"
        assert config?.CODE_TYPE ==~ /vue/: "ERROR: CODE_TYPE is not allow type"

        // 构建类型检查
        assert (config.BUILD_TYPE && (config.BUILD_TYPE != null)): "ERROR: key is not exist or null"
        assert config?.BUILD_TYPE ==~ /yarn/: "ERROR: BUILD_TYPE is not allow type"
        config.IS_DOCKER = config.IS_DOCKER ? config.IS_DOCKER : 'false'
    }

    // 进入CD参数判断流程
    if ( jobType.toUpperCase() == 'CD' ) {

    }


    // 打印初始化参数
    config.each {
        println(it.key + ": " + it.value)
    }

    
    return config
}
