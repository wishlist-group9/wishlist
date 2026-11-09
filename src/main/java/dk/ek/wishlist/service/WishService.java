package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.repository.WishRepository;
import dk.ek.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import dk.ek.wishlist.exception.InvalidInputException;

import java.util.Comparator;
import java.util.List;

/**
 * Handles wish validation, creation, listing and updates
 */
@Service
public class WishService {

    private final WishRepository wishRepository;
    private final WishlistRepository wishlistRepository;

    static final String ALREADY_RESERVED = "Ønsket er allerede reserveret.";
    static final String NOT_RESERVED_BY_YOU = "Du har ikke reserveret dette ønske.";
    static final String WISHLIST_NOT_FOUND = "Ønskesedlen findes ikke.";

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
     * @param userId the id of the logged in user
     * @throws InvalidInputException if the title is missing or the price is negative
     * @throws NotFoundException if the wishlist does not exist or belongs to another user
     */
    public void createWish(Wish wish, int userId) {
       validateWish(wish);

        if (!wishlistRepository.existsByIdAndUserId(wish.getWishlistId(), userId)) {
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }

        wishRepository.save(wish);
    }

    /**
     * Gets all wishes on a wishlist. Check that the user may see the list
     * (with {@link WishlistService#getVisibleWishlist}) before calling this.
     *
     * @param wishlistId id of the wishlist
     * @return the wishes, oldest first; an empty list if there are none
     */
    public List<Wish> getWishes(int wishlistId) {
        return wishRepository.findByWishlistId(wishlistId);
    }

    /**
     * Returns the wishes for a wishlist in the requested order
     * When sorting by price, wishes without a price appear last in both directions
     *
     * @param wishlistId the id of the wishlist
     * @param sort the sort option: {@code price-asc} for lowest price first or
     *             {@code price-desc} for highest price first. Other values
     *             preserve the original order
     * @return the wishes in the requested order, or an empty list if there are no wishes
     */
    public List<Wish> getWishes(int wishlistId, String sort){
        List<Wish> wishes = getWishes(wishlistId);

        Comparator<Double> priceOrder = switch(sort){
            case "price-asc" -> Comparator.naturalOrder();
            case "price-desc" -> Comparator.reverseOrder();
            default -> null;
        };

        if (priceOrder == null){
            return wishes;
        }

        return wishes.stream()
                .sorted(Comparator.comparing(
                        Wish::getPrice, Comparator.nullsLast(priceOrder)))
                .toList();
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
            throw new NotFoundException(WISHLIST_NOT_FOUND);
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
            throw new NotFoundException(WISHLIST_NOT_FOUND);
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
    /**
     * Reserves a wish for a guest, so nobody else buys the same gift.
     * Only users the list is shared with may reserve; the owner never is, so they can't.
     *
     * @param wishId     id of the wish
     * @param wishlistId id of the wishlist the wish is on
     * @param userId     id of the logged-in user
     * @throws NotFoundException     if the list isn't shared with the user (also the owner)
     * @throws InvalidInputException if the wish is already reserved, or isn't on that list
     */
    public void reserve(int wishId, int wishlistId, int userId) {
        if (!wishlistRepository.isSharedWith(wishlistId, userId)) {
            throw new NotFoundException(WISHLIST_NOT_FOUND);
        }
        if (wishRepository.reserve(wishId, wishlistId, userId) == 0) {
            throw new InvalidInputException(ALREADY_RESERVED);
        }
    }

    /**
     * Cancels the user's own reservation of a wish.
     *
     * @param wishId     id of the wish
     * @param wishlistId id of the wishlist the wish is on
     * @param userId     id of the logged-in user
     * @throws NotFoundException     if the list isn't shared with the user
     * @throws InvalidInputException if the user hasn't reserved that wish
     */
    public void cancelReservation(int wishId, int wishlistId, int userId) {
        if (!wishlistRepository.isSharedWith(wishlistId, userId)) {
            throw new NotFoundException(WISHLIST_NOT_FOUND);
        }
        if (wishRepository.cancelReservation(wishId, wishlistId, userId) == 0) {
            throw new InvalidInputException(NOT_RESERVED_BY_YOU);
        }
    }

}
