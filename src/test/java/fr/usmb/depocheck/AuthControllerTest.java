package fr.usmb.depocheck;

import fr.usmb.depocheck.DTO.LoginResponse;
import fr.usmb.depocheck.DTO.UserDTO;
import fr.usmb.depocheck.Entities.User;
import fr.usmb.depocheck.Repository.UserRepository;
import fr.usmb.depocheck.adapters.api.AuthController;
import fr.usmb.depocheck.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtils;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testAuthenticateUser() {
        // Arrange
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(jwtUtils.generateToken("testUser")).thenReturn("test-jwt-token");

        // Act
        LoginResponse response = authController.authenticateUser(user);

        // Assert
        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals(user.getUsername(), response.getUser().getUsername());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testRegisterUser() {
        // Arrange
        User user = new User();
        user.setUsername("newUser");
        user.setEmail("new@example.com");
        user.setPassword("password");

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPassword");

        // Act
        String result = authController.registerUser(user);

        // Assert
        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegisterUser_UsernameExists() {
        // Arrange
        User user = new User();
        user.setUsername("existingUser");
        user.setPassword("password");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        // Act
        String result = authController.registerUser(user);

        // Assert
        assertEquals("Error: Username is already taken!", result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testValidateToken_Valid() {
        // Arrange
        String token = "Bearer valid-token";
        User user = new User(1L, "testUser", "test@example.com", "password");

        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("valid-token")).thenReturn("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(user);

        // Act
        ResponseEntity<?> response = authController.validateToken(token);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof UserDTO);
    }

    @Test
    public void testValidateToken_Invalid() {
        // Arrange
        String token = "Bearer invalid-token";

        when(jwtUtils.validateJwtToken("invalid-token")).thenReturn(false);

        // Act
        ResponseEntity<?> response = authController.validateToken(token);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid token", response.getBody());
    }
}