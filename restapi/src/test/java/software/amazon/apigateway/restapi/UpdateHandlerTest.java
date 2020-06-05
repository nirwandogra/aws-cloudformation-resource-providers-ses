package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import org.apache.commons.collections.ArrayStack;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.platform.commons.util.CollectionUtils;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetTagsRequest;
import software.amazon.awssdk.services.apigateway.model.GetTagsResponse;
import software.amazon.awssdk.services.apigateway.model.PutRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.TagResourceResponse;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiResponse;
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Rule
    public final EnvironmentVariables environmentVariables
        = new EnvironmentVariables();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        environmentVariables.set("AWS_REGION", "us-east-1");
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder().id("random").name("name2")
            .binaryMediaTypes(Arrays.asList("application/octet"))
            .build();

        ResourceModel previousStage = ResourceModel.builder().id("random").name("name2")
            .binaryMediaTypes(Arrays.asList("application/octet"))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .previousResourceState(previousStage)
            .logicalResourceIdentifier("id")
            .build();

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(UpdateRestApiRequest.class), Mockito.any())).
            thenReturn(UpdateRestApiResponse.builder().name("name2").id("random").build());

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getId()).isEqualTo("random");
        assertThat(response.getResourceModel().getName()).isEqualTo("name2");
    }

    @Test
    public void handleRequest_SimpleSuccess_S3Body() {
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

        Map bodyMap = new Gson().fromJson(body, Map.class);

        final ResourceModel model = ResourceModel.builder()
            .body(bodyMap)
            .tags(Arrays.asList(Tag.builder().key("hello").value("world").build()))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("rest api")
            .build();

        final UpdateHandler updateHandler = new UpdateHandler();

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(PutRestApiRequest.class), Mockito.any())).
            thenReturn(UpdateRestApiResponse.builder().id("random").build());

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(GetTagsRequest.class), Mockito.any())).
            thenReturn(GetTagsResponse.builder().tags(Collections.singletonMap("hello", "world")).build());

        final ResourceHandlerRequest<ResourceModel> updateRequest = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("rest api")
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = updateHandler.handleRequest(proxy, updateRequest, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertEquals(response.getResourceModel().getTags().get(0).getKey(), "hello");
        assertThat(response.getResourceModel().getId()).isEqualTo("random");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SimpleSuccess_S3Url() {
        final UpdateHandler handler = new UpdateHandler();

        S3Location bodyS3Location = S3Location.builder().bucket("random")
            .eTag("random").key("random").version("random").build();

        final ResourceModel model = ResourceModel.builder()
            .bodyS3Location(bodyS3Location)
            .tags(Arrays.asList(Tag.builder().key("hello").value("testing").build()))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("rest api")
            .build();

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

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(PutRestApiRequest.class), Mockito.any())).
            thenReturn(UpdateRestApiResponse.builder().id("random").build());

        when(proxy.injectCredentialsAndInvokeV2(Mockito.any(GetTagsRequest.class), Mockito.any())).
            thenReturn(GetTagsResponse.builder().tags(Collections.singletonMap("hello", "world")).build());

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
        assertEquals(response.getResourceModel().getTags().get(0).getValue(), "testing");
    }
}
