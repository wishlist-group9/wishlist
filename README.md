# Wishlist

A web app for creating and sharing wish lists. Miniprojekt, EK Datamatiker 2. semester.

## Features
- Health check: the front page shows whether the database is reachable
- (fill in as user stories are completed)

## Tech
Java 21 · Spring Boot 4 · Thymeleaf · JdbcTemplate · MySQL · H2 (tests) · GitHub Actions · Qodana · Azure App Service

## Installation
Requirements: JDK 21, MySQL running locally.

1. Clone the repo and open the folder in IntelliJ:
   `git clone https://github.com/wishlist-group9/wishlist.git`
2. Set your MySQL password once (Windows terminal), then restart IntelliJ:
   `setx DEV_PASSWORD "yourpassword"`
3. Run `WishlistApplication` → http://localhost:8080 should say "Database: OK"

The `wishlist` database is created automatically on first run.
Using a different MySQL user or URL? Also set `DEV_USERNAME` / `DEV_DATABASE_URL`.

Run tests (no database needed): `./mvnw clean test`

## Usage
- Local: http://localhost:8080
- Live: https://wishlist-group9-gxetc8eph5dma6bd.swedencentral-01.azurewebsites.net/
  (Free tier: the first load after idle can take ~30 seconds)

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).