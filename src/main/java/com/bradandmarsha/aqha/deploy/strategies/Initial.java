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
public class Initial extends DeploymentStrategy {
    private static final Logger LOGGER = Logger.getLogger(Initial.class);

    public Initial(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() throws aqhaDeploymentException {
        LOGGER.info("Initial deployment of application " + this.getNewApplication().getApplicationFullName());

        try {
            //Create new application
            Stopwatch applicationAvailabilityStopwatch = Stopwatch.createStarted();
            this.getNewApplication().create(applicationAvailabilityStopwatch);

            ////Attach load balancers
            this.getNewApplication().attachLoadBalancers();

            if (this.getNewApplication().verifyLoadBalancerHealth(applicationAvailabilityStopwatch)) {
                LOGGER.info("All load balancers became healthy in " +
                        applicationAvailabilityStopwatch.elapsed(TimeUnit.SECONDS) +
                        " seconds");
            } else {
                LOGGER.warn("Initial application " + this.getNewApplication().getApplicationFullName() +
                        " did not become healthy  ... letting it remain running");
            }
        } catch(aqhaDeploymentException | IOException e) {
            failDeployment(e);
        }
    }
}
