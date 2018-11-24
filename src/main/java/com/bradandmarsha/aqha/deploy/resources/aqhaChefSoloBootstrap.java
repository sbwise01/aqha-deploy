package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.s3.AmazonS3Client;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.utils.Client;
import com.bradandmarsha.aqha.deploy.utils.TarUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author sbwise01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class aqhaChefSoloBootstrap {
    private final String configurationDirectory;
    private final String bucket;

    @JsonCreator
    public aqhaChefSoloBootstrap(@JsonProperty("configurationDirectory") String configurationDirectory,
            @JsonProperty("bucket") String bucket) {
        this.configurationDirectory = configurationDirectory;
        this.bucket = bucket;
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
    
    public void uploadConfiguration(aqhaConfiguration configuration, String launchTemplateName) throws IOException {
        //Create tar.gz file
        String key = getKey(launchTemplateName);
        String archiveFileName = configurationDirectory + File.separator + key;
        TarUtil.compressDirectoryChildren(archiveFileName, configurationDirectory);

        //Upload to S3 bucket
        AmazonS3Client client = Client.getS3Client(configuration.getRegion());
        File archiveFile = new File(archiveFileName);
        client.putObject(bucket, key, archiveFile);
        
        //Remove localtar.gz file
        archiveFile.delete();
    }
    
    public void destroyConfiguration(aqhaConfiguration configuration, String launchTemplateName) {
        AmazonS3Client client = Client.getS3Client(configuration.getRegion());
        client.deleteObject(bucket, getKey(launchTemplateName));
    }
    
    public String getKey(String launchTemplateName) {
        return launchTemplateName + ".tar.gz";
    }
    
    /**
     * @return the configurationDirectory
     */
    public String getConfigurationDirectory() {
        return configurationDirectory;
    }

    /**
     * @return the bucket
     */
    public String getBucket() {
        return bucket;
    }
}
