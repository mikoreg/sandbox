import com.example.powernapwatch.core.*;

public class DetectorNoSleepSmokeTest {
    public static void main(String[] args) {
        long start = 1_700_000_000_000L;
        final boolean[] detected = {false};
        SleepOnsetDetector d = new SleepOnsetDetector(new SleepOnsetDetector.Config(), new SleepOnsetDetector.Listener() {
            @Override public void onSnapshot(DetectorSnapshot s) { }
            @Override public void onSleepConfirmed(long estimatedOnsetMs, double confidence) { detected[0] = true; }
        });
        d.start(start);
        double x = 0;
        for (int sec = 0; sec < 240; sec++) {
            long base = start + sec * 1000L;
            for (int i = 0; i < 25; i++) {
                long t = base + i * 40L;
                double a = (sec % 10 < 8) ? 0.45 : 0.08;
                x += (i % 2 == 0 ? a : -a);
                d.onAcceleration(t, x, 0, 0);
            }
            d.onHeartRate(base + 900, 70.0 + (sec % 5) * 0.2);
            d.onIbi(base + 900, 857 + ((sec % 3) - 1) * 8);
        }
        if (detected[0]) throw new AssertionError("False sleep detection during active synthetic session");
        System.out.println("No-sleep smoke test OK");
    }
}
