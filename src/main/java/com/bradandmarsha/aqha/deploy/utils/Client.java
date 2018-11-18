package com.bradandmarsha.aqha.deploy.utils;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

/**
 *
 * @author sbwise01
 */
public class Client {
    public static AmazonEC2Client getEC2Client(String region) {
        //TODO:  add caching of client
        return (AmazonEC2Client) AmazonEC2ClientBuilder
                .standard()
                .withRegion("us-west-2")
                .build();
    }
    
    public static AmazonAutoScalingClient getAutoScalingClient(String region) {
        return (AmazonAutoScalingClient) AmazonAutoScalingClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }
}
