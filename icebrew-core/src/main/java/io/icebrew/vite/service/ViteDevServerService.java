package io.icebrew.vite.service;

import io.icebrew.vite.config.ViteProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing Vite dev server lifecycle
 */
@Service
public class ViteDevServerService {

    private static final Logger logger = LoggerFactory.getLogger(ViteDevServerService.class);

    private final ViteProperties viteProperties;
    private Process viteProcess;
    private boolean isRunning = false;

    public ViteDevServerService(ViteProperties viteProperties) {
        this.viteProperties = viteProperties;
    }

    /**
     * Start Vite dev server
     */
    public void startDevServer() {
        if (isRunning) {
            logger.info("Vite dev server is already running");
            return;
        }

        if (!viteProperties.isEnabled() || !viteProperties.isAutoStart()) {
            logger.info("Vite dev server auto-start is disabled");
            return;
        }

        try {
            File frontendDir = new File(viteProperties.getFrontendDir());
            if (!frontendDir.exists()) {
                logger.warn("Frontend directory does not exist: {}", frontendDir.getAbsolutePath());
                return;
            }

            logger.info("Starting Vite dev server at {}:{}", viteProperties.getHost(), viteProperties.getPort());

            List<String> command = buildStartCommand();
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(frontendDir);
            processBuilder.redirectErrorStream(true);

            // Add environment variables
            Map<String, String> environment = processBuilder.environment();
            environment.putAll(viteProperties.getEnv());

            viteProcess = processBuilder.start();

            // Start log reader thread
            startLogReader();

            // Wait for server to be ready
            waitForServerReady();

            isRunning = true;
            logger.info("Vite dev server started successfully at {}", viteProperties.getDevServerUrl());

        } catch (Exception e) {
            logger.error("Failed to start Vite dev server", e);
            stopDevServer();
        }
    }

    /**
     * Stop Vite dev server
     */
    @PreDestroy
    public void stopDevServer() {
        if (viteProcess != null && viteProcess.isAlive()) {
            logger.info("Stopping Vite dev server");
            viteProcess.destroy();
            try {
                viteProcess.waitFor(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Vite process to terminate");
                viteProcess.destroyForcibly();
            }
            isRunning = false;
            logger.info("Vite dev server stopped");
        }
    }

    /**
     * Check if Vite dev server is running
     */
    public boolean isRunning() {
        return isRunning && viteProcess != null && viteProcess.isAlive();
    }

    /**
     * Get dev server URL
     */
    public String getDevServerUrl() {
        return viteProperties.getDevServerUrl();
    }

    private List<String> buildStartCommand() {
        List<String> command = new ArrayList<>();

        // Detect OS and use appropriate command
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            command.add("cmd.exe");
            command.add("/c");
            command.add("npm.cmd");
        } else {
            command.add("npm");
        }

        command.add("run");
        command.add("dev");
        command.add("--");
        command.add("--host");
        command.add(viteProperties.getHost());
        command.add("--port");
        command.add(String.valueOf(viteProperties.getPort()));

        return command;
    }

    private void startLogReader() {
        Thread logThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(viteProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[Vite] {}", line);
                }
            } catch (IOException e) {
                logger.debug("Vite log reader closed", e);
            }
        });
        logThread.setDaemon(true);
        logThread.start();
    }

    private void waitForServerReady() throws InterruptedException {
        String serverUrl = viteProperties.getDevServerUrl();
        int timeout = viteProperties.getStartupTimeout();
        int elapsed = 0;
        int checkInterval = 500; // milliseconds

        logger.info("Waiting for Vite dev server to be ready...");

        while (elapsed < timeout * 1000) {
            if (isServerReady(serverUrl)) {
                return;
            }
            Thread.sleep(checkInterval);
            elapsed += checkInterval;
        }

        throw new RuntimeException("Vite dev server did not start within " + timeout + " seconds");
    }

    private boolean isServerReady(String serverUrl) {
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200 || responseCode == 404; // 404 is ok, means server is running
        } catch (IOException e) {
            return false;
        }
    }
}
