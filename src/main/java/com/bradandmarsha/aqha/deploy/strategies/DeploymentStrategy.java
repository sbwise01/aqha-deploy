package com.bradandmarsha.aqha.deploy.strategies;

import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;

/**
 *
 * @author sbwise01
 */
public class DeploymentStrategy {
    
    private final aqhaApplication oldApplication;
    private final aqhaApplication newApplication;

    public DeploymentStrategy(aqhaApplication oldApplication, aqhaApplication newApplication) {
        this.oldApplication = oldApplication;
        this.newApplication = newApplication;
    }
    
    public void deploy() {
    }

    public static DeploymentStrategy selectDeploymentStrategy(aqhaConfiguration configuration,
            boolean destroy, aqhaApplication oldApplication, aqhaApplication newApplication) {
        if (destroy) {
            return new Destroy(oldApplication, newApplication);
        } else if (oldApplication == null) {
            return new Initial(oldApplication, newApplication);
        }
        
        return new BlueGreen(oldApplication, newApplication);
    }

    /**
     * @return the oldApplication
     */
    public aqhaApplication getOldApplication() {
        return oldApplication;
    }

    /**
     * @return the newApplication
     */
    public aqhaApplication getNewApplication() {
        return newApplication;
    }
}
