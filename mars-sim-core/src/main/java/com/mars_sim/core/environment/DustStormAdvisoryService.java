package com.mars_sim.core.environment;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Publishes dust‑storm advisories derived from DustStormForecaster.
 * Typical usage from your weather update loop:
 *
 *   forecaster.update(currentTau, windMS, dtHours);
 *   advisoryService.onTick(forecaster);
 */
public final class DustStormAdvisoryService {

    public enum Severity { BENIGN, CAUTION, NO_EVA }

    public static final class Advisory {
        public final Instant time = Instant.now();
        public final double hazard;   // 0..1
        public final double tau;      // smoothed tau
        public final double windMS;   // smoothed wind
        public final Severity severity;
        public final String message;

        private Advisory(double hazard, double tau, double wind, Severity sev, String msg) {
            this.hazard = hazard; this.tau = tau; this.windMS = wind; this.severity = sev; this.message = msg;
        }

        @Override public String toString() {
            return time + " [" + severity + "] hazard=" + String.format("%.2f", hazard) +
                   " tau=" + String.format("%.2f", tau) + " wind=" + String.format("%.1f", windMS) +
                   " msg=" + message;
        }
    }

    private final List<Consumer<Advisory>> listeners = new CopyOnWriteArrayList<>();
    private Severity lastSeverity = null;

    public void addListener(Consumer<Advisory> l)    { listeners.add(l); }
    public void removeListener(Consumer<Advisory> l) { listeners.remove(l); }

    /** Call this each simulation tick after forecaster.update(...). */
    public void onTick(DustStormForecaster f) {
        Severity sev = f.isAdvisedNoEVA() ? Severity.NO_EVA
                      : (f.isCautionEVA() ? Severity.CAUTION : Severity.BENIGN);
        if (sev != lastSeverity) {
            lastSeverity = sev;
            final String msg;
            switch (sev) {
                case BENIGN:
                    msg = "Dust conditions benign. EVAs permitted.";
                    break;
                case CAUTION:
                    msg = "Dust conditions deteriorating. EVAs with caution; limit duration.";
                    break;
                case NO_EVA:
                default:
                    msg = "Severe dust storm forecast. Avoid EVAs and secure equipment.";
                    break;
            }
            Advisory adv = new Advisory(f.getHazard(), f.getSmoothedTau(), f.getSmoothedWindMS(), sev, msg);
            for (var l : listeners) l.accept(adv);
        }
    }
}
