package com.bradandmarsha.aqha.deploy.resources;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateLaunchTemplateRequest;
import com.amazonaws.services.ec2.model.CreateLaunchTemplateResult;
import com.amazonaws.services.ec2.model.DeleteLaunchTemplateRequest;
import com.amazonaws.services.ec2.model.DescribeLaunchTemplateVersionsRequest;
import com.amazonaws.services.ec2.model.DescribeLaunchTemplatesRequest;
import com.amazonaws.services.ec2.model.LaunchTemplate;
import com.amazonaws.services.ec2.model.LaunchTemplateIamInstanceProfileSpecificationRequest;
import com.amazonaws.services.ec2.model.LaunchTemplateVersion;
import com.amazonaws.services.ec2.model.RequestLaunchTemplateData;
import com.bradandmarsha.aqha.deploy.aqhaConfiguration;
import com.bradandmarsha.aqha.deploy.utils.Client;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author sbwise01
 */
public class aqhaLaunchTemplate {
    private final LaunchTemplate launchTemplate;
    private final LaunchTemplateVersion launchTemplateVersion;
    
    public aqhaLaunchTemplate(LaunchTemplate launchTemplate, LaunchTemplateVersion launchTemplateVersion) {
        this.launchTemplate = launchTemplate;
        this.launchTemplateVersion = launchTemplateVersion;
    }
    
    public void destroy(aqhaConfiguration configuration) {
        AmazonEC2Client client = Client.getEC2Client(configuration.getRegion());
        DeleteLaunchTemplateRequest request = new DeleteLaunchTemplateRequest()
                .withLaunchTemplateId(launchTemplate.getLaunchTemplateId());
        client.deleteLaunchTemplate(request);
        if (configuration.getChefSoloBootstrap() != null) {
            configuration.getChefSoloBootstrap().destroyConfiguration(configuration, getLaunchTemplateName());
        }
    }

    public static List<aqhaLaunchTemplate> retrieveLaunchTemplates(aqhaConfiguration configuration) {
        List<aqhaLaunchTemplate> launchTemplates = new ArrayList<>();
        AmazonEC2Client client = Client.getEC2Client(configuration.getRegion());
        //TODO:  implement windowing
        DescribeLaunchTemplatesRequest request = new DescribeLaunchTemplatesRequest()
                .withMaxResults(100);
        //TODO:  handle HTTP response code errors
        client.describeLaunchTemplates(request).getLaunchTemplates()
                .forEach((lt) -> {
            if (lt.getLaunchTemplateName().toLowerCase().startsWith(aqhaApplication.getNamePrefix(configuration))) {
                launchTemplates.add(new aqhaLaunchTemplate(lt, retrieveLaunchTemplateVersion(configuration, lt.getLaunchTemplateId())));
            }
        });
        
        return launchTemplates;
    }
    
    private static LaunchTemplateVersion retrieveLaunchTemplateVersion(aqhaConfiguration configuration, String launchTemplateId) {
        LaunchTemplateVersion launchTemplateVersion = null;
        AmazonEC2Client client = Client.getEC2Client(configuration.getRegion());
        DescribeLaunchTemplateVersionsRequest ltvrequest = new DescribeLaunchTemplateVersionsRequest()
                .withLaunchTemplateId(launchTemplateId);
        for (LaunchTemplateVersion ltv : client.describeLaunchTemplateVersions(ltvrequest).getLaunchTemplateVersions()) {
            if (ltv.isDefaultVersion()) {
                launchTemplateVersion = ltv;
                break;
            }
        }

        return launchTemplateVersion;
    }
    
    public static aqhaLaunchTemplate createNewLaunchTemplate(aqhaConfiguration configuration, String launchTemplateName) throws IOException {
        AmazonEC2Client client = Client.getEC2Client(configuration.getRegion());
        RequestLaunchTemplateData launchTemplateData = new RequestLaunchTemplateData()
                .withImageId(configuration.getAmiId())
                .withInstanceType(configuration.getInstanceType())
                .withSecurityGroupIds(configuration.getSecurityGroupIds())
                .withKeyName(configuration.getKeyName());
        //Add user data
        if (configuration.getChefSoloBootstrap() != null) {
            configuration.getChefSoloBootstrap().uploadConfiguration(configuration, launchTemplateName);
            JSONObject userData = new JSONObject();
            userData.put("bucket", configuration.getChefSoloBootstrap().getBucket());
            userData.put("key", configuration.getChefSoloBootstrap().getKey(launchTemplateName));
            launchTemplateData.setUserData(Base64.getEncoder().encodeToString(userData.toJSONString().getBytes()));
        }
        //Add instance profile
        if (configuration.getInstanceProfile() != null) {
            LaunchTemplateIamInstanceProfileSpecificationRequest iamInstanceProfile =
                    new LaunchTemplateIamInstanceProfileSpecificationRequest()
                    .withArn(configuration.getInstanceProfile());
            launchTemplateData.setIamInstanceProfile(iamInstanceProfile);
        }
        CreateLaunchTemplateRequest request = new CreateLaunchTemplateRequest()
                .withLaunchTemplateName(launchTemplateName)
                .withLaunchTemplateData(launchTemplateData);
        CreateLaunchTemplateResult result = client.createLaunchTemplate(request);
        
        return new aqhaLaunchTemplate(result.getLaunchTemplate(),
                retrieveLaunchTemplateVersion(configuration, result.getLaunchTemplate().getLaunchTemplateId()));
    }

    @Override
    public String toString() {
        return "aqhaLaunchTemplate = " + getLaunchTemplate().toString() +
                "\n     LaunchTemplateVersion = " + getLaunchTemplateVersion().toString();
    }
    
    public String getLaunchTemplateId() {
        return getLaunchTemplate().getLaunchTemplateId();
    }

    public String getLaunchTemplateName() {
        return getLaunchTemplate().getLaunchTemplateName();
    }
    
    public String getAmiId() {
        return getLaunchTemplateVersion().getLaunchTemplateData().getImageId();
    }
    
    public String getInstanceType() {
        return getLaunchTemplateVersion().getLaunchTemplateData().getInstanceType();
    }
    
    public String getKeyName() {
        return getLaunchTemplateVersion().getLaunchTemplateData().getKeyName();
    }

    /**
     * @return the launchTemplate
     */
    public LaunchTemplate getLaunchTemplate() {
        return launchTemplate;
    }

    /**
     * @return the launchTemplateVersion
     */
    public LaunchTemplateVersion getLaunchTemplateVersion() {
        return launchTemplateVersion;
    }
}
