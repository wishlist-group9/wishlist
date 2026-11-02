package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.repository.WishlistRepository;

import java.util.UUID;

/**
 * Business rules for wishlists: validates input and creates share tokens
 * before anything is saved through {@link WishlistRepository}.
 */
public class WishlistService {
    private final WishlistRepository repository;

    static final int MAX_TITLE_LENGTH = 100;   // matches VARCHAR(100) in schema.sql
    static final String TITLE_REQUIRED = "Giv ønskesedlen et navn.";
    static final String TITLE_TOO_LONG = "Navnet må højst være " + MAX_TITLE_LENGTH + " tegn.";

    public WishlistService(WishlistRepository repository) {
        this.repository = repository;
    }


    /**
     * Creates a new wishlist for a user.
     * <p>
     * The title is trimmed before it is checked and saved. Each new wishlist gets a random
     * share token, which is later used in its share link.
     *
     * @param userId id of the user who will own the wishlist
     * @param title  the title the user typed; leading and trailing spaces are removed
     * @return the id of the new wishlist
     * @throws InvalidInputException if the title is missing, blank, or longer than
     *                               {@value #MAX_TITLE_LENGTH} characters
     */
    public int create(int userId, String title) {
        // 1. Clean the input
        String cleanTitle = title == null ? "" : title.trim();

        // 2. Check the rules
        if (cleanTitle.isEmpty()) {
            throw new InvalidInputException(TITLE_REQUIRED);
        }
        if (cleanTitle.length() > 100) {
            throw new InvalidInputException(TITLE_TOO_LONG);
        }

        // 3. Create the share token
        String shareToken = UUID.randomUUID().toString();

        // 4. Save, and return the new id
        return repository.save(userId, cleanTitle, shareToken);
    }

}
