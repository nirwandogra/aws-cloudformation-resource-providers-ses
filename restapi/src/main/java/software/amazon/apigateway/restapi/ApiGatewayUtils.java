package software.amazon.apigateway.restapi;

import com.amazonaws.regions.Region;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.apigateway.model.EndpointConfiguration;
import software.amazon.awssdk.services.apigateway.model.EndpointType;
import software.amazon.awssdk.services.apigateway.model.Op;
import software.amazon.awssdk.services.apigateway.model.PatchOperation;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.cloudformation.exceptions.TerminalException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApiGatewayUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> getTypesToCreateOrRemove(
        final List<String> oldTypes,
        final List<String> newTypes,
        final Boolean isCreate) {
        if(CollectionUtils.isEmpty(newTypes)) {
            return isCreate ? new ArrayList<>() : newTypes;
        } else if (CollectionUtils.isEmpty(oldTypes)) {
            return isCreate ? newTypes : new ArrayList<>();
        }

        final List<String> types = new ArrayList<>();

        final Set<String> typesToProcess = isCreate ?
            Sets.difference(Sets.newHashSet(newTypes), Sets.newHashSet(oldTypes)) :
            Sets.difference(Sets.newHashSet(oldTypes), Sets.newHashSet(newTypes));
        typesToProcess.forEach(type -> types.add(type));

        return types;
    }

    public static List<PatchOperation> newPatchOperations(final Map<String, Object> values, final Op operation) {
        final List<PatchOperation> output = new ArrayList<>();
        if (values == null) {
            return output; //Empty patch operations are permissible by API gateway.
        }
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            final String value = toString(entry.getValue());
            //This is a replace if not null. We are not going to replace unspecified values with defaults !!
            if (value != null) {
                output.add(PatchOperation.builder().op(operation).path(entry.getKey()).value(value).build());
            }
        }
        return output;
    }

    public static String toString(final Object value) {
        if (value == null) {
            return null;
        }
        else if (value instanceof String) {
            return (String) value;
        }
        else if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        else if (value instanceof Date) {
            return Long.toString(((Date) value).getTime());
        }
        else if (value instanceof Instant) {
            return Long.toString(((Instant) value).getEpochSecond());
        }
        else if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }
        else {
            try {
                return mapper.writeValueAsString(value);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unsupported value type, unable to convert to string", e);
            }
        }
    }

    public static String getBodyFromS3(
        S3Location s3Location,
        AmazonWebServicesClientProxy proxy,
        Logger logger) {
        S3Client s3Client = S3Client.builder().build();
        try {
            logger.log("Fetching file from S3");
            GetObjectRequest request = GetObjectRequest.builder().bucket(s3Location.getBucket())
                .ifMatch(s3Location.getETag())
                .key(s3Location.getKey()).build();

            ResponseBytes<GetObjectResponse> response = proxy.injectCredentialsAndInvokeV2Bytes
                (request, s3Client::getObjectAsBytes);

            return response.asUtf8String();
        } catch (Throwable t) {
            logger.log("Error while fetching file from S3 " + t.getMessage());
            return null;
        }
    }

    public static void validateRestApiResource(ResourceModel resource) {
        if (resource.getBody() != null && resource.getBodyS3Location() != null) {
            throw new TerminalException("Body cannot be specified together with BodyS3Location");
        }

        if (useImportApi(resource) && resource.getCloneFrom() != null) {
            throw new TerminalException("Body and/or BodyS3Location cannot be specified together with CloneFrom");
        }
    }

    public static boolean useImportApi(ResourceModel resource) {
        return resource.getBody() != null || resource.getBodyS3Location() != null;
    }

    public static String getRestApiArn(final String restApiId, Region region) {
        return Arn.builder()
            .accountId("")
            .partition(region.getPartition())
            .region(region.getName())
            .service(ApiGatewayClientWrapper.Constants.API_GATEWAY_SERVICE_NAME.getValue())
            .resource("/restapis/" + restApiId)
            .build().toString();
    }

    public static EndpointConfiguration translateEndpointConfiguration(
        software.amazon.apigateway.restapi.
            EndpointConfiguration endpointConfiguration) {
        if (endpointConfiguration == null) {
            return null;
        }
        List<String> types = endpointConfiguration.getTypes();
        List<EndpointType> endpointTypes = new ArrayList<>();
        for (String type : types) {
            endpointTypes.add(EndpointType.valueOf(type));
        }

        return endpointConfiguration != null ?
            EndpointConfiguration.builder().types(endpointTypes).
                vpcEndpointIds(endpointConfiguration.getVpcEndpointIds()).build() : null;
    }
}
