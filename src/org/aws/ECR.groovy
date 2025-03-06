#!groovy
package org.aws
// import com.amazonaws.auth.BasicAWSCredentials
// import com.amazonaws.auth.WebIdentityTokenCredentialsProvider
// import com.amazonaws.auth.BasicSessionCredentials
// import com.amazonaws.auth.AWSStaticCredentialsProvider
// import com.amazonaws.services.ecr.AmazonECRClient
// import com.amazonaws.services.ecr.AmazonECRClientBuilder
// import com.amazonaws.services.ecr.model.CreateRepositoryRequest
// import com.amazonaws.services.ecr.model.CreateRepositoryResult
// import com.amazonaws.services.ecr.model.RepositoryAlreadyExistsException
// import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest
// import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult
// import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
// import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
// import com.amazonaws.services.securitytoken.AWSSecurityTokenService
// import com.amazonaws.regions.Regions


// def createRepository(String region, String repoName, String roleArn) {    
//     println "***INFO: AWS ECR Creating Repository ${repoName}."
//     println "***INFO: AWS ECR Region: ${region}"
//     AmazonECRClient ecrClient
//     // String sa_roleArn = System.getenv("AWS_ROLE_ARN")
//     // String tokenFile = System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE")
//     def sa_roleArn = sh(script: 'echo $AWS_ROLE_ARN', returnStdout: true).trim()
//     def tokenFile = sh(script: 'echo $AWS_WEB_IDENTITY_TOKEN_FILE', returnStdout: true).trim()
//     if (!sa_roleArn || !tokenFile) {
//         throw new Exception("IRSA 环境变量未正确配置: AWS_ROLE_ARN 或 AWS_WEB_IDENTITY_TOKEN_FILE 缺失")
//     }
//     println "***INFO: Using IRSA credentials: ${sa_roleArn}"
//     println "***INFO: Using IRSA token file: ${tokenFile}"
//     // 显式配置 WebIdentityTokenCredentialsProvider
//     WebIdentityTokenCredentialsProvider credentialsProvider = WebIdentityTokenCredentialsProvider.builder()
//         .roleArn(sa_roleArn)
//         .roleSessionName("jenkins-session")
//         .webIdentityTokenFile(tokenFile)
//         .build()
//     if (roleArn) {
//         // sts assumeRole
//         println "***INFO: Assume Role ${roleArn}"
//         AssumeRoleRequest assumeRole = new AssumeRoleRequest().withRoleArn(roleArn).withRoleSessionName("jenkins-slave-login-ecr");
        
//         AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard().withRegion(region).withCredentials(credentialsProvider).build();
//         credentials = sts.assumeRole(assumeRole).getCredentials();

//         BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
//                 credentials.getAccessKeyId(),
//                 credentials.getSecretAccessKey(),
//                 credentials.getSessionToken());

//         ecrClient = AmazonECRClientBuilder.standard()
//                 .withRegion(region)
//                 .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
//                 .build()
//     } else {
//         println "***INFO: AWS ECR Region: ${region}"
//         ecrClient = AmazonECRClientBuilder.standard()
//             .withRegion(Regions.fromName("us-west-2"))
//             .withCredentials(credentialsProvider)  // 使用 WebIdentityTokenCredentialsProvider
//             .build()
//     }
//     ecrClient.setRegion(com.amazonaws.regions.Region.getRegion(com.amazonaws.regions.Regions.fromName(region)))
//     println "***DEBUG: Explicitly set ECR client region to ${region}"
//     try {
//         GetAuthorizationTokenRequest request = new GetAuthorizationTokenRequest()
//         println "***INFO: AWS ECR Getting Authorization Token."
//         GetAuthorizationTokenResult response = ecrClient.getAuthorizationToken(request)
//         def token = response.getAuthorizationData().get(0).getAuthorizationToken()
//         String[] ecrCreds = new String(token.decodeBase64(), 'UTF-8').split(':')
//         def result = java.util.Arrays.asList(ecrCreds)
//         println "***INFO: AWS ECR Authorization Token: ${result}"
        
//         CreateRepositoryRequest createRequest = new CreateRepositoryRequest().withRepositoryName(repoName)
//         try {
//             println "***INFO: AWS ECR Creating Repository ${repoName}."
//             CreateRepositoryResult createResult = ecrClient.createRepository(createRequest)
//         } catch (RepositoryAlreadyExistsException e) {
//             println "***INFO: AWS ECR Repository ${repoName} already exists."
//         }
//         return result
//     } catch (Exception e) {
//         println "***ERROR: ECR operation failed: ${e.getMessage()}"
//         throw e
//     }

//     return result
// }

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