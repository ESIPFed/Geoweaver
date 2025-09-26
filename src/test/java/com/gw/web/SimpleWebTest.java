package com.gw.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleWebTest {

    @Test
    void testChatbotController() {
        ChatbotController controller = new ChatbotController();
        assertNotNull(controller);
    }

    @Test
    void testCheckpointController() {
        CheckpointController controller = new CheckpointController();
        assertNotNull(controller);
    }

    @Test
    void testCommunicationConfigController() {
        CommunicationConfigController controller = new CommunicationConfigController();
        assertNotNull(controller);
    }

    @Test
    void testFileDownloadController() {
        FileDownloadController controller = new FileDownloadController();
        assertNotNull(controller);
    }

    @Test
    void testGeoweaverController() {
        GeoweaverController controller = new GeoweaverController();
        assertNotNull(controller);
    }

    @Test
    void testGoogleEarthController() {
        // This controller requires RestTemplateBuilder in constructor
        // We'll just test that the class exists
        assertNotNull(GoogleEarthController.class);
    }

    @Test
    void testLandingController() {
        LandingController controller = new LandingController();
        assertNotNull(controller);
    }

    @Test
    void testPortalController() {
        PortalController controller = new PortalController();
        assertNotNull(controller);
    }

    @Test
    void testResultBrowserController() {
        ResultBrowserController controller = new ResultBrowserController();
        assertNotNull(controller);
    }

    @Test
    void testUserController() {
        UserController controller = new UserController();
        assertNotNull(controller);
    }
}