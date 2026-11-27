# Auto Loading Server Profiles

## Purpose
Automatically select an existing client profile when the player connects to a configured Minecraft server.

## Current prototype structure
- **model** — immutable server-to-profile binding and match result.
- **matching** — address normalization and best-match selection.
- **registry** — in-memory binding management.
- **storage** — local Java Properties persistence behind a storage interface.
- **runtime** — connection-time orchestration and a profile-activation adapter contract.

## Matching rules
1. Disabled bindings are ignored.
2. An exact server address wins over a domain match.
3. A domain binding also matches its subdomains (for example, `play.example.net` can match `example.net`).
4. When multiple domain bindings match, the most specific/longest domain wins.
5. An explicitly configured port must match the connected address port. No default port is assumed.

## Integration boundary
The prototype deliberately does not hook Minecraft/Forge connection events or modify the existing **Server Profiles** feature. The client integration must:
- load bindings from `ServerProfileStore` into `ServerProfileRegistry`;
- persist registry changes through `ServerProfileStore`;
- implement `ServerProfileActivationPort` by calling the existing profile-selection mechanism;
- call `ServerProfileAutoLoadController.onServerConnected(address)` after a successful server connection.

This keeps address matching independent from the current profile implementation and avoids coupling the prototype to unrelated client code.

## Persistence format
The `PropertiesServerProfileStore` stores the binding count and each binding's server address, profile identifier, and enabled state in a local properties file.

## Scope
Prototype only. No Supabase, network service, automatic Minecraft event registration, GUI, or Alpha Client core integration is included.
