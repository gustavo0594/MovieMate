# Movie Recommendation App — Product & Architecture Specification

## 1. Product Overview

**Working concept:** A personal assistant for deciding what movie to watch.

The app is a local-first Android application focused exclusively on movies. It allows users to discover movies, maintain a personal movie library, and receive personalized recommendations generated with **ML Kit Prompt API**.

The key differentiator is not simply tracking movies. The app learns from the user's local movie history and asks the on-device AI to select recommendations from a known local catalog.

> **TMDB provides movie knowledge and discovery. The local database provides the user's movie context and candidate catalog. ML Kit Prompt API provides the recommendation intelligence.**

The initial version will not require accounts or a backend.

---

## 2. Goals

### Primary goals

- Discover movies.
- Search for movies.
- View detailed movie information.
- Save favorite movies.
- Mark movies as watched.
- Optionally rate watched movies.
- Maintain a watchlist.
- Watch movie trailers.
- Build a local movie catalog.
- Generate personalized recommendations using ML Kit Prompt API.
- Explain why a movie was recommended when possible.
- Work without a user account.
- Keep user-specific data locally on the device.

### Non-goals for the MVP

- User accounts.
- Cloud synchronization.
- TV shows.
- Streaming availability.
- Social features.
- Public profiles.
- A custom backend.
- Server-side ML recommendations.

---

## 3. External API

### TMDB — The Movie Database

The application will use **TMDB API v3** as its primary external movie-data provider.

Official documentation: https://developer.themoviedb.org/

TMDB will provide:

- Movie ID
- Title
- Overview
- Poster
- Backdrop
- Release date
- Genres
- Runtime
- Vote average
- Popularity
- Cast
- Crew/directors
- Videos/trailers
- Similar movies
- Discoverable movie catalogs

### Main API capabilities

| Capability | Purpose |
|---|---|
| Discover Movies | Build and refresh candidate movie catalogs |
| Popular Movies | Home discovery |
| Top Rated Movies | Home discovery |
| Now Playing | Current movies |
| Upcoming Movies | Future releases |
| Search Movies | Search feature |
| Movie Details | Movie detail screen |
| Credits | Cast and crew information |
| Videos | Movie trailers |
| Similar Movies | Additional discovery candidates |

TMDB is the movie information and discovery source, **not the recommendation engine**.

### API authentication

The application uses a TMDB API Read Access Token.

**Security requirement:** the token supplied during project planning must NOT be committed to source control or included in production documentation. Store it securely through local development configuration and an appropriate release strategy.

If a real active credential was shared during project setup, it should be considered exposed and **rotated/revoked before production use**.

---

## 4. Local-First Architecture

The application will be **100% local for user data**.

There will be no login and no user account in the MVP.

```text
                         TMDB API
                            |
                            | Movie metadata
                            v
                 +-----------------------+
                 |     Remote Data       |
                 |       Source          |
                 +-----------+-----------+
                             |
                             v
                 +-----------------------+
                 |    Local Movie DB     |
                 |        (Room)         |
                 +-----------+-----------+
                             |
             +---------------+---------------+
             |               |               |
             v               v               v
         Favorites        Watched        Watchlist
                             |
                       Personal ratings
                             |
                             v
                 +-----------------------+
                 | Candidate Movie Set    |
                 +-----------+-----------+
                             |
                             v
                 +-----------------------+
                 | ML Kit Prompt API      |
                 |  On-device AI          |
                 +-----------+-----------+
                             |
                             v
                 +-----------------------+
                 | Personalized Movie     |
                 | Recommendations        |
                 +-----------------------+
```

---

## 5. Navigation Structure

Primary bottom navigation:

```text
+------------------------------------------------+
|                                                |
|                    CONTENT                     |
|                                                |
+------------------------------------------------+
|  Home  | Watchlist | Favorites | Watched       |
+------------------------------------------------+
```

### Primary destinations

1. Home
2. Watchlist
3. Favorites
4. Watched

### Secondary destinations

- Search
- Movie Details
- AI Recommendation / Movie Assistant
- Trailer player

---

## 6. Home Screen

The Home screen is the main discovery and recommendation surface.

### Personal recommendation

The most important section should answer:

> **What should I watch tonight?**

with a primary action:

```text
[ Surprise Me ]
```

The app can then show a recommendation selected by ML Kit Prompt API from the local candidate catalog.

### Discovery sections

Potential TMDB-powered sections:

- Picked for You
- Popular Movies
- Top Rated
- Now Playing
- Upcoming

The number of sections should remain limited so Home does not feel like a generic movie database.

---

## 7. Search

Users can search TMDB for movies.

```text
Search
  |
  v
TMDB Search API
  |
  v
Search results
  |
  v
Movie Details
```

From search results, users can:

- Open movie details.
- Add to favorites.
- Add to watchlist.
- Mark as watched.
- Rate a watched movie.

When a movie is interacted with, its relevant TMDB metadata should be persisted locally.

---

## 8. Movie Details

The Movie Details screen should contain the information needed to decide whether to watch a movie.

### Information

- Backdrop
- Poster
- Title
- Release date
- Runtime
- TMDB rating
- Overview
- Genres
- Cast
- Director/crew
- Trailer
- Similar movies

### User actions

```text
[ Favorite ]
[ Watchlist ]
[ Mark as Watched ]
[ Watch Trailer ]
```

If the movie has already been watched, show the user's optional personal rating.

---

## 9. Favorites

Favorites represent movies the user explicitly considers favorites.

Favorite, watched, and watchlist states are **not mutually exclusive**.

Example:

```text
Interstellar

Favorite: YES
Watched: YES
Rating: 5/5
Watchlist: NO
```

Favorites are an important signal for recommendations.

---

## 10. Watched Movies

Users can mark movies as watched.

When marking a movie as watched, the app can optionally ask:

```text
How did you like it?

☆ ☆ ☆ ☆ ☆
```

The watched record should preserve:

- Movie ID
- Watched status
- Date watched
- Optional personal rating

Watched movies are one of the most important data sources for personalized recommendations.

---

## 11. Watchlist

The watchlist contains movies the user wants to watch.

- **Favorite:** I really like this movie.
- **Watched:** I have already watched this movie.
- **Watchlist:** I want to watch this movie.

A movie may belong to more than one category.

The recommendation engine can use the watchlist as an additional signal.

---

## 12. Personal Ratings

Ratings are optional.

Recommended scale:

```text
1 ★
2 ★★
3 ★★★
4 ★★★★
5 ★★★★★
```

The rating is a **personal user rating**, separate from TMDB's rating.

```text
TMDB rating:
8.4 / 10

User rating:
★★★★★
```

Personal ratings should receive greater importance than external ratings when identifying the user's preferences.

---

## 13. Recommendation System

This is the core feature of the application.

The app should **not** simply ask the AI:

> "Recommend me a movie."

Instead, ML Kit Prompt API receives a controlled set of candidate movies that actually exist in the local database.

### Core rule

> **ML Kit Prompt API chooses from known candidate movies. It should not invent movie titles or IDs.**

```text
                    Local Database
                         |
              +----------+----------+
              |                     |
              v                     v
        User Movie Data       Candidate Movies
              |                     |
              +----------+----------+
                         |
                         v
                 ML Kit Prompt API
                         |
                         v
                  Recommendation
                         |
                         v
                   Movie ID
                    + reason
```

The recommendation request should use signals such as:

- Watched movies
- Personal ratings
- Favorites
- Watchlist
- Movie genres
- Directors
- Cast
- Release year
- Runtime
- TMDB rating
- Candidate movie metadata
- Optional user request, such as "something funny tonight"

---

## 14. Candidate Movie Catalog

The local database should **not** attempt to contain every movie in TMDB.

It should contain:

1. Movies the user has interacted with.
2. Movies discovered through Home.
3. Movies fetched through search.
4. Movies explicitly added to the watchlist or favorites.
5. A controlled set of additional candidate movies obtained from TMDB.

Conceptually:

```text
Local Candidate Catalog

500 - 2,000 movies
        |
        +-- User history
        +-- Popular
        +-- Top rated
        +-- Trending/discovery
        +-- Genre discovery
        +-- Similar movies
```

The exact size should be determined during implementation and testing based on Prompt API context limits and performance.

---

## 15. Surprise Me

This should be one of the main features of the application.

The user selects:

```text
Surprise Me
```

The application prepares:

- User's watched movies
- User ratings
- Favorites
- Watchlist
- Candidate movies
- Relevant movie metadata

Then ML Kit Prompt API selects one or more candidates.

Example output:

```text
Your pick

Blade Runner 2049

94% Match

Why this movie?

You rated Interstellar and Arrival highly,
and you appear to enjoy science-fiction movies
with strong visual storytelling.
```

The exact structured output will be defined during implementation.

---

## 16. Explainable Recommendations

Recommendations should ideally include an explanation.

Example:

```text
Why this movie?

You rated these movies highly:

- Interstellar
- Arrival
- The Martian

You seem to enjoy:

- Science fiction
- Strong visual storytelling
- Serious tone
```

The explanation should be generated by ML Kit Prompt API when feasible.

---

## 17. AI Movie Assistant

A future feature can extend recommendations into a conversational assistant.

Example:

```text
User:
I want something like Interstellar,
but shorter and more action-oriented.

Assistant:
Based on your movie history, I recommend:

Dune

Why:
You rated Interstellar highly and tend to enjoy
science-fiction movies with strong world building.
```

Another example:

```text
User:
I only have two hours tonight.

Assistant:
Here are three movies from your local catalog...
```

This should be implemented after the basic recommendation flow is stable.

---

## 18. Movie Taste Profile

A future feature can summarize the user's movie preferences.

```text
Your Movie Taste

Top genres

Sci-Fi       ██████████
Thriller     ████████
Drama        ██████
Adventure    █████

Average personal rating

★★★★☆ 4.2

Movies watched
47

Favorites
12
```

This profile should preferably be derived from local movie history rather than stored as static preferences.

---

## 19. Trailer Feature

Movie Details should expose available trailers through TMDB video information.

```text
[ ▶ Watch Trailer ]
```

Trailer playback implementation can be decided later based on the video source returned by TMDB.

---

## 20. Future Feature: Streaming Availability

Not part of the MVP.

A future version may show where a movie is available to watch, potentially using another API and country-specific availability data.

---

## 21. Future Feature: TV Shows

Not part of the MVP. The application will initially focus exclusively on movies.

---

## 22. Data Model — Conceptual

The exact Room schema will be designed during implementation.

### Movie

```text
Movie
-------------------------
tmdbId
title
overview
posterPath
backdropPath
releaseDate
runtime
voteAverage
popularity
genres
cast
directors
trailer
```

### UserMovie

```text
UserMovie
-------------------------
movieId
isFavorite
isWatched
isInWatchlist
personalRating
watchedAt
```

Additional entities may be introduced for normalization, history, recommendations, and efficient queries.

---

## 23. Recommendation Data Flow

```text
User watches Interstellar
        |
        v
User gives 5 stars
        |
        v
Saved in Room
        |
        v
User watches Arrival
        |
        v
User gives 4 stars
        |
        v
More user signals
        |
        v
Candidate catalog refreshed
        |
        v
User selects "Surprise Me"
        |
        v
Application builds AI context
        |
        v
ML Kit Prompt API
        |
        v
AI selects candidate movie
        |
        v
Recommendation displayed
        |
        v
User watches it
        |
        v
New signal is stored
```

This feedback loop is the core of the product.

---

## 24. Architecture Direction

The application should use a modern Android architecture.

Suggested structure:

```text
app
|
+-- data
|   +-- remote
|   |   +-- TmdbApi
|   |   +-- TmdbDto
|   |
|   +-- local
|   |   +-- Room Database
|   |   +-- DAOs
|   |   +-- Entities
|   |
|   +-- repository
|
+-- domain
|   +-- models
|   +-- repositories
|   +-- usecases
|
+-- presentation
    +-- home
    +-- search
    +-- movie_detail
    +-- favorites
    +-- watched
    +-- watchlist
    +-- recommendations
```

Recommended Android technologies:

- Kotlin
- Jetpack Compose
- ViewModel
- Kotlin Coroutines
- Flow
- Room
- Retrofit
- Kotlin Serialization
- Paging 3
- Coil
- ML Kit Prompt API

Dependency versions should be selected when implementation begins.

---

## 25. MVP Feature Priorities

### P0 — Essential

- Home
- Search
- Movie Details
- TMDB integration
- Local Room database
- Favorites
- Watched
- Watchlist
- Personal ratings
- Candidate movie catalog
- ML Kit Prompt API recommendations

### P1 — Important

- Surprise Me
- Recommendation explanations
- Trailers
- Similar movies
- Candidate catalog refresh strategy

### P2 — Future

- Conversational Movie Assistant
- Movie Taste Profile
- Streaming availability
- More advanced recommendation controls

---

## 26. Product Identity

The app should not feel like a generic TMDB client.

Its positioning should be:

> **A personal assistant for deciding what movie to watch.**

TMDB is the data source.

The local database is the user's memory.

ML Kit Prompt API is the recommendation intelligence.

```text
              TMDB
               |
          Movie knowledge
               |
               v
         Local Database
               |
        User's movie life
               |
               v
       ML Kit Prompt API
               |
               v
       Personal Assistant
               |
               v
      "What should I watch?"
```

---

## 27. Key Product Principle

The application should continuously improve its recommendations as the user interacts with it.

More interactions produce more signals:

```text
Watch
  +
Rate
  +
Favorite
  +
Watchlist
  +
Search
  +
Discover
  ↓
Better understanding of preferences
  ↓
Better candidate selection
  ↓
Better recommendations
```

The long-term goal is for the user to feel:

> **"This app knows what kind of movies I like."**

---

## 28. Security Note

Never commit API credentials to Git.

If a real TMDB access token was shared during project planning, treat it as exposed and rotate/revoke it before production use.

For development, use local configuration such as `local.properties` or environment-specific configuration. Do not place the token in this product specification or source code.

---

## 29. Current Product Definition

| Item | Decision |
|---|---|
| App type | Local-first Android movie recommendation app |
| Primary purpose | Help users decide what movie to watch |
| Data source | TMDB API |
| Local persistence | Room |
| AI | ML Kit Prompt API |
| Authentication | None |
| Content | Movies only |
| User data | Local device only |
| Recommendation source | Local candidate catalog |
| Recommendation engine | ML Kit Prompt API |
| Favorite | Yes |
| Watched | Yes |
| Watchlist | Yes |
| Personal rating | Optional |
| Trailers | Yes |
| Streaming availability | Future |
| TV shows | Future |

## Core user loop

```text
Discover
   ↓
Save / Watch / Rate
   ↓
Build movie history
   ↓
Ask AI
   ↓
Get personalized recommendation
   ↓
Watch movie
   ↓
Rate / save
   ↓
Improve future recommendations
```

The ultimate product goal is:

> **A movie assistant that learns what you like and helps you decide what to watch next.**
