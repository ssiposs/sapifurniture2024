package ro.sapientia.furniture.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import ro.sapientia.furniture.model.AppUser;
import ro.sapientia.furniture.repository.AppUserRepository;

// EZEK HIÁNYOZTAK:
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Kikapcsolja a Security-t a teszt idejére
@TestPropertySource(locations = "classpath:test.properties")
// Ez a sor kényszeríti a Springet, hogy ne akarjon valódi DB-t indítani:
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) 
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppUserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void register_ShouldReturnSuccess() throws Exception {
        Map<String, String> userMap = Map.of(
            "username", "ujuser",
            "email", "uj@test.com",
            "password", "jelszo123"
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userMap))) // Map-et küldünk
                .andExpect(status().isOk());
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        Map<String, String> loginReq = Map.of("username", "user", "password", "pass");
        
        // Szimuláljuk a sikeres autentikációt
        Authentication auth = new UsernamePasswordAuthenticationToken(
            new org.springframework.security.core.userdetails.User("user", "pass", new java.util.ArrayList<>()), 
            null
        );
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()); // Ellenőrizzük, hogy van JSON token
    }
}