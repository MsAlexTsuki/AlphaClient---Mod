# AlphaCoins

Location: `src/main/java/com/alpharip/staffmod/AlphaCoins/`  
Branch: `mod-ideas`

## Structure

- `CORE/` — shared AlphaCoins currency, wallet, transaction ledger, service API, and local persistence.
- `AlphaCoinsDailyChallenges/` — daily challenge definitions, rotation, event tracking, progress, and GUI.
- `AlphaCoinsCommands.java` — standalone command-handler prototype for balance, challenge, and account coin operations.
- `alphacoinsplaytime/` — playtime-based recurring rewards and one-time milestones.

## CORE components

- `CORE/AlphaCoinsCore.java` — central singleton and profile lifecycle.
- `CORE/AlphaCoin.java` — AlphaCoin currency metadata and validated amount value.
- `CORE/AlphaCoinWallet.java` — balance, credit/debit ledger, and bounded history.
- `CORE/AlphaCoinTransaction.java` — immutable transaction record.
- `CORE/AlphaCoinService.java` — public API for adding, spending, and querying coins.
- `CORE/AlphaCoinStorage.java` — versioned local wallet persistence and legacy wallet migration.

Other modules should use `AlphaCoinsCore.getInstance().getService()` rather than creating their own wallet.

## Daily Challenges

Daily Challenges award AlphaCoins through the CORE service. Challenge progress and claimed-state remain in the Daily Challenges module. The current rotation contains four categories: playtime, block breaking, crafting, and hostile mobs. Initial rewards range from 10 to 35 AlphaCoins and are defined in `AlphaCoinsDailyChallenges/DailyChallengeCatalog.java`.

## Commands prototype

`AlphaCoinsCommands.java` contains the requested command parsing and validation:

- `/acoins`, `/alphacoins`, and `/alphaclient:Alphacoins` — show the active local wallet balance.
- `/acoins challenges` and `/alphacoins challanges` — list the active daily challenges.
- `check <alpha account>` — look up another account's balance.
- `give <alpha account> <amount>` — ordinary transfers, limited to 200 coins per issuing account in a rolling 48-hour window.
- `give OW <alpha account> <amount>` and `take OW <alpha account> <amount>` — Owner/Founder-only operations, limited to 1,200 coins per command.
- `challengesc <task-id>` — test-completes the selected active challenge's remaining progress and checks whether the normal progress flow automatically claims its reward.

The command class is not registered with Minecraft. Account lookup and remote-account mutations are exposed through an `AccountGateway` interface for later integration. The ordinary-give 48-hour ledger is currently in memory and resets when the command handler is recreated or the game restarts.

### Automatic-claim test result from code inspection

The current `DailyChallengeManager.record()` method updates progress and saves it, but does not call `claim()`. Therefore, `challengesc` deliberately does not call `claim()`: it reports whether automatic claiming actually happened instead of simulating a successful auto-claim. Based on the current code, a challenge can become complete but will remain unclaimed through the normal `record()` flow.

## Local data and limitations

- CORE wallet files are stored under `config/alphaclient/alphacoins/<player-uuid>.properties`.
- Daily challenge progress remains under `config/alphaclient/daily-challenges/<player-uuid>.properties`.
- On first CORE activation, the previous wallet balance and transaction history are imported from the old Daily Challenges properties file when no CORE wallet file exists.
- No Supabase, remote API, payment service, account entitlement, or cross-device synchronization is included.
- The wallet is local prototype data, not proof of a Store purchase. Store integration is not connected.
- The module and command prototype are not registered in `AlphaClient.java` or added to the live menu.
- The existing CORE and Daily Challenges refactor has not been compiled or runtime-tested in the project environment. The command prototype has not been compiled or runtime-tested either.


## Playtime Rewards

The `alphacoinsplaytime/` package tracks active playtime for the current Alpha profile and credits rewards through the existing CORE service. It uses the CORE wallet; it does not create a second balance.

| Playtime interval | Reward |
|---|---:|
| Every completed 24 hours | 5 AlphaCoins |
| Every completed 10 days | 50 AlphaCoins |
| 1 month (30 days) | 100 AlphaCoins |
| 3 months (90 days) | 300 AlphaCoins |
| 6 months (180 days) | 800 AlphaCoins |
| 9 months (270 days) | 1,200 AlphaCoins |
| 1 year (365 days) | 2,400 AlphaCoins |

The 24-hour and 10-day rewards repeat for each completed interval. The month/year milestones are one-time rewards per profile. All durations are active playtime, not calendar time.

### Files

- `AlphaCoinsPlaytime.java` — profile-aware playtime and reward coordinator.
- `PlaytimeRewardDefinition.java` — reward intervals and milestone amounts.
- `PlaytimeRewardStatus.java` — immutable progress/status model for a future GUI.
- `PlaytimeRewardStorage.java` — local versioned properties persistence for playtime and issued-reward markers.
- `AlphaCoinsPlaytimeEventHandler.java` — explicit Forge client-tick adapter.
- `AlphaCoinsPlaytimeModule.java` — explicit registration lifecycle wrapper.

### Storage and integration status

- Playtime state: `config/alphaclient/alphacoins/playtime/<player-uuid>.properties`.
- Coin balances continue to use the existing CORE path: `config/alphaclient/alphacoins/<player-uuid>.properties`.
- No Supabase or remote persistence is included.
- The module is not registered from AlphaClient Core and has no live menu/GUI entry.
- The original idea file under `staffops/ideas/` remains unchanged.
- Compile/runtime behavior has not been verified. The wallet and reward-state files are separate, so crash-safe atomicity across a reward credit and its marker is not guaranteed.
