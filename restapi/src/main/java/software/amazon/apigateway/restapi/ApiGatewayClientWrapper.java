package software.amazon.apigateway.restapi;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.BadRequestException;
import software.amazon.awssdk.services.apigateway.model.ConflictException;
import software.amazon.awssdk.services.apigateway.model.GetTagsRequest;
import software.amazon.awssdk.services.apigateway.model.GetTagsResponse;
import software.amazon.awssdk.services.apigateway.model.LimitExceededException;
import software.amazon.awssdk.services.apigateway.model.NotFoundException;
import software.amazon.awssdk.services.apigateway.model.TagResourceRequest;
import software.amazon.awssdk.services.apigateway.model.TooManyRequestsException;
import software.amazon.awssdk.services.apigateway.model.UnauthorizedException;
import software.amazon.awssdk.services.apigateway.model.UntagResourceRequest;
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

import static software.amazon.apigateway.restapi.ApiGatewayUtils.getRestApiArn;
import static software.amazon.apigateway.restapi.ResourceModel.IDENTIFIER_KEY_ID;
import static software.amazon.cloudformation.Action.DELETE;

public class ApiGatewayClientWrapper {
    public static final ApiGatewayClient apiGatewayClient = ApiGatewayClient.builder().build();

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

    public static <RequestT extends AwsRequest, ResultT extends AwsResponse> String execute(
        final AmazonWebServicesClientProxy clientProxy, final ResourceModel resourceModel
        , Action action, RequestT request, Function<RequestT, ResultT> requestFunction,
        String logicalResourceIdentifier, Logger logger) {

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

    public static void addTags(
        ResourceModel input,
        AmazonWebServicesClientProxy proxy) {
        String resourceArn = getRestApiArn(input.getId(), region);

        GetTagsResponse oldTagsResult = proxy.injectCredentialsAndInvokeV2
            (GetTagsRequest.builder().resourceArn(resourceArn).build(), apiGatewayClient::getTags);

        if (!newTagsUpdated(input, oldTagsResult)) {
            return;
        }

        Map<String, String> tags = convertTagListToMap(input.getTags());

        if (tags != null && tags.size() > 0) {
            proxy.injectCredentialsAndInvokeV2
                (TagResourceRequest.builder().tags(tags).build(), apiGatewayClient::tagResource);
        }
    }

    public static void updateTags(ResourceModel resourceModel, AmazonWebServicesClientProxy proxy) {
        removeExistingTags(resourceModel, proxy);
        addTags(resourceModel, proxy);
    }

    public static void removeExistingTags(
        ResourceModel input,
        AmazonWebServicesClientProxy proxy) {
        String resourceArn = getRestApiArn(input.getId(), region);
        if (resourceArn == null) {
            return;
        }

        GetTagsResponse oldTagsResult = proxy.injectCredentialsAndInvokeV2
            (GetTagsRequest.builder().resourceArn(resourceArn).build(), apiGatewayClient::getTags);

        if (!newTagsUpdated(input, oldTagsResult)) {
            return;
        }

        if (oldTagsResult != null && oldTagsResult.tags() != null && oldTagsResult.tags().size() > 0) {
            proxy.injectCredentialsAndInvokeV2(UntagResourceRequest.builder().tagKeys
                (new ArrayList<>(oldTagsResult.tags().keySet())).build(), apiGatewayClient::untagResource);
        }
    }

    private static boolean newTagsUpdated(ResourceModel input, GetTagsResponse oldTagsResult) {
        Map<String, String> newTags = new HashMap<>();
        if (input.getTags() != null) {
            newTags = convertTagListToMap(input.getTags());
        }

        Map<String, String> oldTags = oldTagsResult == null ? new HashMap<>() : oldTagsResult.tags();

        if (oldTags == null && newTags == null) {
            return false;
        }

        if ((oldTags == null && newTags != null) || (oldTags != null && newTags == null)) {
            return true;
        }

        if (oldTags != null) {
            return !oldTags.equals(newTags);
        }

        return true;
    }

    private static Map<String, String> convertTagListToMap(List<Tag> tags) {
        Map<String, String> map = new HashMap<>();
        if(tags != null) {
            for (Tag tag : tags) {
                map.put(tag.getKey(), tag.getValue());
            }
        }
        return map;
    }
}
