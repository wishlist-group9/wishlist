package dk.ek.wishlist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DesignPreviewController.class)
class DesignPreviewControllerTest {

    // Protected pages require a logged-in user (see LoginInterceptor)
    private static final String USER_ID = "userId";

    @Autowired
    MockMvc mockMvc;

    @Test
    void askSantaPage_rendersIdeas() throws Exception {
        mockMvc.perform(get("/letters/1/santa").sessionAttr(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(view().name("santa"))
                .andExpect(content().string(containsString("Spørg Santa Claude")))
                .andExpect(content().string(containsString("Tilføj ønske")))
                .andExpect(content().string(containsString("På din ønskeseddel")));
    }

    @Test
    void askSanta_redirectsBackToChat() throws Exception {
        mockMvc.perform(post("/letters/1/santa").sessionAttr(USER_ID, 1).param("prompt", "kaffe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/letters/1/santa"));
    }
}