package com.blazenn.realtime_document_editing.controller;

import com.blazenn.realtime_document_editing.dto.AppUserDTO;
import com.blazenn.realtime_document_editing.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/app-user")
public class AppUserController {
    Logger log = LoggerFactory.getLogger(AppUserController.class);
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }


    @PostMapping("/register")
    ResponseEntity<AppUserDTO> registerNewAppUser(@RequestBody AppUserDTO appUserDTO) throws URISyntaxException {
        log.info("POST request of /register with body: {}", appUserDTO);
        AppUserDTO result = appUserService.create(appUserDTO);
        return ResponseEntity.created(new URI("/api/users/" + result.getId())).body(result);
    }
}
