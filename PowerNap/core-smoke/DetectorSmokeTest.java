import com.example.powernapwatch.core.*;

public class DetectorSmokeTest {
    public static void main(String[] args) {
        long start = 1_700_000_000_000L;
        final long[] onset = {0};
        SleepOnsetDetector d = new SleepOnsetDetector(new SleepOnsetDetector.Config(), new SleepOnsetDetector.Listener() {
            @Override public void onSnapshot(DetectorSnapshot s) { }
            @Override public void onSleepConfirmed(long estimatedOnsetMs, double confidence) {
                onset[0] = estimatedOnsetMs;
                System.out.println("Detected onset=" + estimatedOnsetMs + " confidence=" + confidence);
            }
        });
        d.start(start);

        double x = 0;
        for (int sec = 0; sec < 240; sec++) {
            long base = start + sec * 1000L;
            double movementAmplitude = sec < 55 ? 0.35 : 0.02;
            for (int i = 0; i < 25; i++) {
                long t = base + i * 40L;
                x += (i % 2 == 0 ? movementAmplitude : -movementAmplitude);
                d.onAcceleration(t, x, 0, 0);
            }
            double hr = sec < 55 ? 70.0 : Math.max(64.0, 70.0 - (sec - 55) * 0.08);
            d.onHeartRate(base + 900, hr);
            double ibi = 60_000.0 / hr + ((sec % 3) - 1) * 12.0;
            d.onIbi(base + 900, ibi);
            if (onset[0] != 0) break;
        }

        if (onset[0] == 0) throw new AssertionError("Sleep onset was not detected in synthetic sleep scenario");
        if (onset[0] < start + 45_000L) throw new AssertionError("Onset detected before baseline was available");
        System.out.println("Smoke test OK");
    }
}
