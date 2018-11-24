package com.bradandmarsha.aqha.deploy.utils;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 *
 * @author sbwise01
 */
public class Client {
    public static AmazonEC2Client getEC2Client(String region) {
        //TODO:  add caching of client
        return (AmazonEC2Client) AmazonEC2ClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }
    
    public static AmazonAutoScalingClient getAutoScalingClient(String region) {
        return (AmazonAutoScalingClient) AmazonAutoScalingClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }

    public static AmazonS3Client getS3Client(String region) {
        return (AmazonS3Client) AmazonS3ClientBuilder
                .standard()
                .withRegion(region)
                .build();
    }
}
