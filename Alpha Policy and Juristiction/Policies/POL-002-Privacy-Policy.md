# Alpha Privacy Policy

**Document ID:** POL-002  
**Version:** 0.1-draft  
**Status:** Draft — not approved for publication  
**Effective date:** Not effective until formally published  
**Last updated:** 16 November 2026  
**Controller:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]  
**Country of establishment:** The Netherlands

> **Draft notice:** This document is based on a limited inspection of the Alpha Launcher source branch and information supplied by the operator. It is not a completed data-protection audit or a final privacy notice. Do not publish it until all processing details, providers, legal bases, retention periods, and contact details have been verified.

## 1. Who is responsible for your personal data?

Alpha is currently operated by a private individual established in the Netherlands. The identity of the data controller must be stated using the operator's full legal name.

**Data controller:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]  
**Privacy contact email:** [REQUIRES ALPHA CONFIRMATION]  
**Postal address:** [REQUIRES ALPHA CONFIRMATION]

For the purposes of the General Data Protection Regulation (GDPR/AVG), the controller is the person who determines the purposes and means of processing personal data. The exact controller identity and any joint-controller arrangements must be verified before publication.

## 2. Which services does this notice cover?

This notice is intended to cover the Alpha services that the controller operates, including, where enabled:
- Alpha Launcher;
- Alpha Client account-related features;
- Alpha Accounts and community profiles;
- friends, online presence, and private messaging;
- links between Alpha profiles and Minecraft accounts;
- Store and cosmetic features, if they process personal data;
- support, security, and moderation processes.

A separate third-party service may process data under its own privacy notice. The final notice must identify the relevant services and providers accurately.

## 3. Personal data that may be processed

The inspected Launcher source contains code relating to the following data categories. Their actual collection, transmission, storage, and use in the released service still require verification.

| Data category | What the inspected source indicates | Verification status |
|---|---|---|
| Alpha account and authentication data | Alpha Account authentication is implemented through Supabase | Exact fields, authentication logs, and provider configuration not fully audited |
| Community profile data | Username, display name, rank key, Founder flag, and avatar URL are referenced | Verify which fields are stored, public, and visible to other users |
| Presence data | Online-presence functionality is present in the inspected code | Exact events, visibility, and retention not audited |
| Friend and private-message data | Friend functionality and private messages are present; message records include sender, recipient, body, and timestamp in the inspected database migration | Verify production schema, retention, moderation access, and deletion behavior |
| Minecraft account link data | A migration maps Minecraft UUIDs to Alpha profiles | Verify all linked fields and unlink/deletion behavior |
| Local launcher data | Launcher settings and saved Microsoft-account display information are stored locally in the inspected code | Exact local fields, operating-system storage protections, and sync behavior not audited |
| Store and cosmetic data | A cosmetic catalogue is present | Purchase history, payment data, and checkout processing are not established by the inspected catalogue code |

Alpha must not claim that it collects a data category merely because a field exists in source code. The final notice must be based on runtime behavior, production configuration, and all relevant service-provider documentation.

## 4. Why personal data is processed

The purposes below are candidate purposes suggested by the inspected features. The controller must confirm each purpose and select a valid GDPR legal basis for each processing activity before publication.

| Potential purpose | Related feature | Legal basis |
|---|---|---|
| Create and authenticate Alpha Accounts | Account authentication | [REQUIRES ALPHA CONFIRMATION] |
| Display and manage community profiles | Username, display name, rank, avatar | [REQUIRES ALPHA CONFIRMATION] |
| Provide friends and presence features | Friends and online status | [REQUIRES ALPHA CONFIRMATION] |
| Deliver private messaging | Private messages | [REQUIRES ALPHA CONFIRMATION] |
| Associate a Minecraft account with an Alpha profile | Minecraft UUID link | [REQUIRES ALPHA CONFIRMATION] |
| Save launcher preferences and account display information locally | Local launcher settings | [REQUIRES ALPHA CONFIRMATION] |
| Provide Store functionality, if purchases are enabled | Store/cosmetics | [REQUIRES ALPHA CONFIRMATION] |

A legal basis must be determined separately for each purpose. Alpha must not use consent as a catch-all basis or claim legitimate interests without documenting the assessment.

## 5. Where data is stored and who receives it

The inspected source references Supabase for Alpha Account and community-related functionality. This does not, by itself, establish the exact hosting region, contractual processor, subprocessors, international transfers, or production configuration.

**Supabase project region and legal entity:** [REQUIRES ALPHA CONFIRMATION]  
**Hosting and infrastructure providers:** [REQUIRES ALPHA CONFIRMATION]  
**Authentication providers and data flows:** [REQUIRES ALPHA CONFIRMATION]  
**Other processors or recipients:** [REQUIRES ALPHA CONFIRMATION]  
**International transfers and safeguards, if any:** [REQUIRES ALPHA CONFIRMATION]

The final notice must identify recipients or categories of recipients and explain any transfers outside the European Economic Area, including the safeguards relied upon where required.

## 6. How long data is kept

No verified retention schedule was established by the limited source inspection.

**Retention periods or objective retention criteria for each data category:** [REQUIRES ALPHA CONFIRMATION]

Alpha must define retention for account records, profile data, presence records, private messages, Minecraft links, security logs, support records, backups, and any Store records that actually exist. Data must not be described as deleted immediately unless the full deletion and backup lifecycle supports that statement.

## 7. Your privacy rights

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

## 8. How to make a privacy request

Send privacy requests to:

**Email:** [REQUIRES ALPHA CONFIRMATION]

Alpha must implement a process to verify requests proportionately, respond within the applicable legal time limits, and protect the rights of other people whose data may appear in the requested records.

**Request procedure and identity-verification method:** [REQUIRES ALPHA CONFIRMATION]

## 9. Children and age-related processing

**Minimum age for Alpha services:** [REQUIRES ALPHA CONFIRMATION]  
**Parental-consent process, if applicable:** [REQUIRES ALPHA CONFIRMATION]

Do not publish an age threshold or claim that parental consent is collected until the actual product flow and legal requirements have been checked.

## 10. Security

Alpha should use appropriate technical and organisational measures to protect personal data. The exact measures currently in place have not been established by this limited code inspection.

**Verified security measures and incident contact route:** [REQUIRES ALPHA CONFIRMATION]

No statement in this draft guarantees that a service is completely secure.

## 11. Cookies, local storage, and similar technologies

The inspected Launcher code uses local storage for certain launcher settings and saved account display information. This does not establish whether the Alpha website or Launcher uses cookies, analytics, telemetry, crash reporting, advertising identifiers, or other tracking technologies.

**Complete cookie/local-storage/telemetry inventory and consent requirements:** [REQUIRES ALPHA CONFIRMATION]

If separate cookie or tracking information is required, it will be provided in POL-011.

## 12. Changes to this Privacy Policy

The published policy will display its version and effective date. If the purposes or manner of processing change, Alpha will update this notice and provide any additional information or obtain any consent required by law.

## 13. Complaints

You may contact Alpha using the privacy contact details above. You also have the right to lodge a complaint with the Dutch Data Protection Authority:

**Autoriteit Persoonsgegevens:** https://autoriteitpersoonsgegevens.nl/

## 14. Contact

**Data controller:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]  
**Privacy email:** [REQUIRES ALPHA CONFIRMATION]  
**Postal address:** [REQUIRES ALPHA CONFIRMATION]  
**Country:** The Netherlands

---

## Internal completion checklist — remove before publication

- [ ] Insert controller's full legal identity and valid contact details.
- [ ] Complete an end-to-end data inventory covering Launcher, Client, Supabase, authentication, Microsoft linking, Store, telemetry, logs, and support.
- [ ] Identify each processor, recipient, subprocessor, hosting region, and international transfer.
- [ ] Determine and document a valid legal basis for every processing purpose.
- [ ] Set and implement retention periods, including backups and deletion behavior.
- [ ] Verify user rights workflows and response ownership.
- [ ] Confirm age policy and any parental-consent mechanism.
- [ ] Verify security measures and incident-response procedures.
- [ ] Audit cookies, local storage, analytics, telemetry, and crash reporting.
- [ ] Review whether a record of processing activities, DPIA, or other GDPR measures are required.
- [ ] Obtain Dutch/EU data-protection legal review.
- [ ] Remove all unresolved markers and this internal checklist before publication.
