package com.blazenn.realtime_document_editing.integration.service;

import com.blazenn.realtime_document_editing.controller.advice.errors.InvalidPasswordException;
import com.blazenn.realtime_document_editing.dto.AppUserDTO;
import com.blazenn.realtime_document_editing.dto.LoginVM;
import com.blazenn.realtime_document_editing.repository.AppUserRepository;
import com.blazenn.realtime_document_editing.service.AppUserService;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AppUserServiceIntegrationTest {

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    private AppUserDTO buildUserDto(String email) {
        AppUserDTO dto = new AppUserDTO();
        dto.setFirstName("Hamza");
        dto.setLastName("Nawab");
        dto.setEmail(email);
        dto.setPassword("plainPassword");
        return dto;
    }

    private LoginVM buildLoginVM(String email, String password) {
        LoginVM loginVM = new LoginVM();
        loginVM.setEmail(email);
        loginVM.setPassword(password);
        return loginVM;
    }

    @Test
    void create_shouldPersistAndEncodePassword() throws BadRequestException {
        // Arrange
        AppUserDTO dto = buildUserDto("hamza@test.com");

        // Act
        AppUserDTO saved = appUserService.create(dto);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(appUserRepository.findAppUserByEmail("hamza@test.com")).isNotNull();
    }

    @Test
    void authenticateAppUser_shouldReturnTokenWhenValid() throws BadRequestException {
        // Arrange
        AppUserDTO dto = buildUserDto("valid@test.com");
        appUserService.create(dto); // persist user first
        LoginVM loginVM = buildLoginVM("valid@test.com", "plainPassword");

        // Act
        String token = appUserService.authenticateAppUser(loginVM);

        // Assert
        assertThat(token).isNotNull().isNotEmpty().isNotBlank();
    }

    @Test
    void authenticateAppUser_shouldThrowOnWrongPassword() throws BadRequestException {
        // Arrange
        AppUserDTO dto = buildUserDto("wrongpass@test.com");
        appUserService.create(dto); // persist user first
        LoginVM loginVM = buildLoginVM("wrongpass@test.com", "wrongPassword");

        // Act + Assert
        assertThrows(InvalidPasswordException.class,
                () -> appUserService.authenticateAppUser(loginVM));
    }
}