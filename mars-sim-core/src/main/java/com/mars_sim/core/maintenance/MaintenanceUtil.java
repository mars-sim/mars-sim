/*
 * Mars Simulation Project
 * MaintenanceUtil.java
 * @date 2025-09-05
 * @author Manny Kung
 */
package com.mars_sim.core.maintenance;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.tool.RandomUtil;

public class MaintenanceUtil {

	private static final double BASE = 10D;
	
	// Minimum %age of the inspection window to trigger maintenance
	public static final double INSPECTION_PERCENTAGE = 0.0625;
	
	private MaintenanceUtil() {
	}

	/**
	 * Scores the entity in terms of need for maintenance. Considers malfunction, condition & time
	 * since last maintenance.
	 * 
	 * @param manager MalfunctionManager
	 * @param entity
	 * @param partsPosted
	 * @return A score on the need for maintenance
	 */
	public static RatingScore scoreMaintenance(MalfunctionManager manager, Malfunctionable entity, 
			boolean partsPosted) {
		
		RatingScore score = new RatingScore("base", 0D);
		
		boolean hasMalfunction = manager.hasMalfunction();
		
		// Note: Look for entities that are NOT malfunction since
		//       malfunctioned entities are being taken care of by the two Repair*Malfunction tasks
		if (hasMalfunction)
			return score;
		
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		double inspectionWindow = manager.getStandardInspectionWindow();	
	
		if (partsPosted) {	
			score = computeScore(manager, score, 
					effectiveTime, inspectionWindow, partsPosted);
		}
		else {

			// Note: Set the probability to be around 1/16 (INSPECTION_PERCENTAGE is 0.0625) of time into the inspection window
			
			// As a result, settlers may begin to do a little bit of inspection whenever possible, even at the beginning of the window 
			// and the inspection is a long way from being due
			
			// This is important since inspection work won't need to become a time crunch at the end
			
			double chance = RandomUtil.getRandomDouble(inspectionWindow * INSPECTION_PERCENTAGE, 
					inspectionWindow);
			
			if ((effectiveTime >= chance)
				// if needed parts have been posted, hurry up to swap out the parts without waiting for 
				// the standard inspection/maintenance due
				|| partsPosted) {
				score = computeScore(manager, score, 
						effectiveTime, inspectionWindow, partsPosted);
			}
		}

 
		return score;
	}
	
	/**
	 * Computes the rating score.
	 * 
	 * @param manager
	 * @param score
	 * @param effectiveTime
	 * @param inspectionWindow
	 * @param partsPosted
	 * @return
	 */
	private static RatingScore computeScore(MalfunctionManager manager, RatingScore score, 
			double effectiveTime, double inspectionWindow, boolean partsPosted) {
		
		double condition = manager.getAdjustedCondition();
		
		score.addBase("maintenance", BASE);
		// Score is based on condition plus %age overdue
		score.addModifier("condition", 10 * (100D - condition));
		
		score.addModifier("maint.win", 10 * (effectiveTime / inspectionWindow));
		
		if (partsPosted) {
			// If needed parts are available, double up the speed of the maintenance
			score.addModifier("parts", 2);
		}
		
		return score;
	}
	
	
}
