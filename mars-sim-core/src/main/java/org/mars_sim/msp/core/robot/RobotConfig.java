/*
 * Mars Simulation Project
 * RobotConfig.java
 * @date 2022-07-05
 * @author Manny Kung
 */
package org.mars_sim.msp.core.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;

/**
 * Provides configuration information about robots. Uses a JDOM document to
 * get the information.
 */
public class RobotConfig {

	// Element names
	private static final String ROBOT_LIST = "robot-list";
	private static final String ROBOT = "robot";

	private static final String ROBOT_MAKE = "make";
	private static final String ROBOT_TYPE = "type";
	
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

	// min-air-pressure
	private double minap;
	// min-temperature
	private double minT;
	// max-temperature
	private double maxT;
	// freezing-time
	private double ft;
	/** A map of various robot specs. */
	private List<RobotSpec> specs;

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
		if (specs != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		specs = new ArrayList<>();
		
		Element root = robotDoc.getRootElement();
		
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
		
		for (Element listElement : listNodes) {
			String type = listElement.getAttributeValue(ROBOT_TYPE).toUpperCase();
			RobotType robotType = RobotType.valueOf(type);
			String robotMake = listElement.getAttributeValue(ROBOT_MAKE);
	
			double standbyPower = ConfigHelper.getOptionalAttributeDouble(listElement, STANDBY_POWER_CONSUMPTION, 0.05D);
			double fcr = ConfigHelper.getOptionalAttributeDouble(listElement, FUEL_CONSUMPTION_RATE, 1D);
			double lowPower = ConfigHelper.getOptionalAttributeDouble(listElement, LOW_POWER_MODE, 20D);

			// Attributes
			Element attributeListElement = listElement.getChild(ROBOTIC_ATTRIBUTE_LIST);
			Map<NaturalAttributeType, Integer> attributeMap = new HashMap<>();
			if (attributeListElement != null) {
				List<Element> attributesElement = attributeListElement.getChildren(ROBOTIC_ATTRIBUTE);
				for (Element attributeElement : attributesElement) {
					String attributeName = attributeElement.getAttributeValue(NAME);
					NaturalAttributeType aType = NaturalAttributeType.valueOf(ConfigHelper.convertToEnumName(attributeName));

					Integer value = Integer.parseInt(attributeElement.getAttributeValue(VALUE));
					attributeMap.put(aType, value);
				}
			}

			// Skills
			Element skillListElement = listElement.getChild(SKILL_LIST);
			Map<SkillType, Integer> skillMap = new HashMap<>();
			if (skillListElement != null) {
				List<Element> skillsElement = skillListElement.getChildren(SKILL);
				for (Element skillElement : skillsElement) {
					String skillName = skillElement.getAttributeValue(NAME);
					SkillType sType = SkillType.valueOf(ConfigHelper.convertToEnumName(skillName));

					Integer level = Integer.parseInt(skillElement.getAttributeValue(LEVEL));
					skillMap.put(sType, level);
				}
			}

			RobotSpec spec = new RobotSpec(
					robotType, robotMake, 
					standbyPower, fcr, lowPower,
					attributeMap,
					skillMap);

			// and keep results for later use
			specs.add(spec);
		}

		// Check all types are defined
		for( RobotType rt : RobotType.values()) {
			// THis will trigger a exception if not defined
			getRobotSpec(rt, null);
		}
	}
	
	/**
	 * Gets the robot spec for a typ and opttional model
	 * 
	 * @param type Robot type is mandatory
	 * @param make Optional name of a particular make model
	 * @return
	 */
	public RobotSpec getRobotSpec(RobotType type, String make) {
		List<RobotSpec> matches = specs.stream().filter(s -> (s.getRobotType() == type)
													&& ((make == null) || (make.equals(s.getMakeModel()))))
										.collect(Collectors.toList());
		// Return the first match
		if (matches.isEmpty()) {
			throw new IllegalArgumentException("No RobotSpec with type=" + type + ", make=" + make);
		}
		return matches.get(0);
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
}
