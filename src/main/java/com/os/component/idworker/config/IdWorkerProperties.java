package com.os.component.idworker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IdWorker配置
 *
 * @author pengjunjie
 */
@ConfigurationProperties(prefix = "component.idworker")
public class IdWorkerProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String enabled;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}
