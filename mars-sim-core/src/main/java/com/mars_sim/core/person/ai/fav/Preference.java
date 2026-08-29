/*
 * Mars Simulation Project
 * Preference.java
 * @date 2026-08-23
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.fav;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.person.Connection;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.meta.ConnectOnlineMeta;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.MetaTaskUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.tool.RandomUtil;

/**
 * The Preference class determines the task preferences of a person.
 */
public class Preference implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private final int WEIGHT = 2;
	
	/** A map of MetaTasks that can only be done once a day. */
	private Map<Integer, Boolean> onceADayMap;
	/** A map of MetaTasks that has been accomplished once a day. */
	private Map<Integer, Boolean> taskAccomplishedMap;
	/**  A map of meta task identifier and preference scores. */
	private Map<Integer, Integer> scoreMap;
	/**  A connection preference map. */
	private Map<Connection, Integer> connectionMap;

	
	/** The Person instance. */
	private Person person;
	
	
	/**
	 * Constructor.
	 * 
	 * @param person
	 */
	public Preference(Person person) {

		this.person = person;

		// These lookups are all static in terms of the Person so they do not
		// need to use the concurrent list/maps
		scoreMap = new HashMap<>();
		taskAccomplishedMap = new HashMap<>();
		onceADayMap = new HashMap<>();
		connectionMap = new HashMap<>();
	}

	/*
	 * Initializes the preference score on each particular task. 
	 * 
	 * Note: the favorite activity must be pre-defined.
	 */
	public void initializePreference() {

		NaturalAttributeManager naturalAttributeManager = person.getNaturalAttributeManager();

		// Computes the adjustment from a person's natural attributes
		double aa = naturalAttributeManager.getAttribute(NaturalAttributeType.ACADEMIC_APTITUDE) / 50D * 1.5;
		
		double discipline = naturalAttributeManager.getAttribute(NaturalAttributeType.DISCIPLINE) / 50D * 1.5;
		double org = naturalAttributeManager.getAttribute(NaturalAttributeType.ORGANIZATION) / 50D * 1.5;
		
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

		// Note: how to incorporate EXPERIENCE_APTITUDE ?
		
		int result = 0;
		FavoriteType hobby = person.getFavorite().getFavoriteActivity();
		
		List<MetaTask> metatasks = MetaTaskUtil.getPersonMetaTasks();

		int connectOnlineMetaID = -1;
		
		for (MetaTask metaTask : metatasks) {
			
			if (metaTask instanceof ConnectOnlineMeta m) {
				connectOnlineMetaID = m.getIdentifier();
			}
			
			
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
				case TENDING_FARM:
				case TINKERING:
					rand += 1 * RandomUtil.getRandomDouble(3);
					break;

				default:
					
				}
			}
			
			// PART 2 : influenced by natural attribute
			for (TaskTrait trait : metaTask.getTraits()) {
				switch (trait) {
				case ACADEMIC:
					rand += aa + .5;
					break;
				case AGILITY:
					rand += ag;
					break;
				case ARTISTIC:
					rand += art;
					break;
				case DISCIPLINE:
					rand += discipline;
					break;					
				case LEADERSHIP:
					rand += l;
					break;
				case MEDICAL:
					// need patience and stability to administer healing
					rand += (se + ss) / 2D;
					break;
				case ORGANIZATION:
					rand += org;
					break;
				case PEOPLE:
					rand += ca;
					break;
				case RELAXATION:
					// if a person has high spirituality score and has alternative ways to deal with
					// stress,
					// he will less likely require extra time to relax/sleep/workout/do yoga.
					rand -= ss;
					break;
				case STRENGTH:
					rand += .7 * es + .3 * cou;
					break;
				case TEACHING:
					rand += .7 * t + .3 * aa;
					break;						
				case TREATMENT:
					// if a person is stress-resilient and relatively emotional stable,
					// he will more likely endure pain and less likely ask to be medicated.
					rand -= se;
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
		
			int id = metaTask.getIdentifier();
			
			if (!scoreMap.containsKey(id)) {
				scoreMap.put(id, result);
			}
		}
		
		int connectionScore = scoreMap.get(connectOnlineMetaID);
		initializeConnections(connectionScore);
	}

	/**
	 * Initializes probability for each connection.
	 * 
	 * @param scoreMap
	 */
	private void initializeConnections(int scoreMap) {
		
		Connection[] connections = Connection.values();
		int size = connections.length;
		
		for (int i = 0; i < size; i++) {
			int p = RandomUtil.getRandomInt(0, 100) + scoreMap * WEIGHT;
			if (p < 5)
				p = 5;
			if (p > 100)
				p = 100;
			connectionMap.put(connections[i], p);
		}
	}

	/**
	 * Obtains the preference score modified by its priority for a meta task.
	 * 
	 * @param metaTask
	 * @return the score
	 */
	public int getPreferenceScore(int id) {
		int result = 0;

		// DO NOT use MetaTask instance as the key because they are not serialized and
		// hence on a reload will not find a match since the instance will be different.
//		String s = getStringName(metaTask);

//		int id = metaTask.getIdentifier();
		
//		System.out.println("MetaTask id " + id+ ". " + metaTask.getName() + ". " + metaTask.getSimpleName() + ". " + s);
		
		if (scoreMap.containsKey(id)) {
			result = scoreMap.get(id);
		}

		return result;
	}


	/**
	 * Checks if this task is due.
	 * 
	 * @param id
	 * @return true if it does
	 */
	public boolean isTaskDue(int id) {
		if (taskAccomplishedMap.isEmpty()) {
			// if it does not exist (either it is not scheduled or it have been
			// accomplished),
			// the status is true
			return true;
		} else if (taskAccomplishedMap.get(id) == null)
			return true;
		else
			return taskAccomplishedMap.get(id);
	}

	/**
	 * Flags this task as being due or not due.
	 * 
	 * @param MetaTask
	 * @param          true if it is due
	 */
	public void setTaskDue(Task task, boolean value) {
		MetaTask mt = MetaTaskUtil.getMetaTypeFromTask(task);
		int id = mt.getIdentifier();
		
		// if this accomplished meta task is once-a-day task, remove it.
		if (value && onceADayMap.get(id) != null && !onceADayMap.isEmpty())
			if (onceADayMap.get(id) != null && onceADayMap.get(id)) {
				onceADayMap.remove(id);
				taskAccomplishedMap.remove(id);
			} else
				taskAccomplishedMap.put(id, value);

	}

	public Map<Integer, Integer> getScoreStringMap() {
		return scoreMap;
	}

	public Connection getRandomConnection() {
		return RandomUtil.getWeightedIntegerRandomObject(connectionMap);
	}
	
	public int getConnectionScore(Connection connection) {
		return connectionMap.get(connection);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		person = null;
		onceADayMap.clear();
		onceADayMap = null;
		taskAccomplishedMap.clear();
		taskAccomplishedMap = null;
		scoreMap.clear();
		scoreMap = null;
	}
}
