package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
import java.io.IOException;

/**
 *
 * @author sbwise01
 */
public class BlueGreen extends DeploymentStrategy {

    public BlueGreen(aqhaApplication oldApplication, aqhaApplication newApplication) {
        super(oldApplication, newApplication);
    }

    @Override
    public void deploy() throws IOException, aqhaDeploymentException {
        System.out.println("Executing BlueGreen replacement of application " + this.getOldApplication().getApplicationFullName() +
                " with application " + this.getNewApplication().getApplicationFullName());

        //Create new application
        this.getNewApplication().create();

        //TODO:  perhaps instance health check can be folded into create() method
        //Check for instance health before attaching load balancers
        if (!this.getNewApplication().verifyInstanceHealth()) {
            failDeployment("New application " + this.getNewApplication().getApplicationFullName() +
                    " did not become healthy  ... removing new application");
        }

        //Attach load balancers
        this.getNewApplication().attachLoadBalancers();

        //Check for load balancer health before destroying old application
        if (!this.getNewApplication().verifyLoadBalancerHealth()) {
            failDeployment("New application " + this.getNewApplication().getApplicationFullName() +
                    " did not become healthy  ... removing new application");
        }

        //Destroy old application
        DeploymentStrategy strategy = new Destroy(this.getOldApplication(), null);
        strategy.deploy();
    }
}
