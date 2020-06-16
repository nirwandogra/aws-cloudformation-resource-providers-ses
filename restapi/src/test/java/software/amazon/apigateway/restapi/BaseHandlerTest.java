package software.amazon.apigateway.restapi;

import com.google.gson.Gson;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

import static org.mockito.Mockito.mock;

public class BaseHandlerTest {

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected Logger logger;

    @Rule
    private final EnvironmentVariables environmentVariables
        = new EnvironmentVariables();

    protected static final Gson gson = new Gson();

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        environmentVariables.set("AWS_REGION", "us-east-1");
    }
}
