package com.project.base_v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.project.base_v1.dto.request.auth.LoginRequest;
import com.project.base_v1.entity.User;
import com.project.base_v1.enums.UserRole;
import com.project.base_v1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String testUsername;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        testUsername = "admin-" + UUID.randomUUID();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(testUsername)
                .password(passwordEncoder.encode("123456"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(user);
    }


    @Test
    void login_success() throws Exception {

        LoginRequest request = new LoginRequest(testUsername, "123456");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void login_fail_wrong_password() throws Exception {

        LoginRequest request = new LoginRequest(testUsername, "wrong");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value(10401));
    }

    @Test
    void login_rate_limit() throws Exception {

        LoginRequest request = new LoginRequest(testUsername, "wrong");

        for (int i = 0; i < 6; i++) {
            mockMvc.perform(
                    post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );
        }

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value(10429));
    }

    @Test
    void refresh_token_success() throws Exception {

        String loginResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                    {"username":"%s","password":"123456"}
                                """, testUsername))
        ).andReturn().getResponse().getContentAsString();

        String refreshToken =
                JsonPath.read(loginResponse, "$.data.refreshToken");

        mockMvc.perform(
                        post("/auth/refresh")
                                .param("refreshToken", refreshToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    void access_token_revoked() throws Exception {

        String loginResponse = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                    {"username":"%s","password":"123456"}
                                """, testUsername))
        ).andReturn().getResponse().getContentAsString();


        String accessToken =
                JsonPath.read(loginResponse, "$.data.accessToken");

        String refreshToken =
                JsonPath.read(loginResponse, "$.data.refreshToken");

        // logout
        mockMvc.perform(
                post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("refreshToken", refreshToken)
        );

        // gọi API protected
        mockMvc.perform(
                        get("/users/me")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isUnauthorized());
    }

}