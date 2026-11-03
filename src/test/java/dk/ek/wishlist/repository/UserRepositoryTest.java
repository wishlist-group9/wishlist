package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/h2init.sql")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldGetUserByEmail() {
        // Act
        User user = userRepository.findByEmail("ejer@test.dk");

        // Assert
        assertNotNull(user);
        assertEquals("Test Ejer", user.getName());
        assertEquals("hash", user.getPasswordHash());
    }

    @Test
    void shouldReturnNullWhenEmailDoesNotExist() {
        // Act
        User user = userRepository.findByEmail("isak@test.dk");

        // Assert
        assertNull(user);
    }

    @Test
    void shouldReturnNullWhenEmailIsAnEmptyString() {
        // Act
        User user = userRepository.findByEmail("");

        // Assert
        assertNull(user);
    }
}
