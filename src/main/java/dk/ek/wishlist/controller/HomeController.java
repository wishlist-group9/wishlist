package dk.ek.wishlist.controller;

import dk.ek.wishlist.service.HealthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final HealthService healthService;

    public HomeController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("dbStatus", healthService.databaseStatus());
        return "index";
    }
}
