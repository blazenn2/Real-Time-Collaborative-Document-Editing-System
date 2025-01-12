package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.dto.AppUserDTO;
import com.blazenn.realtime_document_editing.model.AppUser;
import com.blazenn.realtime_document_editing.repository.AppUserRepository;
import com.blazenn.realtime_document_editing.service.mapper.AppUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {
    Logger log = LoggerFactory.getLogger(AppUserService.class);
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;
    private final AppUserRepository appUserRepository;

    public AppUserService(PasswordEncoder passwordEncoder, AppUserMapper appUserMapper, AppUserRepository appUserRepository) {
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
        this.appUserRepository = appUserRepository;
    }

    public AppUserDTO create(AppUserDTO appUserDTO) {
        log.info("Request to create user appUserDTO[{}]", appUserDTO);
        AppUser appUser = appUserMapper.appUserDTOToAppUser(appUserDTO);
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserMapper.appUserToAppUserDTO(appUserRepository.save(appUser));
    }
}
