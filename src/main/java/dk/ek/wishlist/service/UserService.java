package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.InvalidLoginException;
import dk.ek.wishlist.model.User;
import dk.ek.wishlist.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * Business rules for users: validates input, checks that the email is free
 * and hashes the password before anything is saved through UserRepository
 *
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String INVALID_LOGIN = "Forkert e-mail eller adgangskode";

    // Max lengths - they match the columns in shcema.sql, so the database
    // never has to reject a value that is too long
    static final int MAX_NAME_LENGTH = 100;
    static final int MAX_EMAIL_LENGTH = 255;
    static final int MIN_PASSWORD_LENGTH = 8;


    // The error messages live in constants so they are written once and the tests
    // can reuse them. This is what is shown to the user
    static final String NAME_REQUIRED = "Skriv dit navn";
    static final String NAME_TOO_LONG = "Navnet må højst være " + MAX_NAME_LENGTH + " tegn.";
    static final String EMAIL_REQUIRED = "Skriv din e-mail.";
    static final String EMAIL_INVALID = "E-mailen er ikke gyldig.";
    static final String EMAIL_TOO_LONG = "E-mailen må højst være " + MAX_EMAIL_LENGTH + " tegn.";
    static final String EMAIL_TAKEN = "Der findes allerede en bruger med den e-mail.";
    static final String PASSWORD_REQUIRED = "Skriv en adgangskode.";
    static final String PASSWORD_TOO_SHORT = "Adgangskoden skal være mindst " + MIN_PASSWORD_LENGTH + " tegn.";


    // Constructor injection: Spring sees that this class needs a UserRepository,
    // creates it, and passes it in.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
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

    // Registers a new user

    // Name and email are trimmed and the email is made lower case before it is checked
    // and saved. The password is hashed with BCrypt, so the plain password is never stored

    /**
     * Registers a new user
     * <p>
     *
     *     Name and email are trimmed and the email is made lower case before it is checked
     *     and saved. The password is not trimmed. It is hashed with BCrypt, so the plain
     *     password is never stored.
     * </p>
     * @param name      the name the user typed
     * @param email     the email the used typed
     * @param password  the plain password the user typed
     */
    public void register(String name, String email, String password) {

        // 1. Clean the input:
        // Makes sure that the String is always a real text,
        // and is ready to be correctly checked by the rules below
        String cleanName = name == null ? "" : name.trim();
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();


        // 2. Check the rules, one at a time
        // "throw" stops the method immediately
        if (cleanName.isEmpty()) {
            throw new InvalidInputException(NAME_REQUIRED);
        }
        if (cleanName.length() > MAX_NAME_LENGTH) {
            throw new InvalidInputException(NAME_TOO_LONG);
        }
        if (cleanEmail.isEmpty()) {
            throw new InvalidInputException(EMAIL_REQUIRED);
        }
        if (cleanEmail.length() > MAX_EMAIL_LENGTH) {
            throw new InvalidInputException(EMAIL_TOO_LONG);
        }
        // Email format check:
        // matches() compares the cleanEmail text with this pattern (a regex),
        // to make sure that it fits the standard email pattern.
        if (!cleanEmail.matches("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$")) {
            throw new InvalidInputException(EMAIL_INVALID);
        }
        if (password == null || password.isEmpty()) {
            throw new InvalidInputException(PASSWORD_REQUIRED);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidInputException(PASSWORD_TOO_SHORT);
        }

        // 3. Is the email already taken?
        // findByEmail returns an Optional, and isPresent() is true if a user was found
        if (userRepository.findByEmailNick(cleanEmail).isPresent()) {
            throw new InvalidInputException(EMAIL_TAKEN);
        }


        // 4. Turn the plain password into a BCrypt hash.
        // From here on only the hash is used, and the plain password is never saved
        String passwordHash = passwordEncoder.encode(password);


        // 5. Save the user.

        // If two people register at the same time with the same email:
        // both will pass step 3, because nothing is saved yet.
        // The UNIQUE rule in the database then rejects the second INSERT with a
        // DataIntegrityViolationException - we catch it and show the error message
        try {
            userRepository.save(cleanName, cleanEmail, passwordHash);
        } catch (DataIntegrityViolationException ex) {
            throw new InvalidInputException(EMAIL_TAKEN);
        }
    }
}
