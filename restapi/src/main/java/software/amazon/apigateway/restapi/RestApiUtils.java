package software.amazon.apigateway.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.apigateway.model.Op;
import software.amazon.awssdk.services.apigateway.model.PatchOperation;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestApiUtils {

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

    public static void validateRestApiResource(ResourceModel resource) {
        if (resource.getBody() != null && resource.getBodyS3Location() != null) {
            throw new CfnInvalidRequestException("Body cannot be specified together with BodyS3Location");
        }

        if (useImportApi(resource) && resource.getCloneFrom() != null) {
            throw new CfnInvalidRequestException("Body and/or BodyS3Location cannot be specified together with CloneFrom");
        }
    }

    public static boolean useImportApi(ResourceModel resource) {
        return resource.getBody() != null || resource.getBodyS3Location() != null;
    }

    //
    //  Private methods
    //

    private static String toString(final Object value) {
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
}
