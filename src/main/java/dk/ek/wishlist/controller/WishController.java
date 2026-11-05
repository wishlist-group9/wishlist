package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.service.WishService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import dk.ek.wishlist.exception.InvalidInputException;


/**
 * Handles requests for wishes: creating and editing them from the modals on the wishlist page
 */
@Controller
public class WishController {

    private final WishService wishService;

    /**
     * Creates a controller with the given service
     * @param wishService used to handle wish operations
     */
    public WishController(WishService wishService){
        this.wishService = wishService;
    }

    /**
     * Creates a wish on the selected wishlist
     *
     * @param wishlistId the ID of the selected wishlist
     * @param userId the id of the logged in user, or {@code null} if absent
     * @param wish the wish containing the values submitted in the form
     * @param bindingResult contains errors when converting form values
     * @param redirectAttributes used to display messages after redirecting
     * @return a redirect to the wishlist page, to the overview if the wishlist
     *         does not exist or belongs to another user, or to the login page
     *         if the user is not logged in
     */
    @PostMapping("/wishlists/{wishlistId}/wishes")
    public String createWish(
            @PathVariable int wishlistId,
            @SessionAttribute(name = "userId", required = false) Integer userId,
            @ModelAttribute Wish wish,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes){

        if(userId == null){
            return "redirect:/login";
        }
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Kontrollér de indtastede oplysninger."
            );
            return "redirect:/wishlists/" + wishlistId;
        }

        //Assigns the wish to the wishlist identified by the ID in the URL
        wish.setWishlistId(wishlistId);

        //Calls the service to create the wish
        //If title or price is invalid, catches the exception
        //and adds its message to be displayed after the redirect
        try{
            wishService.createWish(wish, userId);

            redirectAttributes.addFlashAttribute(
                    "flash", "Ønsket blev oprettet."
            );
        } catch (InvalidInputException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        } catch (NotFoundException e) {
            //Return to the overview when the wishlist does not exist
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
            return "redirect:/letters";
        }
        return "redirect:/wishlists/" + wishlistId;
    }

    /**
     * Saves the edited values for a wish on the user's wishlist. The form is the
     * "Redigér ønske" modal on the wishlist page (#edit-wish-{wishId}).
     *
     * @param wishlistId the id of the selected wishlist
     * @param wishId the id of the wish to update
     * @param userId the id of the logged in user, or {@code null} if absent
     * @param wish the values submitted in the edit form
     * @param bindingResult contains form binding errors, e.g. a price that is not a number
     * @param redirectAttributes carries messages across redirects
     * @return the wishlist page on success, the wishlist page with the edit modal open
     *         again on invalid input, the overview if the wish cannot be edited,
     *         or a redirect to login
     */
    @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/update")
    public String updateWish(
            @PathVariable int wishlistId,
            @PathVariable int wishId,
            @SessionAttribute(name = "userId", required = false) Integer userId,
            @ModelAttribute("wish") Wish wish,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (userId == null) {
            return "redirect:/login";
        }

        String listPage = "redirect:/wishlists/" + wishlistId;
        String editModal = "edit-wish-" + wishId;

        //A value that could not be converted (e.g. price "abc") never reaches the service
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("formError", "Kontrollér de indtastede oplysninger.");
            redirectAttributes.addFlashAttribute("formErrorFor", editModal);
            return listPage + "#" + editModal;
        }

        try {
            wishService.updateWish(wishId, wishlistId, userId, wish);
        } catch (InvalidInputException e) {
            //Open the same modal again with the error inside it
            redirectAttributes.addFlashAttribute("formError", e.getMessage());
            redirectAttributes.addFlashAttribute("formErrorFor", editModal);
            return listPage + "#" + editModal;
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/letters";
        }

        redirectAttributes.addFlashAttribute("flash", "Ønsket blev opdateret.");
        return listPage;
    }

    /**
     * Deletes a wish from the user's wishlist
     * HTML forms can only send GET and POST, so deleting uses post
     *
     * @param wishlistId            the id of the wishlist in the URL
     * @param wishId                the id of the wish to delete
     * @param userId                the id of the logged-in user, or null if absent
     * @param redirectAttributes    carries messages across redirects
     * @return                      a redirect to login if the user is not logged in,
     *                              to the wishlist overview if the wish cannot be deleted,
     *                              or back to the wishlist after successful delete
     */
    @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/delete")
    public String deleteWish(
            @PathVariable int wishlistId,
            @PathVariable int wishId,
            @SessionAttribute(name = "userId", required = false) Integer userId,
            RedirectAttributes redirectAttributes) {

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            wishService.deleteWish(wishId, wishlistId, userId);
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/letters";
        }

        redirectAttributes.addFlashAttribute("flash", "Ønsket blev slettet.");
        return "redirect:/wishlists/" + wishlistId;
    }
}
