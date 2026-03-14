package com.example.demo;

import com.example.demo.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_PASSWORD = "SecurePass1!";

    @Test
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest("testuser1", VALID_PASSWORD, "test1@mail.ru", "CUSTOMER");
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser1"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_weakPassword_rejected() throws Exception {
        RegisterRequest req = new RegisterRequest("user2", "short", "user2@mail.ru", "CUSTOMER");
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_thenLoginWithJwt() throws Exception {
        String username = "jwtuser";
        RegisterRequest req = new RegisterRequest(username, VALID_PASSWORD, "jwt@mail.ru", "MECHANIC");
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        String loginJson = "{\"username\":\"" + username + "\",\"password\":\"" + VALID_PASSWORD + "\"}";
        String loginResp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResp).get("accessToken").asText();
        mockMvc.perform(get("/api/parts")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void apiWithoutAuth_denied() throws Exception {
        mockMvc.perform(get("/api/parts"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 401 && status != 403) {
                        throw new AssertionError("Expected 401 or 403, got " + status);
                    }
                });
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customer_canGetVehicles() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customer_cannotDeleteCustomers() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canAccessMechanics() throws Exception {
        mockMvc.perform(get("/api/mechanics"))
                .andExpect(status().isOk());
    }

    @Test
    void login_refresh_oldRefreshReturns401() throws Exception {
        String username = "refreshuser";
        RegisterRequest req = new RegisterRequest(username, VALID_PASSWORD, "refresh@mail.ru", "CUSTOMER");
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        String loginJson = "{\"username\":\"" + username + "\",\"password\":\"" + VALID_PASSWORD + "\"}";
        String loginResp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResp).get("refreshToken").asText();

        String refreshReq = objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken));
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshReq))
                .andExpect(status().isUnauthorized());
    }
}
