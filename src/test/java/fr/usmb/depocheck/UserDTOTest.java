package fr.usmb.depocheck;

import fr.usmb.depocheck.DTO.UserDTO;
import fr.usmb.depocheck.Entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDTOTest {

    @Test
    public void testFromEntity() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password"); // Ce champ ne doit pas être copié dans le DTO

        // Act
        UserDTO dto = UserDTO.fromEntity(user);

        // Assert
        assertEquals(1L, dto.getId());
        assertEquals("testUser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
    }
}