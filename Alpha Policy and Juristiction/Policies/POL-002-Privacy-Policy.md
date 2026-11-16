# Alpha Privacy Policy

**Document ID:** POL-002  
**Version:** 0.3-draft  
**Status:** Draft — not approved for publication  
**Effective date:** Not effective until formally published  
**Last updated:** 16 November 2026  
**Controller:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]  
**Country of establishment:** The Netherlands

> **Draft notice:** This notice includes a review of the live Alpha Supabase database schema, its current tables, selected database policies, authentication metadata structure, storage configuration, and deployed Edge Function code on 16 November 2026. It is not a complete audit of every application, network, provider, or production data flow. Do not publish it until the remaining items are verified and the notice has received appropriate legal review.

## 1. Who is responsible for your personal data?

Alpha is currently operated by a private individual established in the Netherlands. The data controller must be identified by the operator's full legal name.

**Data controller:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]  
**Privacy contact email:** [REQUIRES ALPHA CONFIRMATION]  
**Postal address:** [REQUIRES ALPHA CONFIRMATION]

For the purposes of the General Data Protection Regulation (GDPR/AVG), the controller is the person who determines the purposes and means of processing personal data. The exact controller identity and any joint-controller arrangements must be confirmed before publication.

## 2. Which services does this notice cover?

This notice is intended to cover Alpha services that the controller operates, including, where enabled:

- Alpha Launcher and Alpha Client account-related functionality;
- Alpha Accounts and community profiles;
- friends, online presence, and private messaging;
- links between Alpha profiles and Minecraft accounts;
- rank, permission, staff, and moderation systems;
- Pixel Garden account saves and progression;
- support, security, and administrative processes;
- release-management functions used by authorized Alpha staff.

The live Supabase project inspected for this draft is named **AlphaClient** and reports its database region as **EU West (Ireland / eu-west-1)**. This is the configured project region returned by Supabase; it does not by itself identify every subprocessor, backup location, transfer, or legal entity involved in processing.

## 3. What information Alpha receives and stores

The following inventory is based on the Supabase database schema and deployed function code inspected on 16 November 2026. It describes the information represented by the current database structures. Some fields are populated only when a feature is used, and some records are created by Alpha or automatically by the service rather than typed directly by a user.

### 3.1 Information you provide when creating and using an Alpha Account

| Information | What Alpha's Supabase setup contains | How it is obtained |
|---|---|---|
| Email address | An email field in the authentication user record and linked identity record | Supplied during email-based account registration or account management |
| Password credential | Supabase Auth maintains an authentication password-credential field; the Alpha profile table does not contain a plaintext password field | Submitted to Supabase Auth for authentication; Alpha's application should not ask for or store your plaintext password separately |
| Alpha username | A username field in the community profile and optional `username` authentication metadata | Supplied during registration or profile setup; a database trigger uses the supplied username metadata when creating the profile |
| Display name | A display-name field in the community profile and optional `display_name` authentication metadata | Supplied during registration or profile editing |
| Profile biography | A `bio` field in the community profile | Supplied by the user if the profile biography feature is used |
| Profile avatar | An `avatar_url` field and an uploaded image object in the `profile-avatars` storage bucket | Uploaded by the user if an avatar is selected |
| Minecraft account identifier | A Minecraft UUID in the profile/link tables | Created when a Minecraft account is linked to an Alpha profile |

The current authentication identity provider recorded in the inspected database is email. The database also has phone-related authentication columns, but no phone numbers were populated in the current user records inspected for this draft. This is a current database observation, not a promise that phone authentication can never be introduced.

Supabase Auth also maintains internal account and security records, including account IDs, identity-provider metadata, email-confirmation status and timestamps, account creation/update timestamps, last-sign-in timestamps, authentication sessions, refresh-token records, and fields used for confirmation, recovery, reauthentication, and email changes. These are authentication-system records; they are not all fields that a user manually enters into an Alpha profile.

The authentication schema contains a password-credential field named `encrypted_password`. Alpha's public profile table does not contain a plaintext password field. The exact credential handling and security controls are also governed by Supabase Auth and its applicable documentation.

### 3.2 Profile and community information

The Supabase `profiles` table contains:

- Alpha account/profile ID;
- Alpha username;
- display name;
- biography;
- avatar URL;
- community rank key;
- linked Minecraft UUID;
- profile creation and update timestamps.

Some profile attributes, such as the community rank, are controlled by Alpha's server-side permission and rank systems rather than freely supplied by the user.

Authenticated Alpha users can read community profiles under the current database policy. The profile-avatar storage bucket is configured as public, meaning avatar files in that bucket are publicly readable. The current bucket configuration allows PNG, JPEG, WebP, and GIF files, with a maximum file size of 5 MB. Users can upload, update, and delete their own avatar files under the configured storage policies.

### 3.3 Friends, messages, and community activity

When these community features are used, Supabase stores:

- friend-request sender and recipient account IDs;
- friend-request status and creation/response timestamps;
- friendship records identifying the two accounts;
- private-message sender and recipient account IDs;
- the text/body of each private message;
- private-message creation timestamps;
- profile activity, including the last-played timestamp and last-played AlphaClient version.

The current database policy allows only the sender and recipient to read a private-message record. Sending a private message is restricted to accounts that are friends under the current database policy. Friend-request records are readable by their sender and recipient.

### 3.4 Minecraft account links and active sessions

When a user links a Minecraft account, the database stores:

- the Alpha profile ID;
- the Minecraft UUID;
- the time the link was created.

The active Minecraft session table also contains a session ID, Alpha profile ID, Minecraft UUID, heartbeat timestamp, and session-expiry timestamp. These fields support the current session/presence-related functionality.

No Minecraft password field was found in the inspected Supabase application tables. This does not establish how every external Minecraft or Microsoft authentication component handles credentials; that separate authentication flow must be audited before publication.

### 3.5 Online presence and staff presence

The staff-presence table contains:

- the staff user's account ID and Alpha name;
- online and AFK status;
- last-seen and last-heartbeat timestamps;
- session-start and record-update timestamps.

The current database policies restrict this staff-presence information to staff access. This section describes the staff-presence table; it does not establish that Alpha records a complete activity history for every ordinary user.

### 3.6 Pixel Garden game progress

When Pixel Garden is used while signed in, Supabase stores a save associated with the user's account ID. The save data currently has fields for game state such as:

- inventory and seeds;
- plots and unlocked plots;
- growth levels and progression;
- total harvested amounts;
- purchased upgrades and upgrade coins;
- automatic harvest, planting, selling, quest, and crafting settings;
- crafting inventory, active orders, queued orders, ready orders, and capacity/queue levels;
- reset-token and save-processing state.

The related Pixel Garden prestige table stores account-linked game progression, including prestige coins, active seconds, heartbeat time, prestige count, upgrade steps, multiplier override, upgrade settings, and update time. A separate reset-token record stores the account's token count and update time.

These are game-state and gameplay-progress records associated with an Alpha account. They are not a record of the user's real-world financial balance.

The current database policy for Pixel Garden saves allows a signed-in user to read and update their own save. The full behavior of every game event and every client-side calculation has not been independently audited for this notice.

### 3.7 Ranks, permissions, and account verification

The database also stores information used to administer Alpha accounts and access, including:

- rank definitions and rank display metadata;
- rank assignments, assignment timestamps, and the account that made an assignment;
- rank permission settings and the account that last changed them;
- per-user module/category grants, effects, reasons, expiry times, revocation times, and the staff account responsible;
- per-user command permission overrides and update information;
- community verification status, verifier account ID, and verification timestamps;

These records are generally created or changed by authorized Alpha staff or server-side processes. They are not all information submitted by the affected user.

### 3.8 Staff operations, notes, and administrative records

For staff and moderation functionality, the database contains structures for:

- staff operation records, including record type, title, status, creator, assignee, timestamps, and a JSON payload;
- staff module activity, including actor account ID/name, module, event type, action, target label, details, and event timestamp;
- staff player notes, including player name, note text, author account ID/name, and creation time;
- staff private notes, including subject name, note text, author account ID/name, and creation/update times;
- command-permission notes, including permission identifier, note text, author account ID, and creation time.

Depending on the feature, note text or record details may contain information about a user or a Minecraft player. Staff-only access rules apply to several of these records. The precise access and retention rules for each staff record type still need a separate review.

### 3.9 Security and administrative audit records

The Supabase `audit_logs` table can record:

- actor account ID and actor label;
- target account ID and target label, where relevant;
- product and category;
- action performed;
- outcome, such as success, failure, or denied;
- structured details, which may include before/after values, rank or permission identifiers, Alpha names, item/quantity information, or other action-specific details;
- request ID and event timestamp.

The deployed permission, Easter Egg, and release-management functions also write administrative activity to audit records for relevant actions. These records are not a general transcript of everything a user does in Alpha; they document the administrative actions and events that the relevant code sends to the audit system.

The Supabase Auth schema includes an audit-log table with an IP-address field. That table contained no rows at the time of this inspection. Whether IP addresses are populated or retained by the live authentication configuration, infrastructure logs, or other providers has not been established. Alpha must verify this before stating that IP addresses are or are not collected.

### 3.10 Alpha avatar storage

The inspected Supabase project has a `profile-avatars` storage bucket configured as public. Its current configuration allows PNG, JPEG, WebP, and GIF files and sets a 5 MB file-size limit. The storage policies allow public reading and restrict authenticated uploads, updates, and deletions to the user's own account folder.

Users should not upload an avatar that contains personal information they do not want to make publicly accessible.

## 4. Information not established as collected by the inspected Supabase schema

The following distinctions are important:

- **Microsoft account passwords and access tokens:** No Microsoft password or Microsoft OAuth-token columns were found in the inspected Supabase application tables. The Launcher source previously inspected stores saved Microsoft-account display information locally. That local Launcher storage is separate from the Supabase database. The complete Microsoft sign-in and token flow has not been audited here.
- **Phone numbers:** Phone-related columns exist in Supabase Auth, but no phone numbers were populated in the current user records inspected.
- **Payment-card or transaction details:** No payment-card or payment-transaction tables were found in the inspected Supabase application schema. A Store catalogue or cosmetic listing does not prove that checkout or payment processing is active. Any external payment provider or future payment flow must be documented separately before it is enabled.
- **Precise device location:** No precise-location field was identified in the inspected application tables.
- **Advertising identifiers or analytics profiles:** No such fields were identified in the inspected application tables. The full client telemetry, crash-reporting, and third-party SDK configuration has not been audited.
- **Minecraft passwords:** No Minecraft password field was identified in the inspected Supabase application tables.

The absence of a field from the inspected database does not prove that no other component, local application, external provider, server log, or future feature processes that information. These areas remain subject to further verification.

## 5. Information processed locally by the Launcher

The Launcher source inspected separately contains local storage for launcher settings and saved Microsoft-account display information. This is not the same as information stored in the Alpha Supabase project.

The complete local-storage inventory, operating-system protection, account-token handling, and any information transmitted from the Launcher to other providers must be documented in a separate end-to-end data-flow review. Do not assume that every locally stored item is sent to Alpha.

## 6. Why personal data is processed

The following are the purposes suggested by the inspected features. The controller must confirm each purpose and select a valid GDPR legal basis for each processing activity before publication.

| Potential purpose | Related information | Legal basis |
|---|---|---|
| Create and authenticate Alpha Accounts | Email, account ID, authentication records | [REQUIRES ALPHA CONFIRMATION] |
| Create and maintain community profiles | Username, display name, biography, avatar, profile timestamps | [REQUIRES ALPHA CONFIRMATION] |
| Provide friends and private messaging | Friend requests, friendship links, message content and timestamps | [REQUIRES ALPHA CONFIRMATION] |
| Link Minecraft accounts and support session features | Minecraft UUID, link timestamp, session/heartbeat timestamps | [REQUIRES ALPHA CONFIRMATION] |
| Provide profile and staff presence features | Online/AFK status and presence timestamps | [REQUIRES ALPHA CONFIRMATION] |
| Save Pixel Garden progress | Inventory, plots, upgrades, progression, gameplay timers and related save data | [REQUIRES ALPHA CONFIRMATION] |
| Administer ranks, permissions, and verification | Rank assignments, permission grants, verification and staff actor records | [REQUIRES ALPHA CONFIRMATION] |
| Maintain moderation, support, and administrative records | Notes, staff records, audit events and action details | [REQUIRES ALPHA CONFIRMATION] |
| Maintain security and investigate misuse | Authentication records and relevant audit/security events | [REQUIRES ALPHA CONFIRMATION] |

A legal basis must be determined separately for each purpose. Alpha must not use consent as a catch-all basis or claim legitimate interests without documenting the assessment.

## 7. Where data is stored and who receives it

The inspected Alpha Supabase project is hosted in the **EU West (Ireland / eu-west-1)** region. The project uses Supabase Auth, Supabase Database, Supabase Storage, and deployed Supabase Edge Functions.

**Supabase contracting entity and applicable data-processing agreement:** [REQUIRES ALPHA CONFIRMATION]  
**Other hosting, infrastructure, or service providers:** [REQUIRES ALPHA CONFIRMATION]  
**Other recipients or subprocessors:** [REQUIRES ALPHA CONFIRMATION]  
**International transfers and safeguards, if any:** [REQUIRES ALPHA CONFIRMATION]

The final notice must identify recipients or categories of recipients and explain any transfers outside the European Economic Area, including the safeguards relied upon where required.

## 8. How long data is kept

The schema contains creation, update, activity, expiry, and session timestamps for various records, but a timestamp or expiry field is not a complete retention policy. No verified retention schedule was established by this inspection.

**Retention periods or objective retention criteria for each data category:** [REQUIRES ALPHA CONFIRMATION]

Alpha must define retention for account records, profile data, avatars, friend requests, private messages, Minecraft links and sessions, presence records, Pixel Garden saves, staff notes, audit logs, security records, backups, and any Store records that actually exist. Data must not be described as deleted immediately unless the full deletion and backup lifecycle supports that statement.

## 9. Your privacy rights

Depending on the circumstances and applicable law, you may have the right to:

- receive information about the processing of your personal data;
- request access to your personal data;
- request correction of inaccurate or incomplete data;
- request erasure of personal data;
- request restriction of processing;
- object to certain processing;
- receive or transfer certain data in a portable format;
- withdraw consent where processing is based on consent, without affecting processing that took place before withdrawal;
- lodge a complaint with the Dutch Data Protection Authority (Autoriteit Persoonsgegevens).

These rights are subject to the conditions and exceptions in the GDPR. The right to erasure, for example, is not absolute.

## 10. How to make a privacy request

Send privacy requests to:

**Email:** [REQUIRES ALPHA CONFIRMATION]

Alpha must implement a process to verify requests proportionately, respond within the applicable legal time limits, and protect the rights of other people whose data may appear in the requested records.

**Request procedure and identity-verification method:** [REQUIRES ALPHA CONFIRMATION]

## 11. Children and age-related processing

**Minimum age for Alpha services:** [REQUIRES ALPHA CONFIRMATION]  
**Parental-consent process, if applicable:** [REQUIRES ALPHA CONFIRMATION]

Do not publish an age threshold or claim that parental consent is collected until the actual product flow and legal requirements have been checked.

## 12. Security

Alpha should use appropriate technical and organisational measures to protect personal data. The database has row-level security enabled on the inspected application tables and contains access policies for several features. This inspection was not a complete security assessment and does not establish that every table, function, storage object, or production configuration is correctly protected.

**Verified security measures and incident contact route:** [REQUIRES ALPHA CONFIRMATION]

No statement in this draft guarantees that a service is completely secure.

## 13. Cookies, local storage, telemetry, and similar technologies

The Launcher source inspected separately uses local storage for certain launcher settings and saved Microsoft-account display information. This Supabase review does not establish whether Alpha websites or applications use cookies, analytics, telemetry, crash reporting, advertising identifiers, or other tracking technologies.

**Complete cookie/local-storage/telemetry inventory and consent requirements:** [REQUIRES ALPHA CONFIRMATION]

If separate cookie or tracking information is required, it will be provided in POL-011.

## 14. Changes to this Privacy Policy

The published policy will display its version and effective date. If the purposes or manner of processing change, Alpha will update this notice and provide any additional information or obtain any consent required by law.

## 15. Complaints

You may contact Alpha using the privacy contact details above. You also have the right to lodge a complaint with the Dutch Data Protection Authority:

**Autoriteit Persoonsgegevens:** https://autoriteitpersoonsgegevens.nl/

## 16. Contact

**Data controller:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]  
**Privacy email:** [REQUIRES ALPHA CONFIRMATION]  
**Postal address:** [REQUIRES ALPHA CONFIRMATION]  
**Country:** The Netherlands

---

## Internal completion checklist — remove before publication

- [ ] Insert controller's full legal identity and valid contact details.
- [ ] Confirm the Supabase contracting entity, data-processing agreement, subprocessors, and hosting/backup locations.
- [ ] Complete an end-to-end data inventory covering Launcher, Client, Supabase, Microsoft authentication, third-party SDKs, telemetry, crash reporting, logs, and support.
- [ ] Confirm whether authentication or infrastructure logs collect IP addresses and define their retention.
- [ ] Determine and document a valid legal basis for every processing purpose.
- [ ] Set and implement retention periods, including backups and deletion behavior.
- [ ] Verify avatar visibility and explain the public bucket clearly in the user interface.
- [ ] Verify user rights workflows and response ownership.
- [ ] Confirm age policy and any parental-consent mechanism.
- [ ] Verify security measures and incident-response procedures.
- [ ] Audit cookies, local storage, analytics, telemetry, and crash reporting.
- [ ] Confirm whether the Store accepts payments; if so, document the seller, payment provider, data received, and consumer disclosures.
- [ ] Review whether a record of processing activities, DPIA, or other GDPR measures are required.
- [ ] Obtain Dutch/EU data-protection legal review.
- [ ] Remove all unresolved markers and this internal checklist before publication.
