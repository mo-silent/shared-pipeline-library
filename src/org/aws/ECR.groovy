#!groovy
package org.aws
// import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.ecr.AmazonECRClient
import com.amazonaws.services.ecr.AmazonECRClientBuilder
import com.amazonaws.services.ecr.model.CreateRepositoryRequest
import com.amazonaws.services.ecr.model.CreateRepositoryResult
import com.amazonaws.services.ecr.model.RepositoryAlreadyExistsException
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.AWSSecurityTokenService


def createRepository(String region, String repoName, String roleArn) {    
    AmazonECRClient ecrClient
    // String sa_roleArn = System.getenv("AWS_ROLE_ARN")
    // String tokenFile = System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE")
    def sa_roleArn = sh(script: 'echo $AWS_ROLE_ARN', returnStdout: true).trim()
    def tokenFile = sh(script: 'echo $AWS_WEB_IDENTITY_TOKEN_FILE', returnStdout: true).trim()
    if (!sa_roleArn || !tokenFile) {
        throw new Exception("IRSA 环境变量未正确配置: AWS_ROLE_ARN 或 AWS_WEB_IDENTITY_TOKEN_FILE 缺失")
    }
    println "***INFO: Using IRSA credentials: ${sa_roleArn}"
    println "***INFO: Using IRSA token file: ${tokenFile}"
    // 显式配置 WebIdentityTokenCredentialsProvider
    WebIdentityTokenCredentialsProvider credentialsProvider = new WebIdentityTokenCredentialsProvider(
        sa_roleArn,
        "jenkins-session",  // roleSessionName 可自定义
        tokenFile
    )
    if (roleArn) {
        // sts assumeRole
        println "***INFO: Assume Role ${roleArn}"
        AssumeRoleRequest assumeRole = new AssumeRoleRequest().withRoleArn(roleArn).withRoleSessionName("jenkins-slave-login-ecr");
        
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard().withRegion(region).withCredentials(credentialsProvider).build();
        credentials = sts.assumeRole(assumeRole).getCredentials();

        BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
                credentials.getAccessKeyId(),
                credentials.getSecretAccessKey(),
                credentials.getSessionToken());

        ecrClient = AmazonECRClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withRegion(region)
                .build()
    } else {
        ecrClient = AmazonECRClientBuilder.standard()
            .withRegion(region)
            .withCredentials(credentialsProvider)  // 使用 WebIdentityTokenCredentialsProvider
            .build()
    }
    println "***INFO: AWS ECR Client: ${ecrClient}"
    GetAuthorizationTokenRequest request = new GetAuthorizationTokenRequest()
    println "***INFO: AWS ECR Getting Authorization Token."
    GetAuthorizationTokenResult response = ecrClient.getAuthorizationToken(request)
    //println response.getAuthorizationData()
    token = response.getAuthorizationData().get(0).getAuthorizationToken()
    String[] ecrCreds = new String(token.decodeBase64(), 'UTF-8').split(':')
    result = java.util.Arrays.asList(ecrCreds)
    
    CreateRepositoryRequest createRequest = new CreateRepositoryRequest().withRepositoryName(repoName)
    //println (createRequest)
    try {
        println "***INFO: AWS ECR Creating Repository ${repoName}."
        CreateRepositoryResult createResult = ecrClient.createRepository(createRequest)
    } catch (RepositoryAlreadyExistsException e) {
        println "***INFO: AWS ECR Repository ${repoName} already exists."
    }

    return result
}