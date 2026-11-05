package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wish;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

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

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Wish(
                        rs.getInt("id"),
                        rs.getInt("wishlist_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("link"),
                        rs.getObject("price", Double.class)
                ),
                wishId,
                wishlistId
        ).stream().findFirst();
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

    /**
     * Permanently deletes a Wish from the specified wishlist
     *
     * @param wishId        the id of the wish to delete
     * @param wishlistId    the id of the wishlist that contains the wish
     * @return              the number of rows deleted: 1 if the wish was deleted,
     *                      0 if no wish has that id on that wishlist
     */
    public int delete(int wishId, int wishlistId) {
        // Both ids are in the WHERE clause, so a wish can only be deleted
        // through the wishlist it actually belongs to
        String sql = """
                DELETE FROM wish WHERE id = ? AND wishlist_id = ?
                """;
        return jdbcTemplate.update(sql, wishId, wishlistId);

    }

}
