/*
 * Mars Simulation Project
 * RatingScoreConstant.java
 * @date 2026-03-08
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * A fixed/read-only implementation of {@link RatingScore}. Once created the score cannot be changed.
 * Suitable for use in constants like {@link RatingScore#ZERO_RATING}.
 */
public class RatingScoreConstant implements RatingScore, Serializable {

	private static final long serialVersionUID = 1L;

    private final double score;
    private final Map<String, Double> bases;

    /**
     * Creates a constant rating score with a single base value.
     * 
     * @param score The fixed score value
     */
    public RatingScoreConstant(double score) {
        this.score = score;
        this.bases = Collections.singletonMap(BASE, score);
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public Map<String, Double> getModifiers() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Double> getBases() {
        return bases;
    }
}
