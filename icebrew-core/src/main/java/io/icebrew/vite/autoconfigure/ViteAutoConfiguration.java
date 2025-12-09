package io.icebrew.vite.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import io.icebrew.vite.config.ViteProperties;
import io.icebrew.vite.service.ViteDevServerService;
import io.icebrew.vite.web.ViteHmrProxyFilter;
import io.icebrew.vite.web.ViteProxyController;
import io.icebrew.vite.web.ViteStaticResourceConfiguration;
import jakarta.annotation.PostConstruct;

/**
 * Auto-configuration for Vite integration
 */
@Configuration
@EnableConfigurationProperties(ViteProperties.class)
@ConditionalOnProperty(prefix = "icebrew.vite", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({ ViteStaticResourceConfiguration.class })
public class ViteAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ViteAutoConfiguration.class);

    private final ViteProperties viteProperties;
    private final Environment environment;
    private ViteDevServerService viteDevServerService;

    public ViteAutoConfiguration(ViteProperties viteProperties, Environment environment) {
        this.viteProperties = viteProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        logger.info("IceBrew Vite integration initialized");
        logger.info("Vite dev server URL: {}", viteProperties.getDevServerUrl());
        logger.info("Frontend directory: {}", viteProperties.getFrontendDir());
        logger.info("Build directory: {}", viteProperties.getBuildDir());
    }

    /**
     * Start Vite dev server after Spring Boot application is fully ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (viteDevServerService != null && isDevelopmentMode() && viteProperties.isAutoStart()) {
            logger.info("Starting Vite dev server after Spring Boot is ready...");
            viteDevServerService.startDevServer();
        }
    }

    @Bean
    public ViteDevServerService viteDevServerService() {
        viteDevServerService = new ViteDevServerService(viteProperties);
        // Don't start here - wait for ApplicationReadyEvent
        return viteDevServerService;
    }

    @Bean
    @ConditionalOnProperty(prefix = "icebrew.vite", name = "auto-start", havingValue = "true", matchIfMissing = true)
    public ViteProxyController viteProxyController(ViteDevServerService devServerService) {
        return new ViteProxyController(viteProperties, devServerService, environment);
    }

    @Bean
    public FilterRegistrationBean<ViteHmrProxyFilter> viteHmrProxyFilter() {
        FilterRegistrationBean<ViteHmrProxyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ViteHmrProxyFilter(viteProperties, environment));
        registration.addUrlPatterns("/*");
        registration.setName("viteHmrProxyFilter");
        registration.setOrder(1);
        return registration;
    }

    private boolean isDevelopmentMode() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return true; // Default to development if no profile is set
        }
        for (String profile : profiles) {
            if ("dev".equals(profile) || "development".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
