package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Handles the web requests for registering a new user: shows the form and receives
 * it when it is submitted. The rules (validation, hashing and saving)
 * live in UserService, so this class only connects the web to tho the service
 */
@Controller
public class UserController {


    // The name the layout reads to show a one-time confirmation message.
    // It must match "flash" in fragments/layout.html
    private static final String FLASH = "flash";


    private final UserService userService;


    /**
     * Creates the controller with the service it needs.
     *
     * @param userService the service that registers users
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * Shows the empty registration form.
     *
     * @return the name of the template register.html
     */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }


    /**
     *  Receives the registration form. If the input is valid, the user is saved and
     *  redirected to the login page with a confirmation. If a rule is broken, the form
     *  is shown again with the error message and what the user typed.
     *
     * @param name      the name field from the form
     * @param email     the email field from the form
     * @param password  the password field from the form
     * @param model     used to send the error message and typed values to the template
     * @param redirect  used to carry the confirmation message across the redirect
     * @return a redirect to /login on succes, otherwise the register template
     */
    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model,
                           RedirectAttributes redirect) {
        try {
            userService.register(name, email, password);

            redirect.addFlashAttribute(FLASH, "Din konto er oprettet. Log in for at komme i gang.");
            return "redirect:/login";
        } catch (InvalidInputException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "register";
        }
    }
}
