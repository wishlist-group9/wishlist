package dk.ek.wishlist.service;

import dk.ek.wishlist.model.Wish;
import dk.ek.wishlist.repository.WishRepository;
import org.springframework.stereotype.Service;

/**
 * Handles validation and creation of wishes
 */
@Service
public class WishService {

    private final WishRepository wishRepository;

    /**
     * Creates a service with the given repository
     * @param wishRepository used to access and modify wish data
     */
    public WishService(WishRepository wishRepository){
        this.wishRepository = wishRepository;
    }

    /**
     * Validates a wish and saves it to the database
     * @param wish the wish to create
     * @throws IllegalArgumentException if the title is missing or the price is negative
     */
    //TODO: Replace IllegalArgumentException with InvalidInputException when merged
    public void createWish(Wish wish){
        if(wish.getTitle() == null || wish.getTitle().isBlank()){
            throw new IllegalArgumentException(
                    "Ønsket skal have en titel."
            );
        }

        if (wish.getPrice() != null && wish.getPrice() < 0) {
            throw new IllegalArgumentException(
                    "Prisen må ikke være negativ."
            );
        }

        wishRepository.save(wish);
    }
}
