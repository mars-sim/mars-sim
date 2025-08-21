/*
 * Mars Simulation Project
 * PhysicalConditionFormat.java
 * @date 2024-09-23
 * @author Barry Evans
 */
package com.mars_sim.core.person;

import com.mars_sim.core.tool.Msg;

/**
 * Helper class to create text representations of the PhysicalCondition instances.
 */
public final class PhysicalConditionFormat {
    
    private static final String ENERGY_LEVEL_1 = Msg.getString("PersonTableModel.column.energy.level1");
    private static final String ENERGY_LEVEL_2 = Msg.getString("PersonTableModel.column.energy.level2");
    private static final String ENERGY_LEVEL_3 = Msg.getString("PersonTableModel.column.energy.level3");
    private static final String ENERGY_LEVEL_4 = Msg.getString("PersonTableModel.column.energy.level4");
    private static final String ENERGY_LEVEL_5 = Msg.getString("PersonTableModel.column.energy.level5");
    private static final String ENERGY_LEVEL_6 = Msg.getString("PersonTableModel.column.energy.level6");
    private static final String ENERGY_LEVEL_7 = Msg.getString("PersonTableModel.column.energy.level7");

    private static final String WATER_LEVEL_1 = Msg.getString("PersonTableModel.column.water.level1");
    private static final String WATER_LEVEL_2 = Msg.getString("PersonTableModel.column.water.level2");
    private static final String WATER_LEVEL_3 = Msg.getString("PersonTableModel.column.water.level3");
    private static final String WATER_LEVEL_4 = Msg.getString("PersonTableModel.column.water.level4");
    private static final String WATER_LEVEL_5 = Msg.getString("PersonTableModel.column.water.level5");

    private static final String FATIGUE_LEVEL_1 = Msg.getString("PersonTableModel.column.fatigue.level1");
    private static final String FATIGUE_LEVEL_2 = Msg.getString("PersonTableModel.column.fatigue.level2");
    private static final String FATIGUE_LEVEL_3 = Msg.getString("PersonTableModel.column.fatigue.level3");
    private static final String FATIGUE_LEVEL_4 = Msg.getString("PersonTableModel.column.fatigue.level4");
    private static final String FATIGUE_LEVEL_5 = Msg.getString("PersonTableModel.column.fatigue.level5");

    private static final String STRESS_LEVEL_1 = Msg.getString("PersonTableModel.column.stress.level1");
    private static final String STRESS_LEVEL_2 = Msg.getString("PersonTableModel.column.stress.level2");
    private static final String STRESS_LEVEL_3 = Msg.getString("PersonTableModel.column.stress.level3");
    private static final String STRESS_LEVEL_4 = Msg.getString("PersonTableModel.column.stress.level4");
    private static final String STRESS_LEVEL_5 = Msg.getString("PersonTableModel.column.stress.level5");

    private static final String PERF_LEVEL_1 = Msg.getString("PersonTableModel.column.performance.level1");
    private static final String PERF_LEVEL_2 = Msg.getString("PersonTableModel.column.performance.level2");
    private static final String PERF_LEVEL_3 = Msg.getString("PersonTableModel.column.performance.level3");
    private static final String PERF_LEVEL_4 = Msg.getString("PersonTableModel.column.performance.level4");
    private static final String PERF_LEVEL_5 = Msg.getString("PersonTableModel.column.performance.level5");

	private static final String WELL = "Well";
	private static final String DEAD_COLON = "Dead: ";
	private static final String SICK_COLON = "Sick: ";

    /**
     * Helper cannot be created
     */
    private PhysicalConditionFormat() {}

    /**
     * Gives the status of a person's hunger level.
     * @param b 
     *
     * @param hunger
     * @return status
     */
    public static String getPerformanceStatus(PhysicalCondition pc, boolean showValue) {
        var value = pc.getPerformanceFactor();
    	String status;
    	if (value > .95)
    		status = PERF_LEVEL_1;
    	else if (value > .75)
    		status = PERF_LEVEL_2;
    	else if (value > .50)
    		status = PERF_LEVEL_3;
    	else if (value > .25)
    		status = PERF_LEVEL_4;
    	else
    		status = PERF_LEVEL_5;
        if (showValue) {
           status += " (" + Math.round(value*10.0)/10.0 + " %)";
        }
    	return status;
    }

    /**
     * Gives the status of a person's stress level.
     *
     * @param pc
     * @return status
     */
    public static String getStressStatus(PhysicalCondition pc, boolean showValue) {
        var value = pc.getStress();
    	String status;
    	if (value < 10)
    		status = STRESS_LEVEL_1;
    	else if (value < 40)
    		status = STRESS_LEVEL_2;
    	else if (value < 75)
    		status = STRESS_LEVEL_3;
    	else if (value < 95)
    		status = STRESS_LEVEL_4;
    	else
    		status = STRESS_LEVEL_5;
        if (showValue) {
            status += " (" + Math.round(value*10.0)/10.0 + " %)";
        }
    	return status;
    }

    /**
     * Gives the status of a person's fatigue level.
     *
     * @param pc
     * @return status
     */
    public static String getFatigueStatus(PhysicalCondition pc, boolean showValue) {
        var value = pc.getFatigue();
    	String status;
    	if (value < 500)
    		status = FATIGUE_LEVEL_1;
    	else if (value < 800)
    		status = FATIGUE_LEVEL_2;
    	else if (value < 1200)
    		status = FATIGUE_LEVEL_3;
    	else if (value < 1600)
    		status = FATIGUE_LEVEL_4;
    	else
    		status = FATIGUE_LEVEL_5;
        if (showValue) {
            status += " (" + Math.round(value*10.0)/10.0 + ")";
        }
    	return status;
    }

    /**
     * Gives the status of a person's water level.
     *
     * @param pc
     * @return status
     */
    public static String getThirstyStatus(PhysicalCondition pc, boolean showValue) {
        var thirst = pc.getThirst();
    	String status;
    	if (thirst < 150)
    		status = WATER_LEVEL_1;
    	else if (thirst < 500)
    		status = WATER_LEVEL_2;
    	else if (thirst < 1000)
    		status = WATER_LEVEL_3;
    	else if (thirst < 1600)
    		// Note : Use getDehydrationStartTime()
    		status = WATER_LEVEL_4;
    	else
    		status = WATER_LEVEL_5;
        if (showValue) {
            status += " (" + Math.round(thirst*10.0)/10.0 + ")";
        }
    	return status;
    }

    /**
     * Gives the status of a person's hunger level.
     *
     * @param pc
     * @param showValue Show the actual value
     * @return status
     */
    public static String getHungerStatus(PhysicalCondition pc, boolean showValue) {
        var hunger = pc.getHunger();
        var energy =  pc.getEnergy();
    	String status;
    	if (hunger < 50 && energy > 15000) // Full
    		status = PhysicalConditionFormat.ENERGY_LEVEL_1;
    	else if (hunger < 250 && energy > 10000) // Satisfied
    		status = ENERGY_LEVEL_2;
    	else if (hunger < 500 && energy > 5000) // Comfy
    		status = ENERGY_LEVEL_3;
    	else if (hunger < 750 && energy > PhysicalCondition.ENERGY_THRESHOLD) // Adequate
    		status = ENERGY_LEVEL_4;
    	else if (hunger < 1000 && energy > 1000) // Rumbling
    		status = ENERGY_LEVEL_5;
    	else if (hunger < 1500 && energy > 500) // Ravenous
    		status = ENERGY_LEVEL_6;
    	else // Famished
    		status = ENERGY_LEVEL_7;
        
        if (showValue) {
            status += " (" + Math.round(hunger*10.0)/10.0 + ")";
        }
    	return status;
    }

    /**
	 * Gets a string description of the most mostSeriousProblem health situation.
	 *
	 * @return A string containing the current illness if any.
	 */
	public static String getHealthSituation(PhysicalCondition pc) {
		String situation = WELL;
	
        var mostSeriousProblem = pc.getMostSerious();
    	if (pc.isDead()) {
			situation = DEAD_COLON + mostSeriousProblem.printDeadStatus();
		}
    	else if (mostSeriousProblem != null) {
			situation = SICK_COLON + mostSeriousProblem.printStatus();
		}
		return situation;
	}
	
}
