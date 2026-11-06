package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wishlist;
import dk.ek.wishlist.model.WishlistSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/** Reads and writes the wishlist table **/

@Repository
public class WishlistRepository {
    private final JdbcTemplate jdbcTemplate;

    public WishlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT_WISHLIST = "INSERT INTO wishlist (user_id, title, share_token) VALUES (?,?,?)";
    private static final String UPDATE_TITLE = "UPDATE wishlist SET title = ? WHERE id = ? AND user_id = ?";
    private static final String SELECT_BY_ID = "SELECT id, user_id, title, share_token FROM wishlist WHERE id = ?";
    private static final String IS_SHARED_WITH = "SELECT COUNT(*) FROM wishlist_share WHERE wishlist_id = ? AND user_id = ?";
    private static final String SELECT_BY_SHARE_TOKEN = "SELECT id, user_id, title, share_token FROM wishlist WHERE share_token = ?";
    private static final String INSERT_SHARE = "INSERT INTO wishlist_share (wishlist_id, user_id) VALUES (?, ?)";

    // Overview queries: one row per wishlist with the owner's name and the number of wishes.
    // LEFT JOIN keeps lists without wishes (COUNT gives 0 for them).
    private static final String SELECT_OWNED_BY = """
            SELECT w.id, w.title, u.name AS owner_name, COUNT(wi.id) AS wish_count
            FROM wishlist w
            JOIN app_user u ON u.id = w.user_id
            LEFT JOIN wish wi ON wi.wishlist_id = w.id
            WHERE w.user_id = ?
            GROUP BY w.id, w.title, u.name
            ORDER BY w.id
            """;
    private static final String SELECT_SHARED_WITH = """
            SELECT w.id, w.title, u.name AS owner_name, COUNT(wi.id) AS wish_count
            FROM wishlist w
            JOIN app_user u ON u.id = w.user_id
            JOIN wishlist_share s ON s.wishlist_id = w.id
            LEFT JOIN wish wi ON wi.wishlist_id = w.id
            WHERE s.user_id = ?
            GROUP BY w.id, w.title, u.name
            ORDER BY w.id
            """;

    /** Turns one row from the wishlist table into a Wishlist. Shared by every query that reads whole wishlists. */
    private static final RowMapper<Wishlist> WISHLIST_MAPPER = (rs, rowNum) -> new Wishlist(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("title"),
            rs.getString("share_token")
    );

    private static final RowMapper<WishlistSummary> SUMMARY_MAPPER = (rs, rowNum) -> new WishlistSummary(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("owner_name"),
            rs.getInt("wish_count")
    );


    /**
     * Saves a new wishlist and returns the id the database gave it.
     *
     * @param userId     id of the user who owns the wishlist; must exist in {@code app_user}
     * @param title      the wishlist's title, already validated by the service
     * @param shareToken the random token used in the share link; must be unique
     * @return the generated id of the new wishlist
     * @throws org.springframework.dao.DataIntegrityViolationException
     *         if the user doesn't exist or the share token is already in use
     * @throws IllegalStateException if the database doesn't return a generated id
     */
    public int save(int userId, String title, String shareToken){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_WISHLIST, new String[] {"id"});
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, shareToken);
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null){
            throw new IllegalStateException("Databasen returnerede intet id for den nye ønskeseddel");
        }
        return id.intValue();
    }

    /**
     * Changes the title of a wishlist, but only if it belongs to the given user.
     *
     * @param userId     id of the user trying to rename the list
     * @param wishlistId id of the wishlist to rename
     * @param title      the new title, already validated by the service
     * @return {@code true} if the title was changed; {@code false} if the wishlist
     *         doesn't exist or belongs to someone else
     */
    public boolean updateTitle(int userId, int wishlistId, String title) {
        return jdbcTemplate.update(UPDATE_TITLE, title, wishlistId, userId) == 1;
    }

    /**
     * Checks whether a wishlist exists.
     *
     * @param wishlistId the wishlist ID
     * @return true if the wishlist exists, otherwise false
     */
    public boolean existsById(int wishlistId) {
        String sql = "SELECT COUNT(*) FROM wishlist WHERE id = ?";

        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, wishlistId
        );
        return count != null && count > 0;
    }

    /**
     * Checks whether the specified wishlist belongs to the given user
     *
     * @param wishlistId the id of the wishlist to check
     * @param userId the id of the user to check ownership for
     * @return {@code true} if the wishlist exists and belongs to the user,
     *         otherwise {@code false}
     */
    public boolean existsByIdAndUserId(int wishlistId, int userId){
        String sql = """
                SELECT COUNT(*)
                FROM wishlist
                WHERE id = ? AND user_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, wishlistId, userId);
        return count !=null && count >0;
    }

    /**
     * Finds a wishlist by its id.
     *
     * @param wishlistId id of the wishlist
     * @return the wishlist, or an empty {@code Optional} if no wishlist has that id
     */
    public Optional<Wishlist> findById(int wishlistId) {
        return jdbcTemplate.query(SELECT_BY_ID, WISHLIST_MAPPER, wishlistId).stream().findFirst();
    }

    /**
     * Checks whether a wishlist has been shared with a user.
     *
     * @param wishlistId id of the wishlist
     * @param userId     id of the user
     * @return {@code true} if the user is on the list's share list
     */
    public boolean isSharedWith(int wishlistId, int userId) {
        Integer count = jdbcTemplate.queryForObject(IS_SHARED_WITH, Integer.class, wishlistId, userId);
        return count != null && count > 0;
    }

    /**
     * Finds the wishlists a user owns, oldest first, for the overview page.
     *
     * @param userId id of the owner
     * @return one summary per list; an empty list if the user has none
     */
    public List<WishlistSummary> findOwnedBy(int userId) {
        return jdbcTemplate.query(SELECT_OWNED_BY, SUMMARY_MAPPER, userId);
    }

    /**
     * Finds the wishlists other users have shared with a user, oldest first.
     *
     * @param userId id of the user the lists are shared with
     * @return one summary per list; an empty list if nothing is shared with the user
     */
    public List<WishlistSummary> findSharedWith(int userId) {
        return jdbcTemplate.query(SELECT_SHARED_WITH, SUMMARY_MAPPER, userId);
    }

    /**
     * Finds the wishlist a share link points to.
     *
     * @param shareToken the token from the share link
     * @return the wishlist, or an empty {@code Optional} if no wishlist has that token
     */
    public Optional<Wishlist> findByShareToken(String shareToken) {
        return jdbcTemplate.query(SELECT_BY_SHARE_TOKEN, WISHLIST_MAPPER, shareToken).stream().findFirst();
    }

    /**
     * Shares a wishlist with a user, so it shows up under "Delt med dig" and they may open it.
     *
     * @param wishlistId id of the wishlist
     * @param userId     id of the user it is shared with
     * @throws org.springframework.dao.DataIntegrityViolationException
     *         if it is already shared with the user, or the list or user doesn't exist
     */
    public void addShare(int wishlistId, int userId) {
        jdbcTemplate.update(INSERT_SHARE, wishlistId, userId);
    }
}
