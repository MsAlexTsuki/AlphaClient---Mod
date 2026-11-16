# Appeal Conflict Detector prototype

## Location
`src/main/java/com/alpharip/staffmod/appealconflictdetector/`

## Purpose
Checks whether an assigned appeal reviewer is also listed as a participant in that appeal, using only conflict rules explicitly supplied by the caller. It produces findings for human review; it does not automatically reject an appeal or change an assignment.

## Java components
- `AppealCase`: immutable case input and assigned reviewer.
- `AppealInvolvement` / `AppealInvolvementType`: participants and their supplied roles.
- `AppealConflictRule` / `AppealConflictPolicy`: configurable conflict policy.
- `AppealConflictDetector`: matching engine.
- `AppealConflictFinding` / `AppealConflictReport`: immutable results.
- `AppealConflictRegistry`: in-memory prototype case storage.
- `AppealConflictService`: evaluates one or all registered cases.
- `AppealConflictFormatter`: readable text output.
- `AppealConflictDetectorGui`: read-only Minecraft GUI overview.

## Policy is deliberately not preconfigured
The idea file does not define Alpha's actual conflict-of-interest policy. Therefore the policy starts empty. A caller must explicitly add enabled rules for involvement types that should count as conflicts. The enum values are data categories, not an assertion that any one category is automatically disqualifying.

## Example wiring
```java
AppealConflictRegistry registry = new AppealConflictRegistry();
AppealConflictPolicy policy = new AppealConflictPolicy();
policy.putRule(new AppealConflictRule(
    AppealInvolvementType.ORIGINAL_ACTION_ISSUER,
    "Reviewer issued the original action",
    true
));
AppealConflictService service = new AppealConflictService(registry, policy);
```

The example rule is illustrative only; it is not an approved Alpha policy.

## Scope and limitations
- Local in-memory prototype only. No Supabase, network calls, server changes, or persistence.
- No automatic retrieval of appeals, staff identities, or participant history; the host must supply the data.
- No automatic reassignment, appeal rejection, punishment changes, or command execution.
- The GUI is read-only and is not registered in AlphaClient core or StaffMods.
- No rank/permission checks are connected.
- Minecraft 1.8.9 / Forge 11.15.1.2318 compilation and runtime behavior have not been verified.
- No build or tests have been run.
