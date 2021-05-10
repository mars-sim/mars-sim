/**
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.ai.task.meta.AssistScientificStudyResearcherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CompileScientificStudyResultsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConnectWithEarthMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConsolidateContainersMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CookMealMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DayDreamMeta;
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
import org.mars_sim.msp.core.person.ai.task.meta.TendFishTankMeta;
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
		
		initializeMetaTasks();
		
		initDutyHourTasks();
		initNonDutyHourTasks();
	};

	/**
	 * Lazy initialization of metaTasks list.
	 */
	private synchronized static void initializeMetaTasks() {

		if (allMetaTasks != null) {
			// Created by another thread during the wait
			return;
		}
		
		// 55 tasks in total as of 2016-10-04
		List<MetaTask> tasks = new ArrayList<MetaTask>();

		// should initialize any-hour tasks first before other tasks
		// Note: currently, the 3 lists below have tasks that are mutually exclusive
		initAnyHourTasks();
		initWorkHourTasks();
		initNonWorkHourTasks();
		
		tasks.addAll(workHourMetaTasks); 
		tasks.addAll(nonWorkHourMetaTasks); 
		tasks.addAll(anyHourMetaTasks); 
		
		allMetaTasks = Collections.unmodifiableList(tasks);
	}

	/**
	 * Lazy initialization of any-hour metaTasks list.
	 */
	private synchronized static void initAnyHourTasks() {

		if (anyHourMetaTasks != null) {
			// Created by another Thread during the wait
			return;
		}

		List<MetaTask> tasks = new ArrayList<MetaTask>();
		
		tasks.add(new DayDreamMeta());
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

		anyHourMetaTasks = Collections.unmodifiableList(tasks);
	}

	/**
	 * Lazy initialization of duty hour tasks list.
	 */
	private synchronized static void initDutyHourTasks() {

		if (dutyHourTasks != null) {
			// Created by another Thread during the wait
			return;
		}
		ArrayList<MetaTask> tasks = new ArrayList<MetaTask>();

		tasks.addAll(anyHourMetaTasks);
		tasks.addAll(workHourMetaTasks);
		
		dutyHourTasks = Collections.unmodifiableList(tasks);
	}
	
	/**
	 * Lazy initialization of work-hour metaTasks list.
	 */
	private synchronized static void initWorkHourTasks() {

		if (workHourMetaTasks != null) {
			// Already build by another Thread whilse I was waiting
			return;
		}

		List<MetaTask> tasks = new ArrayList<>();

		// Use set to ensure non-duplicate tasks
		// TODO: how to get around the need of comparing the new instance of the same class			
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
		tasks.add(new TendFishTankMeta());
		tasks.add(new TendGreenhouseMeta());
		tasks.add(new ToggleFuelPowerSourceMeta());
		tasks.add(new ToggleResourceProcessMeta());
//	        tasks.add(new TreatMedicalPatientMeta());
		tasks.add(new UnloadVehicleEVAMeta());
		tasks.add(new UnloadVehicleGarageMeta());
		tasks.add(new WriteReportMeta());

		workHourMetaTasks = Collections.unmodifiableList(tasks);
	}
	
	/**
	 * Lazy initialization of duty hour tasks list.
	 */
	private synchronized static void initNonDutyHourTasks() {

		if (nonDutyHourTasks != null) {
			// Already build by a differnet Thread
			return;
		}

		List<MetaTask> tasks = new ArrayList<MetaTask>();

		// What about if these are not created ?
		tasks.addAll(anyHourMetaTasks);
		tasks.addAll(nonWorkHourMetaTasks);
		nonDutyHourTasks = Collections.unmodifiableList(tasks);
	}
	
	/**
	 * Lazy initialization of non-work hour metaTasks list.
	 */
	private synchronized static void initNonWorkHourTasks() {

		if (nonWorkHourMetaTasks != null) {
			// Create by a different Thread during the wait
			return;
		}

		List<MetaTask> tasks = new ArrayList<MetaTask>();

		tasks.add(new ConnectWithEarthMeta());
		// tasks.add(new HaveConversationMeta());
		// tasks.add(new ListenToMusicMeta());
		tasks.add(new PlayHoloGameMeta());
		tasks.add(new ReadMeta());
		// tasks.add(new SleepMeta());
		tasks.add(new WorkoutMeta());
		tasks.add(new YogaMeta());

		nonWorkHourMetaTasks = Collections.unmodifiableList(tasks);
	}

	private synchronized static void initializeRobotMetaTasks() {

		if (robotMetaTasks != null) {
			// Created by another Thread during the wait
			return;
		}
		List<MetaTask> tasks = new ArrayList<MetaTask>();

		// Populate robotMetaTasks list with all robotMeta tasks.
		tasks.add(new CookMealMeta());
		tasks.add(new ConsolidateContainersMeta());
		// tasks.add(new ConstructBuildingMeta());
		// tasks.add(new LoadVehicleEVAMeta());
		tasks.add(new LoadVehicleGarageMeta());
		// tasks.add(new MaintenanceEVAMeta());
		tasks.add(new MaintenanceMeta());
		tasks.add(new MaintainGroundVehicleGarageMeta());
		tasks.add(new ManufactureGoodMeta());
		tasks.add(new PrepareDessertMeta());
		tasks.add(new PrescribeMedicationMeta());
		tasks.add(new ProduceFoodMeta());
		// tasks.add(new RepairEVAMalfunctionMeta());
		tasks.add(new RepairMalfunctionMeta());
		// tasks.add(new ReturnLightUtilityVehicleMeta());
		// tasks.add(new SalvageBuildingMeta());
		tasks.add(new SleepMeta());
		tasks.add(new TendGreenhouseMeta());
		// tasks.add(new UnloadVehicleEVAMeta());
		tasks.add(new UnloadVehicleGarageMeta());
		tasks.add(new WalkMeta());
		
		robotMetaTasks = Collections.unmodifiableList(tasks);
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
