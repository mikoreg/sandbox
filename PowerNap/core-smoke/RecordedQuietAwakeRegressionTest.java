import com.example.powernapwatch.core.*;
import java.io.*;
import java.nio.file.*;

public class RecordedQuietAwakeRegressionTest {
    private static boolean replay(Path path) throws Exception {
        final boolean[] detected = {false};
        SleepOnsetDetector detector = new SleepOnsetDetector(
                new SleepOnsetDetector.Config(),
                new SleepOnsetDetector.Listener() {
                    @Override public void onSnapshot(DetectorSnapshot s) { }
                    @Override public void onSleepConfirmed(long onset, double confidence) {
                        detected[0] = true;
                    }
                });

        boolean started = false;
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] a = line.split(",", 16);
                if (a.length < 16 || a[2].isEmpty()) continue;
                long t = Long.parseLong(a[2]);
                if (!started) {
                    detector.start(t);
                    started = true;
                }
                switch (a[1]) {
                    case "ACC" -> detector.onAcceleration(t,
                            Double.parseDouble(a[5]), Double.parseDouble(a[6]), Double.parseDouble(a[7]));
                    case "HR" -> {
                        if (!a[8].isEmpty()) detector.onHeartRate(t, Double.parseDouble(a[8]));
                    }
                    case "IBI" -> {
                        if (!a[9].isEmpty()) detector.onIbi(t, Double.parseDouble(a[9]));
                    }
                    case "EVENT" -> {
                        if (a[15].contains("onBody=true")) detector.onBodyState(true, t);
                        else if (a[15].contains("onBody=false")) detector.onBodyState(false, t);
                    }
                }
            }
        }
        return detected[0];
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("CSV paths required");
        for (String arg : args) {
            Path path = Path.of(arg);
            if (replay(path)) {
                throw new AssertionError("Recorded quiet-awake session was falsely detected as sleep: " + path);
            }
            System.out.println("Quiet-awake regression OK: " + path.getFileName());
        }
    }
}
