# Alpha Data Retention and Privacy Requests Procedure

**Document ID:** POL-012  
**Version:** 0.1-draft  
**Status:** Draft — operational procedure not approved  
**Effective date:** Not effective until formally adopted  
**Last updated:** 16 November 2026  
**Procedure owner:** [REQUIRES ALPHA CONFIRMATION]

> **Draft notice:** This is an internal operational procedure supporting POL-002. It is not a published retention schedule. No retention periods are invented in this document.

## 1. Purpose

This procedure sets out how Alpha should define retention periods and handle requests from people exercising rights under applicable data-protection law.

It must be implemented by the person responsible for Alpha's data processing before Alpha claims that requests are routinely handled or that records are deleted on a particular schedule.

## 2. Responsibilities

Alpha must designate:
- a person responsible for receiving privacy requests;
- a person authorized to assess and approve responses;
- an administrator able to locate and export relevant records;
- an authorized person able to correct, restrict, or delete data where appropriate.

**Named role holders and backup contact:** [REQUIRES ALPHA CONFIRMATION]

Access to request records must be limited to people who need it to handle the request.

## 3. Retention schedule

For each category of personal data, Alpha must record:
- the purpose for which it is held;
- the system and storage location;
- the controller's lawful basis;
- the retention period or objective criteria used to determine it;
- the event that starts the retention period;
- the deletion, anonymisation, or archival method;
- any legal hold or other reason that may require retention;
- how backups and third-party copies are handled.

The schedule must cover at least:
- Alpha Auth accounts and authentication records;
- profiles and avatar files;
- friend requests, friendships, and private messages;
- Minecraft UUID links and active sessions;
- online/staff presence;
- Pixel Garden saves, progression, and reset tokens;
- ranks, permissions, and verification records;
- staff operations, activity, player notes, and private staff notes;
- administrative and security audit records;
- local Launcher storage and any confirmed external-provider records;
- Store/order records, only if a real transaction system exists.

**Approved retention periods and criteria:** [REQUIRES ALPHA CONFIRMATION]

A database timestamp, session expiry, or storage lifecycle setting is not automatically a complete retention rule. Do not state that data is deleted immediately unless deletion from primary systems, replicas, backups, and processors has been verified.

## 4. Receiving a privacy request

Requests may include access, rectification, erasure, restriction, objection, portability, or withdrawal of consent. Alpha must provide a clear contact route.

**Privacy request email or form:** [REQUIRES ALPHA CONFIRMATION]

On receipt, the responsible person should:
1. record the date received and the request type;
2. acknowledge receipt where appropriate;
3. identify the relevant Alpha account or data subject;
4. clarify the request only where necessary;
5. verify identity proportionately, without collecting excessive new information;
6. identify the systems and providers likely to hold relevant data;
7. assess the request under applicable law;
8. record the decision, action, and response date.

## 5. Identity verification

Verification must be proportionate to the sensitivity and risk of the requested data. Do not request a password, authentication code, or full payment-card details. Do not require more identity information than is reasonably necessary.

**Approved verification methods:** [REQUIRES ALPHA CONFIRMATION]

## 6. Locating and exporting data

The response process must account for data held in Supabase Auth, database tables, Storage, Edge Functions or logs, Launcher local storage, and any confirmed third-party provider.

Searches must be limited to authorized personnel. Do not expose another person's private messages or personal data when preparing an access response. Where records contain multiple people's information, assess the rights of all affected people.

**Approved export format, secure delivery method, and system-owner checklist:** [REQUIRES ALPHA CONFIRMATION]

## 7. Assessing and completing requests

The responsible person must assess the request against the GDPR and other applicable law. Rights are not absolute. A request may be restricted or refused only where a valid legal exception applies, and the reason must be documented.

Where a request is accepted:
- correct inaccurate data in the relevant source system;
- delete data where erasure is legally required and technically appropriate;
- restrict processing where required;
- stop consent-based processing after valid withdrawal where no other lawful basis applies;
- notify relevant recipients where legally required;
- verify the result and record any systems or backups that cannot be changed immediately.

Do not promise that a request will always result in deletion of every record. Some records may need to be retained for legal obligations, claims, security, or other lawful purposes.

## 8. Time limits and response

Under the GDPR, requests generally require a response within one month, with extensions possible in legally permitted circumstances. The responsible person must calculate the applicable deadline from the actual receipt date and communicate any permitted extension and its reasons within the required time.

**Internal target for triage and approval:** [REQUIRES ALPHA CONFIRMATION]

## 9. Record of requests

Keep a limited record of each request, including receipt date, request category, identity-verification outcome, systems checked, decision, actions, response date, and reason for any refusal or extension.

The request log itself contains personal data and must have a defined purpose, access restriction, and retention period.

**Request-log location and retention:** [REQUIRES ALPHA CONFIRMATION]

## 10. Data breaches and urgent cases

If a request reveals an unauthorized disclosure, lost device, exposed credential, or other personal-data breach, escalate it through the incident process. The controller must assess applicable GDPR notification duties and deadlines.

**Incident escalation contact:** [REQUIRES ALPHA CONFIRMATION]

## 11. Related policies

- POL-002 — Privacy Policy
- POL-009 — Security and Vulnerability Disclosure Policy
- POL-010 — Service Availability and Third-Party Services Policy

## Internal adoption checklist — do not claim this procedure is operational until complete

- [ ] Appoint the request owner and backup.
- [ ] Publish a working privacy request contact route.
- [ ] Approve proportionate identity-verification methods.
- [ ] Create a system-by-system data retrieval and deletion runbook.
- [ ] Set retention periods/criteria for every category and backup.
- [ ] Define secure export and response delivery.
- [ ] Create a restricted request log and its retention rule.
- [ ] Define escalation for personal-data breaches.
- [ ] Test the complete access, correction, restriction, and erasure workflows.
- [ ] Obtain Dutch/EU privacy legal review and remove unresolved markers.
