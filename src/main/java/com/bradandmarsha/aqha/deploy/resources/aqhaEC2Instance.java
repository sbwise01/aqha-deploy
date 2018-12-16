package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.utils.Client;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

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
    
    public Boolean isHealthy(aqhaConfiguration configuration) {
        Boolean isHealthy = Boolean.TRUE;

        if(configuration.getInstanceHealthCheck() != null) {
            try {
                // Create HTTP client builder
                HttpClientBuilder builder = HttpClientBuilder.create();
                builder.setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(2000).build());

                //Handle self signed certificates if SSL
                if(configuration.getInstanceHealthCheck().getProtocol().equalsIgnoreCase("https")) {
                    //add certificate ignore logic
                    builder.setSSLSocketFactory(new SSLConnectionSocketFactory(
                            new SSLContextBuilder()
                                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                                .build(),
                            new NoopHostnameVerifier()
                        )
                    );
                }

                // Create HTTP client
                CloseableHttpClient client = builder.build();

                //execute request
                String host = configuration.getInstanceHealthCheck().getUsePublicAddress() ?
                        instance.getPublicIpAddress() : instance.getPrivateIpAddress();
                String url = configuration.getInstanceHealthCheck().getHealthCheckUrl(host);
                System.out.println("Checking instance health on instance " +
                        instance.getInstanceId() + " with url " + url);
                HttpResponse response = client.execute(new HttpHead(url));
                isHealthy = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;

                //Close HTTP client
                client.close();
            } catch(NoSuchAlgorithmException | KeyStoreException | KeyManagementException | URISyntaxException | IOException e) {
                System.out.println("Exception checking instance health:  " + e.getMessage());
                isHealthy = Boolean.FALSE;
            }
        }

        return isHealthy;
    }

    /**
     * @return the instance
     */
    public Instance getInstance() {
        return instance;
    }
}
