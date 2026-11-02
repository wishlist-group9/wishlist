package dk.ek.wishlist.exception;

/** Thrown by services when user input breaks a rule. The message is shown to the user, so it is in Danish. */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
