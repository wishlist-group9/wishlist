package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wish;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Handles database operations for wishes
 */
@Repository
public class WishRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a repository with the JdbcTemplate provided by Spring
     * @param jdbcTemplate used to execute SQL statements
     */
    public WishRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Saves a new wish to the database
     * @param wish the wish to save
     */
    public void save(Wish wish){
        String sql = """
                INSERT INTO wish(wishlist_id, title, description, link, price)
                VALUES (?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                wish.getWishlistId(),
                wish.getTitle(),
                wish.getDescription(),
                wish.getLink(),
                wish.getPrice()
        );
    }
}
