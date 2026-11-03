package dk.ek.wishlist.controller;

import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.service.WishService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
     * @return a redirect to the wishlist page
     */
    @PostMapping("/wishlists{wishlistId}/wishes")
    public String createWish(
            @PathVariable int wishlistId,
            @ModelAttribute Wish wish,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes){
        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Kontrollér de indtastede oplysninger."
            );
            return "redirect:/letters/" + wishlistId + "?view=owner";        }

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
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", e.getMessage()
            );
        }

        return "redirect:/letters/" + wishlistId + "?view=owner";    }
}
