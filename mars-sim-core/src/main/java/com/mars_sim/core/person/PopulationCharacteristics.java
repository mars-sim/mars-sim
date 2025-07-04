/*
 * Mars Simulation Project
 * PopulationCharaceristics.java
 * @date 2023-12-23
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.tool.RandomUtil;

/**
 * This represents the characteristic of a specific population.
 */
public class PopulationCharacteristics{
    
    private double maleAge;
    private double femaleAge;
    private double averageAge;
    
    private double maleHeight;
    private double maleWeight;
    private double femaleHeight;
    private double femaleWeight;
    private double averageWeight;
    private double averageHeight;

    public PopulationCharacteristics(double maleAge, double femaleAge,
    							double maleHeight, double femaleHeight,
    							double maleWeight, double femaleWeight) {
    	this.maleAge = maleAge;
        this.femaleAge = femaleAge;
    	this.averageAge = (maleAge + femaleAge)/2D;
        
        this.maleHeight = maleHeight;
        this.maleWeight = maleWeight;
        this.femaleHeight = femaleHeight;
        this.femaleWeight = femaleWeight;
    	this.averageHeight = (maleHeight + femaleHeight)/2D;
        this.averageWeight = (maleWeight + femaleWeight)/2D;
        
    }

    /**
  	 * Computes a person's age.
  	 * 
     * @param gender
     * @return
     */
  	public double getRandomAge(GenderType gender) {
  
  		double mediaAge = 0;
  		if (gender == GenderType.MALE)
  			mediaAge = maleAge;
  		else
  			mediaAge = femaleAge;
  		
  		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
  		// Attempt to compute age with gaussian curve
  		// double tempAge = 10 + RandomUtil.getGaussianDouble() * age / 13.5;
 
  		// Note: computeGaussianWithLimit(double center, double fraction, double variance)
  		double tempAge = RandomUtil.computeGaussianWithLimit(mediaAge, .5, .1);
		
  		if (tempAge > 65) {
  			tempAge = 65;
		}
		else if (tempAge < 18) {
			tempAge = 18;
		}
  		
  		return tempAge;
  	}
  	
  	
    public double getRandomHeight(GenderType gender) {

		double dadHeight = maleHeight + RandomUtil.getGaussianDouble() * maleHeight / 7D;
		double momHeight = femaleHeight + RandomUtil.getGaussianDouble() * femaleHeight / 10D;

		double geneticFactor = .65;
		double sexFactor = (maleHeight - averageHeight) / averageHeight;
		// Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			return Math.round(
					(geneticFactor * dadHeight + (1 - geneticFactor) * momHeight * (1 + sexFactor)) * 100D) / 100D;
		return Math.round(
					((1 - geneticFactor) * dadHeight + geneticFactor * momHeight * (1 - sexFactor)) * 100D) / 100D;
	}

    /**
	 * Computes a person's weight and its chromosome.
	 */
	public double getRandomWeight(GenderType gender, double height) {
		// For a 20-year-old in the US:
		// male : height : 176.5 weight : 68.5
		// female : height : 162.6 weight : 57.2

		// Note: factor in country of origin.
		// Note: look for a gender-correlated curve

		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Attempt to compute height with gaussian curve
		double dadWeight = maleWeight + RandomUtil.getGaussianDouble() * maleWeight / 13.5;
		double momWeight = femaleWeight + RandomUtil.getGaussianDouble() * femaleWeight / 10.5;

		double geneticFactor = .65;
		double sexFactor = (maleWeight - averageWeight) / averageWeight; 
		double heightFactor = height / averageHeight;

		// Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			return Math.round(heightFactor
					* (geneticFactor * dadWeight + (1 - geneticFactor) * momWeight * (1 + sexFactor)) * 100D)
					/ 100D;
		return Math.round(heightFactor
					* ((1 - geneticFactor) * dadWeight + geneticFactor * momWeight * (1 - sexFactor)) * 100D)
					/ 100D;
	}

    public double getAverageHeight() {
        return averageHeight;
    }

    public double getAverageWeight() {
        return averageWeight;
    }
    
    public double getAverageAge() {
        return averageAge;
    }

}
