# AlphaCoins

Location: `src/main/java/com/alpharip/staffmod/AlphaCoins/`  
Branch: `mod-ideas`

## Structure

- `CORE/` — shared AlphaCoins currency, wallet, transaction ledger, service API, and local persistence.
- `AlphaCoinsDailyChallenges/` — daily challenge definitions, rotation, event tracking, progress, and GUI.

## CORE components

- `AlphaCoinsCore.java` — central singleton and profile lifecycle.
- `AlphaCoin.java` — AlphaCoin currency metadata and validated amount value.
- `AlphaCoinWallet.java` — balance, credit/debit ledger, and bounded history.
- `AlphaCoinTransaction.java` — immutable transaction record.
- `AlphaCoinService.java` — public API for adding, spending, and querying coins.
- `AlphaCoinStorage.java` — versioned local wallet persistence and legacy wallet migration.

Other modules should use `AlphaCoinsCore.getInstance().getService()` rather than creating their own wallet.

## Daily Challenges

Daily Challenges award AlphaCoins through the CORE service. Challenge progress and claimed-state remain in the Daily Challenges module. The current rotation contains four categories: playtime, block breaking, crafting, and hostile mobs. Initial rewards range from 10 to 35 AlphaCoins and are defined in `DailyChallengeCatalog.java`.

## Local data and limitations

- CORE wallet files are stored under `config/alphaclient/alphacoins/<player-uuid>.properties`.
- Daily challenge progress remains under `config/alphaclient/daily-challenges/<player-uuid>.properties`.
- On first CORE activation, the previous wallet balance and transaction history are imported from the old Daily Challenges properties file when no CORE wallet file exists.
- No Supabase, remote API, payment service, account entitlement, or cross-device synchronization is included.
- The wallet is local prototype data, not proof of a Store purchase. Store integration is not connected.
- The module is not registered in `AlphaClient.java` or added to the live menu.
- The refactor has not been compiled or runtime-tested in the project environment.
