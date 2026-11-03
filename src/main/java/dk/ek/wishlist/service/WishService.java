package dk.ek.wishlist.service;

import dk.ek.wishlist.exception.NotFoundException;
import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.repository.WishRepository;
import dk.ek.wishlist.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import dk.ek.wishlist.exception.InvalidInputException;

/**
 * Handles validation and creation of wishes
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
        if (wish.getTitle() == null || wish.getTitle().isBlank()) {
            throw new InvalidInputException(
                    "Ønsket skal have en titel."
            );
        }

        if (wish.getPrice() != null && wish.getPrice() < 0) {
            throw new InvalidInputException(
                    "Prisen må ikke være negativ."
            );
        }

        // Check that the wishlist exists before saving the wish.
        if (!wishlistRepository.existsById(wish.getWishlistId())) {
            throw new NotFoundException("Ønskesedlen findes ikke.");
        }

        wishRepository.save(wish);
    }
}
