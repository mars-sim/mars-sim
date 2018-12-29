/**
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
		initAnyHourTasks();
		initWorkHourTasks();
		initNonWorkHourTasks();
		initDutyHourTasks();
		initNonDutyHourTasks();
	};

	/**
	 * Lazy initialization of metaTasks list.
	 */
	private static void initializeMetaTasks() {

		if (allMetaTasks == null) {

			allMetaTasks = new ArrayList<MetaTask>();

			// should initialize any-hour tasks first before other tasks
			// Note: currently, the 3 lists below have tasks that are mutually exclusive
			initAnyHourTasks();
			initWorkHourTasks();
			initNonWorkHourTasks();

			Set<MetaTask> allTasksSet = new HashSet<>();
			// Note: Using Set for adding tasks should prevent duplicate tasks when creating
			// the task list
			// However, each instance of the tasks must be explicitedly stated

			allTasksSet.addAll(workHourMetaTasks);
			allTasksSet.addAll(nonWorkHourMetaTasks);
			allTasksSet.addAll(anyHourMetaTasks);

			allMetaTasks.addAll(allTasksSet); // 55 tasks in total as of 2016-10-04

			// Note: anyHourTasks are supposed to be the union of workHourTasks and
			// nonWorkHourTasks.
			// Therefore, add anyHourTasks into the other two sets

			// Incorporate anyHourTasks into workHourTasks
			workHourMetaTasks.addAll(anyHourMetaTasks);

			// Incorporate anyHourTasks into nonWorkHourTasks
			nonWorkHourMetaTasks.addAll(anyHourMetaTasks);

		}
	}

	/**
	 * Lazy initialization of any-hour metaTasks list.
	 */
	private static void initAnyHourTasks() {

		if (anyHourMetaTasks == null) {

			anyHourMetaTasks = new ArrayList<MetaTask>();

			List<MetaTask> tasks = new ArrayList<MetaTask>();

			tasks.add(new EatMealMeta());
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
		Iterator<MetaTask> i = getAllMetaTasks().iterator();
		while (i.hasNext()) {
			MetaTask t = i.next();
			if (t.getClass().getSimpleName().equals(name)) {
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
		return new ArrayList<MetaTask>(robotMetaTasks);
	}

	public void destroy() {
		allMetaTasks = null;
		workHourMetaTasks = null;
		nonWorkHourMetaTasks = null;
		anyHourMetaTasks = null;
		robotMetaTasks = null;
	}
}