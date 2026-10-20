# PowerNap v0.4.2-version-ui

Small UI/diagnostic release based on v0.4.1.

## Changes

- Bumped app version to `0.4.2-version-ui` (`versionCode 7`).
- Kept the compact version label directly below the `POWER NAP` title.
- Added a second version footer at the bottom of the start screen.
- Both labels are populated from the actually installed APK package metadata.
- Footer includes both `versionName` (including flavor suffix such as `-samsung`) and build/version code.

Example:

`PowerNap 0.4.2-version-ui-samsung • build 7`

No detector, alarm, sensor, logging, or session semantics were changed in this release.
