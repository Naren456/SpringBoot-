package com.example.rideshare.config;

import com.example.rideshare.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import org.springframework.stereotype.Component;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String user = "anonymous";

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            user = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        boolean allowed = rateLimitService.allowRequest(user);

        if (!allowed) {
            response.setStatus(429); // 429
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\", \"message\": \"Please try again later\"}");
            return; // ⛔ stop filter chain
        }

        // ✅ continue request
        filterChain.doFilter(request, response);
    }
}
