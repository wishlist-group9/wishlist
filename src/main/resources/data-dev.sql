-- Demo data for local development only (dev profile). Never runs on Azure.
-- INSERT IGNORE skips rows that already exist, so restarting the app is safe.

-- OBS! Passwordet til demo@santaclaude.dk er "julemand123", men det skulle hashes for at virke og blev til følgende
-- hash:
-- $2a$10$rZ0479o3gm2XLMhfPqgp6eRKRr5u8bBxoY8Z73ZLOIEHH6Dcjwck6 hvilket er bedre end 'not-a-real-hash' da det ikke
-- matcher et BCrypt-pattern by default nogensinde.

INSERT IGNORE INTO app_user (id, name, email, password_hash)
VALUES (1, 'Demo Nisse', 'demo@santaclaude.dk', '$2a$10$rZ0479o3gm2XLMhfPqgp6eRKRr5u8bBxoY8Z73ZLOIEHH6Dcjwck6');
