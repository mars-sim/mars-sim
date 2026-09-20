/*
 * Mars Simulation Project
 * CharacteristicsCreator.java
 * @date 2026-09-20
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This class is responsible for creating and managing the characteristics of a person.
 */
class CharacteristicsCreator {

    private CharacteristicsCreator() {
        // Stop instantiation
    }

    /**
	 * Computes a person's blood type. This derives from 2 random parents.
	 * @return the computed blood type of the person, including the Rh factor.
	 */
	public static String calculateBloodType() {

		String dad = getRandomBloodtype();
		String mom = getRandomBloodtype();
		
		String[] dadSplitted = dad.split("_");
		String dadBlood = dadSplitted[0];
		String dadRh = dadSplitted[1];
		
		String[] momSplitted = mom.split("_");
		String momBlood = momSplitted[0];
		String momRh = momSplitted[1];
		
		// Compute the person's blood type
		String tempBloodType = dadBlood + "-" + momBlood;
		
		
		tempBloodType = switch(tempBloodType) {
			
			case "A-A" -> RandomUtil.getRandomString("A", "O");
			case "A-B", "B-A" -> RandomUtil.getRandomString("A", "B", "AB", "O");

			case "A-AB", "AB-A" -> RandomUtil.getRandomString("A", "B", "AB");
			case "A-O", "O-A" -> RandomUtil.getRandomString("A", "O");
            
			case "B-B" -> RandomUtil.getRandomString("B", "O");
			case "B-AB", "AB-B" -> RandomUtil.getRandomString("A" , "B", "AB");
			case "B-O", "O-B" -> RandomUtil.getRandomString("B", "O");
						
			case "AB-AB" -> RandomUtil.getRandomString("A", "B", "AB");
			case "AB-O", "O-AB" ->  RandomUtil.getRandomString("A", "B");
						
			case "O-O" -> "O";
			
			default -> throw new IllegalStateException("Cannot get bloodtype from parents of " + tempBloodType);
		};
		
		// Compute the person's Rh factor
		String tempRh = null; //"POS";
		double percentRhPositive = 0;
		
		if (momRh.equals("POS") && dadRh.equals("POS"))
			percentRhPositive = 93.75;
		else if ((momRh.equals("POS") && dadRh.equals("NEG"))
			|| (momRh.equals("NEG") && dadRh.equals("POS")))
			percentRhPositive = 75.0;
		else 
			tempRh = "-";
		
		if (tempRh == null) {
			int rand = RandomUtil.getRandomInt(100);
			if (rand <= percentRhPositive)
				tempRh =  "+";
			else 
				tempRh = "-";
		}

		return tempBloodType + tempRh;
	}


    private static final String getRandomBloodtype() {
    	int rand = RandomUtil.getRandomInt(100);
    	if (rand <= 34)
    		return "A_POS";
    	else if (rand < 40)
    		return "A_NEG";
    	else if (rand < 49)
    		return "B_POS";
    	else if (rand < 51)
    		return "B_NEG";
    	else if (rand < 55)
    		return "AB_POS";
    	else if (rand < 56)
    		return "AB_NEG";
    	else if (rand < 94)
    		return "O_POS";
    	else 
    		return "O_NEG";
    }

    /**
	 * Computes a person's carrying capacity and attributes and its chromosome.
	 * 
	 * @param personConfig
	 */
	static int calculateCarryingCapacity(PersonConfig personConfig, int age, double weight,
							NaturalAttributeManager attributes) {
		// Note: set up a set of genes that was passed onto this person
		// from two hypothetical parents

		int strength = attributes.getAttribute(NaturalAttributeType.STRENGTH);
		int endurance = attributes.getAttribute(NaturalAttributeType.ENDURANCE);

		if (age < 0) {
			throw new IllegalStateException("Age is not defined");
		}
		int baseCap = (int)personConfig.getBaseCapacity();
		int load = 0;
		if (age > 4 && age < 8)
			load = age;
		else if (age > 7 && age <= 12)
			load = age * 2;
		else if (age > 11 && age <= 14)
			load = (baseCap/3 + age * 2);
		else if (age > 14 && age <= 18)
			load = (int)(baseCap/2.5 + age * 1.5);
		else if (age > 18 && age <= 25)
			load = (int)(baseCap/2.0 + 35 - age / 7.5);
		else if (age > 25 && age <= 35)
			load = (int)(baseCap + 30 - age / 12.5);
		else if (age > 35 && age <= 45)
			load = (baseCap + 25 - age / 10);
		else if (age > 45 && age <= 55)
			load = (int)(baseCap + 20 - age / 7.5);
		else if (age > 55 && age <= 65)
			load = (int)(baseCap/1.25 + 15 - age / 6.0);
		else if (age > 65 && age <= 70)
			load = (int)(baseCap/1.5 + 10 - age / 5.0);
		else if (age > 70 && age <= 75)
			load = (int)(baseCap/1.75 - age / 4.0);
		else if (age > 75 && age <= 80)
			load = (int)(baseCap/2.0 - age / 4.0);
		else
			load = (int)(baseCap/2.5 - age / 4.0);

		// Set inventory total mass capacity based on the person's weight and strength.
		// Must be able to carry an EVA suit
		return Math.max((int)(EVASuit.getEmptyMass() * 2),
						(int)(load + Math.max(20, weight/6.0) + (strength - 50)/1.5 + (endurance - 50)/2.0
				+ RandomUtil.getRandomRegressionInteger(10)));
	}
}
