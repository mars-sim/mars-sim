/**
 * Mars Simulation Project
 * Preference.java
 * @version 3.08 2015-06-07
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
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.meta.AssistScientificStudyResearcherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CompileScientificStudyResultsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConsolidateContainersMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.InviteStudyCollaboratorMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTask;
import org.mars_sim.msp.core.person.ai.task.meta.MetaTaskUtil;
import org.mars_sim.msp.core.person.ai.task.meta.PeerReviewStudyPaperMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformMathematicalModelingMeta;
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
import org.mars_sim.msp.core.person.ai.task.meta.TeachMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TreatMedicalPatientMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
//import org.mars_sim.msp.core.person.ai.task.meta.WriteReportMeta;
import org.mars_sim.msp.core.person.ai.task.meta.YogaMeta;
import org.mars_sim.msp.core.time.MarsClock;


/**
 * The Preference class handles the task preferences of a person
 */
public class Preference implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private int solCache = 0;

	private NaturalAttributeManager naturalAttributeManager;
	private Person person;
	private MarsClock clock;

	private List<MetaTask> metaTaskList;
	private Map<MetaTask, Integer> scorekMap;
	private Map<String, Integer> stringNameMap;
	private List<String> metaTaskStringList;
	private Map<MarsClock, MetaTask> futureTaskMap;
	private Map<MetaTask, Boolean> statusMap; // true if the activity has been accomplished
	private Map<MetaTask, Integer> priorityMap;
	private Map<MetaTask, Boolean> frequencyMap; // true if the activity can only be done once a day

	public Preference(Person person) {
		//System.out.println("starting Preference's constructor");

		this.person = person;

		metaTaskList = MetaTaskUtil.getMetaTasks();
		metaTaskStringList = new ArrayList<>();

		scorekMap = new ConcurrentHashMap<>();
		stringNameMap = new ConcurrentHashMap<>();

		futureTaskMap = new ConcurrentHashMap<>();
		statusMap = new ConcurrentHashMap<>();
		priorityMap = new ConcurrentHashMap<>();
		frequencyMap = new ConcurrentHashMap<>();

		//scheduleTask("WriteReportMeta", 600, 900);
		//scheduleTask("ConnectWithEarthMeta", 700, 950);

		//System.out.println("done with Preference's constructor");

	}

	public void initializePreference() {

		if (naturalAttributeManager == null)
			naturalAttributeManager = person.getNaturalAttributeManager();

		int result = 0 ;

		// Computes the adjustment from a person's natural attributes
        double a =  naturalAttributeManager.getAttribute(NaturalAttribute.ACADEMIC_APTITUDE)/50D * 1.5;
        double t =  naturalAttributeManager.getAttribute(NaturalAttribute.TEACHING)/50D * 1.5;
        double l =  naturalAttributeManager.getAttribute(NaturalAttribute.LEADERSHIP)/50D * 1.5;
        double sa =  (naturalAttributeManager.getAttribute(NaturalAttribute.STRENGTH)
        			+ naturalAttributeManager.getAttribute(NaturalAttribute.AGILITY))/100D * 1.5;
        double ss =  (naturalAttributeManager.getAttribute(NaturalAttribute.STRESS_RESILIENCE)
        			+ naturalAttributeManager.getAttribute(NaturalAttribute.SPIRITUALITY))/100D * 1.5;
        double se =  (naturalAttributeManager.getAttribute(NaturalAttribute.STRESS_RESILIENCE)
        			+ naturalAttributeManager.getAttribute(NaturalAttribute.EMOTIONAL_STABILITY))/100D * 1.5;

		Iterator<MetaTask> i = metaTaskList.iterator();
		while (i.hasNext()) {
			MetaTask metaTask = i.next();

			// add randomness
			result = RandomUtil.getRandomInt(-3, 3);

			// Note: the preference score on a metaTask is adjusted by a person's natural attributes

			if (metaTask instanceof CompileScientificStudyResultsMeta
				|| metaTask instanceof AssistScientificStudyResearcherMeta
				|| metaTask instanceof PeerReviewStudyPaperMeta
				|| metaTask instanceof PerformMathematicalModelingMeta
				|| metaTask instanceof ProposeScientificStudyMeta
				|| metaTask instanceof RespondToStudyInvitationMeta)
				result += (int)a;

			if (metaTask instanceof TeachMeta
				|| metaTask instanceof ReadMeta)
				result += (int)t;

			if (metaTask instanceof LoadVehicleEVAMeta
				|| metaTask instanceof LoadVehicleGarageMeta
				|| metaTask instanceof UnloadVehicleEVAMeta
				|| metaTask instanceof UnloadVehicleGarageMeta
				|| metaTask instanceof SalvageBuildingMeta
				|| metaTask instanceof SalvageGoodMeta
				|| metaTask instanceof RepairEVAMalfunctionMeta
				|| metaTask instanceof RepairMalfunctionMeta
				|| metaTask instanceof MaintenanceEVAMeta
				|| metaTask instanceof MaintenanceMeta
				|| metaTask instanceof ConsolidateContainersMeta
				|| metaTask instanceof ConstructBuildingMeta)
				result += (int)sa;

			if (metaTask instanceof ProposeScientificStudyMeta
				|| metaTask instanceof InviteStudyCollaboratorMeta
				|| metaTask instanceof ReviewJobReassignmentMeta)
				result += (int)l;

			if (metaTask instanceof TreatMedicalPatientMeta)
				result += (int)((se + se)/2D);

			if (metaTask instanceof RequestMedicalTreatmentMeta)
				// if a person is stress-resilient and relatively emotional stable,
				// he will more likely endure pain and less likely ask to be medicated.
				result -= (int)se;


			if (metaTask instanceof RelaxMeta
				|| metaTask instanceof SleepMeta
				|| metaTask instanceof WorkoutMeta
				|| metaTask instanceof YogaMeta)
				// if a person has high spirituality score and thus have ways to deal with stress
				// he will less likely require extra time to relax/sleep/workout/do yoga.
				result -= (int)ss;

			if (result > 8)
				result = 8;

			String s = getStringName(metaTask);
			if (!stringNameMap.containsKey(s)) {
				stringNameMap.put(s, result);
			}

			if (!scorekMap.containsKey(metaTask)) {
				scorekMap.put(metaTask, result);
			}

		}

        for (MetaTask key : scorekMap.keySet()) {
        	metaTaskStringList.add(getStringName(key));
        }

        Collections.sort(metaTaskStringList);
	}

	public int getPreferenceScore(MetaTask metaTask) {
		int result = 0;
		//String s = getStringName(metaTask);
		if (scorekMap.containsKey(metaTask))
			result = scorekMap.get(metaTask);
		else {
			scorekMap.put(metaTask, 0);
			result = 0;
		}

		if (futureTaskMap.containsValue(metaTask)
				&& (statusMap.get(metaTask) != null)
				&& !statusMap.get(metaTask)
				&& frequencyMap.get(metaTask)) {
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
		return scorekMap;
	}

	public Map<String, Integer> getMetaTaskStringMap(){
		return stringNameMap;
	}


	public List<String> getMetaTaskStringList() {
		return metaTaskStringList;
	}

	public String getStringName(MetaTask metaTask) {
		String s = metaTask.getClass().getSimpleName();

/*
		StringBuilder ss = new StringBuilder(s);
		  for (int i = 1; i < s.length(); ++i) {
		     if (Character.isUpperCase( s.charAt( i ))) {
		    	 ss.insert(i++, ' ' );
		     }
		  }
*/

		String ss = s.replaceAll("(?!^)([A-Z])", " $1").replace("E V A ", "EVA").replace("Meta", "");
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
			statusMap.put(metaTask, false);
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
	    if (clock == null)
	    	clock = Simulation.instance().getMasterClock().getMarsClock();

		int solElapsed = MarsClock.getSolOfYear(clock);
		if (solElapsed != solCache) {

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

			solCache = solElapsed;
		}

    }

    public boolean getTaskStatus(MetaTask mt) { //Task task) {
    	//MetaTask mt = convertTask2MetaTask(task);
    	if (statusMap.isEmpty()) {
    		// if it does not exist (either it is not scheduled or it have been accomplished), the status is true
    		return true;
    	}
    	else if (statusMap.get(mt) == null)
    		return true;
    	else
    		return statusMap.get(mt);
    }

    public void setTaskStatus(Task task, boolean value) {
      	MetaTask mt = convertTask2MetaTask(task);

		// if this accomplished meta task is onceOnly task, remove it.
		if (value && frequencyMap.get(mt) != null && !frequencyMap.isEmpty())
			if (frequencyMap.get(mt) != null && frequencyMap.get(mt)) {
				futureTaskMap.remove(mt);
				frequencyMap.remove(mt);
				statusMap.remove(mt);
				priorityMap.remove(mt);
			}
		else
			statusMap.put(mt, value);

    }

    public static MetaTask convertTask2MetaTask(Task task) {
    	MetaTask result = null;
    	String name = task.getTaskName();
    	//System.out.println(" task name is " + name);
    	result = MetaTaskUtil.getMetaTask(name + "Meta");
    	return result;
    }
}
