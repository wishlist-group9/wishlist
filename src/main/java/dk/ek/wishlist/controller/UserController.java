package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidLoginException;
import dk.ek.wishlist.model.User;
import dk.ek.wishlist.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles logging in. A successful login stores the user's id in the session
 * under {@code "userId"}.
 */
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Shows the login form. */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
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
            return "redirect:/letters";

        } catch (InvalidLoginException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }
}
