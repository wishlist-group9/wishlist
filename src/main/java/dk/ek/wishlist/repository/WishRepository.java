package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wish;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

/**
 * Handles database operations for wishes
 */
@Repository
public class WishRepository {

    /** Turns one row from the wish table into a Wish. Shared by every query that reads wishes. */
    private static final RowMapper<Wish> WISH_MAPPER = (rs, rowNum) -> new Wish(
            rs.getInt("id"),
            rs.getInt("wishlist_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("link"),
            rs.getObject("price", Double.class)
    );

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

    /**
     * Finds a wish by its id within the specified wishlist
     *
     * @param wishId the id of the wish to find
     * @param wishlistId the id of the wishlist to search
     * @return an optional containing the wish if both ids match, or an empty optional
     */
    public Optional<Wish> findByIdAndWishlistId(int wishId, int wishlistId){
        String sql = """
                SELECT id, wishlist_id, title, description, link, price
                FROM wish
                WHERE id = ? AND wishlist_id = ?
                """;

        return jdbcTemplate.query(sql, WISH_MAPPER, wishId, wishlistId)
                .stream().findFirst();
    }

    /**
     * Finds all wishes on one wishlist, oldest first.
     *
     * @param wishlistId id of the wishlist
     * @return the wishes on that list; an empty list if it has none
     */
    public List<Wish> findByWishlistId(int wishlistId) {
        String sql = """
                SELECT id, wishlist_id, title, description, link, price
                FROM wish
                WHERE wishlist_id = ?
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, WISH_MAPPER, wishlistId);
    }

    /**
     * Updates the title, description, link and price of a wish
     * within the specified wishlist
     *
     * @param wishId the id of the wish to update
     * @param wishlistId the id of the wishlist that contains the wish
     * @param wish the wish containing the new values
     * @return the number of rows affected by the update
     */
    public int update(int wishId, int wishlistId, Wish wish){
        String sql = """
                UPDATE wish
                SET title = ?, description = ?, link = ?, price = ?
                WHERE id = ? AND wishlist_id = ?
                """;

        return jdbcTemplate.update(
                sql,
                wish.getTitle(),
                wish.getDescription(),
                wish.getLink(),
                wish.getPrice(),
                wishId,
                wishlistId
        );
    }
}
