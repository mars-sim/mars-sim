/*
 * Mars Simulation Project
 * Preference.java
 * @date 2022-07-13
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Preference class determines the task preferences of a person.
 */
public class Preference implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** Meta static string. */
	private static final String META = "Meta";

	/** A string list of Tasks. */
	private List<String> taskList;
	/** A map of priority scores for scheduled task. */
	private Map<MetaTask, Integer> priorityMap;
	/** A map of MetaTasks that can only be done once a day. */
	private Map<MetaTask, Boolean> onceADayMap;
	/** A map of MetaTasks that has been accomplished once a day. */
	private Map<MetaTask, Boolean> taskAccomplishedMap;
	/**  A string map of tasks and preference scores. */
	private Map<String, Integer> scoreStringMap;
	/**  A string map of future MetaTasks. */
	private Map<MarsClock, MetaTask> futureTaskMap;
	/**  A connection preference map. */
	private Map<Connection, Integer> connectionMap;

	/** The Person instance. */
	private Person person;
	/** The MarsClock instance. */
	private static MarsClock marsClock;
	
	
	/**
	 * Constructor.
	 * 
	 * @param person
	 */
	public Preference(Person person) {

		this.person = person;

		// These lookups are all static in terms of the Person so they do not
		// need to use the concurent list/maps
		taskList = new ArrayList<>();
		scoreStringMap = new HashMap<>();
		futureTaskMap = new HashMap<>();
		taskAccomplishedMap = new HashMap<>();
		priorityMap = new HashMap<>();
		onceADayMap = new HashMap<>();
		
		connectionMap = new HashMap<>();
		Connection[] connections = Connection.values();
		int size = connections.length;
		for (int i = 0; i < size; i++) {
			int p = RandomUtil.getRandomInt(0, 100);
			connectionMap.put(connections[i], p);
		}
	}

	/*
	 * Initializes the preference score on each particular task. 
	 * TODO Ideally would be good to move this into the Person.initialise method
	 * but the Favorite.activity has to be defined. Maybe loading of the Favorite
	 * could be move to Person.initialise.
	 */
	public void initializePreference() {

		NaturalAttributeManager naturalAttributeManager = person.getNaturalAttributeManager();

		// Computes the adjustment from a person's natural attributes
		double aa = naturalAttributeManager.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE) / 50D * 1.5;
		double t = naturalAttributeManager.getAttribute(NaturalAttributeType.TEACHING) / 50D * 1.5;
		double l = naturalAttributeManager.getAttribute(NaturalAttributeType.LEADERSHIP) / 50D * 1.5;
		double es = (naturalAttributeManager.getAttribute(NaturalAttributeType.ENDURANCE)
				+ naturalAttributeManager.getAttribute(NaturalAttributeType.STRENGTH)) / 100D * 1.5;
		double ag = naturalAttributeManager.getAttribute(NaturalAttributeType.AGILITY) / 500D * 1.5;

		double ss = (naturalAttributeManager.getAttribute(NaturalAttributeType.STRESS_RESILIENCE)
				+ naturalAttributeManager.getAttribute(NaturalAttributeType.SPIRITUALITY)) / 100D * 1.5;
		double se = (naturalAttributeManager.getAttribute(NaturalAttributeType.STRESS_RESILIENCE)
				+ naturalAttributeManager.getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY)) / 100D * 1.5;

		double ca = (naturalAttributeManager.getAttribute(NaturalAttributeType.CONVERSATION) / 50D
				+ naturalAttributeManager.getAttribute(NaturalAttributeType.ATTRACTIVENESS) / 200D) * 1.5;

		double art = naturalAttributeManager.getAttribute(NaturalAttributeType.ARTISTRY) / 50D * 1.5;

		double cou = naturalAttributeManager.getAttribute(NaturalAttributeType.COURAGE) / 50D * 1.5;

		// TODO: how to incorporate EXPERIENCE_APTITUDE ?
		int result = 0;
		FavoriteType hobby = person.getFavorite().getFavoriteActivity();
		for (MetaTask metaTask : MetaTaskUtil.getPersonMetaTasks()) {
			// Set them up in random
			double rand = RandomUtil.getRandomDouble(-5, 5);
			
			// Note: the preference score on a metaTask is modified by a person's natural
			// attributes

			// PART 1 : Influenced by FavoriteType 
			Set<FavoriteType> hobbies = metaTask.getFavourites();
			if (hobbies.contains(hobby)) {
				switch (hobby) {
				case ASTRONOMY:
				case LAB_EXPERIMENTATION:
				case RESEARCH:
					rand += 2 * RandomUtil.getRandomDouble(3);
					break;
					
				case COOKING:
				case FIELD_WORK:
				case GAMING:
				case OPERATION:
				case SPORT:
				case TENDING_PLANTS:
				case TINKERING:
					rand += 1 * RandomUtil.getRandomDouble(3);
					break;

				default:
					
				}
			}
			
			// PART 2 : influenced by natural attribute
			for(TaskTrait trait : metaTask.getTraits()) {
				switch (trait) {
				case ACADEMIC:
					rand += aa + .5;
					break;
				case TEACHING:
					rand += .7 * t + .3 * aa;
					break;
				case STRENGTH:
					rand += .7 * es + .3 * cou;
					break;
				case LEADERSHIP:
					rand += l;
					break;
				case MEDICAL:
					// need patience and stability to administer healing
					rand += (se + ss) / 2D;
					break;
				case TREATMENT:
					// if a person is stress-resilient and relatively emotional stable,
					// he will more likely endure pain and less likely ask to be medicated.
					rand -= se;
					break;
				case PEOPLE:
					rand += ca;
					break;
				case ARTISITC:
					rand += art;
					break;
				case AGILITY:
					rand += ag;
					break;
				case RELAXATION:
					// if a person has high spirituality score and has alternative ways to deal with
					// stress,
					// he will less likely require extra time to relax/sleep/workout/do yoga.
					rand -= ss;
					break;
				default:
					break;
				}
			}

			result = (int) Math.round(rand);
			
			if (result > 8)
				result = 8;
			else if (result < -8)
				result = -8;

			String s = getStringName(metaTask);
			if (!scoreStringMap.containsKey(s)) {
				scoreStringMap.put(s, result);
			}
		}

		for (String key : scoreStringMap.keySet()) {
			taskList.add(key);
		}

		Collections.sort(taskList);
	}


	/**
	 * Obtains the preference score modified by its priority for a meta task.
	 * 
	 * @param metaTask
	 * @return the score
	 */
	public int getPreferenceScore(MetaTask metaTask) {
		int result = 0;

		// DO NOT use MetaTask instance as the key because they are not serialized and
		// hence on a reload will not find a match since the instance will be different.
		String s = getStringName(metaTask);
		if (scoreStringMap.containsKey(s)) {
			result = scoreStringMap.get(s);
		}
		
		if (futureTaskMap.containsValue(metaTask) && (taskAccomplishedMap.get(metaTask) != null)
				&& !taskAccomplishedMap.get(metaTask) && onceADayMap.get(metaTask)) {
			// preference scores are not static. They are influenced by priority scores
			result += obtainPrioritizedScore(metaTask);
		}

		return result;
	}

	/**
	 * Obtains the prioritized score of a meta task from its priority map.
	 * 
	 * @param metaTask
	 * @return the prioritized score
	 */
	private int obtainPrioritizedScore(MetaTask metaTask) {
		int result = 0;
		// iterate over
		Iterator<Entry<MarsClock, MetaTask>> i = futureTaskMap.entrySet().iterator();
		while (i.hasNext()) {
			Entry<MarsClock, MetaTask> entry = i.next();
			MarsClock sch_clock = entry.getKey();
			MetaTask task = entry.getValue();
			if (metaTask.equals(task)) {
				int now = (int) marsClock.getTotalMillisols();
				int sch = (int) sch_clock.getTotalMillisols();
				if (now - sch > 0 && now - sch <= 5) {
					// examine its timestamp down to within 5 millisols
					result += priorityMap.get(task);
				}
			}
		}

		return result;
	}

	/**
	 * Obtains the proper string name of a meta task.
	 * 
	 * @param metaTask {@link MetaTask}
	 * @return string name of a meta task
	 */
	public static String getStringName(MetaTask metaTask) {
		String s = metaTask.getClass().getSimpleName();
		String ss = s.replaceAll("(?!^)([A-Z])", " $1")
				.replace("Meta", "")
				.replace("E V A ", "EVA ")
				.replace("With ", "with ")
				.replace("To ", "to ");
		return ss.trim();
	}

	/**
	 * Obtains the proper string name of a task.
	 * 
	 * @param task {@link Task}
	 * @return string name of a task
	 */
	public static String getStringName(Task task) {
		String s = task.getClass().getSimpleName();
		String ss = s.replaceAll("(?!^)([A-Z])", " $1")
				.replace("E V A ", "EVA ")
				.replace("With ", "with ")
				.replace("To ", "to ");
		return ss.trim();
	}

	/**
	 * Checks if this task is due.
	 * 
	 * @param MetaTask
	 * @return true if it does
	 */
	public boolean isTaskDue(MetaTask mt) {
		// MetaTask mt = convertTask2MetaTask(task);
		if (taskAccomplishedMap.isEmpty()) {
			// if it does not exist (either it is not scheduled or it have been
			// accomplished),
			// the status is true
			return true;
		} else if (taskAccomplishedMap.get(mt) == null)
			return true;
		else
			return taskAccomplishedMap.get(mt);
	}

	/**
	 * Flags this task as being due or not due.
	 * 
	 * @param MetaTask
	 * @param          true if it is due
	 */
	public void setTaskDue(Task task, boolean value) {
		MetaTask mt = convertTask2MetaTask(task);

		// if this accomplished meta task is once-a-day task, remove it.
		if (value && onceADayMap.get(mt) != null && !onceADayMap.isEmpty())
			if (onceADayMap.get(mt) != null && onceADayMap.get(mt)) {
				for (MarsClock c : futureTaskMap.keySet()) {
					if (futureTaskMap.get(c).equals(mt))
						futureTaskMap.remove(c);
				}
				onceADayMap.remove(mt);
				taskAccomplishedMap.remove(mt);
				priorityMap.remove(mt);
			} else
				taskAccomplishedMap.put(mt, value);

	}

	/**
	 * Converts a task to its corresponding meta task.
	 * 
	 * @param a task
	 */
	public static MetaTask convertTask2MetaTask(Task task) {
		MetaTask result = null;
		String name = task.getTaskName();
		result = MetaTaskUtil.getMetaTask(name + META);
		return result;
	}

	public Map<String, Integer> getScoreStringMap() {
		return scoreStringMap;
	}

	//TODO Don't need this really as it is the same for ALL People
	public List<String> getTaskStringList() {
		return taskList;
	}

	public int getConnectionProbability(Connection connection) {
		return connectionMap.get(connection);
	}
	
	public Connection getRandomConnection() {
		return RandomUtil.getWeightedIntegerRandomObject(connectionMap);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
		marsClock = null;
		taskList = null;
		priorityMap = null;
		onceADayMap = null;
		taskAccomplishedMap = null;
		scoreStringMap = null;
		futureTaskMap = null;
	}
}
