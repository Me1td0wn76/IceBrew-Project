package io.icebrew.vite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Vite integration
 */
@ConfigurationProperties(prefix = "icebrew.vite")
public class ViteProperties {

    /**
     * Enable Vite dev server integration
     */
    private boolean enabled = true;

    /**
     * Vite dev server host
     */
    private String host = "localhost";

    /**
     * Vite dev server port
     */
    private int port = 5173;

    /**
     * Frontend source directory (relative to project root)
     */
    private String frontendDir = "frontend";

    /**
     * Production build output directory (relative to frontendDir)
     */
    private String buildDir = "dist";

    /**
     * Enable auto-start of Vite dev server in development mode
     */
    private boolean autoStart = true;

    /**
     * Vite dev server base URL path
     */
    private String basePath = "";

    /**
     * Timeout for starting Vite dev server (in seconds)
     */
    private int startupTimeout = 60;

    /**
     * Additional environment variables for Vite process
     */
    private java.util.Map<String, String> env = new java.util.HashMap<>();

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFrontendDir() {
        return frontendDir;
    }

    public void setFrontendDir(String frontendDir) {
        this.frontendDir = frontendDir;
    }

    public String getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(String buildDir) {
        this.buildDir = buildDir;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public int getStartupTimeout() {
        return startupTimeout;
    }

    public void setStartupTimeout(int startupTimeout) {
        this.startupTimeout = startupTimeout;
    }

    public java.util.Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(java.util.Map<String, String> env) {
        this.env = env;
    }

    public String getDevServerUrl() {
        return "http://" + host + ":" + port;
    }
}
