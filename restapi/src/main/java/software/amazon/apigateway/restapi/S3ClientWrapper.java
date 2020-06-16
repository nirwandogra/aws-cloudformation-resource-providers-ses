package software.amazon.apigateway.restapi;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

public class S3ClientWrapper {
    S3Client s3Client = S3Client.builder().build();

    public ResponseBytes<GetObjectResponse> getObjectRequest(
        AmazonWebServicesClientProxy proxy,
        GetObjectRequest request) {
        return proxy.injectCredentialsAndInvokeV2Bytes
            (request, s3Client::getObjectAsBytes);
    }
}
