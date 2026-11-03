package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.repository.WishRepository;
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

    //Creates the service and injects the mock repository
    @InjectMocks
    private WishService service;

    @Test
    void createWish_validWish_saveWish(){
        //Arrange: Create a wish with valid values
        Wish wish = new Wish(
                0, 1, "En bog", "En fantasybog",
                "https://example.com", 149.95
        );

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

        //Act: Create the wish. A price is optional
        service.createWish(wish);

        //Assert: Check that the wish is passed to the repository
        verify(repository).save(wish);
    }

    @Test
    void createWish_zeroPrice_savesWish() {
        //Arrange: Create a wish with a price of zero
        Wish wish = new Wish(0, 1, "Et hjemmelavet kort", null, null, 0.0);

        //Act: Create the wish. Zero is an allowed price
        service.createWish(wish);

        //Assert: Check that the wish is passed to the repository
        verify(repository).save(wish);
    }
}
