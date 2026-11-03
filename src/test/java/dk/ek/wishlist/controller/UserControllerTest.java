package dk.ek.wishlist.controller;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UserService userService;

    @Test
    void registerPage_rendersForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void register_validInput_redirectsToLoginWithFlash() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Anna")
                        .param("email", "anna@mail.dk")
                        .param("password", "hunter22"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("flash"));

        verify(userService).register("Anna", "anna@mail.dk", "hunter22");
    }

    @Test
    void register_invalidInput_showsFormAgainWithError() throws Exception {
        doThrow(new InvalidInputException("Skriv dit navn."))
                .when(userService).register("", "anna@mail.dk", "hunter22");

        mockMvc.perform(post("/register")
                        .param("name", "")
                        .param("email", "anna@mail.dk")
                        .param("password", "hunter22"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attribute("error", "Skriv dit navn."))
                .andExpect(model().attribute("email", "anna@mail.dk"))
                .andExpect(model().attributeDoesNotExist("password"));
    }
}