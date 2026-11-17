# Alpha Crash Reporter prototype

Package: `com.alpharip.staffmod.AlphaCrashReporter`

## Included
- Explicit throwable capture and an optional JVM uncaught-exception adapter.
- Structured reports with timestamps, versions, source, category, severity, module and environment metadata.
- Local versioned report storage, crash history, deletion and retention.
- Fingerprint-based duplicate grouping and heuristic crash classification.
- Suggested fixes, privacy redaction and plain-text export.
- Standalone Minecraft 1.8.9 history GUI.

## Entry point
```java
AlphaCrashReporter reporter = AlphaCrashReporter.open(reportDirectory, new CrashReporterConfig());
CrashReport report = reporter.capture(error, CrashSource.ALPHA_MODULE, "ExampleModule");
```
Open the optional GUI with `reporter.openScreen(parentScreen)`.

## Prototype boundary
Local-only: no Supabase, network upload, telemetry or account data. The prototype is not registered in AlphaClient menus, keybinds, startup hooks or module events. Capture is explicit unless an integration deliberately installs the optional uncaught-exception adapter. Minecraft/Forge crash-report hooks, module instrumentation, persistent settings and runtime integration remain separate work. Existing `staffops` files and AlphaClient core are not changed.
