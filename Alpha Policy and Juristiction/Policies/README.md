# Alpha Policies

**Status:** Drafting workspace  
**Intended publication surface:** Alpha Launcher  
**Document language:** English  
**Legal review:** Required before publication

This directory contains the policy documents intended to govern the Alpha ecosystem. These documents are being prepared for clear presentation inside Alpha Launcher, so each policy must be understandable as a standalone user-facing document.

## Scope

The policy framework is intended to cover, where applicable:

- Alpha Launcher and its updater
- Alpha Client and related client features
- Alpha Accounts, authentication, profiles, and linked Minecraft accounts
- Community features, including friends, presence, and private messages
- Ranks, permissions, staff access, and moderation
- Store listings, cosmetics, digital content, and any future transactions
- Beta testing, support, security reports, and service availability
- Alpha-operated websites and other services that are formally brought within scope

A feature must not be described as available, a payment as supported, or a data practice as occurring unless it has been verified.

## Policy index

| ID | Document | Status |
|---|---|---|
| POL-001 | Terms of Service | Draft v0.1 — confirmation and legal review required |
| POL-002 | Privacy Policy | Draft v0.2 — Supabase schema reviewed; full data-flow verification and legal review required |
| POL-003 | Acceptable Use Policy | Not drafted |
| POL-004 | Community Guidelines | Not drafted |
| POL-005 | Staff Code of Conduct | Not drafted |
| POL-006 | Ranks and Permissions Policy | Not drafted |
| POL-007 | Store and Digital Content Policy | Not drafted |
| POL-008 | Beta Testing Policy | Not drafted |
| POL-009 | Security and Vulnerability Disclosure Policy | Not drafted |
| POL-010 | Service Availability and Third-Party Services Policy | Not drafted |
| POL-011 | Cookie and Tracking Notice | Applicability to be verified |
| POL-012 | Data Retention and Privacy Requests Procedure | Not drafted |

## Drafting rules

1. Use plain, precise English suitable for an in-app reading experience.
2. Give every policy a document ID, title, version, effective date, and last-updated date.
3. Explain who the policy applies to and which Alpha services it covers.
4. Separate contractual terms, community rules, operational procedures, and legal privacy notices.
5. Do not invent the legal operator, address, contact details, age threshold, payment arrangements, retention periods, or legal bases for processing.
6. Mark unresolved facts as **[REQUIRES ALPHA CONFIRMATION]** in drafts. Remove all such markers before publication.
7. Do not claim that a policy has been legally reviewed or that Alpha is compliant unless that has been independently verified.
8. Keep policy content separate from Launcher implementation. This repository folder is the drafting source; it does not itself publish or enforce policies.

## Operator information currently confirmed\n\n- Operator model: private individual (not a company), as supplied by the project owner.\n- Country of establishment: The Netherlands.\n- Full legal name, postal address, and contact email: not yet supplied; drafts use explicit placeholders.\n\n## Supabase inspection snapshot\n\nThe live AlphaClient Supabase project was inspected on 16 November 2026. The database reports the `eu-west-1` region. The privacy draft now identifies the data categories represented by the current Auth schema, public application tables, profile-avatar storage bucket, selected row-level-security policies, and deployed Edge Functions. This is a schema/code snapshot, not a complete audit of all runtime traffic, external providers, infrastructure logs, backups, or local Launcher storage. See POL-002 for the user-facing inventory and unresolved verification items.\n\n## Publication readiness

No document in this workspace should be treated as final or legally approved until its factual placeholders have been resolved and the appropriate legal review has taken place.
