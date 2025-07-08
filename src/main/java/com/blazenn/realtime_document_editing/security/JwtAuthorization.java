package com.blazenn.realtime_document_editing.security;

import com.blazenn.realtime_document_editing.controller.advice.errors.JwtUnathorizedException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthorization extends OncePerRequestFilter {
    private final Jwt jwt;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthorization(Jwt jwt, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwt = jwt;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            if (Boolean.FALSE.equals(jwt.validateToken(token.substring(7)))) {
                throw new JwtUnathorizedException("Invalid JWT token found.");
            }
        } catch (JwtUnathorizedException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
