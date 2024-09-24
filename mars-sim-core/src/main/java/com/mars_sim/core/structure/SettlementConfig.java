/*
 * Mars Simulation Project
 * SettlementConfig.java
 * @date 2023-07-30
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.activities.GroupActivityInfo;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.person.ai.shift.ShiftPattern;
import com.mars_sim.core.person.ai.shift.ShiftSpec;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.building.BuildingCategory;

/**
 * Provides configuration information about settlements templates. Uses a DOM document to
 * get the information.
 */
public class SettlementConfig {

	public record ResourceLimits(int reserve, int max) {}

	// Element names
	private static final String ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN = "rover-life-support-range-error-margin";
	private static final String ROVER_FUEL_RANGE_ERROR_MARGIN = "rover-fuel-range-error-margin";
	private static final String MISSION_CONTROL = "mission-control";
	private static final String LIFE_SUPPORT_REQUIREMENTS = "life-support-requirements";
	private static final String TOTAL_PRESSURE = "total-pressure";
	private static final String PARTIAL_PRESSURE_OF_O2 = "partial-pressure-of-oxygen"; 
	private static final String PARTIAL_PRESSURE_OF_N2 = "partial-pressure-of-nitrogen";
	private static final String PARTIAL_PRESSURE_OF_CO2 = "partial-pressure-of-carbon-dioxide"; 
	private static final String TEMPERATURE = "temperature";
	private static final String RELATIVE_HUMIDITY = "relative-humidity"; 
	private static final String VENTILATION = "ventilation";
	private static final String LOW = "low";
	private static final String HIGH = "high";
	private static final String NAME = "name";


	private static final String TYPE = "type";
	private static final String VALUE = "value";
	private static final String RESOURCE = "resource";

    private static final String SHIFT_PATTERN = "shift-pattern";
	private static final String SHIFT_PATTERNS = "shifts";
	private static final String SHIFT_SPEC = "shift";
	private static final String SHIFT_START = "start";
	private static final String SHIFT_END = "end";
	private static final String SHIFT_PERC = "pop-percentage";
	private static final String LEAVE_PERC = "leave-perc";
	private static final String ROTATION_SOLS = "rotation-sols";

    private static final String ESSENTIAL_RESOURCES = "essential-resources";
	private static final String RESERVE = "reserve";
	private static final String MAX = "max";

	private static final String ACTIVITIES = "activities";
	private static final String SCHEDULE = "schedule";
	private static final String MIN_POP = "minPopulation";
	private static final String MEETING = "meeting";
	private static final String ACTIVITY = "activity";
	private static final String SCORE = "score";
	private static final String DURATION = "duration";
	private static final String WAIT_DURATION = "waitDuration";
	private static final String SCOPE = "scope";
	private static final String LOCATION = "location";
	private static final String POPULATION = "population";
	private static final String IMPACT = "impact";
    private static final String GROUP_ACTIVITIES = "group-activities";
	private static final String CALENDAR = "calendar";

	private double[] roverValues = new double[] { 0, 0 };
	private double[][] lifeSupportValues = new double[2][7];
	
	private List<ShiftPattern> shiftDefinitions = new ArrayList<>();

	private Map<Integer, ResourceLimits> resLimits = new HashMap<>();

	private List<GroupActivitySchedule> rulesets = new ArrayList<>();
	/**
	 * Constructor.
	 *
	 * @param settlementDoc     DOM document with settlement configuration.
	 * @throws Exception if error reading XML document.
	 */
	public SettlementConfig(Document settlementDoc) {
		Element root = settlementDoc.getRootElement();
		loadMissionControl(root.getChild(MISSION_CONTROL));
		loadLifeSupportRequirements(root.getChild(LIFE_SUPPORT_REQUIREMENTS));
		loadResourceLimits(root.getChild(ESSENTIAL_RESOURCES));
		loadShiftPatterns(root.getChild(SHIFT_PATTERNS));
		loadGroupActivity(root.getChild(GROUP_ACTIVITIES));
	}

	private void loadGroupActivity(Element groupActivities) {
		Map<String,GroupActivityInfo> activityPool = loadActivityPool(groupActivities.getChild(ACTIVITIES));
		
		for(Element node : groupActivities.getChildren(SCHEDULE)) {
			String name = node.getAttributeValue(NAME);
			int pop = ConfigHelper.getOptionalAttributeInt(node, MIN_POP, -1);

			// Load the specials; only 1 per type
			List<GroupActivityInfo> meetings = new ArrayList<>();
			Map<GroupActivityType,GroupActivityInfo> specials = new EnumMap<>(GroupActivityType.class);
			for(Element ra : node.getChildren(MEETING)) {
				String meetingName = ra.getAttributeValue(NAME);
				var ga = activityPool.get(meetingName);
				if (ga == null) {
					throw new IllegalArgumentException("No Activity called " + name);
				}

				// Is it a special meeting
				String meetingType = ra.getAttributeValue(TYPE);
				if (meetingType != null) {
					GroupActivityType type = ConfigHelper.getEnum(GroupActivityType.class,
																meetingType);
					if (specials.containsKey(type)) {
						throw new IllegalStateException("There is already a meeting defined for type :" + type);
					}
					specials.put(type, ga);
				}
				else {
					meetings.add(ga);
				}
			}
			rulesets.add(new GroupActivitySchedule(name, pop, specials, meetings));
		}

		// Order rulles sets in increasing minimum population
		rulesets.sort(Comparator.comparingInt(GroupActivitySchedule::minPop).reversed());
	}

	private Map<String, GroupActivityInfo> loadActivityPool(Element activityPool) {
		Map<String, GroupActivityInfo> pool = new HashMap<>();

		// Load pool of reusable activity meetings
		for(Element ra : activityPool.getChildren(ACTIVITY)) {
			var act = parseGroupActivity(ra);
			pool.put(act.name(), act);
		}
		return pool;
	}

	/**
	 * Converts a XML element into a GroupActivityInfo.
	 * 
	 * @param ra
	 * @return
	 */
	private static GroupActivityInfo parseGroupActivity(Element ra) {
		String name = ra.getAttributeValue(NAME);

		int score = ConfigHelper.getOptionalAttributeInt(ra, SCORE, -1);
		int wait = ConfigHelper.getAttributeInt(ra, WAIT_DURATION);
		int duration = ConfigHelper.getAttributeInt(ra, DURATION);
		double pop = ConfigHelper.getAttributeDouble(ra, POPULATION);
		TaskScope scope = ConfigHelper.getEnum(TaskScope.class, ra.getAttributeValue(SCOPE));
		String locationText = ra.getAttributeValue(LOCATION);
		BuildingCategory meetingPlace = BuildingCategory.LIVING;
		if (locationText != null) {
			meetingPlace = ConfigHelper.getEnum(BuildingCategory.class, locationText);
		}

		ExperienceImpact impact = GroupActivityInfo.DEFAULT_IMPACT;
		Element impactEl = ra.getChild(IMPACT);
		if (impactEl != null) {
			impact = ConfigHelper.parseImpact(impactEl);
		}
		var eventCal = ConfigHelper.parseEventCalendar(ra.getChild(CALENDAR));
		return new GroupActivityInfo(name, wait, duration, eventCal, pop, score, scope,
										meetingPlace, impact);
	}

	/**
	 * Finds a activity schedule by name.
	 * 
	 * @param name 
	 * @return Selected; could be null
	 */
	public GroupActivitySchedule getActivityByName(String name) {
		var found = rulesets.stream().filter(a -> name.equalsIgnoreCase(a.name())).findFirst();
		if (found.isPresent()) {
			return found.get();
		}
		throw new IllegalArgumentException("Cannot find a Activity Scheduled called " + name);
	}

	/**
	 * Finds a activity rules set which has the highest minPopulation than this population
	 * size can cover.
	 * 
	 * @param popSize
	 * @return Selected; could be null
	 */
	public GroupActivitySchedule getActivityByPopulation(int popSize) {
		// Rule sets are ordered in terms of decreasing population to find first that is smaller than 
		// target population
		for(var a : rulesets) {
			if ((a.minPop() <= popSize) && (a.minPop() > 0)) {
				return a;
			}
		}
		return null;
	}

	private void loadResourceLimits(Element limits) {

		List<Element> resources = limits.getChildren(RESOURCE);
		for(Element node : resources) {
			String resName = node.getAttributeValue(NAME);
			int resId = ResourceUtil.findIDbyAmountResourceName(resName);
			if (resId <= 0) {
				throw new IllegalArgumentException("Cannot find essential resource called " + resName);
			}

			int reserve = Integer.parseInt(node.getAttributeValue(RESERVE));
			int max = Integer.parseInt(node.getAttributeValue(MAX));

			resLimits.put(resId, new ResourceLimits(reserve, max));
		}
	}

	public Map<Integer,ResourceLimits> getEssentialResources() {
		return resLimits;
	}

	public double[] getRoverValues() {
		return roverValues;
	}

	/**
	 * Loads the shift patterns details.
	 * 
	 * @throws Exception if error reading XML document.
	 */
	private void loadShiftPatterns(Element shiftPatterns) {

		List<Element> shiftNodes = shiftPatterns.getChildren(SHIFT_PATTERN);
		for(Element node : shiftNodes) {
			String name = node.getAttributeValue(NAME);

			int rotSol = ConfigHelper.getOptionalAttributeInt(node, ROTATION_SOLS, 10);
			int leave = ConfigHelper.getOptionalAttributeInt(node, LEAVE_PERC, 10);
			int minPop = ConfigHelper.getOptionalAttributeInt(node, MIN_POP, -1);

			List<ShiftSpec> shiftSpecs = new ArrayList<>();
			List<Element> specNodes = node.getChildren(SHIFT_SPEC);
			for(Element spec : specNodes) {
				String sname = spec.getAttributeValue(NAME);
				int start = Integer.parseInt(spec.getAttributeValue(SHIFT_START));
				int end = Integer.parseInt(spec.getAttributeValue(SHIFT_END));
				int population = Integer.parseInt(spec.getAttributeValue(SHIFT_PERC));
				
				shiftSpecs.add(new ShiftSpec(sname, start, end, population));
			}

			shiftDefinitions.add(new ShiftPattern(name, shiftSpecs, rotSol, leave, minPop));
		}

		// Order rules sets in increasing minimum population
		shiftDefinitions.sort(Comparator.comparingInt(ShiftPattern::getMinPopulation).reversed());
	}

	/**
	 * Finds a shift pattern by name.
	 * 
	 * @param name
	 * @return
	 */
	public ShiftPattern getShiftByName(String name) {
		var found = shiftDefinitions.stream().filter(a -> name.equalsIgnoreCase(a.getName())).findFirst();
		if (found.isPresent()) {
			return found.get();
		}
		throw new IllegalArgumentException("No shift pattern called " + name);
	}

	/**
	 * Finds a shift pattern  which has the highest minPopulation than this population
	 * size can cover.
	 * 
	 * @param popSize
	 * @return Selected; could be null
	 */
	public ShiftPattern getShiftByPopulation(int popSize) {
		// Pattern are order in terms of decreasing population sp find first that is smaller than 
		// target population
		for(var a : shiftDefinitions) {
			if ((a.getMinPopulation() <= popSize) && (a.getMinPopulation() > 0)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Loads the rover range margin error from the mission control parameters of a
	 * settlement from the XML document.
	 *
	 * @return range margin.
	 * @throws Exception if error reading XML document.
	 */
	private void loadMissionControl(Element missionControlElement) {
		if (roverValues[0] != 0 || roverValues[1] != 0) {
			return;
		}

		Element lifeSupportRange = missionControlElement.getChild(ROVER_LIFE_SUPPORT_RANGE_ERROR_MARGIN);
		Element fuelRange = missionControlElement.getChild(ROVER_FUEL_RANGE_ERROR_MARGIN);

		roverValues[0] = Double.parseDouble(lifeSupportRange.getAttributeValue(VALUE));
		if (roverValues[0] < 1.0 || roverValues[0] > 3.0)
			throw new IllegalStateException(
					"Error in SettlementConfig.xml: rover life support range error margin is beyond acceptable range.");

		roverValues[1] = Double.parseDouble(fuelRange.getAttributeValue(VALUE));
		if (roverValues[1] < 1.0 || roverValues[1] > 3.0)
			throw new IllegalStateException(
					"Error in SettlementConfig.xml: rover fuel range error margin is beyond acceptable range.");
	}

	/**
	 * Loads the life support requirements from the XML document.
	 *
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	public double[][] getLifeSupportRequirements() {
		return lifeSupportValues;
	}

	/**
	 * Loads the life support requirements from the XML document.
	 *
	 * @return an array of double.
	 * @throws Exception if error reading XML document.
	 */
	private void loadLifeSupportRequirements(Element req) {
		if (lifeSupportValues[0][0] != 0) {
			// testing only the value at [0][0]
			return;
		}

		String[] types = new String[] {
				TOTAL_PRESSURE,
				PARTIAL_PRESSURE_OF_O2,
				PARTIAL_PRESSURE_OF_N2,
				PARTIAL_PRESSURE_OF_CO2,
				TEMPERATURE,
				RELATIVE_HUMIDITY,
				VENTILATION};

		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < types.length; i++) {
				double [] t = getLowHighValues(req, types[i]);
				lifeSupportValues[j][i] = t[j];
			}
		}
	}

	private double[] getLowHighValues(Element element, String name) {
		Element el = element.getChild(name);
		double a = Double.parseDouble(el.getAttributeValue(LOW));
		double b = Double.parseDouble(el.getAttributeValue(HIGH));

		return new double[] { a, b };
	}
}
