import com.example.powernapwatch.util.FeedbackCsvRow;

public final class FeedbackCsvRowTest {
    public static void main(String[] args) {
        for (boolean slept : new boolean[]{false, true}) {
            String row = FeedbackCsvRow.format(42L, 123456L, slept, "FINISHED");
            String[] cells = row.trim().split(",", -1);
            if (cells.length != 16) throw new AssertionError("Expected 16 columns, got " + cells.length);
            if (!"42".equals(cells[0])) throw new AssertionError("Sequence missing");
            if (!"FEEDBACK".equals(cells[1])) throw new AssertionError("Missing FEEDBACK type");
            if (!"123456".equals(cells[2]) || !"123456".equals(cells[3])) throw new AssertionError("Bad timestamp");
            if (!"\"FINISHED\"".equals(cells[11])) throw new AssertionError("Bad state: " + cells[11]);
            String expected = "\"user_reported_sleep=" + (slept ? "YES" : "NO") + "\"";
            if (!expected.equals(cells[15])) throw new AssertionError("Unexpected label " + cells[15]);
        }
        System.out.println("Feedback CSV schema test OK");
    }
}
