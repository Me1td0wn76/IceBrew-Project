package io.icebrew.vite.web;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.icebrew.vite.config.ViteProperties;

/**
 * Configuration for serving static files from Vite build output in production
 * mode
 */
@Configuration
public class ViteStaticResourceConfiguration implements WebMvcConfigurer {

    private final ViteProperties viteProperties;
    private final Environment environment;

    public ViteStaticResourceConfiguration(ViteProperties viteProperties, Environment environment) {
        this.viteProperties = viteProperties;
        this.environment = environment;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (isProductionMode()) {
            String buildPath = viteProperties.getFrontendDir() + File.separator + viteProperties.getBuildDir();
            File buildDir = new File(buildPath);

            if (buildDir.exists() && buildDir.isDirectory()) {
                registry.addResourceHandler("/**")
                        .addResourceLocations("file:" + buildDir.getAbsolutePath() + File.separator)
                        .setCachePeriod(3600)
                        .resourceChain(true);
            }
        }
    }

    private boolean isProductionMode() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        return activeProfiles.contains("prod") || activeProfiles.contains("production");
    }
}
