package dk.ek.wishlist.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/h2init.sql")
public class WishlistRepositoryTest {
    private static final int OWNER_ID = 1;   // "Test Ejer" from h2init.sql
    private static final int UNKNOWN_USER_ID = 999;

    @Autowired
    WishlistRepository repository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void save_returnsNewId(){
        int id = repository.save(OWNER_ID, "Jul 2026", "token-1");

        assertTrue(id > 0, "save should return the id the database generated");
    }

    @Test
    void save_storesTheValues() {
        int id = repository.save(OWNER_ID, "Jul 2026", "token-1");

        String title = jdbcTemplate.queryForObject(
                "SELECT title FROM wishlist WHERE id = ?", String.class, id);
        Integer userId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM wishlist WHERE id = ?", Integer.class, id);
        String shareToken = jdbcTemplate.queryForObject(
                "SELECT share_token FROM wishlist WHERE id = ?", String.class, id);

        assertEquals("Jul 2026", title);
        assertEquals(OWNER_ID, userId);
        assertEquals("token-1", shareToken);
    }

    @Test
    void save_twoLists_getDifferentIds() {
        int first = repository.save(OWNER_ID, "Jul 2026", "token-1");
        int second = repository.save(OWNER_ID, "Fødselsdag", "token-2");

        assertNotEquals(first, second);
    }

    @Test
    void save_unknownUser_throws() {
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save(UNKNOWN_USER_ID, "Jul 2026", "token-1"));
    }


}
