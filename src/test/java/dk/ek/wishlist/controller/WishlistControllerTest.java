package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
}