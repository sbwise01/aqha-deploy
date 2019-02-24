package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.CreateGrantRequest;
import com.amazonaws.services.kms.model.GrantConstraints;
import com.amazonaws.services.kms.model.GrantListEntry;
import com.amazonaws.services.kms.model.ListGrantsRequest;
import com.amazonaws.services.kms.model.ListGrantsResult;
import com.amazonaws.services.kms.model.RevokeGrantRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
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
import java.util.HashMap;

/**
 *
 * @author sbwise01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class aqhaChefSoloBootstrap {
    private final String configurationDirectory;
    private final String bucket;
    private final String kmsKeyId;

    @JsonCreator
    public aqhaChefSoloBootstrap(@JsonProperty("configurationDirectory") String configurationDirectory,
            @JsonProperty("bucket") String bucket,
            @JsonProperty("kmsKeyId") String kmsKeyId) {
        this.configurationDirectory = configurationDirectory;
        this.bucket = bucket;
        this.kmsKeyId = kmsKeyId;
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, archiveFile);
        if(createGrant(configuration)) {
            putObjectRequest.setSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(kmsKeyId));
        }
        client.putObject(putObjectRequest);

        if(createGrant(configuration)) {
            AWSKMSClient kmsClient = Client.getKmsClient(configuration.getRegion());
            String s3Arn = "arn:aws:s3:::" + bucket + "/" + key;
            HashMap<String, String> encryptionContextSubset = new HashMap<>();
            encryptionContextSubset.put("aws:s3:arn", s3Arn);
            CreateGrantRequest request = new CreateGrantRequest()
                    .withKeyId(kmsKeyId)
                    .withGranteePrincipal(aqhaIamRole.getRoleArn(configuration))
                    .withOperations("Decrypt")
                    .withConstraints(new GrantConstraints()
                            .withEncryptionContextSubset(encryptionContextSubset))
                    .withName(launchTemplateName);
            kmsClient.createGrant(request);
        }

        //Remove localtar.gz file
        archiveFile.delete();
    }
    
    public void destroyConfiguration(aqhaConfiguration configuration, String launchTemplateName) {
        if(createGrant(configuration)) {
            AWSKMSClient kmsClient = Client.getKmsClient(configuration.getRegion());
            ListGrantsResult results = kmsClient.listGrants(
                    new ListGrantsRequest().withKeyId(kmsKeyId)
            );
            for(GrantListEntry grant : results.getGrants()) {
                if(grant.getName().equalsIgnoreCase(launchTemplateName)) {
                    kmsClient.revokeGrant(
                            new RevokeGrantRequest()
                                    .withKeyId(kmsKeyId)
                                    .withGrantId(grant.getGrantId())
                    );
                }
            }
        }
        AmazonS3Client client = Client.getS3Client(configuration.getRegion());
        client.deleteObject(bucket, getKey(launchTemplateName));
    }
    
    private Boolean createGrant(aqhaConfiguration configuration) {
        return kmsKeyId != null && configuration.getRoleNameForInstanceProfile() != null;
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

    /**
     * @return the kmsKeyId
     */
    public String getKmsKeyId() {
        return kmsKeyId;
    }
}
