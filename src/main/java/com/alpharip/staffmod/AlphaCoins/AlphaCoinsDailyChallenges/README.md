# AlphaDailyChallenges

Location: `src/main/java/com/alpharip/staffmod/AlphaCoins/AlphaCoinsDailyChallenges/`  
Branch: `mod-ideas`

## Purpose

A standalone Minecraft 1.8.9 daily challenge module. Players complete rotating objectives, claim AlphaCoins, and retain a local wallet balance. The wallet exposes a debit API intended for a future Store purchase integration.

## Included files

- `AlphaDailyChallenges.java` — module facade, registration lifecycle, GUI entry point.
- `DailyChallengeManager.java` — daily rotation, progress updates, reward claiming, profile selection, and wallet coordination.
- `DailyChallengeEventHandler.java` — playtime, block-break, crafting, and hostile-mob event collection.
- `DailyChallengesGui.java` — challenge list, progress, reward values, wallet balance, and claim controls.
- `DailyChallengeCatalog.java` — deterministic four-category daily challenge rotation.
- `DailyChallengeDefinition.java` / `DailyChallengeProgress.java` — objective metadata and bounded progress state.
- `ChallengeMetric.java` / `ChallengeDifficulty.java` — supported progress metrics and difficulty labels.
- `AlphaCoinWallet.java` / `AlphaCoinTransaction.java` — local balance, spend API, and bounded transaction history.
- `DailyChallengeStorage.java` — versioned local Properties persistence with temporary-file replacement.

## Current challenge categories

Each day includes one objective from each category:

- Playtime: 10, 20, or 30 minutes.
- Block breaking: 20, 40, or 64 blocks.
- Crafting: 3, 8, or 15 crafted items.
- Hostile mobs: defeat 2, 5, or 8 hostile mobs.

The rotation is deterministic by local calendar date. Default rewards range from 10 to 35 AlphaCoins. These are initial prototype values and are defined in `DailyChallengeCatalog.java`.

## Data and integration boundaries

- Progress, claimed rewards, wallet balance, and the last 100 transactions are stored locally under `config/alphaclient/daily-challenges/<player-uuid>.properties`.
- No Supabase, remote API, payment service, or account entitlement is used.
- AlphaCoins in this prototype are local data only. They are not synchronized between devices or accounts and are not proof of a purchased Store entitlement.
- `AlphaCoinWallet.spend` and `DailyChallengeManager.spendCoins` provide a local debit boundary for a future Store integration. The existing Store purchase flow is not modified or connected here.
- The module is not registered in `AlphaClient.java`, added to the live menu, or wired to the Store. A future integration must explicitly register the facade and connect the Store's purchase validation to the wallet.
- Event collection is implemented against the Forge 1.8.9 event APIs but has not been compiled or runtime-tested in the project environment.
- The prototype does not include anti-cheat validation. In multiplayer, authoritative rewards require server-side validation before AlphaCoins can be treated as transferable or redeemable account currency.
