package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.EndpointType;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Rule
    public final EnvironmentVariables environmentVariables
        = new EnvironmentVariables();

    private static final Gson gson = new Gson();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        environmentVariables.set("AWS_REGION", "us-east-1");
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
            .endpointConfiguration(EndpointConfiguration.builder().types(Arrays.
                asList(EndpointType.REGIONAL.toString())).build())
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("rest api")
            .build();

        CreateRestApiResponse createApiResponse = CreateRestApiResponse.builder().id("random").build();

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any())).thenReturn(createApiResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getId()).isEqualTo("random");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_S3Body() {
        final CreateHandler handler = new CreateHandler();

        String body= "{\n" +
                     "        \"swagger\": 2,\n" +
                     "        \"info\":\n" +
                     "        {\n" +
                     "            \"version\": \"0.0.1\",\n" +
                     "            \"title\": \"test\"\n" +
                     "        },\n" +
                     "        \"basePath\": \"/pete\",\n" +
                     "        \"schemes\": [\n" +
                     "            \"https\"\n" +
                     "        ],\n" +
                     "        \"definitions\":\n" +
                     "        {\n" +
                     "            \"Empty\":\n" +
                     "            {\n" +
                     "                \"type\": \"object\"\n" +
                     "            }\n" +
                     "        }\n" +
                     "    }";

        Map bodyMap = gson.fromJson(body, Map.class);

        final ResourceModel model = ResourceModel.builder()
            .body(bodyMap)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("rest api")
            .build();

        CreateRestApiResponse createApiResponse = CreateRestApiResponse.builder().id("random").build();

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any())).thenReturn(createApiResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getId()).isEqualTo("random");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_S3Url() {
        final CreateHandler handler = new CreateHandler();

        S3Location bodyS3Location = S3Location.builder().bucket("random")
            .eTag("random").key("random").version("random").build();

        final ResourceModel model = ResourceModel.builder()
            .bodyS3Location(bodyS3Location)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("rest api")
            .build();

        CreateRestApiResponse createApiResponse = CreateRestApiResponse.builder().id("random").build();

        String body= "{\n" +
                     "        \"swagger\": 2,\n" +
                     "        \"info\":\n" +
                     "        {\n" +
                     "            \"version\": \"0.0.1\",\n" +
                     "            \"title\": \"test\"\n" +
                     "        },\n" +
                     "        \"basePath\": \"/pete\",\n" +
                     "        \"schemes\": [\n" +
                     "            \"https\"\n" +
                     "        ],\n" +
                     "        \"definitions\":\n" +
                     "        {\n" +
                     "            \"Empty\":\n" +
                     "            {\n" +
                     "                \"type\": \"object\"\n" +
                     "            }\n" +
                     "        }\n" +
                     "    }";


        when(proxy.injectCredentialsAndInvokeV2Bytes(Mockito.any(), Mockito.any()))
            .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(),
                body.getBytes(StandardCharsets.UTF_8)));

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any())).thenReturn(createApiResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getId()).isEqualTo("random");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }
}
