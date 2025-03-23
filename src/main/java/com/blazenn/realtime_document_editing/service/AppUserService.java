package com.blazenn.realtime_document_editing.service;

import com.blazenn.realtime_document_editing.controller.advice.errors.InvalidPasswordException;
import com.blazenn.realtime_document_editing.controller.advice.errors.UserNotFoundException;
import com.blazenn.realtime_document_editing.dto.AppUserDTO;
import com.blazenn.realtime_document_editing.dto.LoginVM;
import com.blazenn.realtime_document_editing.model.AppUser;
import com.blazenn.realtime_document_editing.repository.AppUserRepository;
import com.blazenn.realtime_document_editing.security.Jwt;
import com.blazenn.realtime_document_editing.service.mapper.AppUserMapper;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserService {
    Logger log = LoggerFactory.getLogger(AppUserService.class);
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;
    private final AppUserRepository appUserRepository;
    private final Jwt jwt;

    public AppUserService(PasswordEncoder passwordEncoder, AppUserMapper appUserMapper, AppUserRepository appUserRepository, Jwt jwt) {
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
        this.appUserRepository = appUserRepository;
        this.jwt = jwt;
    }

    public AppUserDTO create(AppUserDTO appUserDTO) {
        log.info("Request to create user appUserDTO[{}]", appUserDTO);
        AppUser appUser = appUserMapper.appUserDTOToAppUser(appUserDTO);
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserMapper.appUserToAppUserDTO(appUserRepository.save(appUser));
    }

    @Transactional(readOnly = true)
    public String authenticateAppUser(LoginVM loginVM) throws BadRequestException, ResourceNotFoundException {
        AppUserDTO appUserDTO = this.findOneByEmail(loginVM.getEmail());
        if (appUserDTO == null) throw new UserNotFoundException("User not found");
        if (!passwordEncoder.matches(loginVM.getPassword(), appUserDTO.getPassword())) throw new InvalidPasswordException("Password does not match");
        return jwt.createToken(appUserDTO.getEmail());
    }

    @Transactional(readOnly = true)
    public AppUserDTO findOneByEmail(String username) {
        log.info("Request to find user by username: {}", username);
        return appUserMapper.appUserToAppUserDTO(appUserRepository.findAppUserByEmail(username));
    }
}
