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

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

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
