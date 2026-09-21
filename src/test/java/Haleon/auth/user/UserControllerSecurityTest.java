package Haleon.auth.user;

import Haleon.auth.dto.LoginDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = login("prajyot@gmail.com", "prajyot");
        customerToken = login("customer@gmail.com", "customer");
    }

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

    private int getSeededUserId() {
        return userRepository.findByEmail("customer@gmail.com").orElseThrow().getId();
    }

    @Test
    void getAllUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_asCustomer_returns200() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/users/{id}", getSeededUserId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_asCustomer_returns200() throws Exception {
        mockMvc.perform(get("/users/{id}", getSeededUserId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/users/{id}", getSeededUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void createUser_unauthenticated_returns401() throws Exception {
        String body = """
                {
                    "name": "testuser",
                    "email": "testuser@gmail.com",
                    "password": "testpassword",
                    "enabled": true,
                    "phoneNumber": "9999999999",
                    "address": "test address",
                    "role": {"id": 2}
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_asCustomer_returns403() throws Exception {
        String body = """
                {
                    "name": "testuser",
                    "email": "testuser@gmail.com",
                    "password": "testpassword",
                    "enabled": true,
                    "phoneNumber": "9999999999",
                    "address": "test address",
                    "role": {"id": 2}
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_asAdmin_returns200() throws Exception {
        String body = """
                {
                    "name": "testuser",
                    "email": "testuser@gmail.com",
                    "password": "testpassword",
                    "enabled": true,
                    "phoneNumber": "9999999999",
                    "address": "test address",
                    "role": {"id": 2}
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_unauthenticated_returns401() throws Exception {
        int userId = getSeededUserId();
        String body = """
                {
                    "name": "updated",
                    "email": "customer@gmail.com",
                    "password": "customer",
                    "enabled": true,
                    "phoneNumber": "1234567890",
                    "address": "updated address",
                    "role": {"id": 2}
                }
                """;

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_asCustomer_returns403() throws Exception {
        int userId = getSeededUserId();
        String body = """
                {
                    "name": "updated",
                    "email": "customer@gmail.com",
                    "password": "customer",
                    "enabled": true,
                    "phoneNumber": "1234567890",
                    "address": "updated address",
                    "role": {"id": 2}
                }
                """;

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_asAdmin_returns200() throws Exception {
        int userId = getSeededUserId();
        String body = """
                {
                    "name": "updated",
                    "email": "customer@gmail.com",
                    "password": "customer",
                    "enabled": true,
                    "phoneNumber": "1234567890",
                    "address": "updated address",
                    "role": {"id": 2}
                }
                """;

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void patchUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch("/users/{id}", getSeededUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"patched\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchUser_asCustomer_returns403() throws Exception {
        mockMvc.perform(patch("/users/{id}", getSeededUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"patched\"}")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void patchUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(patch("/users/{id}", getSeededUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"patched\"}")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/users/{id}", getSeededUserId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUser_asCustomer_returns403() throws Exception {
        mockMvc.perform(delete("/users/{id}", getSeededUserId())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_asAdmin_returns200() throws Exception {
        mockMvc.perform(delete("/users/{id}", getSeededUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
