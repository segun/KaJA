/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.trinisoftinc.kannel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author trinisoftinc
 */
public class KaJATest {

    static String url = "localhost";
    static String kannelPort = "13000";
    static String smsboxPort = "13014";
    static final Logger logger = Logger.getLogger(KaJATest.class.getName());

    /**
     * Some tests may fail the first time. Just run them again. By then all services that are not started
     * must have been started.
     */
    public KaJATest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStartBearerBox_String_String() throws Exception {
        logger.log(Level.INFO, "startBearerBox");
        String configFileLocation = "";
        KaJA.startBearerBox();
        Thread.sleep(5000);
        //enable this line to test the stop functionality. All other tests should fail
        //KaJA.stopBearerBox();
        logger.log(Level.INFO, "running");
        logger.log(Level.INFO, KaJA.outputBufferBearerBox.toString());
        assertTrue(KaJA.bearerBoxIsRunning);
    }

    @Test
    public void testStartSMSBox_String_String() throws Exception {
        logger.log(Level.INFO, "startSMSBox");
        KaJA.startSMSBox();
        Thread.sleep(5000);
        //enable this line to test the stop functionality. All other tests should fail
        //KaJA.stopSMSBox();
        logger.log(Level.INFO, "running");
        logger.log(Level.INFO, KaJA.outputBufferSMSBox.toString());
        assertTrue(KaJA.smsboxIsRunning);
    }

    @Test
    public void testStartWAPBox_String_String() throws Exception {
        logger.log(Level.INFO, "startWAPBox");
        KaJA.startWAPBox();
        Thread.sleep(5000);
        //enable this line to test the stop functionality. All other tests should fail
        //KaJA.stopSMSBox();
        logger.log(Level.INFO, "running");
        logger.log(Level.INFO, KaJA.outputBufferWAPBox.toString());
        assertTrue(KaJA.wapboxIsRunning);
    }

    @Test
    public void testSendSMS() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "sendSMS");
        String host = url;
        String smsboxUsername = "peter";
        String smsboxPassword = "ford";
        String to = "08089370313";
        String text = "hello world, hello birds";
        String retval = KaJA.sendSMS(host, smsboxPort, smsboxUsername, smsboxPassword, to, text, null);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);

    }

    @Test
    public void testStatus() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "status");
        String format = "xml";
        String kannelHost = url;

        String password = "cyberdeen_status";
        String retval = KaJA.status(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testStoreStatus() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "storeStatus");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen_status";
        String retval = KaJA.storeStatus(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testSuspend() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "suspend");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.suspend(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testIsolate() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "isolate");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.isolate(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testResume() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "resume");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.resume(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testFlushDLR() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "flushDLR");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.suspend(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        retval = KaJA.flushDLR(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testStartSMSC() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "startSMSC");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.startSMSC(format, kannelHost, kannelPort, password, "pavel");
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testStopSMSC() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "stopSMSC");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.stopSMSC(format, kannelHost, kannelPort, password, "pavel");
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testLogLevel() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "logLevel");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.logLevel(format, kannelHost, kannelPort, password, "0");
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testReloadLists() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "reloadLists");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.reloadLists(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    @Test
    public void testRestart() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "restart");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.restart(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }

    //to run this test disable testFlushDLR first. They can not co-exist
    //@Test
    public void testShutdown() throws MalformedURLException, IOException {
        logger.log(Level.INFO, "shutdown");
        String format = "txt";
        String kannelHost = url;

        String password = "cyberdeen";
        String retval = KaJA.shutdown(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        logger.log(Level.INFO, retval);
    }
}
