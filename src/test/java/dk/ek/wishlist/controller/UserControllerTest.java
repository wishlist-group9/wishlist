    package dk.ek.wishlist.controller;

    import dk.ek.wishlist.exception.InvalidLoginException;
    import dk.ek.wishlist.model.User;
    import dk.ek.wishlist.service.UserService;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
    import org.springframework.mock.web.MockHttpSession;
    import org.springframework.test.context.bean.override.mockito.MockitoBean;
    import org.springframework.test.web.servlet.MockMvc;

    import static org.hamcrest.Matchers.containsString;
    import static org.junit.jupiter.api.Assertions.assertTrue;
    import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @WebMvcTest(UserController.class)
    class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @Test
        void shouldShowLoginPage() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"))
                    .andExpect(content().string(containsString("Ho ho, velkommen tilbage")));
        }

        @Test
        void shouldRedirectToLettersAndStoreUserIdWhenLoginIsCorrect() throws Exception {
            // Arrange
            User user = new User(1, "Test Ejer", "ejer@test.dk", "hash");
            when(userService.login("ejer@test.dk", "1234")).thenReturn(user);

            // Act + Assert
            mockMvc.perform(post("/login")
                            .param("email", "ejer@test.dk")
                            .param("password", "1234"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/letters"))
                    .andExpect(request().sessionAttribute("userId", 1))
                    .andExpect(request().sessionAttribute("userName", "Test Ejer"));
        }

        @Test
        void shouldShowErrorWhenLoginIsWrong() throws Exception {
            // Arrange
            when(userService.login("ejer@test.dk", "forkert"))
                    .thenThrow(new InvalidLoginException("Forkert e-mail eller adgangskode"));

            // Act + Assert
            mockMvc.perform(post("/login")
                            .param("email", "ejer@test.dk")
                            .param("password", "forkert"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("login"))
                    .andExpect(model().attribute("error", "Forkert e-mail eller adgangskode"))
                    .andExpect(content().string(containsString("Forkert e-mail eller adgangskode")));
        }

        @Test
        void shouldInvalidateSessionAndRedirectToLoginOnLogout() throws Exception {
            // Arrange
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("userId", 1);

            // Act +  Assert
            mockMvc.perform(post("/logout").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));

            assertTrue(session.isInvalid());
        }
    }