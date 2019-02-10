package com.bradandmarsha.aqha.deploy;

import com.bradandmarsha.aqha.deploy.resources.aqhaApplication;
import com.bradandmarsha.aqha.deploy.strategies.DeploymentStrategy;
import com.bradandmarsha.aqha.deploy.utils.Log4jHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 *
 * @author sbwise01
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) throws IOException, ParseException, aqhaDeploymentException {
        // Configure logging
        Log4jHelper.configureLogging();

        //Parse command line arguments
        Options options = new Options();
        options.addRequiredOption("c", "configuration", true, "Path to json file containing application configuration information.");
        options.addOption("d", "destroy", false, "Destroy existing deployed application instance");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);
        
        //Map configuration
        ObjectMapper mapper = new ObjectMapper();
        aqhaConfiguration configuration = mapper.readValue(new File(cmd.getOptionValue("c")), aqhaConfiguration.class);
        
        //Fetch all current application instances from account
        List<aqhaApplication> applications = aqhaApplication.retrieveAqhaApplications(configuration);
        applications.forEach((app) -> {
            LOGGER.info("Found application: " + app.toString());
        });
        
        //Check for prior failed deployment
        if (applications.size() > 1) {
            //Prior deployment error ... fail and report
            LOGGER.error("Found " + applications.size() + " instances of application " +
                    configuration.getApplicationName() + " for stack " +
                    configuration.getStackName() + ".  Expected to find 1 instance.");
            System.exit(1);
        }
        
        //Check for no deployments to destroy
        if (applications.size() < 1 && cmd.hasOption("d")) {
            LOGGER.error("Didn't find application " + configuration.getApplicationName() +
                    " for stack " + configuration.getStackName() +
                    " to destroy ... exiting.");
            System.exit(2);
        }

        //Create new and old application objects
        aqhaApplication newApplication = new aqhaApplication(configuration);
        aqhaApplication oldApplication = applications.size() < 1 ? null : applications.get(0);
        
        //Select Deployment Strategy
        DeploymentStrategy strategy = DeploymentStrategy.selectDeploymentStrategy(configuration, cmd.hasOption("d"), oldApplication, newApplication);
        
        //Execute deployment
        strategy.deploy();

        LOGGER.info("Finished.");
    }
}
