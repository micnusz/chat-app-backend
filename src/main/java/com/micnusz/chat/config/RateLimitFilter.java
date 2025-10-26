package com.micnusz.chat.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket(int tokens, Duration period) {
        Bandwidth limit = Bandwidth.classic(tokens, Refill.intervally(tokens, period));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket resolveBucket(String key, int tokens, Duration period) {
        return buckets.computeIfAbsent(key, k -> newBucket(tokens, period));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        if (!path.equals("/api/users/login") && !path.equals("/api/users/register")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = req.getRemoteAddr();

        Bucket bucket;
        if (path.equals("/api/users/register")) {
            bucket = resolveBucket(ip + ":register", 5, Duration.ofMinutes(10)); // 3 req / 10 min
        } else { // login
            bucket = resolveBucket(ip + ":login", 10, Duration.ofMinutes(1)); // 10 req / 1 min
        }

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"Too many requests. Try again later.\"}");
        }
    }
}
