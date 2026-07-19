package com.m1k0.orbitgate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Random;

public class GameEngineTest {

    private static final double ARENA_RADIUS = 100.0;

    @Test
    public void eachPressAlternatesDirectionAndReleaseStopsRotation() {
        GameEngine engine = new GameEngine(new Random(1L));

        engine.startTurning();
        assertEquals(1, engine.getActiveDirection());

        engine.stopTurning();
        assertEquals(0, engine.getActiveDirection());

        engine.startTurning();
        assertEquals(-1, engine.getActiveDirection());

        engine.stopTurning();
        engine.startTurning();
        assertEquals(1, engine.getActiveDirection());
    }

    @Test
    public void dotAlignedWithGapIncreasesScore() {
        GameEngine engine = new GameEngine(new Random(2L));
        engine.addDotForTest(engine.getRingAngle(), 96.0, 5.0, 0.0);

        engine.update(0.016, ARENA_RADIUS);

        assertEquals(1, engine.getScore());
        assertEquals(GameEngine.INITIAL_LIVES, engine.getLives());
        assertTrue(engine.getCollisionEvents().get(0).isSuccess());
    }

    @Test
    public void dotOutsideGapRemovesLife() {
        GameEngine engine = new GameEngine(new Random(3L));
        engine.addDotForTest(engine.getRingAngle() + Math.PI, 96.0, 5.0, 0.0);

        engine.update(0.016, ARENA_RADIUS);

        assertEquals(0, engine.getScore());
        assertEquals(GameEngine.INITIAL_LIVES - 1, engine.getLives());
        assertFalse(engine.getCollisionEvents().get(0).isSuccess());
    }

    @Test
    public void threeMissesEndGame() {
        GameEngine engine = new GameEngine(new Random(4L));

        for (int index = 0; index < GameEngine.INITIAL_LIVES; index++) {
            engine.addDotForTest(engine.getRingAngle() + Math.PI, 96.0, 5.0, 0.0);
            engine.update(0.016, ARENA_RADIUS);
        }

        assertEquals(0, engine.getLives());
        assertFalse(engine.isRunning());
        assertEquals(0, engine.getActiveDirection());
    }

    @Test
    public void eighthSuccessAdvancesToLevelTwo() {
        GameEngine engine = new GameEngine(new Random(5L));

        for (int index = 0; index < 8; index++) {
            engine.addDotForTest(engine.getRingAngle(), 96.0, 5.0, 0.0);
            engine.update(0.016, ARENA_RADIUS);
        }

        assertEquals(8, engine.getScore());
        assertEquals(2, engine.getLevel());
    }

    @Test
    public void angularDistanceHandlesZeroCrossing() {
        double first = Math.toRadians(359.0);
        double second = Math.toRadians(1.0);

        assertEquals(Math.toRadians(2.0), GameEngine.angularDistance(first, second), 0.000001);
    }
}
