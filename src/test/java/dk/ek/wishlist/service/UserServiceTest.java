package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidLoginException;
import dk.ek.wishlist.model.User;
import dk.ek.wishlist.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenLoginIsCorrect() {
        // Arrange
        User user = new User(1, "Test Ejer", "ejer@test.dk", "hash");
        when(userRepository.findByEmail("ejer@test.dk")).thenReturn(user);
        when(passwordEncoder.matches("1234", "hash")).thenReturn(true);

        // Act
        User result = userService.login("ejer@test.dk", "1234");

        // Assert
        assertEquals(user, result);
    }

    @Test
    void shouldThrowWhenEmailDoesNotExist() {
        // Arrange
        when(userRepository.findByEmail("isak@test.dk")).thenReturn(null);

        // Act + Assert
        assertThrows(InvalidLoginException.class,
                () -> userService.login("isak@test.dk", "1234"));
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {
        // Arrange
        User user = new User(1, "Test Ejer", "ejer@test.dk", "hash");
        when(userRepository.findByEmail("ejer@test.dk")).thenReturn(user);
        when(passwordEncoder.matches("forkert", "hash")).thenReturn(false);

        // Act + Assert
        assertThrows(InvalidLoginException.class,
                () -> userService.login("ejer@test.dk", "forkert"));
    }
}