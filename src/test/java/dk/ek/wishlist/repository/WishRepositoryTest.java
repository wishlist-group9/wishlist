package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wish;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests wish persistence using an H2 test database
 */
@SpringBootTest
@ActiveProfiles("test")
@Sql("/h2init.sql")
class WishRepositoryTest {

    @Autowired
    private WishRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        //Creates two wishlists belonging to the test user
        jdbcTemplate.update("""
                INSERT INTO wishlist (id, user_id, title, share_token)
                VALUES (?, ?, ?, ?)
                """, 1, 1, "Jul", "test-token-1");

        jdbcTemplate.update("""
                INSERT INTO wishlist (id, user_id, title, share_token)
                VALUES (?, ?, ?, ?)
                """, 2, 1, "Fødselsdag", "test-token-2");
    }

    @Test
    void save_validWish_storesValuesOnCorrectWishlist() {
        //Arrange: Create a wish for the second wishlist
        Wish wish = new Wish(
                0, 2, "En bog", "En fantasybog",
                "https://example.com", 149.95
        );

        //Act: Save the wish using the real repository
        repository.save(wish);

        //Assert: Read the stored values directly from the database
        Map<String, Object> row = jdbcTemplate.queryForMap("""
                SELECT wishlist_id, title, description, link, price
                FROM wish
                """);

        assertEquals(2, ((Number) row.get("wishlist_id")).intValue());
        assertEquals("En bog", row.get("title"));
        assertEquals("En fantasybog", row.get("description"));
        assertEquals("https://example.com", row.get("link"));
        assertEquals(
                149.95,
                ((Number) row.get("price")).doubleValue(),
                0.001
        );

        //Check that nothing was saved on the first wishlist
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wish WHERE wishlist_id = 1",
                Integer.class
        );

        assertEquals(Integer.valueOf(0), count);
    }

    @Test
    void save_validWish_generatesId() {
        Wish wish = new Wish(0, 1, "En bog", null, null, null);

        repository.save(wish);

        //The generated ID is stored in the database
        Integer id = jdbcTemplate.queryForObject(
                "SELECT id FROM wish",
                Integer.class
        );

        assertNotNull(id);
        assertTrue(id > 0);
    }

    @Test
    void save_nullPrice_storesNull() {
        Wish wish = new Wish(0, 1, "En bog", null, null, null);

        repository.save(wish);

        //Check that the price is NULL when no price is provided
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT price FROM wish"
        );

        assertNull(row.get("price"));
    }

    @Test
    void save_unknownWishlist_throwsAndSavesNothing() {
        //Arrange: Wishlist 999 does not exist
        Wish wish = new Wish(0, 999, "En bog", null, null, null);

        //Act and assert: The foreign key rejects the wish
        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.save(wish)
        );

        //Check that no wish was saved
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wish",
                Integer.class
        );

        assertEquals(Integer.valueOf(0), count);
    }

    @Test
    void delete_existingWish_removesOnlyThatWish() {
        //Arrange: Save two wishes on wishlist 1
        repository.save(new Wish(0, 1, "En bog", null, null, null));
        repository.save(new Wish(0, 1, "En trøje", null, null, null));
        Integer bookId = jdbcTemplate.queryForObject(
                "SELECT id FROM wish WHERE title = 'En bog'", Integer.class);
        assertNotNull(bookId);

        //Act: Delete the book
        int deletedRows = repository.delete(bookId, 1);

        //Assert: One row was deleted, and the other wish is still there
        assertEquals(1, deletedRows);

        Integer bookCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wish WHERE title = 'En bog'", Integer.class);
        Integer shirtCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wish WHERE title = 'En trøje'", Integer.class);

        assertEquals(Integer.valueOf(0), bookCount);
        assertEquals(Integer.valueOf(1), shirtCount);
    }

    @Test
    void delete_wishOnOtherWishlist_deletesNothing() {
        //Arrange: The wish is on wishlist 1
        repository.save(new Wish(0, 1, "En bog", null, null, null));
        Integer wishId = jdbcTemplate.queryForObject(
                "SELECT id FROM wish", Integer.class);
        assertNotNull(wishId);

        //Act: Try to delete it through wishlist 2
        int deletedRows = repository.delete(wishId, 2);

        //Assert: Nothing was deleted, and the wish is still on wishlist 1
        assertEquals(0, deletedRows);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wish WHERE wishlist_id = 1", Integer.class);
        assertEquals(Integer.valueOf(1), count);
    }

    @Test
    void delete_unknownWish_returnsZero() {
        //Act and assert: No wish with id 999 exists
        assertEquals(0, repository.delete(999, 1));
    }
}