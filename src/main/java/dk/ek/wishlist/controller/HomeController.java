package dk.ek.wishlist.controller;

import dk.ek.wishlist.service.HealthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

/** The front page and a small health check. */
@Controller
public class HomeController {
    private final HealthService healthService;

    public HomeController(HealthService healthService) {
        this.healthService = healthService;
    }

    /**
     * The front page has no content of its own: logged-in users go to their wishlists,
     * everyone else to the login page.
     *
     * @param userId id of the logged-in user, or {@code null} if nobody is logged in
     * @return a redirect to {@code /letters} or {@code /login}
     */
    @GetMapping("/")
    public String home(@SessionAttribute(name = "userId", required = false) Integer userId) {
        return userId == null ? "redirect:/login" : "redirect:/letters";
    }

    /**
     * Shows whether the database answers. Useful right after a deploy to Azure.
     *
     * @param model receives {@code dbStatus}: "OK" or "ERROR"
     * @return the health page
     */
    @GetMapping("/health")
    public String health(Model model) {
        model.addAttribute("dbStatus", healthService.databaseStatus());
        return "health";
    }
}