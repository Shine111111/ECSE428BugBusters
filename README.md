# ECSE428BugBusters

## Trending movie backend (SID0027)

`GET /movies/trending` returns a `movies` array with each movie's ID, title, rating,
number of votes, comment count, view count, and trending score. An empty result
includes `"message": "No trending movies available"`; otherwise `message` is null.

Eligible movies have at least 30,000 votes and all four popularity signals present
and nonnegative. The score is `rating * 10 + votes / 10000 + comments / 100 + views / 1000`,
using decimal arithmetic. Results are ordered by score descending, votes descending,
title ascending (ignoring case), then movie ID ascending. Each request reads current
catalog data and recalculates the scores.

Production uses `spring.jpa.hibernate.ddl-auto=none`. Before deploying these changes,
apply [the SQL migration](backend/src/main/resources/db/manual/ID027_trending_movie_signals.sql)
to add nullable `comment_count` and `view_count` columns to `movies`. This is a manual
migration; the application does not run it automatically. Populate these columns from
the catalog's authoritative activity data. Unknown counts remain NULL and exclude a
movie from trending; use 0 only when the count is known to be zero. This functionality
reads those counts; recording view events or synchronizing review activity is separate.

Run the focused calculation, service, database retrieval, and API tests with JDK 21:

```shell
./gradlew :backend:trendingTest --console=plain
```

In Windows PowerShell, use `.\gradlew.bat :backend:trendingTest --console=plain`.
Run all JUnit tests with `.\gradlew.bat :backend:unitTest --console=plain`.
These tasks select the JUnit Jupiter engine so Cucumber scenarios are not discovered.
Their reports are in `backend/build/reports/tests/trendingTest/index.html` and
`backend/build/reports/tests/unitTest/index.html`, respectively.

The existing `test` task includes Cucumber acceptance scenarios. The SID0027 feature
file still requires Cucumber step definitions; the six backend tasks above are covered
by JUnit tests, including database retrieval and API response tests.
