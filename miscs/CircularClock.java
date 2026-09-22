import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalTime;

/**
 * Jednoplikowy, okrągły zegar-widget Swing.
 *
 * Uruchomienie (Java 11+):
 *   java CircularClock.java
 *
 * Sterowanie:
 *   - przeciąganie LPM: przesuwa zegar
 *   - F5: zmniejsza zegar
 *   - F6: powiększa zegar
 *   - F7: zwiększa przezroczystość (zmniejsza krycie)
 *   - F8: szybkie przełączenie krycia 100% <-> 82%
 *   - F9: zmniejsza przezroczystość (zwiększa krycie)
 *   - + / - oraz strzałki pozostają jako skróty dodatkowe
 *   - Esc / Ctrl+Q / Cmd+Q / Alt+F4: zamyka
 *
 * Opcje:
 *   - -v, --verbose: szczegółowy log diagnostyczny
 *   - -h, --help: pomoc
 *
 * Renderer:
 *   - geometria double (bez zaokrąglania końców wskazówek do pikseli)
 *   - VALUE_STROKE_PURE (subpixel positioning)
 *   - fractional font metrics
 *   - supersampling 2x + bicubic downsampling
 *   - pełne buforowanie klatki ograniczające migotanie shaped-window na Linuxie
 */
public class CircularClock {

    private static final int DEFAULT_SIZE = 360;
    private static final int MIN_SIZE = 180;
    private static final int MAX_SIZE = 800;
    private static final int SIZE_STEP = 30;

    // Około 60 FPS. Rzeczywista pozycja wskazówek zawsze pochodzi z dokładnego czasu.
    private static final int FRAME_DELAY_MS = 16;

    // 2x supersampling znacząco redukuje "schodkowanie" i skoki między pikselami.
    private static final int SUPERSAMPLE = 2;

    private static final float QUICK_TRANSLUCENT_OPACITY = 0.82f;
    private static final float OPACITY_STEP = 0.05f;
    private static final float MIN_OPACITY = 0.25f;

    private static boolean VERBOSE = false;

    public static void main(String[] args) {
        if (!parseArguments(args)) {
            return;
        }
        /*
         * Workaround dla części linuksowych backendów AWT.
         * Musi być ustawiony przed inicjalizacją Toolkit.
         */
        if (System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.setProperty("sun.awt.noerasebackground", "true");
        }

        SwingUtilities.invokeLater(CircularClock::createAndShow);
    }

    private static boolean parseArguments(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case "-v", "--verbose" -> VERBOSE = true;
                case "-h", "--help" -> {
                    printUsage();
                    return false;
                }
                default -> {
                    System.err.println("Nieznana opcja: " + arg);
                    printUsage();
                    return false;
                }
            }
        }
        return true;
    }

    private static void printUsage() {
        System.out.println("""
                Użycie:
                  java CircularClock.java [opcje]

                Opcje:
                  -v, --verbose   szczegółowy log diagnostyczny
                  -h, --help      pokaż tę pomoc

                Klawisze:
                  F5              zmniejsz zegar
                  F6              powiększ zegar
                  F7              zwiększ przezroczystość
                  F8              przełącz krycie 100% <-> 82%
                  F9              zmniejsz przezroczystość
                  Esc / Ctrl+Q    zamknij
                """);
    }

    private static void log(String format, Object... args) {
        if (VERBOSE) {
            System.out.printf("[CircularClock] " + format + "%n", args);
        }
    }

    private static void createAndShow() {
        JFrame frame = new JFrame("Circular Clock");
        log("Java: %s", System.getProperty("java.version"));
        log("OS: %s %s", System.getProperty("os.name"), System.getProperty("os.version"));
        log("AWT toolkit: %s", Toolkit.getDefaultToolkit().getClass().getName());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setResizable(false);
        frame.setSize(DEFAULT_SIZE, DEFAULT_SIZE);

        // Bez per-pixel translucency. Kształt koła realizuje Window#setShape().
        frame.setBackground(new Color(24, 28, 34));

        if (frame.isAlwaysOnTopSupported()) {
            frame.setAlwaysOnTop(true);
        }

        ClockPanel clock = new ClockPanel();
        frame.setContentPane(clock);

        installCloseShortcuts(clock, frame);
        installOpacityShortcuts(clock, frame);
        installResizeShortcuts(clock, frame);
        installDragging(clock, frame);
        installVerboseKeyDiagnostics(frame);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        applyCircularShape(frame);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyCircularShape(frame);
                clock.invalidateBuffersAndRender();
            }
        });

        SwingUtilities.invokeLater(clock::requestFocusInWindow);

        if (VERBOSE) {
            printControls();
            logGraphicsCapabilities(frame);
        }
    }

    private static void printControls() {
        System.out.println("""
                CircularClock:
                  F5                 zmniejsz
                  F6                 powiększ
                  F7                 większa przezroczystość
                  F8                 100% <-> 82%
                  F9                 mniejsza przezroczystość
                  + / -              dodatkowo: zmiana rozmiaru
                  Up / PageUp        dodatkowo: większe krycie
                  Down / PageDown    dodatkowo: większa przezroczystość
                  Esc / Ctrl+Q       zamknij
                  LPM + drag         przesuń
                """);
    }

    private static void logGraphicsCapabilities(JFrame frame) {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        if (gc == null) {
            log("Brak GraphicsConfiguration");
            return;
        }
        GraphicsDevice gd = gc.getDevice();
        log("GraphicsDevice: %s", gd.getIDstring());
        log("TRANSLUCENT: %s", gd.isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.TRANSLUCENT));
        log("PERPIXEL_TRANSPARENT: %s", gd.isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT));
        log("PERPIXEL_TRANSLUCENT: %s", gd.isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT));
        log("Initial size: %dx%d", frame.getWidth(), frame.getHeight());
        log("Initial opacity: %.2f", frame.getOpacity());
    }

    private static void installVerboseKeyDiagnostics(JFrame frame) {
        if (!VERBOSE) {
            return;
        }

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(event -> {
                    if (event.getID() == KeyEvent.KEY_PRESSED) {
                        Window active = KeyboardFocusManager
                                .getCurrentKeyboardFocusManager()
                                .getActiveWindow();
                        log(
                                "KEY_PRESSED code=%d text=%s modifiers=%s activeWindow=%s",
                                event.getKeyCode(),
                                KeyEvent.getKeyText(event.getKeyCode()),
                                KeyEvent.getModifiersExText(event.getModifiersEx()),
                                active == frame ? "clock" : String.valueOf(active)
                        );
                    }
                    return false;
                });
    }

    private static void applyCircularShape(Window window) {
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        if (gc == null) {
            return;
        }

        GraphicsDevice gd = gc.getDevice();
        if (!gd.isWindowTranslucencySupported(
                GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            return;
        }

        try {
            window.setShape(new Ellipse2D.Double(
                    0.0,
                    0.0,
                    window.getWidth(),
                    window.getHeight()
            ));
            log("Shape applied: %dx%d", window.getWidth(), window.getHeight());
        } catch (UnsupportedOperationException ex) {
            System.err.println("Shaped windows are not supported: " + ex.getMessage());
        }
    }

    private static void installCloseShortcuts(JComponent component, JFrame frame) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        inputMap.put(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_Q,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
                ),
                "close"
        );
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK),
                "close"
        );

        actionMap.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
    }

    private static void installResizeShortcuts(
            JComponent component,
            JFrame frame
    ) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        // Główne, jednoznaczne skróty funkcyjne.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "sizeDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "sizeUp");

        // Dodatkowo: główny blok klawiatury + numpad.
        inputMap.put(KeyStroke.getKeyStroke('+'), "sizeUp");
        inputMap.put(KeyStroke.getKeyStroke('-'), "sizeDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "sizeUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "sizeDown");

        // Typowy "+" jako Shift + "=".
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.SHIFT_DOWN_MASK),
                "sizeUp"
        );
        inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0),
                "sizeDown"
        );

        actionMap.put("sizeUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log("Action sizeUp");
                resizeAroundCenter(frame, frame.getWidth() + SIZE_STEP);
            }
        });

        actionMap.put("sizeDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log("Action sizeDown");
                resizeAroundCenter(frame, frame.getWidth() - SIZE_STEP);
            }
        });
    }

    private static void resizeAroundCenter(JFrame frame, int requestedSize) {
        int newSize = Math.max(MIN_SIZE, Math.min(MAX_SIZE, requestedSize));
        if (newSize == frame.getWidth()) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        Rectangle oldBounds = frame.getBounds();
        double cx = oldBounds.getCenterX();
        double cy = oldBounds.getCenterY();

        int newX = (int) Math.round(cx - newSize / 2.0);
        int newY = (int) Math.round(cy - newSize / 2.0);

        frame.setBounds(newX, newY, newSize, newSize);
        applyCircularShape(frame);

        log("Rozmiar: %d px", newSize);
    }

    private static void installOpacityShortcuts(
            JComponent component,
            JFrame frame
    ) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), "opacityDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "toggleOpacity");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "opacityUp");

        // Dodatkowe skróty.
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "opacityUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "opacityUp");

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "opacityDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "opacityDown");

        actionMap.put("toggleOpacity", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log("Action toggleOpacity");
                float current = frame.getOpacity();
                float target = current >= 0.99f
                        ? QUICK_TRANSLUCENT_OPACITY
                        : 1.0f;
                setWindowOpacity(frame, target);
            }
        });

        actionMap.put("opacityUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log("Action opacityUp");
                setWindowOpacity(
                        frame,
                        Math.min(1.0f, frame.getOpacity() + OPACITY_STEP)
                );
            }
        });

        actionMap.put("opacityDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log("Action opacityDown");
                setWindowOpacity(
                        frame,
                        Math.max(MIN_OPACITY, frame.getOpacity() - OPACITY_STEP)
                );
            }
        });
    }

    private static void setWindowOpacity(JFrame frame, float opacity) {
        GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        if (gc == null) {
            return;
        }

        GraphicsDevice gd = gc.getDevice();

        if (opacity < 0.999f
                && !gd.isWindowTranslucencySupported(
                        GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
            Toolkit.getDefaultToolkit().beep();
            System.out.println("System nie obsługuje jednolitej przezroczystości okna.");
            return;
        }

        try {
            float clamped = Math.max(MIN_OPACITY, Math.min(1.0f, opacity));
            frame.setOpacity(clamped);

            log(
                    "Krycie: %d%% (przezroczystość: %d%%)",
                    Math.round(clamped * 100.0f),
                    Math.round((1.0f - clamped) * 100.0f)
            );
        } catch (IllegalComponentStateException | UnsupportedOperationException ex) {
            Toolkit.getDefaultToolkit().beep();
            System.err.println("Nie można zmienić opacity: " + ex.getMessage());
        }
    }

    private static void installDragging(JComponent component, JFrame frame) {
        MouseAdapter dragging = new MouseAdapter() {
            private Point pressedOnScreen;
            private Point windowLocation;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressedOnScreen = e.getLocationOnScreen();
                    windowLocation = frame.getLocation();
                    component.requestFocusInWindow();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (pressedOnScreen == null || windowLocation == null) {
                    return;
                }

                Point current = e.getLocationOnScreen();

                int dx = current.x - pressedOnScreen.x;
                int dy = current.y - pressedOnScreen.y;

                frame.setLocation(
                        windowLocation.x + dx,
                        windowLocation.y + dy
                );
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressedOnScreen = null;
                windowLocation = null;
            }
        };

        component.addMouseListener(dragging);
        component.addMouseMotionListener(dragging);
    }

    private static final class ClockPanel extends JPanel {

        private static final Color FACE_CENTER = new Color(43, 48, 57);
        private static final Color FACE_EDGE = new Color(24, 28, 34);

        private static final Color OUTER_RING = new Color(95, 103, 116);
        private static final Color MAJOR_TICK = new Color(224, 228, 233);
        private static final Color MINOR_TICK = new Color(119, 127, 140);

        private static final Color HOUR_HAND = new Color(242, 244, 247);
        private static final Color MINUTE_HAND = new Color(210, 216, 223);
        private static final Color SECOND_HAND = new Color(225, 93, 82);

        private static final Color DIGITAL_BG = new Color(11, 14, 18, 145);
        private static final Color DIGITAL_MAIN = new Color(242, 244, 247);
        private static final Color DIGITAL_SEC = new Color(164, 172, 184);

        private static final Font DIGITAL_FONT =
                new Font(Font.MONOSPACED, Font.PLAIN, 25);

        private final Timer timer;

        /*
         * staticFace i renderBuffer są w rozdzielczości 2x.
         * displayBuffer ma rzeczywisty rozmiar okna.
         */
        private BufferedImage staticFace;
        private BufferedImage renderBuffer;
        private BufferedImage displayBuffer;

        private int bufferWidth = -1;
        private int bufferHeight = -1;

        ClockPanel() {
            setOpaque(true);
            setBackground(FACE_EDGE);
            setDoubleBuffered(true);
            setFocusable(true);

            timer = new Timer(FRAME_DELAY_MS, e -> {
                renderNextFrame();
                repaint();
            });

            timer.setCoalesce(true);
            timer.setInitialDelay(0);
            timer.start();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            renderNextFrame();
        }

        @Override
        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        void invalidateBuffersAndRender() {
            bufferWidth = -1;
            bufferHeight = -1;
            staticFace = null;
            renderBuffer = null;
            displayBuffer = null;

            renderNextFrame();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            BufferedImage image = displayBuffer;
            if (image != null) {
                graphics.drawImage(image, 0, 0, null);
            }

            /*
             * Przy części backendów X11 pomaga utrzymać stabilny shaped-window.
             * Nie wpływa na geometrię wskazówek, która jest już gotowa w buforze.
             */
            Toolkit.getDefaultToolkit().sync();
        }

        private void ensureBuffers() {
            int w = getWidth();
            int h = getHeight();

            if (w <= 0 || h <= 0) {
                return;
            }

            if (w == bufferWidth
                    && h == bufferHeight
                    && staticFace != null
                    && renderBuffer != null
                    && displayBuffer != null) {
                return;
            }

            bufferWidth = w;
            bufferHeight = h;

            int rw = w * SUPERSAMPLE;
            int rh = h * SUPERSAMPLE;

            staticFace = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_RGB);
            renderBuffer = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_RGB);
            displayBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

            renderStaticFace();
        }

        private void renderStaticFace() {
            if (staticFace == null) {
                return;
            }

            Graphics2D g = staticFace.createGraphics();

            try {
                configureGraphics(g);

                // Od tej chwili rysujemy we współrzędnych logicznych okna.
                g.scale(SUPERSAMPLE, SUPERSAMPLE);

                int w = bufferWidth;
                int h = bufferHeight;

                g.setColor(FACE_EDGE);
                g.fill(new Rectangle2D.Double(0, 0, w, h));

                double scale = Math.min(w, h) / (double) DEFAULT_SIZE;
                double cx = w / 2.0;
                double cy = h / 2.0;
                double radius = Math.min(w, h) / 2.0 - 8.0 * scale;

                paintFace(g, cx, cy, radius, scale);
                paintTicks(g, cx, cy, radius, scale);
            } finally {
                g.dispose();
            }
        }

        private void renderNextFrame() {
            ensureBuffers();

            if (staticFace == null
                    || renderBuffer == null
                    || displayBuffer == null) {
                return;
            }

            Graphics2D g = renderBuffer.createGraphics();

            try {
                configureGraphics(g);

                // Statyczna tarcza jest już w rozdzielczości supersamplingu.
                g.drawImage(staticFace, 0, 0, null);

                // Elementy dynamiczne rysujemy w logicznych współrzędnych.
                g.scale(SUPERSAMPLE, SUPERSAMPLE);

                int w = bufferWidth;
                int h = bufferHeight;

                double scale = Math.min(w, h) / (double) DEFAULT_SIZE;
                double cx = w / 2.0;
                double cy = h / 2.0;
                double radius = Math.min(w, h) / 2.0 - 8.0 * scale;

                LocalTime now = LocalTime.now();

                /*
                 * Nanosekundy zachowujemy w double.
                 * Nic nie jest zaokrąglane do pełnej sekundy/minuty/piksela.
                 */
                double seconds =
                        now.getSecond()
                                + now.getNano() / 1_000_000_000.0;

                double minutes =
                        now.getMinute()
                                + seconds / 60.0;

                double hours =
                        (now.getHour() % 12)
                                + minutes / 60.0;

                paintDigitalTime(g, cx, cy, now, scale);

                paintHand(
                        g,
                        cx,
                        cy,
                        hours * 30.0 - 90.0,
                        radius * 0.48,
                        8.0 * scale,
                        HOUR_HAND
                );

                paintHand(
                        g,
                        cx,
                        cy,
                        minutes * 6.0 - 90.0,
                        radius * 0.68,
                        5.0 * scale,
                        MINUTE_HAND
                );

                paintSecondHand(
                        g,
                        cx,
                        cy,
                        seconds * 6.0 - 90.0,
                        radius * 0.76,
                        scale
                );

                paintCenterCap(g, cx, cy, scale);

            } finally {
                g.dispose();
            }

            downsampleFrame();
        }

        private void downsampleFrame() {
            Graphics2D g = displayBuffer.createGraphics();

            try {
                configureGraphics(g);

                g.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC
                );

                g.drawImage(
                        renderBuffer,
                        0,
                        0,
                        bufferWidth,
                        bufferHeight,
                        null
                );
            } finally {
                g.dispose();
            }
        }

        private static void configureGraphics(Graphics2D g) {
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            g.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            g.setRenderingHint(
                    RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON
            );

            /*
             * Kluczowe dla płynności geometrii:
             * Java2D nie "dociąga" współrzędnych linii do siatki pikseli.
             */
            g.setRenderingHint(
                    RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE
            );

            g.setRenderingHint(
                    RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
            );

            g.setRenderingHint(
                    RenderingHints.KEY_COLOR_RENDERING,
                    RenderingHints.VALUE_COLOR_RENDER_QUALITY
            );
        }

        private static void paintFace(
                Graphics2D g,
                double cx,
                double cy,
                double radius,
                double scale
        ) {
            RadialGradientPaint face = new RadialGradientPaint(
                    new Point2D.Double(cx, cy),
                    (float) radius,
                    new float[]{0.0f, 1.0f},
                    new Color[]{FACE_CENTER, FACE_EDGE}
            );

            g.setPaint(face);

            g.fill(new Ellipse2D.Double(
                    cx - radius,
                    cy - radius,
                    radius * 2.0,
                    radius * 2.0
            ));

            g.setColor(OUTER_RING);
            g.setStroke(new BasicStroke(
                    (float) (2.0 * scale),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            double inset = 1.5 * scale;

            g.draw(new Ellipse2D.Double(
                    cx - radius + inset,
                    cy - radius + inset,
                    radius * 2.0 - inset * 2.0,
                    radius * 2.0 - inset * 2.0
            ));
        }

        private static void paintTicks(
                Graphics2D g,
                double cx,
                double cy,
                double radius,
                double scale
        ) {
            for (int i = 0; i < 60; i++) {
                boolean major = i % 5 == 0;

                double angle = Math.toRadians(i * 6.0 - 90.0);

                double outer = radius - 11.0 * scale;
                double inner =
                        outer - (major ? 14.0 : 6.0) * scale;

                double x1 = cx + Math.cos(angle) * inner;
                double y1 = cy + Math.sin(angle) * inner;

                double x2 = cx + Math.cos(angle) * outer;
                double y2 = cy + Math.sin(angle) * outer;

                g.setColor(major ? MAJOR_TICK : MINOR_TICK);

                g.setStroke(new BasicStroke(
                        (float) ((major ? 2.6 : 1.0) * scale),
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                ));

                // Bez konwersji do int — pełna geometria subpikselowa.
                g.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }

        private static void paintDigitalTime(
                Graphics2D g,
                double cx,
                double cy,
                LocalTime now,
                double scale
        ) {
            double boxWidth = 136.0 * scale;
            double boxHeight = 43.0 * scale;
            double corner = 18.0 * scale;

            double x = cx - boxWidth / 2.0;
            double y = cy + 63.0 * scale;

            g.setColor(DIGITAL_BG);

            g.fill(new RoundRectangle2D.Double(
                    x,
                    y,
                    boxWidth,
                    boxHeight,
                    corner,
                    corner
            ));

            String main =
                    "%02d:%02d".formatted(
                            now.getHour(),
                            now.getMinute()
                    );

            String seconds =
                    "%02d".formatted(now.getSecond());

            Font mainFont =
                    DIGITAL_FONT.deriveFont((float) (25.0 * scale));

            Font secondsFont =
                    DIGITAL_FONT.deriveFont((float) (12.0 * scale));

            FontRenderContext frc = g.getFontRenderContext();

            Rectangle2D mainBounds =
                    mainFont.getStringBounds(main, frc);

            Rectangle2D secondsBounds =
                    secondsFont.getStringBounds(seconds, frc);

            double gap = 7.0 * scale;

            double totalWidth =
                    mainBounds.getWidth()
                            + gap
                            + secondsBounds.getWidth();

            double startX =
                    cx - totalWidth / 2.0;

            /*
             * Font bounds pozwalają zachować pozycjonowanie w double,
             * zamiast zaokrąglać całe wyrażenie przez FontMetrics/int.
             */
            double baseline =
                    y
                            + (boxHeight - mainBounds.getHeight()) / 2.0
                            - mainBounds.getY();

            g.setFont(mainFont);
            g.setColor(DIGITAL_MAIN);
            g.drawString(
                    main,
                    (float) startX,
                    (float) baseline
            );

            g.setFont(secondsFont);
            g.setColor(DIGITAL_SEC);
            g.drawString(
                    seconds,
                    (float) (
                            startX
                                    + mainBounds.getWidth()
                                    + gap
                    ),
                    (float) (baseline - 1.0 * scale)
            );
        }

        private static void paintHand(
                Graphics2D g,
                double cx,
                double cy,
                double angleDegrees,
                double length,
                double strokeWidth,
                Color color
        ) {
            double angle = Math.toRadians(angleDegrees);

            double x =
                    cx + Math.cos(angle) * length;

            double y =
                    cy + Math.sin(angle) * length;

            g.setColor(color);

            g.setStroke(new BasicStroke(
                    (float) strokeWidth,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            // Line2D.Double zachowuje ułamkowe współrzędne końców.
            g.draw(new Line2D.Double(cx, cy, x, y));
        }

        private static void paintSecondHand(
                Graphics2D g,
                double cx,
                double cy,
                double angleDegrees,
                double length,
                double scale
        ) {
            double angle = Math.toRadians(angleDegrees);

            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double x = cx + cos * length;
            double y = cy + sin * length;

            double tail = 23.0 * scale;

            double tailX = cx - cos * tail;
            double tailY = cy - sin * tail;

            g.setColor(SECOND_HAND);

            g.setStroke(new BasicStroke(
                    (float) (2.0 * scale),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));

            // Tu również zero zaokrągleń do int.
            g.draw(new Line2D.Double(
                    tailX,
                    tailY,
                    x,
                    y
            ));
        }

        private static void paintCenterCap(
                Graphics2D g,
                double cx,
                double cy,
                double scale
        ) {
            double outer = 7.0 * scale;
            double inner = 3.5 * scale;

            g.setColor(FACE_EDGE);

            g.fill(new Ellipse2D.Double(
                    cx - outer,
                    cy - outer,
                    outer * 2.0,
                    outer * 2.0
            ));

            g.setColor(SECOND_HAND);

            g.fill(new Ellipse2D.Double(
                    cx - inner,
                    cy - inner,
                    inner * 2.0,
                    inner * 2.0
            ));
        }
    }
}
