package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.model.Wishlist;
import dk.ek.wishlist.model.WishlistSummary;
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
        mockMvc.perform(post("/wishlists").sessionAttr("userId", 1).param("title", "Jul 2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters"))
                .andExpect(flash().attribute("flash", "Ønskesedlen er oprettet."));

        verify(wishlistService).create(1, "Jul 2026");
    }

    @Test
    void create_invalidTitle_redirectsBackToFormWithError() throws Exception {
        when(wishlistService.create(anyInt(), anyString()))
                .thenThrow(new InvalidInputException("Giv ønskesedlen et navn."));

        mockMvc.perform(post("/wishlists").sessionAttr("userId", 1).param("title", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters#new-letter"))
                .andExpect(flash().attribute("titleError", "Giv ønskesedlen et navn."))
                .andExpect(flash().attributeCount(1));
    }

    @Test
    void create_missingTitle_passesEmptyStringToService() throws Exception {
        mockMvc.perform(post("/wishlists").sessionAttr("userId", 1))
                .andExpect(status().is3xxRedirection());

        verify(wishlistService).create(1, "");
    }

    @Test
    void rename_validTitle_redirectsToListWithConfirmation() throws Exception {
        mockMvc.perform(post("/wishlists/5/title").sessionAttr("userId", 1).param("title", "Fødselsdag"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/5"))
                .andExpect(flash().attribute("flash", "Navnet er ændret."));

        verify(wishlistService).rename(1, 5, "Fødselsdag");
    }

    @Test
    void rename_invalidTitle_redirectsBackToFormWithError() throws Exception {
        doThrow(new InvalidInputException("Giv ønskesedlen et navn."))
                .when(wishlistService).rename(anyInt(), anyInt(), anyString());

        mockMvc.perform(post("/wishlists/5/title").sessionAttr("userId", 1).param("title", "   "))
                .andExpect(redirectedUrl("/wishlists/5#edit-letter"))
                .andExpect(flash().attribute("titleError", "Giv ønskesedlen et navn."))
                .andExpect(flash().attributeCount(1));
    }

    @Test
    void rename_someoneElsesList_returns404() throws Exception {
        doThrow(new NotFoundException("Ønskesedlen findes ikke."))
                .when(wishlistService).rename(anyInt(), anyInt(), anyString());

        mockMvc.perform(post("/wishlists/5/title").sessionAttr("userId", 1).param("title", "Fødselsdag"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_notLoggedIn_redirectsToLoginAndSavesNothing() throws Exception {
        mockMvc.perform(post("/wishlists").param("title", "Jul 2026"))
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(wishlistService);
    }

    @Test
    void rename_notLoggedIn_redirectsToLoginAndChangesNothing() throws Exception {
        mockMvc.perform(post("/wishlists/5/title").param("title", "Fødselsdag"))
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(wishlistService);
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
                .andExpect(content().string(containsString("Tilføj ønske")))
                .andExpect(content().string(containsString("id=\"edit-wish-1\"")))
                .andExpect(content().string(containsString("/wishlists/5/wishes/1/update")))
                .andExpect(content().string(containsString("id=\"delete-wish-1\"")))
                .andExpect(content().string(containsString("/wishlists/5/wishes/1/delete")))
                .andExpect(model().attribute("shareLink", "http://localhost/s/token-1"))
                .andExpect(content().string(containsString("http://localhost/s/token-1")));
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
                .andExpect(content().string(not(containsString("Redigér navn"))))
                .andExpect(content().string(not(containsString("edit-wish-1"))))
                .andExpect(content().string(not(containsString("delete-wish-1"))))
                .andExpect(model().attributeDoesNotExist("shareLink"));
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

    @Test
    void overview_showsOwnAndSharedLists() throws Exception {
        when(wishlistService.getOwnWishlists(1)).thenReturn(List.of(
                new WishlistSummary(5, "Jul 2026", "Test Ejer", 1)));
        when(wishlistService.getSharedWishlists(1)).thenReturn(List.of(
                new WishlistSummary(7, "Mors liste", "Mor", 4)));

        mockMvc.perform(get("/letters").sessionAttr("userId", 1).sessionAttr("userName", "Test Ejer"))
                .andExpect(status().isOk())
                .andExpect(view().name("letters"))
                .andExpect(content().string(containsString("Hej Test Ejer")))
                .andExpect(content().string(containsString("Jul 2026")))
                .andExpect(content().string(containsString("1 ønske")))
                .andExpect(content().string(containsString("/wishlists/5")))
                .andExpect(content().string(containsString("Mors liste")))
                .andExpect(content().string(containsString("Fra Mor · 4 ønsker")))
                .andExpect(content().string(containsString("/wishlists/7")))
                .andExpect(content().string(not(containsString("Patrick"))));
    }

    @Test
    void overview_nothingShared_showsMessage() throws Exception {
        when(wishlistService.getOwnWishlists(1)).thenReturn(List.of());
        when(wishlistService.getSharedWishlists(1)).thenReturn(List.of());

        mockMvc.perform(get("/letters").sessionAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingen har delt en ønskeseddel med dig endnu.")));
    }

    @Test
    void overview_showsFlashMessage() throws Exception {
        when(wishlistService.getOwnWishlists(1)).thenReturn(List.of());
        when(wishlistService.getSharedWishlists(1)).thenReturn(List.of());

        mockMvc.perform(get("/letters").sessionAttr("userId", 1).flashAttr("flash", "Ønskesedlen er oprettet."))
                .andExpect(content().string(containsString("Ønskesedlen er oprettet.")));
    }

    @Test
    void overview_notLoggedIn_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/letters"))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(wishlistService);
    }

    @Test
    void openShareLink_loggedIn_redirectsToTheList() throws Exception {
        when(wishlistService.openShareLink(2, "token-1")).thenReturn(5);

        mockMvc.perform(get("/s/token-1").sessionAttr("userId", 2))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/wishlists/5"));
    }

    @Test
    void openShareLink_notLoggedIn_remembersTokenAndRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/s/token-1"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(request().sessionAttribute(WishlistController.PENDING_SHARE_TOKEN, "token-1"));

        verifyNoInteractions(wishlistService);
    }

    @Test
    void openShareLink_unknownToken_returns404() throws Exception {
        when(wishlistService.openShareLink(2, "findes-ikke"))
                .thenThrow(new NotFoundException("Ønskesedlen findes ikke."));

        mockMvc.perform(get("/s/findes-ikke").sessionAttr("userId", 2))
                .andExpect(status().isNotFound());
    }

    // Three wishes: free, reserved by the guest (2), reserved by someone else (3)
    private List<Wish> wishesInAllStates() {
        Wish free = new Wish(1, 5, "Bog", null, null, null);
        Wish mine = new Wish(2, 5, "Hue", null, null, null);
        mine.setReservedBy(2);
        Wish taken = new Wish(3, 5, "Tørklæde", null, null, null);
        taken.setReservedBy(3);
        return List.of(free, mine, taken);
    }

    @Test
    void show_guest_seesReserveButtonsByStatus() throws Exception {
        when(wishlistService.getVisibleWishlist(2, 5)).thenReturn(new Wishlist(5, 1, "Jul 2026", "token-1"));
        when(wishService.getWishes(5)).thenReturn(wishesInAllStates());

        mockMvc.perform(get("/wishlists/5").sessionAttr("userId", 2))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/wishlists/5/wishes/1/reserve")))
                .andExpect(content().string(containsString("/wishlists/5/wishes/2/unreserve")))
                .andExpect(content().string(containsString("badge-mine")))
                .andExpect(content().string(containsString("badge-reserved")))
                .andExpect(content().string(not(containsString("/wishlists/5/wishes/3/reserve"))));
    }

    @Test
    void show_owner_neverSeesReservations() throws Exception {
        when(wishlistService.getVisibleWishlist(1, 5)).thenReturn(new Wishlist(5, 1, "Jul 2026", "token-1"));
        when(wishService.getWishes(5)).thenReturn(wishesInAllStates());

        mockMvc.perform(get("/wishlists/5").sessionAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("badge-mine"))))
                .andExpect(content().string(not(containsString("badge-reserved"))))
                .andExpect(content().string(not(containsString("/reserve"))))
                .andExpect(content().string(not(containsString("/unreserve"))));
    }
}
