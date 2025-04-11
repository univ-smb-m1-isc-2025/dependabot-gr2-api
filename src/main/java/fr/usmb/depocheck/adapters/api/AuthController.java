package fr.usmb.depocheck.adapters.api;

import fr.usmb.depocheck.DTO.LoginResponse;
import fr.usmb.depocheck.DTO.UserDTO;
import fr.usmb.depocheck.Entities.User;
import fr.usmb.depocheck.Repository.UserRepository;
import fr.usmb.depocheck.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    JwtUtil jwtUtils;

    @PostMapping("/signin")
    public LoginResponse authenticateUser(@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new LoginResponse(jwtUtils.generateToken(userDetails.getUsername()), UserDTO.fromEntity(user));
    }
    @PostMapping("/signup")
    public String registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Error: Username is already taken!";
        }
        // Create new user's account
        User newUser = new User(
                null,
                user.getUsername(),
                user.getEmail(),
                encoder.encode(user.getPassword())
        );
        userRepository.save(newUser);
        return "User registered successfully!";
    }
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username);
            if (user != null) {
                return ResponseEntity.ok(UserDTO.fromEntity(user));
            } else {
                return ResponseEntity.status(401).body("Invalid token");
            }
        } else {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}