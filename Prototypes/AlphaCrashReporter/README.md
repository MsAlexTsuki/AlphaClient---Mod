# Alpha Crash Reporter prototype

Package: \`com.alpharip.staffmod.AlphaCrashReporter\`

## Included

- Explicit throwable capture and an optional JVM uncaught-exception adapter.
- Structured reports with timestamps, client/Minecraft/Forge versions, source, category, severity, module and environment metadata.
- Local versioned report storage, history, retention and deletion.
- Fingerprint-based duplicate grouping and heuristic crash classification.
- Suggested fixes, best-effort privacy redaction and plain-text export.
- Minecraft 1.8.9 history and report-details screens, including paged stack-trace output.

## Entry point

\`\`\`java
AlphaCrashReporter reporter = AlphaCrashReporter.open(reportDirectory, new CrashReporterConfig());
reporter.setVersions(alphaClientVersion, "1.8.9", forgeVersion);
CrashReport report = reporter.capture(error, CrashSource.ALPHA_MODULE, "ExampleModule");
\`\`\`

Open the history UI with \`reporter.openScreen(parentScreen)\`.

## Prototype boundary

Local-only: no Supabase, network upload, telemetry or account data. The prototype is not registered in AlphaClient menus, keybinds, startup hooks or module events. Capture is explicit unless an integration deliberately installs the optional uncaught-exception adapter. Minecraft/Forge crash-report hooks, module instrumentation, persistent settings and runtime integration remain separate work. Heuristic suggestions are diagnostic hints, not verified root causes. Privacy filtering is best-effort and must not be treated as a guarantee that every secret is removed.

This folder is a sibling of \`staffops/\`. Existing \`staffops\` files and AlphaClient core are not changed.
