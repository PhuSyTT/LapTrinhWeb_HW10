package vn.iotstar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vn.iotstar.models.LoginResponse;
import vn.iotstar.models.LoginUserModel;
import vn.iotstar.models.RegisterUserModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSignupAndLoginFlow() throws Exception {
        // 1. Sign up a new user
        RegisterUserModel registerUser = new RegisterUserModel();
        registerUser.setFullName("Nguyen Van Test");
        registerUser.setEmail("testuser@hcmute.edu.vn");
        registerUser.setPassword("password123");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@hcmute.edu.vn"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Test"));

        // 2. Login with newly created user
        LoginUserModel loginUser = new LoginUserModel();
        loginUser.setEmail("testuser@hcmute.edu.vn");
        loginUser.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponse.class
        );
        String token = loginResponse.getToken();
        assertNotNull(token);
        assertEquals(3, token.split("\\.").length, "JWT token generated via Nimbus must have 3 segments");

        // 3. Access protected /users/me using Bearer Token
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@hcmute.edu.vn"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van Test"));

        // 4. Access protected /users list using Bearer Token
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        // Malformed token or invalid signature should trigger GlobalExceptionHandler
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testLoginWithBadCredentials() throws Exception {
        LoginUserModel badLogin = new LoginUserModel();
        badLogin.setEmail("trungnh@hcmute.edu.vn");
        badLogin.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUiPagesAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/profile"))
                .andExpect(status().isOk());
    }
}
