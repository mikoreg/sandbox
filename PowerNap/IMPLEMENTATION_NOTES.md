# Implementation notes

## Design baseline: Galaxy Watch4

The Samsung flavor is intentionally based on the capabilities common to Galaxy Watch4+: continuous heart rate with IBI plus continuous accelerometer. PPG is not used in v0.1.

## Why Samsung data are explicitly flushed

Samsung Health Sensor SDK batches continuous samples after the display turns off. Power Nap cannot wait for the normal batch interval, so `SamsungHealthSensorSource` calls `flush()` on HR and accelerometer trackers every 10 seconds while waiting for sleep.

The detector processes the timestamps belonging to the samples and estimates the onset retrospectively. Therefore the timer is not `callback time + 10 minutes`; it is `estimated sleep onset + 10 minutes`.

## Detector features in v0.1

* delta-vector wrist movement derived from accelerometer samples,
* 30-second motion RMS,
* current median HR versus awake baseline,
* HR linear slope,
* IBI-derived RMSSD when Samsung IBI is available,
* prolonged immobility fallback,
* candidate/confirmation hysteresis.

The values are starting heuristics only. They should be tuned against labelled Watch4 CSV sessions.

## Deliberately excluded for now

* N1/N2/N3/REM classification,
* raw PPG,
* Samsung Health sleep-history integration,
* phone companion app,
* cloud storage,
* ML model.

These are intentionally deferred until real Watch4 sensor traces show where the heuristic fails.

## Distribution limitation

Samsung Health Sensor SDK works for development with Health Sensor Service developer mode. Shipping an app that uses the Samsung SDK requires Samsung partner approval and registration of the final package name and signing certificate. Keep the current `com.example.powernapwatch` package only for prototyping.
