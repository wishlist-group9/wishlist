package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.model.Wishlist;
import dk.ek.wishlist.service.WishService;
import dk.ek.wishlist.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
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
    static final String RENAMED = "Navnet er ændret.";

    private final WishlistService wishlistService;
    private final WishService wishService;

    public WishlistController(WishlistService wishlistService, WishService wishService) {
        this.wishlistService = wishlistService;
        this.wishService = wishService;
    }

    /**
     * Shows a wishlist the logged-in user owns or that is shared with them, with all its wishes.
     * The owner gets the owner view (add and edit); everyone else gets the guest view.
     *
     * @param id     id of the wishlist, from the URL
     * @param userId id of the logged-in user, or {@code null} if nobody is logged in
     * @param model  receives the wishlist, its wishes and whether the user owns it
     * @return the wishlist page, or a redirect to login
     * @throws dk.ek.wishlist.exception.NotFoundException shown as the 404 page if the list
     *         doesn't exist or the user may not see it
     */
    @GetMapping("/wishlists/{id}")
    public String show(@PathVariable int id,
                       @SessionAttribute(name = "userId", required = false) Integer userId,
                       Model model) {
        if (userId == null) {
            return "redirect:/login";
        }
        Wishlist wishlist = wishlistService.getVisibleWishlist(userId, id);
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("wishes", wishService.getWishes(id));
        model.addAttribute("owner", wishlist.getUserId() == userId);
        return "wishlist";
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

    /**
     * Renames a wishlist from the "Redigér navn" form on the list's own page.
     *
     * @param id       id of the wishlist, from the URL
     * @param title    the new name typed in the form
     * @param redirect carries a one-time message to the page we redirect to
     * @return the list's page, with the form open again if the name was invalid
     */
    @PostMapping("/wishlists/{id}/title")
    public String rename(@PathVariable int id, @RequestParam(defaultValue = "") String title,
                         RedirectAttributes redirect) {
        String listPage = "redirect:/wishlists/" + id;
        try {
            wishlistService.rename(DEMO_USER_ID, id, title);
            redirect.addFlashAttribute("flash", RENAMED);
            return listPage;
        } catch (InvalidInputException e) {
            redirect.addFlashAttribute("titleError", e.getMessage());
            return listPage + "#edit-letter";
        }
    }

}