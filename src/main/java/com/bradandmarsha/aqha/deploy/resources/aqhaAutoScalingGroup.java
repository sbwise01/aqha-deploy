package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.LaunchTemplateSpecification;
import com.amazonaws.services.ec2.model.LaunchTemplate;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.utils.Client;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sbwise01
 */
public class aqhaAutoScalingGroup {
    private final AutoScalingGroup autoScalingGroup;
    
    public aqhaAutoScalingGroup(AutoScalingGroup autoScalingGroup) {
        this.autoScalingGroup = autoScalingGroup;
    }
    
    public static List<aqhaAutoScalingGroup> retrieveAutoScalingGroups(aqhaConfiguration configuration,
            String applicationFullName) {
        List<aqhaAutoScalingGroup> autoScalingGroups = new ArrayList<>();
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());
        DescribeAutoScalingGroupsRequest request = new DescribeAutoScalingGroupsRequest()
                .withAutoScalingGroupNames(applicationFullName);
        client.describeAutoScalingGroups(request).getAutoScalingGroups()
                .forEach((asg) -> {
            autoScalingGroups.add(new aqhaAutoScalingGroup(asg));
        });
        return autoScalingGroups;
    }
    
    public static aqhaAutoScalingGroup createNewAutoScalingGroup(aqhaConfiguration configuration,
            String autoScalingGroupName, LaunchTemplate launchTemplate) {
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());
        LaunchTemplateSpecification spec = new LaunchTemplateSpecification()
                .withLaunchTemplateId(launchTemplate.getLaunchTemplateId());
        CreateAutoScalingGroupRequest request = new CreateAutoScalingGroupRequest()
                .withAutoScalingGroupName(autoScalingGroupName)
                .withLaunchTemplate(spec)
                .withMaxSize(configuration.getMaxSize())
                .withMinSize(configuration.getMinSize())
                .withDesiredCapacity(configuration.getDesiredCapacity())
                .withVPCZoneIdentifier(String.join(",", configuration.getSubnetIds()));
        client.createAutoScalingGroup(request);
        
        return new aqhaAutoScalingGroup(retrieveAutoScalingGroups(configuration,
                autoScalingGroupName).get(0).getAutoScalingGroup());
    }
    
    @Override
    public String toString() {
        return "aqhaAutoScalingGroup = " + autoScalingGroup.toString();
    }
    
    public String getAutoScalingGroupName() {
        return autoScalingGroup.getAutoScalingGroupName();
    }

    /**
     * @return the autoScalingGroup
     */
    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
}
