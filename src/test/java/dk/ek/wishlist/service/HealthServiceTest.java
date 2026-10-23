package dk.ek.wishlist.service;

import dk.ek.wishlist.repository.HealthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {
    @Mock
    HealthRepository repository;
    @InjectMocks
    HealthService service;

    @Test
    void databaseStatus_dbUp_returnsOk() {
        when(repository.isDatabaseUp()).thenReturn(true);
        assertEquals("OK", service.databaseStatus());
    }

    @Test
    void databaseStatus_dbError_returnsError() {
        when(repository.isDatabaseUp())
                .thenThrow(new DataAccessResourceFailureException("down"));
        assertEquals("ERROR", service.databaseStatus());
    }
}