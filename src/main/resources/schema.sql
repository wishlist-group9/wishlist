-- Santa Claude database schema (MySQL). See docs/database.md.
-- Runs on every start in the dev profile, so every statement must be safe to run twice.

CREATE TABLE IF NOT EXISTS app_user (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS wishlist (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL,
    title       VARCHAR(100) NOT NULL,
    share_token VARCHAR(36)  NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wish (
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

CREATE TABLE IF NOT EXISTS wishlist_share (
    wishlist_id INT NOT NULL,
    user_id     INT NOT NULL,
    PRIMARY KEY (wishlist_id, user_id),
    FOREIGN KEY (wishlist_id) REFERENCES wishlist(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);
