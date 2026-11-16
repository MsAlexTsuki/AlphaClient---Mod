# Alpha Policy Drafting Framework

**Document ID:** POL-FRAMEWORK  
**Version:** 0.1-draft  
**Status:** Internal drafting standard  
**Audience:** Alpha policy authors and reviewers

## 1. Purpose

This framework defines how Alpha's user-facing policies are to be written and maintained. The policies are intended for presentation in Alpha Launcher and must also remain usable as standalone documents.

## 2. User-facing document format

Each policy should use this structure where relevant:

1. Document title and policy ID
2. Version and effective date
3. Short introduction
4. Who and what the policy covers
5. Definitions of important terms
6. User rights, responsibilities, or restrictions
7. Alpha's responsibilities and decision-making authority
8. Exceptions and appeal or contact routes
9. Changes to the policy
10. Contact details and related policies

Use short sections, descriptive headings, concise paragraphs, and lists. Avoid relying on color, icons, or layout alone to communicate legal meaning. The Launcher must be able to display the complete text without hiding important terms behind ambiguous summaries.

## 3. Document categories

- **Legal terms:** contractual relationship, eligibility, account termination, liability, governing law, and dispute handling.
- **Privacy notices:** controller identity, personal-data categories, purposes, legal bases, recipients, transfers, retention, and data-subject rights.
- **Community rules:** user conduct, prohibited behavior, moderation, sanctions, and appeals.
- **Internal staff rules:** staff duties, confidentiality, access boundaries, conflicts of interest, and enforcement.
- **Product and store rules:** digital content, access conditions, pricing, orders, withdrawal rights, refunds, and consumer remedies where applicable.
- **Operational policies:** beta participation, security reporting, service changes, outages, and third-party dependencies.

These categories must not be merged in a way that obscures which rules apply to ordinary users and which apply only to staff.

## 4. Verified product and Supabase context

The inspected Alpha Launcher branch contains code for:

- Alpha Account authentication through Supabase
- Community profile data, including username, display name, rank, Founder flag, biography, and avatar URL
- Online presence
- Friends and private messages
- Linking Minecraft UUIDs to Alpha profiles
- Locally stored launcher settings and saved Microsoft-account display information
- A Store interface with a cosmetic catalogue

The live AlphaClient Supabase project was also inspected on 16 November 2026. It reports region `eu-west-1` (EU West / Ireland). The database schema includes:

- Authentication accounts and identities, email-based identity records, authentication metadata, sessions, and refresh-token records
- Profiles, profile ranks, rank permissions, and community verification
- Friend requests, friendships, private messages, and profile activity
- Minecraft UUID links and active Minecraft session heartbeat records
- A public `profile-avatars` storage bucket, configured for PNG, JPEG, WebP, and GIF files with a 5 MB file limit
- Staff presence, staff operations, staff activity, player notes, private staff notes, and administrative audit records
- Pixel Garden account saves, progression/prestige state, reset-token state, and shared quest/NPC definitions
- Client module/command permissions, per-user grants/overrides, permission notes, and permission audit records
- Easter Egg activation/settings records and AlphaClient release-management records

The current database contains fields for email authentication, optional username/display-name metadata, profile biography/avatar, Minecraft UUIDs, private-message content, gameplay save state, presence timestamps, staff notes, rank/permission changes, and administrative audit details. The detailed user-facing inventory is maintained in POL-002.

This is a schema and selected deployed-function inspection snapshot, not a complete data-flow audit. It does not establish every runtime network request, data recipient, retention period, legal basis, backup location, infrastructure log, external provider, or local-device data flow. The Supabase Auth audit-log schema has an IP-address field, but its table contained no rows at inspection time; actual IP collection/retention remains unverified.

The inspected Store catalogue code and Supabase application schema do not establish that purchases or payment processing are currently available. Store terms must not promise or describe a checkout, refund process, or payment provider until those flows are verified.

## 5. Alpha-specific rules that must be preserved

When drafting rank, staff, and access policies:

- Preserve the established Alpha rank hierarchy and distinguish staff ranks from donator/community ranks.
- Founder is exclusive to the Founder account; do not describe Founder as an ordinary assignable rank.
- Owner does not receive access to the private Founder folder.
- Beta is a separately assignable beta-tester rank directly below The Nibbler.
- Multiple assigned ranks may contribute permissions while the public profile displays only the highest applicable rank where that display rule is used.
- Do not promise rank benefits or permissions that have not been verified against the current implementation.

These are Alpha policy requirements to document, not evidence that every related permission is currently defect-free.

## 6. Legal and factual safeguards

Before publication, each policy must be checked against the actual service configuration and applicable law. In particular:

- Identify the legal person or entity operating Alpha.
- Confirm the operator's country and official contact details.
- Verify the actual personal-data flows and all processors or third-party services.
- Determine whether the Store accepts payments and identify the actual seller and payment flow.
- Confirm the intended minimum age and any parental-consent process.
- Confirm account suspension, deletion, appeal, and data-retention procedures.
- Review consumer, privacy, platform, intellectual-property, and other applicable obligations for the actual operating model.

Do not use generic disclaimers to remove mandatory consumer or data-protection rights.

## 7. Approval and release

A draft becomes publishable only after:

1. Alpha confirms all factual details.
2. Product behavior and data flows are checked against the relevant code and production configuration.
3. Legal review is completed where required.
4. The policy owner approves the final wording.
5. The final version, effective date, and publication record are recorded.

No Launcher code or configuration is changed by editing documents in this directory.
