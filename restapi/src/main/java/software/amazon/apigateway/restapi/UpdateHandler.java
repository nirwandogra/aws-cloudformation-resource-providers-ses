package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigateway.model.Op;
import software.amazon.awssdk.services.apigateway.model.PatchOperation;
import software.amazon.awssdk.services.apigateway.model.PutRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiRequest;
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

import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.putRestApi;
import static software.amazon.apigateway.restapi.ApiGatewayClientWrapper.updateRestApi;
import static software.amazon.apigateway.restapi.RestApiUtils.getTypesToCreateOrRemove;
import static software.amazon.apigateway.restapi.RestApiUtils.newPatchOperations;
import static software.amazon.apigateway.restapi.RestApiUtils.useImportApi;
import static software.amazon.apigateway.restapi.RestApiUtils.validateRestApiResource;
import static software.amazon.apigateway.restapi.S3Utils.getBodyFromS3;

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

        validateRestApiResource(resourceModel);

        /*
         * APIGW Api can either be update by calling put rest api or directly via Update Rest Api.
         * We decide whether we want to call PutRestApi or UpdateRestApi
         * */
        if (useImportApi(resourceModel)) {
            resourceModel.setId(putRestApi(proxy, resourceModel, getPutRestApiRequest(resourceModel, proxy, logger),
                logger));
        }
        else {
            resourceModel.setId(updateRestApi(proxy, resourceModel, getUpdateRestApiRequest
                (resourceModel, request.getPreviousResourceState()), logger));
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

        final List<String> previousTypes = (previousResource == null) ?
            new ArrayList<>() : previousResource.getBinaryMediaTypes();

        final List<String> currentTypes = (currentResource == null) ?
            new ArrayList<>() : currentResource.getBinaryMediaTypes();

        List<PatchOperation> patchOp = newPatchOperations(getAttributesToReplace(currentResource,
            previousResource, getTypesToCreateOrRemove(previousTypes, currentTypes, true)), Op.REPLACE);

        List<PatchOperation> removeOp = newPatchOperations(getAttributesToRemove(getTypesToCreateOrRemove(
            previousTypes,
            currentTypes,
            false)), Op.REMOVE);

        patchOp.addAll(removeOp);

        Collection<PatchOperation> patchOperations = patchOp;

        return UpdateRestApiRequest.builder().restApiId(currentResource.getId())
            .patchOperations((patchOperations)).build();
    }

    private Map<String, Object> getAttributesToRemove(List<String> typesToRemove) {
        final Map<String, Object> attributesToRemove = new HashMap<>();
        if (CollectionUtils.isNotEmpty(typesToRemove)) {
            typesToRemove.forEach(type -> attributesToRemove.put("/binaryMediaTypes/" + type, type));
        }
        return attributesToRemove;
    }

    private Map<String, Object> getAttributesToReplace(
        ResourceModel currentResource, ResourceModel previousResource, List<String> typesToCreate) {
        final Map<String, Object> attributesToReplace = new HashMap<>();
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
        return attributesToReplace;
    }

    private String getEndpointType(ResourceModel resource) {
        if (resource != null && resource.getEndpointConfiguration() != null &&
            CollectionUtils.isNotEmpty(resource.getEndpointConfiguration().getTypes())) {
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

        /*
         * Extract the body either from BodyS3Location or Body itself
         * */
        if (resourceModel.getBody() != null) {
            body = gson.toJson(resourceModel.getBody());
        }
        else if (resourceModel.getBodyS3Location() != null) {
            body = getBodyFromS3(resourceModel.getBodyS3Location(), proxy, logger);
        }

        Map<String, String> parameters = gson.fromJson(gson.toJson(resourceModel.getParameters()), Map.class);

        return PutRestApiRequest.builder()
            .restApiId(resourceModel.getId())
            .failOnWarnings(resourceModel.getFailOnWarnings())
            .parameters(resourceModel.getParameters() != null ? parameters : null)
            .body(SdkBytes.fromByteArray(body.getBytes(StandardCharsets.UTF_8)))
            .mode(resourceModel.getMode()).build();
    }
}
