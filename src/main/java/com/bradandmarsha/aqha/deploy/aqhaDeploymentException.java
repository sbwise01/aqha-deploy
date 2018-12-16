package com.bradandmarsha.aqha.deploy;

/**
 *
 * @author sbwise01
 */
public class aqhaDeploymentException extends Exception {
    public aqhaDeploymentException(String message) {
        super(message);
    }

    public aqhaDeploymentException(String message, Exception e) {
        super(message, e);
    }
}
