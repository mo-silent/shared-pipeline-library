#!groovy
package org

import java.text.SimpleDateFormat
import jenkins.model.Jenkins as JavaJenkins
import hudson.model.Job

// 设置当次构建显示名称
def setJobDisplayName(String jobType, Map METADATA) {
    String buildNum = "v" + env.BUILD_NUMBER.trim()
    String branch = METADATA.branchName ?: 'null'

    long unixTimestamp = currentBuild.startTimeInMillis
    String startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTimestamp)

    switch (jobType?.toUpperCase()) {
        case ~/(?i)CD|SECRET/:
            currentBuild.displayName = buildNum + "-" + startTime
            break

        default:
            currentBuild.displayName = buildNum + "-" + startTime + "-" + branch
    }

    return currentBuild.displayName
}