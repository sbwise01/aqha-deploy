package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author sbwise01
 */
public class Destroy extends DeploymentStrategy {
    private static final Logger LOGGER = Logger.getLogger(Destroy.class);

    public Destroy(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() throws aqhaDeploymentException {
        LOGGER.info("Destroying application " + this.getOldApplication().getApplicationFullName());
        Stopwatch applicationDestructionStopwatch = Stopwatch.createStarted();
        this.getOldApplication().detachLoadBalancers();
        //Check for load balancer drain before destroying old application
        if(this.getOldApplication().verifyLoadBalancerDrain(applicationDestructionStopwatch)) {
            LOGGER.info("All load balancers drained in " +
                        applicationDestructionStopwatch.elapsed(TimeUnit.SECONDS) +
                        " seconds");
        } else {
            LOGGER.warn("Destroying application " + this.getOldApplication().getApplicationFullName() +
                    " did not fully drain from load balancers  ... proceeding with destroy");
        }
        this.getOldApplication().destroy(applicationDestructionStopwatch);
    }
}
