Samsung flavor requires the REAL Samsung Health Sensor SDK library.

Download Samsung Health Sensor SDK v1.4.1 (or newer compatible release) from:
https://developer.samsung.com/health/sensor/overview.html

Copy the official file here as:

    app/libs/samsung-health-sensor-api.aar

IMPORTANT: do not use a generated compile-time stub. A stub can make the project
compile while connectService()/setEventListener() are no-ops, which causes the
Samsung engine to time out at runtime.

Before building samsungDebug, run:

    ./scripts/check-samsung-sdk.sh

The standard flavor does not require this file.
