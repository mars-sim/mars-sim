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

import org.mars.sim.tools.Msg;

/**
 * A class to represent a score Rating. Consists of a base value and a set
 * of modifiers that are applied to create a final score.
 */
public class RatingScore implements Serializable {
    /**
     * Prefix to the key in the message bundle
     */
    private static final String RATING_KEY = "RatingScore.";

    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");

    /**
     * An instance that is a zero score that is immutable
     */
    public static final RatingScore ZERO_RATING = new RatingScore(0) {
        @Override
        public void addBase(String name, double base) {
            throw new UnsupportedOperationException("Cannot change base of zero rating");
        };

        @Override
        public void addModifier(String name, double value) {
            throw new UnsupportedOperationException("Cannot add modifier to zero rating");
        };
    };

    public static final String BASE = "base";

    private Map<String,Double> bases;
    private Map<String,Double> modifiers;
    private double score = -1;

    public RatingScore(double base) {
        this(BASE, base);
    }
    
    /**
     * Create a Rating Score that has a single base value
     * @param name Name associated with the base
     * @param base Score of the first base
     */
    public RatingScore(String name, double base) {
        this.score = base;
        this.modifiers = new HashMap<>();
        this.bases = new HashMap<>();
        this.bases.put(name, base);
    }
 
    /**
     * This is a copy constrcutor that takes a private copy of the modifiers.
     * @param source Source of the copy
     */
    public RatingScore(RatingScore source) {
        this.score = source.score;
        this.modifiers = new HashMap<>(source.modifiers);
        this.bases = new HashMap<>(source.bases);
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
     * Get the bases score in this Rating
     * @return
     */
    public Map<String,Double> getBases() {
        return Collections.unmodifiableMap(bases);
    }

    /**
     * Add a modifier to the Rating. Apply the value as a modifier
     * @param name
     * @param value
     */
    public void addModifier(String name, double value) {
        if (modifiers.containsKey(name)) {
            calculateScore();
        }
        else {
            score *= value;
        }
        modifiers.put(name, value);
    }
    
    /**
     * Recalculate the score by multiplying the base with the known modifiers.
     * This will replace any existing base
     * @param name Name of the base score.
     * @param base New base score.
     */
    public void addBase(String name, double base) {
        this.bases.put(name, base);

        calculateScore();
    }

    private void calculateScore() {
        double base = bases.values().stream().reduce(0D, (a, b) -> a + b);
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
        output.append(bases.entrySet().stream()
                                .map(entry -> entry.getKey() + ":" + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining(",")));
        if (!modifiers.isEmpty()) {
            output.append(',');
        }
        output.append(modifiers.entrySet().stream()
                                .map(entry -> entry.getKey() + ":" + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining(",")));
        output.append(")");
        return output.toString();
    }

    /**
     * Produce a structure output of this rating. This s not ideal being in this class
     * @return HTML formatted string localised.
     */
    public String getHTMLOutput() {
        
        StringBuilder output = new StringBuilder();
        output.append("<html>");
        output.append("<b>Score: ").append(SCORE_FORMAT.format(score)).append("</b><br>");

        output.append(bases.entrySet().stream()
                                .map(entry -> "  " + Msg.getString(RATING_KEY + entry.getKey())
                                                    + ": " + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining("<br>")));

        if (!modifiers.isEmpty()) {
            output.append("<br>");
        }
        output.append(modifiers.entrySet().stream()
                                .map(entry -> "  " + Msg.getString(RATING_KEY + entry.getKey())
                                                    + ": x" + SCORE_FORMAT.format(entry.getValue()))
                                .collect(Collectors.joining("<br>")));
        output.append("</html>");
        return output.toString();
    }
}
