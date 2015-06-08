/**
 * Mars Simulation Project
 * Preference.java
 * @version 3.08 2015-06-07
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.RandomUtil;
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
import org.mars_sim.msp.core.person.ai.task.meta.RelaxMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairEVAMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RequestMedicalTreatmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RespondToStudyInvitationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SleepMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TeachMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TreatMedicalPatientMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.meta.YogaMeta;

public class Preference {

	/** default serial id. */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private NaturalAttributeManager naturalAttributeManager;
	private Person person;
	private List<MetaTask> metaTaskList;
	private Map<MetaTask, Integer> metaTaskMap;
	private Map<String, Integer> metaTaskStringMap;
	private List<String> metaTaskStringList;


	public Preference(Person person) {
		this.person = person;

		metaTaskList = MetaTaskUtil.getMetaTasks();
		metaTaskStringList = new ArrayList<>();

		metaTaskMap = new ConcurrentHashMap<>();
		metaTaskStringMap = new ConcurrentHashMap<>();
	}

	public void initializePreference() {

		if (naturalAttributeManager == null)
			naturalAttributeManager = person.getNaturalAttributeManager();

		int result = 0 ;

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

			result = RandomUtil.getRandomInt(-4, 4);

			if (metaTask instanceof CompileScientificStudyResultsMeta
				|| metaTask instanceof AssistScientificStudyResearcherMeta
				|| metaTask instanceof PeerReviewStudyPaperMeta
				|| metaTask instanceof PerformMathematicalModelingMeta
				|| metaTask instanceof ProposeScientificStudyMeta
				|| metaTask instanceof RespondToStudyInvitationMeta)
				result += (int)a;

			if (metaTask instanceof TeachMeta)
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
				|| metaTask instanceof InviteStudyCollaboratorMeta)
				result += (int)l;

			if (metaTask instanceof TreatMedicalPatientMeta)
				result += (int)((se + se)/2D);

			if (metaTask instanceof RequestMedicalTreatmentMeta)
				result -= (int)se;

			if (metaTask instanceof RelaxMeta
				|| metaTask instanceof SleepMeta
				|| metaTask instanceof WorkoutMeta
				|| metaTask instanceof YogaMeta)
				result -= (int)ss;

			if (result > 8)
				result = 8;

			String s = getStringName(metaTask);
			if (!metaTaskStringMap.containsKey(s)) {
				metaTaskStringMap.put(s, result);
			}

			if (!metaTaskMap.containsKey(metaTask)) {
				metaTaskMap.put(metaTask, result);
			}

		}

        for (MetaTask key : metaTaskMap.keySet()) {
        	metaTaskStringList.add(getStringName(key));
        }

        Collections.sort(metaTaskStringList);
	}

	public int getPreferenceScore(MetaTask metaTask) {
		int result;
		//String s = getStringName(metaTask);
		if (metaTaskMap.containsKey(metaTask))
			result = metaTaskMap.get(metaTask);
		else {
			metaTaskMap.put(metaTask, 0);
			result = 0;
		}
		return result;
	}

	public Map<MetaTask, Integer> getMetaTaskMap(){
		return metaTaskMap;
	}

	public Map<String, Integer> getMetaTaskStringMap(){
		return metaTaskStringMap;
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
}
