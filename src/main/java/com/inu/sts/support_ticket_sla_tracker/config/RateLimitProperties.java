package com.inu.sts.support_ticket_sla_tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the rate limit filter applied to /api/v1/**.
 */
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /** Whether rate limiting is enabled. */
    private boolean enabled = true;

    /** Max number of requests allowed per client per window. */
    private int requestsPerWindow = 100;

    /** Window duration in seconds. */
    private int windowSeconds = 60;

    /** Path pattern to apply rate limit (e.g. /api/v1/**). Requests not matching are not counted. */
    private String pathPattern = "/api/v1/**";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRequestsPerWindow() {
        return requestsPerWindow;
    }

    public void setRequestsPerWindow(int requestsPerWindow) {
        this.requestsPerWindow = requestsPerWindow;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }
}
