package ro.sapientia.furniture.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.sapientia.furniture.model.AppUser;
import ro.sapientia.furniture.repository.AppUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class AuthComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Minden teszt előtt töröljük a felhasználókat
        userRepository.deleteAll();
    }

    @Test
    public void register_ShouldSaveUserAndReturnOk() throws Exception {
        // 1. ARRANGE: Elkészítjük a regisztrációs adatokat (Map-et használunk a DTO helyett a rugalmasságért)
        Map<String, String> signUpRequest = Map.of(
            "username", "componentUser",
            "email", "comp@test.com",
            "password", "secret123"
        );

        // 2. ACT: Meghívjuk a regisztrációs végpontot
        this.mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk());

        // 3. ASSERT: Ellenőrizzük, hogy az adatbázisba bekerült-e a felhasználó
        var userOptional = userRepository.findByUsername("componentUser");
        assert(userOptional.isPresent());
        assert(userOptional.get().getEmail().equals("comp@test.com"));
    }

    @Test
    public void login_ShouldReturnJwtToken_WhenUserExists() throws Exception {
        // 1. ARRANGE: Manuálisan elmentünk egy kódolt jelszavú felhasználót
        AppUser user = new AppUser();
        user.setUsername("loginUser");
        user.setEmail("login@test.com");
        user.setPassword(passwordEncoder.encode("password123")); // Fontos a PasswordEncoder használata!
        userRepository.save(user);

        Map<String, String> loginRequest = Map.of(
            "username", "loginUser",
            "password", "password123"
        );

        // 2. ACT & ASSERT: Bejelentkezünk és várjuk a tokent
        this.mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }
}