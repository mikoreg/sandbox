package com.m1k0.orbitgate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

/**
 * Natywny widok Canvas realizujący rysowanie, dotyk i pętlę animacji.
 */
final class GameView extends View {

    private static final int BACKGROUND_COLOR = Color.rgb(5, 7, 11);
    private static final int ACCENT_COLOR = Color.rgb(86, 230, 255);
    private static final int RING_COLOR = Color.rgb(244, 247, 251);
    private static final int ERROR_COLOR = Color.rgb(255, 77, 120);

    private final GameEngine engine = new GameEngine();
    private final Random random = new Random();
    private final ArrayList<Particle> particles = new ArrayList<>();

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect textBounds = new Rect();

    private final Vibrator vibrator;

    private boolean animationEnabled;
    private long previousFrameNanos;
    private float centerX;
    private float centerY;
    private float arenaRadius;
    private float density;

    GameView(Context context) {
        super(context);
        setBackgroundColor(BACKGROUND_COLOR);
        setFocusable(true);
        setClickable(true);
        setKeepScreenOn(true);

        density = getResources().getDisplayMetrics().density;
        vibrator = findVibrator(context);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        ringPaint.setColor(RING_COLOR);

        gapPaint.setStyle(Paint.Style.STROKE);
        gapPaint.setStrokeCap(Paint.Cap.ROUND);
        gapPaint.setColor(ACCENT_COLOR);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(ACCENT_COLOR);

        particlePaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        ));
        textPaint.setTextAlign(Paint.Align.LEFT);

        centerTextPaint.setColor(Color.WHITE);
        centerTextPaint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
        ));
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    void resume() {
        animationEnabled = true;
        previousFrameNanos = 0L;
        postInvalidateOnAnimation();
    }

    void pause() {
        animationEnabled = false;
        previousFrameNanos = 0L;
        engine.stopTurning();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        centerX = width / 2.0f;
        centerY = height / 2.0f;
        arenaRadius = Math.max(dp(80.0f), Math.min(width, height) * 0.38f);

        float ringStroke = Math.max(dp(7.0f), arenaRadius * 0.045f);
        ringPaint.setStrokeWidth(ringStroke);
        gapPaint.setStrokeWidth(dp(3.0f));
        textPaint.setTextSize(Math.max(dp(11.0f), Math.min(width, height) * 0.038f));
        centerTextPaint.setTextSize(Math.max(dp(18.0f), Math.min(width, height) * 0.065f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long nowNanos = System.nanoTime();
        double deltaSeconds = previousFrameNanos == 0L
                ? 0.0
                : (nowNanos - previousFrameNanos) / 1_000_000_000.0;
        previousFrameNanos = nowNanos;

        if (animationEnabled) {
            engine.update(deltaSeconds, arenaRadius);
            handleCollisionEvents();
            updateParticles(Math.min(0.033, Math.max(0.0, deltaSeconds)));
        }

        drawRing(canvas);
        drawCore(canvas);
        drawDots(canvas);
        drawParticles(canvas);
        drawHud(canvas);

        if (animationEnabled) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!engine.isRunning()) {
                    engine.restart();
                    particles.clear();
                    performClick();
                    invalidate();
                    return true;
                }

                engine.startTurning();
                vibrate(8L);
                performClick();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                engine.stopTurning();
                return true;

            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void drawRing(Canvas canvas) {
        float startDegrees = radiansToDegrees(
                engine.getRingAngle() + engine.getGapSizeRadians() / 2.0
        );
        float sweepDegrees = 360.0f - radiansToDegrees(engine.getGapSizeRadians());

        canvas.drawArc(
                centerX - arenaRadius,
                centerY - arenaRadius,
                centerX + arenaRadius,
                centerY + arenaRadius,
                startDegrees,
                sweepDegrees,
                false,
                ringPaint
        );

        float gapStartDegrees = radiansToDegrees(
                engine.getRingAngle() - engine.getGapSizeRadians() / 2.0
        );
        canvas.drawArc(
                centerX - arenaRadius,
                centerY - arenaRadius,
                centerX + arenaRadius,
                centerY + arenaRadius,
                gapStartDegrees,
                radiansToDegrees(engine.getGapSizeRadians()),
                false,
                gapPaint
        );
    }

    private void drawCore(Canvas canvas) {
        double pulse = 1.0 + Math.sin(System.nanoTime() / 1_000_000_000.0 * 6.0) * 0.08;
        dotPaint.setColor(ACCENT_COLOR);
        canvas.drawCircle(centerX, centerY, (float) (dp(7.0f) * pulse), dotPaint);
    }

    private void drawDots(Canvas canvas) {
        dotPaint.setColor(ACCENT_COLOR);
        for (GameEngine.Dot dot : engine.getDots()) {
            float x = centerX + (float) (Math.cos(dot.getAngle()) * dot.getDistance());
            float y = centerY + (float) (Math.sin(dot.getAngle()) * dot.getDistance());
            canvas.drawCircle(x, y, (float) dot.getRadius(), dotPaint);
        }
    }

    private void drawParticles(Canvas canvas) {
        for (Particle particle : particles) {
            float alpha = Math.max(0.0f, 1.0f - particle.age / particle.life);
            particlePaint.setColor(particle.color);
            particlePaint.setAlpha(Math.round(alpha * 255.0f));
            canvas.drawCircle(particle.x, particle.y, dp(1.5f), particlePaint);
        }
        particlePaint.setAlpha(255);
    }

    private void drawHud(Canvas canvas) {
        float margin = dp(14.0f);
        String scoreText = String.format(Locale.ROOT, "WYNIK %d", engine.getScore());
        String levelText = String.format(Locale.ROOT, "POZIOM %d", engine.getLevel());

        canvas.drawText(scoreText, margin, margin + textPaint.getTextSize(), textPaint);

        textPaint.getTextBounds(levelText, 0, levelText.length(), textBounds);
        canvas.drawText(
                levelText,
                getWidth() - margin - textBounds.width(),
                margin + textPaint.getTextSize(),
                textPaint
        );

        if (!engine.isRunning()) {
            drawCenteredMultilineText(
                    canvas,
                    "KONIEC GRY",
                    "wynik: " + engine.getScore() + " · dotknij, aby zacząć ponownie"
            );
        } else if (engine.getLives() < GameEngine.INITIAL_LIVES) {
            String livesText = buildLivesText(engine.getLives());
            centerTextPaint.setTextSize(Math.max(dp(13.0f), getWidth() * 0.040f));
            centerTextPaint.setAlpha(190);
            canvas.drawText(livesText, centerX, centerY + arenaRadius * 0.58f, centerTextPaint);
            centerTextPaint.setAlpha(255);
            centerTextPaint.setTextSize(Math.max(dp(18.0f), Math.min(getWidth(), getHeight()) * 0.065f));
        }
    }

    private static String buildLivesText(int lives) {
        StringBuilder text = new StringBuilder("ŻYCIA: ");
        for (int index = 0; index < lives; index++) {
            text.append('●');
        }
        return text.toString();
    }

    private void drawCenteredMultilineText(Canvas canvas, String title, String subtitle) {
        centerTextPaint.setTextSize(Math.max(dp(18.0f), getWidth() * 0.060f));
        canvas.drawText(title, centerX, centerY - dp(8.0f), centerTextPaint);

        centerTextPaint.setTextSize(Math.max(dp(9.0f), getWidth() * 0.029f));
        centerTextPaint.setAlpha(200);
        canvas.drawText(subtitle, centerX, centerY + dp(17.0f), centerTextPaint);
        centerTextPaint.setAlpha(255);
    }

    private void handleCollisionEvents() {
        for (GameEngine.CollisionEvent event : engine.getCollisionEvents()) {
            float x = centerX + (float) (Math.cos(event.getAngle()) * arenaRadius);
            float y = centerY + (float) (Math.sin(event.getAngle()) * arenaRadius);
            createBurst(x, y, event.isSuccess() ? ACCENT_COLOR : ERROR_COLOR,
                    event.isSuccess() ? 10 : 18);
            if (!event.isSuccess()) {
                vibrate(35L);
            }
        }
    }

    private void createBurst(float x, float y, int color, int count) {
        for (int index = 0; index < count; index++) {
            double angle = random.nextDouble() * GameEngine.TWO_PI;
            float speed = 30.0f + random.nextFloat() * 90.0f;
            particles.add(new Particle(
                    x,
                    y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    0.5f + random.nextFloat() * 0.35f,
                    color
            ));
        }
    }

    private void updateParticles(double deltaSeconds) {
        float dt = (float) deltaSeconds;
        for (int index = particles.size() - 1; index >= 0; index--) {
            Particle particle = particles.get(index);
            particle.age += dt;
            particle.x += particle.velocityX * dt;
            particle.y += particle.velocityY * dt;
            float damping = (float) Math.pow(0.08, dt);
            particle.velocityX *= damping;
            particle.velocityY *= damping;

            if (particle.age >= particle.life) {
                particles.remove(index);
            }
        }
    }

    private void vibrate(long durationMillis) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(
                    durationMillis,
                    VibrationEffect.DEFAULT_AMPLITUDE
            ));
        } else {
            vibrator.vibrate(durationMillis);
        }
    }

    private static Vibrator findVibrator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = context.getSystemService(VibratorManager.class);
            return manager == null ? null : manager.getDefaultVibrator();
        }
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private float dp(float value) {
        return value * density;
    }

    private static float radiansToDegrees(double radians) {
        return (float) Math.toDegrees(radians);
    }

    private static final class Particle {
        private float x;
        private float y;
        private float velocityX;
        private float velocityY;
        private final float life;
        private float age;
        private final int color;

        private Particle(
                float x,
                float y,
                float velocityX,
                float velocityY,
                float life,
                int color
        ) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.life = life;
            this.color = color;
        }
    }
}
