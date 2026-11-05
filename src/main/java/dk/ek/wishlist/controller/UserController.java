package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidLoginException;
import dk.ek.wishlist.model.User;
import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Handles logging in. A successful login stores the user's id in the session
 * under {@code "userId"}.
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

    /** Shows the login form. */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
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


    /**
     * Logs the user in and sends them to their wishlists.
     *
     * @param email    the e-mail the user typed
     * @param password the password the user typed, in plain text
     * @param session  the session where the logged-in user's id is stored
     * @param model    gets an {@code "error"} message if the login fails
     * @return a redirect to {@code /letters} on success, otherwise the login page with an error
    */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.login(email, password);

            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());   // shown in the topbar and greeting
            return "redirect:/letters";

        } catch (InvalidLoginException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    /**
     * Logs the user out by ending the session.
     *
     * @param session the current session, which is invalidated
     * @return a redirect to {@code /login}
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
