/*
 * Mars Simulation Project
 * RobotConfig.java
 * @date 2022-06-16
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

	private List<String> alphaRobotCrewName; // = new ArrayList<String>();
	private List<String> alphaRobotCrewType; // = new ArrayList<String>();
	private List<String> alphaRobotCrewJob; // = new ArrayList<String>();

	// Element names
	private static final String ROBOT_LIST = "robot-list";
	private static final String ROBOT = "robot";

	private static final String ROBOT_NAME = "name";
	private static final String ROBOT_TYPE = "type";
	private static final String SETTLEMENT = "settlement";
	private static final String JOB = "job";
	
	private static final String POWER_CONSUMPTION_RATE = "power-consumption-rate";
	private static final String POWER_DEPRIVATION_TIME = "power-deprivation-time";
	private static final String FUEL_CONSUMPTION_RATE = "fuel-consumption-rate";
	private static final String LOW_POWER_MODE_START_TIME = "low-power-mode-start-time";
	private static final String MIN_AIR_PRESSURE = "min-air-pressure";
	private static final String DECOMPRESSION_TIME = "decompression-time";
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

	// power-consumption-rate
	private static double pcr;
	// fuel-consumption-rate
	private static double fcr ;
	// low-power-mode-start-time
	private static double lpmst;
	// min-air-pressure
	private static double minap;
	// min-temperature
	private static double minT;
	// max-temperature
	private static double maxT;
	// freezing-time
	private static double ft;
	
	private Document robotDoc;
	
	private Map<RobotType, RobotSpec> map;

	/**
	 * Constructor
	 * 
	 * @param robotDoc the robot config DOM document.
	 */
	public RobotConfig(Document robotDoc) {
		this.robotDoc = robotDoc;
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
		Map<RobotType, RobotSpec> newMap = new HashMap<>();
		
		Element root = robotDoc.getRootElement();
	
		Element pcrElement = root.getChild("power-consumption-rate");
		pcr = Double.parseDouble(pcrElement.getAttributeValue(VALUE));
		
		Element fcrElement = root.getChild("fuel-consumption-rate");
		fcr = Double.parseDouble(fcrElement.getAttributeValue(VALUE));
		
		Element lpmstElement = root.getChild("low-power-mode-start-time");
		lpmst = Double.parseDouble(lpmstElement.getAttributeValue(VALUE));
		
		Element minapElement = root.getChild("min-air-pressure");
		minap = Double.parseDouble(minapElement.getAttributeValue(VALUE));
		
		Element minTElement = root.getChild("min-temperature");
		minT = Double.parseDouble(minTElement.getAttributeValue(VALUE));
		
		Element maxTElement = root.getChild("max-temperature");
		maxT = Double.parseDouble(maxTElement.getAttributeValue(VALUE));
		
		Element ftElement = root.getChild("freezing-time");
		ft = Double.parseDouble(ftElement.getAttributeValue(VALUE));
		
		Element rlElement = root.getChild(ROBOT_LIST);
		List<Element> listNodes = rlElement.getChildren(ROBOT);
		for (Element listElement : listNodes) {
			String name = listElement.getAttributeValue(ROBOT_NAME).toLowerCase();
			String type = listElement.getAttributeValue(ROBOT_TYPE).toLowerCase();
			RobotType robotType = RobotType.valueOfIgnoreCase(type);
			String settlementName = listElement.getAttributeValue(SETTLEMENT).toLowerCase();
			String jobName = listElement.getAttributeValue(JOB).toLowerCase();

			// Attributes
			Element attributeListElement = listElement.getChild(ROBOTIC_ATTRIBUTE_LIST);
			if (attributeListElement != null) {
				Map<String, Double> attributeMap = new HashMap<>();
				List<Element> attributesElement = attributeListElement.getChildren(ROBOTIC_ATTRIBUTE);
				for (Element attributeElement : attributesElement) {
					String attributeName = attributeElement.getAttributeValue(NAME).toLowerCase();
					double value = Double.parseDouble(attributeElement.getAttributeValue(VALUE));
					attributeMap.put(attributeName, value);
				}
			}

			// Skills
			Element skillListElement = listElement.getChild(SKILL_LIST);
			if (skillListElement != null) {
				Map<String, Double> skillMap = new HashMap<>();
				List<Element> skillsElement = skillListElement.getChildren(SKILL);
				for (Element skillElement : skillsElement) {
					String skillName = skillElement.getAttributeValue(NAME).toLowerCase();
					double level = Double.parseDouble(skillElement.getAttributeValue(LEVEL));
					skillMap.put(skillName, level);
				}
			}

			RobotSpec spec = new RobotSpec(
					name, 
					robotType, 
					settlementName, 
					jobName);

			// and keep results for later use
			newMap.put(robotType, spec);
		}
		
		map = Collections.unmodifiableMap(newMap);
	}
	
	
	/**
	 * Gets the robot map.
	 * 
	 * @return
	 */
	public Map<RobotType, RobotSpec> getRobotMap() {
		return map;
	}
	
	/**
	 * Gets the Power consumption rate.
	 * 
	 * @return Power rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getPowerConsumptionRate() {
		return getValueAsDouble(POWER_CONSUMPTION_RATE);
	}

	/**
	 * Gets the fuel consumption rate.
	 * 
	 * @return fuel rate (kg/sol)
	 * @throws Exception if consumption rate could not be found.
	 */
	public double getFuelConsumptionRate() {
		return getValueAsDouble(FUEL_CONSUMPTION_RATE);
	}

	/**
	 * Gets the Power deprivation time.
	 * 
	 * @return Power time in sols.
	 * @throws Exception if Power deprivation time could not be found.
	 */
	public double getPowerDeprivationTime() {
		return getValueAsDouble(POWER_DEPRIVATION_TIME);
	}

	/**
	 * Gets the low power mode start time.
	 * 
	 * @return low power mode start time in sols.
	 * @throws Exception if low power mode start time could not be found.
	 */
	public double getLowPowerModeStartTime() {
		return getValueAsDouble(LOW_POWER_MODE_START_TIME);
	}

	/**
	 * Gets the required air pressure.
	 * 
	 * @return air pressure in Pa.
	 * @throws Exception if air pressure could not be found.
	 */
	public double getMinAirPressure() {
		return getValueAsDouble(MIN_AIR_PRESSURE);
	}

	/**
	 * Gets the max decompression time a robot can survive.
	 * 
	 * @return decompression time in millisols.
	 * @throws Exception if decompression time could not be found.
	 */
	public double getDecompressionTime() {
		return getValueAsDouble(DECOMPRESSION_TIME);
	}

	/**
	 * Gets the minimum temperature a robot can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if min temperature cannot be found.
	 */
	public double getMinTemperature() {
		return getValueAsDouble(MIN_TEMPERATURE);
	}

	/**
	 * Gets the maximum temperature a robot can tolerate.
	 * 
	 * @return temperature in celsius
	 * @throws Exception if max temperature cannot be found.
	 */
	public double getMaxTemperature() {
		return getValueAsDouble(MAX_TEMPERATURE);
	}

	/**
	 * Gets the time a robot can survive below minimum temperature.
	 * 
	 * @return freezing time in millisols.
	 * @throws Exception if freezing time could not be found.
	 */
	public double getFreezingTime() {
		return getValueAsDouble(FREEZING_TIME);
	}

	/**
	 * Gets the configured robot's starting settlement.
	 * 
	 * @param index the robot's index.
	 * @return the settlement name or null if none.
	 * @throws Exception if error in XML parsing.
	 */
	public String getConfiguredRobotSettlement(int index) {
		return getValueAsString(index, SETTLEMENT);
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
			return getValueAsString(index, JOB);
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
	 * Gets the number of robots configured for the simulation.
	 * 
	 * @return number of robots.
	 * @throws Exception if error in XML parsing.
	 */
	public int getNumberOfConfiguredRobots() {
		Element root = robotDoc.getRootElement();
		Element robotList = root.getChild(ROBOT_LIST);
		List<Element> robotNodes = robotList.getChildren(ROBOT);
		if (robotNodes != null)
			return robotNodes.size();
		else
			return 0;
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
			return getValueAsString(index, ROBOT_NAME);
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
			return RobotType.valueOfIgnoreCase(getValueAsString(index, ROBOT_TYPE));
	}

	/**
	 * Gets a map of the configured robot's natural attributes.
	 * 
	 * @param index the robot's index.
	 * @return map of natural attributes (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getRoboticAttributeMap(int index) {
		Map<String, Integer> result = new ConcurrentHashMap<>();
		Element root = robotDoc.getRootElement();
		Element robotList = root.getChild(ROBOT_LIST);
		Element robotElement = (Element) robotList.getChildren(ROBOT).get(index);
		List<Element> nodes = robotElement.getChildren(ROBOTIC_ATTRIBUTE_LIST);

		if ((nodes != null) && (nodes.size() > 0)) {
			Element attributeList = nodes.get(0);
			int attributeNum = attributeList.getChildren(ROBOTIC_ATTRIBUTE).size();

			for (int x = 0; x < attributeNum; x++) {
				Element attributeElement = (Element) attributeList.getChildren(ROBOTIC_ATTRIBUTE).get(x);
				String name = attributeElement.getAttributeValue(NAME);
				Integer value = Integer.valueOf(attributeElement.getAttributeValue(VALUE));
				result.put(name, value);
			}
		}
		return result;
	}

	private String getValueAsString(int index, String param) {
		Element root = robotDoc.getRootElement();
		Element robotList = root.getChild(ROBOT_LIST);
		Element robotElement = (Element) robotList.getChildren(ROBOT).get(index);
		return robotElement.getAttributeValue(param);
	}

	private double getValueAsDouble(String child) {
		Element root = robotDoc.getRootElement();
		Element element = root.getChild(child);
		String str = element.getAttributeValue(VALUE);
		return Double.parseDouble(str);
	}

	/**
	 * Gets a map of the configured robot's skills.
	 * 
	 * @param index the robot's index.
	 * @return map of skills (empty map if not found).
	 * @throws Exception if error in XML parsing.
	 */
	public Map<String, Integer> getSkillMap(int index) {
		Map<String, Integer> result = new ConcurrentHashMap<>();
		Element root = robotDoc.getRootElement();
		Element robotList = root.getChild(ROBOT_LIST);
		Element robotElement = (Element) robotList.getChildren(ROBOT).get(index);
		List<Element> skillListNodes = robotElement.getChildren(SKILL_LIST);
		if ((skillListNodes != null) && (skillListNodes.size() > 0)) {
			Element skillList = skillListNodes.get(0);
			int skillNum = skillList.getChildren(SKILL).size();
			for (int x = 0; x < skillNum; x++) {
				Element skillElement = (Element) skillList.getChildren(SKILL).get(x);
				String name = skillElement.getAttributeValue(NAME);
				Integer level = Integer.valueOf(skillElement.getAttributeValue(LEVEL));
				result.put(name, level);
			}
		}
		return result;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		robotDoc = null;

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
