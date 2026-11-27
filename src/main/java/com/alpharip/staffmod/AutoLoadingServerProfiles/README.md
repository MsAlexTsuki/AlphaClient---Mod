# Auto Loading Server Profiles

## Overview

Auto Loading Server Profiles is a prototype for selecting an existing Alpha Client profile automatically when the player connects to a configured Minecraft server.

A server address is linked to a profile identifier. When the connection flow supplies the server address to the controller, the matcher finds the best enabled binding and asks the profile-activation adapter to activate that profile.

This prototype is kept separate from the existing `Customization/ServerProfiles.java` implementation. It does not replace or modify that feature.

## Project structure

```text
AutoLoadingServerProfiles/
├── matching/
│   ├── ServerAddressNormalizer.java
│   └── ServerProfileMatcher.java
├── model/
│   ├── ServerProfileBinding.java
│   └── ServerProfileMatch.java
├── registry/
│   └── ServerProfileRegistry.java
├── runtime/
│   ├── ServerProfileActivationPort.java
│   ├── ServerProfileAutoLoadController.java
│   └── ServerProfileAutoLoadResult.java
├── storage/
│   ├── PropertiesServerProfileStore.java
│   └── ServerProfileStore.java
└── README.md
```

### Responsibilities

| File | Responsibility |
|---|---|
| `model/ServerProfileBinding.java` | Immutable mapping containing a normalized server address, profile identifier, and enabled state. |
| `model/ServerProfileMatch.java` | Carries the selected binding and whether it matched exactly or through a domain. |
| `matching/ServerAddressNormalizer.java` | Normalizes addresses and checks exact-host, subdomain, and optional-port compatibility. |
| `matching/ServerProfileMatcher.java` | Selects the best enabled binding for a connected address. |
| `registry/ServerProfileRegistry.java` | Holds bindings in memory and supports adding, retrieving, removing, and replacing them. |
| `storage/ServerProfileStore.java` | Defines the load/save contract for persistence. |
| `storage/PropertiesServerProfileStore.java` | Loads and saves bindings in a local Java Properties file. |
| `runtime/ServerProfileActivationPort.java` | Adapter contract through which the existing profile system can activate a profile. |
| `runtime/ServerProfileAutoLoadController.java` | Coordinates matching and profile activation after a connection is reported. |
| `runtime/ServerProfileAutoLoadResult.java` | Reports whether there was no match, activation succeeded, or activation was rejected. |

## Matching behavior

The matcher applies these rules:

1. Disabled bindings are ignored.
2. A normalized exact address match takes priority over a domain match.
3. A configured domain can match its subdomains. For example, a binding for `example.net` can match `play.example.net`.
4. If multiple domain bindings match, the longest/more specific configured host wins.
5. If a binding explicitly includes a port, the connected address must use the same port.
6. If a binding does not include a port, it can match the host regardless of the connected port. No default port is inserted.
7. Addresses are normalized to lowercase; surrounding whitespace, a trailing hostname dot, and a URL path/query are removed.

Examples:

| Configured binding | Connected address | Result |
|---|---|---|
| `play.example.net` | `play.example.net` | Exact match |
| `example.net` | `play.example.net` | Domain match |
| `example.net` | `eu.play.example.net` | Domain match |
| `play.example.net:25566` | `play.example.net:25566` | Exact match |
| `play.example.net:25566` | `play.example.net:25565` | No match |
| `other.net` | `play.example.net` | No match |

## Runtime flow

The intended integration flow is:

1. Load saved bindings through `ServerProfileStore.load()`.
2. Add the loaded bindings to `ServerProfileRegistry`.
3. When a Minecraft connection succeeds, pass its server address to `ServerProfileAutoLoadController.onServerConnected(address)`.
4. The controller asks `ServerProfileMatcher` to find the best enabled binding.
5. If there is a match, the controller calls `ServerProfileActivationPort.activateProfile(profileId)`.
6. The returned `ServerProfileAutoLoadResult` indicates whether a match was found and whether activation was accepted.
7. When bindings are changed, save the registry snapshot through `ServerProfileStore.save(registry.getAll())`.

## Persistence

`PropertiesServerProfileStore` stores the bindings in a local Java Properties file. It records:

- the number of bindings;
- each binding's normalized server address;
- the profile identifier assigned to that address;
- whether that binding is enabled.

The storage implementation creates the parent directory when necessary. The caller is responsible for choosing the file path and deciding when to load or save.

## Integration status and limitations

**Implemented in this prototype:**
- address normalization and matching;
- exact-match and subdomain selection;
- most-specific-match priority;
- in-memory binding registry;
- local Properties-based persistence;
- controller and profile-activation interface contracts;
- explicit result states for matching and activation.

**Not integrated yet:**
- Minecraft/Forge connection-event registration;
- detection and forwarding of successful server connections;
- an adapter that calls the real Alpha Client profile-selection mechanism;
- loading and saving the registry as part of the client lifecycle;
- a configuration GUI for managing server-to-profile bindings.

The activation interface is deliberately an integration boundary. The prototype does not claim that profile switching already happens in-game.

## Scope

- Prototype only, on the `mod-ideas` branch.
- No Supabase or remote service.
- No changes to the existing Server Profiles implementation.
- No Alpha Launcher changes.
- No Minecraft/Forge event hooks or GUI yet.
