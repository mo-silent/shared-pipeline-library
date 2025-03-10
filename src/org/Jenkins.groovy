#!groovy
package org

import java.text.SimpleDateFormat
import jenkins.model.Jenkins as JavaJenkins
import hudson.model.Job

// set current build display name
def setJobDisplayName(String jobType, Map METADATA) {
    // 获取Jenkins环境变量
    def env = JavaJenkins.getInstance().getGlobalNodeProperties()?.get(hudson.slaves.EnvironmentVariablesNodeProperty.class)?.getEnvVars()
    
    String buildNum = "v" + (env?.BUILD_NUMBER?.trim() ?: '0')
    String branch = METADATA.branchName ?: 'null'

    // 获取当前构建实例
    def currentBuild = JavaJenkins.getInstance().getItemByFullName(env.JOB_NAME)?.getBuildByNumber(Integer.parseInt(env.BUILD_NUMBER))
    
    long unixTimestamp = currentBuild?.getStartTimeInMillis() ?: System.currentTimeMillis()
    String startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTimestamp)

    String displayName
    switch (jobType?.toUpperCase()) {
        case ~/(?i)CD|SECRET/:
            displayName = buildNum + "-" + startTime
            break

        default:
            displayName = buildNum + "-" + startTime + "-" + branch
    }

    if (currentBuild) {
        currentBuild.setDisplayName(displayName)
    }

    return displayName
}