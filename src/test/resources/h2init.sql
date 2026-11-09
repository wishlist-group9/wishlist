-- Test database for H2. Runs before each test class/method that has @Sql("/h2init.sql").
-- Drops everything first, so every test starts from the same clean state.
-- Keep the CREATE TABLEs identical to src/main/resources/schema.sql.

DROP TABLE IF EXISTS wishlist_share;
DROP TABLE IF EXISTS wish;
DROP TABLE IF EXISTS wishlist;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL
);

CREATE TABLE wishlist (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    title       VARCHAR(100) NOT NULL,
    share_token VARCHAR(36)  NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE wish (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    wishlist_id INT           NOT NULL,
    title       VARCHAR(150)  NOT NULL,
    description VARCHAR(500)  NULL,
    link        VARCHAR(500)  NULL,
    price       DECIMAL(10,2) NULL,
    category    VARCHAR(10)   NOT NULL DEFAULT 'play',
    reserved_by INT           NULL,
    FOREIGN KEY (wishlist_id) REFERENCES wishlist(id) ON DELETE CASCADE,
    FOREIGN KEY (reserved_by) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE TABLE wishlist_share (
    wishlist_id INT NOT NULL,
    user_id     INT NOT NULL,
    PRIMARY KEY (wishlist_id, user_id),
    FOREIGN KEY (wishlist_id) REFERENCES wishlist(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Test users: 1 owns lists, 2 is a guest
INSERT INTO app_user (id, name, email, password_hash) VALUES
    (1, 'Test Ejer',  'ejer@test.dk',  'hash'),
    (2, 'Test Gæst',  'gaest@test.dk', 'hash');
