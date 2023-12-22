/*
 * Mars Simulation Project
 * RatingScore.java
 * @date 2023-07-25
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.mars_sim.tools.Msg;

/**
 * A class to represent a score Rating. Consists of a base value and a set
 * of modifiers that are applied to create a final score.
 */
public class RatingScore implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * Prefix to the key in the message bundle
     */
    private static final String RATING_KEY = "RatingScore.";

    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.###");

    /**
     * An instance that is a zero score that is immutable
     */
    public static final RatingScore ZERO_RATING = new RatingScore(0) {
        private static final long serialVersionUID = 1L;

		@Override
        public void addBase(String name, double base) {
            throw new UnsupportedOperationException("Cannot change base of zero rating");
        }

        @Override
        public void addModifier(String name, double value) {
            throw new UnsupportedOperationException("Cannot add modifier to zero rating");
        }
    };

    public static final String BASE = "base";

    private Map<String, Double> bases;
    private Map<String, Double> modifiers;
    private double score = -1;

    /**
     * Constructor 1.
     * 
     */
    public RatingScore() {
        this.modifiers = new HashMap<>();
        this.bases = new HashMap<>();
        this.score = 0;
    }

    /**
     * Constructor 2. Creates a rating with a single base value using @see RatingScore.BASE.
     * 
     * @param base Initial base value
     */
    public RatingScore(double base) {
        this(BASE, base);
    }
    
    /**
     * Constructor 3. Creates a Rating Score that has a single base value.
     * 
     * @param name Name associated with the base
     * @param base Score of the first base
     */
    public RatingScore(String name, double base) {
        this();
        this.score = base;
        this.bases.put(name, base);
    }
 
    /**
     * Constructor 4. This is a copy constructor that takes a private copy of the modifiers.
     * 
     * @param source Source of the copy
     */
    public RatingScore(RatingScore source) {
        this.score = source.score;
        this.modifiers = new HashMap<>(source.modifiers);
        this.bases = new HashMap<>(source.bases);
    }

    /**
     * Gets the score for this rating.
     * 
     * @return
     */
    public double getScore() {
        return score;
    }

    /**
     * Gets the modifiers applied in this Rating.
     * 
     * @return
     */
    public Map<String, Double> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    /**
     * Gets the bases score in this Rating.
     * 
     * @return
     */
    public Map<String, Double> getBases() {
        return Collections.unmodifiableMap(bases);
    }

    /**
     * Adds a modifier to the Rating. Apply the value as a modifier.
     * Note: modifiers are multiplied.
     * 
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
     * Recalculates the score by multiplying the base with the known modifiers.
     * If having the same key, it will replace the value.
     * Note: base values are added.
     * 
     * @param name Name of the base score.
     * @param base New base score.
     */
    public void addBase(String name, double base) {
        this.bases.put(name, base);

        calculateScore();
    }

    private void calculateScore() {
    	// base values are added
        double base = bases.values().stream().reduce(0D, (a, b) -> a + b);
        // modifiers are multiplied
        score = modifiers.values().stream().reduce(base, (a, b) -> a * b);
    }

    /**
     * Applies a range to the final score.
     * This is not persist and needs to be re-applied if the base or modifier change.
     * 
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
     * Produces a string output of this rating.
     * 
     * @return
     */
    public String getOutput() {
        
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
     * Produces a structure output of this rating. This s not ideal being in this class.
     * 
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
