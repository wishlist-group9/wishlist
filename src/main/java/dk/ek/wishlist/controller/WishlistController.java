package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.model.Wishlist;
import dk.ek.wishlist.service.WishService;
import dk.ek.wishlist.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Handles requests for wishlists (ønskesedler).
 * The controller only reads the request and picks the next page;
 * validation and saving happen in {@link WishlistService}.
 */
@Controller
public class WishlistController {
    /** Session key for a share link opened before logging in; login sends the user back to it. */
    static final String PENDING_SHARE_TOKEN = "pendingShareToken";

    static final String CREATED = "Ønskesedlen er oprettet.";
    static final String RENAMED = "Navnet er ændret.";


    private final WishlistService wishlistService;
    private final WishService wishService;

    public WishlistController(WishlistService wishlistService, WishService wishService) {
        this.wishlistService = wishlistService;
        this.wishService = wishService;
    }

    /**
     * Shows the overview: the user's own wishlists and the ones shared with them.
     *
     * @param userId id of the logged-in user, or {@code null} if nobody is logged in
     * @param model  receives the two lists for the page
     * @return the overview page, or a redirect to login
     */
    @GetMapping("/letters")
    public String overview(@SessionAttribute(name = "userId", required = false) Integer userId,
                           Model model) {
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("ownWishlists", wishlistService.getOwnWishlists(userId));
        model.addAttribute("sharedWishlists", wishlistService.getSharedWishlists(userId));
        return "letters";
    }

    /**
     * Shows a wishlist the logged-in user owns or that is shared with them, with all its wishes.
     * The owner gets the owner view (add and edit) and the share link ({@code shareLink});
     * everyone else gets the guest view.
     *
     * @param id     id of the wishlist, from the URL
     * @param userId id of the logged-in user, or {@code null} if nobody is logged in
     * @param model  receives the wishlist, its wishes, whether the user owns it and the share link
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

        boolean owner = wishlist.getUserId() == userId;
        model.addAttribute("owner", owner);

        // Only the owner sees the share panel, so only build the link for them
        if (owner) {
            String shareLink = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/s/{token}")
                    .buildAndExpand(wishlist.getShareToken())
                    .toUriString();
            model.addAttribute("shareLink", shareLink);
        }
        return "wishlist";
    }

    /**
     * Creates a wishlist from the "Skriv en ny ønskeseddel" form.
     * Follows Post/Redirect/Get, so refreshing the page never creates the list twice.
     *
     * @param title    the name typed in the form; an empty string if the field is missing
     * @param userId   id of the logged-in user, who will own the list; {@code null} if nobody is logged in
     * @param redirect carries a one-time message to the page we redirect to
     * @return the overview on success, the overview with the form open again on invalid input,
     *         or a redirect to login
     */
    @PostMapping("/wishlists")
    public String create(@RequestParam(defaultValue = "") String title,
                         @SessionAttribute(name = "userId", required = false) Integer userId,
                         RedirectAttributes redirect) {
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            wishlistService.create(userId, title);
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
     * @param userId   id of the logged-in user; {@code null} if nobody is logged in
     * @param redirect carries a one-time message to the page we redirect to
     * @return the list's page, with the form open again if the name was invalid, or a redirect to login
     */
    @PostMapping("/wishlists/{id}/title")
    public String rename(@PathVariable int id, @RequestParam(defaultValue = "") String title,
                         @SessionAttribute(name = "userId", required = false) Integer userId,
                         RedirectAttributes redirect) {
        if (userId == null) {
            return "redirect:/login";
        }
        String listPage = "redirect:/wishlists/" + id;
        try {
            wishlistService.rename(userId, id, title);
            redirect.addFlashAttribute("flash", RENAMED);
            return listPage;
        } catch (InvalidInputException e) {
            redirect.addFlashAttribute("titleError", e.getMessage());
            return listPage + "#edit-letter";
        }
    }

    /**
     * Opens a share link. A logged-in user gets access to the list and is sent to it.
     * A user who isn't logged in is sent to login first; the token is kept in the
     * session, so login can send them back here.
     *
     * @param token   the share token from the link
     * @param userId  id of the logged-in user, or {@code null} if nobody is logged in
     * @param session where the token is kept while the user logs in
     * @return a redirect to the wishlist, or to login
     * @throws dk.ek.wishlist.exception.NotFoundException shown as the 404 page if the link is unknown
     */
    @GetMapping("/s/{token}")
    public String openShareLink(@PathVariable String token,
                                @SessionAttribute(name = "userId", required = false) Integer userId,
                                HttpSession session) {
        if (userId == null) {
            session.setAttribute(PENDING_SHARE_TOKEN, token);
            return "redirect:/login";
        }
        int wishlistId = wishlistService.openShareLink(userId, token);
        return "redirect:/wishlists/" + wishlistId;
    }
}