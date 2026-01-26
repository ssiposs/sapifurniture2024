package ro.sapientia.furniture.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import ro.sapientia.furniture.model.AppUser;
import ro.sapientia.furniture.repository.AppUserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userService;

    @Test
    void loadUserByUsername_Success() {
        AppUser mockUser = new AppUser("admin", "admin@test.com", "encoded_pass");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        UserDetails details = userService.loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertEquals("encoded_pass", details.getPassword());
    }
}