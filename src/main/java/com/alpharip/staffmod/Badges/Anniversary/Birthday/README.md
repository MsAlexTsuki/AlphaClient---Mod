# Account Birthday Celebration

This is an extension of the existing `Badges/Anniversary` prototype, not a separate top-level module.

## Included

- Local birthday date setting using `YYYY-MM-DD`.
- Enable/disable control in a dedicated birthday settings screen.
- Birthday detection based on month and day; the birth year is not used for annual matching.
- A celebration service that submits one burst of 36 renderer-neutral confetti particle descriptions per matching calendar day.
- A particle sink interface that defines the boundary for the future Minecraft 1.8.9 renderer.

## Integration still required

- The client must provide the birthday date from an approved account-data source. This prototype keeps the date in its in-memory settings object for the current GUI/session and does not connect to Supabase or any network service.
- The client tick must call `BirthdayCelebrationService.tryCelebrate(...)` when the player and world are available.
- A Minecraft 1.8.9 particle adapter must implement `BirthdayConfettiParticleSink` and render the supplied particle descriptions.
- The Anniversary GUI opens the birthday settings screen; the settings object is retained while navigating between these screens.

## Date behavior

The year in the entered date is ignored for annual birthday matching. A February 29 birthday matches February 29 only; no alternative non-leap-year date is assumed.

## Verification status

This is a source prototype. It has not been compiled or tested in-game. The actual particle renderer and client tick registration are not implemented here.
