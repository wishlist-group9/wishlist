package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Reads users from the app_user table. */
@Repository
public class UserRepository {

    //JdbcTemplate runs the SQL statements against the database
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Turns one row from the user table into a User object
    private final RowMapper<User> userRowMapper = (rs, rowNum) ->
            new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("password_hash"));


    /**
     * Finds the user with the given e-mail.
     *
     * @param email the e-mail to look for; must match exactly
     * @return the user, or {@code null} if no user has that e-mail
     */
    public User findByEmail(String email) {
        String sql = "SELECT id, name, email, password_hash FROM app_user WHERE email = ?";
        List<User> result = jdbcTemplate.query(sql, userRowMapper, email);
        if (result.isEmpty()) {
            return null;
        }
        User user = result.get(0);
        return user;
    }

}
