# Blind Case Review

A local Minecraft 1.8.9 / Forge-oriented prototype for reviewing supplied case material before revealing separately supplied restricted context.

## Workflow

1. The host creates a `BlindCase` with a neutral case reference, reviewer-facing summary and evidence, and a separate restricted-context map.
2. `BlindCaseReviewService` opens an in-memory review session.
3. The reviewer sees only the `BlindCaseView` and submits one decision: APPROVE, REJECT, or ESCALATE, with a rationale.
4. Restricted context can be revealed only after the decision is submitted.

## Components

- `BlindCase` and `BlindCaseEvidence`: immutable case input.
- `BlindCaseAnonymizer` and `BlindCaseView`: expose only the fields explicitly assigned to the blind-review side.
- `BlindReviewSession` and `BlindReviewDecision`: enforce one decision before context reveal.
- `BlindCaseReviewService`: in-memory session coordinator.
- `BlindCaseReviewFormatter`: formats decision and context summaries.
- `BlindCaseReviewGui`: local review screen; the host must open it explicitly.

## Important boundaries

- This prototype does **not** automatically detect or redact names, usernames, IP addresses, or other identifying information. The caller must keep identifying details out of the summary/evidence and put any withheld material in restricted context.
- It does not fetch cases, evidence, or staff identities.
- It has no Supabase, network, server-plugin, persistence, or audit-log integration.
- It does not enforce Alpha rank permissions or decide who may review a case.
- It does not automatically assign, approve, reject, punish, or modify an appeal. The decision is only a local review record.
- The in-memory data is lost when the client process ends.
- GUI registration and opening from StaffMods are not connected.
- Java source targets Java 8. A Minecraft 1.8.9 / Forge 11.15.1.2318 build and runtime test have not been performed.

The case separation is explicit input, not a verified anonymisation guarantee.
