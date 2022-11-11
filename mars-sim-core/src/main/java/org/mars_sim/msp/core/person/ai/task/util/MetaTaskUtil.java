/*
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @date 2022-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.person.ai.task.meta.AnalyzeMapDataMeta;
import org.mars_sim.msp.core.person.ai.task.meta.AssistScientificStudyResearcherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CompileScientificStudyResultsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConnectWithEarthMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConsolidateContainersMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConstructBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ConversationMeta;
import org.mars_sim.msp.core.person.ai.task.meta.CookMealMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalIceMeta;
import org.mars_sim.msp.core.person.ai.task.meta.DigLocalRegolithMeta;
import org.mars_sim.msp.core.person.ai.task.meta.EatDrinkMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ExamineBodyMeta;
import org.mars_sim.msp.core.person.ai.task.meta.InviteStudyCollaboratorMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ListenToMusicMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleEVAMeta;
import org.mars_sim.msp.core.person.ai.task.meta.LoadVehicleGarageMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintainBuildingMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintainEVAVehicleMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MaintainGarageVehicleMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureConstructionMaterialsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ManufactureGoodMeta;
import org.mars_sim.msp.core.person.ai.task.meta.MeetTogetherMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ObserveAstronomicalObjectsMeta;
import org.mars_sim.msp.core.person.ai.task.meta.OptimizeSystemMeta;
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
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.meta.ReportMissionControlMeta;
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
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.meta.WriteReportMeta;
import org.mars_sim.msp.core.person.ai.task.meta.YogaMeta;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask.TaskScope;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask.WorkerType;
import org.mars_sim.msp.core.robot.ai.task.ChargeMeta;

/**
 * A utility task for getting the list of meta tasks.
 */
public class MetaTaskUtil {

	private static List<MetaTask> dutyHourTasks = null;
	private static List<MetaTask> nonDutyHourTasks = null;
	private static List<MetaTask> nonWorkHourMetaTasks;

	private static List<MetaTask> personMetaTasks;
	private static List<MetaTask> robotMetaTasks = null;

	private static List<MetaTask> allMetaTasks;

	/**
	 * Private constructor for utility class.
	 */
	private MetaTaskUtil() {
	}

	/**
	 * Lazy initialisation of metaTasks list.
	 */
	public static synchronized void initializeMetaTasks() {

		if (allMetaTasks != null) {
			// Created by another thread during the wait
			return;
		}
		
		// Would be nice to dynamically load based on what is in the package
		allMetaTasks = new ArrayList<>();
		allMetaTasks.add(new AnalyzeMapDataMeta());
		allMetaTasks.add(new AssistScientificStudyResearcherMeta());
		allMetaTasks.add(new ChargeMeta());
		allMetaTasks.add(new CompileScientificStudyResultsMeta());
		allMetaTasks.add(new ConnectWithEarthMeta());
		allMetaTasks.add(new ConsolidateContainersMeta());
		allMetaTasks.add(new ConstructBuildingMeta());
		allMetaTasks.add(new CookMealMeta());
		allMetaTasks.add(new DigLocalIceMeta());
		allMetaTasks.add(new DigLocalRegolithMeta());
		allMetaTasks.add(new EatDrinkMeta());
		allMetaTasks.add(new ExamineBodyMeta());
		allMetaTasks.add(new ConversationMeta());
		allMetaTasks.add(new InviteStudyCollaboratorMeta());
		allMetaTasks.add(new ListenToMusicMeta());
		allMetaTasks.add(new LoadVehicleEVAMeta());
		allMetaTasks.add(new LoadVehicleGarageMeta());
		allMetaTasks.add(new MaintainEVAVehicleMeta());
		allMetaTasks.add(new MaintainGarageVehicleMeta());
		allMetaTasks.add(new MaintainBuildingMeta());
		allMetaTasks.add(new ManufactureConstructionMaterialsMeta());
		allMetaTasks.add(new ManufactureGoodMeta());
		allMetaTasks.add(new MeetTogetherMeta());
		allMetaTasks.add(new ObserveAstronomicalObjectsMeta());
		allMetaTasks.add(new OptimizeSystemMeta());
		allMetaTasks.add(new PeerReviewStudyPaperMeta());
		allMetaTasks.add(new PerformLaboratoryExperimentMeta());
		allMetaTasks.add(new PerformLaboratoryResearchMeta());
		allMetaTasks.add(new PerformMathematicalModelingMeta());
		allMetaTasks.add(new PlanMissionMeta());
		allMetaTasks.add(new PlayHoloGameMeta());
		allMetaTasks.add(new PrepareDessertMeta());
		allMetaTasks.add(new PrescribeMedicationMeta());
		allMetaTasks.add(new ProduceFoodMeta());
		allMetaTasks.add(new ProposeScientificStudyMeta());
		allMetaTasks.add(new ReadMeta());
		allMetaTasks.add(new RecordActivityMeta());
		allMetaTasks.add(new RelaxMeta());
		allMetaTasks.add(new RepairMalfunctionMeta());
		allMetaTasks.add(new ReportMissionControlMeta());
		allMetaTasks.add(new RequestMedicalTreatmentMeta());
		allMetaTasks.add(new RestingMedicalRecoveryMeta());
		allMetaTasks.add(new RespondToStudyInvitationMeta());
		allMetaTasks.add(new ReturnLightUtilityVehicleMeta());
		allMetaTasks.add(new ReviewJobReassignmentMeta());
		allMetaTasks.add(new ReviewMissionPlanMeta());
		allMetaTasks.add(new SalvageBuildingMeta());
		allMetaTasks.add(new SalvageGoodMeta());
		allMetaTasks.add(new SelfTreatHealthProblemMeta());
		allMetaTasks.add(new SleepMeta()); 
		allMetaTasks.add(new StudyFieldSamplesMeta());
		allMetaTasks.add(new TeachMeta());
		allMetaTasks.add(new TendFishTankMeta());
		allMetaTasks.add(new TendGreenhouseMeta());
		allMetaTasks.add(new ToggleFuelPowerSourceMeta());
		allMetaTasks.add(new ToggleResourceProcessMeta());
		allMetaTasks.add(new TreatMedicalPatientMeta());
		allMetaTasks.add(new UnloadVehicleEVAMeta());
		allMetaTasks.add(new UnloadVehicleGarageMeta());
		allMetaTasks.add(new WorkoutMeta());
		allMetaTasks.add(new WriteReportMeta());
		allMetaTasks.add(new YogaMeta());
		
		// Filter out All Unit Tasks
		personMetaTasks = allMetaTasks.stream()
				.filter(m -> ((m.getSupported() == WorkerType.BOTH)
								|| (m.getSupported() == WorkerType.PERSON)))
				.collect(Collectors.toUnmodifiableList());
		robotMetaTasks = allMetaTasks.stream()
				.filter(m -> ((m.getSupported() == WorkerType.BOTH)
								|| (m.getSupported() == WorkerType.ROBOT)))
				.collect(Collectors.toUnmodifiableList());
		
		// Build special Shift based lists
		// Should these be just Person task?
		List<MetaTask> workHourMetaTasks = personMetaTasks.stream()
				.filter(m -> m.getScope() == TaskScope.WORK_HOUR)
				.collect(Collectors.toList());		
		
		nonWorkHourMetaTasks = personMetaTasks.stream()
				.filter(m -> m.getScope() == TaskScope.NONWORK_HOUR)
				.collect(Collectors.toUnmodifiableList());

		List<MetaTask> anyHourMetaTasks = personMetaTasks.stream()
				.filter(m -> m.getScope() == TaskScope.ANY_HOUR)
				.collect(Collectors.toList());
		List<MetaTask> tasks = new ArrayList<>();
		tasks.addAll(anyHourMetaTasks);
		tasks.addAll(workHourMetaTasks);
		dutyHourTasks = Collections.unmodifiableList(tasks);

		tasks = new ArrayList<>();
		tasks.addAll(anyHourMetaTasks);
		tasks.addAll(nonWorkHourMetaTasks);
		nonDutyHourTasks = Collections.unmodifiableList(tasks);
	}

	/**
	 * Gets a list of all Person meta tasks.
	 * 
	 * @return list of meta tasks.
	 */
	public static List<MetaTask> getPersonMetaTasks() {
		return personMetaTasks;
	}

	/**
	 * Gets a list of all non work hour meta tasks.
	 * 
	 * @return list of work hour meta tasks.
	 */
	public static List<MetaTask> getNonWorkHourMetaTasks() {
		return nonWorkHourMetaTasks;
	}

	/**
	 * Gets a list of duty meta tasks.
	 * 
	 * @return list of duty meta tasks.
	 */
	public static List<MetaTask> getDutyHourTasks() {
		return dutyHourTasks;
	}
	
	/**
	 * Gets a list of non-duty meta tasks.
	 * 
	 * @return list of non-duty meta tasks.
	 */
	public static List<MetaTask> getNonDutyHourTasks() {
		return nonDutyHourTasks;
	}
	
	/**
	 * Converts a task name in String to Metatask
	 * 
	 * @return meta tasks.
	 */
	public static MetaTask getMetaTask(String name) {
		MetaTask metaTask = null;
		Iterator<MetaTask> i = allMetaTasks.iterator();
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
		return robotMetaTasks;
	}
}
