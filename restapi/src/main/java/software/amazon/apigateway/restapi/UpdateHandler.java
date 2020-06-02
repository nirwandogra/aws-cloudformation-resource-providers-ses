package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigateway.model.Op;
import software.amazon.awssdk.services.apigateway.model.PatchOperation;
import software.amazon.awssdk.services.apigateway.model.PutRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiRequest;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.apiGatewayClient;
import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.execute;
import static software.amazon.apigateway.restapi.ApiGatewayUtils.   getTypesToCreateOrRemove;
import static software.amazon.apigateway.restapi.ApiGatewayUtils.newPatchOperations;
import static software.amazon.apigateway.restapi.ApiGatewayUtils.useImportApi;

public class UpdateHandler extends BaseHandler<CallbackContext> {
    public static final Gson gson = new Gson();
    private final static String REST_API_DEFAULT_TYPE = "EDGE";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel resourceModel = request.getDesiredResourceState();

        if (useImportApi(resourceModel)) {
            resourceModel.setId(execute(proxy, resourceModel, Action.UPDATE,
                getPutRestApiRequest(resourceModel, proxy, logger), apiGatewayClient::putRestApi,
                request.getLogicalResourceIdentifier(), logger));
        }
        else {
            resourceModel.setId(execute(proxy, resourceModel, Action.UPDATE,
                getUpdateRestApiRequest(resourceModel, request.getPreviousResourceState()),
                apiGatewayClient::updateRestApi,
                request.getLogicalResourceIdentifier(), logger));
        }

        ApiGatewayClientWrapper.updateTags(resourceModel, proxy);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(resourceModel)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private UpdateRestApiRequest getUpdateRestApiRequest(
        ResourceModel currentResource,
        ResourceModel previousResource) {
        final Map<String, Object> attributesToReplace = new HashMap<>();
        final Map<String, Object> attributesToRemove = new HashMap<>();
        final List<String> previousTypes = (previousResource == null) ?
            new ArrayList<>() : previousResource.getBinaryMediaTypes();
        final List<String> currentTypes = (currentResource == null) ?
            new ArrayList<>() : currentResource.getBinaryMediaTypes();

        final List<String> typesToCreate = getTypesToCreateOrRemove(previousTypes, currentTypes, true);
        final List<String> typesToRemove = getTypesToCreateOrRemove(previousTypes, currentTypes, false);

        attributesToReplace.put("/apiKeySource", currentResource.getApiKeySourceType());
        attributesToReplace.put("/description", currentResource.getDescription());
        attributesToReplace.put("/name", currentResource.getName());
        attributesToReplace.put("/minimumCompressionSize", currentResource.getMinimumCompressionSize());
        attributesToReplace.put("/policy", currentResource.getPolicy());

        final String previousEndpointType = getEndpointType(previousResource);
        final String currentEndpointType = getEndpointType(currentResource);
        if (!Objects.equals(previousEndpointType, currentEndpointType)) {
            if (REST_API_DEFAULT_TYPE.equals(currentEndpointType)) {
                attributesToReplace.put("/endpointConfiguration/types/REGIONAL", currentEndpointType);
            }
            else {
                attributesToReplace.put("/endpointConfiguration/types/EDGE", currentEndpointType);
            }
        }

        if (CollectionUtils.isNotEmpty(typesToCreate)) {
            typesToCreate.forEach(type -> attributesToReplace.put("/binaryMediaTypes/" + type, type));
        }
        if (CollectionUtils.isNotEmpty(typesToRemove)) {
            typesToRemove.forEach(type -> attributesToRemove.put("/binaryMediaTypes/" + type, type));
        }

        List<PatchOperation> patchOp = newPatchOperations(attributesToReplace, Op.REPLACE);
        List<PatchOperation> removeOp = newPatchOperations(attributesToRemove, Op.REMOVE);

        patchOp.addAll(removeOp);

        Collection<PatchOperation> patchOperations = patchOp;

        return UpdateRestApiRequest.builder().restApiId(currentResource.getId())
            .patchOperations((patchOperations)).build();
    }

    private String getEndpointType(ResourceModel resource) {
        if (resource != null && resource.getEndpointConfiguration() != null &&
            CollectionUtils.isNotEmpty(resource.getEndpointConfiguration().getTypes())) {
            //User can only specify one type
            return resource.getEndpointConfiguration().getTypes().get(0);
        }
        else {
            return REST_API_DEFAULT_TYPE;
        }
    }

    public PutRestApiRequest getPutRestApiRequest(
        ResourceModel resourceModel,
        AmazonWebServicesClientProxy proxy,
        Logger logger) {
        String body = null;
        if (resourceModel.getBody() != null) {
            body = gson.toJson(resourceModel.getBody());
        }
        else if (resourceModel.getBodyS3Location() != null) {
            body = ApiGatewayUtils.getBodyFromS3(resourceModel.getBodyS3Location(), proxy, logger);
        }

        @SuppressWarnings("unchecked")
        Map<String,String> parameters = gson.fromJson(gson.toJson(resourceModel.getParameters()), Map.class);

        return PutRestApiRequest.builder()
            .restApiId(resourceModel.getId())
            .failOnWarnings(resourceModel.getFailOnWarnings())
            .parameters(resourceModel.getParameters() != null ? parameters : null)
            .body(SdkBytes.fromByteArray(body.getBytes(StandardCharsets.UTF_8)))
            .mode(resourceModel.getMode()).build();
    }
}
