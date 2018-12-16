package com.bradandmarsha.aqha.deploy.resources;

import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.utils.MD5;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sbwise01
 */
public class aqhaApplication {
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
            System.out.println("All instances became healthy in " +
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

    public Boolean verifyLoadBalancerHealth() {
        //TODO:  Load Balancer Health is acheived once all instances show
        //       in service status in all load balancers
        return Boolean.TRUE;
    }

    public Boolean verifyLoadBalancerDrain() {
        //TODO:  Load Balancer Drain is acheived once all instances
        //       have fully drained their connections and are no longer
        //       targets in the load balancer
        return Boolean.TRUE;
    }

    public void detachLoadBalancers() {
        if (configuration.hasLoadBalancers()) {
            autoScalingGroup.detachLoadBalancers(configuration);
        }
    }

    public void destroy() {
        //Destroy AutoScalingGroup
        autoScalingGroup.destroy(configuration);

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
