package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;

/**
 *
 * @author sbwise01
 */
public class Initial extends DeploymentStrategy {

    public Initial(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() {
        System.out.println("Initial deployment of application " + this.getNewApplication().getApplicationFullName());
        this.getNewApplication().create();
    }
}
