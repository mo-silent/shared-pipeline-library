#!groovy
package org.aws

def createRepository(String region, String repoName, String roleArn) {
    if (roleArn) {
        sh "aws sts assume-role --role-arn ${roleArn} --role-session-name jenkins-slave-login-ecr --region ${region} > creds.json"
        def creds = readJSON file: 'creds.json'
        withEnv(["AWS_ACCESS_KEY_ID=${creds.Credentials.AccessKeyId}",
                 "AWS_SECRET_ACCESS_KEY=${creds.Credentials.SecretAccessKey}",
                 "AWS_SESSION_TOKEN=${creds.Credentials.SessionToken}"]) {
            sh "aws ecr get-login-password --region ${region} > token.txt"
            sh "aws ecr create-repository --repository-name ${repoName} --region ${region} || echo 'Repository already exists'"
        }
    } else {
        sh "aws ecr get-login-password --region ${region} > token.txt"
        sh "aws ecr create-repository --repository-name ${repoName} --region ${region} || echo 'Repository already exists'"
    }
    def token = readFile('token.txt').trim()
    return [token] // 简化返回值
}