#!groovy
package org

import java.text.SimpleDateFormat
import jenkins.model.Jenkins as JavaJenkins
import hudson.model.Job

// set current build display name
def setJobDisplayName(String jobType, Map METADATA) {
    String buildNum = "v" + env.BUILD_NUMBER
    String branch = METADATA.branchName ?: 'null'
    println "buildNum: ${buildNum}"

    long unixTimestamp = currentBuild.startTimeInMillis
    println "unixTimestamp: ${unixTimestamp}"
    String startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(unixTimestamp)
    println "startTime: ${startTime}"

    switch (jobType?.toUpperCase()) {
        case ~/(?i)CD|SECRET/:
            currentBuild.displayName = buildNum + "-" + startTime
            break

        default:
            currentBuild.displayName = buildNum + "-" + startTime + "-" + branch
    }

    return currentBuild.displayName
}