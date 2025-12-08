package io.icebrew.vite.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.icebrew.vite.config.ViteProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filter for proxying WebSocket and HMR requests to Vite dev server
 */
public class ViteHmrProxyFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ViteHmrProxyFilter.class);

    private final ViteProperties viteProperties;
    private final Environment environment;

    public ViteHmrProxyFilter(ViteProperties viteProperties, Environment environment) {
        this.viteProperties = viteProperties;
        this.environment = environment;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Check if this is a WebSocket upgrade request for HMR
        if (isWebSocketRequest(httpRequest) && isDevelopmentMode()) {
            logger.debug("WebSocket/HMR request detected: {}", httpRequest.getRequestURI());
            proxyWebSocketRequest(httpRequest, httpResponse);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isWebSocketRequest(HttpServletRequest request) {
        String upgrade = request.getHeader("Upgrade");
        String connection = request.getHeader("Connection");

        return upgrade != null && upgrade.toLowerCase().contains("websocket") &&
                connection != null && connection.toLowerCase().contains("upgrade");
    }

    private boolean isDevelopmentMode() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return activeProfiles.isEmpty() || activeProfiles.contains("dev") || activeProfiles.contains("development");
    }

    private void proxyWebSocketRequest(HttpServletRequest request, HttpServletResponse response) {
        // For WebSocket connections, we need to let the client know to connect directly
        // to Vite
        // This is handled by injecting the Vite client script which manages HMR
        // connections
        response.setStatus(HttpServletResponse.SC_SWITCHING_PROTOCOLS);
        logger.info("WebSocket upgrade for HMR - client should connect to: {}", viteProperties.getDevServerUrl());
    }
}
