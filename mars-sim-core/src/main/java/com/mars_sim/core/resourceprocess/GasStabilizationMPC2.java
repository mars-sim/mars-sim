/*
 * Mars Simulation Project
 * GasStabilizationMPC2.java
 * @date 2026-09-22
 * @author Manny Kung
 */

package com.mars_sim.core.resourceprocess;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mixed-Integer Stochastic MPC for greenhouse/chemical gas stabilization.
 * Controls: O2, CO2, H2O, H2, CH4, CH3OH within safe/target ranges.
 *
 * Stoichiometry used:
 *   Photosynthesis:   CO2 + H2O → O2 + (CH2O)
 *   Methanation:      CO2 + 4H2 → CH4 + 2H2O
 *   Methanol synth:   CO2 + 3H2 → CH3OH + H2O
 *   Electrolysis:     2H2O → 2H2 + O2
 */
public class GasStabilizationMPC2 {

    // ─── Gas species ──────────────────────────────────────────────────────
    public enum Gas { O2, CO2, H2O, H2, CH4, CH3OH }

    public static class GasBounds {
        public final double target;
        public final double min;   // lower safe bound (vol%)
        public final double max;   // upper safe bound (vol%)

        public GasBounds(double target, double min, double max) {
            this.target = target; this.min = min; this.max = max;
        }
    }

    // ─── Process units ────────────────────────────────────────────────────
    public enum Process {
        BIOREACTOR,           // Photosynthesis: consumes CO2+H2O, produces O2
        CO2_INJECTOR,         // Injects CO2 from tank
        WATER_EVAPORATOR,     // Adds H2O vapor
        HYDROGEN_ELECTROLYZER,// Electrolysis: 2H2O → 2H2 + O2
        METHANER,             // CO2 + 4H2 → CH4 + 2H2O
        METHANOL_SYNTHESIZER  // CO2 + 3H2 → CH3OH + H2O
    }

    public static class ProcessEffect {
        public final double[] dGas; // per-step Δ(vol%) when ON
        public final double energyCost;

        public ProcessEffect(double o2, double co2, double h2o, double h2,
                             double ch4, double ch3oh, double cost) {
            this.dGas = new double[]{o2, co2, h2o, h2, ch4, ch3oh};
            this.energyCost = cost;
        }
    }

    // ─── Configuration ────────────────────────────────────────────────────
    private final int horizon = 10;
    private final double confidence = 0.95;
    // Per-gas std dev (vol%) — CO2 has tighter control band
    private final double[] sigma = {0.05, 0.01, 0.08, 0.04, 0.03, 0.02};

    private final Map<Gas, GasBounds> bounds;
    private final Map<Process, ProcessEffect> effects;

    public GasStabilizationMPC2() {
        // CO2: optimal 800-1200 ppm → target 0.10 vol%, safe 0.02-0.15 vol%
        bounds = new LinkedHashMap<>();
        bounds.put(Gas.O2,    new GasBounds(20.9,  19.0,  23.0));
        bounds.put(Gas.CO2,   new GasBounds(0.10,  0.02,  0.15));
        bounds.put(Gas.H2O,   new GasBounds(1.5,   0.5,   3.0));
        bounds.put(Gas.H2,    new GasBounds(0.1,   0.0,   0.5));
        bounds.put(Gas.CH4,   new GasBounds(0.02,  0.0,   0.1));
        bounds.put(Gas.CH3OH, new GasBounds(0.01,  0.0,   0.05));

        effects = new EnumMap<>(Process.class);
        // Per-step Δ(vol%) when process is ON
        // BIOREACTOR: photosynthesis consumes CO2 & H2O, produces O2
        effects.put(Process.BIOREACTOR,
            new ProcessEffect( 0.8, -0.08, -0.05,  0.0,  0.0,  0.0,  10.0));
        // CO2_INJECTOR: adds CO2 from external tank
        effects.put(Process.CO2_INJECTOR,
            new ProcessEffect( 0.0,  0.06,  0.0,   0.0,  0.0,  0.0,  5.0));
        // WATER_EVAPORATOR: adds H2O vapor
        effects.put(Process.WATER_EVAPORATOR,
            new ProcessEffect( 0.0,  0.0,   0.6,   0.0,  0.0,  0.0,  3.0));
        // HYDROGEN_ELECTROLYZER: 2H2O → 2H2 + O2
        effects.put(Process.HYDROGEN_ELECTROLYZER,
            new ProcessEffect( 0.2,  0.0,   -0.3,  0.3,  0.0,  0.0, 15.0));
        // METHANER: CO2 + 4H2 → CH4 + 2H2O
        effects.put(Process.METHANER,
            new ProcessEffect( 0.0, -0.04,  0.08,  -0.12, 0.08, 0.0,  8.0));
        // METHANOL_SYNTHESIZER: CO2 + 3H2 → CH3OH + H2O
        effects.put(Process.METHANOL_SYNTHESIZER,
            new ProcessEffect( 0.0, -0.03,  0.05,  -0.09, 0.0,  0.06, 12.0));
    }

    // ─── Current state ────────────────────────────────────────────────────
    private double[] state = {20.9, 0.10, 1.5, 0.1, 0.02, 0.01};

    public void setState(double o2, double co2, double h2o, double h2,
                         double ch4, double ch3oh) {
        this.state = new double[]{o2, co2, h2o, h2, ch4, ch3oh};
    }

    public double[] getState() { 
    	return state.clone(); 
    }

    // ─── Core MPC step ────────────────────────────────────────────────────
    /**
     * Returns the set of processes to START (or keep running) this step.
     * Brute-force over 2^6 = 64 combinations.
     */
    public Set<Process> computeControlMove() {
        Process[] all = Process.values();
        int n = all.length;
        double bestCost = Double.MAX_VALUE;
        int bestMask = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            double[] action = maskToAction(mask, n);
            double[] projected = predictWithAction(state, action, horizon);
            double cost = evaluateCost(projected, action);

            if (cost < bestCost) {
                bestCost = cost;
                bestMask = mask;
            }
        }

        Set<Process> active = EnumSet.noneOf(Process.class);
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                active.add(all[i]);
            }
        }
        return active;
    }

    // ─── Prediction (linearized dynamics) ─────────────────────────────────
    private double[] predictWithAction(double[] s, double[] action, int steps) {
        double[] x = s.clone();
        Process[] all = Process.values();
        for (int k = 0; k < steps; k++) {
            // Apply process effects
            for (int p = 0; p < all.length; p++) {
                ProcessEffect eff = effects.get(all[p]);
                for (int g = 0; g < 6; g++) {
                    x[g] += eff.dGas[g] * action[p];
                }
            }
            // Natural drift: O2 drops (respiration), CO2 rises (respiration), H2O rises (transpiration)
            x[Gas.O2.ordinal()]    -= 0.05;
            x[Gas.CO2.ordinal()]   += 0.01;
            x[Gas.H2O.ordinal()]   += 0.02;
        }
        return x;
    }

    // ─── Cost function with chance constraints ────────────────────────────
    private double evaluateCost(double[] projected, double[] action) {
        double cost = 0.0;
        Gas[] gases = Gas.values();

        for (int g = 0; g < 6; g++) {
            GasBounds b = bounds.get(gases[g]);
            double val = projected[g];

            // Quadratic tracking cost
            cost += 10.0 * Math.pow(val - b.target, 2);

            // Chance-constraint violation (Gaussian approx)
            double lowerViol = cdf((b.min - val) / sigma[g]);
            double upperViol = 1.0 - cdf((b.max - val) / sigma[g]);
            double viol = Math.max(lowerViol, upperViol);
            if (viol > (1.0 - confidence)) {
                cost += 1000.0 * Math.pow(viol - (1.0 - confidence), 2);
            }

            // Hard safety: already outside [min, max]
            if (val < b.min || val > b.max) {
                cost += 10000.0;
            }
        }

        // Energy cost
        Process[] all = Process.values();
        for (int p = 0; p < all.length; p++) {
            cost += action[p] * effects.get(all[p]).energyCost;
        }

        return cost;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────
    private double[] maskToAction(int mask, int n) {
        double[] a = new double[n];
        for (int i = 0; i < n; i++) a[i] = ((mask >> i) & 1);
        return a;
    }

    /** Standard normal CDF (Abramowitz & Stegun approx) */
    private double cdf(double z) {
        if (z < -8) return 0.0;
        if (z >  8) return 1.0;
        double t = 1.0 / (1.0 + 0.2316419 * Math.abs(z));
        double poly = t * (0.3193815 + t * (-0.3565638 + t * (1.781478
                + t * (-1.821256 + t * 1.330274))));
        double p = 1.0 - 0.3989423 * Math.exp(-z * z / 2.0) * poly;
        return z >= 0 ? p : 1.0 - p;
    }

    // ─── Demo ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        GasStabilizationMPC2 mpc = new GasStabilizationMPC2();

        // Disturbance: CO2 dropped (photosynthesis consuming it), H2 rising
        mpc.setState(20.5, 0.03, 1.8, 0.35, 0.04, 0.01);

        for (int step = 0; step < 20; step++) {
            Set<Process> control = mpc.computeControlMove();

            System.out.printf("Step %2d | Active: %-50s", step,
                control.isEmpty() ? "(none)" : control.toString());

            // Apply control
            double[] s = mpc.getState();
            Process[] all = Process.values();
            for (Process p : control) {
                ProcessEffect eff = mpc.effects.get(p);
                for (int g = 0; g < 6; g++) s[g] += eff.dGas[g];
            }
            // Natural drift
            s[Gas.O2.ordinal()]    -= 0.05;
            s[Gas.CO2.ordinal()]   += 0.01;
            s[Gas.H2O.ordinal()]   += 0.02;
            mpc.setState(s[0], s[1], s[2], s[3], s[4], s[5]);

            System.out.printf(" | O2=%.2f CO2=%.3f H2O=%.2f H2=%.2f CH4=%.3f CH3OH=%.3f%n",
                s[0], s[1], s[2], s[3], s[4], s[5]);
        }
    }
}   
