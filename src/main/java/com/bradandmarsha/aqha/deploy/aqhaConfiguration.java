package com.bradandmarsha.aqha.deploy;

import com.bradandmarsha.aqha.deploy.resources.aqhaUserDataBootstrap;
import com.bradandmarsha.aqha.deploy.resources.aqhaInstanceHealthCheck;
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
    private final Integer asgHealthCheckGracePeriod;
    private final List<String> subnetIds;
    private final List<String> securityGroupIds;
    private final aqhaUserDataBootstrap userDataBootstrap;
    private final String roleNameForInstanceProfile;
    private final List<String> targetGroupARNs;
    private final List<String> elbClassicNames;
    private final Integer applicationAvailabilityTimeout;
    private final Integer applicationAvailabilityWait;
    private final Integer applicationDestructionTimeout;
    private final Integer applicationDestructionWait;
    private final Integer instanceReservationTimeout;
    private final Integer instanceReservationWait;
    private final aqhaInstanceHealthCheck instanceHealthCheck;
    
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
            @JsonProperty("asgHealthCheckGracePeriod") Integer asgHealthCheckGracePeriod,
            @JsonProperty("subnetIds") List<String> subnetIds,
            @JsonProperty("securityGroupIds") List<String> securityGroupIds,
            @JsonProperty("aqhaUserDataBootstrap") aqhaUserDataBootstrap userDataBootstrap,
            @JsonProperty("roleNameForInstanceProfile") String roleNameForInstanceProfile,
            @JsonProperty("targetGroupARNs") List<String> targetGroupARNs,
            @JsonProperty("elbClassicNames") List<String> elbClassicNames,
            @JsonProperty("applicationAvailabilityTimeout") Integer applicationAvailabilityTimeout,
            @JsonProperty("applicationAvailabilityWait") Integer applicationAvailabilityWait,
            @JsonProperty("applicationDestructionTimeout") Integer applicationDestructionTimeout,
            @JsonProperty("applicationDestructionWait") Integer applicationDestructionWait,
            @JsonProperty("instanceReservationTimeout") Integer instanceReservationTimeout,
            @JsonProperty("instanceReservationWait") Integer instanceReservationWait,
            @JsonProperty("aqhaInstanceHealthCheck") aqhaInstanceHealthCheck instanceHealthCheck) {
        this.applicationName = applicationName;
        this.stackName = stackName;
        this.region = region;
        this.instanceType = instanceType;
        this.amiId = amiId;
        this.keyName = keyName;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.desiredCapacity = desiredCapacity;
        this.asgHealthCheckGracePeriod = asgHealthCheckGracePeriod;
        this.subnetIds = subnetIds;
        this.securityGroupIds = securityGroupIds;
        this.userDataBootstrap = userDataBootstrap;
        this.roleNameForInstanceProfile = roleNameForInstanceProfile;
        this.targetGroupARNs = targetGroupARNs;
        this.elbClassicNames = elbClassicNames;
        this.applicationAvailabilityTimeout = applicationAvailabilityTimeout;
        this.applicationAvailabilityWait = applicationAvailabilityWait;
        this.applicationDestructionTimeout = applicationDestructionTimeout;
        this.applicationDestructionWait = applicationDestructionWait;
        this.instanceReservationTimeout = instanceReservationTimeout;
        this.instanceReservationWait = instanceReservationWait;
        this.instanceHealthCheck = instanceHealthCheck;
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

    public Boolean hasLoadBalancers() {
        return this.targetGroupARNs != null || this.elbClassicNames != null;
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
     * @return the asgHealthCheckGracePeriod
     */
    public Integer getAsgHealthCheckGracePeriod() {
        return asgHealthCheckGracePeriod;
    }

    /**
     * @return the subnetIds
     */
    public List<String> getSubnetIds() {
        return subnetIds;
    }

    /**
     * @return the securityGroupIds
     */
    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    /**
     * @return the userDataBootstrap
     */
    public aqhaUserDataBootstrap getUserDataBootstrap() {
        return userDataBootstrap;
    }

    /**
     * @return the roleNameForInstanceProfile
     */
    public String getRoleNameForInstanceProfile() {
        return roleNameForInstanceProfile;
    }

    /**
     * @return the targetGroupARNs
     */
    public List<String> getTargetGroupARNs() {
        return targetGroupARNs;
    }

    /**
     * @return the elbClassicNames
     */
    public List<String> getElbClassicNames() {
        return elbClassicNames;
    }

    /**
     * @return the applicationAvailabilityTimeout
     */
    public Integer getApplicationAvailabilityTimeout() {
        return applicationAvailabilityTimeout;
    }

    /**
     * @return the applicationAvailabilityWait
     */
    public Integer getApplicationAvailabilityWait() {
        return applicationAvailabilityWait;
    }

    /**
     * @return the applicationDestructionTimeout
     */
    public Integer getApplicationDestructionTimeout() {
        return applicationDestructionTimeout;
    }

    /**
     * @return the applicationDestructionWait
     */
    public Integer getApplicationDestructionWait() {
        return applicationDestructionWait;
    }

    /**
     * @return the instanceReservationTimeout
     */
    public Integer getInstanceReservationTimeout() {
        return instanceReservationTimeout;
    }

    /**
     * @return the instanceReservationWait
     */
    public Integer getInstanceReservationWait() {
        return instanceReservationWait;
    }

    /**
     * @return the instanceHealthCheck
     */
    public aqhaInstanceHealthCheck getInstanceHealthCheck() {
        return instanceHealthCheck;
    }
}
