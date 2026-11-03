package dk.ek.wishlist.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserRepositoryTest {
    @Autowired
    UserRepository repository;

    @Test
    void save_thenFindByEmail_returnsTheUser() {
        repository.save("Anna", "anna@mail.dk", "hash");

        var found = repository.findByEmail("anna@mail.dk");

        assertTrue(found.isPresent());
        assertEquals("Anna", found.get().getName());
        assertEquals("hash", found.get().getPasswordHash());
    }

    @Test
    void findByEmail_unknownEmail_returnsEmpty() {
        assertTrue(repository.findByEmail("ingen@mail.dk").isEmpty());
    }

    @Test
    void save_duplicateEmail_throws() {
        repository.save("Anna", "dup@mail.dk", "hash");

        assertThrows(DataIntegrityViolationException.class,
                () -> repository.save("Anna 2", "dup@mail.dk", "hash2"));
    }
}