/*
 * Mars Simulation Project
 * DataManager.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.structure.Settlement;


public class DataManager implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** The settlement's data library. */
		private Map<Settlement, List<FieldDataSet>> dataArchive;

		private static Simulation sim;
			
		/**
		 * Constructor for a {@link DataManager}.
		 */
		public DataManager() {
			dataArchive = new ConcurrentHashMap<>();	
		}
		
		public Map<Settlement, List<FieldDataSet>> getDataArchive() {
			return dataArchive;
		}
		
		/**
		 * Initializes the Medical Complaints from the configuration.
		 * 
		 * @throws exception if not able to initialize complaints.
		 */
		public static void initializeInstances(Simulation s) {
			sim = s;
		}
}
