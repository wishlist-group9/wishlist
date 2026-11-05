package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.model.Wishlist;
import dk.ek.wishlist.service.WishService;
import dk.ek.wishlist.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WishlistService wishlistService;

    @MockitoBean
    WishService wishService;

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
                .andExpect(redirectedUrl("/wishlists/5"))
                .andExpect(flash().attribute("flash", "Navnet er ændret."));

        verify(wishlistService).rename(WishlistController.DEMO_USER_ID, 5, "Fødselsdag");
    }

    @Test
    void rename_invalidTitle_redirectsBackToFormWithError() throws Exception {
        doThrow(new InvalidInputException("Giv ønskesedlen et navn."))
                .when(wishlistService).rename(anyInt(), anyInt(), anyString());

        mockMvc.perform(post("/wishlists/5/title").param("title", "   "))
                .andExpect(redirectedUrl("/wishlists/5#edit-letter"))
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

    @Test
    void show_ownList_rendersOwnerViewWithWishes() throws Exception {
        when(wishlistService.getVisibleWishlist(1, 5)).thenReturn(new Wishlist(5, 1, "Jul 2026", "token-1"));
        when(wishService.getWishes(5)).thenReturn(List.of(
                new Wish(1, 5, "Uldtørklæde", "Rødt", "https://example.com", 249.0)));

        mockMvc.perform(get("/wishlists/5").sessionAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("wishlist"))
                .andExpect(model().attribute("owner", true))
                .andExpect(content().string(containsString("Jul 2026")))
                .andExpect(content().string(containsString("Uldtørklæde")))
                .andExpect(content().string(containsString("249,00 kr.")))
                .andExpect(content().string(containsString("Tilføj ønske")));
    }

    @Test
    void show_sharedList_rendersGuestView() throws Exception {
        when(wishlistService.getVisibleWishlist(2, 5)).thenReturn(new Wishlist(5, 1, "Jul 2026", "token-1"));
        when(wishService.getWishes(5)).thenReturn(List.of(new Wish(1, 5, "Uldtørklæde", null, null, null)));

        mockMvc.perform(get("/wishlists/5").sessionAttr("userId", 2))
                .andExpect(status().isOk())
                .andExpect(model().attribute("owner", false))
                .andExpect(content().string(containsString("Uldtørklæde")))
                .andExpect(content().string(not(containsString("Tilføj ønske"))))
                .andExpect(content().string(not(containsString("Redigér navn"))));
    }

    @Test
    void show_noWishes_showsEmptyState() throws Exception {
        when(wishlistService.getVisibleWishlist(1, 5)).thenReturn(new Wishlist(5, 1, "Jul 2026", "token-1"));
        when(wishService.getWishes(5)).thenReturn(List.of());

        mockMvc.perform(get("/wishlists/5").sessionAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Der er ingen ønsker endnu")));
    }

    @Test
    void show_unknownOrNotVisibleList_returns404() throws Exception {
        when(wishlistService.getVisibleWishlist(1, 5)).thenThrow(new NotFoundException("Ønskesedlen findes ikke."));

        mockMvc.perform(get("/wishlists/5").sessionAttr("userId", 1))
                .andExpect(status().isNotFound());

        verify(wishService, never()).getWishes(anyInt());
    }

    @Test
    void show_notLoggedIn_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/wishlists/5"))
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(wishlistService, wishService);
    }
}
