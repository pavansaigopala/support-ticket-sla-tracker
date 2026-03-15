package com.inu.sts.support_ticket_sla_tracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limit per client IP on paths matching {@link RateLimitProperties#getPathPattern()}.
 * Fixed window; over limit → 429 + Retry-After.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitProperties props;
    private final AntPathMatcher paths = new AntPathMatcher();
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.isEnabled()) return true;
        return !paths.match(props.getPathPattern(), request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String ip = clientIp(req);
        long now = System.currentTimeMillis();
        long windowEnd = now + props.getWindowSeconds() * 1000L;

        Counter c = counters.compute(ip, (k, old) -> (old == null || now >= old.windowEnd) ? new Counter(windowEnd) : old);
        if (c.n.incrementAndGet() > props.getRequestsPerWindow()) {
            log.warn("Rate limit exceeded ip={} path={}", ip, req.getRequestURI());
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(props.getWindowSeconds()));
            res.setContentType("application/json");
            res.getWriter().write("{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr() != null ? req.getRemoteAddr() : "unknown";
    }

    private static final class Counter {
        final long windowEnd;
        final AtomicInteger n = new AtomicInteger(0);

        Counter(long windowEnd) {
            this.windowEnd = windowEnd;
        }
    }
}
