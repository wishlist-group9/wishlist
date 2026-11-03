package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.model.User;
import dk.ek.wishlist.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentCaptor.forClass;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Spy
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Mock
    UserRepository repository;
    @InjectMocks
    UserService service;

    @Test
    void register_validInput_savesUserWithHashedPassword() {
        when(repository.findByEmailNick("anna@mail.dk")).thenReturn(Optional.empty());

        service.register("  Anna  ", "  ANNA@Mail.dk ", "hunter22");

        var hashCaptor = forClass(String.class);
        verify(repository).save(eq("Anna"), eq("anna@mail.dk"), hashCaptor.capture());
        assertNotEquals("hunter22", hashCaptor.getValue());
        assertEquals(60, hashCaptor.getValue().length());
    }

    @Test
    void register_blankName_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("   ", "anna@mail.dk", "hunter22"));
        assertEquals(UserService.NAME_REQUIRED, ex.getMessage());
        verify(repository, never()).save(anyString(), anyString(), anyString());
    }

    @Test
    void register_nullName_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register(null, "anna@mail.dk", "hunter22"));
        assertEquals(UserService.NAME_REQUIRED, ex.getMessage());
    }

    @Test
    void register_nameTooLong_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("a".repeat(101), "anna@mail.dk", "hunter22"));
        assertEquals(UserService.NAME_TOO_LONG, ex.getMessage());
    }

    @Test
    void register_blankEmail_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("Anna", "  ", "hunter22"));
        assertEquals(UserService.EMAIL_REQUIRED, ex.getMessage());
    }

    @Test
    void register_invalidEmail_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("Anna", "anna@mail", "hunter22"));
        assertEquals(UserService.EMAIL_INVALID, ex.getMessage());
    }

    @Test
    void register_nullPassword_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("Anna", "anna@mail.dk", null));
        assertEquals(UserService.PASSWORD_REQUIRED, ex.getMessage());
    }

    @Test
    void register_shortPassword_throws() {
        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("Anna", "anna@mail.dk", "1234567"));
        assertEquals(UserService.PASSWORD_TOO_SHORT, ex.getMessage());
    }

    @Test
    void register_emailAlreadyTaken_throwsAndDoesNotSave() {
        var existing = new User(1, "Anna", "anna@mail.dk", "hash");
        when(repository.findByEmailNick("anna@mail.dk")).thenReturn(Optional.of(existing));

        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("Anna 2", "anna@mail.dk", "hunter22"));
        assertEquals(UserService.EMAIL_TAKEN, ex.getMessage());
        verify(repository, never()).save(anyString(), anyString(), anyString());
    }

    @Test
    void register_databaseRejectsDuplicate_throwsEmailTaken() {
        when(repository.findByEmailNick("anna@mail.dk")).thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(repository).save(eq("Anna"), eq("anna@mail.dk"), any());

        var ex = assertThrows(InvalidInputException.class,
                () -> service.register("Anna", "anna@mail.dk", "hunter22"));
        assertEquals(UserService.EMAIL_TAKEN, ex.getMessage());
    }
}