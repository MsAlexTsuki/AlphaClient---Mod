# Alpha Cookie and Tracking Notice

**Document ID:** POL-011  
**Version:** 0.1-draft  
**Status:** Draft — applicability and technical inventory not verified  
**Effective date:** Not effective until formally published  
**Last updated:** 16 November 2026  
**Operator:** [FULL LEGAL NAME — REQUIRES ALPHA CONFIRMATION]

> **Draft notice:** The technical review identified Launcher local-storage use for certain settings and saved Microsoft-account display information. It did not complete an inventory of browser cookies, web storage, analytics, telemetry, crash reporting, advertising technologies, or all third-party SDKs. This document must not be presented as a complete tracking notice yet.

## 1. Purpose and scope

This notice is intended to explain cookies and similar technologies used by Alpha-operated websites, Launcher components, and other Alpha services where applicable.

A cookie or similar technology may store information on a device or access information already stored there. Legal requirements depend on the technology, purpose, and service context.

## 2. What has been identified so far

The Launcher source inspected for the policy work uses local storage for certain launcher settings and saved Microsoft-account display information.

This is not, by itself, proof that Alpha uses advertising cookies or analytics tracking. The inspection did not establish the complete inventory of:
- browser cookies;
- local storage and session storage keys;
- embedded webview storage;
- analytics or product-usage events;
- crash-reporting and diagnostic SDKs;
- advertising identifiers or cross-site tracking;
- third-party scripts, pixels, or SDKs.

**Complete technical inventory:** [REQUIRES ALPHA CONFIRMATION]

## 3. Categories and purposes

The final notice must list each actual technology, its provider, purpose, data accessed or stored, duration, and whether it is strictly necessary or optional.

| Category | Intended description | Current status |
|---|---|---|
| Strictly necessary storage | Authentication, security, session continuity, or a feature explicitly requested by the user | Exact keys/providers and legal assessment not verified |
| Preference storage | Remembering user-selected settings | Launcher local settings storage identified; exact inventory required |
| Analytics | Measuring usage or performance | Not established by the inspection |
| Diagnostics | Crash reports and technical error information | Not established by the inspection |
| Advertising/tracking | Advertising measurement or cross-service tracking | Not established by the inspection |

This table is a checklist of categories to assess, not a statement that every category is currently used.

## 4. Consent and user controls

Where consent is legally required, optional technologies must not be activated before valid consent. Users must be able to refuse or withdraw consent as easily as they can give it. Strictly necessary technologies must be limited to what is required for the requested service.

**Consent mechanism, preference controls, and withdrawal route:** [REQUIRES ALPHA CONFIRMATION]

Do not add a consent banner or claim consent is collected until the actual implementation and configuration have been checked.

## 5. Third-party technologies

If a third party supplies analytics, diagnostics, embedded content, or other technologies, Alpha must identify the provider, explain its role, and link to the relevant privacy information. Any personal-data processing must also be reflected in POL-002.

**Provider and transfer inventory:** [REQUIRES ALPHA CONFIRMATION]

## 6. How to manage stored information

The final notice should explain how users can inspect, clear, or control relevant storage in the Launcher, browser, or operating system, including any effect on saved settings or sign-in state.

**Verified user instructions for each supported platform:** [REQUIRES ALPHA CONFIRMATION]

## 7. Changes and contact

The published notice will show its version and effective date. Material changes to tracking technologies or purposes must be reflected in the notice and consent mechanism where required.

**Privacy contact:** [REQUIRES ALPHA CONFIRMATION]

## 8. Related policies

- POL-002 — Privacy Policy
- POL-001 — Terms of Service

## Internal technical completion checklist — remove before publication

- [ ] Inspect the full Launcher code and packaged dependencies for cookies, web storage, SDKs, analytics, diagnostics, and embedded webviews.
- [ ] Inspect Alpha Client and any Alpha-operated websites for equivalent technologies.
- [ ] Inspect production configuration and network behavior; source search alone is insufficient.
- [ ] Record each provider, purpose, key, duration, and data category.
- [ ] Determine which technologies require consent under Dutch/EU rules.
- [ ] Verify consent/refusal/withdrawal behavior and test that optional technologies remain off until consent.
- [ ] Update POL-002 for any personal-data processing and international transfers.
- [ ] Obtain legal review and remove unresolved markers before publication.
