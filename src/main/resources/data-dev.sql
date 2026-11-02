-- Demo data for local development only (dev profile). Never runs on Azure.
-- INSERT IGNORE skips rows that already exist, so restarting the app is safe.

INSERT IGNORE INTO app_user (id, name, email, password_hash)
VALUES (1, 'Demo Nisse', 'demo@santaclaude.dk', 'not-a-real-hash');
