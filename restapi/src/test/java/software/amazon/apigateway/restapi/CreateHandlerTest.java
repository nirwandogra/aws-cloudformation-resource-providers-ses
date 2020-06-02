package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import org.apache.http.client.methods.HttpRequestBase;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.io.SdkFilterInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.Rule;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

        final ResourceModel model = ResourceModel.builder().build();

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

        ResponseInputStream responseStream = new ResponseInputStream(HttpRequestBase.class,
            AbortableInputStream.create(new ByteArrayInputStream(
                body.getBytes(StandardCharsets.UTF_8))));

        when(proxy.injectCredentialsAndInvokeV2InputStream(Mockito.any(), Mockito.any()))
            .thenReturn(responseStream);

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
