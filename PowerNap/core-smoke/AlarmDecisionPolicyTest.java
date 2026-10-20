import com.example.powernapwatch.core.AlarmDecisionPolicy;

public final class AlarmDecisionPolicyTest {
    public static void main(String[] args) {
        long start = 1_000_000L;
        long nap = 10 * 60_000L;
        long max = 30 * 60_000L;

        var normal = AlarmDecisionPolicy.choose(start, start + 5 * 60_000L,
                start + 6 * 60_000L, nap, max);
        require(normal.selectedAtMs() == start + 15 * 60_000L, "normal +10 min alarm");
        require(AlarmDecisionPolicy.SLEEP_DURATION.equals(normal.reason()), "normal reason");

        var late = AlarmDecisionPolicy.choose(start, start + 25 * 60_000L,
                start + 26 * 60_000L, nap, max);
        require(late.selectedAtMs() == start + 30 * 60_000L, "hard deadline wins");
        require(AlarmDecisionPolicy.HARD_DEADLINE.equals(late.reason()), "hard reason");

        var retrospective = AlarmDecisionPolicy.choose(start, start + 2 * 60_000L,
                start + 20 * 60_000L, nap, max);
        require(retrospective.selectedAtMs() == start + 20 * 60_000L + 2_000L,
                "retrospective sleep wakes immediately");

        var almostHard = AlarmDecisionPolicy.choose(start, start + 1 * 60_000L,
                start + 30 * 60_000L - 1_000L, nap, max);
        require(almostHard.selectedAtMs() == start + 30 * 60_000L,
                "hard deadline caps retrospective re-arm");
        require(AlarmDecisionPolicy.HARD_DEADLINE.equals(almostHard.reason()),
                "capped re-arm uses hard reason");

        System.out.println("AlarmDecisionPolicyTest OK");
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
