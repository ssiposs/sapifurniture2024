package ro.sapientia.furniture.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // <-- HIÁNYZOTT
import org.springframework.test.context.TestPropertySource; // <-- HIÁNYZOTT
import ro.sapientia.furniture.model.AppUser;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:test.properties") // Ez kényszeríti a H2-t
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Leválasztja a valódi DB-t
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void testSaveAndExists() {
        AppUser user = new AppUser("testuser", "test@test.com", "password");
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("testuser"));
        assertTrue(userRepository.existsByEmail("test@test.com"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }
}