package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.aqhaDeploymentException;
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
    public void deploy() throws IOException, aqhaDeploymentException {
        System.out.println("Initial deployment of application " + this.getNewApplication().getApplicationFullName());

        //Create new application
        this.getNewApplication().create();

        //TODO:  perhaps instance health check can be folded into create() method
        //Check for instance health before attaching load balancers
        if (!this.getNewApplication().verifyInstanceHealth()) {
            failDeployment("Initial application " + this.getNewApplication().getApplicationFullName() +
                    " did not become healthy  ... removing initial application");
        }

        ////Attach load balancers
        this.getNewApplication().attachLoadBalancers();
        if (!this.getNewApplication().verifyLoadBalancerHealth()) {
            System.out.println("Initial application " + this.getNewApplication().getApplicationFullName() +
                    " did not become healthy  ... letting it remain running");
        }
    }
}
