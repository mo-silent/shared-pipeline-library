#!groovy
package org.aws
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.EcrClientBuilder
import software.amazon.awssdk.services.ecr.model.CreateRepositoryRequest
import software.amazon.awssdk.services.ecr.model.ResourceAlreadyExistsException
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.StsClientBuilder
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest
import java.util.Base64
import java.util.Arrays

def createRepository(String region, String repoName, String roleArn) {    
    EcrClient ecrClient
    if (roleArn) {
        // STS assumeRole
        println "***INFO: Assume Role ${roleArn}"
        AssumeRoleRequest assumeRole = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName("jenkins-slave-login-ecr")
                .build()

        StsClient sts = StsClientBuilder.standard()
                .region(Region.of(region))
                .build()
        def credentials = sts.assumeRole(assumeRole).credentials()

        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
                credentials.accessKeyId(),
                credentials.secretAccessKey(),
                credentials.sessionToken())

        ecrClient = EcrClientBuilder.standard()
                .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
                .region(Region.of(region))
                .build()
    } else {
        println "***INFO: Using IRSA credentials"
        ecrClient = EcrClientBuilder.standard()
                .region(Region.of(region))
                .build()
    }
    println "***INFO: AWS ECR Client: ${ecrClient}"

    // 获取授权令牌
    println "***INFO: AWS ECR Getting Authorization Token."
    GetAuthorizationTokenResponse response = ecrClient.getAuthorizationToken()
    String token = response.authorizationData().get(0).authorizationToken()
    String[] ecrCreds = new String(Base64.getDecoder().decode(token), 'UTF-8').split(':')
    def result = Arrays.asList(ecrCreds)
    println "***INFO: AWS ECR Authorization Token: ${result}"

    // 创建仓库
    CreateRepositoryRequest createRequest = CreateRepositoryRequest.builder()
            .name(repoName)
            .build()
    try {
        println "***INFO: AWS ECR Creating Repository ${repoName}."
        ecrClient.createRepository(createRequest)
    } catch (ResourceAlreadyExistsException e) {
        println "***INFO: AWS ECR Repository ${repoName} already exists."
    }

    return result
}