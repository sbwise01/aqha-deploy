package com.bradandmarsha.aqha.deploy.utils;

import java.net.UnknownHostException;
import org.apache.log4j.MDC;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 *
 * @author sbwise01
 */
public class Log4jHelper {
    public static void configureLogging() {
        // Bride ALL JUL calls to Log4j
        SLF4JBridgeHandler.install();

        // Define Log4j logging
        DOMConfigurator.configureAndWatch("log4j.xml");

        String serverName = null;

        try {
            serverName = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.err.println("Unknown host exception found while getting host name of sever " + e);
        } catch (Exception e) {
            System.err.println("Exception while getting host name of sever " + e);
        }

        if (serverName != null) {
            MDC.put("serverName", serverName);
        }
    }
}
