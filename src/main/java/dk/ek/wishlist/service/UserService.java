package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidLoginException;
import dk.ek.wishlist.model.User;
import dk.ek.wishlist.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Business rules for users: checks login details against the stored
 * password hash from {@link UserRepository}.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String INVALID_LOGIN = "Forkert e-mail eller adgangskode";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks the e-mail and password and returns the matching user.
     * <p>
     * The same error is used for an unknown e-mail and a wrong password, so the
     * message doesn't reveal which e-mails have an account.
     *
     * @param email       the e-mail the user typed
     * @param rawPassword the password the user typed, in plain text
     * @return the logged-in user
     * @throws InvalidLoginException if no user has that e-mail, or the password doesn't match
     */
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new InvalidLoginException(INVALID_LOGIN);
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidLoginException(INVALID_LOGIN);
        }

        return user;

    }
}
