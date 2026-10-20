# PowerNapWatch v0.2.4 — AndroidX compile fix (2026-10-20)

## Changes

- `gradle.properties`: `android.useAndroidX=true` (previously `false`).
- `app/build.gradle`: `implementation 'androidx.health:health-services-client:1.1.0'` to make `androidx.health.services.client.*` available to **both** sensor flavors.
- No behavior, UI, manifest, sleep detection, or logging changes were made.

## Important limitation of this archive

The ZIP supplied for v0.2.4 does **not** contain `app/src/main/java/com/example/powernapwatch/detection/WearOsSleepStateDetector.java` (nor `DetectionEngine.java` and `AlarmDecisionPolicy.java`), although these classes are present in the newer local worktree and appears in the reported compiler errors. This patch fixes the missing library import, but the exact local detector implementation is not available for an end-to-end compile or Java API verification.

The original Samsung Health Sensor SDK AAR is proprietary and also absent from this source archive. To build the `samsung` flavor, place the real SDK at `app/libs/samsung-health-sensor-api.aar` and run `./scripts/check-samsung-sdk.sh`.

## Apply to newer local worktree

Rather than replacing the entire project (which would discard local files and edits), apply only the two configuration changes shown above. A minimal unified diff is provided separately as `PowerNapWatch-v0.2.4-androidx-fix.patch`.

## Validation commands on developer Fedora computer

```bash
./scripts/test-core.sh
./scripts/test-feedback.sh
./scripts/check-samsung-sdk.sh
./gradlew :app:dependencies --configuration samsungDebugCompileClasspath
./gradlew :app:assembleSamsungDebug --stacktrace
./gradlew :app:assembleStandardDebug
```

To validate the library specifically:

```bash
./gradlew :app:dependencyInsight \
  --configuration samsungDebugCompileClasspath \
  --dependency health-services-client
```

Expected resolved module: `androidx.health:health-services-client:1.1.0`.

`PassiveMonitoringCapabilities.getSupportedUserActivityStates()` is the device capability query, and `PassiveListenerConfig.Builder.setShouldUserActivityInfoBeRequested(true)` requests activity-state callbacks. `USER_ACTIVITY_ASLEEP` support must be checked on the **actual watch**; having the API on the compile classpath does not guarantee that watch firmware supports it.

## Safe apply on the newer source tree

```bash
cd /work/projects/github.com/mikoreg/sandbox/PowerNap
git apply --check ~/Downloads/PowerNapWatch-v0.2.4-androidx-fix.patch
git apply ~/Downloads/PowerNapWatch-v0.2.4-androidx-fix.patch
./gradlew :app:assembleSamsungDebug
```

Do not overwrite newer project source files with the older v0.2.4 ZIP.
