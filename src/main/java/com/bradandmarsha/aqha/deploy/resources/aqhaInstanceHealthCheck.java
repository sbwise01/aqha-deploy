package com.bradandmarsha.aqha.deploy.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.net.URISyntaxException;
import org.apache.http.client.utils.URIBuilder;

/**
 *
 * @author sbwise01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class aqhaInstanceHealthCheck {
    private final String protocol;
    private final Integer port;
    private final String path;
    private final Boolean usePublicAddress;

    @JsonCreator
    public aqhaInstanceHealthCheck(@JsonProperty("protocol") String protocol,
            @JsonProperty("port") Integer port,
            @JsonProperty("path") String path,
            @JsonProperty("usePublicAddress") Boolean usePublicAddress) {
        this.protocol = protocol;
        this.port = port;
        this.path = path;
        this.usePublicAddress = usePublicAddress;
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
    
    public String getHealthCheckUrl(String host) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol);
        builder.setHost(host);
        builder.setPort(port);
        builder.setPath(path);
        return builder.build().toString();
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the usePublicAddress
     */
    public Boolean getUsePublicAddress() {
        return usePublicAddress;
    }

}
