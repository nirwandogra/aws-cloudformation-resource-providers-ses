package software.amazon.apigateway.restapi;

import software.amazon.awssdk.services.apigateway.model.EndpointType;

import java.util.ArrayList;
import java.util.List;

public class Translator {

    public static software.amazon.awssdk.services.apigateway.model.EndpointConfiguration translateEndpointConfiguration(
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
            software.amazon.awssdk.services.apigateway.model.EndpointConfiguration.builder().types(endpointTypes).
                vpcEndpointIds(endpointConfiguration.getVpcEndpointIds()).build() : null;
    }
}
