package dk.ek.wishlist.config;

import dk.ek.wishlist.controller.DesignPreviewController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DesignPreviewController.class)
class LoginInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectToLoginWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/letters"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shouldAllowAccessWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/letters").sessionAttr("userId", 1))
                .andExpect(status().isOk());
    }
}