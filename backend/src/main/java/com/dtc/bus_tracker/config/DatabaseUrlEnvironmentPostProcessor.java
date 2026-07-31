package com.dtc.bus_tracker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String dbUrl = environment.getProperty("DATABASE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = environment.getProperty("spring.datasource.url");
        }

        if (dbUrl != null && !dbUrl.isBlank() && !dbUrl.startsWith("jdbc:")) {
            Map<String, Object> map = new HashMap<>();
            
            try {
                String uriStr = dbUrl;
                if (uriStr.startsWith("postgres://")) {
                    uriStr = "postgresql://" + uriStr.substring("postgres://".length());
                }
                
                URI uri = new URI(uriStr);
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    map.put("spring.datasource.username", userInfo[0]);
                    if (userInfo.length > 1) {
                        map.put("spring.datasource.password", userInfo[1]);
                    }
                    
                    int port = uri.getPort();
                    String portStr = (port != -1) ? ":" + port : "";
                    String path = (uri.getPath() != null) ? uri.getPath() : "";
                    String query = (uri.getQuery() != null) ? "?" + uri.getQuery() : "";
                    
                    String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + portStr + path + query;
                    map.put("spring.datasource.url", jdbcUrl);
                } else {
                    String jdbcUrl = "jdbc:" + dbUrl;
                    map.put("spring.datasource.url", jdbcUrl);
                }
            } catch (Exception e) {
                String jdbcUrl = dbUrl;
                if (jdbcUrl.startsWith("postgres://")) {
                    jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgres://".length());
                } else if (jdbcUrl.startsWith("postgresql://")) {
                    jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgresql://".length());
                } else {
                    jdbcUrl = "jdbc:" + jdbcUrl;
                }
                map.put("spring.datasource.url", jdbcUrl);
            }

            if (!map.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource("customDatabaseUrlProperties", map));
            }
        }
    }
}
