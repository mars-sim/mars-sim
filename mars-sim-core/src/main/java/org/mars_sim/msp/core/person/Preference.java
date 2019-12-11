/**
 * Mars Simulation Project
 * Preference.java
 * @version 3.1.0 2017-02-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.task.meta.AssistScientificStudyResearcherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CompileScientificStudyResultsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConnectWithEarthMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConsolidateContainersMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CookMealMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalIceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalRegolithMeta;
import org.mars_sim.msp.core.person.ai.task.meta.EatDrinkMeta;
import org.mars_sim.msp.core.person.ai.task.meta.HaveConversationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.InviteStudyCollaboratorMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ListenToMusicMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureConstructionMaterialsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ObserveAstronomicalObjectsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PeerReviewStudyPaperMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformLaboratoryExperimentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformLaboratoryResearchMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformMathematicalModelingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PlayHoloGameMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PrepareDessertMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ProduceFoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ProposeScientificStudyMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReadMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RecordActivityMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RelaxMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairEVAMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RequestMedicalTreatmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RespondToStudyInvitationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReviewJobReassignmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReviewMissionPlanMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.StudyFieldSamplesMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TeachMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TendGreenhouseMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TreatMedicalPatientMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WriteReportMeta;
import org.mars_sim.msp.core.person.ai.task.meta.YogaMeta;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The Preference class determines the task preferences of a person
 */
public class Preference implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** Meta static string. */
	private static final String META = "Meta";
	/** The cache for mission sol. */
	private int solCache = 0;

	/** A list of MetaTasks. */
//	private List<MetaTask> metaTaskList;
	/** A string list of Tasks. */
	private List<String> taskList;
	/** A map of MetaTasks and preference scores. */
	private Map<MetaTask, Integer> scoreMap;
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

	/** The Person instance. */
	private Person person;
	/** The MarsClock instance. */
	private static MarsClock marsClock;
	
	
	public Preference(Person person) {

		this.person = person;

//		metaTaskList = MetaTaskUtil.getAllMetaTasks();
		taskList = new ArrayList<>();

		scoreMap = new ConcurrentHashMap<>();
		scoreStringMap = new ConcurrentHashMap<>();

		futureTaskMap = new ConcurrentHashMap<>();
		taskAccomplishedMap = new ConcurrentHashMap<>();
		priorityMap = new ConcurrentHashMap<>();
		onceADayMap = new ConcurrentHashMap<>();

		// scheduleTask("WriteReportMeta", 600, 900);
		// scheduleTask("ConnectWithEarthMeta", 700, 950);

	}

	/*
	 * Initialize the preference score on each particular task
	 */
	public void initializePreference() {

		NaturalAttributeManager naturalAttributeManager = person.getNaturalAttributeManager();

		int result = 0;
		double ast = 0;
		double cook = 0;
		double field = 0;
		double game = 0;
		double lab = 0;
		double ops = 0;
		double research = 0;
		double sport = 0;
		double plant = 0;
		double tinker = 0;

		FavoriteType hobby = person.getFavorite().getFavoriteActivity();
		
		if (hobby == FavoriteType.ASTRONOMY)
			ast = 2;
		
		if (hobby == FavoriteType.COOKING)
			cook = 1;
		
		if (hobby == FavoriteType.FIELD_WORK)
			field = 1;
			
		if (hobby == FavoriteType.GAMING)
			game = 1;
		
		if (hobby == FavoriteType.LAB_EXPERIMENTATION)
			lab = 2;
		
		if (hobby == FavoriteType.OPERATION)
			ops = 1;
		
		if (hobby == FavoriteType.RESEARCH)
			research = 2;
		
		if (hobby == FavoriteType.SPORT)
			sport = 1;
		
		if (hobby == FavoriteType.TENDING_PLANTS)
			plant = 1;
		
		if (hobby == FavoriteType.TINKERING)
			tinker = 1;
			
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

		Iterator<MetaTask> i = MetaTaskUtil.getMetaTasksSet().iterator();
		while (i.hasNext()) {
			MetaTask metaTask = i.next();

			// Set them up in random
			double rand = RandomUtil.getRandomDouble(5.0) - RandomUtil.getRandomDouble(5.0);
			
			// Note: the preference score on a metaTask is modified by a person's natural
			// attributes
			// TODO: turn these hardcoded relationship between attributes and task
			// preferences into a XML/JSON file

			// PART 1 : Influenced by FavoriteType 
			
			if (metaTask instanceof ObserveAstronomicalObjectsMeta)
				rand += ast * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof EatDrinkMeta 
					|| metaTask instanceof ProduceFoodMeta 
					|| metaTask instanceof CookMealMeta
					|| metaTask instanceof PrepareDessertMeta)
				rand += cook * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof StudyFieldSamplesMeta)
				rand += field * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof PlayHoloGameMeta)
				rand += game * RandomUtil.getRandomDouble(3);			
			
			if (metaTask instanceof PerformLaboratoryExperimentMeta
					|| metaTask instanceof PerformLaboratoryResearchMeta)
				rand += lab * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof ConsolidateContainersMeta || metaTask instanceof ConstructBuildingMeta
					|| metaTask instanceof DigLocalIceMeta || metaTask instanceof DigLocalRegolithMeta
					|| metaTask instanceof LoadVehicleEVAMeta || metaTask instanceof LoadVehicleGarageMeta 
					|| metaTask instanceof UnloadVehicleEVAMeta || metaTask instanceof UnloadVehicleGarageMeta 
					|| metaTask instanceof SalvageBuildingMeta || metaTask instanceof SalvageGoodMeta
					|| metaTask instanceof RepairEVAMalfunctionMeta	|| metaTask instanceof RepairMalfunctionMeta
					|| metaTask instanceof MaintenanceMeta || metaTask instanceof MaintenanceEVAMeta)
				rand += ops * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof AssistScientificStudyResearcherMeta
					|| metaTask instanceof CompileScientificStudyResultsMeta
					|| metaTask instanceof PeerReviewStudyPaperMeta
					|| metaTask instanceof PerformLaboratoryResearchMeta
					|| metaTask instanceof PerformMathematicalModelingMeta
					|| metaTask instanceof ProposeScientificStudyMeta
					|| metaTask instanceof RespondToStudyInvitationMeta)
				rand += research * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof PlayHoloGameMeta)
				rand += game * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof WorkoutMeta)
				rand += sport * RandomUtil.getRandomDouble(3);

			if (metaTask instanceof TendGreenhouseMeta)
				rand += plant * RandomUtil.getRandomDouble(3);
			
			if (metaTask instanceof ConsolidateContainersMeta || metaTask instanceof ConstructBuildingMeta
					|| metaTask instanceof SalvageBuildingMeta || metaTask instanceof SalvageGoodMeta
					|| metaTask instanceof RepairEVAMalfunctionMeta	|| metaTask instanceof RepairMalfunctionMeta
					|| metaTask instanceof MaintenanceMeta || metaTask instanceof MaintenanceEVAMeta
					|| metaTask instanceof ManufactureConstructionMaterialsMeta || metaTask instanceof ManufactureGoodMeta)
				rand += tinker * RandomUtil.getRandomDouble(3);
			
			
			// PART 2 : influenced by natural attribute
			
			// Academically driven
			if (metaTask instanceof AssistScientificStudyResearcherMeta
					|| metaTask instanceof CompileScientificStudyResultsMeta || metaTask instanceof MaintenanceEVAMeta
					|| metaTask instanceof MaintenanceMeta 
					|| metaTask instanceof ObserveAstronomicalObjectsMeta
					|| metaTask instanceof PeerReviewStudyPaperMeta
					|| metaTask instanceof PerformLaboratoryExperimentMeta
					|| metaTask instanceof PerformLaboratoryResearchMeta
					|| metaTask instanceof PerformMathematicalModelingMeta
					|| metaTask instanceof ProposeScientificStudyMeta
					|| metaTask instanceof RespondToStudyInvitationMeta 
					|| metaTask instanceof StudyFieldSamplesMeta)
				rand += aa + .5;

			// Teaching excellence
			if (metaTask instanceof TeachMeta || metaTask instanceof ReadMeta
					|| metaTask instanceof PeerReviewStudyPaperMeta || metaTask instanceof WriteReportMeta)
				rand += .7 * t + .3 * aa;

			// Endurance & strength related
			if (metaTask instanceof ConsolidateContainersMeta || metaTask instanceof ConstructBuildingMeta
					|| metaTask instanceof DigLocalIceMeta || metaTask instanceof DigLocalRegolithMeta
					|| metaTask instanceof EatDrinkMeta 
					|| metaTask instanceof LoadVehicleEVAMeta || metaTask instanceof LoadVehicleGarageMeta 
					|| metaTask instanceof UnloadVehicleEVAMeta || metaTask instanceof UnloadVehicleGarageMeta 
					|| metaTask instanceof SalvageBuildingMeta || metaTask instanceof SalvageGoodMeta
					|| metaTask instanceof RepairEVAMalfunctionMeta	|| metaTask instanceof RepairMalfunctionMeta
					|| metaTask instanceof MaintenanceMeta || metaTask instanceof MaintenanceEVAMeta)
				rand += .7 * es + .3 * cou;

			// leadership
			if (metaTask instanceof ProposeScientificStudyMeta || metaTask instanceof InviteStudyCollaboratorMeta
					|| metaTask instanceof ReviewJobReassignmentMeta || metaTask instanceof ReviewMissionPlanMeta
					|| metaTask instanceof WriteReportMeta)
				rand += l;

			// stress & emotion & spiritually
			if (metaTask instanceof TreatMedicalPatientMeta)
				// need patience and stability to administer healing
				rand += (se + ss) / 2D;

			// stress & emotion
			if (metaTask instanceof RequestMedicalTreatmentMeta || metaTask instanceof YogaMeta)
				// if a person is stress-resilient and relatively emotional stable,
				// he will more likely endure pain and less likely ask to be medicated.
				rand -= se;

			// people oriented
			if (metaTask instanceof ConnectWithEarthMeta || metaTask instanceof HaveConversationMeta)
				rand += ca;

			// Artistic quality
			if (metaTask instanceof ConstructBuildingMeta 
					|| metaTask instanceof ManufactureConstructionMaterialsMeta || metaTask instanceof ManufactureGoodMeta 
					|| metaTask instanceof ObserveAstronomicalObjectsMeta
					|| metaTask instanceof ProduceFoodMeta 
					|| metaTask instanceof CookMealMeta
					|| metaTask instanceof PrepareDessertMeta
					|| metaTask instanceof RecordActivityMeta
					|| metaTask instanceof SalvageGoodMeta
					|| metaTask instanceof TendGreenhouseMeta)
				rand += art;

			if (metaTask instanceof WorkoutMeta || metaTask instanceof PlayHoloGameMeta
					|| metaTask instanceof YogaMeta)
				rand += ag;

			if (metaTask instanceof RelaxMeta || metaTask instanceof PlayHoloGameMeta
			// || metaTask instanceof SleepMeta
					|| metaTask instanceof ListenToMusicMeta || metaTask instanceof WorkoutMeta
					|| metaTask instanceof YogaMeta || metaTask instanceof HaveConversationMeta)
				// if a person has high spirituality score and has alternative ways to deal with
				// stress,
				// he will less likely require extra time to relax/sleep/workout/do yoga.
				rand -= ss;
			
			result = (int) Math.round(rand);
			
			if (result > 8)
				result = 8;
			else if (result < -8)
				result = -8;

			String s = getStringName(metaTask);
			if (!scoreStringMap.containsKey(s)) {
				scoreStringMap.put(s, result);
			}

			if (!scoreMap.containsKey(metaTask)) {
				scoreMap.put(metaTask, result);
			}

		}

		for (MetaTask key : scoreMap.keySet()) {
			taskList.add(getStringName(key));
		}

		Collections.sort(taskList);

		// Add metaMissionList (NOT READY to publish metaMissionList as preferences)
//        Iterator<MetaMission> ii = metaMissionList.iterator();
//		while (ii.hasNext()) {
//			MetaMission metaMission = ii.next();
//
//			if (metaMission instanceof AreologyStudyFieldMissionMeta
//				|| metaMission instanceof BiologyStudyFieldMissionMeta
//				|| metaMission instanceof EmergencySupplyMissionMeta
//				|| metaMission instanceof ExplorationMeta
//				|| metaMission instanceof RescueSalvageVehicleMeta)
//
//				result +=(int)cou;
//
//			if (metaMission instanceof BuildingSalvageMissionMeta
//				|| metaMission instanceof MiningMeta
//				|| metaMission instanceof EmergencySupplyMissionMeta
//				|| metaMission instanceof RescueSalvageVehicleMeta)
//
//				result +=(int)ag;
//
//			if (metaMission instanceof CollectIceMeta
//				|| metaMission instanceof CollectRegolithMeta)
//
//				result +=(int)es;
//
//			if (metaMission instanceof TradeMeta
//				|| metaMission instanceof TravelToSettlementMeta)
//
//				result +=(int)l;
//		}

	}

	/**
	 * Obtains the preference score modified by its priority for a meta task
	 * 
	 * @param metaTask
	 * @return the score
	 */
	public int getPreferenceScore(MetaTask metaTask) {
		int result = 0;
		if (scoreMap.containsKey(metaTask))
			result = scoreMap.get(metaTask);
		else {
			scoreMap.put(metaTask, 0);
			result = 0;
		}

		if (futureTaskMap.containsValue(metaTask) && (taskAccomplishedMap.get(metaTask) != null)
				&& !taskAccomplishedMap.get(metaTask) && onceADayMap.get(metaTask)) {
			// preference scores are not static. They are influenced by priority scores
			result += obtainPrioritizedScore(metaTask);
		}

		return result;
	}

//	/**
//	 * Obtains the preference score for a meta task
//	 * @param metaTask
//	 * @return the preference score
//	 
//	public int getPreferenceScore(MetaTask metaTask) {
//		int result = 0;
//		if (scoreMap.containsKey(metaTask))
//			result = scoreMap.get(metaTask);
//		else {
//			scoreMap.put(metaTask, 0);
//			result = 0;
//		}
//
//		return result;
//	}

	/***
	 * Obtains the prioritized score of a meta task from its priority map
	 * 
	 * @param metaTask
	 * @return the prioritized score
	 */
	public int obtainPrioritizedScore(MetaTask metaTask) {
		int result = 0;
		// iterate over
		Iterator<Entry<MarsClock, MetaTask>> i = futureTaskMap.entrySet().iterator();
		while (i.hasNext()) {
			Entry<MarsClock, MetaTask> entry = i.next();
			MarsClock sch_clock = entry.getKey();
			MetaTask task = entry.getValue();
			if (metaTask.equals(task)) {
				int now = (int) MarsClock.getTotalMillisols(marsClock);
				int sch = (int) MarsClock.getTotalMillisols(sch_clock);
				if (now - sch > 0 && now - sch <= 5) {
					// examine its timestamp down to within 5 millisols
					// System.out.println("now - sch = " + (now-sch));
					result += priorityMap.get(task);
				}
			}
		}

		return result;
	}

	/***
	 * Obtains the proper string name of a meta task
	 * 
	 * @param metaTask {@link MetaTask}
	 * @return string name of a meta task
	 */
	public static String getStringName(MetaTask metaTask) {
		String s = metaTask.getClass().getSimpleName();

//		StringBuilder ss = new StringBuilder(s);
//		  for (int i = 1; i < s.length(); ++i) {
//		     if (Character.isUpperCase( s.charAt( i ))) {
//		    	 ss.insert(i++, ' ' );
//		     }
//		  }

		String ss = s.replaceAll("(?!^)([A-Z])", " $1")
				.replace("Meta", "")
				.replace("E V A ", "EVA ")
				.replace("With ", "with ")
				.replace("To ", "to ");
		return ss;
	}

	/***
	 * Obtains the proper string name of a task
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
		return ss;
	}

//	public void scheduleTask(String s, int t1, int t2, boolean onceOnly, int priority) {
//		// set the time between 700 and 950 msols on the next day
//		MarsClock randomTimetomorrow = marsClock.getMarsClockNextSol(marsClock, t1, t2);
//		//System.out.println("randomTimetomorrow : " + randomTimetomorrow.getDateTimeStamp());
//		MetaTask mt = MetaTaskUtil.getMetaTask(s);
//		setPlanner(mt, randomTimetomorrow);
//		oneADayMap.put(mt, onceOnly);
//		priorityMap.put(mt, priority);
//	}
//
//	public boolean setPlanner(MetaTask metaTask, MarsClock marsClock) {
//		if (!futureTaskMap.containsKey(marsClock)) {
//			// TODO: need to compare the clock better
//			futureTaskMap.put(marsClock, metaTask);
//			taskAccomplishedMap.put(metaTask, false);
//			return true;
//		}
//		return false;
//	}

	/**
	 * Performs the action per frame
	 * 
	 * @param time amount of time passing (in millisols).
	 */
	public void timePassing(double time) {
//		if (marsClock == null)
//			marsClock = Simulation.instance().getMasterClock().getMarsClock();
//
//		int solElapsed = marsClock.getMissionSol();
//		if (solElapsed != solCache) {
//			Iterator<Entry<MarsClock, MetaTask>> i = futureTaskMap.entrySet().iterator();
//			while (i.hasNext()) {
//				MetaTask mt = i.next().getValue();
//				// if this meta task is not a recurrent task, remove it.
//				if (!onceADayMap.get(mt)) {
////					futureTaskMap.remove(mt);
//					priorityMap.remove(mt);
//					onceADayMap.remove(mt);
//					taskAccomplishedMap.remove(mt);
//				}
//			}
//			
//			// scheduleTask("WriteReportMeta", 500, 800, true, 750);
////			 scheduleTask("ConnectWithEarthMeta", 700, 900, true, 950);
//
//			solCache = solElapsed;
//		}
	}

	/**
	 * Checks if this task is due
	 * 
	 * @param MetaTask
	 * @return true if it does
	 */
	public boolean isTaskDue(MetaTask mt) { // Task task) {
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
	 * Flag this task as being due or not due
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
	 * Converts a task to its corresponding meta task
	 * 
	 * @param a task
	 */
	public static MetaTask convertTask2MetaTask(Task task) {
		MetaTask result = null;
		String name = task.getTaskName();
		// System.out.println(" task name is " + name);
		result = MetaTaskUtil.getMetaTask(name + META);
		return result;
	}

	public Map<MetaTask, Integer> getScoreMap() {
		return scoreMap;
	}

	public Map<String, Integer> getScoreStringMap() {
		return scoreStringMap;
	}

	public List<String> getTaskStringList() {
		return taskList;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
//		naturalAttributeManager = null;
		person = null;
		marsClock = null;
//		metaTaskList = null;
		taskList = null;
		// metaMissionList = null;
		scoreMap = null;
		priorityMap = null;
		onceADayMap = null;
		taskAccomplishedMap = null;
		scoreStringMap = null;
		futureTaskMap = null;
	}
}
