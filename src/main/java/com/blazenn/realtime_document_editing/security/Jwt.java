package com.blazenn.realtime_document_editing.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Jwt {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private String expiration;

    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        return Jwts.builder().setClaims(claims).setSubject(email).setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(expiration))).signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public Boolean validateToken(String token) {
        // add validation logic here
        return false;
    }
}
