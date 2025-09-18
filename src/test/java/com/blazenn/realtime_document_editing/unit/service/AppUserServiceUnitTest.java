package com.blazenn.realtime_document_editing.unit.service;

import com.blazenn.realtime_document_editing.controller.advice.errors.InvalidPasswordException;
import com.blazenn.realtime_document_editing.controller.advice.errors.UserNotFoundException;
import com.blazenn.realtime_document_editing.dto.AppUserDTO;
import com.blazenn.realtime_document_editing.dto.LoginVM;
import com.blazenn.realtime_document_editing.model.AppUser;
import com.blazenn.realtime_document_editing.repository.AppUserRepository;
import com.blazenn.realtime_document_editing.security.Jwt;
import com.blazenn.realtime_document_editing.service.AppUserService;
import com.blazenn.realtime_document_editing.service.mapper.AppUserMapper;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceUnitTest {

    private AppUserDTO appUserDTO;

    private AppUser appUser;

    private LoginVM loginVM;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserMapper appUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private AppUserService appUserService;

    @SuppressWarnings("unchecked")
    private <T> T createPopulatedAppUserData(Boolean isDTO) {
        if (!isDTO) {
            AppUser u = new AppUser();
            u.setId(1L);
            u.setFirstName("First Name Test");
            u.setLastName("Last Name Test");
            u.setEmail("testuser@testOrg.com");
            u.setPassword("password");
            return (T) u;
        } else {
            AppUserDTO u = new AppUserDTO();
            u.setId(1L);
            u.setFirstName("First Name Test");
            u.setLastName("Last Name Test");
            u.setEmail("testuser@testOrg.com");
            u.setPassword("password");
            return (T) u;
        }
    }

    private LoginVM createPopulatedLoginVM() {
        LoginVM vm = new LoginVM();
        vm.setEmail("testuser@testOrg.com");
        vm.setPassword("password");
        return vm;
    }

    @BeforeEach
    public void buildAppUser() {
        this.appUserDTO = this.createPopulatedAppUserData(true);
        this.appUser = this.createPopulatedAppUserData(false);
        this.loginVM = this.createPopulatedLoginVM();
    }


    // ---------- create() tests ----------
    @Test
    public void create_shouldEncodePasswordAndCreateAppUser() {
        // Arrange
        when(appUserMapper.appUserToAppUserDTO(appUser)).thenReturn(appUserDTO);
        when(passwordEncoder.encode(appUserDTO.getPassword())).thenReturn("encoded_password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(appUser);
        when(appUserMapper.appUserDTOToAppUser(appUserDTO)).thenReturn(appUser);

        // Act
        AppUserDTO createdAppUser = appUserService.create(appUserDTO);

        // Assert
        assertThat(createdAppUser.getFirstName()).isEqualTo("First Name Test");
        assertThat(createdAppUser.getLastName()).isEqualTo("Last Name Test");
        assertThat(createdAppUser.getEmail()).isEqualTo("testuser@testOrg.com");

        verify(appUserRepository).save(any(AppUser.class));
        verify(passwordEncoder).encode("password");
        verify(appUserMapper).appUserToAppUserDTO(appUser);
        verify(appUserMapper).appUserDTOToAppUser(appUserDTO);
    }

    // ---------- partialUpdate() tests ----------
    @Test
    public void partialUpdate_shouldUpdateExistingUser() {
        // Arrange
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(appUserMapper.updateAppUserFromDTO(appUserDTO, appUser)).thenReturn(appUser);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(appUser);
        when(appUserMapper.appUserToAppUserDTO(appUser)).thenReturn(appUserDTO);

        //Act
        AppUserDTO result = appUserService.partialUpdate(appUserDTO);

        // Assert
        verify(appUserRepository).save(any(AppUser.class));
        verify(appUserMapper).updateAppUserFromDTO(appUserDTO, appUser);
        verify(appUserRepository).findById(appUserDTO.getId());

        assertThat(result).isEqualTo(appUserDTO);
    }

    @Test
    public void partialUpdate_shouldReturnNullIfUserNotFound() {
        // Arrange
        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        AppUserDTO result = appUserService.partialUpdate(appUserDTO);

        // Assert
        verify(appUserRepository).findById(appUserDTO.getId());

        assertThat(result).isNull();
    }

    // ---------- authenticateAppUser() tests ----------
    @Test
    void authenticateAppUser_shouldReturnJwtToken() {
        // Arrange
        when(passwordEncoder.matches(loginVM.getPassword(), appUserDTO.getPassword())).thenReturn(true);
        when(appUserService.findOneByEmail(loginVM.getEmail())).thenReturn(appUserDTO);
        when(jwt.createToken(loginVM.getEmail())).thenReturn("token");

        // Act
        String token = appUserService.authenticateAppUser(loginVM);

        // Assert
        verify(passwordEncoder).matches(loginVM.getPassword(), appUserDTO.getPassword());

        assertThat(token).isEqualTo("token");
    }

    @Test
    void authenticateAppUser_shouldThrowUserNotFound() {
        when(appUserService.findOneByEmail(loginVM.getEmail())).thenReturn(null);

        assertThatThrownBy(() -> appUserService.authenticateAppUser(loginVM)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void authenticateAppUser_shouldThrowInvalidPassword() {
        when(appUserService.findOneByEmail(loginVM.getEmail())).thenReturn(appUserDTO);
        when(passwordEncoder.matches(loginVM.getPassword(), appUserDTO.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> appUserService.authenticateAppUser(loginVM)).isInstanceOf(InvalidPasswordException.class);
    }

    // ---------- findOneByEmail() test ----------
    @Test
    void findOneByEmail_shouldReturnDTO() {
        // Arrange
        String email = appUserDTO.getEmail();

        when(appUserRepository.findAppUserByEmail(email)).thenReturn(appUser);
        when(appUserMapper.appUserToAppUserDTO(appUser)).thenReturn(appUserDTO);
        // Act
        AppUserDTO result = appUserService.findOneByEmail(email);

        // Assert
        assertThat(result).isEqualTo(appUserDTO);
        verify(appUserRepository).findAppUserByEmail(email);
        verify(appUserMapper).appUserToAppUserDTO(appUser);
    }



    // ---------- checkIfAppUserExists() tests ----------
    @Test
    void checkIfAppUserExists_shouldReturnTrueIfPresent() {
        // Arrange
        when(appUserRepository.findById(appUser.getId())).thenReturn(Optional.of(appUser));

        // Act
        Boolean userFound = appUserService.checkIfAppUserExists(appUser.getId());

        // Assert
        assertThat(userFound).isTrue();
        verify(appUserRepository).findById(appUser.getId());
    }

    @Test
    void checkIfAppUserExists_shouldReturnFalseIfAbsent() {
        // Arrange
        when(appUserRepository.findById(appUser.getId())).thenReturn(Optional.empty());

        // Act
        Boolean userFound = appUserService.checkIfAppUserExists(appUser.getId());

        // Assert
        assertThat(userFound).isFalse();
        verify(appUserRepository).findById(appUser.getId());
    }

    // ---------- validateAppUserForUpdate() tests ----------
    @Test
    void validateAppUserForUpdate_shouldThrowIfIdNull() {
        appUserDTO.setId(null);

        assertThatThrownBy(() -> appUserService.validateAppUserForUpdate(appUserDTO, appUserDTO.getId())).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateAppUserForUpdate_shouldThrowIfIdMismatch() {
        assertThatThrownBy(() -> appUserService.validateAppUserForUpdate(appUserDTO, 999L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void validateAppUserForUpdate_shouldThrowIfUserDoesNotExist() {
        when(appUserRepository.findById(appUserDTO.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.validateAppUserForUpdate(appUserDTO, appUserDTO.getId())).isInstanceOf(BadRequestException.class);
    }


    @Test
    void validateAppUserForUpdate_shouldPassIfEverythingIsFine() {
        // Arrange
        when(appUserRepository.findById(appUserDTO.getId())).thenReturn(Optional.of(appUser));

        // Act
        try {
            appUserService.validateAppUserForUpdate(appUserDTO, appUserDTO.getId());
        } catch (BadRequestException e) {
            fail("validateAppUserForUpdate should not throw BadRequestException but did", e);
        }
    }
}
