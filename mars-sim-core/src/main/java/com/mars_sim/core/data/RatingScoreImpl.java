/*
 * Mars Simulation Project
 * RatingScoreImpl.java
 * @date 2026-03-08
 * @author Barry Evans
 */
package com.mars_sim.core.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A mutable implementation of {@link RatingScore}. Consists of a base value and a set
 * of modifiers that are applied to create a final score.
 */
public class RatingScoreImpl implements RatingScore, Serializable {

	private static final long serialVersionUID = 1L;

    private Map<String, Double> bases;
    private Map<String, Double> modifiers;
    private double score = -1;

    /**
     * Constructor 1.
     * 
     */
    public RatingScoreImpl() {
        this.modifiers = new HashMap<>();
        this.bases = new HashMap<>();
        this.score = 0D;
    }

    /**
     * Constructor 2. Creates a rating with a single base value using {@link RatingScore#BASE}.
     * 
     * @param base Initial base value
     */
    public RatingScoreImpl(double base) {
        this(BASE, base);
    }
    
    /**
     * Constructor 3. Creates a Rating Score that has a single base value.
     * 
     * @param name Name associated with the base
     * @param base Score of the first base
     */
    public RatingScoreImpl(String name, double base) {
        this();
        this.score = base;
        this.bases.put(name, base);
    }
 
    /**
     * Constructor 4. This is a copy constructor that takes a private copy of the modifiers.
     * 
     * @param source Source of the copy
     */
    public RatingScoreImpl(RatingScore source) {
        this.score = source.getScore();
        this.modifiers = new HashMap<>(source.getModifiers());
        this.bases = new HashMap<>(source.getBases());
    }

    /**
     * Gets the score for this rating.
     * 
     * @return
     */
    @Override
    public double getScore() {
        return score;
    }

    /**
     * Gets the modifiers applied in this Rating.
     * 
     * @return
     */
    @Override
    public Map<String, Double> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    /**
     * Gets the bases score in this Rating.
     * 
     * @return
     */
    @Override
    public Map<String, Double> getBases() {
        return Collections.unmodifiableMap(bases);
    }

    /**
     * Adds a modifier (or multiplier) to the Rating. Apply the value as a modifier.
     * Note: modifiers are multiplied.
     * 
     * @param name
     * @param value
     */
    public void addModifier(String name, double value) {
        modifiers.put(name, value);

        if (modifiers.containsKey(name)) {
            // Recalculate the score
            calculateScore();
        }
        else {
            // New modifier so just apply multiplier
            score *= value;
        }
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
     * This is not persisted and needs to be re-applied if the base or modifier change.
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
}
