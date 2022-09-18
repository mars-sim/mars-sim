/**
 * Mars Simulation Project
 * DiagnosticsManager.java
 * @version 3.2.0 2021-08-02
 * @author Barry Evans
 */
package org.mars_sim.msp.core.logging;

import java.io.FileNotFoundException;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;

public class DiagnosticsManager {

	private static final String UNIT_MODULE = "unit";
	private static final String TASK_MODULE = "task";
	
	// List of modules supporting diagnostics
	public static final String [] MODULE_NAMES = {
			UNIT_MODULE, TASK_MODULE
	};
	
	/**
	 * Set the diagnostics for one of the modules
	 * @param module
	 * @param enabled
	 * @return
	 * @throws FileNotFoundException 
	 */
	public static boolean setDiagnostics(String module, boolean enabled) throws FileNotFoundException {
		switch(module) {
		case UNIT_MODULE:
			Unit.setDiagnostics(enabled);
			break;
	
		case TASK_MODULE:
			TaskManager.setDiagnostics(enabled);
			break;
		
		default:
			return false;
		}
		return true;
	}
}
