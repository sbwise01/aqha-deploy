package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
import com.google.common.base.Stopwatch;
import java.io.IOException;

/**
 *
 * @author sbwise01
 */
public class Initial extends DeploymentStrategy {

    public Initial(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() throws aqhaDeploymentException {
        System.out.println("Initial deployment of application " + this.getNewApplication().getApplicationFullName());

        try {
            //Create new application
            Stopwatch applicationAvailabilityStopwatch = Stopwatch.createStarted();
            this.getNewApplication().create(applicationAvailabilityStopwatch);

            ////Attach load balancers
            this.getNewApplication().attachLoadBalancers();

            if (!this.getNewApplication().verifyLoadBalancerHealth()) {
                System.out.println("Initial application " + this.getNewApplication().getApplicationFullName() +
                        " did not become healthy  ... letting it remain running");
            }
        } catch(aqhaDeploymentException | IOException e) {
            failDeployment(e);
        }
    }
}
