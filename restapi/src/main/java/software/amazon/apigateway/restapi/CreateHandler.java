package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.ImportRestApiRequest;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.apiGatewayClient;
import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.execute;
import static software.amazon.apigateway.restapi.ApiGatewayUtils.getBodyFromS3;
import static software.amazon.apigateway.restapi.ApiGatewayUtils.useImportApi;

public class CreateHandler extends BaseHandler<CallbackContext> {
    public static final Gson gson = new Gson();

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel resourceModel = request.getDesiredResourceState();

        if (useImportApi(resourceModel)) {
            resourceModel.setId(execute(proxy, resourceModel, Action.CREATE,
                getImportRestApiRequest(resourceModel, proxy, logger), apiGatewayClient::importRestApi,
                request.getLogicalResourceIdentifier(), logger));
        }
        else {
            resourceModel.setId(execute(proxy, resourceModel, Action.CREATE,
                getCreateRestApiRequest(resourceModel), apiGatewayClient::createRestApi,
                request.getLogicalResourceIdentifier(), logger));
        }


        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(resourceModel)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    public CreateRestApiRequest getCreateRestApiRequest(ResourceModel resource) {
        final CreateRestApiRequest request = CreateRestApiRequest.builder()
            .apiKeySource(resource.getApiKeySourceType())
            .binaryMediaTypes(resource.getBinaryMediaTypes())
            .cloneFrom(resource.getCloneFrom())
            .description(resource.getDescription())
            .endpointConfiguration(ApiGatewayUtils.translateEndpointConfiguration(resource.getEndpointConfiguration()))
            .minimumCompressionSize(resource.getMinimumCompressionSize())
            .name(resource.getName())
            .policy(resource.getPolicy() != null ? gson.toJson(resource.getPolicy()) : null).build();
        return request;
    }

    public ImportRestApiRequest getImportRestApiRequest(
        ResourceModel resource, AmazonWebServicesClientProxy proxy, Logger logger) {
        String body = null;
        if (resource.getBody() != null) {
            body = gson.toJson(resource.getBody());
        }
        else if (resource.getBodyS3Location() != null) {
            body = getBodyFromS3(resource.getBodyS3Location(), proxy, logger);
            logger.log("this is the body received from s3" + body);
        }

        logger.log("this is the body received from s3" + body);

        @SuppressWarnings("unchecked")
        Map<String, String> parameters = gson.fromJson(gson.toJson(resource.getParameters()), Map.class);

        return ImportRestApiRequest.builder()
            .failOnWarnings(resource.getFailOnWarnings())
            .parameters(resource.getParameters() != null ? parameters : null)
            .body(SdkBytes.fromByteArray(body.getBytes(StandardCharsets.UTF_8))).build();
    }
}
