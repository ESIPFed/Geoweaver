package com.gw.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    @Timeout(10)
    void testGetSiteURLWithValidReferer() {
        // Given
        String referer = "https://example.com:8080/path/to/resource";
        String servletPath = "/path/to/resource";

        when(request.getHeader("referer")).thenReturn(referer);
        when(request.getServletPath()).thenReturn(servletPath);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("example.com"));
        assertTrue(result.contains("8080"));
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithRefererEndingWithSlash() {
        // Given
        String referer = "https://example.com:8080/";
        String servletPath = "/";

        when(request.getHeader("referer")).thenReturn(referer);
        when(request.getServletPath()).thenReturn(servletPath);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertTrue(result.endsWith("/"));
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithRefererNotEndingWithSlash() {
        // Given
        String referer = "https://example.com:8080";
        String servletPath = "";

        when(request.getHeader("referer")).thenReturn(referer);
        when(request.getServletPath()).thenReturn(servletPath);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertTrue(result.endsWith("/"));
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithSpacesInReferer() {
        // Given
        String referer = "https://example.com:8080/path with spaces";
        String servletPath = "/path with spaces";

        when(request.getHeader("referer")).thenReturn(referer);
        when(request.getServletPath()).thenReturn(servletPath);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertFalse(result.contains(" "));
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithNullReferer() {
        // Given
        when(request.getHeader("referer")).thenReturn(null);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("NullPointerException"));
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithException() {
        // Given
        when(request.getHeader("referer")).thenThrow(new RuntimeException("Test exception"));

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Test exception"));
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithEmptyReferer() {
        // Given
        String referer = "";
        String servletPath = "/path";

        when(request.getHeader("referer")).thenReturn(referer);
        when(request.getServletPath()).thenReturn(servletPath);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        // For empty string, after removing servletPath "/path", we get empty string, then add "/"
        assertEquals("/", result);
    }

    @Test
    @Timeout(10)
    void testGetSiteURLWithComplexPath() {
        // Given
        String referer = "https://example.com:8080/very/long/path/to/resource";
        String servletPath = "/very/long/path/to/resource";

        when(request.getHeader("referer")).thenReturn(referer);
        when(request.getServletPath()).thenReturn(servletPath);

        // When
        String result = HttpUtil.getSiteURL(request);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("example.com"));
        assertTrue(result.contains("8080"));
        assertTrue(result.endsWith("/"));
    }
}
