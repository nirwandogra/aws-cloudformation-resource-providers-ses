package software.amazon.apigateway.restapi;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;
import software.amazon.awssdk.services.apigateway.model.GetTagsRequest;
import software.amazon.awssdk.services.apigateway.model.GetTagsResponse;
import software.amazon.awssdk.services.apigateway.model.TagResourceResponse;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.UpdateRestApiResponse;
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

        final ResourceModel model = ResourceModel.builder().id("random").name("name2").build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
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
}
