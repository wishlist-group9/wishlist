package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wish;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

/**
 * Handles database operations for wishes
 */
@Repository
public class WishRepository {

    // Only reserves a free wish: if someone else got there first, 0 rows change.
    private static final String RESERVE =
            "UPDATE wish SET reserved_by = ? WHERE id = ? AND wishlist_id = ? AND reserved_by IS NULL";
    // Only cancels the user's own reservation.
    private static final String CANCEL_RESERVATION =
            "UPDATE wish SET reserved_by = NULL WHERE id = ? AND wishlist_id = ? AND reserved_by = ?";


    /** Turns one row from the wish table into a Wish. Shared by every query that reads wishes. */
    private static final RowMapper<Wish> WISH_MAPPER = (rs, rowNum) -> {
        Wish wish = new Wish(
                rs.getInt("id"),
                rs.getInt("wishlist_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("link"),
                rs.getObject("price", Double.class)
        );
        wish.setReservedBy(rs.getObject("reserved_by", Integer.class));
        return wish;
    };

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
     * Finds all wishes on one wishlist, oldest first.
     *
     * @param wishlistId id of the wishlist
     * @return the wishes on that list; an empty list if it has none
     */
    public List<Wish> findByWishlistId(int wishlistId) {
        String sql = """
                SELECT id, wishlist_id, title, description, link, price, reserved_by
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

    /**
     * Reserves a wish for a user, but only if nobody has reserved it yet.
     *
     * @param wishId     id of the wish
     * @param wishlistId id of the wishlist the wish must belong to
     * @param userId     id of the user reserving it
     * @return 1 if the wish was reserved; 0 if it was already reserved or isn't on that list
     */
    public int reserve(int wishId, int wishlistId, int userId) {
        return jdbcTemplate.update(RESERVE, userId, wishId, wishlistId);
    }

    /**
     * Cancels a reservation, but only the user's own.
     *
     * @param wishId     id of the wish
     * @param wishlistId id of the wishlist the wish must belong to
     * @param userId     id of the user who reserved it
     * @return 1 if the reservation was cancelled; 0 if the user hadn't reserved that wish
     */
    public int cancelReservation(int wishId, int wishlistId, int userId) {
        return jdbcTemplate.update(CANCEL_RESERVATION, wishId, wishlistId, userId);
    }

}
