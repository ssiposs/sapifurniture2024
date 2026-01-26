package ro.sapientia.furniture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource; // <-- HIÁNYZOTT
import ro.sapientia.furniture.model.AppUser;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
void testFullAuthFlow() {
    // 1. Regisztráció - Használjunk Map-et az objektum helyett
    Map<String, String> regDto = Map.of(
        "username", "integUser",
        "email", "integ@test.com",
        "password", "password123"
    );

    ResponseEntity<String> regResponse = restTemplate.postForEntity("/auth/register", regDto, String.class);
    
    // Ha itt még mindig 500-at kapsz, írjuk ki a hiba okát a konzolra:
    if(regResponse.getStatusCode() != HttpStatus.OK) {
        System.out.println("HIBA ÜZENET: " + regResponse.getBody());
    }
        assertEquals(HttpStatus.OK, regResponse.getStatusCode());

        // 2. Login
        Map<String, String> loginReq = Map.of("username", "integUser", "password", "password123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/auth/login", loginReq, Map.class);
        
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        String token = (String) loginResponse.getBody().get("token");
        assertNotNull(token);
    }
}