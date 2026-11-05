package dk.ek.wishlist.config;

import dk.ek.wishlist.controller.DesignPreviewController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Checks the login rule on a protected page. Uses the Santa preview page (/letters/1/santa),
 * because it needs no services, so the test only depends on LoginInterceptor and WebConfig.
 */
@WebMvcTest(DesignPreviewController.class)
class LoginInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectToLoginWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/letters/1/santa"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldAllowAccessWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/letters/1/santa").sessionAttr("userId", 1))
                .andExpect(status().isOk());
    }
}