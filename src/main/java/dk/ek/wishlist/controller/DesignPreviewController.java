package dk.ek.wishlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Shows the Santa Claude design with sample content, so the pages can be
 * viewed before the real features exist. Nothing is saved: every form just
 * redirects back with the confirmation message the real feature should show.
 *
 * Replace each method with a real controller (LoginController,
 * WishlistController, ...) as the features are built, then delete this class.
 */
@Controller
public class DesignPreviewController {

    private static final String FLASH = "flash";
    private static final String OWNER_VIEW = "redirect:/wishlists/1";



    @PostMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/letters")
    public String letters() {
        return "letters";
    }

    @PostMapping("/letters")
    public String fakeCreateLetter(RedirectAttributes redirect) {
        redirect.addFlashAttribute(FLASH, "Ønskesedlen er oprettet.");
        return "redirect:/letters";
    }

    @PostMapping("/letters/{id}/wishes")
    public String fakeAddWish(RedirectAttributes redirect) {
        redirect.addFlashAttribute(FLASH, "Ønsket er tilføjet til din ønskeseddel.");
        return OWNER_VIEW;
    }

    @PostMapping("/wishes/{id}")
    public String fakeEditWish(RedirectAttributes redirect) {
        redirect.addFlashAttribute(FLASH, "Dine ændringer er gemt.");
        return OWNER_VIEW;
    }

    @PostMapping("/wishes/{id}/delete")
    public String fakeDeleteWish(RedirectAttributes redirect) {
        redirect.addFlashAttribute(FLASH, "Ønsket er slettet.");
        return OWNER_VIEW;
    }

    /** Ask Santa Claude for gift ideas. */
    @GetMapping("/letters/{id}/santa")
    public String askSanta() {
        return "santa";
    }

    /** Preview only: asking Santa just shows the same sample conversation again. */
    @PostMapping("/letters/{id}/santa")
    public String fakeAskSanta(@PathVariable int id) {
        return "redirect:/letters/" + id + "/santa";
    }
}
