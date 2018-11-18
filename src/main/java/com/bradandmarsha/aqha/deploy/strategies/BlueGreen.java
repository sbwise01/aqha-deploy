package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;

/**
 *
 * @author sbwise01
 */
public class BlueGreen extends DeploymentStrategy {

    public BlueGreen(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() {
        System.out.println("Executing BlueGreen replacement of application " + this.getOldApplication().getApplicationFullName() +
                " with application " + this.getNewApplication().getApplicationFullName());
        this.getNewApplication().create();
    }
}
