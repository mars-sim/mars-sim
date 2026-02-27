package com.mars_sim.core.environment;

/**
 * Simple dust‑storm forecaster using exponentially weighted moving average (EWMA)
 * of atmospheric opacity (tau) and near‑surface wind speed.
 * Outputs a hazard index in [0, 1]. Deterministic by design, intended as a
 * building block for UI/EVA guards.
 */
public final class DustStormForecaster {

    /** EWMA smoothing factor in (0,1]; larger = more reactive. */
    private final double alphaTau;
    private final double alphaWind;

    /** Normalization constants (domain‑expert tweakable). */
    private final double tau90th;   // tau level mapping ~ to 0.9 hazard from tau alone
    private final double wind90th;  // wind (m/s) mapping ~ to 0.9 hazard from wind alone
    private final double tauWeight; // weight of tau vs wind in the final hazard

    private double ewTau = Double.NaN;   // smoothed tau
    private double ewWind = Double.NaN;  // smoothed wind
    private double lastHazard = 0.0;

    public DustStormForecaster() {
        this(0.35, 0.25, 2.5, 25.0, 0.6); // reasonable defaults
    }

    public DustStormForecaster(double alphaTau, double alphaWind,
                               double tau90th, double wind90th,
                               double tauWeight) {
        this.alphaTau  = clamp01(alphaTau);
        this.alphaWind = clamp01(alphaWind);
        this.tau90th   = Math.max(1e-6, tau90th);
        this.wind90th  = Math.max(1e-6, wind90th);
        this.tauWeight = clamp01(tauWeight);
    }

    /** Feed new observations. dtHours kept for potential future time‑aware smoothing. */
    public void update(double tau, double windMS, double dtHours) {
        if (Double.isNaN(ewTau))   ewTau   = tau;
        else                       ewTau   = alphaTau  * tau    + (1.0 - alphaTau)  * ewTau;

        if (Double.isNaN(ewWind))  ewWind  = windMS;
        else                       ewWind  = alphaWind * windMS + (1.0 - alphaWind) * ewWind;

        lastHazard = computeHazard(ewTau, ewWind);
    }

    /** 0..1 hazard score where 0 = benign, 1 = severe storm. */
    public double getHazard() { return lastHazard; }

    /** Convenience threshold helpers. */
    public boolean isAdvisedNoEVA() { return lastHazard >= 0.75; }
    public boolean isCautionEVA()   { return lastHazard >= 0.45 && lastHazard < 0.75; }
    public boolean isBenign()       { return lastHazard < 0.45; }

    public double getSmoothedTau()      { return ewTau; }
    public double getSmoothedWindMS()   { return ewWind; }

    private double computeHazard(double tau, double wind) {
        // Map tau & wind to [0,1] with soft saturation, then weighted blend.
        double tauH   = saturate(logistic01(tau  / tau90th));
        double windH  = saturate(logistic01(wind / wind90th));
        double blended = tauWeight * tauH + (1.0 - tauWeight) * windH;
        // Slight convex shaping to emphasize high danger
        return saturate(Math.pow(blended, 1.15));
    }

    private static double logistic01(double x) {
        // Smoothly grows from ~0 to ~1; x=1 -> ~0.88 (near “0.9” interpretation).
        double k = 6.0;
        return 1.0 / (1.0 + Math.exp(-k * (x - 1.0)));
    }

    private static double clamp01(double v)   { return Math.max(0.0, Math.min(1.0, v)); }
    private static double saturate(double v)  { return v < 0 ? 0 : (v > 1 ? 1 : v); }

    @Override public String toString() {
        return "DustStormForecaster{ewTau=" + ewTau + ", ewWind=" + ewWind + ", hazard=" + lastHazard + "}";
    }
}
