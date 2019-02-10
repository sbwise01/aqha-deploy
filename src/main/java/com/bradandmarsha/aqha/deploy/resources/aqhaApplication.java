package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;
import com.amazonaws.services.elasticloadbalancing.model.InstanceState;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetHealthResult;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetHealthDescription;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.utils.Client;
import com.bradandmarsha.aqha.deploy.utils.MD5;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author sbwise01
 */
public class aqhaApplication {
    private static final Logger LOGGER = Logger.getLogger(aqhaApplication.class);

    private final aqhaConfiguration configuration;
    private String applicationInstanceId;
    private aqhaLaunchTemplate launchTemplate;
    private aqhaAutoScalingGroup autoScalingGroup;
    
    public aqhaApplication(aqhaConfiguration configuration) {
        this.configuration = configuration;
        this.applicationInstanceId = MD5.getHash();
    }
    
    public static List<aqhaApplication> retrieveAqhaApplications(aqhaConfiguration configuration) {
        List<aqhaApplication> applications = new ArrayList<>();
        //TODO:  handle HTTP response code errors
        aqhaLaunchTemplate.retrieveLaunchTemplates(configuration)
                .forEach((lt) -> {
            aqhaAutoScalingGroup.retrieveAutoScalingGroups(configuration, lt.getLaunchTemplateName())
                .forEach((asg) -> {
                applications.add(new aqhaApplication(configuration).withLaunchTemplate(lt).withAutoScalingGroup(asg));
            });
        });
        return applications;
    }
    
    public static String getNamePrefix(aqhaConfiguration configuration) {
        return configuration.getApplicationName().toLowerCase() +
                "-" + configuration.getStackName().toLowerCase() +
                "-";
    }
    
    private static String parseApplicationInstanceId(String applicationFullName) {
        return applicationFullName.split("-", -1)[2];
    }
    
    @Override
    public String toString() {
        return  "\n     Application Full Name = " + getApplicationFullName() +
                "\n     Application Instance Id = " + applicationInstanceId +
                "\n     Configuration = " + configuration.toString() +
                "\n     LaunchTemplate = " + launchTemplate.toString() +
                "\n     AutoScalingGroup = " + autoScalingGroup.toString();
    }
    
    public void create(Stopwatch applicationAvailabilityStopwatch) throws IOException, aqhaDeploymentException {
        //Create LaunchTemplate
        launchTemplate = aqhaLaunchTemplate.createNewLaunchTemplate(configuration,
                getApplicationFullName());

        //Create AutoScalingGroup
        autoScalingGroup = aqhaAutoScalingGroup.createNewAutoScalingGroup(configuration,
                getApplicationFullName(), launchTemplate.getLaunchTemplate());

        //Verify instance health
        if(verifyInstanceHealth(applicationAvailabilityStopwatch)) {
            LOGGER.info("All instances became healthy in " +
                    applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                    " seconds");
        } else {
            throw new aqhaDeploymentException("New application " + getApplicationFullName() +
                        " did not become healthy  ... removing new application");
        }
    }
    
    public void attachLoadBalancers() {
        if (configuration.hasLoadBalancers()) {
            autoScalingGroup.attachLoadBalancers(configuration);
        }
    }

    public Boolean verifyInstanceHealth(Stopwatch applicationAvailabilityStopwatch) throws aqhaDeploymentException {
        return autoScalingGroup.verifyInstanceHealth(applicationAvailabilityStopwatch, configuration);
    }

    public Boolean verifyLoadBalancerHealth(Stopwatch applicationAvailabilityStopwatch) {
        if (configuration.hasLoadBalancers()) {
            LOGGER.info("Verifying load balancer health");
            List<String> instanceIds = autoScalingGroup.getInstanceIds(configuration);
            if (configuration.getTargetGroupARNs() != null) {
                com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClient
                        client = Client.getElbV2Client(configuration.getRegion());
                Integer healthyTargetGroups = 0;
                while(healthyTargetGroups < configuration.getTargetGroupARNs().size() &&
                        applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getApplicationAvailabilityTimeout()) {
                    LOGGER.info("Healthy target groups " + healthyTargetGroups +
                            " is less than number of target groups " +
                            configuration.getTargetGroupARNs().size() + " ... waiting " +
                            configuration.getApplicationAvailabilityWait() +
                            " seconds ... elapsed time is " + applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                            " seconds");
                    try {
                        TimeUnit.SECONDS.sleep(configuration.getApplicationAvailabilityWait());
                    } catch (InterruptedException ex) {
                        LOGGER.warn("Application availability wait exception ", ex);
                    }
                    healthyTargetGroups = 0;
                    for(String tgArn : configuration.getTargetGroupARNs()) {
                        Integer healthyInstances = 0;
                        DescribeTargetHealthResult describeTargetHealthResult =
                                client.describeTargetHealth(new DescribeTargetHealthRequest()
                                        .withTargetGroupArn(tgArn));
                        for(TargetHealthDescription tghDesc : describeTargetHealthResult.getTargetHealthDescriptions()) {
                            if(instanceIds.contains(tghDesc.getTarget().getId()) &&
                                    tghDesc.getTargetHealth().getState().equalsIgnoreCase("healthy")) {
                                healthyInstances++;
                            }
                        }
                        if(healthyInstances.equals(instanceIds.size())) {
                            healthyTargetGroups++;
                        }
                    }
                }

                if(applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getApplicationAvailabilityTimeout()) {
                    LOGGER.error("Application " + autoScalingGroup.getAutoScalingGroupName() +
                            " did not become avaialble before timeout " +
                            configuration.getApplicationAvailabilityTimeout());
                    return Boolean.FALSE;
                }
            }
            if (configuration.getElbClassicNames() != null) {
                AmazonElasticLoadBalancingClient client = Client.getElbV1Client(configuration.getRegion());
                Integer healthyElbs = 0;
                while(healthyElbs < configuration.getElbClassicNames().size() &&
                        applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getApplicationAvailabilityTimeout()) {
                    LOGGER.info("Healthy ELB classics " + healthyElbs +
                            " is less than number of ELBs " +
                            configuration.getElbClassicNames().size() + " ... waiting " +
                            configuration.getApplicationAvailabilityWait() +
                            " seconds ... elapsed time is " + applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                            " seconds");
                    try {
                        TimeUnit.SECONDS.sleep(configuration.getApplicationAvailabilityWait());
                    } catch (InterruptedException ex) {
                        LOGGER.warn("Application availability wait exception ", ex);
                    }
                    healthyElbs = 0;
                    for(String elbName : configuration.getElbClassicNames()) {
                        Integer healthyInstances = 0;
                        DescribeInstanceHealthResult describeInstanceHealthResult =
                                client.describeInstanceHealth(new DescribeInstanceHealthRequest()
                                        .withLoadBalancerName(elbName));
                        for(InstanceState iState : describeInstanceHealthResult.getInstanceStates()) {
                            if(instanceIds.contains(iState.getInstanceId()) &&
                                    iState.getState().equalsIgnoreCase("InService")) {
                                healthyInstances++;
                            }
                        }
                        if(healthyInstances.equals(instanceIds.size())) {
                            healthyElbs++;
                        }
                    }
                }

                if(applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getApplicationAvailabilityTimeout()) {
                    LOGGER.error("Application " + autoScalingGroup.getAutoScalingGroupName() +
                            " did not become avaialble before timeout " +
                            configuration.getApplicationAvailabilityTimeout());
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public Boolean verifyLoadBalancerDrain(Stopwatch applicationDestructionStopwatch) {
        if (configuration.hasLoadBalancers()) {
            LOGGER.info("Verifying load balancer drain");
            List<String> instanceIds = autoScalingGroup.getInstanceIds(configuration);
            if (configuration.getTargetGroupARNs() != null) {
                com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClient
                        client = Client.getElbV2Client(configuration.getRegion());
                Integer drainedTargetGroups = 0;
                while(drainedTargetGroups < configuration.getTargetGroupARNs().size() &&
                        applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getApplicationDestructionTimeout()) {
                    LOGGER.info("Drained target groups " + drainedTargetGroups +
                            " is less than number of target groups " +
                            configuration.getTargetGroupARNs().size() + " ... waiting " +
                            configuration.getApplicationDestructionWait() +
                            " seconds ... elapsed time is " + applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) +
                            " seconds");
                    try {
                        TimeUnit.SECONDS.sleep(configuration.getApplicationDestructionWait());
                    } catch (InterruptedException ex) {
                        LOGGER.warn("Application destruction wait exception ", ex);
                    }
                    drainedTargetGroups = 0;
                    for(String tgArn : configuration.getTargetGroupARNs()) {
                        Integer attachedInstances = 0;
                        DescribeTargetHealthResult describeTargetHealthResult =
                                client.describeTargetHealth(new DescribeTargetHealthRequest()
                                        .withTargetGroupArn(tgArn));
                        for(TargetHealthDescription tghDesc : describeTargetHealthResult.getTargetHealthDescriptions()) {
                            if(instanceIds.contains(tghDesc.getTarget().getId())) {
                                attachedInstances++;
                            }
                        }
                        if(attachedInstances.equals(0)) {
                            drainedTargetGroups++;
                        }
                    }
                }

                if(applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getApplicationDestructionTimeout()) {
                    LOGGER.error("Application " + autoScalingGroup.getAutoScalingGroupName() +
                            " did not drain before timeout " +
                            configuration.getApplicationDestructionTimeout());
                    return Boolean.FALSE;
                }
            }
            if (configuration.getElbClassicNames() != null) {
                AmazonElasticLoadBalancingClient client = Client.getElbV1Client(configuration.getRegion());
                Integer drainedElbs = 0;
                while(drainedElbs < configuration.getElbClassicNames().size() &&
                        applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) <= configuration.getApplicationDestructionTimeout()) {
                    LOGGER.info("Drained ELB classics " + drainedElbs +
                            " is less than number of ELBs " +
                            configuration.getElbClassicNames().size() + " ... waiting " +
                            configuration.getApplicationDestructionWait() +
                            " seconds ... elapsed time is " + applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) +
                            " seconds");
                    try {
                        TimeUnit.SECONDS.sleep(configuration.getApplicationDestructionWait());
                    } catch (InterruptedException ex) {
                        LOGGER.warn("Application destruction wait exception ", ex);
                    }
                    drainedElbs = 0;
                    for(String elbName : configuration.getElbClassicNames()) {
                        Integer attachedInstances = 0;
                        DescribeInstanceHealthResult describeInstanceHealthResult =
                                client.describeInstanceHealth(new DescribeInstanceHealthRequest()
                                        .withLoadBalancerName(elbName));
                        for(InstanceState iState : describeInstanceHealthResult.getInstanceStates()) {
                            if(instanceIds.contains(iState.getInstanceId())) {
                                attachedInstances++;
                            }
                        }
                        if(attachedInstances.equals(0)) {
                            drainedElbs++;
                        }
                    }
                }

                if(applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) > configuration.getApplicationDestructionTimeout()) {
                    LOGGER.error("Application " + autoScalingGroup.getAutoScalingGroupName() +
                            " did not drain before timeout " +
                            configuration.getApplicationAvailabilityTimeout());
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public void detachLoadBalancers() {
        if (configuration.hasLoadBalancers()) {
            autoScalingGroup.detachLoadBalancers(configuration);
        }
    }

    public void destroy(Stopwatch applicationDestructionStopwatch) {
        //Destroy AutoScalingGroup
        autoScalingGroup.destroy(applicationDestructionStopwatch, configuration);

        //Destroy LaunchTemplate
        launchTemplate.destroy(configuration);
    }

    public String getApplicationFullName() {
        if (launchTemplate == null) {
            return configuration.getApplicationName() +
                    "-" + configuration.getStackName() +
                    "-" + applicationInstanceId;
        } else {
            return launchTemplate.getLaunchTemplateName();
        }
    }

    /**
     * @return the configuration
     */
    public aqhaConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @return the applicationInstanceId
     */
    public String getApplicationInstanceId() {
        return applicationInstanceId;
    }

    /**
     * @return the launchTemplate
     */
    public aqhaLaunchTemplate getLaunchTemplate() {
        return launchTemplate;
    }
    
    public aqhaApplication withLaunchTemplate(aqhaLaunchTemplate launchTemplate) {
        this.launchTemplate = launchTemplate;
        this.applicationInstanceId = parseApplicationInstanceId(launchTemplate.getLaunchTemplateName());
        return this;
    }

    /**
     * @return the autoScalingGroup
     */
    public aqhaAutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }
    
    public aqhaApplication withAutoScalingGroup(aqhaAutoScalingGroup autoScalingGroup) {
        this.autoScalingGroup = autoScalingGroup;
        this.applicationInstanceId = parseApplicationInstanceId(autoScalingGroup.getAutoScalingGroupName());
        return this;
    }
}
