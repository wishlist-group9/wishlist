package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WishlistService wishlistService;

    @Test
    void create_validTitle_redirectsWithConfirmation() throws Exception {
        mockMvc.perform(post("/wishlists").param("title", "Jul 2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters"))
                .andExpect(flash().attribute("flash", "Ønskesedlen er oprettet."));

        verify(wishlistService).create(WishlistController.DEMO_USER_ID, "Jul 2026");
    }

    @Test
    void create_invalidTitle_redirectsBackToFormWithError() throws Exception {
        when(wishlistService.create(anyInt(), anyString()))
                .thenThrow(new InvalidInputException("Giv ønskesedlen et navn."));

        mockMvc.perform(post("/wishlists").param("title", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters#new-letter"))
                .andExpect(flash().attribute("titleError", "Giv ønskesedlen et navn."))
                .andExpect(flash().attributeCount(1));
    }

    @Test
    void create_missingTitle_passesEmptyStringToService() throws Exception {
        mockMvc.perform(post("/wishlists"))
                .andExpect(status().is3xxRedirection());

        verify(wishlistService).create(WishlistController.DEMO_USER_ID, "");
    }

    @Test
    void rename_validTitle_redirectsToListWithConfirmation() throws Exception {
        mockMvc.perform(post("/wishlists/5/title").param("title", "Fødselsdag"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters/5?view=owner"))
                .andExpect(flash().attribute("flash", "Navnet er ændret."));

        verify(wishlistService).rename(WishlistController.DEMO_USER_ID, 5, "Fødselsdag");
    }

    @Test
    void rename_invalidTitle_redirectsBackToFormWithError() throws Exception {
        doThrow(new InvalidInputException("Giv ønskesedlen et navn."))
                .when(wishlistService).rename(anyInt(), anyInt(), anyString());

        mockMvc.perform(post("/wishlists/5/title").param("title", "   "))
                .andExpect(redirectedUrl("/letters/5?view=owner#edit-letter"))
                .andExpect(flash().attribute("titleError", "Giv ønskesedlen et navn."))
                .andExpect(flash().attributeCount(1));
    }

    @Test
    void rename_someoneElsesList_returns404() throws Exception {
        doThrow(new NotFoundException("Ønskesedlen findes ikke."))
                .when(wishlistService).rename(anyInt(), anyInt(), anyString());

        mockMvc.perform(post("/wishlists/5/title").param("title", "Fødselsdag"))
                .andExpect(status().isNotFound());
    }
}