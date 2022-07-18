/*
 * Mars Simulation Project
 * RobotConfig.java
 * @date 2022-07-05
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Provides configuration information about robots. Uses a JDOM document to
 * get the information.
 */
public class RobotConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private List<String> alphaRobotCrewName;
	private List<String> alphaRobotCrewType;
	private List<String> alphaRobotCrewJob;

	// Element names
	private static final String ROBOT_LIST = "robot-list";
	private static final String ROBOT = "robot";

	private static final String ROBOT_NAME = "name";
	private static final String ROBOT_TYPE = "type";
	private static final String SETTLEMENT = "settlement";
	private static final String JOB = "job";
	
	private static final String STANDBY_POWER_CONSUMPTION = "standby-power-consumption";
	private static final String FUEL_CONSUMPTION_RATE = "fuel-consumption-rate";
	private static final String LOW_POWER_MODE = "low-power-mode";
	private static final String MIN_AIR_PRESSURE = "min-air-pressure";
	private static final String MIN_TEMPERATURE = "min-temperature";
	private static final String MAX_TEMPERATURE = "max-temperature";
	private static final String FREEZING_TIME = "freezing-time";

	private static final String ROBOTIC_ATTRIBUTE_LIST = "robotic-attribute-list";
	private static final String ROBOTIC_ATTRIBUTE = "robotic-attribute";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	
	private static final String SKILL_LIST = "skill-list";
	private static final String SKILL = "skill";
	private static final String LEVEL = "level";

	// number of robot in the xml's ROBOT_LIST
	private static int size;
	// power-consumption-rate
	private static double standbyPower;
	// fuel-consumption-rate
	private static double fcr ;
	// low-power-mode-start-time
	private static double lowPower;
	// min-air-pressure
	private static double minap;
	// min-temperature
	private static double minT;
	// max-temperature
	private static double maxT;
	// freezing-time
	private static double ft;
	/** A map of various robot specs. */
	private transient Map<Integer, RobotSpec> map;

	/**
	 * Constructor
	 * 
	 * @param robotDoc the robot config DOM document.
	 */
	public RobotConfig(Document robotDoc) {
		loadRobotSpecs(robotDoc);
	}

	/**
	 * Parses only once, store resulting data for later use.
	 * 
	 * @param robotDoc
	 */
	private synchronized void loadRobotSpecs(Document robotDoc) {
		if (map != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		Map<Integer, RobotSpec> newMap = new HashMap<>();
		
		Element root = robotDoc.getRootElement();
	
		Element pcrElement = root.getChild(STANDBY_POWER_CONSUMPTION);
		standbyPower = Double.parseDouble(pcrElement.getAttributeValue(VALUE));
		
		Element fcrElement = root.getChild(FUEL_CONSUMPTION_RATE);
		fcr = Double.parseDouble(fcrElement.getAttributeValue(VALUE));
		
		Element lpmstElement = root.getChild(LOW_POWER_MODE);
		lowPower = Double.parseDouble(lpmstElement.getAttributeValue(VALUE));
		
		Element minapElement = root.getChild(MIN_AIR_PRESSURE);
		minap = Double.parseDouble(minapElement.getAttributeValue(VALUE));
		
		Element minTElement = root.getChild(MIN_TEMPERATURE);
		minT = Double.parseDouble(minTElement.getAttributeValue(VALUE));
		
		Element maxTElement = root.getChild(MAX_TEMPERATURE);
		maxT = Double.parseDouble(maxTElement.getAttributeValue(VALUE));
		
		Element ftElement = root.getChild(FREEZING_TIME);
		ft = Double.parseDouble(ftElement.getAttributeValue(VALUE));
		
		Element rlElement = root.getChild(ROBOT_LIST);
		List<Element> listNodes = rlElement.getChildren(ROBOT);
		
		size = listNodes.size();
		
		int i = 0;
		for (Element listElement : listNodes) {
			String name = listElement.getAttributeValue(ROBOT_NAME).toLowerCase();
			String type = listElement.getAttributeValue(ROBOT_TYPE).toLowerCase();
			RobotType robotType = RobotType.valueOfIgnoreCase(type);
			String settlementName = listElement.getAttributeValue(SETTLEMENT).toLowerCase();
			String jobName = listElement.getAttributeValue(JOB).toLowerCase();

			// Attributes
			Element attributeListElement = listElement.getChild(ROBOTIC_ATTRIBUTE_LIST);
			Map<String, Integer> attributeMap = new HashMap<>();
			if (attributeListElement != null) {
				List<Element> attributesElement = attributeListElement.getChildren(ROBOTIC_ATTRIBUTE);
				for (Element attributeElement : attributesElement) {
					String attributeName = attributeElement.getAttributeValue(NAME).toLowerCase();
					Integer value = Integer.parseInt(attributeElement.getAttributeValue(VALUE));
					attributeMap.put(attributeName, value);
				}
			}

			// Skills
			Element skillListElement = listElement.getChild(SKILL_LIST);
			Map<String, Integer> skillMap = new HashMap<>();
			if (skillListElement != null) {
				List<Element> skillsElement = skillListElement.getChildren(SKILL);
				for (Element skillElement : skillsElement) {
					String skillName = skillElement.getAttributeValue(NAME).toLowerCase();
					Integer level = Integer.parseInt(skillElement.getAttributeValue(LEVEL));
					skillMap.put(skillName, level);
				}
			}

			RobotSpec spec = new RobotSpec(
					name, 
					robotType, 
					settlementName, 
					jobName,
					attributeMap,
					skillMap);

			// and keep results for later use
			newMap.put(i, spec);
			i++;
		}
		
		map = Collections.unmodifiableMap(newMap);
	}
	
	
	/**
	 * Gets the robot map.
	 * 
	 * @return
	 */
	public Map<Integer, RobotSpec> getRobotMap() {
		return map;
	}
	
	/**
	 * Gets the standby power consumption.
	 * 
	 * @return power consumption (kW)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getStandbyPowerConsumption() {
		return standbyPower;
	}

	/**
	 * Gets the fuel consumption rate.
	 * 
	 * @return fuel rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFuelConsumptionRate() {
		return fcr;
	}

	/**
	 * Gets the percentage that sets in the low power mode.
	 * 
	 * @return low power mode.
	 * @throws Exception if low power mode start time could not be found.
	 */
	public double getLowPowerModePercent() {
		return lowPower;
	}

	/**
	 * Gets the required air pressure.
	 * 
	 * @return air pressure in Pa.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() {
		return minap;
	}

	/**
	 * Gets the minimum temperature a robot can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() {
		return minT;
	}

	/**
	 * Gets the maximum temperature a robot can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() {
		return maxT;
	}

	/**
	 * Gets the time a robot can survive below minimum temperature.
	 * 
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() {
		return ft;
	}

	/**
	 * Gets the configured robot's starting settlement.
	 * 
	 * @param index the robot's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredRobotSettlement(int index) {
		return map.get(index).getSettlementName();	
	}

	/**
	 * Gets the configured robot's job.
	 * 
	 * @param index the robot's index.
	 * @return the job name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredRobotJob(int index) {
		if (alphaRobotCrewJob != null)
			return alphaRobotCrewJob.get(index);
		else
			return map.get(index).getJobName();
	}

	public void setRobotName(int index, String value) {
		if (alphaRobotCrewName == null)
			alphaRobotCrewName = new CopyOnWriteArrayList<String>();
		if (alphaRobotCrewName.size() == 4) {
			alphaRobotCrewName.set(index, value);
		} else
			alphaRobotCrewName.add(value);
	}

	public void setRobotJob(int index, String value) {
		if (alphaRobotCrewJob == null)
			alphaRobotCrewJob = new CopyOnWriteArrayList<String>();
		if (alphaRobotCrewJob.size() == 4) {
			alphaRobotCrewJob.set(index, value);
		} else
			alphaRobotCrewJob.add(value);
	}

	/**
	 * Gets the number of robots listed in the xml file.
	 * 
	 * @return number of robots.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredRobots() {
		return size;
	}

	/**
	 * Gets the configured Robot's name.
	 * 
	 * @param index the Robot's index.
	 * @return name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredRobotName(int index) {
		if (alphaRobotCrewName != null)
			return alphaRobotCrewName.get(index);
		else
			return map.get(index).getName();
	}

	/**
	 * Gets the configured RobotType.
	 * 
	 * @param index the Robot's index.
	 * @return {@link RobotType} or null if not found.
	 * @throws Exception if error in XML parsing.
	 */
	public RobotType getConfiguredRobotType(int index) {
		if (alphaRobotCrewType != null)
			return RobotType.valueOfIgnoreCase(alphaRobotCrewType.get(index));
		else
			return map.get(index).getRobotType(); 
	}

	/**
	 * Gets a map of the configured robot's natural attributes.
	 * 
	 * @param index the robot's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getRoboticAttributeMap(int index) {
		return map.get(index).getAttributeMap();
	}

	/**
	 * Gets a map of the configured robot's skills.
	 * 
	 * @param index the robot's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getSkillMap(int index) {
		return map.get(index).getSkillMap();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		map.clear();
		map = null;
		
		if (alphaRobotCrewName != null) {
			alphaRobotCrewName.clear();
			alphaRobotCrewName = null;
		}

		if (alphaRobotCrewJob != null) {
			alphaRobotCrewJob.clear();
			alphaRobotCrewJob = null;
		}
	}
}
