/*
 * Mars Simulation Project
 * Rating.java
 * @date 2026-05-09
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class to represent a score Rating. Consists of a base value and a set
 * of modifiers that are applied to create a final score.
 */
public interface Rating extends Comparable<RatingScore>, Serializable {

    public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");

    public static final String BASE = "base";


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
        
        StringBuilder output = new StringBuilder();
        output.append("Score: ").append(SCORE_FORMAT.format(getScore())).append(" (");
        output.append(getBases().entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining(", ")));
        
        var modifiers = getModifiers();
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
     * @param o Other rating score to compare with.
     * @return Return comparative result based on score.
     */
    @Override
    default int compareTo(RatingScore o) {
        return Double.compare(getScore(), o.getScore());
    }
}
