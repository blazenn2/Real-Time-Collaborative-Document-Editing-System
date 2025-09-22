package com.blazenn.realtime_document_editing.integration.controller;

import com.blazenn.realtime_document_editing.dto.AppUserDTO;
import com.blazenn.realtime_document_editing.model.AppUser;
import com.blazenn.realtime_document_editing.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AppUserControllerIntegrationTest {

    public static final String TEST_USERNAME = "tester";

    public static final String TEST_ROLE = "USER";

    public static final String FIRST_NAME = "Hamza";

    public static final String LAST_NAME = "Nawab";

    public static final String EMAIL = "hamza.nawab@10pearls.com";

    public static final String PASSWORD = "plainPassword";

    public static final String UPDATED_FIRST_NAME = "John";

    public static final String UPDATED_LAST_NAME = "Doe";

    public static final String UPDATED_EMAIL = "john.doe@10pearls.com";

    /**
     * Creating a JSON string for app user because in its DTO, I have prevented password serialization.
     * This means that the object mapper cannot serialize password, that is why raw JSON is used here.
     */
    public static final String JSON = String.format("""
        {
          "firstName": "%s",
          "lastName": "%s",
          "email": "%s",
          "password": "%s"
        }
        """, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD);

    private static final String UPDATED_JSON_TEMPLATE = """
        {
          "id": %d,
          "firstName": "%s",
          "lastName": "%s",
          "email": "%s",
          "password": "%s"
        }
        """;

    private static final String PATCH_UPDATE_JSON_TEMPLATE = """
        {
          "id": %d,
          "firstName": "%s",
          "lastName": "%s"
        }
        """;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        appUserRepository.deleteAll();
    }

    private AppUserDTO buildUserDto() {
        AppUserDTO dto = new AppUserDTO();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setPassword(PASSWORD);
        dto.setEmail(EMAIL);
        return dto;
    }

    private AppUser buildUser() {
        AppUser user = new AppUser();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(PASSWORD);
        user.setEmail(EMAIL);
        return user;
    }

    private String createUpdatedUserJSON(Long id) {
        return String.format(UPDATED_JSON_TEMPLATE,
                id,
                UPDATED_FIRST_NAME,
                UPDATED_LAST_NAME,
                UPDATED_EMAIL,
                PASSWORD);
    }

    private String createPatchUpdateUserJSON(Long id) {
        return String.format(PATCH_UPDATE_JSON_TEMPLATE,
                id,
                UPDATED_FIRST_NAME,
                UPDATED_LAST_NAME);
    };

    @Test
    @Transactional
    public void testRegisterNewAppUser() throws Exception {
        long appUserSize = appUserRepository.count();
        AppUserDTO appUserDTO = buildUserDto();
        mockMvc.perform(post("/api/app-user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        assertPersistedAppUsers(users -> {
            assertThat(users).hasSize((int) appUserSize + 1);
            AppUser appUser = users.get(users.size() - 1);
            assertThat(appUser.getEmail()).isEqualTo(appUserDTO.getEmail());
            assertThat(appUser.getFirstName()).isEqualTo(appUserDTO.getFirstName());
            assertThat(appUser.getLastName()).isEqualTo(appUserDTO.getLastName());
        });
    }

    @Test
    @Transactional
    public void createUserWithExistingEmail() throws Exception {
        appUserRepository.saveAndFlush(buildUser());
        int databaseSizeBeforeCreate = appUserRepository.findAll().size();

        mockMvc.perform(post("/api/app-user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON))
                .andExpect(status().isBadRequest());

        assertPersistedAppUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser", roles = {"USER"})
    public void shouldUpdateTestUser() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());
        int databaseSizeBeforeUpdate = appUserRepository.findAll().size();

        String updatedJson = this.createUpdatedUserJSON(createdAppUser.getId());

        mockMvc.perform(put("/api/app-user/" + createdAppUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"));

        assertPersistedAppUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            AppUser updatedAppUser = users.get(users.size() - 1);
            assertThat(updatedAppUser.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
            assertThat(updatedAppUser.getLastName()).isEqualTo(UPDATED_LAST_NAME);
            assertThat(updatedAppUser.getEmail()).isEqualTo(UPDATED_EMAIL);
        });
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void putUpdate_shouldFailWhenPayloadDoesNotContainIdForUpdate() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());

        // Payload missing id
        String updatedJson = this.createUpdatedUserJSON(null);

        mockMvc.perform(put("/api/app-user/" + createdAppUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void putUpdate_shouldFailWhenPayloadIdDoesNotMatchUrlId() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());

        // Payload id doesn't match URL id
        String updatedJson = this.createUpdatedUserJSON(createdAppUser.getId() + 1);

        mockMvc.perform(put("/api/app-user/" + createdAppUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void putUpdate_shouldFailWhenUrlIdDoesNotExistInDatabase() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());

        // Payload id matches URL id but URL id does not exist in DB
        String updatedJson = this.createUpdatedUserJSON(createdAppUser.getId() + 1);

        mockMvc.perform(put("/api/app-user/" + (createdAppUser.getId() + 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void shouldPerformPartialUpdate() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());
        int databaseSizeBeforeUpdate = appUserRepository.findAll().size();

        String updatedJson = this.createPatchUpdateUserJSON(createdAppUser.getId());

        mockMvc.perform(patch("/api/app-user/" + (createdAppUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(header().exists("Location"));

        assertPersistedAppUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            AppUser updatedAppUser = users.get(users.size() - 1);
            assertThat(updatedAppUser.getFirstName()).isEqualTo(UPDATED_FIRST_NAME);
            assertThat(updatedAppUser.getLastName()).isEqualTo(UPDATED_LAST_NAME);
            assertThat(updatedAppUser.getEmail()).isEqualTo(EMAIL);
        });
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void partialUpdate_shouldFailWhenPayloadDoesNotContainIdForUpdate() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());

        // Payload missing id
        String updatedJson = this.createPatchUpdateUserJSON(null);

        mockMvc.perform(patch("/api/app-user/" + createdAppUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void partialUpdate_shouldFailWhenPayloadIdDoesNotMatchUrlId() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());

        // Payload id doesn't match URL id
        String updatedJson = this.createPatchUpdateUserJSON(createdAppUser.getId() + 1);

        mockMvc.perform(patch("/api/app-user/" + createdAppUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(username = TEST_USERNAME, roles = {TEST_ROLE})
    public void partialUpdate_shouldFailWhenUrlIdDoesNotExistInDatabase() throws Exception {
        AppUser createdAppUser = appUserRepository.saveAndFlush(buildUser());

        // Payload id matches URL id but URL id does not exist in DB
        String updatedJson = this.createPatchUpdateUserJSON(createdAppUser.getId() + 1);

        mockMvc.perform(patch("/api/app-user/" + (createdAppUser.getId() + 1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isBadRequest());
    }

    private void assertPersistedAppUsers(Consumer<List<AppUser>> consumer) {
        consumer.accept(appUserRepository.findAll());
    }
}
