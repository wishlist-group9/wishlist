package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.service.WishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests wish creation requests without starting the application or accessing a database
 */
@ExtendWith(MockitoExtension.class)
class WishControllerTest{

    @Mock
    private WishService service;

    @InjectMocks
    private WishController controller;

    private MockMvc mockMvc;

    //Runs this setup before each test
    @BeforeEach
    void setUp(){
        //Sets up the MockMvc with only the controller being tested
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void createWish_validInput_redirectWithSuccessMessage() throws Exception{
        //Act and assert: Submit valid form data and check the response
        mockMvc.perform(post("/wishlists/3/wishes")
                .sessionAttr("userId", 7)
                .param("title", "En bog")
                .param("description", "En fantasybog")
                .param("link", "https://example.com")
                .param("price", "149.95"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/3"))
                .andExpect(flash().attribute(
                        "flash", "Ønsket blev oprettet."
                ));
        // Check that the controller called the service.
        verify(service).createWish(any(Wish.class), eq(7));
    }

    @Test
    void createWish_usesWishlistIdFromUrl() throws Exception {
        // Arrange: Capture the wish passed to the service.
        ArgumentCaptor<Wish> captor =
                ArgumentCaptor.forClass(Wish.class);

        // Act: Submit to wishlist 3 with a different ID in the form.
        mockMvc.perform(post("/wishlists/3/wishes")
                        .sessionAttr("userId", 7)
                        .param("wishlistId", "99")
                        .param("title", "En bog")
                        .param("price", "149.95"))
                .andExpect(status().is3xxRedirection());

        // Assert: The URL determines which wishlist receives the wish.
        verify(service).createWish(captor.capture(), eq(7));

        // Gets the captured wish so its values can be checked.
        Wish capturedWish = captor.getValue();

        assertEquals(3, capturedWish.getWishlistId());
        assertEquals("En bog", capturedWish.getTitle());
        assertEquals(Double.valueOf(149.95), capturedWish.getPrice());
    }

    @Test
    void createWish_invalidInput_redirectsWithErrorMessage() throws Exception {
        // Arrange: Make the mock service reject the wish.
        doThrow(new InvalidInputException("Ønsket skal have en titel."))
                .when(service).createWish(any(Wish.class), eq(7));

        // Act and assert: Check that the controller handles the exception.
        mockMvc.perform(post("/wishlists/3/wishes")
                        .sessionAttr("userId", 7)
                        .param("title", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/3"))
                .andExpect(flash().attribute(
                        "errorMessage", "Ønsket skal have en titel."
                ))
                .andExpect(flash().attributeCount(1));

        verify(service).createWish(any(Wish.class), eq(7));
    }

    @Test
    void createWish_invalidPriceFormat_doesNotCallService() throws Exception {
        // Act and assert: Submit a price that cannot be converted to Double.
        mockMvc.perform(post("/wishlists/3/wishes")
                        .sessionAttr("userId", 7)
                        .param("title", "En bog")
                        .param("price", "hej"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/3"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "Kontrollér de indtastede oplysninger."
                ));

        // Binding errors must stop the request before calling the service.
        verifyNoInteractions(service);
    }

    @Test
    void createWish_unknownWishlist_redirectsToOverviewWithError() throws Exception {
        //Arrange: Make the mock service reject the unknown wishlist
        doThrow(new NotFoundException("Ønskesedlen findes ikke."))
                .when(service).createWish(any(Wish.class), eq(7));

        //Act and assert: Check the redirect and error message
        mockMvc.perform(post("/wishlists/999/wishes")
                        .sessionAttr("userId", 7)
                        .param("title", "En bog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters"))
                .andExpect(flash().attribute(
                        "errorMessage", "Ønskesedlen findes ikke."
                ))
                .andExpect(flash().attributeCount(1));

        verify(service).createWish(any(Wish.class), eq(7));
    }

    @Test
    void createWish_notLoggedIn_redirectsToLoginWithoutCallingService() throws Exception {
        //Act and assert: Submit a valid wish without a user id in the session
        mockMvc.perform(post("/wishlists/3/wishes")
                        .param("title", "En bog"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        //Assert: The controller stops the request before calling the service
        verifyNoInteractions(service);
    }

    @Test
    void deleteWish_loggedIn_redirectsToListWithSuccessMessage() throws Exception {
        //Act and assert: A logged-in user (id 7) deletes wish 5 on wishlist 3
        mockMvc.perform(post("/wishlists/3/wishes/5/delete")
                        .sessionAttr("userId", 7))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/3"))
                .andExpect(flash().attribute("flash", "Ønsket blev slettet."));

        //The ids from the URL and the session are passed to the service
        verify(service).deleteWish(5, 3, 7);
    }

    @Test
    void deleteWish_notLoggedIn_redirectsToLoginAndDeletesNothing() throws Exception {
        //Act and assert: No userId in the session
        mockMvc.perform(post("/wishlists/3/wishes/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(service);
    }

    @Test
    void deleteWish_notFound_redirectsToOverviewWithError() throws Exception {
        //Arrange: Make the mock service reject the deletion
        doThrow(new NotFoundException("Ønsket findes ikke på denne ønskeseddel."))
                .when(service).deleteWish(999, 3, 7);

        //Act and assert: Check the redirect and the error message
        mockMvc.perform(post("/wishlists/3/wishes/999/delete")
                        .sessionAttr("userId", 7))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters"))
                .andExpect(flash().attribute(
                        "errorMessage", "Ønsket findes ikke på denne ønskeseddel."
                ))
                .andExpect(flash().attributeCount(1));
    }

    @Test
    void updateWish_validInput_redirectsToListWithConfirmation() throws Exception {
        mockMvc.perform(post("/wishlists/3/wishes/5/update")
                        .sessionAttr("userId", 1)
                        .param("title", "Uldtørklæde")
                        .param("price", "249.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/3"))
                .andExpect(flash().attribute("flash", "Ønsket blev opdateret."));

        //The ids come from the URL and the user from the session
        verify(service).updateWish(eq(5), eq(3), eq(1), any(Wish.class));
    }

    @Test
    void updateWish_invalidInput_reopensTheModalWithError() throws Exception {
        doThrow(new InvalidInputException("Ønsket skal have en titel."))
                .when(service).updateWish(eq(5), eq(3), eq(1), any(Wish.class));

        mockMvc.perform(post("/wishlists/3/wishes/5/update")
                        .sessionAttr("userId", 1)
                        .param("title", "   "))
                .andExpect(redirectedUrl("/wishlists/3#edit-wish-5"))
                .andExpect(flash().attribute("formError", "Ønsket skal have en titel."))
                .andExpect(flash().attribute("formErrorFor", "edit-wish-5"));
    }

    @Test
    void updateWish_invalidPriceFormat_doesNotCallService() throws Exception {
        mockMvc.perform(post("/wishlists/3/wishes/5/update")
                        .sessionAttr("userId", 1)
                        .param("title", "En bog")
                        .param("price", "hej"))
                .andExpect(redirectedUrl("/wishlists/3#edit-wish-5"))
                .andExpect(flash().attribute("formError", "Kontrollér de indtastede oplysninger."));

        verifyNoInteractions(service);
    }

    @Test
    void updateWish_notOwnersWish_redirectsToOverviewWithError() throws Exception {
        doThrow(new NotFoundException("Ønskesedlen findes ikke."))
                .when(service).updateWish(eq(5), eq(3), eq(2), any(Wish.class));

        mockMvc.perform(post("/wishlists/3/wishes/5/update")
                        .sessionAttr("userId", 2)
                        .param("title", "Hacket"))
                .andExpect(redirectedUrl("/letters"))
                .andExpect(flash().attribute("errorMessage", "Ønskesedlen findes ikke."));
    }

    @Test
    void updateWish_notLoggedIn_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/wishlists/3/wishes/5/update")
                        .param("title", "Uldtørklæde"))
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(service);
    }
}
