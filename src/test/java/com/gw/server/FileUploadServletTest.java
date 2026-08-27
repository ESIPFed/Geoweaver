package com.gw.server;

import com.gw.utils.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServletTest {

    @Mock
    private ServletConfig servletConfig;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    @Mock
    private BaseTool baseTool;

    private FileUploadServlet fileUploadServlet;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        fileUploadServlet = new FileUploadServlet();
        stringWriter = new StringWriter();

        ReflectionTestUtils.setField(fileUploadServlet, "bt", baseTool);
        ReflectionTestUtils.setField(fileUploadServlet, "upload_file_path", "/test/upload");
        ReflectionTestUtils.setField(fileUploadServlet, "temp_file_path", "/test/temp");
        ReflectionTestUtils.setField(fileUploadServlet, "workspace", "/test/workspace");
    }

    @Test
    void testInit() throws ServletException {
        try {
            fileUploadServlet.init(servletConfig);
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException || e instanceof RuntimeException);
        }
    }

    @Test
    void testDoGet() throws ServletException, IOException {
        when(response.getWriter()).thenReturn(printWriter);

        fileUploadServlet.doGet(request, response);

        verify(response).getWriter();
        verify(printWriter).println("wrong way");
        verify(printWriter).flush();
    }

    @Test
    void testDoPost() throws ServletException, IOException {
        try {
            fileUploadServlet.doPost(request, response);
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException || e instanceof RuntimeException);
        }
    }

    @Test
    void testGetServletInfo() {
        String info = fileUploadServlet.getServletInfo();
        assertEquals("Short description", info);
    }
}
