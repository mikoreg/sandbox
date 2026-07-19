package com.m1k0.orbitgate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Czysta, niezależna od Androida logika proof of concept gry Orbit Gate.
 *
 * <p>Model sterowania:</p>
 * <ul>
 *     <li>naciśnięcie uruchamia obrót,</li>
 *     <li>puszczenie zatrzymuje obrót,</li>
 *     <li>każde kolejne naciśnięcie zmienia kierunek obrotu.</li>
 * </ul>
 */
final class GameEngine {

    static final double TWO_PI = Math.PI * 2.0;
    static final int INITIAL_LIVES = 3;

    private static final double INITIAL_RING_ANGLE = -Math.PI / 2.0;
    private static final double INITIAL_SPAWN_INTERVAL_SECONDS = 1.15;
    private static final double INITIAL_DOT_SPEED = 115.0;
    private static final double INITIAL_GAP_SIZE_RADIANS = 0.48;

    private final Random random;
    private final ArrayList<Dot> dots = new ArrayList<>();
    private final ArrayList<CollisionEvent> collisionEvents = new ArrayList<>();

    private double ringAngle;
    private double angularVelocity;
    private int activeDirection;
    private int nextDirection;
    private int score;
    private int level;
    private int lives;
    private boolean running;
    private double spawnTimerSeconds;
    private double spawnIntervalSeconds;
    private double dotSpeed;
    private double gapSizeRadians;

    GameEngine() {
        this(new Random());
    }

    GameEngine(Random random) {
        this.random = random;
        restart();
    }

    void restart() {
        ringAngle = INITIAL_RING_ANGLE;
        angularVelocity = 0.0;
        activeDirection = 0;
        nextDirection = 1;
        score = 0;
        level = 1;
        lives = INITIAL_LIVES;
        running = true;
        spawnTimerSeconds = 0.0;
        spawnIntervalSeconds = INITIAL_SPAWN_INTERVAL_SECONDS;
        dotSpeed = INITIAL_DOT_SPEED;
        gapSizeRadians = INITIAL_GAP_SIZE_RADIANS;
        dots.clear();
        collisionEvents.clear();
    }

    void startTurning() {
        if (!running) {
            return;
        }
        activeDirection = nextDirection;
        nextDirection *= -1;
    }

    void stopTurning() {
        activeDirection = 0;
    }

    void update(double deltaSeconds, double arenaRadius) {
        collisionEvents.clear();

        if (deltaSeconds <= 0.0 || arenaRadius <= 0.0) {
            return;
        }

        double dt = Math.min(0.033, deltaSeconds);
        double turnSpeed = 3.7 + Math.min(1.8, level * 0.05);
        double targetAngularVelocity = activeDirection * turnSpeed;
        double smoothing = 1.0 - Math.exp(-16.0 * dt);

        angularVelocity += (targetAngularVelocity - angularVelocity) * smoothing;
        ringAngle = normalizeAngle(ringAngle + angularVelocity * dt);

        if (!running) {
            return;
        }

        spawnTimerSeconds -= dt;
        if (spawnTimerSeconds <= 0.0) {
            spawnDot();
            spawnTimerSeconds = spawnIntervalSeconds * (0.82 + random.nextDouble() * 0.36);
        }

        for (int index = dots.size() - 1; index >= 0; index--) {
            Dot dot = dots.get(index);
            dot.distance += dot.speed * dt;

            if (dot.distance + dot.radius >= arenaRadius) {
                boolean success = angularDistance(dot.angle, ringAngle) <= gapSizeRadians / 2.0;
                collisionEvents.add(new CollisionEvent(dot.angle, success));

                if (success) {
                    score++;
                    updateLevelFromScore();
                } else {
                    lives--;
                    if (lives <= 0) {
                        lives = 0;
                        running = false;
                        activeDirection = 0;
                    }
                }
                dots.remove(index);
            }
        }
    }

    private void spawnDot() {
        double angle = random.nextDouble() * TWO_PI;
        double radius = 5.0 + random.nextDouble() * 2.0;
        double speed = dotSpeed * (0.90 + random.nextDouble() * 0.20);
        dots.add(new Dot(angle, 10.0, radius, speed));
    }

    private void updateLevelFromScore() {
        int newLevel = 1 + score / 8;
        if (newLevel == level) {
            return;
        }

        level = newLevel;
        spawnIntervalSeconds = Math.max(
                0.38,
                INITIAL_SPAWN_INTERVAL_SECONDS - (level - 1) * 0.065
        );
        dotSpeed = Math.min(260.0, INITIAL_DOT_SPEED + (level - 1) * 10.0);
        gapSizeRadians = Math.max(
                0.22,
                INITIAL_GAP_SIZE_RADIANS - (level - 1) * 0.018
        );
    }

    static double normalizeAngle(double angle) {
        double normalized = angle % TWO_PI;
        return normalized < 0.0 ? normalized + TWO_PI : normalized;
    }

    static double angularDistance(double first, double second) {
        double distance = Math.abs(normalizeAngle(first) - normalizeAngle(second));
        return Math.min(distance, TWO_PI - distance);
    }

    double getRingAngle() {
        return ringAngle;
    }

    int getActiveDirection() {
        return activeDirection;
    }

    int getScore() {
        return score;
    }

    int getLevel() {
        return level;
    }

    int getLives() {
        return lives;
    }

    boolean isRunning() {
        return running;
    }

    double getGapSizeRadians() {
        return gapSizeRadians;
    }

    List<Dot> getDots() {
        return Collections.unmodifiableList(dots);
    }

    List<CollisionEvent> getCollisionEvents() {
        return Collections.unmodifiableList(collisionEvents);
    }

    /** Metoda pakietowa używana wyłącznie przez testy czystej logiki. */
    void addDotForTest(double angle, double distance, double radius, double speed) {
        dots.add(new Dot(angle, distance, radius, speed));
        spawnTimerSeconds = Double.POSITIVE_INFINITY;
    }

    static final class Dot {
        private final double angle;
        private double distance;
        private final double radius;
        private final double speed;

        Dot(double angle, double distance, double radius, double speed) {
            this.angle = normalizeAngle(angle);
            this.distance = distance;
            this.radius = radius;
            this.speed = speed;
        }

        double getAngle() {
            return angle;
        }

        double getDistance() {
            return distance;
        }

        double getRadius() {
            return radius;
        }
    }

    static final class CollisionEvent {
        private final double angle;
        private final boolean success;

        CollisionEvent(double angle, boolean success) {
            this.angle = angle;
            this.success = success;
        }

        double getAngle() {
            return angle;
        }

        boolean isSuccess() {
            return success;
        }
    }
}
