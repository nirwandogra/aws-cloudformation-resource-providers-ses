package software.amazon.apigateway.restapi;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.BadRequestException;
import software.amazon.awssdk.services.apigateway.model.ConflictException;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.DeleteRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.GetRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.GetTagsRequest;
import software.amazon.awssdk.services.apigateway.model.ImportRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.LimitExceededException;
import software.amazon.awssdk.services.apigateway.model.NotFoundException;
import software.amazon.awssdk.services.apigateway.model.PutRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.TagResourceRequest;
import software.amazon.awssdk.services.apigateway.model.TooManyRequestsException;
import software.amazon.awssdk.services.apigateway.model.UnauthorizedException;
import software.amazon.awssdk.services.apigateway.model.UntagResourceRequest;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiRequest;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static software.amazon.apigateway.restapi.ArnUtils.getRestApiArn;
import static software.amazon.apigateway.restapi.ResourceModel.IDENTIFIER_KEY_ID;
import static software.amazon.cloudformation.Action.DELETE;

public class ApiGatewayClientWrapper {
    private static final ApiGatewayClient apiGatewayClient = ApiGatewayClient.builder().build();

    private static final Region region = Region.getRegion(
        Regions.fromName(System.getenv(Constants.ENV_VARIABLE_REGION.getValue()))
    );

    enum Constants {
        API_GATEWAY_SERVICE_NAME("apigateway"),
        ENV_VARIABLE_REGION("AWS_REGION");

        @Getter
        private String value;

        Constants(final String val) {
            value = val;
        }
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String createRestApi(
        final AmazonWebServicesClientProxy proxy, final ResourceModel resourceModel, RequestT request, Logger logger) {
        return execute(proxy, resourceModel, Action.CREATE,
            (CreateRestApiRequest) request, apiGatewayClient::createRestApi, logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String updateRestApi(
        final AmazonWebServicesClientProxy proxy, final ResourceModel resourceModel, RequestT request, Logger logger) {
        return execute(proxy, resourceModel, Action.UPDATE,
            (UpdateRestApiRequest) request, apiGatewayClient::updateRestApi, logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String deleteRestApi(
        final AmazonWebServicesClientProxy proxy, final ResourceModel resourceModel, RequestT request, Logger logger) {
        return execute(proxy, resourceModel, DELETE,
            (DeleteRestApiRequest) request, apiGatewayClient::deleteRestApi, logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String getRestApi(
        final AmazonWebServicesClientProxy proxy, final ResourceModel resourceModel, RequestT request, Logger logger) {
        return execute(proxy, resourceModel, Action.CREATE,
            (GetRestApiRequest) request, apiGatewayClient::getRestApi, logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String importRestApi(
        final AmazonWebServicesClientProxy proxy, final ResourceModel resourceModel, RequestT request, Logger logger) {
        return execute(proxy, resourceModel, Action.CREATE,
            (ImportRestApiRequest) request, apiGatewayClient::importRestApi,
            logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String putRestApi(
        final AmazonWebServicesClientProxy proxy, final ResourceModel resourceModel, RequestT request,
        Logger logger) {
        return execute(proxy, resourceModel, Action.UPDATE,
            (PutRestApiRequest) request, apiGatewayClient::putRestApi, logger);
    }

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String execute(
        final AmazonWebServicesClientProxy clientProxy, final ResourceModel resourceModel
        , Action action, RequestT request, Function<RequestT, ResultT> requestFunction,
        Logger logger) {

        logger.log("Invoking with request:" + request.toString());

        try {
            if (!action.name().equalsIgnoreCase(DELETE.name())) {
                ResultT response = clientProxy.injectCredentialsAndInvokeV2(
                    request, requestFunction);
                return response.getValueForField(StringUtils.uncapitalize(IDENTIFIER_KEY_ID.split("/")[2])
                    , String.class).get();
            }
            else if (action.name().equalsIgnoreCase(DELETE.name())) {
                clientProxy.injectCredentialsAndInvokeV2(
                    request, requestFunction);
            }
        } catch (NotFoundException e) {
            throw new CfnNotFoundException(
                ResourceModel.TYPE_NAME, resourceModel.getPrimaryIdentifier().toString()
            );
        } catch (UnauthorizedException | BadRequestException | SdkClientException
            | ConflictException | LimitExceededException | TooManyRequestsException e) {
            throw new CfnInvalidRequestException(e.getMessage(), e);
        } catch (AwsServiceException e) {
            throw new CfnGeneralServiceException(request.getClass().getName(), e);
        }
        return null;
    }

    public static void updateTags(ResourceModel resourceModel, AmazonWebServicesClientProxy proxy) {
        String resourceArn = getRestApiArn(resourceModel.getId(), region);

        Map<String, String> previousTags = proxy.injectCredentialsAndInvokeV2
            (GetTagsRequest.builder().resourceArn(resourceArn).build(), apiGatewayClient::getTags).tags();

        Map<String, String> newTags = resourceModel.getTags() != null ? resourceModel.getTags().
            stream().collect(Collectors.toMap(Tag::getKey, Tag:: getValue)) : new HashMap<>();

        final List<String> tagsToRemove = new ArrayList<>(
            Maps.difference(newTags, previousTags).entriesOnlyOnRight().keySet());

        final Map<String, String> tagsToCreate =
            Maps.difference(newTags, previousTags).entriesOnlyOnLeft();

        if (!tagsToRemove.isEmpty()) {
            proxy.injectCredentialsAndInvokeV2(UntagResourceRequest.builder().tagKeys
                (new ArrayList<>(previousTags.keySet())).build(), apiGatewayClient::untagResource);
        }

        if (!tagsToCreate.isEmpty()) {
            proxy.injectCredentialsAndInvokeV2
                (TagResourceRequest.builder().tags(newTags).build(), apiGatewayClient::tagResource);
        }
    }
}
