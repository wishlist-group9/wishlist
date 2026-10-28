# SantaClaude

SantaClaude er en webapplikation til at oprette og organisere sine juleønsker i form af en digital ønskeseddel. 

## Features
- Opret en ønskeseddel
- Tilføj en næsten uendelig mængde juleønsker til din ønskeseddel
- Tilpas dine ønsker, selv de mest flittige julenisser kan lave stavefejl
- Slet dine ønsker, selv de mest dydige julenisser kan trænge til en (træ)skefuld selvindsigt

## Tech
Java 21 · Spring Boot 4 · Thymeleaf · JdbcTemplate · MySQL · H2 (tests) · GitHub Actions · Qodana · Azure App Service

## Installation
Tekniske krav: JDK 21, MySQL lokalt.

1. Klon repositoriet og åben mappen i IntelliJ:
   `git clone https://github.com/wishlist-group9/wishlist.git`
2. Sæt dit MySQL password én gang (i din foretrukne terminal), derefter genstart IntelliJ:
   `setx DEV_PASSWORD "ditkodeord"`
3. Kør `WishlistApplication` → http://localhost:8080 bør vise "Database: OK"

`wishlist` databasen bliver oprettet automatisk ved første kørsel.
Bruger du en anden MySQL bruger eller URL? Indsæt også `DEV_USERNAME` / `DEV_DATABASE_URL`.

Kør tests (intet behov for eksisterende database): `./mvnw clean test`

## Anvendelse
- Lokal: http://localhost:8080
- Live: https://wishlist-group9-gxetc8eph5dma6bd.swedencentral-01.azurewebsites.net/
  (Free tier: det første run kan tage op til ~30 sekunder)

## Vil du bidrage til projektet?
Se [CONTRIBUTING.md](CONTRIBUTING.md).
