package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.InvalidInputException;
import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.repository.WishlistRepository;

import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Business rules for wishlists: validates input and creates share tokens
 * before anything is saved through {@link WishlistRepository}.
 */
@Service
public class WishlistService {
    private final WishlistRepository repository;

    static final int MAX_TITLE_LENGTH = 100;   // matches VARCHAR(100) in schema.sql
    static final String TITLE_REQUIRED = "Giv ønskesedlen et navn.";
    static final String TITLE_TOO_LONG = "Navnet må højst være " + MAX_TITLE_LENGTH + " tegn.";

    public WishlistService(WishlistRepository repository) {
        this.repository = repository;
    }

    /**
     * Trims the title and checks the rules shared by create and rename.
     *
     * @throws InvalidInputException if the title is empty or longer than {@value #MAX_TITLE_LENGTH} characters
     */
    private String cleanTitle(String title) {
        String clean = title == null ? "" : title.trim();
        if (clean.isEmpty()) {
            throw new InvalidInputException(TITLE_REQUIRED);
        }
        if (clean.length() > MAX_TITLE_LENGTH) {
            throw new InvalidInputException(TITLE_TOO_LONG);
        }
        return clean;
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
        String cleanTitle = cleanTitle(title);
        String shareToken = UUID.randomUUID().toString();
        return repository.save(userId, cleanTitle, shareToken);
    }

    /**
     * Gives one of the user's wishlists a new title.
     *
     * @param userId     id of the user renaming the list
     * @param wishlistId id of the wishlist to rename
     * @param title      the new title as typed; it's trimmed before it's saved
     * @throws InvalidInputException if the title breaks the rules in {@link #cleanTitle(String)}
     * @throws NotFoundException     if the wishlist doesn't exist or belongs to someone else
     */
    public void rename(int userId, int wishlistId, String title) {
        String cleanTitle = cleanTitle(title);
        if (!repository.updateTitle(userId, wishlistId, cleanTitle)) {
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }
    }

}
