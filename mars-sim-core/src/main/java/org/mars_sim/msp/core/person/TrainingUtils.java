/**
 * Mars Simulation Project
 * TrainingUtils.java
 * @version 3.1.0 2019-09-19
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

/**
 * The TrainingUtils class derives the role prospects from a person's prior training
 */
public class TrainingUtils {
	
	// 19 trainings
//	BIOETHICAL	
//	EXTREME_ENV_OPS	
//	NASA_DESERT_RATS	
//	SURVIVAL_TRAINING		
//	SCUBA_DIVING_MASTER	
	
//	FLIGHT_SAFETY			
//	SEARCH_AND_RESCUE		
//	MOUNTAINEERING_MASTER		
//	AIRBORNE_AND_RANGER_SCHOOL	
//	HAUGHTON_MARS_GEOLOGICAL	
	
//	HALO_JUMPMASTER				
//	MISHAP_INVESTIGATION	
//	MARS_500_C			
//	MARS_ANALOG_ENVIRONMENT	
//	UNDERSEA_MISSION		
	
//	MILITIARY_DEPLOYMENT			
//	COMMERCIAL_PILOT	
//	ANTARCTICA_RESEARCH				
//	MARS_TWO_FINAL_100		
//  UNDERGROUND_CAVES_EXPLORATION

	// 7 roles
//	AGRICULTURE_SPECIALIST	
//	ENGINEERING_SPECIALIST	
//	MISSION_SPECIALIST	
//	LOGISTIC_SPECIALIST	
//	RESOURCE_SPECIALIST	
//	SAFETY_SPECIALIST
//	SCIENCE_SPECIALIST
	
	private static int [][] modifiers = new int[][] {
		{8, 7, 2, 2, 4, 5, 6}, //  BIOETHICAL
		{6, 6, 6, 4, 5, 3, 8}, //  EXTREME_ENV_OPS
		{2, 6, 6, 8, 5, 3, 8}, //  NASA_DESERT_RATS
		{6, 2, 4, 8, 7, 5, 3}, //  SURVIVAL_TRAINING	
		{0, 2, 5, 8, 6, 6, 2}, //  SCUBA_DIVING_MASTER	
	
		{0, 6, 4, 4, 2, 8, 5}, //	FLIGHT_SAFETY				
		{1, 3, 8, 8, 4, 7, 1}, //	SEARCH_AND_RESCUE
		{1, 2, 7, 9, 4, 8, 2}, //	MOUNTAINEERING_MASTER
		{4, 2, 9, 6, 8, 4, 1}, //	AIRBORNE_AND_RANGER_SCHOOL	
		{6, 6, 6, 4, 5, 3, 8}, //	HAUGHTON_MARS_GEOLOGICAL
		
		{1, 3, 4, 9, 8, 2, 2}, //	HALO_JUMPMASTER				
		{0, 7, 5, 8, 2, 9, 5}, //	MISHAP_INVESTIGATION
		{6, 4, 6, 4, 5, 2, 7}, //	MARS_500_C	
		{7, 5, 6, 4, 5, 2, 8}, //	MARS_ANALOG_ENVIRONMENT	
		{3, 6, 6, 4, 5, 3, 8}, //	UNDERSEA_MISSION	
		
		{0, 2, 9, 8, 5, 5, 0}, //  MILITIARY_DEPLOYMENT			
		{0, 4, 3, 4, 3, 9, 2}, //  COMMERCIAL_PILOT
		{8, 7, 4, 4, 6, 2, 9}, //  ANTARCTICA_RESEARCH		
		{5, 4, 6, 4, 5, 2, 3}, //  MARS_TWO_FINAL_100	
		{5, 5, 4, 8, 4, 7, 6}  //  UNDERGROUND_CAVES_EXPLORATION
			
	};
	
	public static int getModifier(int col, int row) {
		return modifiers[row][col];
	}
	
}
