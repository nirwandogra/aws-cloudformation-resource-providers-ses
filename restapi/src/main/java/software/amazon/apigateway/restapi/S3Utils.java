package software.amazon.apigateway.restapi;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

public class S3Utils {
    private static S3ClientWrapper s3ClientWrapper = new S3ClientWrapper();

    public static String getBodyFromS3(
        S3Location s3Location,
        AmazonWebServicesClientProxy proxy,
        Logger logger) {
        try {
            GetObjectRequest request = GetObjectRequest.builder().bucket(s3Location.getBucket())
                .ifMatch(s3Location.getETag())
                .key(s3Location.getKey()).build();

            logger.log("Fetching file from S3 with request:" + request.toString());

            return s3ClientWrapper.getObjectRequest(proxy, request).asUtf8String();
        } catch (Throwable t) {
            logger.log("Error while fetching file from S3 " + t.getMessage());
            throw new CfnInvalidRequestException("Unable to retrieve file from S3", t);
        }
    }
}
