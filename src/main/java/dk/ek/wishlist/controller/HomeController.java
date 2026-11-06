package dk.ek.wishlist.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Redirects to the wishlist overview. A user who is not logged in is sent on
     * to the login page by LoginInterceptor
     *
     * @return a redirect to /letters
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/letters";
    }
}
