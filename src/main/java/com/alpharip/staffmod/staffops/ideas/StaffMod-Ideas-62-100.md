# AlphaClient StaffMod Ideas 62–100

Status: proposal / design foundations only. These entries are not implemented runtime features. Keep the numbering and category headings unchanged. Any data or action that the client cannot observe must come from a trusted server/backend and be permission-checked.

## Patrol en crowd (62–67)

62. **Staff Patrol Route** — Save ordered patrol waypoints and let staff step through the route; optional route notes.
63. **Staff Patrol Timer** — Track patrol duration and show configurable reminders or elapsed-time HUD.
64. **Staff Random Player Teleport** — Pick a random eligible online player and offer the permitted server teleport command; never bypass server permissions.
65. **Staff Player Density Heatmap** — Visualize observed player counts by area from available location data; do not fabricate hidden-player positions.
66. **Staff Crowd Alert** — Notify staff when observed player density exceeds a configurable threshold in an area.
67. **Staff Report Hotspot Map** — Plot report locations and frequency when report records contain authorized coordinates.

## Chat moderation (68–78)

68. **Staff Chat Spam Heatmap** — Show message-rate spikes by time window or channel using observed chat events.
69. **Staff Advertising Detector** — Flag likely advertisements, server addresses, or invite links for moderator review; detections are not automatic verdicts.
70. **Staff Link Guard** — Highlight links matching configurable allow/block rules and warn about suspicious destinations.
71. **Staff Toxicity Highlighter** — Highlight potentially abusive language using configurable rules; retain human review and context.
72. **Staff Filter Bypass Detector** — Detect likely obfuscation of configured blocked terms, including spacing and symbol substitutions.
73. **Staff Mute Evasion Detector** — Flag possible evasion only when visible chat events can be compared with trusted mute state.
74. **Staff Social Spy Panel** — Display private-message records only when the server explicitly authorizes access; no client-side interception or permission bypass.
75. **Staff Chat Pin** — Pin selected visible chat messages locally with optional notes.
76. **Staff Channel Switcher** — Switch quickly between staff chat channels the account is authorized to use.
77. **Staff Chat Tab Separator** — Separate configured chat channels into distinct tabs with independent filters.
78. **Staff Rank Chat Highlighter** — Highlight messages by verified rank metadata, falling back to clearly marked text-prefix matching when necessary.

## Spelerprofiel en identiteit (79–87)

79. **Staff Name History Viewer** — Display historical usernames from a trusted history source, including source and timestamp when available.
80. **Staff Account Age Badge** — Show account age only when reliable account creation data is available; otherwise show “Unknown”.
81. **Staff First Join Marker** — Mark a player's first observed join using reliable server join history.
82. **Staff Alt Account Linker** — Present possible account links with supporting evidence and confidence for manual review; similarity alone is not proof.
83. **Staff Ban Evasion Alert** — Flag possible ban evasion by comparing authorized ban and identity records; require staff review.
84. **Staff Watchlist Join Alert** — Notify authorized staff when a watchlisted player is observed joining.
85. **Staff Player Reputation Score** — Summarize documented moderation outcomes using transparent, explainable factors; avoid opaque or permanent judgments.
86. **Staff Client Brand Viewer** — Display the client-brand string only when the connected session/server exposes it; do not treat it as proof of cheating.
87. **Staff Skin Checker** — Check visible skin metadata against configurable policy rules and highlight possible matches for review.

## Content scannen (88–91)

88. **Staff Sign Text Scanner** — Search loaded/visible sign text for configured terms and surface matching signs for review.
89. **Staff Book Content Reader** — Read accessible book pages for moderation review when the game and server permissions allow access.
90. **Staff Anvil Name Scanner** — Flag visible renamed item names that match configured policy rules.
91. **Staff Username Policy Checker** — Check observed usernames against configurable naming rules and explain which rule matched.

## Reports (92–95)

92. **Staff Reporter Reliability Score** — Summarize verified report outcomes using transparent factors and safeguards against retaliation or popularity bias.
93. **Staff False Report Detector** — Flag reports with evidence of abuse or repeated disproven claims for manual review; never auto-punish a reporter solely from a score.
94. **Staff Report Reply Macros** — Provide editable, reusable response templates for common report states, with variables for case details.
95. **Staff Report Auto Teleport** — Offer a teleport action directly from a report when permitted; confirm the target and rely on server-side authorization.

## Quality of life (96–100)

96. **Staff Catch-Up Summary** — Summarize missed staff alerts, unresolved reports, and assigned cases since the last session.
97. **Staff Alert Sound Manager** — Configure sounds, volume, and mute behavior per alert type.
98. **Staff Tray Notification** — Send desktop notifications for selected staff events when supported by the platform and enabled by the user.
99. **Staff Time Zone Converter** — Convert report and case timestamps between selected time zones and show the original timestamp.
100. **Staff Quick Note Overlay** — Provide a compact overlay for drafting and reviewing staff notes, with explicit save/discard controls and appropriate access restrictions.
