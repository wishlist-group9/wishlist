package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles requests for wishlists (ønskesedler).
 * The controller only reads the request and picks the next page;
 * validation and saving happen in {@link WishlistService}.
 */
@Controller
public class WishlistController {

    /** Temporary owner of new wishlists until login (#59) exists. Matches the demo user in data-dev.sql. */
    static final int DEMO_USER_ID = 1;

    static final String CREATED = "Ønskesedlen er oprettet.";

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /**
     * Creates a wishlist from the "Skriv en ny ønskeseddel" form.
     * Follows Post/Redirect/Get, so refreshing the page never creates the list twice.
     *
     * @param title    the name typed in the form; an empty string if the field is missing
     * @param redirect carries a one-time message to the page we redirect to
     * @return the overview on success, or the overview with the form open again on invalid input
     */
    @PostMapping("/wishlists")
    public String create(@RequestParam(defaultValue = "") String title, RedirectAttributes redirect) {
        try {
            wishlistService.create(DEMO_USER_ID, title);
            redirect.addFlashAttribute("flash", CREATED);
            return "redirect:/letters";
        } catch (InvalidInputException e) {
            redirect.addFlashAttribute("titleError", e.getMessage());
            return "redirect:/letters#new-letter";
        }
    }
}