/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.trinisoftinc.kannel;

import java.io.IOException;
import java.net.MalformedURLException;
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

    /**
     * Test of startBearerBox method, of class KaJA.
     */
    //@Test
    public void testStartBearerBox_String_String() throws Exception {
        System.out.println("startBearerBox");
        String kannelInstallationDirectory = "/Users/trinisoftinc/bin";
        String configFileLocation = "";
        KaJA.startBearerBox(kannelInstallationDirectory, configFileLocation);
        while(KaJA.bearerBoxIsRunning) {
            System.out.println("running");
            Thread.sleep(30000);
            KaJA.stopBearerBox();
        }
        System.out.println(KaJA.outputBufferBearerBox);
    }

    @Test
    public void testStatus() throws MalformedURLException, IOException {
        System.out.println("storeStatus");
        String format = "xml";
        String kannelHost = "192.168.0.115";
        String kannelPort = "13000";
        String password = "cyberdeen_status";
        String retval = KaJA.status(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        assertEquals(true, KaJA.bearerBoxIsRunning);
        System.out.println(retval);
    }

    @Test
    public void testStoreStatus() throws MalformedURLException, IOException {
        System.out.println("status");
        String format = "txt";
        String kannelHost = "192.168.0.115";
        String kannelPort = "13000";
        String password = "cyberdeen_status";
        String retval = KaJA.storeStatus(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        assertEquals(true, KaJA.bearerBoxIsRunning);
        System.out.println(retval);
    }

    @Test
    public void testSuspend() throws MalformedURLException, IOException {
        System.out.println("suspend");
        String format = "txt";
        String kannelHost = "192.168.0.115";
        String kannelPort = "13000";
        String password = "cyberdeen";
        String retval = KaJA.suspend(format, kannelHost, kannelPort, password);
        assertNotSame("", retval);
        assertEquals(true, KaJA.bearerBoxIsRunning);
        System.out.println(retval);
    }
}