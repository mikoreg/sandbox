package com.example.powernapwatch.util;

/** An EVENT-compatible FEEDBACK row in the existing 16-column CSV format. */
public final class FeedbackCsvRow {
    private FeedbackCsvRow() {}

    public static String format(long seq, long nowMs, boolean slept, String state) {
        String[] cells = new String[16];
        java.util.Arrays.fill(cells, "");
        cells[0] = Long.toString(seq);
        cells[1] = "FEEDBACK";
        cells[2] = Long.toString(nowMs);
        cells[3] = Long.toString(nowMs);
        cells[4] = "0";
        cells[11] = quoted(state);
        cells[15] = quoted("user_reported_sleep=" + (slept ? "YES" : "NO"));
        return String.join(",", cells) + "\n";
    }

    private static String quoted(String input) {
        return "\"" + (input == null ? "" : input.replace("\"", "\"\"")) + "\"";
    }
}
