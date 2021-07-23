/**
 * Mars Simulation Project
 * TrainingUtils.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.person.ai.role.RoleType;

/**
 * The TrainingUtils class derives the role prospects from a person's prior training
 */
public class TrainingUtils {

	/**
	 * Composite key for the lookups
	 */
	private final static class KeyClass {
		TrainingType training;
		RoleType role;
		
		KeyClass(TrainingType training, RoleType role) {
			super();
			this.training = training;
			this.role = role;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((role == null) ? 0 : role.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KeyClass other = (KeyClass) obj;
			if (role != other.role)
				return false;
            return training == other.training;
        }
	}

	/**
	 * Helper method to define set of modifiers to a set of predefined
	 * specialist RoleTypes.
	 */
	private final static void addTraining(Map<KeyClass,Integer> m, TrainingType t,
										  int agr, int eng, int mis, int log, int res, int saf, int sci) {
		m.put(new KeyClass(t, RoleType.AGRICULTURE_SPECIALIST), agr);
		m.put(new KeyClass(t, RoleType.ENGINEERING_SPECIALIST), eng);
		m.put(new KeyClass(t, RoleType.MISSION_SPECIALIST), mis);
		m.put(new KeyClass(t, RoleType.LOGISTIC_SPECIALIST), log);
		m.put(new KeyClass(t, RoleType.RESOURCE_SPECIALIST), res);
		m.put(new KeyClass(t, RoleType.SAFETY_SPECIALIST), saf);
		m.put(new KeyClass(t, RoleType.SCIENCE_SPECIALIST), sci);		
	}
	
	private static Map<KeyClass, Integer> modifiers = null;
	
	static {
		modifiers = new HashMap<>();
		addTraining(modifiers, TrainingType.BIOETHICAL, 8, 7, 2, 2, 4, 5, 6);
		addTraining(modifiers, TrainingType.EXTREME_ENV_OPS, 6, 6, 6, 4, 5, 3, 8); 
		addTraining(modifiers, TrainingType.NASA_DESERT_RATS, 2, 6, 6, 8, 5, 3, 8); 
		addTraining(modifiers, TrainingType.SURVIVAL_TRAINING, 6, 2, 4, 8, 7, 5, 3); 
		addTraining(modifiers, TrainingType.SCUBA_DIVING_MASTER, 0, 2, 5, 8, 6, 6, 2); 
	
		addTraining(modifiers, TrainingType.FLIGHT_SAFETY, 0, 6, 4, 4, 2, 8, 5); 		
		addTraining(modifiers, TrainingType.SEARCH_AND_RESCUE, 1, 3, 8, 8, 4, 7, 1); 
		addTraining(modifiers, TrainingType.MOUNTAINEERING_MASTER, 1, 2, 7, 9, 4, 8, 2); 
		addTraining(modifiers, TrainingType.AIRBORNE_AND_RANGER_SCHOOL, 4, 2, 9, 6, 8, 4, 1); 
		addTraining(modifiers, TrainingType.HAUGHTON_MARS_GEOLOGICAL, 6, 6, 6, 4, 5, 3, 8); 
		
		addTraining(modifiers, TrainingType.HALO_JUMPMASTER, 1, 3, 4, 9, 8, 2, 2); 	
		addTraining(modifiers, TrainingType.MISHAP_INVESTIGATION, 0, 7, 5, 8, 2, 9, 5); 
		addTraining(modifiers, TrainingType.MARS_500_C, 6, 4, 6, 4, 5, 2, 7);
		addTraining(modifiers, TrainingType.MARS_ANALOG_ENVIRONMENT, 7, 5, 6, 4, 5, 2, 8);
		addTraining(modifiers, TrainingType.UNDERSEA_MISSION, 3, 6, 6, 4, 5, 3, 8); 	
		
		addTraining(modifiers, TrainingType.MILITIARY_DEPLOYMENT, 0, 2, 9, 8, 5, 5, 0); 	
		addTraining(modifiers, TrainingType.AVIATION_CERTIFICATION, 0, 4, 3, 4, 3, 9, 2);  
		addTraining(modifiers, TrainingType.ANTARCTICA_EDEN_ISS, 8, 7, 4, 4, 6, 2, 9);   		
		addTraining(modifiers, TrainingType.MARS_TWO_FINAL_100, 5, 4, 6, 4, 5, 2, 3); 
		addTraining(modifiers, TrainingType.UNDERGROUND_CAVES_EXPLORATION, 5, 5, 4, 8, 4, 7, 6); 
	}

	/**
	 * Find the modifiers for a combination ot training and role.
	 * @param role
	 * @param tt
	 * @return
	 */
	public static int getModifier(RoleType role, TrainingType tt) {

		// lookup in modifier table
		KeyClass k = new KeyClass(tt, role);
		Integer v = modifiers.get(k);
		if (v == null) {
			return 0;
		}
		else {
			return v.intValue();
		}
	}
}
