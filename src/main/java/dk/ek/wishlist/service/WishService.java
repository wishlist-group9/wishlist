package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.repository.WishRepository;
import dk.ek.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import dk.ek.wishlist.exception.InvalidInputException;

/**
 * Handles wish validation, creation, retrieval for editing, and updates
 */
@Service
public class WishService {

    private final WishRepository wishRepository;
    private final WishlistRepository wishlistRepository;

    /**
     * Creates a service with the given repositories
     * @param wishRepository used to access and modify wish data
     * @param wishlistRepository used to access wishlist data
     */
    public WishService(WishRepository wishRepository, WishlistRepository wishlistRepository){
        this.wishRepository = wishRepository;
        this.wishlistRepository = wishlistRepository;
    }

    /**
     * Validates a wish and saves it to the database
     *
     * @param wish the wish to create
     * @throws InvalidInputException if the title is missing or the price is negative
     * @throws NotFoundException if the wishlist does not exist
     */
    public void createWish(Wish wish) {
       validateWish(wish);

        // Check that the wishlist exists before saving the wish.
        if (!wishlistRepository.existsById(wish.getWishlistId())) {
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }

        wishRepository.save(wish);
    }

    /**
     * Retrieves an existing wish for editing from the specified wishlist
     *
     * @param wishId the id of the wish to retrieve
     * @param wishlistId the id of the wishlist that contains the wish
     * @param userId the id of the logged-in user
     * @return the wish with its current values
     * @throws NotFoundException if the wishlist does not exist, belongs to another user,
     *          or does not contain the specified wish
     */
    public Wish getWishForEdit(int wishId, int wishlistId, int userId){
        //Use the same error for missing and inaccessible wishlists
        if(!wishlistRepository.existsByIdAndUserId(wishlistId, userId)){
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }
        return wishRepository.findByIdAndWishlistId(wishId, wishlistId)
                .orElseThrow(() ->
                        new NotFoundException("Ønsket findes ikke på denne ønskeseddel."));
    }

    /**
     * Validates and saves the edited values for an existing wish
     *
     * @param wishId the id of the wish to update
     * @param wishlistId the id of the wishlist that contains the wish
     * @param userId the id of the logged-in user
     * @param wish the wish containing the new values
     * @throws InvalidInputException if the title is missing or the price is negative
     * @throws NotFoundException if the wishlist does not exist, belongs to another user,
     *        or does not contain the specified wish
     */
    public void updateWish(int wishId, int wishlistId, int userId, Wish wish){
        if (!wishlistRepository.existsByIdAndUserId(wishlistId, userId)) {
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }

        validateWish(wish);

        int updatedRows = wishRepository.update(wishId, wishlistId, wish);

        if (updatedRows == 0){
            throw new NotFoundException("Ønsket findes ikke på denne ønskeseddel.");
        }
    }

    /**
     * Permanently deletes a wish from a wishlist that belongs to the logged-in user
     *
     * @param wishId        the id of the wish to delete
     * @param wishlistId    the id of the wishlist that contains the wish
     * @param userId        the id of the logged-in user
     * @throws NotFoundException if the wishlist does not exist, belongs to another user,
     *                           or does not contain the specified wish
     */
    public void deleteWish(int wishId, int wishlistId, int userId) {
        // Use the same error for missing and inaccessible wishlists
        if (!wishlistRepository.existsByIdAndUserId(wishlistId, userId)) {
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }

        int deletedRows = wishRepository.delete(wishId, wishlistId);

        // 0 deleted rows means the wish does not exist on this wishlist
        if (deletedRows == 0) {
            throw new NotFoundException("Ønsket findes ikke på denne ønskeseddel.");
        }
    }

    /**
     * Checks that the wish has a title and that its price, if provided, is not negative
     *
     * @param wish the wish to validate
     * @throws InvalidInputException if the title is missing or the price is negative
     */
    private void validateWish(Wish wish){
        if (wish.getTitle() == null || wish.getTitle().isBlank()) {
            throw new InvalidInputException(
                    "Ønsket skal have en titel."
            );
        }

        //A price is optional, so only validate it when one is provided
        if (wish.getPrice() != null && wish.getPrice() < 0) {
            throw new InvalidInputException(
                    "Prisen må ikke være negativ."
            );
        }
    }
}
