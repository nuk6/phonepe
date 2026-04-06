package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple fixed-window rate limiter for sensitive endpoints.
 * In prod you'd use something like Redis + sliding window, but this
 * is enough to demonstrate the concept without pulling in extra deps.
 */
@Slf4j
@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute

    private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong requestCount = new AtomicLong(0);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        long now = System.currentTimeMillis();
        long currentWindowStart = windowStart.get();

        if (now - currentWindowStart > WINDOW_MILLIS) {
            // reset window
            windowStart.set(now);
            requestCount.set(1);
            return true;
        }

        long count = requestCount.incrementAndGet();
        if (count > MAX_REQUESTS_PER_WINDOW) {
            log.warn("Rate limit exceeded for {} from {}", request.getRequestURI(),
                    request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests. Try again later.\"}");
            return false;
        }

        return true;
    }
}

