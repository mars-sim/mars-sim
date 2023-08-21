/*
 * Mars Simulation Project
 * Rating.java
 * @date 2023-07-25
 * @author Barry Evans
 */
package org.mars_sim.msp.core.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class to represent a score Rating. Consists of a base value and a set
 * of modifiers that are applied to create a final score.
 */
public class Rating implements Serializable {
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");

    /**
     * An instance that is a zero score that is immutable
     */
    public static final Rating ZERO_RATING = new Rating(0) {
        @Override
        public void setBase(double base) {
            throw new UnsupportedOperationException("Cannot change base of zero rating");
        };

        @Override
        public void addModifier(String name, double value) {
            throw new UnsupportedOperationException("Cannot add modifier to zero rating");
        };
    };

    private Map<String,Double> modifiers = new HashMap<>();
    private double score = -1;
    private double base;

    public Rating(double base) {
        this.base = base;
        this.score = base;
    }
 
    /**
     * Get the score for this rating.
     * @return
     */
    public double getScore() {
        return score;
    }

    /**
     * Get the modifiers applied in this Rating
     * @return
     */
    public Map<String,Double> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    /**
     * Get the base score
     * @return
     */
    public double getBase() {
        return base;
    }

    /**
     * Add a modifier to the Rating. Apply the value as a modifier
     * @param name
     * @param value
     */
    public void addModifier(String name, double value) {
        modifiers.put(name, value);
        score *= value;
    }
    /**
     * Recalculate the score by multiplying the base with the known modifiers.
     * @param base New base score.
     */
    public void setBase(double base) {
        this.base = base;
        score = modifiers.values().stream().reduce(base, (a, b) -> a * b);
    }

    /**
     * Apply a range to the final score.
     * This is not persist and needs to be reappliyed if the based or midifeirs change.
     * @param lower
     * @param upper
     */
    public void applyRange(double lower, double upper) {
        if (score < lower) {
            score = lower;
        }
        else if (score > upper) {
            score = upper;
        }
    }

    /**
     * Produce a string output of this rating
     * @return
     */
    public String getOutput() {
        
        StringBuilder output = new StringBuilder();
        output.append("Score:").append(SCORE_FORMAT.format(score)).append(" (");
        output.append("base:").append(SCORE_FORMAT.format(base)).append(',');
        output.append(modifiers.entrySet().stream()
                                .map(entry -> entry.getKey() + ":" + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining(",")));
        output.append(")");
        return output.toString();
    }
}
