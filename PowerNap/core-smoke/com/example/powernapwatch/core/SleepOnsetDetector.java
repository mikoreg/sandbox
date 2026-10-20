package com.example.powernapwatch.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Experimental Wake -> Sleep onset detector.
 *
 * This is deliberately transparent and heuristic. It is meant to be tuned with
 * real CSV sessions from the watch, not treated as a medical sleep classifier.
 */
public final class SleepOnsetDetector {

    public interface Listener {
        void onSnapshot(DetectorSnapshot snapshot);
        void onSleepConfirmed(long estimatedOnsetMs, double confidence);
    }

    public static final class Config {
        public long baselineDurationMs = 45_000;
        public long evaluationStepMs = 5_000;
        public long motionWindowMs = 30_000;
        public long heartWindowMs = 30_000;
        public long hrvWindowMs = 45_000;
        public long candidateConfirmMs = 45_000;
        public long quietFallbackMs = 120_000;

        public double quietMotionRms = 0.18;
        public double veryQuietMotionRms = 0.08;
        public double strongMovementDelta = 1.00;
        public int maxStrongMovements = 1;
        public double candidateThreshold = 0.65;
        public double candidateCancelThreshold = 0.45;
    }

    private record MotionPoint(long t, double delta) {}
    private record HeartPoint(long t, double bpm) {}
    private record IbiPoint(long t, double ms) {}
    private record Vec(long t, double x, double y, double z) {}

    private final Config cfg;
    private final Listener listener;
    private final ArrayDeque<MotionPoint> motion = new ArrayDeque<>();
    private final ArrayDeque<HeartPoint> heart = new ArrayDeque<>();
    private final ArrayDeque<IbiPoint> ibi = new ArrayDeque<>();

    private NapState state = NapState.IDLE;
    private long sessionStartMs;
    private long latestMotionMs;
    private long latestHeartMs;
    private long lastEvaluationMs;
    private long quietSinceMs;
    private long candidateSinceMs;
    private long candidateEstimatedOnsetMs;
    private boolean onBody = true;
    private Vec previousAcceleration;
    private boolean sleepReported;

    public SleepOnsetDetector(Config cfg, Listener listener) {
        this.cfg = cfg;
        this.listener = listener;
    }

    public synchronized void start(long nowMs) {
        motion.clear();
        heart.clear();
        ibi.clear();
        sessionStartMs = nowMs;
        latestMotionMs = 0;
        latestHeartMs = 0;
        lastEvaluationMs = 0;
        quietSinceMs = 0;
        candidateSinceMs = 0;
        candidateEstimatedOnsetMs = 0;
        previousAcceleration = null;
        sleepReported = false;
        onBody = true;
        state = NapState.BASELINE;
    }

    public synchronized void onBodyState(boolean onBody, long timestampMs) {
        this.onBody = onBody;
        if (!onBody) {
            candidateSinceMs = 0;
            candidateEstimatedOnsetMs = 0;
            quietSinceMs = 0;
            if (!sleepReported && state != NapState.BASELINE) {
                state = NapState.WAITING_FOR_SLEEP;
            }
        }
        maybeEvaluate(timestampMs, true);
    }

    public synchronized void onAcceleration(long timestampMs, double x, double y, double z) {
        Vec current = new Vec(timestampMs, x, y, z);
        if (previousAcceleration != null && timestampMs > previousAcceleration.t()) {
            double dx = x - previousAcceleration.x();
            double dy = y - previousAcceleration.y();
            double dz = z - previousAcceleration.z();
            double delta = Math.sqrt(dx * dx + dy * dy + dz * dz);
            motion.addLast(new MotionPoint(timestampMs, delta));
            latestMotionMs = Math.max(latestMotionMs, timestampMs);
        }
        previousAcceleration = current;
        prune(timestampMs);
        maybeEvaluate(timestampMs, false);
    }

    public synchronized void onHeartRate(long timestampMs, double bpm) {
        if (bpm < 30 || bpm > 220) return;
        heart.addLast(new HeartPoint(timestampMs, bpm));
        latestHeartMs = Math.max(latestHeartMs, timestampMs);
        prune(timestampMs);
        maybeEvaluate(timestampMs, false);
    }

    public synchronized void onIbi(long timestampMs, double ibiMs) {
        if (ibiMs < 250 || ibiMs > 2_000) return;
        ibi.addLast(new IbiPoint(timestampMs, ibiMs));
        prune(timestampMs);
    }

    private void prune(long nowMs) {
        long cutoff = nowMs - 240_000;
        while (!motion.isEmpty() && motion.peekFirst().t() < cutoff) motion.removeFirst();
        while (!heart.isEmpty() && heart.peekFirst().t() < cutoff) heart.removeFirst();
        while (!ibi.isEmpty() && ibi.peekFirst().t() < cutoff) ibi.removeFirst();
    }

    private void maybeEvaluate(long hintMs, boolean force) {
        if (sleepReported || state == NapState.IDLE) return;
        if (latestMotionMs == 0 || latestHeartMs == 0) return;

        long evalAt = Math.min(latestMotionMs, latestHeartMs);
        if (!force && lastEvaluationMs != 0 && evalAt - lastEvaluationMs < cfg.evaluationStepMs) return;
        if (evalAt <= lastEvaluationMs) return;
        lastEvaluationMs = evalAt;
        evaluate(evalAt);
    }

    private void evaluate(long t) {
        double baselineHr = percentileHeart(sessionStartMs, Math.min(t, sessionStartMs + 90_000), 0.70);
        double currentHr = medianHeart(t - cfg.heartWindowMs, t);
        double hrSlope = heartSlope(t - 45_000, t);
        double motionRms = motionRms(t - cfg.motionWindowMs, t);
        int strongMoves = strongMovements(t - cfg.motionWindowMs, t, cfg.strongMovementDelta);
        double rmssd = rmssd(t - cfg.hrvWindowMs, t);
        double baselineRmssd = rmssd(sessionStartMs, Math.min(t, sessionStartMs + 90_000));

        boolean enoughHeart = countHeart(t - cfg.heartWindowMs, t) >= 8;
        boolean enoughMotion = countMotion(t - cfg.motionWindowMs, t) >= 30;
        boolean quiet = enoughMotion && motionRms <= cfg.quietMotionRms && strongMoves <= cfg.maxStrongMovements;

        if (quiet && onBody) {
            // The full motion window is already quiet, so backdate the start of
            // sustained immobility to the beginning of that window.
            if (quietSinceMs == 0) quietSinceMs = Math.max(sessionStartMs, t - cfg.motionWindowMs);
        } else {
            quietSinceMs = 0;
        }
        long quietDuration = quietSinceMs == 0 ? 0 : Math.max(0, t - quietSinceMs);

        if (t - sessionStartMs < cfg.baselineDurationMs || !enoughHeart || !enoughMotion) {
            state = NapState.BASELINE;
            emit(t, 0, baselineHr, currentHr, hrSlope, motionRms, rmssd,
                    quietDuration, 0, "collecting baseline");
            return;
        }

        double score = 0.0;
        StringBuilder reason = new StringBuilder();

        if (!onBody) {
            state = NapState.WAITING_FOR_SLEEP;
            emit(t, 0, baselineHr, currentHr, hrSlope, motionRms, rmssd,
                    0, 0, "watch off wrist");
            return;
        }

        if (quiet) {
            if (motionRms <= cfg.veryQuietMotionRms) score += 0.55;
            else score += 0.40;
            reason.append("quiet ");
        }

        if (!Double.isNaN(baselineHr) && !Double.isNaN(currentHr)) {
            double drop = baselineHr - currentHr;
            if (drop >= 3.0 || currentHr <= baselineHr * 0.96) {
                score += 0.25;
                reason.append("HRdrop ");
            } else if (drop >= 1.5) {
                score += 0.12;
                reason.append("HRsoft ");
            }
        }

        if (!Double.isNaN(hrSlope) && hrSlope <= -0.025) {
            score += 0.10;
            reason.append("HRslope ");
        }

        if (!Double.isNaN(rmssd)) {
            score += 0.04; // Valid IBI itself improves confidence slightly.
            if (!Double.isNaN(baselineRmssd) && rmssd >= baselineRmssd * 1.10) {
                score += 0.06;
                reason.append("HRVup ");
            }
        }

        if (quietDuration >= cfg.quietFallbackMs) {
            score += 0.18;
            reason.append("longQuiet ");
        }

        score = Math.min(1.0, score);

        if (state == NapState.SLEEP_CANDIDATE) {
            if (!quiet || score < cfg.candidateCancelThreshold) {
                candidateSinceMs = 0;
                candidateEstimatedOnsetMs = 0;
                state = NapState.WAITING_FOR_SLEEP;
            } else if (t - candidateSinceMs >= cfg.candidateConfirmMs) {
                state = NapState.SLEEP_CONFIRMED;
                sleepReported = true;
                emit(t, score, baselineHr, currentHr, hrSlope, motionRms, rmssd,
                        quietDuration, t - candidateSinceMs, "sleep confirmed " + reason);
                listener.onSleepConfirmed(candidateEstimatedOnsetMs, score);
                return;
            }
        } else if (score >= cfg.candidateThreshold && quiet) {
            state = NapState.SLEEP_CANDIDATE;
            candidateSinceMs = t;
            // Backdate the probable onset. We avoid using the first second of immobility,
            // but also avoid starting later merely because callbacks were batched.
            long fromQuiet = quietSinceMs == 0 ? t : quietSinceMs + 15_000;
            long recent = t - 30_000;
            candidateEstimatedOnsetMs = Math.max(sessionStartMs, Math.max(fromQuiet, recent));
        } else {
            state = NapState.WAITING_FOR_SLEEP;
        }

        long candidateDuration = candidateSinceMs == 0 ? 0 : Math.max(0, t - candidateSinceMs);
        emit(t, score, baselineHr, currentHr, hrSlope, motionRms, rmssd,
                quietDuration, candidateDuration, reason.toString().trim());
    }

    private void emit(long t, double score, double baselineHr, double currentHr,
                      double hrSlope, double motionRms, double rmssd,
                      long quietDuration, long candidateDuration, String reason) {
        listener.onSnapshot(new DetectorSnapshot(
                t, state, score, baselineHr, currentHr, hrSlope, motionRms,
                rmssd, quietDuration, candidateDuration, onBody, reason));
    }

    private int countMotion(long from, long to) {
        int c = 0;
        for (MotionPoint p : motion) if (p.t() >= from && p.t() <= to) c++;
        return c;
    }

    private int countHeart(long from, long to) {
        int c = 0;
        for (HeartPoint p : heart) if (p.t() >= from && p.t() <= to) c++;
        return c;
    }

    private double motionRms(long from, long to) {
        double sumSq = 0;
        int n = 0;
        for (MotionPoint p : motion) {
            if (p.t() >= from && p.t() <= to) {
                sumSq += p.delta() * p.delta();
                n++;
            }
        }
        return n == 0 ? Double.NaN : Math.sqrt(sumSq / n);
    }

    private int strongMovements(long from, long to, double threshold) {
        int n = 0;
        for (MotionPoint p : motion) {
            if (p.t() >= from && p.t() <= to && p.delta() >= threshold) n++;
        }
        return n;
    }

    private double medianHeart(long from, long to) {
        List<Double> values = new ArrayList<>();
        for (HeartPoint p : heart) if (p.t() >= from && p.t() <= to) values.add(p.bpm());
        return percentile(values, 0.50);
    }

    private double percentileHeart(long from, long to, double q) {
        List<Double> values = new ArrayList<>();
        for (HeartPoint p : heart) if (p.t() >= from && p.t() <= to) values.add(p.bpm());
        return percentile(values, q);
    }

    private static double percentile(List<Double> values, double q) {
        if (values.isEmpty()) return Double.NaN;
        Collections.sort(values);
        int index = (int) Math.round((values.size() - 1) * q);
        index = Math.max(0, Math.min(values.size() - 1, index));
        return values.get(index);
    }

    private double heartSlope(long from, long to) {
        List<HeartPoint> points = new ArrayList<>();
        for (HeartPoint p : heart) if (p.t() >= from && p.t() <= to) points.add(p);
        if (points.size() < 5) return Double.NaN;
        points.sort(Comparator.comparingLong(HeartPoint::t));
        double t0 = points.get(0).t();
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        int n = points.size();
        for (HeartPoint p : points) {
            double x = (p.t() - t0) / 1000.0;
            double y = p.bpm();
            sx += x;
            sy += y;
            sxx += x * x;
            sxy += x * y;
        }
        double den = n * sxx - sx * sx;
        return Math.abs(den) < 1e-9 ? Double.NaN : (n * sxy - sx * sy) / den;
    }

    private double rmssd(long from, long to) {
        List<IbiPoint> points = new ArrayList<>();
        for (IbiPoint p : ibi) if (p.t() >= from && p.t() <= to) points.add(p);
        if (points.size() < 5) return Double.NaN;
        points.sort(Comparator.comparingLong(IbiPoint::t));
        double sum = 0;
        int n = 0;
        for (int i = 1; i < points.size(); i++) {
            double d = points.get(i).ms() - points.get(i - 1).ms();
            sum += d * d;
            n++;
        }
        return n == 0 ? Double.NaN : Math.sqrt(sum / n);
    }
}
