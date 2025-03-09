#!groovy
package org.aws

def createRepository(String region, String repoName, String roleArn) {
    if (roleArn) {
        sh "aws sts assume-role --role-arn ${roleArn} --role-session-name jenkins-slave-login-ecr --region ${region} > creds.json"
    } else {
        sh "aws sts get-caller-identity --output json > creds.json"
    }
    sh "cat creds.json"
    def creds = readJSON file: 'creds.json'
    withEnv(["AWS_ACCESS_KEY_ID=${creds.Credentials.AccessKeyId}",
                "AWS_SECRET_ACCESS_KEY=${creds.Credentials.SecretAccessKey}",
                "AWS_SESSION_TOKEN=${creds.Credentials.SessionToken}"]) {
        sh "aws ecr get-login-password --region ${region} > token.txt"
        def repoExists = sh(script: "aws ecr describe-repositories --repository-names ${repoName} --region ${region}", returnStatus: true) == 0
        if (!repoExists) {
            sh "aws ecr create-repository --repository-name ${repoName} --region ${region}"
        }
    }
    def token = readFile('token.txt').trim()
    return [token] // 简化返回值
}