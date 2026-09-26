/*
 * Mars Simulation Project
 * GasStabilizationMPC.java
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
 * Mixed-Integer Stochastic MPC controller for greenhouse/chemical gas stabilization.
 * Controls: O2, H2O, H2, CH4, CH3OH within safe/target ranges.
 * 
 * Note: Mixed-Integer Stochastic MPC (MISMPC) is a real-time control 
 *       algorithm that combines three ideas:
 * 
 * 1. MPC (Model Predictive Control)	
 *    At each time step, solves an optimization over a finite prediction horizon 
 *    using a system model, applies only the first control move, then re-solves 
 *    (receding horizon)
 * 
 * 2. Mixed-Integer	
 *    The optimization variables include binary/discrete decisions (e.g., start/stop 
 *    a reactor, open/close a valve, select a process mode) alongside continuous 
 *    variables (flow rates, temperatures).
 *    
 * 3. Stochastic	
 *    Uncertainties (disturbances, model errors, sensor noise) are modeled probabilistically, 
 *    and constraints are expressed as chance constraints — e.g., "P(concentration stays 
 *    within bounds) ≥ 95%" — rather than hard worst-case bounds.
 *    
 *    
 *   How it works 
 *   1. Measure current state (gas concentrations, temperatures, etc.)
 *   2. Formulate an optimization problem over the next N steps:
 *      A. Decision variables: continuous (flows, setpoints) + binary (on/off, mode selection)
 *      B. Objective: minimize a cost (e.g., deviation from target O₂/CO₂ levels, energy use)
 *      C. Constraints: system dynamics + chance constraints on all species
 *   3. Solve the resulting mixed-integer program (typically a MIQP or MINLP)
 *   4. Apply only the first control action; repeat at the next sampling instant  
 */
public class GasStabilizationMPC {

    // ─── Gas species definition ───────────────────────────────────────────
    public enum Gas { O2, H2O, H2, CH4, CH3OH }

    public static class GasBounds {
        public final double target;
        public final double min;   // lower safe bound (vol%)
        public final double max;   // upper safe bound (vol%)

        public GasBounds(double target, double min, double max) {
            this.target = target; this.min = min; this.max = max;
        }
    }

    // ─── Process (start/stop unit) ────────────────────────────────────────
    public enum Process {
        BIOREACTOR,        		// produces O2, consumes CO2
        WATER_EVAPORATOR,  		// adds H2O vapor
        HYDROGEN_ELECTROLYZER, 	// produces H2
        METHANER,          		// produces CH4
        METHANOL_SYNTHESIZER 	// produces CH3OH (consumes H2 + CO)
    }

    public static class ProcessEffect {
        public final double[] dGas; // per-step concentration change (vol%) when ON
        public final double energyCost;

        public ProcessEffect(double o2, double h2o, double h2, double ch4, double ch3oh, double cost) {
            this.dGas = new double[]{o2, h2o, h2, ch4, ch3oh};
            this.energyCost = cost;
        }
    }

    // ─── Configuration ────────────────────────────────────────────────────
    private final int horizon = 10;          // prediction steps
    private final double confidence = 0.95;  // chance-constraint level
    private final double[] sigma = {0.05, 0.08, 0.04, 0.03, 0.02}; // per-gas std dev (vol%)
    private final double zScore = 1.645;     // 95% one-sided

    private final Map<Gas, GasBounds> bounds;
    private final Map<Process, ProcessEffect> effects;

    public GasStabilizationMPC() {
        bounds = new LinkedHashMap<>();
        bounds.put(Gas.O2,    new GasBounds(20.9, 19.0, 23.0));
        bounds.put(Gas.H2O,   new GasBounds(1.5,  0.5,  3.0));
        bounds.put(Gas.H2,    new GasBounds(0.1,  0.0,  0.5));
        bounds.put(Gas.CH4,   new GasBounds(0.02, 0.0,  0.1));
        bounds.put(Gas.CH3OH, new GasBounds(0.01, 0.0,  0.05));

        effects = new EnumMap<>(Process.class);
        // Per-step Δ(vol%) when process is ON
        
        																// o2, h2o, h2, ch4, ch3oh
        
        // produces O2, consumes CO2
        effects.put(Process.BIOREACTOR,          new ProcessEffect( 0.8, -0.1,  0.0,  0.0,  0.0, 10.0));
        // adds H2O vapor
        effects.put(Process.WATER_EVAPORATOR,    new ProcessEffect( 0.0,  0.6,  0.0,  0.0,  0.0,  3.0));
        // produces H2
        effects.put(Process.HYDROGEN_ELECTROLYZER, new ProcessEffect(0.0,  0.0,  0.3,  0.0,  0.0, 15.0));
        // produces CH4
        effects.put(Process.METHANER,            new ProcessEffect( 0.0,  0.0, -0.1,  0.08, 0.0, 8.0));
        // produces CH3OH (consumes H2 + CO)
        effects.put(Process.METHANOL_SYNTHESIZER, new ProcessEffect( 0.0,  0.0, -0.2,  0.0,  0.06, 12.0));
    }

    // ─── Current state ────────────────────────────────────────────────────
    private double[] state = {20.9, 1.5, 0.1, 0.02, 0.01}; // current vol%

    public void setState(double o2, double h2o, double h2, double ch4, double ch3oh) {
        this.state = new double[]{o2, h2o, h2, ch4, ch3oh};
    }

    // ─── Core MPC step ────────────────────────────────────────────────────
    /**
     * Returns the set of processes to START (or keep running) this step.
     */
    public Set<Process> computeControlMove() {
        // 1. Predict nominal trajectory with no control
        double[] predicted = predictNominal(state, 0);

        // 2. Evaluate which processes to activate via mixed-integer search
        //    (brute-force over 2^5 = 32 combinations — fine for illustration)
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

        // 3. Decode winning mask
        EnumSet<Process> active = EnumSet.noneOf(Process.class);
        for (int i = 0; i < n; i++) {
            if ((bestMask & (1 << i)) != 0) {
                active.add(all[i]);
            }
        }
        return active;
    }

    // ─── Prediction (linearized dynamics) ─────────────────────────────────
    private double[] predictNominal(double[] s, int steps) {
        double[] x = s.clone();
        for (int k = 0; k < steps; k++) {
            // Natural drift: O2 slowly drops, H2O slowly rises (transpiration)
            x[0] -= 0.05; // O2 decay
            x[1] += 0.02; // H2O rise
        }
        return x;
    }

    private double[] predictWithAction(double[] s, double[] action, int steps) {
        double[] x = s.clone();
        for (int k = 0; k < steps; k++) {
            // Apply process effects (scaled by action 0/1)
            for (int p = 0; p < Process.values().length; p++) {
                ProcessEffect eff = effects.get(Process.values()[p]);
                for (int g = 0; g < 5; g++) {
                    x[g] += eff.dGas[g] * action[p];
                }
            }
            // Natural drift
            x[0] -= 0.05;
            x[1] += 0.02;
        }
        return x;
    }

    // ─── Cost function with chance constraints ────────────────────────────
    /**
     * Penalizes:
     *   1. Deviation from target
     *   2. Violation of chance-constraint bounds (Gaussian approximation)
     *   3. Energy cost of running processes
     */
    private double evaluateCost(double[] projected, double[] action) {
        double cost = 0.0;
        Gas[] gases = Gas.values();

        for (int g = 0; g < 5; g++) {
            GasBounds b = bounds.get(gases[g]);
            double val = projected[g];

            // Quadratic tracking cost
            cost += 10.0 * Math.pow(val - b.target, 2);

            // Chance-constraint violation (Gaussian approx):
            //   P(x < min) ≈ Φ((min - x)/σ)  → penalize if > 1-confidence
            //   P(x > max) ≈ 1 - Φ((max - x)/σ)
            double lowerViol = cdf((b.min - val) / sigma[g]);
            double upperViol = 1.0 - cdf((b.max - val) / sigma[g]);
            double viol = Math.max(lowerViol, upperViol);
            if (viol > (1.0 - confidence)) {
                cost += 1000.0 * Math.pow(viol - (1.0 - confidence), 2); // heavy penalty
            }

            // Hard safety: if projected value is already outside [min, max]
            if (val < b.min || val > b.max) {
                cost += 10000.0;
            }
        }

        // Energy cost
        for (int p = 0; p < Process.values().length; p++) {
            cost += action[p] * effects.get(Process.values()[p]).energyCost;
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

    // ─── Demo / main loop ─────────────────────────────────────────────────
    public static void main(String[] args) {
        GasStabilizationMPC mpc = new GasStabilizationMPC();

        // Simulate a disturbance: O2 drops, H2 rises
        mpc.setState(19.2, 1.8, 0.35, 0.05, 0.01);

        for (int step = 0; step < 20; step++) {
            Set<Process> control = mpc.computeControlMove();

            System.out.printf("Step %2d | Active: %-40s", step,
                control.isEmpty() ? "(none)" : control.toString());

            // Apply control: update state
            double[] s = mpc.getState();
            for (Process p : control) {
                ProcessEffect eff = mpc.effects.get(p);
                for (int g = 0; g < 5; g++) s[g] += eff.dGas[g];
            }
            // Natural drift
            s[0] -= 0.05; s[1] += 0.02;
            mpc.setState(s[0], s[1], s[2], s[3], s[4]);

            System.out.printf(" | O2 = %.2f, H2O = %.2f, H2 = %.2f, CH4 = %.3f, CH3OH = %.3f%n",
                s[0], s[1], s[2], s[3], s[4]);
        }
    }

    public double[] getState() { 
    	return state.clone(); 
    }
    
    /**
     * The mixed-integer part handles the start/stop decisions for your processes 
     * (bioreactors, methanol synthesis units, CO₂ scrubbers, etc.), while the stochastic 
     * part ensures that despite uncertainty in reaction kinetics, weather, and feedstock
     * composition, the gas concentrations (O₂, H₂O, CO₂, H₂, CH₄, CH₃OH) stay within 
     * safe/target ranges with a specified probability.
     */
    
}   