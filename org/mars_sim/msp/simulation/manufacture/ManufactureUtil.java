/**
 * Mars Simulation Project
 * ManufactureUtil.java
 * @version 2.83 2008-01-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.manufacture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.SimulationConfig;

/**
 * Utility class for getting manufacturing processes.
 */
public final class ManufactureUtil {
	
	/**
	 * Private constructor.
	 */
	private ManufactureUtil() {}
	
	/**
	 * Gets all manufacturing processes.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static final List<ManufactureProcessInfo> getAllManufactureProcesses() 
			throws Exception {
		ManufactureConfig config = SimulationConfig.instance().getManufactureConfiguration();
		return new ArrayList<ManufactureProcessInfo>(config.getManufactureProcessList());
	}
	
	/**
	 * Gets manufacturing processes within the capability of a tech level.
	 * @param techLevel the tech level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static final List<ManufactureProcessInfo> getManufactureProcessesForTechLevel(
			int techLevel) throws Exception {
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		
		ManufactureConfig config = SimulationConfig.instance().getManufactureConfiguration();
		Iterator<ManufactureProcessInfo> i = config.getManufactureProcessList().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if (process.getTechLevelRequired() <= techLevel) result.add(process);
		}
		
		return result;
	}
	
	/**
	 * Gets manufacturing processes within the capability of a tech level and a skill level.
	 * @param techLevel the tech level.
	 * @param skillLevel the skill level.
	 * @return list of processes.
	 * @throws Exception if error getting processes.
	 */
	public static final List<ManufactureProcessInfo> getManufactureProcessesForTechSkillLevel(
			int techLevel, int skillLevel) throws Exception {
		List<ManufactureProcessInfo> result = new ArrayList<ManufactureProcessInfo>();
		
		ManufactureConfig config = SimulationConfig.instance().getManufactureConfiguration();
		Iterator<ManufactureProcessInfo> i = config.getManufactureProcessList().iterator();
		while (i.hasNext()) {
			ManufactureProcessInfo process = i.next();
			if ((process.getTechLevelRequired() <= techLevel) && 
					(process.getSkillLevelRequired() <= skillLevel)) result.add(process);
		}
		
		return result;
	}
}