package org.aws
// import com.amazonaws.auth.BasicAWSCredentials
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


def createRepository(String region, String repoName, roleArn) {    
    // sts assumeRole
    AssumeRoleRequest assumeRole = new AssumeRoleRequest().withRoleArn(roleArn).withRoleSessionName("io-klerch-mp3-converter");

    AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard().withRegion(region).build();
    credentials = sts.assumeRole(assumeRole).getCredentials();

    BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
            credentials.getAccessKeyId(),
            credentials.getSecretAccessKey(),
            credentials.getSessionToken());

    AmazonECRClient ecrClient = AmazonECRClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
            .withRegion(region)
            .build()

    GetAuthorizationTokenRequest request = new GetAuthorizationTokenRequest()
    GetAuthorizationTokenResult response = ecrClient.getAuthorizationToken(request)
    //println response.getAuthorizationData()
    token = response.getAuthorizationData().get(0).getAuthorizationToken()
    String[] ecrCreds = new String(token.decodeBase64(), 'UTF-8').split(':')
    result = java.util.Arrays.asList(ecrCreds)

    CreateRepositoryRequest createRequest = new CreateRepositoryRequest().withRepositoryName(repoName)
    //println (createRequest)
    try {
        println "***INFO：AWS ECR Creating Repository ${repoName}."
        CreateRepositoryResult createResult = ecrClient.createRepository(createRequest)
    } catch (RepositoryAlreadyExistsException e) {
        println "***INFO：AWS ECR Repository ${repoName} already exists."
    }

    return result
}