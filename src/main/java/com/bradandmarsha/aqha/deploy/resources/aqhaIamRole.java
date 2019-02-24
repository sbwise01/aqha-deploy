package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleResult;
import com.amazonaws.services.identitymanagement.model.ListInstanceProfilesForRoleRequest;
import com.amazonaws.services.identitymanagement.model.ListInstanceProfilesForRoleResult;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.utils.Client;

/**
 *
 * @author sbwise01
 */
public class aqhaIamRole {
    public static String getInstanceProfileArn(aqhaConfiguration configuration) {
        AmazonIdentityManagementClient client = Client.getIamClient(configuration.getRegion());
        ListInstanceProfilesForRoleResult result = client.listInstanceProfilesForRole(
                new ListInstanceProfilesForRoleRequest()
                        .withRoleName(configuration.getRoleNameForInstanceProfile())
        );
        return result.getInstanceProfiles().get(0).getArn();
    }

    public static String getRoleArn(aqhaConfiguration configuration) {
        AmazonIdentityManagementClient client = Client.getIamClient(configuration.getRegion());
        GetRoleResult result = client.getRole(
                new GetRoleRequest()
                        .withRoleName(configuration.getRoleNameForInstanceProfile())
        );
        return result.getRole().getArn();
    }
}
