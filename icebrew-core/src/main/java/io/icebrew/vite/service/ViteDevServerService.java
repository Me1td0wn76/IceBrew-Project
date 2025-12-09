package io.icebrew.vite.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.icebrew.vite.config.ViteProperties;
import jakarta.annotation.PreDestroy;

/**
 * Service for managing Vite dev server lifecycle
 */
@Service
public class ViteDevServerService {

    private static final Logger logger = LoggerFactory.getLogger(ViteDevServerService.class);

    private final ViteProperties viteProperties;
    private Process viteProcess;
    private boolean isRunning = false;
    private volatile boolean serverReady = false;

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

        // Check if port is already in use
        if (isPortInUse(viteProperties.getHost(), viteProperties.getPort())) {
            logger.warn("Port {} is already in use. Skipping Vite dev server start.", viteProperties.getPort());
            logger.warn("Please stop the existing process or change the port in application.properties");
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

            // Register shutdown hook for cleanup
            registerShutdownHook();

            // Start log reader thread
            startLogReader();

            // Wait for server to be ready
            waitForServerReady();

            isRunning = true;
            logger.info("Vite dev server started successfully at {}", viteProperties.getDevServerUrl());

        } catch (IOException | InterruptedException e) {
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

            // Try graceful shutdown first
            viteProcess.destroy();

            try {
                boolean exited = viteProcess.waitFor(5, TimeUnit.SECONDS);
                if (!exited) {
                    logger.warn("Vite process did not terminate gracefully, forcing shutdown");
                    killViteProcessTree();
                    viteProcess.destroyForcibly();
                    viteProcess.waitFor(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for Vite process to terminate");
                Thread.currentThread().interrupt();
                killViteProcessTree();
                viteProcess.destroyForcibly();
            }

            isRunning = false;
            serverReady = false;
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

    private boolean isPortInUse(String host, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered, stopping Vite dev server");
            stopDevServer();
        }));
    }

    private void killViteProcessTree() {
        if (viteProcess == null) {
            return;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            long pid = viteProcess.pid();

            if (os.contains("win")) {
                // Windows: Kill process tree using taskkill
                ProcessBuilder pb = new ProcessBuilder("taskkill", "/F", "/T", "/PID", String.valueOf(pid));
                Process killProcess = pb.start();
                killProcess.waitFor(3, TimeUnit.SECONDS);
                logger.debug("Killed Windows process tree for PID: {}", pid);
            } else {
                // Unix/Linux/Mac: Kill process group
                ProcessBuilder pb = new ProcessBuilder("pkill", "-P", String.valueOf(pid));
                Process killProcess = pb.start();
                killProcess.waitFor(3, TimeUnit.SECONDS);
                logger.debug("Killed Unix process tree for PID: {}", pid);
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Failed to kill Vite process tree", e);
        }
    }

    private void startLogReader() {
        Thread logThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(viteProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[Vite] {}", line);
                    // Detect when Vite is ready - check for various patterns
                    String cleanLine = line.replaceAll("\\u001B\\[[;\\d]*m", "").trim(); // Remove ANSI codes
                    if (cleanLine.contains("ready in") ||
                            cleanLine.contains("Local:") ||
                            cleanLine.contains("VITE") && cleanLine.contains("ready")) {
                        logger.info("Vite dev server is ready!");
                        serverReady = true;
                    }
                }
            } catch (IOException e) {
                logger.debug("Vite log reader closed", e);
            }
        });
        logThread.setDaemon(true);
        logThread.start();
    }

    private void waitForServerReady() throws InterruptedException {
        int timeout = viteProperties.getStartupTimeout();
        int elapsed = 0;
        int checkInterval = 500; // milliseconds

        logger.info("Waiting for Vite dev server to be ready...");

        while (elapsed < timeout * 1000) {
            if (serverReady) {
                return;
            }
            Thread.sleep(checkInterval);
            elapsed += checkInterval;
        }

        throw new RuntimeException("Vite dev server did not start within " + timeout + " seconds");
    }
}
