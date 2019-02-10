package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AttachLoadBalancerTargetGroupsRequest;
import com.amazonaws.services.autoscaling.model.AttachLoadBalancersRequest;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DetachLoadBalancerTargetGroupsRequest;
import com.amazonaws.services.autoscaling.model.DetachLoadBalancersRequest;
import com.amazonaws.services.autoscaling.model.LaunchTemplateSpecification;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.ec2.model.LaunchTemplate;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.utils.Client;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sbwise01
 */
public class aqhaAutoScalingGroup {
    private AutoScalingGroup autoScalingGroup;
    private Boolean loadBalancersAttached;
    
    public aqhaAutoScalingGroup(AutoScalingGroup autoScalingGroup) {
        this.autoScalingGroup = autoScalingGroup;
        if(autoScalingGroup.getTargetGroupARNs().size() > 0 ||
                autoScalingGroup.getLoadBalancerNames().size() > 0) {
            this.loadBalancersAttached = Boolean.TRUE;
        } else {
            this.loadBalancersAttached = Boolean.FALSE;
        }
    }

    public void attachLoadBalancers(aqhaConfiguration configuration) {
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());

        if (configuration.getTargetGroupARNs() != null) {
            AttachLoadBalancerTargetGroupsRequest request = new AttachLoadBalancerTargetGroupsRequest()
                    .withAutoScalingGroupName(this.autoScalingGroup.getAutoScalingGroupName())
                    .withTargetGroupARNs(configuration.getTargetGroupARNs());
            client.attachLoadBalancerTargetGroups(request);
            loadBalancersAttached = Boolean.TRUE;
        }

        if (configuration.getElbClassicNames() != null) {
            AttachLoadBalancersRequest request = new AttachLoadBalancersRequest()
                    .withAutoScalingGroupName(this.autoScalingGroup.getAutoScalingGroupName())
                    .withLoadBalancerNames(configuration.getElbClassicNames());
            client.attachLoadBalancers(request);
            loadBalancersAttached = Boolean.TRUE;
        }
    }

    public void detachLoadBalancers(aqhaConfiguration configuration) {
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());

        if (loadBalancersAttached) {
            if (configuration.getTargetGroupARNs() != null) {
                DetachLoadBalancerTargetGroupsRequest request = new DetachLoadBalancerTargetGroupsRequest()
                        .withAutoScalingGroupName(this.autoScalingGroup.getAutoScalingGroupName())
                        .withTargetGroupARNs(configuration.getTargetGroupARNs());
                client.detachLoadBalancerTargetGroups(request);
            }

            if (configuration.getElbClassicNames() != null) {
                DetachLoadBalancersRequest request = new DetachLoadBalancersRequest()
                        .withAutoScalingGroupName(this.autoScalingGroup.getAutoScalingGroupName())
                        .withLoadBalancerNames(configuration.getElbClassicNames());
                client.detachLoadBalancers(request);
            }
        }
    }

    public void destroy(Stopwatch applicationDestructionStopwatch, aqhaConfiguration configuration) {
        System.out.println("Destroying auto scaling group");
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());

        //Scale down to 0 instances
        UpdateAutoScalingGroupRequest updateRequest = new UpdateAutoScalingGroupRequest()
                .withAutoScalingGroupName(autoScalingGroup.getAutoScalingGroupName())
                .withMaxSize(0)
                .withMinSize(0)
                .withDesiredCapacity(0);
        client.updateAutoScalingGroup(updateRequest);

        //Wait for scaling to complete
        aqhaAutoScalingGroup tmpAsg = new aqhaAutoScalingGroup(
                retrieveAutoScalingGroups(configuration,
                        autoScalingGroup.getAutoScalingGroupName()).get(0).getAutoScalingGroup()
        );
        while(tmpAsg.getInstanceIds(configuration).size() > 0 &&
                applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getApplicationDestructionTimeout()) {
            System.out.println("ASG scale down not complete for " +
                    autoScalingGroup.getAutoScalingGroupName() + " ... wating " +
                    configuration.getApplicationDestructionWait() + " seconds ... elapsed time is " +
                    applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) +
                    " seconds");
            try {
                TimeUnit.SECONDS.sleep(configuration.getApplicationDestructionWait());
            } catch (InterruptedException ex) {
                System.out.println("Application destruction wait exception " + ex.getMessage());
            }
            tmpAsg = new aqhaAutoScalingGroup(retrieveAutoScalingGroups(configuration,
                    autoScalingGroup.getAutoScalingGroupName()).get(0).getAutoScalingGroup()
            );
        }

        //Delete ASG
        DeleteAutoScalingGroupRequest request = new DeleteAutoScalingGroupRequest()
                .withAutoScalingGroupName(autoScalingGroup.getAutoScalingGroupName());
        if(applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getApplicationDestructionTimeout()) {
            System.out.println("Auto scaling group " + autoScalingGroup.getAutoScalingGroupName() +
                    " did not scale down to 0 instances before timeout " +
                    configuration.getApplicationDestructionTimeout() +
                    " ... adding ForceDelete option to deletion of ASG");
            request.setForceDelete(Boolean.TRUE);
        }
        client.deleteAutoScalingGroup(request);
    }
    
    public Boolean verifyInstanceHealth(Stopwatch applicationAvailabilityStopwatch,
            aqhaConfiguration configuration) throws aqhaDeploymentException {
        System.out.println("Verifying instance health");
        Stopwatch reservationStopwatch = Stopwatch.createStarted();
        while(autoScalingGroup.getInstances().size() < configuration.getMinSize() &&
                reservationStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getInstanceReservationTimeout()) {
            System.out.println("Instance reservations not complete ... wating " +
                    configuration.getInstanceReservationWait() + " seconds ... elapsed time is " +
                    applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                    " seconds");
            try {
                TimeUnit.SECONDS.sleep(configuration.getInstanceReservationWait());
            } catch (InterruptedException ex) {
                System.out.println("Instance reservation wait exception " + ex.getMessage());
            }
            List<AutoScalingGroup> asgs = retrieveASGs(configuration, autoScalingGroup.getAutoScalingGroupName());
            if(asgs.size() != 1) {
                throw new aqhaDeploymentException("Error retrieving autoScalingGroup during instance reservation check");
            }
            autoScalingGroup = asgs.get(0);
        }

        if(reservationStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getInstanceReservationTimeout()) {
            System.out.println("Instance reservations did not complete before timeout " +
                    configuration.getInstanceReservationTimeout());
            return Boolean.FALSE;
        }

        System.out.println("Instance reservations completed in " +
                reservationStopwatch.elapsed(TimeUnit.SECONDS) + " seconds");

        List<aqhaEC2Instance> instances = aqhaEC2Instance.getInstances(configuration, getInstanceIds(configuration));
        Integer healthyInstances = 0;
        while(healthyInstances < configuration.getMinSize() &&
                applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getApplicationAvailabilityTimeout()) {
            System.out.println("Healthy instances " + healthyInstances + " is less than minimum size " +
                    configuration.getMinSize() + " ... waiting " + configuration.getApplicationAvailabilityWait() +
                    " seconds ... elapsed time is " + applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                    " seconds");
            try {
                TimeUnit.SECONDS.sleep(configuration.getApplicationAvailabilityWait());
            } catch (InterruptedException ex) {
                System.out.println("Application availability wait exception " + ex.getMessage());
            }
            healthyInstances = 0;
            for(aqhaEC2Instance instance : instances) {
                if(instance.isHealthy(configuration)) {
                    healthyInstances++;
                }
            }
        }

        if(applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getApplicationAvailabilityTimeout()) {
            System.out.println("Application " + autoScalingGroup.getAutoScalingGroupName() +
                    " did not become avaialble before timeout " +
                    configuration.getApplicationAvailabilityTimeout());
            return Boolean.FALSE;
        }

        return healthyInstances >= configuration.getMinSize();
    }

    public List<String> getInstanceIds(aqhaConfiguration configuration) {
        List<String> instanceIds = new ArrayList<>();
        autoScalingGroup
                .getInstances()
                .forEach((instance) -> {
            instanceIds.add(instance.getInstanceId());
        });
        return instanceIds;
    }

    public static List<aqhaAutoScalingGroup> retrieveAutoScalingGroups(aqhaConfiguration configuration,
            String applicationFullName) {
        List<aqhaAutoScalingGroup> autoScalingGroups = new ArrayList<>();
        retrieveASGs(configuration, applicationFullName)
                .forEach((asg) -> {
            autoScalingGroups.add(new aqhaAutoScalingGroup(asg));
        });
        return autoScalingGroups;
    }
    
    private static List<AutoScalingGroup> retrieveASGs(aqhaConfiguration configuration,
            String applicationFullName) {
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());
        DescribeAutoScalingGroupsRequest request = new DescribeAutoScalingGroupsRequest()
                .withAutoScalingGroupNames(applicationFullName);
        return client.describeAutoScalingGroups(request).getAutoScalingGroups();
    }

    public static aqhaAutoScalingGroup createNewAutoScalingGroup(aqhaConfiguration configuration,
            String autoScalingGroupName, LaunchTemplate launchTemplate) {
        AmazonAutoScalingClient client = Client.getAutoScalingClient(configuration.getRegion());
        LaunchTemplateSpecification spec = new LaunchTemplateSpecification()
                .withLaunchTemplateId(launchTemplate.getLaunchTemplateId());
        CreateAutoScalingGroupRequest request = new CreateAutoScalingGroupRequest()
                .withAutoScalingGroupName(autoScalingGroupName)
                .withLaunchTemplate(spec)
                .withHealthCheckType(configuration.hasLoadBalancers() ? "ELB" : "EC2")
                .withMaxSize(configuration.getMaxSize())
                .withMinSize(configuration.getMinSize())
                .withDesiredCapacity(configuration.getDesiredCapacity())
                .withVPCZoneIdentifier(String.join(",", configuration.getSubnetIds()));
        if(configuration.hasLoadBalancers()) {
            request.setHealthCheckGracePeriod(configuration.getAsgHealthCheckGracePeriod());
        }
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
