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

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.meta.AreologyStudyFieldMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.BiologyStudyFieldMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.BuildingSalvageMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.CollectIceMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.CollectRegolithMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.EmergencySupplyMissionMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.ExplorationMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMission;
import org.mars_sim.msp.core.person.ai.mission.meta.MetaMissionUtil;
import org.mars_sim.msp.core.person.ai.mission.meta.MiningMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.RescueSalvageVehicleMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.TradeMeta;
import org.mars_sim.msp.core.person.ai.mission.meta.TravelToSettlementMeta;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.meta.AssistScientificStudyResearcherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CompileScientificStudyResultsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConnectWithEarthMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConsolidateContainersMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CookMealMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalIceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalRegolithMeta;
import org.mars_sim.msp.core.person.ai.task.meta.EatMealMeta;
import org.mars_sim.msp.core.person.ai.task.meta.HaveConversationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.InviteStudyCollaboratorMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ListenToMusicMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureConstructionMaterialsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTaskUtil;
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
import org.mars_sim.msp.core.person.ai.task.meta.RelaxMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairEVAMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RequestMedicalTreatmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RespondToStudyInvitationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReviewJobReassignmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SleepMeta;
import org.mars_sim.msp.core.person.ai.task.meta.StudyFieldSamplesMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TeachMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TendGreenhouseMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TreatMedicalPatientMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WriteReportMeta;
//import org.mars_sim.msp.core.person.ai.task.meta.WriteReportMeta;
import org.mars_sim.msp.core.person.ai.task.meta.YogaMeta;
import org.mars_sim.msp.core.time.MarsClock;


/**
 * The Preference class handles the task preferences of a person
 */
public class Preference implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String META = "Meta";

	private int solCache = 0;

	private NaturalAttributeManager naturalAttributeManager;
	private Person person;
	private MarsClock clock;

	private List<MetaTask> metaTaskList;
	private List<String> metaTaskStringList;
	//private List<MetaMission> metaMissionList;

	private Map<MetaTask, Integer> scoreMap; // store preference scores
	private Map<MetaTask, Integer> priorityMap; // store priority scores for scheduled tasks
	private Map<MetaTask, Boolean> frequencyMap; // true if the activity can only be done once a day
	private Map<MetaTask, Boolean> taskDueMap; // true if the activity has been accomplished

	private Map<String, Integer> stringNameMap;

	private Map<MarsClock, MetaTask> futureTaskMap;

	public Preference(Person person) {
		//System.out.println("starting Preference's constructor");

		this.person = person;

		metaTaskList = MetaTaskUtil.getAllMetaTasks();
		metaTaskStringList = new ArrayList<>();
		//metaMissionList = MetaMissionUtil.getMetaMissions();

		scoreMap = new ConcurrentHashMap<>();
		stringNameMap = new ConcurrentHashMap<>();

		futureTaskMap = new ConcurrentHashMap<>();
		taskDueMap = new ConcurrentHashMap<>();
		priorityMap = new ConcurrentHashMap<>();
		frequencyMap = new ConcurrentHashMap<>();

		//scheduleTask("WriteReportMeta", 600, 900);
		//scheduleTask("ConnectWithEarthMeta", 700, 950);

		//System.out.println("done with Preference's constructor");
	}

	/*
	 * Initialize the preference score on each particular task
	 */
	public void initializePreference() {

		if (naturalAttributeManager == null)
			naturalAttributeManager = person.getNaturalAttributeManager();

		int result = 0 ;

		// Computes the adjustment from a person's natural attributes
        double aa =  naturalAttributeManager.getAttribute(NaturalAttribute.ACADEMIC_APTITUDE)/50D * 1.5;
        double t =  naturalAttributeManager.getAttribute(NaturalAttribute.TEACHING)/50D * 1.5;
        double l =  naturalAttributeManager.getAttribute(NaturalAttribute.LEADERSHIP)/50D * 1.5;
        double es =  (naturalAttributeManager.getAttribute(NaturalAttribute.ENDURANCE)
        			+ naturalAttributeManager.getAttribute(NaturalAttribute.STRENGTH))/100D * 1.5;
        double ag =  naturalAttributeManager.getAttribute(NaturalAttribute.AGILITY)/500D * 1.5;

        double ss =  (naturalAttributeManager.getAttribute(NaturalAttribute.STRESS_RESILIENCE)
        			+ naturalAttributeManager.getAttribute(NaturalAttribute.SPIRITUALITY))/100D * 1.5;
        double se =  (naturalAttributeManager.getAttribute(NaturalAttribute.STRESS_RESILIENCE)
        			+ naturalAttributeManager.getAttribute(NaturalAttribute.EMOTIONAL_STABILITY))/100D * 1.5;

        double ca = (naturalAttributeManager.getAttribute(NaturalAttribute.CONVERSATION)/50D
    			+ naturalAttributeManager.getAttribute(NaturalAttribute.ATTRACTIVENESS)/200D) * 1.5;

        double art = naturalAttributeManager.getAttribute(NaturalAttribute.ARTISTRY)/50D * 1.5;

        double cou = naturalAttributeManager.getAttribute(NaturalAttribute.COURAGE)/50D * 1.5;

        //TODO: how to incorporate EXPERIENCE_APTITUDE ?

		Iterator<MetaTask> i = metaTaskList.iterator();
		while (i.hasNext()) {
			MetaTask metaTask = i.next();

			// Set them up in random
			result = RandomUtil.getRandomInt(-2, 2);

			// Note: the preference score on a metaTask is modified by a person's natural attributes
			// TODO: turn these hardcoded relationship between attributes and task preferences into a XML/JSON file

			// Academically driven
			if (metaTask instanceof AssistScientificStudyResearcherMeta
				|| metaTask instanceof CompileScientificStudyResultsMeta
				|| metaTask instanceof MaintenanceEVAMeta
				|| metaTask instanceof MaintenanceMeta
				|| metaTask instanceof ObserveAstronomicalObjectsMeta
				|| metaTask instanceof PeerReviewStudyPaperMeta
				|| metaTask instanceof PerformLaboratoryExperimentMeta
				|| metaTask instanceof PerformLaboratoryResearchMeta
				|| metaTask instanceof PerformMathematicalModelingMeta
				|| metaTask instanceof ProposeScientificStudyMeta
				|| metaTask instanceof RespondToStudyInvitationMeta
				|| metaTask instanceof RepairEVAMalfunctionMeta
				|| metaTask instanceof RepairMalfunctionMeta
				|| metaTask instanceof StudyFieldSamplesMeta)
				result += (int)aa;

			// Teaching excellence
			if (metaTask instanceof TeachMeta
				|| metaTask instanceof ReadMeta
				|| metaTask instanceof PeerReviewStudyPaperMeta
				|| metaTask instanceof WriteReportMeta)
				result += (int)t;

			// Endurance & strength related
			if ( metaTask instanceof ConsolidateContainersMeta
				|| metaTask instanceof ConstructBuildingMeta
				|| metaTask instanceof DigLocalIceMeta
				|| metaTask instanceof DigLocalRegolithMeta
				|| metaTask instanceof EatMealMeta
				|| metaTask instanceof LoadVehicleEVAMeta
				|| metaTask instanceof LoadVehicleGarageMeta
				|| metaTask instanceof UnloadVehicleEVAMeta
				|| metaTask instanceof UnloadVehicleGarageMeta
				|| metaTask instanceof SalvageBuildingMeta
				|| metaTask instanceof SalvageGoodMeta
				|| metaTask instanceof RepairEVAMalfunctionMeta
				|| metaTask instanceof RepairMalfunctionMeta
				|| metaTask instanceof MaintenanceEVAMeta
				|| metaTask instanceof MaintenanceMeta)
				result += (int)es;

			// leadership
			if (metaTask instanceof ProposeScientificStudyMeta
				|| metaTask instanceof InviteStudyCollaboratorMeta
				|| metaTask instanceof ReviewJobReassignmentMeta
				|| metaTask instanceof WriteReportMeta)
				result += (int)l;

			if (metaTask instanceof TreatMedicalPatientMeta)
				// need patience and stability to administer healing
				result += (int)((se + ss)/2D);

			if (metaTask instanceof RequestMedicalTreatmentMeta
					|| metaTask instanceof YogaMeta)
				// if a person is stress-resilient and relatively emotional stable,
				// he will more likely endure pain and less likely ask to be medicated.
				result -= (int)se;

			if (metaTask instanceof RelaxMeta
				|| metaTask instanceof PlayHoloGameMeta
				//|| metaTask instanceof SleepMeta
				|| metaTask instanceof ListenToMusicMeta
				|| metaTask instanceof WorkoutMeta
				|| metaTask instanceof YogaMeta
				|| metaTask instanceof HaveConversationMeta)
				// if a person has high spirituality score and thus have ways to deal with stress
				// he will less likely require extra time to relax/sleep/workout/do yoga.
				result -= (int)ss;

			if (metaTask instanceof ConnectWithEarthMeta
				||	metaTask instanceof HaveConversationMeta)
				result += (int)ca;

			// Artistic quality
			if (metaTask instanceof ConstructBuildingMeta
				|| metaTask instanceof CookMealMeta
				|| metaTask instanceof ManufactureConstructionMaterialsMeta
				|| metaTask instanceof ManufactureGoodMeta
				|| metaTask instanceof ObserveAstronomicalObjectsMeta
				|| metaTask instanceof ProduceFoodMeta
				|| metaTask instanceof PrepareDessertMeta
				|| metaTask instanceof ProduceFoodMeta
				|| metaTask instanceof SalvageGoodMeta
				|| metaTask instanceof TendGreenhouseMeta)
				result += (int)art;

			if (metaTask instanceof WorkoutMeta
				|| metaTask instanceof PlayHoloGameMeta)
				result +=(int)ag;

			if (result > 7)
				result = 7;
			else if (result < -7)
				result = -7;

			String s = getStringName(metaTask);
			if (!stringNameMap.containsKey(s)) {
				stringNameMap.put(s, result);
			}

			if (!scoreMap.containsKey(metaTask)) {
				scoreMap.put(metaTask, result);
			}

		}

        for (MetaTask key : scoreMap.keySet()) {
        	metaTaskStringList.add(getStringName(key));
        }

        Collections.sort(metaTaskStringList);

/*
        // 2015-10-14 Added metaMissionList (NOT READY to publish metaMissionList as preferences)
        Iterator<MetaMission> ii = metaMissionList.iterator();
		while (ii.hasNext()) {
			MetaMission metaMission = ii.next();

			if (metaMission instanceof AreologyStudyFieldMissionMeta
				|| metaMission instanceof BiologyStudyFieldMissionMeta
				|| metaMission instanceof EmergencySupplyMissionMeta
				|| metaMission instanceof ExplorationMeta
				|| metaMission instanceof RescueSalvageVehicleMeta)

				result +=(int)cou;

			if (metaMission instanceof BuildingSalvageMissionMeta
				|| metaMission instanceof MiningMeta
				|| metaMission instanceof EmergencySupplyMissionMeta
				|| metaMission instanceof RescueSalvageVehicleMeta)

				result +=(int)ag;

			if (metaMission instanceof CollectIceMeta
				|| metaMission instanceof CollectRegolithMeta)

				result +=(int)es;

			if (metaMission instanceof TradeMeta
				|| metaMission instanceof TravelToSettlementMeta)

				result +=(int)l;
		}
*/ 
	}

	public int getPreferenceScore(MetaTask metaTask) {
		int result = 0;
		//String s = getStringName(metaTask);
		if (scoreMap.containsKey(metaTask))
			result = scoreMap.get(metaTask);
		else {
			scoreMap.put(metaTask, 0);
			result = 0;
		}

		if (futureTaskMap.containsValue(metaTask)
				&& (taskDueMap.get(metaTask) != null)
				&& !taskDueMap.get(metaTask)
				&& frequencyMap.get(metaTask)) {
			// preference scores are not static. They are influenced by priority scores
			result += checkScheduledTask(metaTask);
		}

		return result;
	}

	public int checkScheduledTask(MetaTask metaTask) {
		int result = 0;

		// iterate over
		Iterator<Entry<MarsClock, MetaTask>> i = futureTaskMap.entrySet().iterator();
		while (i.hasNext()) {
			Entry<MarsClock, MetaTask> entry = i.next();
			MarsClock clock = entry.getKey();
			MetaTask task = entry.getValue();
			if (metaTask.equals(task)) {
				//System.out.println("task matched!");
				//if (MarsClock.getTotalSol(clock) == MarsClock.getTotalSol(currentTime)){
				MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
				int now = (int) MarsClock.getTotalMillisols(currentTime);
				int sch = (int) MarsClock.getTotalMillisols(clock);
				if (now - sch > 0 && now - sch <= 5) {
					// examine its timestamp down to within 5 millisols
					//System.out.println("now - sch = " + (now-sch));
					result += priorityMap.get(task);
				}
			}
		}


		return result;
	}

	public Map<MetaTask, Integer> getMetaTaskMap(){
		return scoreMap;
	}

	public Map<String, Integer> getMetaTaskStringMap(){
		return stringNameMap;
	}


	public List<String> getMetaTaskStringList() {
		return metaTaskStringList;
	}

	public static String getStringName(MetaTask metaTask) {
		String s = metaTask.getClass().getSimpleName();

/*
		StringBuilder ss = new StringBuilder(s);
		  for (int i = 1; i < s.length(); ++i) {
		     if (Character.isUpperCase( s.charAt( i ))) {
		    	 ss.insert(i++, ' ' );
		     }
		  }
*/

		String ss = s.replaceAll("(?!^)([A-Z])", " $1").replace("Meta", "").replace("E V A ", "EVA ").replace("To ", "to ");
		//System.out.println(ss + " <-- " + s);
		return ss;
	}

	public static String getStringName(Task task) {
		String s = task.getClass().getSimpleName();

		String ss = s.replaceAll("(?!^)([A-Z])", " $1").replace("E V A ", "EVA ").replace("To ", "to ");
		//System.out.println(ss + " <-- " + s);
		return ss;
	}


	public void scheduleTask(String s, int t1, int t2, boolean onceOnly, int priority) {

		// set the time between 700 and 950 msols on the next day
		MarsClock randomTimetomorrow = clock.getMarsClockNextSol(clock, t1, t2);
		//System.out.println("randomTimetomorrow : " + randomTimetomorrow.getDateTimeStamp());
		MetaTask mt = MetaTaskUtil.getMetaTask(s);
		setPlanner(mt, randomTimetomorrow);
		frequencyMap.put(mt, onceOnly);
		priorityMap.put(mt, priority);
	}

	public boolean setPlanner(MetaTask metaTask, MarsClock marsClock) {
		if (!futureTaskMap.containsKey(marsClock)) {
			// TODO: need to compare the clock better
			futureTaskMap.put(marsClock, metaTask);
			taskDueMap.put(metaTask, false);
			return true;
		}
		return false;
	}

    /**
     * Performs the action per frame
     * @param time amount of time passing (in millisols).
     */
	// 2015-06-29 Added timePassing()
    public void timePassing(double time) {
	    //if (clock == null)
	    //	clock = Simulation.instance().getMasterClock().getMarsClock();

		//int solElapsed = MarsClock.getSolOfYear();//clock);
		//if (solElapsed != solCache) {
			//TODO change of preference occus when ....
/*
			Iterator<Entry<MarsClock, MetaTask>> i = futureTaskMap.entrySet().iterator();
			while (i.hasNext()) {
				MetaTask mt = i.next().getValue();
				// if this meta task is not a recurrent task, remove it.
				if (!frequencyMap.get(mt)) {
					futureTaskMap.remove(mt);
					frequencyMap.remove(mt);
					statusMap.remove(mt);
					priorityMap.remove(mt);
				}
			}
*/
			//scheduleTask("WriteReportMeta", 500, 800, true, 750);
			//scheduleTask("ConnectWithEarthMeta", 700, 900, true, 950);

			//solCache = solElapsed;
		//}

    }

    /**
     * Checks if this task is due
     * @param MetaTask
     * @return true if it does
     */
    public boolean isTaskDue(MetaTask mt) { //Task task) {
    	//MetaTask mt = convertTask2MetaTask(task);
    	if (taskDueMap.isEmpty()) {
    		// if it does not exist (either it is not scheduled or it have been accomplished), the status is true
    		return true;
    	}
    	else if (taskDueMap.get(mt) == null)
    		return true;
    	else
    		return taskDueMap.get(mt);
    }

    /**
     * Flag this task as being due or not due
     * @param MetaTask
     * @param true if it is due
     */
    public void setTaskDue(Task task, boolean value) {
      	MetaTask mt = convertTask2MetaTask(task);

		// if this accomplished meta task is onceOnly task, remove it.
		if (value && frequencyMap.get(mt) != null && !frequencyMap.isEmpty())
			if (frequencyMap.get(mt) != null && frequencyMap.get(mt)) {
				futureTaskMap.remove(mt);
				frequencyMap.remove(mt);
				taskDueMap.remove(mt);
				priorityMap.remove(mt);
			}
		else
			taskDueMap.put(mt, value);

    }

    /**
     * Converts a task to its corresponding meta task
     * @param a task
     */
    public static MetaTask convertTask2MetaTask(Task task) {
    	MetaTask result = null;
    	String name = task.getTaskName();
    	//System.out.println(" task name is " + name);
    	result = MetaTaskUtil.getMetaTask(name + META);
    	return result;
    }
}
