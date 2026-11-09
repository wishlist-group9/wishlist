package dk.ek.wishlist.config;

import dk.ek.wishlist.controller.WishlistController;
import dk.ek.wishlist.service.WishService;
import dk.ek.wishlist.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Checks the login rule on a protected page (/letters).
 */
@WebMvcTest(WishlistController.class)
class LoginInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishlistService wishlistService;

    @MockitoBean
    private WishService wishService;

    @Test
    void shouldRedirectToLoginWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/letters"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldAllowAccessWhenLoggedIn() throws Exception {
        when(wishlistService.getOwnWishlists(1)).thenReturn(List.of());
        when(wishlistService.getSharedWishlists(1)).thenReturn(List.of());

        mockMvc.perform(get("/letters").sessionAttr("userId", 1))
                .andExpect(status().isOk())
                .andExpect(view().name("letters"));
    }
}
