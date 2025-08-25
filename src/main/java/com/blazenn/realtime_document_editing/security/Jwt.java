package com.blazenn.realtime_document_editing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Jwt {
    Logger log = LoggerFactory.getLogger(Jwt.class);
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private String expiration;
    @Value("${jwt.dlq.secret:${jwt.secret}}")
    private String secretDLQ;
    @Value("${jwt.dlq.expiration:${jwt.expiration}}")
    private String expirationDLQ;

    public String createToken(String email) {
        return this.createToken(email, false);
    }

    public String createToken(String email, Boolean isDLQ) {
        Claims claims = Jwts.claims().setSubject(email);
        return Jwts.builder().setClaims(claims).setSubject(email).setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(isDLQ ? expirationDLQ : expiration))).signWith(SignatureAlgorithm.HS256, isDLQ ? secretDLQ : secret).compact();
    }

    public Boolean validateToken(String token) {
        return this.validateToken(token, false);
    }

    public Boolean validateToken(String token, Boolean isDLQ) {
        try {
            Jwts.parserBuilder().setSigningKey(isDLQ ? secretDLQ : secret).build().parse(token);
            return true;
        } catch (JwtException e) {
            log.error("Exception while validating token", e.fillInStackTrace());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody().getSubject();
    }
}
