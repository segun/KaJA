/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trinisoftinc.kannel;

import com.trinisoft.libraries.PropertyHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
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
     * The default location of the kannel config file
     */
    public static final String configFileLocation = "/etc/kannel.conf";

    /**
     * The default location of the smsbox config file
     */
    public static final String smsboxConfigFileLocation = "/etc/kannel.conf";

    /**
     * The default location of the wapbox config file
     */
    public static final String wapboxConfigFileLocation = "/etc/kannel.conf";
    
    public static final Logger logger = Logger.getLogger(KaJA.class.getName());

    /**
     * Implementers should put this in a threaded loop to test if the bearer box is still running
     * Check the tests for how.
     */
    public static boolean bearerBoxIsRunning = false;
    /**
     * This variable is ONLY true if there's an error during start of bearer box
     */
    public static boolean bearerBoxError = false;

    /**
     * Implementers should put this in a threaded loop to test if the smsbox is still running
     * Check the tests for how.
     */
    public static boolean smsboxIsRunning = false;
    /**
     * This variable is ONLY true if there's an error during start of sms box
     */
    public static boolean smsBoxError = true;

    /**
     * Implementers should put this in a threaded loop to test if the wapbox is still running
     * Check the tests for how.
     */
    public static boolean wapboxIsRunning = false;
    /**
     * This variable is ONLY true if there's an error during start of wap box
     */
    public static boolean wapBoxError = true;

    /**
     * This stores the messages from starting the bearer box
     */
    public static StringBuilder outputBufferBearerBox = new StringBuilder();

    /**
     * This stores the messages from starting the smsbox
     */
    public static StringBuilder outputBufferSMSBox = new StringBuilder();

    /**
     * This stores the messages from starting the wapbox
     */
    public static StringBuilder outputBufferWAPBox = new StringBuilder();

    /**
     * Log files are where we pull log information from.
     */
    public static String kannelLogFile = "/var/log/kannel/kannel.log";
    public static String smsboxLogFile = "/var/log/kannel/smsbox.log";
    public static String wapboxLogFile = "/var/log/kannel/wapbox.log";

    static {
        try {
            PropertyHelper helper = new PropertyHelper();
            Properties props = helper.getProperties("/etc/KaJA.properties");
            kannelLogFile = props.getProperty("kannel-log-file");
            smsboxLogFile = props.getProperty("smsbox-log-file");
            wapboxLogFile = props.getProperty("wapbox-log-file");
        } catch (IOException ex) {
            Logger.getLogger(KaJA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * This method is used to start the bearer box. There are three versions since java doesn't support default
     * parameters in methods. Implementors should wait a moment before testing if the bearerbox is running using
     * the bearerBoxIsRunning variable because of network latency. A reasonable time would be 30 seconds.
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @param configFileLocation the location of the config file. The default is /etc/kannel.conf
     */
    public static void startBearerBox(final String kannelInstallationDirectory, final String configFileLocation) {
        bearerBoxIsRunning = true;  
        new Thread() {

            @Override
            public void run() {
                try {
                    String start_stop_daemon = concat(kannelInstallationDirectory, File.separator, 
                            "sbin", File.separator, "start-stop-daemon --start --exec ");
                    String bearerbox = concat(kannelInstallationDirectory, File.separator, "sbin",
                            File.separator, "bearerbox -- ");

                    String command = concat(start_stop_daemon, bearerbox, configFileLocation);

                    Runtime.getRuntime().exec(command);
                    
                    command = concat("tail -f ", kannelLogFile);
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
                    bearerBoxError = true;
                    logger.log(Level.OFF, null, bearerBoxIsRunning);
                } catch (IOException ex) {
                    bearerBoxIsRunning = false;
                    bearerBoxError = true;
                    Logger.getLogger(KaJA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    public static void startBearerBox(String configFileLocation) {
        startBearerBox(kannelInstallationDirectory, configFileLocation);
    }

    public static void startBearerBox() {
        startBearerBox(kannelInstallationDirectory, configFileLocation);
    }

    /**
     * This method is used to stop the bearer box. There are two versions since java doesn't support default
     * parameters in methods
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel 
     * @return true if the stop command is successful
     */
    public static boolean stopBearerBox(final String kannelInstallationDirectory) throws IOException, InterruptedException {
        String start_stop_daemon = concat(kannelInstallationDirectory, File.separator, "sbin", File.separator, "start-stop-daemon --stop --exec ");
        String bearerbox = concat(kannelInstallationDirectory, File.separator, "sbin", File.separator, "bearerbox");

        String command = concat(start_stop_daemon, bearerbox);

        Process p = Runtime.getRuntime().exec(command);
        //wait for 30 seconds
        Thread.sleep(30000);

        bearerBoxIsRunning = false;

        return true;
    }

    public static boolean stopBearerBox() throws IOException, InterruptedException {
        return stopBearerBox(kannelInstallationDirectory);
    }

    /**
     * This method is used to start the smsbox. There are three versions since java doesn't support default
     * parameters in methods. Implementors should wait a moment before testing if the smsbox is running using
     * the smsboxIsRunning variable because of network latency. A reasonable time would be 30 seconds.
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @param smsboxConfigFileLocation the location of the config file. The default is /etc/kannel.conf
     */
    public static void startSMSBox(final String kannelInstallationDirectory, final String smsboxConfigFileLocation) {
        smsboxIsRunning = true;
        new Thread() {

            @Override
            public void run() {
                try {
                    String start_stop_daemon = concat(kannelInstallationDirectory, File.separator,
                            "sbin", File.separator, "start-stop-daemon --start --exec ");
                    String smsbox = concat(kannelInstallationDirectory, File.separator, "sbin",
                            File.separator, "smsbox -- ");

                    String command = concat(start_stop_daemon, smsbox, smsboxConfigFileLocation);

                    Runtime.getRuntime().exec(command);

                    command = concat("tail -f ", smsboxLogFile);
                    Process p = Runtime.getRuntime().exec(command);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = "";
                    logger.log(Level.OFF, "----Starting INPUT STREAM LOG------");
                    while ((line = reader.readLine()) != null) {
                        outputBufferSMSBox.append(line);
                        logger.log(Level.INFO, line);
                    }
                    logger.log(Level.OFF, "----Ending INPUT STREAM LOG------");
                    //if we get here, it is either that the smsbox has stopped running
                    //or that there is an error starting it

                    reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    line = "";
                    logger.log(Level.OFF, "----Starting Error STREAM LOG------");
                    while ((line = reader.readLine()) != null) {
                        outputBufferSMSBox.append(line);
                        logger.log(Level.SEVERE, line);
                    }
                    logger.log(Level.OFF, "----Stopping Error STREAM LOG------");
                    p.destroy();
                    smsboxIsRunning = false;
                    smsBoxError = true;
                    logger.log(Level.OFF, null, smsboxIsRunning);
                } catch (IOException ex) {
                    smsboxIsRunning = false;
                    smsBoxError = true;
                    Logger.getLogger(KaJA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    public static void startSMSBox(String configFileLocation) {
        startSMSBox(kannelInstallationDirectory, configFileLocation);
    }

    public static void startSMSBox() {
        startSMSBox(kannelInstallationDirectory, configFileLocation);
    }

    /**
     * This method is used to stop the smsbox. There are two versions since java doesn't support default
     * parameters in methods
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @return true if the stop command is successful
     */
    public static boolean stopSMSBox(final String kannelInstallationDirectory) throws IOException, InterruptedException {
        String start_stop_daemon = concat(kannelInstallationDirectory, File.separator, "sbin", File.separator, "start-stop-daemon --stop --exec ");
        String smsbox = concat(kannelInstallationDirectory, File.separator, "sbin", File.separator, "smsbox");

        String command = concat(start_stop_daemon, smsbox);

        Process p = Runtime.getRuntime().exec(command);
        //wait for 30 seconds
        Thread.sleep(30000);

        smsboxIsRunning = false;

        return true;
    }

    public static boolean stopSMSBox() throws IOException, InterruptedException {
        return stopSMSBox(kannelInstallationDirectory);
    }
    

    /**
     * This method is used to start the wapbox. There are three versions since java doesn't support default
     * parameters in methods. Implementors should wait a moment before testing if the wapbox is running using
     * the wapboxIsRunning variable because of network latency. A reasonable time would be 30 seconds.
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @param wapboxConfigFileLocation the location of the config file. The default is /etc/kannel.conf
     */
    public static void startWAPBox(final String kannelInstallationDirectory, final String wapboxConfigFileLocation) {
        wapboxIsRunning = true;
        new Thread() {

            @Override
            public void run() {
                try {
                    String start_stop_daemon = concat(kannelInstallationDirectory, File.separator,
                            "sbin", File.separator, "start-stop-daemon --start --exec ");
                    String wapbox = concat(kannelInstallationDirectory, File.separator, "sbin",
                            File.separator, "wapbox -- ");

                    String command = concat(start_stop_daemon, wapbox, wapboxConfigFileLocation);

                    Runtime.getRuntime().exec(command);

                    command = concat("tail -f ", wapboxLogFile);
                    Process p = Runtime.getRuntime().exec(concat("tail -f ", wapboxLogFile));
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = "";
                    logger.log(Level.OFF, "----Starting INPUT STREAM LOG------");
                    while ((line = reader.readLine()) != null) {
                        outputBufferWAPBox.append(line);
                        logger.log(Level.INFO, line);
                    }
                    logger.log(Level.OFF, "----Ending INPUT STREAM LOG------");
                    //if we get here, it is either that the wapbox has stopped running
                    //or that there is an error starting it

                    reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    line = "";
                    logger.log(Level.OFF, "----Starting Error STREAM LOG------");
                    while ((line = reader.readLine()) != null) {
                        outputBufferWAPBox.append(line);
                        logger.log(Level.SEVERE, line);
                    }
                    logger.log(Level.OFF, "----Stopping Error STREAM LOG------");
                    p.destroy();
                    wapboxIsRunning = false;
                    wapBoxError = true;
                    logger.log(Level.OFF, null, wapboxIsRunning);
                } catch (IOException ex) {
                    wapboxIsRunning = false;
                    wapBoxError = true;
                    Logger.getLogger(KaJA.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    public static void startWAPBox(String configFileLocation) {
        startWAPBox(kannelInstallationDirectory, configFileLocation);
    }

    public static void startWAPBox() {
        startWAPBox(kannelInstallationDirectory, configFileLocation);
    }

    /**
     * This method is used to stop the wapbox. There are two versions since java doesn't support default
     * parameters in methods
     * @param kannelInstallationDirectory the directory where kannel is installed. The default is
     * /usr/local/kannel
     * @return true if the stop command is successful
     */
    public static boolean stopWAPBox(final String kannelInstallationDirectory) throws IOException, InterruptedException {
        String start_stop_daemon = concat(kannelInstallationDirectory, File.separator, "sbin", File.separator, "start-stop-daemon --stop --exec ");
        String wapbox = concat(kannelInstallationDirectory, File.separator, "sbin", File.separator, "wapbox");

        String command = concat(start_stop_daemon, wapbox);

        Process p = Runtime.getRuntime().exec(command);
        //wait for 30 seconds
        Thread.sleep(30000);

        wapboxIsRunning = false;

        return true;
    }

    public static boolean stopWAPBox() throws IOException, InterruptedException {
        return stopWAPBox(kannelInstallationDirectory);
    }
    
    /**
     * Calls a url
     *
     * @param urlString the url to call
     * @return the result of calling urlString
     * @throws MalformedURLException
     * @throws IOException
     */
    private static String callURL(String urlString) throws MalformedURLException, IOException {
        logger.log(Level.INFO, urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String line = "";
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer = buffer.append(line);
        }
        return buffer.toString();
    }

    /**
     * Calls a kannel method using the url http get method
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
        String urlString = concat("http://", kannelHostIp, ":", kannelPort, "/", command, ".", format, "?password=", URLEncoder.encode(password, "UTF-8"));
        if(otherParameters != null) {
            urlString = concat(urlString, "&", otherParameters);
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
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String status(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "status", null);
    }

    /**
     * This method calls callKannel with command parameter set to
     * <code>store-status</code><br />
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String storeStatus(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "store-status", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>suspend</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String) 
     */
    public static String suspend(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "suspend", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>isolate</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String isolate(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "isolate", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>resume</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String resume(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "resume", null);
    }
    
    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>shutdown</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String shutdown(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "shutdown", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>flush-dlr</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String flushDLR(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "flush-dlr", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>start-smsc</code> and otherParameter <code>smsc=<i>smscname</i></code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String startSMSC(String format, String kannelHostIp, String kannelPort, String password, String smscName) throws MalformedURLException, IOException {
        String otherParameter = concat("smsc=", smscName);
        return callKannel(format, kannelHostIp, kannelPort, password, "start-smsc", otherParameter);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>stop-smsc</code> and otherParameter <code>smsc=<i>smscname</i></code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String stopSMSC(String format, String kannelHostIp, String kannelPort, String password, String smscName) throws MalformedURLException, IOException {
        String otherParameter = concat("smsc=", smscName);
        return callKannel(format, kannelHostIp, kannelPort, password, "stop-smsc", otherParameter);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>restart</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String restart(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "restart", null);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>log-level</code> and otherParameter <code>level=<i>new level</i></code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String logLevel(String format, String kannelHostIp, String kannelPort, String password, String newLogLevel) throws MalformedURLException, IOException {
        String otherParameter = concat("level=" , newLogLevel);
        return callKannel(format, kannelHostIp, kannelPort, password, "log-level", otherParameter);
    }

    /**
     * This method calls callKannel with <code>command<code> parameter set to
     * <code>reload-lists</code><br/>
     * @see #callKannel(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static String reloadLists(String format, String kannelHostIp, String kannelPort, String password) throws MalformedURLException, IOException {
        return callKannel(format, kannelHostIp, kannelPort, password, "reload-lists", null);
    }

    /**
     * Sends an sms
     * @param sendsmsHost the smsbox(kannel) host
     * @param sendsmsPort the smsbox port as specified in the smsbox group of kannel config file
     * @param sendsmsUsername username as provided in the sendsms-user group of kannel config file
     * @param sendsmsPassword password (see username)
     * @param to the recipient of the sms
     * @param text the sms to send
     * @param otherOptions kannel provides many other optional parameters. put these in a map(key, value)
     * where key is the parameter name and value is the parameter value
     * @return the result of calling the kannel sendsms url with the values specified
     */
    public static String sendSMS(String sendsmsHost, String sendsmsPort, String sendsmsUsername, String sendsmsPassword, String to, String text, HashMap<String, String> otherOptions) throws MalformedURLException, IOException {
        String sendStatus = "";
        String url = concat("http://", sendsmsHost, ":", sendsmsPort, "/cgi-bin/sendsms?");
        String params = concat("username=", URLEncoder.encode(sendsmsUsername, "UTF-8"),
                "&password=", URLEncoder.encode(sendsmsPassword, "UTF-8"), "&to=", to,
                "&text=", URLEncoder.encode(text, "UTF-8"));
        if(otherOptions != null) {
            Set<String> keys = otherOptions.keySet();
            for(String k: keys) {
                String v = otherOptions.get(k);
                params += concat("&", k, "=", URLEncoder.encode(v, "UTF-8"));
            }
        }
        sendStatus = callURL(concat(url, params));
        return sendStatus;
    }

    /**
     * This method is a helper method. What it does is concat a list of strings 
     * to form a single string
     *
     * @param strings the strings to concatenate
     * @return the concatenated String
     */
    private static String concat(String ... strings) {
        String retval = "";
        for(String s: strings) {
            retval += s;
        }
        return retval;
    }
}
