// package com.mars_sim.core.automation; // <-- adjust or leave default package if you prefer

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * ResourceProcessPlanner
 *
 * One-file, plug-and-play controller that automatically starts/stops resource processes
 * to meet settlement stock targets using the project's Value Point (VP) economics.
 *
 * Design goals:
 *  - Zero compile-time coupling to mars-sim classes (supply lambdas/adapters).
 *  - Stable: hysteresis + minimum run time prevent flapping.
 *  - Efficient: power-budget aware; penalizes high-power/low-yield processes.
 *  - Extensible: you can add custom scoring weights without editing other files.
 *
 * Typical wiring:
 *
 *   var planner = ResourceProcessPlanner.<Settlement, Process>builder()
 *       .listProcesses(settlement -> settlement.getProcesses()) // Iterable<Process>
 *       .getOutputsKgPerSol(proc -> Map.of("Water", 120.0))     // per-output kg/sol
 *       .getInputsKgPerSol(proc -> Map.of("Ice", 130.0))
 *       .isRunning(proc -> proc.isRunning())
 *       .canRun(proc -> proc.canRunNow())                       // has crew/inputs/etc
 *       .start(proc -> proc.start())
 *       .stop(proc -> proc.stop())
 *       .getProcessPowerKW(proc -> proc.getPowerKW())
 *       .getProcessSetupMillisols(proc -> proc.getSetupMillisols())
 *       .getStockKg(resource -> settlement.getStock(resource))
 *       .getVP(resource -> goodsManager.getValuePoint(resource)) // VP per resource
 *       .powerBudgetKW(() -> grid.getAvailableProcessPowerKW())
 *       .targetsKg(Map.of("Water", 500.0, "Oxygen", 300.0, "Food", 600.0))
 *       .hysteresisFraction(0.15)     // 15% band to avoid toggling
 *       .minRunTicks(12)              // don't stop a process until it ran >= 12 ticks
 *       .powerPenaltyPerKW(0.5)       // tune power cost pressure
 *       .inputPenaltyMultiplier(1.0)  // penalize consuming high-VP inputs
 *       .deficitBoostMultiplier(2.0)  // boost score if producing under-target outputs
 *       .build();
 *
 *   // Call once per simulation tick (or every N ticks) per settlement:
 *   planner.tick(settlement);
 *
 * Notes:
 *  - "tick" is count-based (not wall time). Use any cadence (e.g., every 5 clock pulses).
 *  - If you don't have per-process kg/sol, pass 1.0 for each output/input; it still works
 *    because relative VP and deficits drive decisions.
 */
public final class ResourceProcessPlanner<S, P> {

    // ---- Functional adapters you wire in -------------------------------------------------

    /** All currently available processes for a settlement (manufacturing, refining, etc.). */
    public interface ProcessLister<S, P> extends Function<S, Iterable<P>> {}

    /** Outputs (kg/sol) produced by a process. e.g., {"Water":120, "Oxygen":5} */
    public interface OutputsKgPerSol<P> extends Function<P, Map<String, Double>> {}

    /** Inputs (kg/sol) consumed by a process. e.g., {"Ice":130, "PowerCell":0.1} */
    public interface InputsKgPerSol<P> extends Function<P, Map<String, Double>> {}

    /** Is the process currently running? */
    public interface IsRunning<P> extends Predicate<P> {}

    /** Can the process start now (crew, site, inputs, maintenance)? */
    public interface CanRun<P> extends Predicate<P> {}

    /** Start/stop hooks. */
    public interface StartProcess<P> extends Consumer<P> {}
    public interface StopProcess<P> extends Consumer<P> {}

    /** Nominal power draw (kW) and setup latency (millisols) for a process. */
    public interface ProcessPowerKW<P> extends ToDoubleFunction<P> {}
    public interface ProcessSetupMillisols<P> extends ToDoubleFunction<P> {}

    /** Current stock (kg) by resource name (the sim is resource-name centric). */
    public interface StockKg extends ToDoubleFunction<String> {}

    /** Current "value point" (VP) per resource (mars-sim’s economics). */
    public interface ValuePoint extends ToDoubleFunction<String> {}

    /** Current available power budget (kW) for processes (post life-support baseline). */
    public interface PowerBudgetKW extends DoubleSupplier {}

    // ---- Builder ------------------------------------------------------------------------

    public static final class Builder<S, P> {
        private ProcessLister<S, P> listProcesses;
        private OutputsKgPerSol<P> getOutputs;
        private InputsKgPerSol<P> getInputs;
        private IsRunning<P> isRunning;
        private CanRun<P> canRun = p -> true;
        private StartProcess<P> start;
        private StopProcess<P> stop;
        private ProcessPowerKW<P> getPowerKW = p -> 0.0;
        private ProcessSetupMillisols<P> getSetupMillisols = p -> 0.0;

        private StockKg getStock;
        private ValuePoint getVP;
        private PowerBudgetKW powerBudgetKW = () -> Double.POSITIVE_INFINITY;

        private Map<String, Double> targetsKg = new HashMap<>();
        private double hysteresisFraction = 0.15;
        private int minRunTicks = 10;
        private double powerPenaltyPerKW = 0.3;
        private double inputPenaltyMultiplier = 1.0;
        private double deficitBoostMultiplier = 2.0;

        public Builder<S, P> listProcesses(ProcessLister<S, P> f) { this.listProcesses = f; return this; }
        public Builder<S, P> getOutputsKgPerSol(OutputsKgPerSol<P> f) { this.getOutputs = f; return this; }
        public Builder<S, P> getInputsKgPerSol(InputsKgPerSol<P> f) { this.getInputs = f; return this; }
        public Builder<S, P> isRunning(IsRunning<P> f) { this.isRunning = f; return this; }
        public Builder<S, P> canRun(CanRun<P> f) { this.canRun = f; return this; }
        public Builder<S, P> start(StartProcess<P> f) { this.start = f; return this; }
        public Builder<S, P> stop(StopProcess<P> f) { this.stop = f; return this; }
        public Builder<S, P> getProcessPowerKW(ProcessPowerKW<P> f) { this.getPowerKW = f; return this; }
        public Builder<S, P> getProcessSetupMillisols(ProcessSetupMillisols<P> f) { this.getSetupMillisols = f; return this; }
        public Builder<S, P> getStockKg(StockKg f) { this.getStock = f; return this; }
        public Builder<S, P> getVP(ValuePoint f) { this.getVP = f; return this; }
        public Builder<S, P> powerBudgetKW(PowerBudgetKW f) { this.powerBudgetKW = f; return this; }
        public Builder<S, P> targetsKg(Map<String, Double> targets) { this.targetsKg.putAll(targets); return this; }
        public Builder<S, P> hysteresisFraction(double f) { this.hysteresisFraction = clamp01(f); return this; }
        public Builder<S, P> minRunTicks(int ticks) { this.minRunTicks = Math.max(1, ticks); return this; }
        public Builder<S, P> powerPenaltyPerKW(double w) { this.powerPenaltyPerKW = Math.max(0.0, w); return this; }
        public Builder<S, P> inputPenaltyMultiplier(double m) { this.inputPenaltyMultiplier = Math.max(0.0, m); return this; }
        public Builder<S, P> deficitBoostMultiplier(double m) { this.deficitBoostMultiplier = Math.max(0.0, m); return this; }

        public ResourceProcessPlanner<S, P> build() {
            Objects.requireNonNull(listProcesses, "listProcesses");
            Objects.requireNonNull(getOutputs, "getOutputsKgPerSol");
            Objects.requireNonNull(getInputs, "getInputsKgPerSol");
            Objects.requireNonNull(isRunning, "isRunning");
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(stop, "stop");
            Objects.requireNonNull(getStock, "getStockKg");
            Objects.requireNonNull(getVP, "getVP");

            return new ResourceProcessPlanner<>(
                listProcesses, getOutputs, getInputs, isRunning, canRun, start, stop,
                getPowerKW, getSetupMillisols, getStock, getVP, powerBudgetKW,
                targetsKg, hysteresisFraction, minRunTicks, powerPenaltyPerKW,
                inputPenaltyMultiplier, deficitBoostMultiplier
            );
        }

        private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }
    }

    public static <S, P> Builder<S, P> builder() { return new Builder<>(); }

    // ---- Instance -----------------------------------------------------------------------

    private final ProcessLister<S, P> listProcesses;
    private final OutputsKgPerSol<P> getOutputs;
    private final InputsKgPerSol<P> getInputs;
    private final IsRunning<P> isRunning;
    private final CanRun<P> canRun;
    private final StartProcess<P> start;
    private final StopProcess<P> stop;
    private final ProcessPowerKW<P> getPowerKW;
    private final ProcessSetupMillisols<P> getSetupMillisols;

    private final StockKg getStock;
    private final ValuePoint getVP;
    private final PowerBudgetKW powerBudgetKW;

    private final Map<String, Double> targetsKg;
    private final double hysteresisFraction;
    private final int minRunTicks;
    private final double powerPenaltyPerKW;
    private final double inputPenaltyMultiplier;
    private final double deficitBoostMultiplier;

    private long tickCounter = 0L;

    /** Per-process runtime bookkeeping (to ensure minRunTicks / stability). */
    private final Map<P, RunState> state = new IdentityHashMap<>();

    private static final class RunState {
        long lastStartTick = Long.MIN_VALUE / 4;
        long lastStopTick = Long.MIN_VALUE / 4;
    }

    private ResourceProcessPlanner(
        ProcessLister<S, P> listProcesses,
        OutputsKgPerSol<P> getOutputs,
        InputsKgPerSol<P> getInputs,
        IsRunning<P> isRunning,
        CanRun<P> canRun,
        StartProcess<P> start,
        StopProcess<P> stop,
        ProcessPowerKW<P> getPowerKW,
        ProcessSetupMillisols<P> getSetupMillisols,
        StockKg getStock,
        ValuePoint getVP,
        PowerBudgetKW powerBudgetKW,
        Map<String, Double> targetsKg,
        double hysteresisFraction,
        int minRunTicks,
        double powerPenaltyPerKW,
        double inputPenaltyMultiplier,
        double deficitBoostMultiplier
    ) {
        this.listProcesses = listProcesses;
        this.getOutputs = getOutputs;
        this.getInputs = getInputs;
        this.isRunning = isRunning;
        this.canRun = canRun;
        this.start = start;
        this.stop = stop;
        this.getPowerKW = getPowerKW;
        this.getSetupMillisols = getSetupMillisols;
        this.getStock = getStock;
        this.getVP = getVP;
        this.powerBudgetKW = powerBudgetKW;
        this.targetsKg = new HashMap<>(targetsKg);
        this.hysteresisFraction = hysteresisFraction;
        this.minRunTicks = minRunTicks;
        this.powerPenaltyPerKW = powerPenaltyPerKW;
        this.inputPenaltyMultiplier = inputPenaltyMultiplier;
        this.deficitBoostMultiplier = deficitBoostMultiplier;
    }

    // ---- Public API ---------------------------------------------------------------------

    /** Call once per simulation tick (or every N ticks). */
    public void tick(S settlement) {
        tickCounter++;

        // 1) Compute resource deficits vs targets (with hysteresis)
        Map<String, Double> stock = new HashMap<>();
        Map<String, Double> deficit = new HashMap<>();
        for (Map.Entry<String, Double> e : targetsKg.entrySet()) {
            String r = e.getKey();
            double target = nz(e.getValue());
            double have = getStock.applyAsDouble(r);
            stock.put(r, have);

            double lowThresh = target * (1.0 - hysteresisFraction);
            double highThresh = target * (1.0 + hysteresisFraction);
            double d = have < lowThresh ? (target - have) : 0.0;
            deficit.put(r, Math.max(0.0, d));
            // If have > highThresh, we consider this resource fully satisfied for stopping.
        }

        // 2) Score processes by (VP of outputs - VP of inputs - power cost), boosted by deficits
        List<ProcScore<P>> scored = new ArrayList<>();
        for (P p : listProcesses.apply(settlement)) {
            Map<String, Double> outs = safeMap(getOutputs.apply(p));
            Map<String, Double> ins  = safeMap(getInputs.apply(p));

            double outValue = 0.0;
            for (var e : outs.entrySet()) {
                String r = e.getKey();
                double kgPerSol = Math.max(0.0, e.getValue());
                double vp = Math.max(0.0, getVP.applyAsDouble(r));
                double boost = 1.0 + deficitBoostMultiplier * normalizedDeficit(deficit, targetsKg, r);
                outValue += vp * kgPerSol * boost;
            }

            double inCost = 0.0;
            for (var e : ins.entrySet()) {
                String r = e.getKey();
                double kgPerSol = Math.max(0.0, e.getValue());
                double vp = Math.max(0.0, getVP.applyAsDouble(r));
                inCost += vp * kgPerSol * inputPenaltyMultiplier;
            }

            double power = Math.max(0.0, getPowerKW.applyAsDouble(p));
            double setupPenalty = Math.max(0.0, getSetupMillisols.applyAsDouble(p)) * 0.01; // tiny bias

            double score = outValue - inCost - powerPenaltyPerKW * power - setupPenalty;

            // Consider "is this process relevant now?" -> if all outputs are already above high-threshold,
            // reduce its score to avoid needless starts.
            if (allOutputsSatisfied(outs, stock, targetsKg, hysteresisFraction)) {
                score *= 0.25; // soft-deprioritize, not outright forbid
            }

            scored.add(new ProcScore<>(p, score, outs, ins, power));
        }

        // 3) Decide start/stop under power budget and stability constraints
        double budget = powerBudgetKW.getAsDouble();
        double usedPower = 0.0;

        // Keep already-running processes if they still have non-negative contribution or are within min run time,
        // then fill the rest with best candidates by score.
        List<P> running = scored.stream().filter(ps -> isRunning.test(ps.p)).map(ps -> ps.p).collect(Collectors.toList());
        // Compute power already used by running processes (we assume all draw nominal power)
        for (var ps : scored) if (isRunning.test(ps.p)) usedPower += ps.powerKW;

        // Sort by score descending for starting candidates
        scored.sort(Comparator.comparingDouble((ProcScore<P> ps) -> ps.score).reversed());

        // Start high-scoring, not-running processes while power allows
        for (var ps : scored) {
            if (ps.score <= 0.0) break; // no more positive-value work
            if (isRunning.test(ps.p)) continue;
            if (!canRun.test(ps.p)) continue;
            if (!producesAnyDeficit(ps.outs, deficit)) continue; // optional: only start if helps a deficit

            double projected = usedPower + ps.powerKW;
            if (projected <= budget) {
                // Start
                start.accept(ps.p);
                usedPower = projected;
                state(ps.p).lastStartTick = tickCounter;
            }
        }

        // Stop low-value processes if:
        //  (a) score < 0 and (b) ran at least minRunTicks and (c) outputs are satisfied above high threshold
        for (var ps : scored) {
            if (!isRunning.test(ps.p)) continue;
            if (ps.score >= 0.0) continue;
            RunState rs = state(ps.p);
            if ((tickCounter - rs.lastStartTick) < minRunTicks) continue;
            if (!allOutputsSatisfied(ps.outs, stock, targetsKg, hysteresisFraction)) continue;
            stop.accept(ps.p);
            rs.lastStopTick = tickCounter;
        }
    }

    // ---- Helpers ------------------------------------------------------------------------

    private static class ProcScore<P> {
        final P p; final double score; final Map<String, Double> outs; final Map<String, Double> ins; final double powerKW;
        ProcScore(P p, double score, Map<String, Double> outs, Map<String, Double> ins, double powerKW) {
            this.p = p; this.score = score; this.outs = outs; this.ins = ins; this.powerKW = powerKW;
        }
    }

    private RunState state(P p) { return state.computeIfAbsent(p, k -> new RunState()); }

    private static Map<String, Double> safeMap(Map<String, Double> m) {
        return (m == null) ? Collections.emptyMap() : m;
    }

    /** Returns 0..1 deficit for resource r vs its target, including hysteresis lower band. */
    private static double normalizedDeficit(Map<String, Double> deficit, Map<String, Double> targets, String r) {
        double d = nz(deficit.get(r));
        if (d <= 0) return 0.0;
        double t = Math.max(1e-9, nz(targets.get(r)));
        return Math.max(0.0, Math.min(1.0, d / t));
        // If target is 500kg and we're at 350kg, deficit=150 -> 0.3 normalized deficit.
    }

    /** True if all outputs of this process are above the *upper* hysteresis band (fully satisfied). */
    private static boolean allOutputsSatisfied(Map<String, Double> outs, Map<String, Double> stock,
                                               Map<String, Double> targets, double hystFrac) {
        for (String r : outs.keySet()) {
            double target = nz(targets.get(r));
            if (target <= 0.0) continue; // not a targeted resource
            double have = nz(stock.get(r));
            double highThresh = target * (1.0 + hystFrac);
            if (have < highThresh) return false;
        }
        return true;
    }

    /** True if any output addresses a current deficit. */
    private static boolean producesAnyDeficit(Map<String, Double> outs, Map<String, Double> deficit) {
        for (String r : outs.keySet()) {
            if (nz(deficit.get(r)) > 0.0) return true;
        }
        return false;
    }

    private static double nz(Double v) { return v == null ? 0.0 : v; }
}
