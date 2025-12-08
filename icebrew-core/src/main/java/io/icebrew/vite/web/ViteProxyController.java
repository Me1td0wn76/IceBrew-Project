package io.icebrew.vite.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.icebrew.vite.config.ViteProperties;
import io.icebrew.vite.service.ViteDevServerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller that proxies requests to Vite dev server in development mode
 */
@RestController
public class ViteProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ViteProxyController.class);
    private static final List<String> EXCLUDED_HEADERS = List.of(
            "host", "connection", "content-length", "transfer-encoding");

    private final ViteProperties viteProperties;
    private final ViteDevServerService devServerService;
    private final Environment environment;
    private final RestTemplate restTemplate;

    public ViteProxyController(ViteProperties viteProperties,
            ViteDevServerService devServerService,
            Environment environment) {
        this.viteProperties = viteProperties;
        this.devServerService = devServerService;
        this.environment = environment;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Proxy requests to Vite dev server
     */
    @RequestMapping("/**")
    public void proxyToVite(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Skip if not in development mode or if it's an API request
        String requestUri = request.getRequestURI();
        if (!isDevelopmentMode() || isApiRequest(requestUri)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!devServerService.isRunning()) {
            logger.warn("Vite dev server is not running");
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("Vite dev server is not running");
            return;
        }

        try {
            String targetUrl = buildTargetUrl(request);
            HttpHeaders headers = buildProxyHeaders(request);
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            logger.debug("Proxying {} {} to {}", method, requestUri, targetUrl);

            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                    targetUrl,
                    method,
                    new HttpEntity<>(headers),
                    byte[].class);

            // Copy response
            response.setStatus(responseEntity.getStatusCode().value());

            // Copy headers
            responseEntity.getHeaders().forEach((name, values) -> {
                if (!EXCLUDED_HEADERS.contains(name.toLowerCase())) {
                    values.forEach(value -> response.addHeader(name, value));
                }
            });

            // Copy body
            byte[] body = responseEntity.getBody();
            if (body != null && body.length > 0) {
                response.getOutputStream().write(body);
            }

        } catch (Exception e) {
            logger.error("Error proxying request to Vite dev server", e);
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.getWriter().write("Error proxying to Vite dev server: " + e.getMessage());
        }
    }

    private boolean isDevelopmentMode() {
        List<String> activeProfiles = List.of(environment.getActiveProfiles());
        return activeProfiles.isEmpty() || activeProfiles.contains("dev") || activeProfiles.contains("development");
    }

    private boolean isApiRequest(String uri) {
        return uri.startsWith("/api/") || uri.startsWith("/actuator/");
    }

    private String buildTargetUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(devServerService.getDevServerUrl())
                .path(request.getRequestURI());

        if (queryString != null && !queryString.isEmpty()) {
            builder.query(queryString);
        }

        return builder.build().toUriString();
    }

    private HttpHeaders buildProxyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase())) {
                    List<String> headerValues = Collections.list(request.getHeaders(headerName));
                    headers.addAll(headerName, headerValues);
                }
            }
        }

        return headers;
    }
}
