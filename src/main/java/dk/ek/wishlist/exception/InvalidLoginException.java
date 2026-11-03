package dk.ek.wishlist.exception;

/**
 * Thrown when the e-mail or password is wrong. The message is shown to the user, so it is in Danish
 * and doesn't say which of the two was wrong.
 */
public class InvalidLoginException extends RuntimeException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
