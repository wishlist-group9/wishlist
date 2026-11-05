package dk.ek.wishlist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
    void lettersPage_rendersBothSections() throws Exception {
        mockMvc.perform(get("/letters").sessionAttr(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Skrevet af dig")))
                .andExpect(content().string(containsString("Delt med dig")));
    }

    @Test
    void lettersPage_showsFlashMessage() throws Exception {
        mockMvc.perform(get("/letters").sessionAttr(USER_ID, 1).flashAttr("flash", "Ønskesedlen er oprettet."))
                .andExpect(content().string(containsString("Ønskesedlen er oprettet.")));
    }

    @Test
    void wishlist_guestSeesReservations() throws Exception {
        mockMvc.perform(get("/letters/1").sessionAttr(USER_ID, 1))
                .andExpect(status().isOk())
                .andExpect(model().attribute("owner", false))
                .andExpect(content().string(containsString("badge-reserved")))
                .andExpect(content().string(containsString("Fortryd reservation")))
                .andExpect(content().string(not(containsString("Tilføj ønske"))))
                .andExpect(content().string(not(containsString("Slet ønsket?"))));
    }

    @Test
    void wishlist_marksActiveFilter() throws Exception {
        mockMvc.perform(get("/letters/1").sessionAttr(USER_ID, 1).param("filter", "available"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("filter", "available"))
                .andExpect(content().string(containsString("aria-current=\"page\"")));
    }

    @Test
    void wishlist_ownerNeverSeesReservations() throws Exception {
        mockMvc.perform(get("/letters/1").sessionAttr(USER_ID, 1).param("view", "owner"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("owner", true))
                .andExpect(content().string(containsString("Tilføj ønske")))
                .andExpect(content().string(containsString("Lad Santa Claude hjælpe")))
                .andExpect(content().string(containsString("Slet ønsket?")))
                .andExpect(content().string(not(containsString("badge-reserved"))))
                .andExpect(content().string(not(containsString("badge-mine"))));
    }

    @Test
    void addWish_redirectsToOwnerViewWithConfirmation() throws Exception {
        mockMvc.perform(post("/letters/1/wishes").sessionAttr(USER_ID, 1).param("title", "Uldtørklæde"))
                .andExpect(redirectedUrl("/letters/1?view=owner"))
                .andExpect(flash().attribute("flash", "Ønsket er tilføjet til din ønskeseddel."));
    }

    @Test
    void deleteWish_redirectsWithConfirmation() throws Exception {
        mockMvc.perform(post("/wishes/1/delete").sessionAttr(USER_ID, 1))
                .andExpect(redirectedUrl("/letters/1?view=owner"))
                .andExpect(flash().attribute("flash", "Ønsket er slettet."));
    }

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