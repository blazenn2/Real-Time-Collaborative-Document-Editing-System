package com.blazenn.realtime_document_editing.security;

import com.blazenn.realtime_document_editing.controller.advice.errors.JwtUnathorizedException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthorization extends OncePerRequestFilter implements ChannelInterceptor {
    private final Jwt jwt;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthorization(Jwt jwt, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwt = jwt;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = bearerToken.substring(7);
            if (Boolean.FALSE.equals(jwt.validateToken(token))) {
                throw new JwtUnathorizedException("Invalid JWT token found.");
            }
            String email = jwt.getEmailFromToken(token);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, null, null);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (JwtUnathorizedException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        if (Arrays.asList(StompCommand.CONNECT, StompCommand.SEND).contains(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new JwtUnathorizedException("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            if (Boolean.FALSE.equals(jwt.validateToken(token))) {
                throw new JwtUnathorizedException("Invalid JWT token found.");
            }

            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    jwt.getEmailFromToken(token),
                    token,
                    List.of() // map roles from claims if present
            ));
        }
        return message;
    }
}
