package com.blazenn.realtime_document_editing.integration.controller;

import com.blazenn.realtime_document_editing.dto.LoginVM;
import com.blazenn.realtime_document_editing.model.AppUser;
import com.blazenn.realtime_document_editing.repository.AppUserRepository;
import com.blazenn.realtime_document_editing.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationControllerIntegrationTest {

    public static final String FIRST_NAME = "Hamza";

    public static final String LAST_NAME = "Nawab";

    public static final String EMAIL = "hamza.nawab@10pearls.com";

    public static final String PASSWORD = "plainPassword";

    public static final String WRONG_PASSWORD = "wrongPassword";

    public static final String WRONG_EMAIL = "wrongEmail@10pearls.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser buildUser() {
        AppUser user = new AppUser();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setEmail(EMAIL);
        return user;
    }

    private LoginVM buildLoginVM(Boolean setWrongPassword, Boolean setWrongEmail) {
        LoginVM vm = new LoginVM();
        vm.setEmail(setWrongEmail? WRONG_EMAIL : EMAIL);
        vm.setPassword(setWrongPassword ? WRONG_PASSWORD : PASSWORD);
        return vm;
    }

    @BeforeEach
    public void setup() {
        appUserRepository.deleteAll();
    }

    @Test
    public void testAuthentication() throws Exception {
        appUserRepository.saveAndFlush(buildUser());
        LoginVM loginVM = buildLoginVM(false, false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.convertObjectToJsonBytes(loginVM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString());
    }

    @Test
    public void testFailAuthenticationWithWrongPassword() throws Exception {
        appUserRepository.saveAndFlush(buildUser());
        LoginVM loginVM = buildLoginVM(true, false);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.convertObjectToJsonBytes(loginVM)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Password does not match"));
    }

    @Test
    public void testFailAuthenticationWithWrongEmail() throws Exception {
        appUserRepository.saveAndFlush(buildUser());
        LoginVM loginVM = buildLoginVM(true, true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.convertObjectToJsonBytes(loginVM)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("User not found"));
    }
}
