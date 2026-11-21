package Haleon.auth.controller;

import Haleon.auth.dto.LoginDto;
import Haleon.auth.role.Role;
import Haleon.auth.role.RoleRepository;
import Haleon.auth.user.User;
import Haleon.auth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String login(String email, String password) throws Exception {
        LoginDto loginDto = new LoginDto(email, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void login_validCredentials_returnsJwt() throws Exception {
        LoginDto loginDto = new LoginDto("prajyot@gmail.com", "prajyot");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_invalidPassword_returns401() throws Exception {
        LoginDto loginDto = new LoginDto("prajyot@gmail.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nonexistentUser_returns401() throws Exception {
        LoginDto loginDto = new LoginDto("nonexistent@gmail.com", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_disabledUser_returns401() throws Exception {
        Role customerRole = roleRepository.findById(2).orElseThrow();
        User disabledUser = new User();
        disabledUser.setName("disabled");
        disabledUser.setEmail("disabled@gmail.com");
        disabledUser.setPassword(passwordEncoder.encode("disabled"));
        disabledUser.setEnabled(false);
        disabledUser.setPhoneNumber("0000000000");
        disabledUser.setDob(LocalDate.of(2000, 1, 1));
        disabledUser.setAddress("test");
        disabledUser.setCifNumber("CIFTEST01");
        disabledUser.setCreatedAt(LocalDateTime.now());
        disabledUser.setUpdatedAt(LocalDateTime.now());
        disabledUser.setRole(customerRole);
        userRepository.saveAndFlush(disabledUser);

        LoginDto loginDto = new LoginDto("disabled@gmail.com", "disabled");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwt_disabledUserToken_returns401() throws Exception {
        String employeeToken = login("employee@gmail.com", "employee");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        User employee = userRepository.findByEmail("employee@gmail.com").orElseThrow();
        employee.setEnabled(false);
        userRepository.saveAndFlush(employee);

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwt_missingToken_returns401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwt_malformedToken_returns401() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwt_tamperedToken_returns401() throws Exception {
        String validToken = login("prajyot@gmail.com", "prajyot");
        String tamperedToken = validToken.substring(0, validToken.length() - 4) + "XXXX";

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorization_customerAccessingAdminEndpoint_returns403() throws Exception {
        String customerToken = login("customer@gmail.com", "customer");

        mockMvc.perform(get("/test/admin")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorization_employeeAccessingAdminEndpoint_returns403() throws Exception {
        String employeeToken = login("employee@gmail.com", "employee");

        mockMvc.perform(get("/test/admin")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorization_adminAccessingAdminEndpoint_returns200() throws Exception {
        String adminToken = login("prajyot@gmail.com", "prajyot");

        mockMvc.perform(get("/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void jwt_expiredToken_returns401() throws Exception {
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .subject("prajyot@gmail.com")
                .issuedAt(new java.util.Date(System.currentTimeMillis() - 60000))
                .expiration(new java.util.Date(System.currentTimeMillis() - 30000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "nRXsU8T03B1iAA+EEwsUBHMMo2ytbp/riCuM/WkjOY8".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swagger_endpointAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpoint_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/test/public"))
                .andExpect(status().isOk());
    }

    @Test
    void jwt_nonBearerAuthorizationHeader_returns401() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwt_emptyBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_emptyBody_doesNotReturn200() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_jwtCanAccessProtectedEndpoint() throws Exception {
        String token = login("prajyot@gmail.com", "prajyot");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void errorResponse_401_hasCorrectStructure() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void errorResponse_403_hasCorrectStructure() throws Exception {
        String customerToken = login("customer@gmail.com", "customer");
        int userId = userRepository.findByEmail("customer@gmail.com").orElseThrow().getId();

        mockMvc.perform(delete("/users/{id}", userId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/users/" + userId));
    }
}
