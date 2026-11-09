package dk.ek.wishlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Shows the Santa Claude design with sample content, so the pages can be
 * viewed before the real features exist.
 */
@Controller
public class DesignPreviewController {

    /** Ask Santa Claude for gift ideas. */
    @GetMapping("/letters/{id}/santa")
    public String askSanta() {
        return "santa";
    }

    /** Preview only: asking Santa just shows the same sample conversation again. */
    @PostMapping("/letters/{id}/santa")
    public String fakeAskSanta(@PathVariable int id) {
        return "redirect:/letters/" + id + "/santa";
    }
}
