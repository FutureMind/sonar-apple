# Changelog

## Unreleased (Future Mind fork, based on upstream 0.5.1)

### Compatibility
- Built against SonarQube plugin API 13.7 (SonarQube Community Build 26.6), Java 17.
- Moved off deprecated plugin APIs (old `Loggers`, `onQualifiers`).
- Plugin declares `swift,objc` as required languages, so other projects do not download it.

### Merged from other forks and upstream PRs
- Upstream PR #107 (alindaj6): accept mobsfscan exit code 1; tolerate missing `_values` in xcresult JSON.
- alindaj6: `sonar.apple.periphery.skip`.
- atikomtancharoen: `sonar.apple.periphery.projectPath` and `sonar.apple.periphery.schemes`.
- vethx: import pre-generated reports (`sonar.apple.*.reportPaths`), refactored into one shared reader.

### Fixes
- Swift parser no longer crashes on a postfix operator at end of file (`x!` without trailing newline).
- An issue on a line outside the file is kept on the file instead of aborting the sensor (upstream #94/#105).
- Highlighting and duplication ranges are correct in files with emoji or a UTF-8 BOM.
- Tool-reported paths are matched through symlinks (`/tmp` vs `/private/tmp` on macOS).
- Tools run in the project base directory.
- Clear errors when a tool is missing, times out or exits with an error.
- Xcode warnings sensor no longer runs `xcresulttool` when there is no result bundle.
- OCLint exit code 5 (thresholds exceeded) no longer drops all OCLint issues.

### Rules
- SwiftLint rules synced with SwiftLint 0.65.1: 29 added, 6 removed.

### Build
- Dependency updates (jackson 2.22, guava 33.6, commons-io 2.22, analyzer-commons 2.30...).
- Test-only libraries no longer bundled; plugin jar 14 MB -> 11 MB.
- GitHub Actions: CI on push/PR, release with the jar attached on version tags.
