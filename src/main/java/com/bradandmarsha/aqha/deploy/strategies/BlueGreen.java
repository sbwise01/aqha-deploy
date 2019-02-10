package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author sbwise01
 */
public class BlueGreen extends DeploymentStrategy {
    private static final Logger LOGGER = Logger.getLogger(DeploymentStrategy.class);

    public BlueGreen(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() throws aqhaDeploymentException {
        LOGGER.info("Executing BlueGreen replacement of application " + this.getOldApplication().getApplicationFullName() +
                " with application " + this.getNewApplication().getApplicationFullName());

        try {
            //Create new application
            Stopwatch applicationAvailabilityStopwatch = Stopwatch.createStarted();
            this.getNewApplication().create(applicationAvailabilityStopwatch);

            //Attach load balancers
            this.getNewApplication().attachLoadBalancers();

            //Check for load balancer health before destroying old application
            if (this.getNewApplication().verifyLoadBalancerHealth(applicationAvailabilityStopwatch)) {
                LOGGER.info("All load balancers became healthy in " +
                        applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                        " seconds");
            } else {
                failDeployment("New application " + this.getNewApplication().getApplicationFullName() +
                        " did not become healthy  ... removing new application");
            }
        } catch(aqhaDeploymentException | IOException e) {
            failDeployment(e);
        }

        //Destroy old application
        DeploymentStrategy strategy = new Destroy(this.getOldApplication(), null);
        strategy.deploy();
    }
}
