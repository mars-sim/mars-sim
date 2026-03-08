/*
 * Mars Simulation Project
 * RatingScore.java
 * @date 2026-03-08
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An immutable interface to represent a score Rating. Consists of a base value and a set
 * of modifiers that are applied to create a final score.
 * Use {@link RatingScoreImpl} to build a mutable rating score.
 */
public interface RatingScore extends Comparable<RatingScore>, Serializable {

    DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");

    /**
     * An instance that is a zero score that is immutable.
     */
    RatingScore ZERO_RATING = new RatingScoreConstant(0);

    String BASE = "base";

    /**
     * Gets the score for this rating.
     * 
     * @return
     */
    double getScore();

    /**
     * Gets the modifiers applied in this Rating.
     * 
     * @return
     */
    Map<String, Double> getModifiers();

    /**
     * Gets the bases score in this Rating.
     * 
     * @return
     */
    Map<String, Double> getBases();

    /**
     * Produces a string output of this rating.
     * 
     * @return
     */
    default String getOutput() {
        double score = getScore();
        Map<String, Double> bases = getBases();
        Map<String, Double> modifiers = getModifiers();

        StringBuilder output = new StringBuilder();
        output.append("Score: ").append(SCORE_FORMAT.format(score)).append(" (");
        output.append(bases.entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining(", ")));
        if (!modifiers.isEmpty()) {
            output.append(", ");
        }
        output.append(modifiers.entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining(", ")));
        output.append(")");
        return output.toString();
    }

    /**
     * Compares with another rating score, based on the total.
     * 
     * @param o
     * @return
     */
    @Override
    default int compareTo(RatingScore o) {
        return Double.compare(getScore(), o.getScore());
    }
}
