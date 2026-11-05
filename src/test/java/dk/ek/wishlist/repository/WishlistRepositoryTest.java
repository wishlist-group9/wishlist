package dk.ek.wishlist.repository;

import dk.ek.wishlist.model.Wishlist;
import dk.ek.wishlist.model.WishlistSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql("/h2init.sql")
public class WishlistRepositoryTest {
    private static final int OWNER_ID = 1;   // "Test Ejer" from h2init.sql
    private static final int UNKNOWN_USER_ID = 999;
    private static final int GUEST_ID = 2;   // "Test Gæst" from h2init.sql

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


    @Test
    void updateTitle_ownList_changesTitle() {
        int id = repository.save(1, "Jul 2026", "token-1");

        boolean changed = repository.updateTitle(1, id, "Fødselsdag");

        assertTrue(changed);
        assertEquals("Fødselsdag",
                jdbcTemplate.queryForObject("SELECT title FROM wishlist WHERE id = ?", String.class, id));
    }

    @Test
    void updateTitle_someoneElsesList_returnsFalseAndKeepsTitle() {
        int id = repository.save(1, "Jul 2026", "token-1");

        boolean changed = repository.updateTitle(2, id, "Hacket");

        assertFalse(changed);
        assertEquals("Jul 2026",
                jdbcTemplate.queryForObject("SELECT title FROM wishlist WHERE id = ?", String.class, id));
    }

    @Test
    void updateTitle_unknownList_returnsFalse() {
        assertFalse(repository.updateTitle(1, 999, "Fødselsdag"));
    }

    @Test
    void findById_existingList_returnsAllValues() {
        int id = repository.save(OWNER_ID, "Jul 2026", "token-1");

        Wishlist wishlist = repository.findById(id).orElseThrow();

        assertEquals(id, wishlist.getId());
        assertEquals(OWNER_ID, wishlist.getUserId());
        assertEquals("Jul 2026", wishlist.getTitle());
        assertEquals("token-1", wishlist.getShareToken());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repository.findById(999).isEmpty());
    }

    @Test
    void isSharedWith_sharedUser_returnsTrue() {
        int id = repository.save(OWNER_ID, "Jul 2026", "token-1");
        jdbcTemplate.update("INSERT INTO wishlist_share (wishlist_id, user_id) VALUES (?, ?)", id, GUEST_ID);

        assertTrue(repository.isSharedWith(id, GUEST_ID));
    }

    @Test
    void isSharedWith_notShared_returnsFalse() {
        int id = repository.save(OWNER_ID, "Jul 2026", "token-1");

        assertFalse(repository.isSharedWith(id, GUEST_ID));
    }

    @Test
    void findOwnedBy_returnsOnlyOwnListsWithOwnerNameAndWishCount() {
        int jul = repository.save(OWNER_ID, "Jul 2026", "token-1");
        int empty = repository.save(OWNER_ID, "Tom liste", "token-2");
        repository.save(GUEST_ID, "Gæstens liste", "token-3");
        jdbcTemplate.update("INSERT INTO wish (wishlist_id, title) VALUES (?, ?)", jul, "Bog");
        jdbcTemplate.update("INSERT INTO wish (wishlist_id, title) VALUES (?, ?)", jul, "Hue");

        List<WishlistSummary> lists = repository.findOwnedBy(OWNER_ID);

        assertEquals(2, lists.size());
        assertEquals(jul, lists.get(0).getId());
        assertEquals("Jul 2026", lists.get(0).getTitle());
        assertEquals("Test Ejer", lists.get(0).getOwnerName());
        assertEquals(2, lists.get(0).getWishCount());
        assertEquals(empty, lists.get(1).getId());
        assertEquals(0, lists.get(1).getWishCount(), "a list without wishes must still be returned, with 0");
    }

    @Test
    void findOwnedBy_noLists_returnsEmptyList() {
        assertTrue(repository.findOwnedBy(GUEST_ID).isEmpty());
    }

    @Test
    void findSharedWith_returnsOnlyListsSharedWithTheUser() {
        int shared = repository.save(OWNER_ID, "Jul 2026", "token-1");
        repository.save(OWNER_ID, "Ikke delt", "token-2");
        jdbcTemplate.update("INSERT INTO wishlist_share (wishlist_id, user_id) VALUES (?, ?)", shared, GUEST_ID);

        List<WishlistSummary> lists = repository.findSharedWith(GUEST_ID);

        assertEquals(1, lists.size());
        assertEquals(shared, lists.get(0).getId());
        assertEquals("Test Ejer", lists.get(0).getOwnerName());
        assertEquals(0, lists.get(0).getWishCount());
    }

    @Test
    void findSharedWith_ownListsAreNotIncluded() {
        repository.save(OWNER_ID, "Jul 2026", "token-1");

        assertTrue(repository.findSharedWith(OWNER_ID).isEmpty());
    }
}
