package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.repository.WishRepository;
import dk.ek.wishlist.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests wish validation and repository calls without using a database
 */
@ExtendWith(MockitoExtension.class)
class WishServiceTest{

    //Creates a mock repository that does not access the database
    @Mock
    private WishRepository repository;

    @Mock
    private WishlistRepository wishlistRepository;

    //Creates the service and injects the mock repositories
    @InjectMocks
    private WishService service;

    @Test
    void createWish_validWish_saveWish(){
        //Arrange: Create a wish with valid values
        Wish wish = new Wish(
                0, 1, "En bog", "En fantasybog",
                "https://example.com", 149.95
        );

        // Make the mock report that wishlist 1 exists
        when(wishlistRepository.existsById(1)).thenReturn(true);

        //Act: Ask the service to create the wish
        service.createWish(wish);

        //Assert: Check that the wish is passed to the repository
        verify(repository).save(wish);
    }

    @Test
    void createWish_nullTitle_throwsAndSavesNothing(){
        //Arrange: Create a wish without a title
        Wish wish = new Wish(0, 1, null, null, null, null);

        // Act and assert: Check that the missing title is rejected
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> service.createWish(wish)
        );

        //Check the error message and that the repository was not called
        assertEquals("Ønsket skal have en titel.", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void createWish_emptyTitle_throwsAndSavesNothing() {
        //Arrange: Create a wish with an empty title
        Wish wish = new Wish(0, 1, "", null, null, null);

        //Act and assert: Check that the empty title is rejected
        assertThrows(
                InvalidInputException.class,
                () -> service.createWish(wish)
        );

        //Check that the repository was not called
        verifyNoInteractions(repository);
    }

    @Test
    void createWish_blankTitle_throwsAndSavesNothing() {
        //Arrange: Create a wish with a title containing only spaces
        Wish wish = new Wish(0, 1, "   ", null, null, null);

        //Act and assert: Check that a title containing only spaces is rejected
        assertThrows(
                InvalidInputException.class,
                () -> service.createWish(wish)
        );

        //Check that the repository was not called
        verifyNoInteractions(repository);
    }

    @Test
    void createWish_negativePrice_throwsAndSavesNothing() {
        //Arrange: Create a wish with a negative price
        Wish wish = new Wish(0, 1, "En bog", null, null, -50.0);

        //Act and assert: Check that the negative price is rejected
        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> service.createWish(wish)
        );

        //Check the error message and that the repository was not called
        assertEquals("Prisen må ikke være negativ.", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void createWish_nullPrice_savesWish() {
        //Arrange: Create a wish without a price
        Wish wish = new Wish(0, 1, "En bog", null, null, null);

        // Make the mock report that wishlist 1 exists
        when(wishlistRepository.existsById(1)).thenReturn(true);

        //Act: Create the wish. A price is optional
        service.createWish(wish);

        //Assert: Check that the wish is passed to the repository
        verify(repository).save(wish);
    }

    @Test
    void createWish_zeroPrice_savesWish() {
        //Arrange: Create a wish with a price of zero
        Wish wish = new Wish(0, 1, "Et hjemmelavet kort", null, null, 0.0);

        // Make the mock report that wishlist 1 exists
        when(wishlistRepository.existsById(1)).thenReturn(true);

        //Act: Create the wish. Zero is an allowed price
        service.createWish(wish);

        //Assert: Check that the wish is passed to the repository
        verify(repository).save(wish);
    }

    @Test
    void createWish_unknownWishlist_throwsAndSavesNothing() {
        //Arrange: Wishlist 999 does not exist
        Wish wish = new Wish(0, 999, "En bog", null, null, null);

        when(wishlistRepository.existsById(999)).thenReturn(false);

        //Act and assert: Check that the unknown wishlist is rejected
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.createWish(wish)
        );

        assertEquals("Ønskesedlen findes ikke.", exception.getMessage());

        //Check that the wishlist was checked and no wish was saved
        verify(wishlistRepository).existsById(999);
        verifyNoInteractions(repository);
    }

    @Test
    void deleteWish_ownWishlist_deletesWish() {
        //Arrange: User 7 owns wishlist 1, and the repository deletes one row
        when(wishlistRepository.existsByIdAndUserId(1, 7)).thenReturn(true);
        when(repository.delete(5, 1)).thenReturn(1);

        //Act: Delete wish 5 from wishlist 1
        service.deleteWish(5, 1, 7);

        //Assert: The repository was asked to delete wish 5 from wishlist 1
        verify(repository).delete(5, 1);
    }

    @Test
    void deleteWish_wishlistOfAnotherUser_throwsAndDeletesNothing() {
        //Arrange: Wishlist 1 does not belong to user 8
        when(wishlistRepository.existsByIdAndUserId(1, 8)).thenReturn(false);

        //Act and assert: The delete is rejected
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.deleteWish(5, 1, 8)
        );

        assertEquals("Ønskesedlen findes ikke.", exception.getMessage());

        //Check that the wish table was never touched
        verifyNoInteractions(repository);
    }

    @Test
    void deleteWish_wishNotOnWishlist_throws() {
        //Arrange: The user owns the wishlist, but no row is deleted
        when(wishlistRepository.existsByIdAndUserId(1, 7)).thenReturn(true);
        when(repository.delete(999, 1)).thenReturn(0);

        //Act and assert: Zero deleted rows is reported as "not found"
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.deleteWish(999, 1, 7)
        );

        assertEquals("Ønsket findes ikke på denne ønskeseddel.", exception.getMessage());
    }


}
