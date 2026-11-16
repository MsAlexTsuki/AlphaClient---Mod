# Alpha Ranks and Permissions Policy

**Document ID:** POL-006  
**Version:** 0.1-draft  
**Status:** Draft — not approved for publication  
**Effective date:** Not effective until formally published  
**Last updated:** 16 November 2026  
**Policy owner:** [REQUIRES ALPHA CONFIRMATION]

> **Draft notice:** This document records Alpha's supplied rank and access requirements. It is not proof that every permission is currently implemented correctly. The live configuration and code must be verified before publication.

## 1. Purpose and scope

This policy explains the distinction between community ranks, staff ranks, and feature permissions in Alpha. It applies to Alpha profiles, rank badges, StaffMods, and any Alpha service that displays or enforces rank-based access.

## 2. Rank hierarchy

The project-defined order from lowest to highest is:

1. Member
2. Scout
3. Rogue
4. Elite
5. Alpha
6. Alpha+
7. Beta
8. The Nibbler
9. Media
10. Famous
11. Partner
12. Trial Moderator (T-Mod)
13. Moderator
14. Sr Moderator
15. Admin
16. Sr Admin
17. Platform Admin
18. Manager
19. Developer
20. Owner
21. Founder

The first group through Partner consists of community/donator or special community ranks, not staff ranks. Staff ranks begin at Trial Moderator (T-Mod).

**Beta requirements:** The rank is a beta-tester rank, must be labelled **BETA**, sits directly below The Nibbler in the hierarchy, and must remain separately assignable. It must not be silently merged with another rank.

## 3. Founder and Owner restrictions

Founder is exclusive to the project owner. It is not an ordinary rank that staff can assign to users.

Owner must not receive access to the private Founder folder. Owner access may include the other staff areas allowed by the configured hierarchy, but the Founder-only folder remains excluded.

These restrictions apply to both user-interface visibility and server-side authorization. Hiding a menu item alone is not sufficient access control.

## 4. Highest-rank display and combined permissions

Where Alpha displays one primary community/staff rank, it must display only the highest applicable rank. The display label should be **Alpha Rank: [rank]** where that profile presentation is used.

If a user has multiple assigned ranks, permissions from those assignments may combine according to the permission model. Combining permissions must not cause the interface to display multiple primary ranks where the single-highest-rank display rule applies.

The community profile rank and staff authorization are distinct concepts. A badge or displayed rank must not be treated as proof of every permission.

## 5. StaffMods access

StaffMods is a restricted staff area, not a public category. Access is limited to eligible staff ranks or explicit staff permissions.

The project-defined staff hierarchy is:
- Trial Moderator (T-Mod)
- Moderator
- Sr Moderator
- Admin
- Sr Admin
- Platform Admin
- Manager
- Developer
- Owner
- Founder

General staff modules must be granted according to the configured permission model. Manager and Platform Admin scopes must not be conflated; each retains its defined access boundary.

**Bhop is permitted only in StaffMods → General.** It must be visible/configurable only when the account has the required Bhop permission. It must not appear as a public Movement module or grant an Active Alpha/Bhop HUD to users without the required permission.

## 6. Rank assignment and permission administration

Rank assignments and individual permission grants must be made only by authorized staff or trusted server-side processes. Each assignment or change should have an accountable actor, timestamp, and reason where supported by the system.

Users must not be able to self-assign privileged ranks, Founder status, or staff permissions by editing profile fields or client-side data.

**Exact rank-management roles, approval rules, and appeal route:** [REQUIRES ALPHA CONFIRMATION]

## 7. Rank benefits and availability

A rank label does not by itself promise a particular cosmetic, module, service, or benefit. Benefits must be documented separately and checked against the current implementation.

Alpha must not advertise a rank benefit as available until it has been verified in the released product.

## 8. Errors, revocation, and disputes

If a rank or permission appears incorrect, users should report the account name, displayed rank, affected feature, and relevant context through the official support route.

Alpha may correct an erroneous assignment or permission where authorized. Any review or appeal process must be defined before being promised.

**Support and rank-dispute procedure:** [REQUIRES ALPHA CONFIRMATION]

## 9. Changes

Changes to rank order, rank meaning, Founder exclusivity, Owner boundaries, or staff access are controlled policy changes. They must be approved by the policy owner and reflected consistently in the application, backend authorization, and this document.

## 10. Related policies

- POL-001 — Terms of Service
- POL-003 — Acceptable Use Policy
- POL-005 — Staff Code of Conduct
- POL-002 — Privacy Policy

## Internal verification checklist — remove before publication

- [ ] Compare the full hierarchy with current rank definitions and UI labels.
- [ ] Verify Beta remains assignable, labelled BETA, and ordered directly below The Nibbler.
- [ ] Verify Founder exclusivity in server-side logic and assignment tools.
- [ ] Verify Owner cannot access the Founder folder through UI, API, storage, or direct requests.
- [ ] Verify only the highest rank is displayed where required while permissions combine as specified.
- [ ] Verify StaffMods eligibility and Manager/Platform Admin boundaries.
- [ ] Verify Bhop exists only in StaffMods → General and is permission-gated.
- [ ] Verify rank assignment audit and appeal procedures.
- [ ] Remove unresolved markers and obtain owner/legal approval before publication.
