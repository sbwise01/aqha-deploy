package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.utils.Client;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sbwise01
 */
public class aqhaEC2Instance {
    private final Instance instance;

    public aqhaEC2Instance(Instance instance) {
        this.instance = instance;
    }
    
    public static List<aqhaEC2Instance> getInstances(aqhaConfiguration configuration, List<String> instanceIds) {
        List<aqhaEC2Instance> instances = new ArrayList<>();
        
        AmazonEC2Client client = Client.getEC2Client(configuration.getRegion());
        client.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceIds))
            .getReservations()
            .forEach((instanceReservation) -> {
            instanceReservation
                    .getInstances()
                    .forEach((instance) -> {
                instances.add(new aqhaEC2Instance(instance));
            });
        });

        return instances;
    }
    
    public Boolean isHealthy() {
        //TODO:  check instance health
        return Boolean.TRUE;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }
}
