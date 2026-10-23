package dk.ek.wishlist.service;

import dk.ek.wishlist.repository.HealthRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class HealthService {
    private final HealthRepository repository;

    public HealthService(HealthRepository repository) {
        this.repository = repository;
    }

    public String databaseStatus() {
        try {
            return repository.isDatabaseUp() ? "OK" : "ERROR";
        } catch (DataAccessException ex) {
            return "ERROR";
        }
    }
}