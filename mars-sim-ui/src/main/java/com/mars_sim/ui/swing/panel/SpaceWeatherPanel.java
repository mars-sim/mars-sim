package com.mars_sim.ui.swing.panel;

import com.mars_sim.core.environment.spaceweather.SpaceWeatherEvent;
import com.mars_sim.core.environment.spaceweather.SpaceWeatherListener;
import com.mars_sim.core.environment.spaceweather.SpaceWeatherService;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Minimal Swing panel that shows the current space weather state.
 * You can add this to CommanderWindow/Monitor tool without refactoring.
 */
public class SpaceWeatherPanel extends JPanel {

    private final JLabel status = new JLabel("—");
    private final JProgressBar riskBar = new JProgressBar(0, 100);
    private final SpaceWeatherService svc;

    public SpaceWeatherPanel(SpaceWeatherService svc) {
        super(new BorderLayout(8, 8));
        this.svc = svc;
        setBorder(BorderFactory.createTitledBorder("Space Weather"));
        status.setFont(status.getFont().deriveFont(Font.BOLD));
        riskBar.setStringPainted(true);

        add(status, BorderLayout.NORTH);
        add(riskBar, BorderLayout.CENTER);

        svc.addListener(new SpaceWeatherListener() {
            @Override public void onSpaceWeather(SpaceWeatherEvent e) {
                SwingUtilities.invokeLater(() -> update(e));
            }
        });

        // Optional: drive service locally if not wired to master clock yet.
        Timer timer = new Timer("SpaceWeatherPanelSim", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            private Instant t = Instant.now();
            @Override public void run() {
                t = t.plusSeconds(30);
                svc.tick(t, Duration.ofSeconds(30));
            }
        }, 0, 1000);
    }

    private void update(SpaceWeatherEvent e) {
        status.setText(e.getKind() + " / " + e.getSeverity() + "  (ends: " + e.getExpectedEnd() + ")");
        int val = switch (e.getSeverity()) {
            case NONE -> 0; case LOW -> 25; case MODERATE -> 50; case HIGH -> 75; case EXTREME -> 100;
        };
        riskBar.setValue(val);
        riskBar.setString(e.getMessage());
    }
}
