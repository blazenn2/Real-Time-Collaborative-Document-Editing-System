package com.blazenn.realtime_document_editing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authenticate")
public class AuthenticationController {
    Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping("")
    public String authenticateUser() {
        log.info("POST Request to authenticateUser");
        return "Authenticated";
    }
}
