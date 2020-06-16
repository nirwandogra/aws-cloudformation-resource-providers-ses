package software.amazon.apigateway.restapi;

import com.amazonaws.regions.Region;
import software.amazon.awssdk.arns.Arn;

public class ArnUtils {

    private static final String ARN_PREFIX = "arn:";
    private static final String COLON = ":";

    private static final String vendorCode = "apigateway";


    public static String getRestApiArn(final String restApiId, Region region) {
        StringBuilder arnBuilder = getCommonArnBuilder(region)
            .append("/restapis/")
            .append(restApiId);

        return arnBuilder.toString();
    }

    public static StringBuilder getCommonArnBuilder(Region region) {
        StringBuilder arnBuilder = new StringBuilder();
        arnBuilder
            .append(ARN_PREFIX)
            .append(region.getPartition())
            .append(COLON)
            .append(vendorCode)
            .append(COLON)
            .append(region.getName())
            .append(COLON)
            .append(COLON);

        return arnBuilder;
    }
}
