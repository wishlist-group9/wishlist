package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

/** Reads users from the app_user table. */
@Repository
public class UserRepository {

    //JdbcTemplate runs the SQL statements against the database
    private final JdbcTemplate jdbcTemplate;


    // Constructor injection - Spring builds a JdbcTemplate from the database settings in
    // application.properties, and passes it in here

    /**
     * Creates the repository.
     *
     * @param jdbcTemplate Spring's helper for running SQL
     */
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Turns one row from the user table into a User object
    private final RowMapper<User> userRowMapper = (rs, rowNum) ->
            new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("password_hash"));
    // The SQL in stored in constants, so it is written once and easy to find
    // We use placeholders (?), to stop users from sneaking in their own SQL (SQL injection)
    private static final String INSERT_USER =
            "INSERT INTO app_user (name, email, password_hash) VALUES (?,?,?)";
    private static final String SELECT_BY_EMAIL =
            "SELECT id, name, email, password_hash FROM app_user WHERE email = ?";


    // A row mapper turns ONE row from the database into ONE java object
    // The name in quotes must match the column names in schema.sql exactly
    // It is a constant because it never changes, so we create it only once
    private static final RowMapper<User> USER_MAPPER = (rs, rowNum) -> new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash"));


    // Saves a new user


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

    public void save(String name, String email, String passwordHash) {
        jdbcTemplate.update(INSERT_USER, name, email, passwordHash);
    }


    // Finds a user by email

    // query() runs a SELECT and returns a List<User>, one user per row found
    // USER_MAPPER is what turns each database row into a User object
    // Email is UNIQUE, so the list has either 0 or 1 users
    // The methods takes the first user if there is one
    // The return type and result is an Optional: A box that either contains a User or is empty
    // This way the caller must deal with "no such user", and nothing crashes

    /**
     * Finds a user by email.
     *
     * @param email the email to look up
     * @return the user, or empty if no user has that email
     */
    public Optional<User> findByEmailNick(String email) {
        return jdbcTemplate.query(SELECT_BY_EMAIL, USER_MAPPER, email)
                .stream()
                .findFirst();
    }

}
