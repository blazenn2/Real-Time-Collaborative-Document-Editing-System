package com.blazenn.realtime_document_editing.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorization extends OncePerRequestFilter {
    private final Jwt jwt;

    public JwtAuthorization(Jwt jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) filterChain.doFilter(request, response);
        else if (!jwt.validateToken(token)) throw new AccessDeniedException("Access denied");
    }
}
