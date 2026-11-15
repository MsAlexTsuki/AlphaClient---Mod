# AlphaCoinsDailyChallenges

Location: `src/main/java/com/alpharip/staffmod/AlphaCoins/AlphaCoinsDailyChallenges/`  
Branch: `mod-ideas`

## Purpose

A standalone Minecraft 1.8.9 daily challenge module. Players complete rotating objectives and claim AlphaCoins through the shared AlphaCoins CORE.

## Included files

- `AlphaDailyChallenges.java` — module facade, registration lifecycle, and GUI entry point.
- `DailyChallengeManager.java` — daily rotation, progress updates, reward claims, profile selection, and CORE service coordination.
- `DailyChallengeEventHandler.java` — playtime, block-break, crafting, and hostile-mob event collection.
- `DailyChallengesGui.java` — challenge list, progress, reward values, wallet balance, and claim controls.
- `DailyChallengeCatalog.java` — deterministic four-category daily challenge rotation.
- `DailyChallengeDefinition.java` / `DailyChallengeProgress.java` — objective metadata and bounded progress state.
- `ChallengeMetric.java` / `ChallengeDifficulty.java` — supported progress metrics and difficulty labels.
- `DailyChallengeStorage.java` — local persistence for challenge progress and claimed state only.

## AlphaCoins dependency

All currency logic belongs to the sibling `AlphaCoins/CORE/` package:

- `AlphaCoinsCore.java` — central currency facade and profile lifecycle.
- `AlphaCoinService.java` — add, spend, balance, and transaction-history API.
- `AlphaCoinWallet.java` / `AlphaCoinTransaction.java` — wallet and ledger implementation.
- `AlphaCoinStorage.java` — local wallet persistence and migration from the previous Daily Challenges wallet format.

Daily Challenges must not create or own a separate wallet. It awards coins through `AlphaCoinsCore.getInstance().getService().addCoins(...)`.

## Current challenge categories

Each day includes one objective from each category:

- Playtime: 10, 20, or 30 minutes.
- Block breaking: 20, 40, or 64 blocks.
- Crafting: 3, 8, or 15 crafted items.
- Hostile mobs: defeat 2, 5, or 8 hostile mobs.

The rotation is deterministic by local calendar date. Default rewards range from 10 to 35 AlphaCoins and are defined in `DailyChallengeCatalog.java`.

## Data and integration boundaries

- Challenge progress and claimed rewards remain under `config/alphaclient/daily-challenges/<player-uuid>.properties`.
- Wallet balance and the last 100 transactions are stored by CORE under `config/alphaclient/alphacoins/<player-uuid>.properties`.
- CORE imports the old wallet balance and transaction history from the previous Daily Challenges properties file when no CORE wallet file exists yet.
- No Supabase, remote API, payment service, or account entitlement is used.
- AlphaCoins are local prototype data only. They are not synchronized between devices or accounts and are not proof of a purchased Store entitlement.
- The module is not registered in `AlphaClient.java`, added to the live menu, or wired to the Store.
- Event collection and this refactor have not been compiled or runtime-tested in the project environment.
- The prototype does not include anti-cheat validation. In multiplayer, authoritative rewards require server-side validation before AlphaCoins can be treated as transferable or redeemable account currency.
