package software.amazon.apigateway.restapi;

import software.amazon.awssdk.services.apigateway.model.GetRestApiRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.getRestApi;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        getRestApi(proxy, model, GetRestApiRequest.builder().restApiId
                (model.getId()).build(), logger);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
