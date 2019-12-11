/**
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.person.ai.task.meta.AssistScientificStudyResearcherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CompileScientificStudyResultsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConnectWithEarthMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConsolidateContainersMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CookMealMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalIceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalRegolithMeta;
import org.mars_sim.msp.core.person.ai.task.meta.EatDrinkMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ExamineBodyMeta;
import org.mars_sim.msp.core.person.ai.task.meta.HaveConversationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.InviteStudyCollaboratorMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ListenToMusicMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintainGroundVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintainGroundVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintenanceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureConstructionMaterialsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MeetTogetherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ObserveAstronomicalObjectsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PeerReviewStudyPaperMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformLaboratoryExperimentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformLaboratoryResearchMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PerformMathematicalModelingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PlanMissionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PlayHoloGameMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PrepareDessertMeta;
import org.mars_sim.msp.core.person.ai.task.meta.PrescribeMedicationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ProduceFoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ProposeScientificStudyMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReadMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RecordActivityMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RelaxMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairEVAMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RequestMedicalTreatmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RespondToStudyInvitationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.RestingMedicalRecoveryMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReturnLightUtilityVehicleMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReviewJobReassignmentMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReviewMissionPlanMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SalvageGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SelfTreatHealthProblemMeta;
import org.mars_sim.msp.core.person.ai.task.meta.SleepMeta;
import org.mars_sim.msp.core.person.ai.task.meta.StudyFieldSamplesMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TeachMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TendGreenhouseMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ToggleFuelPowerSourceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ToggleResourceProcessMeta;
import org.mars_sim.msp.core.person.ai.task.meta.TreatMedicalPatientMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.UnloadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WalkMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WriteReportMeta;
import org.mars_sim.msp.core.person.ai.task.meta.YogaMeta;

/**
 * A utility task for getting the list of meta tasks.
 */
public class MetaTaskUtil {

	// Static values.
	private static List<MetaTask> allMetaTasks = null;

	private static List<MetaTask> workHourMetaTasks = null;
	private static List<MetaTask> nonWorkHourMetaTasks = null;
	private static List<MetaTask> anyHourMetaTasks = null;

	private static List<MetaTask> dutyHourTasks = null;
	private static List<MetaTask> nonDutyHourTasks = null;
	
	private static List<MetaTask> robotMetaTasks = null;

	/**
	 * Private constructor for utility class.
	 */
	public MetaTaskUtil() {
//		initAnyHourTasks();
//		initWorkHourTasks();
//		initNonWorkHourTasks();
		
		initializeMetaTasks();
		
		initDutyHourTasks();
		initNonDutyHourTasks();
	};

	/**
	 * Lazy initialization of metaTasks list.
	 */
	private static void initializeMetaTasks() {

		if (allMetaTasks == null) {
			// 55 tasks in total as of 2016-10-04
			allMetaTasks = new ArrayList<MetaTask>();

			// should initialize any-hour tasks first before other tasks
			// Note: currently, the 3 lists below have tasks that are mutually exclusive
			initAnyHourTasks();
			initWorkHourTasks();
			initNonWorkHourTasks();

//			Set<MetaTask> allTasksSet = new HashSet<>();
//			// Note: Using Set for adding tasks should prevent duplicate tasks when creating
//			// the task list
//			// However, each instance of the tasks must be explicitly stated
//			allTasksSet.addAll(workHourMetaTasks);
//			allTasksSet.addAll(nonWorkHourMetaTasks);
//			allTasksSet.addAll(anyHourMetaTasks);
//			allMetaTasks.addAll(allTasksSet);
			
			allMetaTasks.addAll(workHourMetaTasks); 
			allMetaTasks.addAll(nonWorkHourMetaTasks); 
			allMetaTasks.addAll(anyHourMetaTasks); 
		}
	}

	/**
	 * Lazy initialization of any-hour metaTasks list.
	 */
	private static void initAnyHourTasks() {

		if (anyHourMetaTasks == null) {

			anyHourMetaTasks = new ArrayList<MetaTask>();

			List<MetaTask> tasks = new ArrayList<MetaTask>();

			// May use
//			Class cls = Class.forName(clsName);
//			MetaTask mt = cls.getDeclaredConstructor().newInstance()
			
			tasks.add(new EatDrinkMeta());
			tasks.add(new HaveConversationMeta());
			tasks.add(new ListenToMusicMeta());
			tasks.add(new LoadVehicleEVAMeta());
			tasks.add(new LoadVehicleGarageMeta());
			tasks.add(new ObserveAstronomicalObjectsMeta());
			tasks.add(new PrescribeMedicationMeta());
			tasks.add(new RelaxMeta());
			tasks.add(new RepairEVAMalfunctionMeta());
			tasks.add(new RepairMalfunctionMeta());
			tasks.add(new RequestMedicalTreatmentMeta());
			tasks.add(new RestingMedicalRecoveryMeta());
			tasks.add(new ReturnLightUtilityVehicleMeta());
			tasks.add(new SelfTreatHealthProblemMeta());
			tasks.add(new SleepMeta()); // if a person is having high fatigue, he/she may fall asleep at work
			tasks.add(new TreatMedicalPatientMeta());
			tasks.add(new WalkMeta());

			anyHourMetaTasks.addAll(tasks);
		}
	}

	/**
	 * Lazy initialization of duty hour tasks list.
	 */
	private static void initDutyHourTasks() {

		if (dutyHourTasks == null) {

			dutyHourTasks = new ArrayList<MetaTask>();

			dutyHourTasks.addAll(anyHourMetaTasks);
			dutyHourTasks.addAll(workHourMetaTasks);
		}
	}
	
	/**
	 * Lazy initialization of work-hour metaTasks list.
	 */
	private static void initWorkHourTasks() {

		if (workHourMetaTasks == null) {

			workHourMetaTasks = new ArrayList<MetaTask>();

			List<MetaTask> tasks = new ArrayList<MetaTask>();

			// Use set to ensure non-duplicate tasks
			// TODO: how to get around the need of comparing the new instance of the same class
//			Set<MetaTask> tasks = new HashSet<>();
			
			tasks.add(new AssistScientificStudyResearcherMeta());
			tasks.add(new CompileScientificStudyResultsMeta());
			tasks.add(new ConsolidateContainersMeta());
			tasks.add(new ConstructBuildingMeta());
			tasks.add(new CookMealMeta());
			tasks.add(new DigLocalIceMeta());
			tasks.add(new DigLocalRegolithMeta());
			tasks.add(new ExamineBodyMeta());
			tasks.add(new InviteStudyCollaboratorMeta());
			// tasks.add(new LoadVehicleEVAMeta());
			// tasks.add(new LoadVehicleGarageMeta());
			tasks.add(new MaintainGroundVehicleEVAMeta());
			tasks.add(new MaintainGroundVehicleGarageMeta());
			tasks.add(new MaintenanceEVAMeta());
			tasks.add(new MaintenanceMeta());
			tasks.add(new ManufactureConstructionMaterialsMeta());
			tasks.add(new ManufactureGoodMeta());
			tasks.add(new MeetTogetherMeta());
			tasks.add(new PeerReviewStudyPaperMeta());
			tasks.add(new PerformLaboratoryExperimentMeta());
			tasks.add(new PerformLaboratoryResearchMeta());
			tasks.add(new PerformMathematicalModelingMeta());
			tasks.add(new PlanMissionMeta());
			tasks.add(new PrepareDessertMeta());
//	        tasks.add(new PrescribeMedicationMeta());
			tasks.add(new ProduceFoodMeta());
			tasks.add(new ProposeScientificStudyMeta());
			tasks.add(new RecordActivityMeta());
			tasks.add(new RespondToStudyInvitationMeta());
			// tasks.add(new ReturnLightUtilityVehicleMeta());
			tasks.add(new ReviewJobReassignmentMeta());
			tasks.add(new ReviewMissionPlanMeta());
			tasks.add(new SalvageBuildingMeta());
			tasks.add(new SalvageGoodMeta());
			tasks.add(new StudyFieldSamplesMeta());
			tasks.add(new TeachMeta());
			tasks.add(new TendGreenhouseMeta());
			tasks.add(new ToggleFuelPowerSourceMeta());
			tasks.add(new ToggleResourceProcessMeta());
//	        tasks.add(new TreatMedicalPatientMeta());
			tasks.add(new UnloadVehicleEVAMeta());
			tasks.add(new UnloadVehicleGarageMeta());
			tasks.add(new WriteReportMeta());
		
//			taskList.addAll(tasks);

			workHourMetaTasks.addAll(tasks);
		}
	}

	/**
	 * Lazy initialization of duty hour tasks list.
	 */
	private static void initNonDutyHourTasks() {

		if (nonDutyHourTasks == null) {

			nonDutyHourTasks = new ArrayList<MetaTask>();

			nonDutyHourTasks.addAll(anyHourMetaTasks);
			nonDutyHourTasks.addAll(nonWorkHourMetaTasks);
		}
	}
	
	/**
	 * Lazy initialization of non-work hour metaTasks list.
	 */
	private static void initNonWorkHourTasks() {

		if (nonWorkHourMetaTasks == null) {

			nonWorkHourMetaTasks = new ArrayList<MetaTask>();

			List<MetaTask> tasks = new ArrayList<MetaTask>();

			tasks.add(new ConnectWithEarthMeta());
			// tasks.add(new HaveConversationMeta());
			// tasks.add(new ListenToMusicMeta());
			tasks.add(new PlayHoloGameMeta());
			tasks.add(new ReadMeta());
			// tasks.add(new SleepMeta());
			tasks.add(new WorkoutMeta());
			tasks.add(new YogaMeta());

			// Set<MetaTask> s = new HashSet<>();
			// TODO: NOT WORKING: fix the use of set to avoid duplicate tasks
			// Using Set for adding below should prevent duplicate tasks when creating the
			// task list
			// s.addAll(tasks);
			// s.addAll(anyHourTasks);

			nonWorkHourMetaTasks.addAll(tasks);
		}
	}

	private static void initializeRobotMetaTasks() {

		robotMetaTasks = new ArrayList<MetaTask>();

		// Populate robotMetaTasks list with all robotMeta tasks.
		robotMetaTasks.add(new CookMealMeta());
		robotMetaTasks.add(new ConsolidateContainersMeta());
		// robotMetaTasks.add(new ConstructBuildingMeta());
		// robotMetaTasks.add(new LoadVehicleEVAMeta());
		robotMetaTasks.add(new LoadVehicleGarageMeta());
		// robotMetaTasks.add(new MaintenanceEVAMeta());
		robotMetaTasks.add(new MaintenanceMeta());
		robotMetaTasks.add(new MaintainGroundVehicleGarageMeta());
		robotMetaTasks.add(new ManufactureGoodMeta());
		robotMetaTasks.add(new PrepareDessertMeta());
		robotMetaTasks.add(new PrescribeMedicationMeta());
		robotMetaTasks.add(new ProduceFoodMeta());
		// robotMetaTasks.add(new RepairEVAMalfunctionMeta());
		robotMetaTasks.add(new RepairMalfunctionMeta());
		// robotMetaTasks.add(new ReturnLightUtilityVehicleMeta());
		// robotMetaTasks.add(new SalvageBuildingMeta());
		robotMetaTasks.add(new SleepMeta());
		robotMetaTasks.add(new TendGreenhouseMeta());
		// robotMetaTasks.add(new UnloadVehicleEVAMeta());
		robotMetaTasks.add(new UnloadVehicleGarageMeta());
		robotMetaTasks.add(new WalkMeta());

	}

	/**
	 * Gets a list of all meta tasks.
	 * 
	 * @return list of meta tasks.
	 */
	public static List<MetaTask> getAllMetaTasks() {

		// Lazy initialize meta tasks list if necessary.
		if (allMetaTasks == null) {
			initializeMetaTasks();
		}

//		System.out.println("size of metaTasks : " + allMetaTasks.size()); 
		// Note : 58 meta tasks so far

		// Return copy of meta task list.
		// return new ArrayList<MetaTask>(metaTasks);
		return allMetaTasks;
	}

	/**
	 * Gets a list of all meta tasks.
	 * 
	 * @return list of meta tasks.
	 */
	public static Set<MetaTask> getMetaTasksSet() {
		return new HashSet<MetaTask>(getAllMetaTasks());
	}
	
	/**
	 * Gets a list of all work hour meta tasks.
	 * 
	 * @return list of work hour meta tasks.
	 */
	public static List<MetaTask> getWorkHourMetaTasks() {

		// Lazy initialize work hour meta tasks list if necessary.
		if (workHourMetaTasks == null) {
			initWorkHourTasks();
		}

		// Return copy of work hour meta task list.
//		return new ArrayList<MetaTask>(workHourMetaTasks);
		return workHourMetaTasks;
	}

	/**
	 * Gets a list of all non work hour meta tasks.
	 * 
	 * @return list of work hour meta tasks.
	 */
	public static List<MetaTask> getNonWorkHourMetaTasks() {

		// Lazy initialize non work hour meta tasks list if necessary.
		if (nonWorkHourMetaTasks == null) {
			initNonWorkHourTasks();
		}

		// Return copy of non work hour meta task list.
//		return new ArrayList<MetaTask>(nonWorkHourMetaTasks);
		return nonWorkHourMetaTasks;
	}

	/**
	 * Gets a list of any hour meta tasks.
	 * 
	 * @return list of any hour meta tasks.
	 */
	public static List<MetaTask> getAnyHourTasks() {

		// Lazy initialize all hour meta tasks list if necessary.
		if (anyHourMetaTasks == null) {
			initAnyHourTasks();
		}

		// Return copy of all hour meta task list.
		// return new ArrayList<MetaTask>(anyHourTasks);
		return anyHourMetaTasks;
	}

	/**
	 * Gets a list of duty meta tasks.
	 * 
	 * @return list of duty meta tasks.
	 */
	public static List<MetaTask> getDutyHourTasks() {
		// Lazy initialize all hour meta tasks list if necessary.
		if (dutyHourTasks == null) {
			initDutyHourTasks();
		}
		return dutyHourTasks;
	}
	
	/**
	 * Gets a list of non-duty meta tasks.
	 * 
	 * @return list of non-duty meta tasks.
	 */
	public static List<MetaTask> getNonDutyHourTasks() {
		// Lazy initialize all hour meta tasks list if necessary.
		if (nonDutyHourTasks == null) {
			initNonDutyHourTasks();
		}
		return nonDutyHourTasks;
	}
	
	/**
	 * Converts a task name in String to Metatask
	 * 
	 * @return meta tasks.
	 */
	public static MetaTask getMetaTask(String name) {
		MetaTask metaTask = null;
		Iterator<MetaTask> i = getMetaTasksSet().iterator();
		while (i.hasNext()) {
			MetaTask t = i.next();
			if (t.getClass().getSimpleName().equalsIgnoreCase(name)) {
				metaTask = t;
				break;
			}
		}
		return metaTask;
	}

	public static List<MetaTask> getRobotMetaTasks() {

		// Lazy initialize meta tasks list if necessary.
		if (robotMetaTasks == null) {
			initializeRobotMetaTasks();
		}

		// Return copy of meta task list.
//		return new ArrayList<MetaTask>(robotMetaTasks);
		return robotMetaTasks;
	}

	public void destroy() {
		allMetaTasks = null;
		workHourMetaTasks = null;
		nonWorkHourMetaTasks = null;
		anyHourMetaTasks = null;
		robotMetaTasks = null;
	}
}