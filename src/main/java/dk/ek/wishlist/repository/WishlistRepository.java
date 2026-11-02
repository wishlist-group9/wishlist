package dk.ek.wishlist.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

/** Reads and writes the wishlist table **/

@Repository
public class WishlistRepository {
    private final JdbcTemplate jdbcTemplate;

    public WishlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT_WISHLIST = "INSERT INTO wishlist (user_id, title, share_token) VALUES (?,?,?)";

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
}
