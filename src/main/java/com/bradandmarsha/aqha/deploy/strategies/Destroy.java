package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
import java.io.IOException;

/**
 *
 * @author sbwise01
 */
public class Destroy extends DeploymentStrategy {

    public Destroy(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() throws IOException {
        System.out.println("Destroying application " + this.getOldApplication().getApplicationFullName());
        this.getOldApplication().detachLoadBalancers();
        this.getOldApplication().destroy();
    }
}
