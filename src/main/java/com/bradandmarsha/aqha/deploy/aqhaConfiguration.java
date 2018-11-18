package com.bradandmarsha.aqha.deploy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.List;

/**
 *
 * @author sbwise01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class aqhaConfiguration {
    private final String applicationName;
    private final String stackName;
    private final String region;
    private final String instanceType;
    private final String amiId;
    private final String keyName;
    private final Integer maxSize;
    private final Integer minSize;
    private final Integer desiredCapacity;
    private final List<String> subnetIds;
    
    @JsonCreator
    public aqhaConfiguration(@JsonProperty("applicationName") String applicationName,
            @JsonProperty("stackName") String stackName,
            @JsonProperty("region") String region,
            @JsonProperty("instanceType") String instanceType,
            @JsonProperty("amiId") String amiId,
            @JsonProperty("keyName") String keyName,
            @JsonProperty("maxSize") Integer maxSize,
            @JsonProperty("minSize") Integer minSize,
            @JsonProperty("desiredCapacity") Integer desiredCapacity,
            @JsonProperty("subnetIds") List<String> subnetIds) {
        this.applicationName = applicationName;
        this.stackName = stackName;
        this.region = region;
        this.instanceType = instanceType;
        this.amiId = amiId;
        this.keyName = keyName;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.desiredCapacity = desiredCapacity;
        this.subnetIds = subnetIds;
    }
    
    @Override
    public String toString() {
        ObjectWriter ow = new ObjectMapper().writer();
        try {
            return ow.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            //Since object was mapped in, this should not occur
            return "";
        }
    }

    /**
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @return the stackName
     */
    public String getStackName() {
        return stackName;
    }

    /**
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * @return the instanceType
     */
    public String getInstanceType() {
        return instanceType;
    }

    /**
     * @return the amiId
     */
    public String getAmiId() {
        return amiId;
    }

    /**
     * @return the keyName
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * @return the maxSize
     */
    public Integer getMaxSize() {
        return maxSize;
    }

    /**
     * @return the minSize
     */
    public Integer getMinSize() {
        return minSize;
    }

    /**
     * @return the desiredCapacity
     */
    public Integer getDesiredCapacity() {
        return desiredCapacity;
    }

    /**
     * @return the subnetIds
     */
    public List<String> getSubnetIds() {
        return subnetIds;
    }
}
