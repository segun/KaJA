/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trinisoftinc.kannel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class <code>KaJA</code> allows you to administer Kannel without having to know all the intricasies involved
 * It contains static methods you can call directly and the methods returns the messages recieved from Kannel.
 * @author trinisoftinc
 */
public class KaJA {

    /**
     * The default location where kannel is installed
     */
    public static final String kannelInstallationDirectory = "/usr/local/kannel";
    /**
     * The default location of the config file
     */
    public static final String configFileLocation = "/etc/kannel.conf";
    public static final Logger logger = Logger.getLogger(KaJA.class.getName());
    /**
     * Implementers should put this in a loop to test if the bearer box is still running
     * Check the tests for how.
     */
    public static boolean bearerBoxIsRunning = false;
    /**
     * This stores the messages from starting the bearer box
     */
    public static StringBuffer outputBufferBearerBox = new StringBuffer();

    /**
     * This method is used to start the bearer box. There are three versions since java doesn't support default
     * parameters in methods
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @param configFileLocation the location of the config file. The default is /etc/kannel.conf
     * @return
     */
    public static void startBearerBox(final String kannelInstallationDirectory, final String configFileLocation) throws IOException {
        bearerBoxIsRunning = true;        
        new Thread() {

            @Override
            public void run() {
                try {
                    String start_stop_daemon = kannelInstallationDirectory + File.separator + "sbin" + File.separator + "start-stop-daemon --start --exec ";
                    String bearerbox = kannelInstallationDirectory + File.separator + "sbin" + File.separator + "bearerbox -- ";

                    String command = start_stop_daemon + bearerbox + configFileLocation;

                    Process p = Runtime.getRuntime().exec(command);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = "";
                    logger.log(Level.OFF, "----Starting INPUT STREAM LOG------");
                    while ((line = reader.readLine()) != null) {
                        outputBufferBearerBox.append(line);
                        logger.log(Level.INFO, line);
                    }
                    logger.log(Level.OFF, "----Ending INPUT STREAM LOG------");
                    //if we get here, it is either that the bearerbox has stopped running
                    //or that there is an error starting it

                    reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    line = "";
                    logger.log(Level.OFF, "----Starting Error STREAM LOG------");
                    while ((line = reader.readLine()) != null) {
                        outputBufferBearerBox.append(line);
                        logger.log(Level.SEVERE, line);
                    }
                    logger.log(Level.OFF, "----Stopping Error STREAM LOG------");
                    p.destroy();
                    bearerBoxIsRunning = false;
                    logger.log(Level.OFF, null, bearerBoxIsRunning);
                } catch (IOException ex) {
                    bearerBoxIsRunning = false;
                    Logger.getLogger(KaJA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    public static void startBearerBox(String configFileLocation) throws IOException {
        startBearerBox(kannelInstallationDirectory, configFileLocation);
    }

    public static void startBearerBox() throws IOException {
        startBearerBox(kannelInstallationDirectory, configFileLocation);
    }

    /**
     * This method is used to start the beare box. There are three versions since java doesn't support default
     * parameters in methods
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @param configFileLocation the location of the config file. The default is /etc/kannel.conf
     * @return
     */
    public static boolean stopBearerBox(final String kannelInstallationDirectory, final String configFileLocation) throws IOException, InterruptedException {
        String start_stop_daemon = kannelInstallationDirectory + File.separator + "sbin" + File.separator + "start-stop-daemon --start --exec ";
        String bearerbox = kannelInstallationDirectory + File.separator + "sbin" + File.separator + "bearerbox -- ";

        String command = start_stop_daemon + bearerbox + configFileLocation;

        Process p = Runtime.getRuntime().exec(command);
        //wait for 30 seconds
        Thread.sleep(30000);

        bearerBoxIsRunning = false;

        return true;
    }

    public static boolean stopBearerBox(String configFileLocation) throws IOException, InterruptedException {
        return stopBearerBox(kannelInstallationDirectory, configFileLocation);
    }

    public static boolean stopBearerBox() throws IOException, InterruptedException {
        return stopBearerBox(kannelInstallationDirectory, configFileLocation);
    }

    /**
     *
     * @param format this is the format of the return value. It can either be txt, html, wml, or xml
     * @param kannelHostIp this is the hostname(resolvable) or ip address of the kannel host machine
     * @param kannelPort this is the admin-port specified in the core group of kannel configuration file
     * @param password this is the password specified in the core group of kannel configuration file. It can either be the main admin password which works for all command or the status password which works for only status commands
     * @param command this is the command to execute on kannel
     * @param otherParameters any other parameters required to fulfill the request. For example start-smsc require a smsc parameter
     * @return the result of execution of <code>command</code> on kannel. This does no checks. So the implementation must do the checks
     * @throws MalformedURLException
     * @throws IOException
     */
    private static String callKannel(String format, String kannelHostIp, String kannelPort, String password, String command, String otherParameters) throws MalformedURLException, IOException {
        String urlString = "http://" + kannelHostIp + ":" + kannelPort + "/" + command + "." + format + "?password=" + password;
        if(otherParameters != null) {
            urlString += "&" + otherParameters;
        }
        logger.log(Level.INFO, urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line = "";
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer = buffer.append(line).append("\n");
        }
        return buffer.toString();
    }

    /**
     * This method calls callKannel with command parameter set to
     * <code>status</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String status(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "status", null);
    }

    /**
     * This method calls callKannel with command parameter set to
     * <code>store-status</code><br />
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String storeStatus(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "store-status", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>suspend</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String suspend(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "suspend", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>isolate</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String isolate(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "isolate", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>resume</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String resume(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "resume", null);
    }
    
    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>shutdown</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String shutdown(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "shutdown", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>flush-dlr</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String flushDLR(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "flush-dlr", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>start-smsc</code> and otherParameter <code>smsc=<i>smscname</i></code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String startSMSC(String format, String kannelHostIp, String kannelPort, String password, String smscName) throws MalformedURLException, IOException {
        String otherParameter = "smsc=" + smscName;
        return callKannel(format, kannelHostIp, kannelPort, password, "start-smsc", otherParameter);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>stop-smsc</code> and otherParameter <code>smsc=<i>smscname</i></code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String stopSMSC(String format, String kannelHostIp, String kannelPort, String password, String smscName) throws MalformedURLException, IOException {
        String otherParameter = "smsc=" + smscName;
        return callKannel(format, kannelHostIp, kannelPort, password, "stop-smsc", otherParameter);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>restart</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String restart(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "restart", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>log-level</code> and otherParameter <code>level=<i>new level</i></code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String logLevel(String format, String kannelHostIp, String kannelPort, String password, String newLogLevel) throws MalformedURLException, IOException {
        String otherParameter = "level=" + newLogLevel;
        return callKannel(format, kannelHostIp, kannelPort, password, "log-level", otherParameter);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>reload-lists</code><br/>
     * @see com.trinisoftinc.kannel.KaJA.callKannel()
     */
    public static String reloadLists(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "reload-lists", null);
    }
}
