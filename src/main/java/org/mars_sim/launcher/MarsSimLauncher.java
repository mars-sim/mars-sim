/*
 * Mars-Sim Launcher
 * SPDX-License-Identifier: GPL-3.0-only
 * (c) 2025 Mars-Sim contributors. See LICENSE for details.
 */
package org.mars_sim.launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A small, dependency-free launcher for Mars-Sim.
 * Features:
 *  - Auto-detect runnable Mars-Sim JAR (Swing/Console)
 *  - Java 21+ check and helpful diagnostics
 *  - Memory picker, sim speed picker, extra args
 *  - Safe Mode: disable Java2D OpenGL pipeline for compatibility
 *  - Optional: "Check for updates" (GitHub releases)
 *
 * Usage (CLI, optional):
 *   java -jar mars-sim-launcher.jar [--console|--swing] [--mem 2048] [--speed 512x]
 *                                   [--safe] [--jar /path/to/mars-sim-*.jar] [-- extra args...]
 */
public class MarsSimLauncher {

    // ---------- Entry point ----------
    public static void main(String[] args) {
        // If user passes CLI flags, try to launch without GUI.
        var cli = parseCli(args);
        if (cli != null) {
            // CLI mode
            var log = (Consumer<String>) System.out::println;
            try {
                JavaLocator.checkJava21OrExplain(log);
                var config = cli.toLaunchConfig();
                if (config.jarPath() == null) {
                    var detected = JarDiscovery.autoDetect(config.variant(), Paths.get("").toAbsolutePath());
                    if (detected.isPresent()) config = config.withJar(detected.get());
                    else throw new IllegalStateException("No Mars-Sim runnable JAR found. Use --jar <path>.");
                }
                LaunchService.launch(config, log);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                System.exit(2);
            }
            return;
        }

        // Otherwise, start Swing UI
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LauncherFrame().setVisible(true);
        });
    }

    // ---------- Swing UI ----------
    static class LauncherFrame extends JFrame {
        private final JTextField jarField = new JTextField();
        private final JButton detectBtn = new JButton("Auto-detect");
        private final JButton browseBtn = new JButton("Browse…");
        private final JRadioButton swingBtn = new JRadioButton("Swing (UI)");
        private final JRadioButton consoleBtn = new JRadioButton("Console (headless)");
        private final JSpinner memSpinner = new JSpinner(new SpinnerNumberModel(recommendMemoryMb(), 512, 16384, 128));
        private final JComboBox<String> speedCombo = new JComboBox<>(new String[]{
                "1x","2x","4x","8x","16x","32x","64x","128x","256x","512x","1024x"
        });
        private final JCheckBox safeModeChk = new JCheckBox("Safe Mode (disable Java2D OpenGL)");
        private final JTextField extraArgsField = new JTextField();
        private final JButton launchBtn = new JButton("Launch Mars‑Sim");
        private final JButton checkUpdBtn = new JButton("Check for updates");
        private final JLabel javaLabel = new JLabel();
        private final JTextArea logArea = new JTextArea(12, 80);
        private final ExecutorService ioPool = Executors.newCachedThreadPool();
        private final SettingsManager settings = new SettingsManager();

        LauncherFrame() {
            super("Mars‑Sim Launcher");
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) { ioPool.shutdownNow(); }
            });

            // Load settings
            var s = settings.load();

            var variantGroup = new ButtonGroup();
            swingBtn.setSelected(s.variant.equals("swing"));
            consoleBtn.setSelected(s.variant.equals("console"));
            variantGroup.add(swingBtn); variantGroup.add(consoleBtn);

            jarField.setText(s.jarPath);
            speedCombo.setSelectedItem(Optional.ofNullable(s.speed).orElse("1x"));
            memSpinner.setValue(s.memoryMb > 0 ? s.memoryMb : recommendMemoryMb());
            safeModeChk.setSelected(s.safeMode);
            extraArgsField.setText(Optional.ofNullable(s.extraArgs).orElse(""));

            var panel = new JPanel(new BorderLayout(12, 12));
            panel.setBorder(new EmptyBorder(12,12,12,12));
            panel.add(buildTop(), BorderLayout.NORTH);
            panel.add(buildCenter(), BorderLayout.CENTER);
            panel.add(buildBottom(), BorderLayout.SOUTH);

            setContentPane(panel);
            pack();
            setLocationRelativeTo(null);

            // Populate Java info and try detect JAR
            javaLabel.setText("Java: " + JavaLocator.describe());
            detectBtn.addActionListener(e -> autoDetect());
            browseBtn.addActionListener(e -> browseJar());
            checkUpdBtn.addActionListener(e -> checkUpdates());
            launchBtn.addActionListener(e -> doLaunch());

            // Auto-detect on first run if field empty
            if (jarField.getText().isBlank()) autoDetect();
        }

        private JPanel buildTop() {
            var north = new JPanel(new GridBagLayout());
            var c = new GridBagConstraints();
            c.insets = new Insets(6,6,6,6);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx=0; c.gridy=0; north.add(new JLabel("Variant:"), c);
            var variantPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            variantPanel.add(swingBtn); variantPanel.add(consoleBtn);
            c.gridx=1; c.weightx=1.0; north.add(variantPanel, c);

            c.gridy=1; c.gridx=0; c.weightx=0; north.add(new JLabel("Mars‑Sim JAR:"), c);
            var jarRow = new JPanel(new BorderLayout(6,0));
            jarRow.add(jarField, BorderLayout.CENTER);
            var btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            btns.add(detectBtn); btns.add(browseBtn);
            jarRow.add(btns, BorderLayout.EAST);
            c.gridx=1; c.weightx=1.0; north.add(jarRow, c);

            c.gridy=2; c.gridx=0; c.weightx=0; north.add(new JLabel("Java:"), c);
            c.gridx=1; c.weightx=1.0; north.add(javaLabel, c);
            return north;
        }

        private JPanel buildCenter() {
            var center = new JPanel(new GridBagLayout());
            var c = new GridBagConstraints();
            c.insets = new Insets(6,6,6,6);
            c.fill = GridBagConstraints.HORIZONTAL;

            c.gridx=0; c.gridy=0; center.add(new JLabel("Max Memory (MB):"), c);
            c.gridx=1; c.weightx=1.0; center.add(memSpinner, c);

            c.gridx=0; c.gridy=1; c.weightx=0; center.add(new JLabel("Sim Speed:"), c);
            c.gridx=1; c.weightx=1.0; center.add(speedCombo, c);

            c.gridx=0; c.gridy=2; c.gridwidth=2; center.add(safeModeChk, c);

            c.gridy=3; c.gridwidth=1; c.gridx=0; center.add(new JLabel("Extra Args:"), c);
            c.gridx=1; c.weightx=1.0; center.add(extraArgsField, c);

            return center;
        }

        private JPanel buildBottom() {
            logArea.setEditable(false);
            logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            var scroller = new JScrollPane(logArea);

            var south = new JPanel(new BorderLayout(6,6));
            south.add(scroller, BorderLayout.CENTER);

            var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.add(checkUpdBtn);
            buttons.add(launchBtn);
            south.add(buttons, BorderLayout.SOUTH);

            return south;
        }

        private void log(String msg) {
            SwingUtilities.invokeLater(() -> {
                logArea.append(msg + System.lineSeparator());
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }

        private void autoDetect() {
            try {
                JavaLocator.checkJava21OrExplain(this::log);
                var variant = swingBtn.isSelected() ? Variant.SWING : Variant.CONSOLE;
                var detected = JarDiscovery.autoDetect(variant, Paths.get("").toAbsolutePath());
                if (detected.isPresent()) {
                    jarField.setText(detected.get().toString());
                    log("Auto-detected: " + detected.get());
                } else {
                    log("No runnable Mars‑Sim JAR found. Build the project or browse to a release ZIP/JAR.");
                }
            } catch (Exception ex) {
                log("Detection error: " + ex.getMessage());
            }
        }

        private void browseJar() {
            var fc = new JFileChooser(jarField.getText().isBlank() ? "." : jarField.getText());
            fc.setFileFilter(new FileNameExtensionFilter("Java Archives (*.jar)", "jar"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jarField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }

        private void checkUpdates() {
            log("Checking GitHub releases…");
            CompletableFuture.supplyAsync(UpdateChecker::latestTag, ioPool)
                .orTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
                .whenComplete((tag, err) -> {
                    if (err != null) log("Update check failed: " + err.getMessage());
                    else if (tag == null) log("Could not determine latest version.");
                    else log("Latest release tag: " + tag + "  (see GitHub releases)");
                });
        }

        private void doLaunch() {
            try {
                JavaLocator.checkJava21OrExplain(this::log);

                var jar = Path.of(jarField.getText().trim());
                if (!Files.isRegularFile(jar)) {
                    JOptionPane.showMessageDialog(this, "Select a valid Mars‑Sim JAR.", "JAR not found", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                var variant = swingBtn.isSelected() ? Variant.SWING : Variant.CONSOLE;
                var memMb = ((Number) memSpinner.getValue()).intValue();
                var speed = Objects.toString(speedCombo.getSelectedItem(), "1x");
                var safe = safeModeChk.isSelected();
                var extra = extraArgsField.getText().trim();

                var cfg = new LaunchConfig(variant, jar, memMb, speed, safe, extra);
                // Save settings
                settings.save(Settings.from(cfg));

                log("Launching Mars‑Sim…");
                LaunchService.launch(cfg, this::log);

            } catch (Exception ex) {
                log("Launch failed: " + ex.getMessage());
            }
        }

        private static int recommendMemoryMb() {
            try {
                // Rough recommendation: 1/4 of total physical, within 1024..4096MB
                long total = MemoryUtils.totalSystemMemoryBytes();
                long quarter = total / 4 / (1024 * 1024);
                return (int) Math.max(1024, Math.min(4096, quarter));
            } catch (Throwable t) {
                return 2048;
            }
        }
    }

    // ---------- Domain types ----------
    enum Variant { SWING, CONSOLE }

    record LaunchConfig(Variant variant, Path jarPath, int memoryMb, String speed, boolean safeMode, String extraArgs) {
        LaunchConfig withJar(Path p) { return new LaunchConfig(variant, p, memoryMb, speed, safeMode, extraArgs); }
    }

    // ---------- CLI parsing ----------
    static Cli parseCli(String[] args) {
        if (args == null || args.length == 0) return null;
        var it = new ArrayList<>(List.of(args)).listIterator();
        Variant variant = Variant.SWING;
        Integer mem = null;
        String speed = "1x";
        boolean safe = false;
        Path jar = null;
        List<String> extras = new ArrayList<>();

        boolean afterDashDash = false;
        while (it.hasNext()) {
            var a = it.next();
            if ("--".equals(a)) { afterDashDash = true; continue; }
            if (afterDashDash) { extras.add(a); continue; }
            switch (a) {
                case "--console" -> variant = Variant.CONSOLE;
                case "--swing" -> variant = Variant.SWING;
                case "--safe" -> safe = true;
                case "--mem" -> {
                    if (!it.hasNext()) throw new IllegalArgumentException("--mem requires value in MB");
                    mem = Integer.parseInt(it.next());
                }
                case "--speed" -> {
                    if (!it.hasNext()) throw new IllegalArgumentException("--speed requires value (e.g., 512x)");
                    speed = it.next();
                }
                case "--jar" -> {
                    if (!it.hasNext()) throw new IllegalArgumentException("--jar requires path");
                    jar = Path.of(it.next());
                }
                default -> extras.add(a);
            }
        }
        return new Cli(variant, jar, mem, speed, safe, String.join(" ", extras));
    }

    record Cli(Variant variant, Path jar, Integer mem, String speed, boolean safe, String extras) {
        LaunchConfig toLaunchConfig() {
            return new LaunchConfig(variant, jar, mem == null ? 2048 : mem, speed == null ? "1x" : speed, safe, extras == null ? "" : extras);
        }
    }

    // ---------- LaunchService ----------
    static final class LaunchService {
        static void launch(LaunchConfig cfg, Consumer<String> log) throws IOException {
            Objects.requireNonNull(cfg.jarPath(), "JAR path required");
            var javaExe = JavaLocator.javaExecutable();
            var cmd = new ArrayList<String>();
            cmd.add(javaExe.toString());
            cmd.add("-Xms256m");
            cmd.add("-Xmx" + cfg.memoryMb() + "m");
            if (cfg.safeMode()) {
                // Disable Java2D OpenGL pipeline to avoid driver issues
                cmd.add("-Dsun.java2d.opengl=false");
            }
            if (cfg.variant() == Variant.CONSOLE) {
                cmd.add("-Djava.awt.headless=true");
            }

            // Jar + Mars-Sim args
            cmd.add("-jar");
            cmd.add(cfg.jarPath().toString());

            // If user picked a speed other than 1x, pass it through.
            if (!"1x".equalsIgnoreCase(cfg.speed())) cmd.add(cfg.speed());

            // Extra args (split on whitespace)
            if (!cfg.extraArgs().isBlank()) {
                cmd.addAll(Arrays.stream(cfg.extraArgs().trim().split("\\s+")).toList());
            }

            log.accept(String.join(" ", printable(cmd)));
            var pb = new ProcessBuilder(cmd);
            pb.directory(cfg.jarPath().toAbsolutePath().getParent().toFile());
            pb.redirectErrorStream(true);
            var p = pb.start();

            // Pipe output
            new Thread(() -> pipe(p.getInputStream(), log), "mars-sim-stdout").start();

            // Detach: do not block UI; exit launcher when Mars-Sim exits
            new Thread(() -> {
                try {
                    int code = p.waitFor();
                    log.accept("Mars‑Sim exited with code " + code);
                } catch (InterruptedException ignored) {}
            }, "mars-sim-wait").start();
        }

        private static void pipe(InputStream in, Consumer<String> log) {
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) log.accept(line);
            } catch (IOException ignored) {}
        }

        private static List<String> printable(List<String> cmd) {
            return cmd.stream().map(s -> s.contains(" ") ? "\"" + s + "\"" : s).toList();
        }
    }

    // ---------- JarDiscovery ----------
    static final class JarDiscovery {

        static Optional<Path> autoDetect(Variant variant, Path start) {
            var roots = defaultRoots(start);
            var candidates = new ArrayList<Path>();
            for (var root : roots) {
                candidates.addAll(findJars(root));
            }
            // Prefer variant-specific names, then "main", then anything "mars-sim"
            PredicatePath prefer = switch (variant) {
                case SWING -> name -> contains(name, "swing") || contains(name, "ui");
                case CONSOLE -> name -> contains(name, "console") || contains(name, "headless");
            };
            var pick = pickNewest(candidates.stream()
                    .filter(p -> !contains(p.getFileName().toString(), "launcher")) // don't pick ourselves
                    .filter(p -> contains(p.getFileName().toString(), "mars-sim"))
                    .sorted(JarDiscovery::byMtimeDesc)
                    .toList(), prefer);

            if (pick.isPresent()) return pick;

            // Fallback: "main"
            var main = candidates.stream()
                    .filter(p -> contains(p.getFileName().toString(), "main"))
                    .sorted(JarDiscovery::byMtimeDesc).findFirst();
            if (main.isPresent()) return main;

            // Any mars-sim jar at all
            return candidates.stream()
                    .filter(p -> contains(p.getFileName().toString(), "mars-sim"))
                    .sorted(JarDiscovery::byMtimeDesc).findFirst();
        }

        private static boolean contains(String name, String token) {
            return name.toLowerCase(Locale.ROOT).contains(token);
        }

        private static int byMtimeDesc(Path a, Path b) {
            try {
                FileTime fa = Files.getLastModifiedTime(a);
                FileTime fb = Files.getLastModifiedTime(b);
                return -fa.compareTo(fb);
            } catch (IOException e) { return 0; }
        }

        private static List<Path> findJars(Path root) {
            if (!Files.isDirectory(root)) return List.of();
            try (Stream<Path> s = Files.find(root, 4,
                    (p, attrs) -> attrs.isRegularFile() && p.getFileName().toString().endsWith(".jar"))) {
                return s.collect(Collectors.toList());
            } catch (IOException e) {
                return List.of();
            }
        }

        private static List<Path> defaultRoots(Path start) {
            var roots = new LinkedHashSet<Path>();
            roots.add(start);
            roots.add(start.resolve("target"));
            roots.add(start.resolve("dist"));
            roots.add(start.resolve("build"));

            // Relative to repo layout
            roots.add(start.resolve("mars-sim-dist/target"));
            roots.add(start.resolve("mars-sim-ui/target"));
            roots.add(start.resolve("mars-sim-console/target"));

            // lib folders in zips / extracted bundles
            roots.add(start.resolve("lib"));
            return new ArrayList<>(roots);
        }

        @FunctionalInterface interface PredicatePath { boolean match(String fileNameLower); }

        private static Optional<Path> pickNewest(List<Path> all, PredicatePath prefer) {
            for (var p : all) {
                if (prefer.match(p.getFileName().toString())) return Optional.of(p);
            }
            return Optional.empty();
        }
    }

    // ---------- Settings ----------
    static final class SettingsManager {
        private static final Path FILE = Paths.get(System.getProperty("user.home"), ".mars-sim", "launcher.properties");

        record Settings(String variant, String jarPath, int memoryMb, String speed, boolean safeMode, String extraArgs) {
            static Settings defaults() { return new Settings("swing", "", 2048, "1x", false, ""); }
            static Settings from(LaunchConfig cfg) {
                return new Settings(cfg.variant().name().toLowerCase(Locale.ROOT),
                        cfg.jarPath() == null ? "" : cfg.jarPath().toString(),
                        cfg.memoryMb(), cfg.speed(), cfg.safeMode(), cfg.extraArgs());
            }
        }

        Settings load() {
            try {
                var p = new Properties();
                if (Files.exists(FILE)) {
                    try (var in = Files.newInputStream(FILE)) p.load(in);
                }
                return new Settings(
                        p.getProperty("variant", "swing"),
                        p.getProperty("jarPath", ""),
                        Integer.parseInt(p.getProperty("memoryMb", "2048")),
                        p.getProperty("speed", "1x"),
                        Boolean.parseBoolean(p.getProperty("safeMode", "false")),
                        p.getProperty("extraArgs", "")
                );
            } catch (Exception e) {
                return Settings.defaults();
            }
        }

        void save(Settings s) {
            try {
                Files.createDirectories(FILE.getParent());
                var p = new Properties();
                p.setProperty("variant", s.variant);
                p.setProperty("jarPath", s.jarPath);
                p.setProperty("memoryMb", Integer.toString(s.memoryMb));
                p.setProperty("speed", s.speed);
                p.setProperty("safeMode", Boolean.toString(s.safeMode));
                p.setProperty("extraArgs", s.extraArgs);
                try (var out = Files.newOutputStream(FILE)) {
                    p.store(out, "Mars-Sim Launcher Settings");
                }
            } catch (IOException ignored) {}
        }
    }

    // ---------- Java locator ----------
    static final class JavaLocator {
        static void checkJava21OrExplain(Consumer<String> log) {
            int v = Runtime.version().feature();
            if (v < 21) {
                log.accept("Detected Java " + v + ". Mars‑Sim v3.9.0 requires Java 21+. " +
                           "Please install JRE/JDK 21 and ensure it's first on PATH/JAVA_HOME.");
            }
        }

        static String describe() {
            return System.getProperty("java.runtime.name") + " " +
                   System.getProperty("java.runtime.version") + " (" +
                   System.getProperty("java.vendor") + ")";
        }

        static Path javaExecutable() {
            // 1) The running JVM
            String running = System.getProperty("java.home");
            if (running != null) {
                var p = Paths.get(running, "bin", isWindows() ? "java.exe" : "java");
                if (Files.isExecutable(p)) return p;
            }
            // 2) JAVA_HOME
            String jh = System.getenv("JAVA_HOME");
            if (jh != null) {
                var p = Paths.get(jh, "bin", isWindows() ? "java.exe" : "java");
                if (Files.isExecutable(p)) return p;
            }
            // 3) Fallback to PATH
            return Paths.get(isWindows() ? "java.exe" : "java");
        }

        static boolean isWindows() {
            return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        }
    }

    // ---------- Update checker (optional) ----------
    static final class UpdateChecker {
        static String latestTag() {
            try {
                var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
                var req = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/mars-sim/mars-sim/releases/latest"))
                        .timeout(Duration.ofSeconds(6))
                        .header("Accept", "application/vnd.github+json")
                        .build();
                var res = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() != 200) return null;
                // Minimal JSON parse: look for "tag_name":"v3.9.0"
                Matcher m = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").matcher(res.body());
                return m.find() ? m.group(1) : null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    // ---------- Memory utils ----------
    static final class MemoryUtils {
        static long totalSystemMemoryBytes() {
            try {
                var cl = Class.forName("com.sun.management.OperatingSystemMXBean");
                var os = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
                if (cl.isInstance(os)) {
                    var m = cl.getMethod("getTotalPhysicalMemorySize");
                    m.setAccessible(true);
                    return (long) m.invoke(os);
                }
            } catch (Throwable ignored) {}
            // Fallback guess: 8 GB
            return 8L * 1024 * 1024 * 1024;
        }
    }
}
