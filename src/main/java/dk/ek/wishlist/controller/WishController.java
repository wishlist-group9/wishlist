package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.service.WishService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import dk.ek.wishlist.exception.InvalidInputException;


/**
 * Handles requests for wishes
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
     * @param wish the wish containing the values submitted in the form
     * @param bindingResult contains errors when converting form values
     * @param redirectAttributes used to display messages after redirecting
     * @return a redirect to the wishlist page, or the overview if th wishlist does not exist
     */
    @PostMapping("/wishlists/{wishlistId}/wishes")
    public String createWish(
            @PathVariable int wishlistId,
            @ModelAttribute Wish wish,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes){
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
            wishService.createWish(wish);

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
     * Shows the edit form with the current values of the selected wish
     *
     * @param wishlistId the id of the selected wishlist
     * @param wishId the id of the wish to edit
     * @param userId the ID of the logged-in user, or {@code null} if no user is logged in
     * @param model used to pass the wish to the edit form
     * @param redirectAttributes used to display an error after redirecting
     *@return the edit form, a redirect to login if the user is not logged in,
     *        or a redirect to the wishlist overview if the wish cannot be edited
     */
    @GetMapping("/wishlists/{wishlistId}/wishes/{wishId}/edit")
    public String showEditWishForm(
            @PathVariable int wishlistId,
            @PathVariable int wishId,
            @SessionAttribute(name = "userId", required = false) Integer userId,
            Model model,
            RedirectAttributes redirectAttributes){
        if(userId == null){
            return "redirect:/login";
        }
        try{
            Wish wish = wishService.getWishForEdit(wishId, wishlistId, userId);
            model.addAttribute("wish", wish);
            model.addAttribute("wishlistId", wishlistId);
            model.addAttribute("wishId", wishId);
            return "edit-wish";
        } catch (NotFoundException e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/letters";
        }
    }

    /**
     * Saves the edited values for a wish on the user's wishlist
     *
     * @param wishlistId the id of the selected wishlist
     * @param wishId the id of the wish to update
     * @param userId the id of the logged in user, or {@code null} if absent
     * @param wish the values submitted in the edit form
     * @param bindingResult contains form binding and validation errors
     * @param model supplies the ids when the edit form is shown again
     * @param redirectAttributes carries messages across redirects
     * @return the edit form on input errors, or a redirect to login,
     *         the wishlist overview, or the selected wishlist
     */
    @PostMapping("/wishlists/{wishlistId}/wishes/{wishId}/update")
    public String updateWish(
            @PathVariable int wishlistId,
            @PathVariable int wishId,
            @SessionAttribute(name = "userId", required = false) Integer userId,
            @ModelAttribute("wish") Wish wish,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("wishlistId", wishlistId);
        model.addAttribute("wishId", wishId);

        try {
            if (bindingResult.hasErrors()) {
                //Check access before showing the form again
                wishService.getWishForEdit(wishId, wishlistId, userId);
                return "edit-wish";
            }

            wishService.updateWish(wishId, wishlistId, userId, wish);
        } catch (InvalidInputException e) {
            bindingResult.reject("wish.invalid", e.getMessage());
            return "edit-wish";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/letters";
        }

        redirectAttributes.addFlashAttribute("flash", "Ønsket blev opdateret.");
        return "redirect:/wishlists/" + wishlistId;
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
        return "redirect:/letters/" + wishlistId + "?view=owner";
    }
}
