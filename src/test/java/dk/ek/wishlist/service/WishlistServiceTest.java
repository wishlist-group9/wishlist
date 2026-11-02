package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.repository.HealthRepository;
import dk.ek.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {
    @Mock
    WishlistRepository repository;
    @InjectMocks
    WishlistService service;

    @Test
    void create_validTitle_savesTrimmedTitleAndNewToken() {
        when(repository.save(anyInt(), anyString(), anyString())).thenReturn(7);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        int id = service.create(1, "  Jul 2026  ");

        assertEquals(7, id);
        verify(repository).save(eq(1), eq("Jul 2026"), tokenCaptor.capture());
        assertEquals(36, tokenCaptor.getValue().length());
    }

    @Test
    void create_nullTitle_throwsAndSavesNothing() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> service.create(1, null));

        assertEquals(WishlistService.TITLE_REQUIRED, e.getMessage());
        verify(repository, never()).save(anyInt(), anyString(), anyString());
    }

    @Test
    void create_titleTooLong_throwsAndSavesNothing() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> service.create(1, "a".repeat(101)));

        assertEquals(WishlistService.TITLE_TOO_LONG, e.getMessage());
        verify(repository, never()).save(anyInt(), anyString(), anyString());
    }

    @Test
    void create_titleExactly100_isAllowed() {
        // Arrange
        when(repository.save(anyInt(), anyString(), anyString())).thenReturn(7);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        int id = service.create(1, "a".repeat(100));

        // Assert
        verify(repository).save(eq(1), eq("a".repeat(100)), anyString());
    }

    @Test
    void create_twoLists_getDifferentTokens() {
        // Arrange
        when(repository.save(anyInt(), anyString(), anyString())).thenReturn(7, 8);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        service.create(1, "Jul 2026");
        service.create(1, "Fødselsdag");

        // Assert
        verify(repository, times(2)).save(eq(1), anyString(), tokenCaptor.capture());
        List<String> tokens = tokenCaptor.getAllValues();
        assertEquals(2, tokens.size());
        assertNotEquals(tokens.get(0), tokens.get(1));
    }

    @Test
    void rename_validTitle_savesTrimmedTitle() {
        when(repository.updateTitle(1, 5, "Fødselsdag")).thenReturn(true);

        service.rename(1, 5, "  Fødselsdag  ");

        verify(repository).updateTitle(1, 5, "Fødselsdag");
    }

    @Test
    void rename_blankTitle_throwsAndDoesNotUpdate() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> service.rename(1, 5, "   "));

        assertEquals(WishlistService.TITLE_REQUIRED, e.getMessage());
        verify(repository, never()).updateTitle(anyInt(), anyInt(), anyString());
    }

    @Test
    void rename_notOwnersList_throwsNotFound() {
        when(repository.updateTitle(1, 5, "Fødselsdag")).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.rename(1, 5, "Fødselsdag"));
    }
}
