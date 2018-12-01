package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
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
    public void deploy() throws IOException {
        System.out.println("Initial deployment of application " + this.getNewApplication().getApplicationFullName());
        this.getNewApplication().create();
        this.getNewApplication().attachLoadBalancers();
    }
}
