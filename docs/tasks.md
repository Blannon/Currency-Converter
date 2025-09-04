# Improvement Tasks Checklist

Enumerated, actionable improvements for the CurrencyConveter project. Check off each item when complete.

1. [ ] Establish coding standards and linters
   - [ ] Add Kotlin code style config (ktlint or Ktlint Gradle plugin) and format the project
   - [ ] Enable Android Lint with failOnWarning in CI and address current warnings
   - [ ] Add detekt with a sensible baseline and rules tuned for this codebase

2. [ ] Strengthen project structure and modularity
   - [ ] Extract networking (Ktor client, API DTOs) into a separate data.network package
   - [ ] Introduce a mapper layer (data -> domain) for API models, avoid leaking transport models
   - [ ] Consider a separate module for domain (Clean Architecture), if app size justifies: :domain, :data, :app

3. [ ] Improve dependency injection
   - [ ] Ensure all dependencies are provided via DI (HttpClient, repositories, use cases)
   - [ ] Centralize HttpClient configuration (timeouts, logging, JSON config)
   - [ ] Replace direct use of BuildConfig.API_KEY inside repository with an injected ApiConfig

4. [ ] Robust error handling and resiliency
   - [ ] Wrap network calls with Result/Either to convey errors without throwing
   - [ ] Implement retry/backoff for transient HTTP/network errors
   - [ ] Map backend errors to user-friendly messages in ViewModel layer
   - [ ] Add offline handling and caching of latest successful conversion rates

5. [ ] Data and domain improvements
   - [ ] Replace the hard-coded currency catalog with a remotely fetched list and local cache
   - [ ] Validate currency codes against ISO 4217 and constrain input via typesafe enums/value classes
   - [ ] Introduce a RatesRepository abstraction for fetching and caching rate tables

6. [ ] Presentation layer enhancements
   - [ ] Tighten input validation for amounts (single decimal point, max precision, locale-aware)
   - [ ] Add UI state for loading, error, and empty states; avoid silent failures
   - [ ] Extract UI strings to resources for full localization; avoid hard-coded strings in Composables and Widgets
   - [ ] Improve accessibility (content descriptions, contrast, dynamic type support)

7. [ ] Widget architecture and UX
   - [ ] Move widget logic into a dedicated package (widget.domain, widget.data for shared prefs)
   - [ ] Abstract SharedPreferences access into a small WidgetPrefs data source
   - [ ] Use WorkManager or GlanceAppWidget update scheduling for periodic refreshes
   - [ ] Add configuration activity validation and error surfacing
   - [ ] Replace magic strings for prefs keys with constants

8. [ ] Concurrency and performance
   - [ ] Ensure repository calls are on Dispatchers.IO with structured concurrency
   - [ ] Debounce/throttle conversions more deterministically (cancel and latest)
   - [ ] Introduce caching/memoization for repeated same-parameters conversions during a session

9. [ ] Testing strategy
   - [ ] Add unit tests for ConvertUseCase, ExchangeRepositoryImpl (with mocked HttpClient)
   - [ ] Add ViewModel tests for ExchangeViewModel actions and debouncing behavior
   - [ ] Add instrumentation tests for the main screen happy path and error path
   - [ ] Add snapshot tests for Composables (if feasible) and a Glance widget test

10. [ ] Observability and logging
    - [ ] Replace println with a structured logger (e.g., Napier/Timber) and unify tags
    - [ ] Add minimal analytics for conversion actions (respecting privacy)

11. [ ] Security and secrets management
    - [ ] Move API key out of BuildConfig to encrypted storage or remote config where possible
    - [ ] Ensure API key is not committed; verify via git-secrets or pre-commit hook
    - [ ] Add network security config (HTTPS enforcement, certificate pinning optional)

12. [ ] Build and CI/CD improvements
    - [ ] Add a GitHub Actions (or preferred CI) workflow: build, lint, test
    - [ ] Enable caching for Gradle and KMP toolchains if used
    - [ ] Add a release build job with versioning and changelog generation

13. [ ] Gradle and dependency hygiene
    - [ ] Centralize versions in libs.versions.toml (already present) and remove duplicates from module build files
    - [ ] Enable version catalogs for plugins; keep versions up to date with Renovate/Dependabot
    - [ ] Turn on configuration cache and build cache; fix issues that block it

14. [ ] API layer hardening
    - [ ] Define typed request/response DTOs for the pair endpoint
    - [ ] Add JSON serialization configuration (ignore unknown keys, strictness) and error body parsing
    - [ ] Handle HTTP status codes explicitly

15. [ ] UX polish
    - [ ] Provide proper loading indicators instead of text-only (progress indicators where appropriate)
    - [ ] Show last updated time for rates; indicate stale data in widget and app
    - [ ] Add currency picker search and grouping by region/favorites

16. [ ] Internationalization (i18n) and localization (l10n)
    - [ ] Use NumberFormat with Locale for amount formatting and parsing
    - [ ] Provide translations for key locales (at least en, es, fr) and set up Crowdin/strings pipeline if needed

17. [ ] State management and immutability
    - [ ] Make state updates atomic and minimize intermediate recompositions
    - [ ] Extract smaller state holders for quickAccessRates to avoid full recompositions

18. [ ] Cleanup and consistency
    - [ ] Fix typos in package names (currencyconveter -> currencyconverter) via a safe refactor plan
    - [ ] Remove unused imports and resources
    - [ ] Ensure filenames match class names and Kotlin conventions

19. [ ] Documentation
    - [ ] Add README sections: architecture overview, setup, secrets, build, testing
    - [ ] Document API endpoints and error cases
    - [ ] Add CONTRIBUTING.md with code style and commit message conventions

20. [ ] Monitoring app quality
    - [ ] Integrate Crashlytics or a privacy-preserving crash reporter (optional)
    - [ ] Add ANR and performance monitoring (e.g., Android Performance Tuner, if applicable)
