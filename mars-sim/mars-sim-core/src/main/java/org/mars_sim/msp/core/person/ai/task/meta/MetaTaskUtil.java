/**
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @version 3.08 2015-07-10
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
    private static List<MetaTask> metaTasks = null;
    private static List<MetaTask> workHourTasks = null;
    private static List<MetaTask> nonWorkHourTasks = null;
    private static List<MetaTask> allHourTasks = null;

    private static List<MetaTask> robotMetaTasks = null;
    /**
     * Private constructor for utility class.
     */
    private MetaTaskUtil() {};

    /**
     * Lazy initialization of metaTasks list.
     */
    private static void initializeMetaTasks() {

        metaTasks = new ArrayList<MetaTask>();

        initWorkHourTasks();
        initNonWorkHourTasks();
        initAllHourTasks();

        Set<MetaTask> s = new HashSet<>();

        s.addAll(workHourTasks);
        s.addAll(nonWorkHourTasks);
        s.addAll(allHourTasks);

        metaTasks.addAll(s);

        //metaTasks = ListUtils.union(workHourTasks, nonWorkHourTasks);
        //metaTasks = ListUtils.union(metaTasks, allHourTasks);

/*
        // Populate metaTasks list with all meta tasks.
        metaTasks.add(new AssistScientificStudyResearcherMeta());
        metaTasks.add(new CompileScientificStudyResultsMeta());
        metaTasks.add(new ConnectWithEarthMeta());
        metaTasks.add(new ConsolidateContainersMeta());
        metaTasks.add(new ConstructBuildingMeta());
        metaTasks.add(new CookMealMeta());
        metaTasks.add(new DigLocalIceMeta());
        metaTasks.add(new DigLocalRegolithMeta());
        metaTasks.add(new EatMealMeta());
        metaTasks.add(new InviteStudyCollaboratorMeta());
        metaTasks.add(new LoadVehicleEVAMeta());
        metaTasks.add(new LoadVehicleGarageMeta());
        metaTasks.add(new MaintainGroundVehicleEVAMeta());
        metaTasks.add(new MaintainGroundVehicleGarageMeta());
        metaTasks.add(new MaintenanceEVAMeta());
        metaTasks.add(new MaintenanceMeta());
        metaTasks.add(new ManufactureConstructionMaterialsMeta());
        metaTasks.add(new ManufactureGoodMeta());
        metaTasks.add(new ObserveAstronomicalObjectsMeta());
        metaTasks.add(new PeerReviewStudyPaperMeta());
        metaTasks.add(new PerformLaboratoryExperimentMeta());
        metaTasks.add(new PerformLaboratoryResearchMeta());
        metaTasks.add(new PerformMathematicalModelingMeta());
        metaTasks.add(new PrepareDessertMeta());
        metaTasks.add(new PrescribeMedicationMeta());
        metaTasks.add(new ProduceFoodMeta());
        metaTasks.add(new ProposeScientificStudyMeta());
		metaTasks.add(new ReadMeta());
        metaTasks.add(new RelaxMeta());
        metaTasks.add(new RepairEVAMalfunctionMeta());
        metaTasks.add(new RepairMalfunctionMeta());
        metaTasks.add(new RequestMedicalTreatmentMeta());
        metaTasks.add(new RespondToStudyInvitationMeta());
        metaTasks.add(new RestingMedicalRecoveryMeta());
        metaTasks.add(new ReviewJobReassignmentMeta());
        metaTasks.add(new ReturnLightUtilityVehicleMeta());
        metaTasks.add(new SalvageBuildingMeta());
        metaTasks.add(new SalvageGoodMeta());
        metaTasks.add(new SelfTreatHealthProblemMeta());
        metaTasks.add(new SleepMeta());
        metaTasks.add(new StudyFieldSamplesMeta());
        metaTasks.add(new TeachMeta());
        metaTasks.add(new TendGreenhouseMeta());
        metaTasks.add(new ToggleFuelPowerSourceMeta());
        metaTasks.add(new ToggleResourceProcessMeta());
        metaTasks.add(new TreatMedicalPatientMeta());
        metaTasks.add(new UnloadVehicleEVAMeta());
        metaTasks.add(new UnloadVehicleGarageMeta());
        metaTasks.add(new WalkMeta());
        metaTasks.add(new WorkoutMeta());
        metaTasks.add(new WriteReportMeta());
        metaTasks.add(new YogaMeta());
*/
    }


    /**
     * Lazy initialization of work hour metaTasks list.
     */
    private static void initWorkHourTasks() {

        workHourTasks = new ArrayList<MetaTask>();

    	List<MetaTask> tasks = new ArrayList<MetaTask>();

        tasks.add(new AssistScientificStudyResearcherMeta());
        tasks.add(new CompileScientificStudyResultsMeta());
        tasks.add(new ConsolidateContainersMeta());
        tasks.add(new ConstructBuildingMeta());
        tasks.add(new CookMealMeta());
        tasks.add(new DigLocalIceMeta());
        tasks.add(new DigLocalRegolithMeta());
        tasks.add(new InviteStudyCollaboratorMeta());
        tasks.add(new LoadVehicleEVAMeta());
        tasks.add(new LoadVehicleGarageMeta());
        tasks.add(new MaintainGroundVehicleEVAMeta());
        tasks.add(new MaintainGroundVehicleGarageMeta());
        tasks.add(new MaintenanceEVAMeta());
        tasks.add(new MaintenanceMeta());
        tasks.add(new ManufactureConstructionMaterialsMeta());
        tasks.add(new ManufactureGoodMeta());
        tasks.add(new PeerReviewStudyPaperMeta());
        tasks.add(new PerformLaboratoryExperimentMeta());
        tasks.add(new PerformLaboratoryResearchMeta());
        tasks.add(new PerformMathematicalModelingMeta());
        tasks.add(new PrepareDessertMeta());
        tasks.add(new PrescribeMedicationMeta());
        tasks.add(new ProduceFoodMeta());
        tasks.add(new ProposeScientificStudyMeta());
        tasks.add(new RespondToStudyInvitationMeta());
        tasks.add(new ReturnLightUtilityVehicleMeta());
        tasks.add(new ReviewJobReassignmentMeta());
        tasks.add(new SalvageBuildingMeta());
        tasks.add(new SalvageGoodMeta());
        tasks.add(new StudyFieldSamplesMeta());
        tasks.add(new TeachMeta());
        tasks.add(new TendGreenhouseMeta());
        tasks.add(new ToggleFuelPowerSourceMeta());
        tasks.add(new ToggleResourceProcessMeta());
        tasks.add(new TreatMedicalPatientMeta());
        tasks.add(new UnloadVehicleEVAMeta());
        tasks.add(new UnloadVehicleGarageMeta());
        tasks.add(new WriteReportMeta());

        initAllHourTasks();

        Set<MetaTask> s = new HashSet<>();

        s.addAll(tasks);
        s.addAll(allHourTasks);

        workHourTasks.addAll(s);
    }

    /**
     * Lazy initialization of work hour metaTasks list.
     */
    private static void initNonWorkHourTasks() {

    	nonWorkHourTasks = new ArrayList<MetaTask>();

    	List<MetaTask> tasks = new ArrayList<MetaTask>();

    	tasks.add(new ReadMeta());
    	tasks.add(new ConnectWithEarthMeta());
        tasks.add(new SleepMeta());
        tasks.add(new WalkMeta());
        tasks.add(new WorkoutMeta());
        tasks.add(new YogaMeta());

        initAllHourTasks();

        Set<MetaTask> s = new HashSet<>();

        s.addAll(tasks);
        s.addAll(allHourTasks);

        nonWorkHourTasks.addAll(s);
    }


    /**
     * Lazy initialization of work hour metaTasks list.
     */
    private static void initAllHourTasks() {

        allHourTasks = new ArrayList<MetaTask>();

        allHourTasks.add(new EatMealMeta());
        allHourTasks.add(new ObserveAstronomicalObjectsMeta());
        allHourTasks.add(new RelaxMeta());
        allHourTasks.add(new RepairEVAMalfunctionMeta());
        allHourTasks.add(new RepairMalfunctionMeta());
        allHourTasks.add(new RequestMedicalTreatmentMeta());
        allHourTasks.add(new RestingMedicalRecoveryMeta());
        allHourTasks.add(new SelfTreatHealthProblemMeta());
    }



   private static void initializeRobotMetaTasks() {

        robotMetaTasks = new ArrayList<MetaTask>();

        // Populate robotMetaTasks list with all robotMeta tasks.
        robotMetaTasks.add(new CookMealMeta());
        robotMetaTasks.add(new ConsolidateContainersMeta());
        robotMetaTasks.add(new ConstructBuildingMeta());
        robotMetaTasks.add(new LoadVehicleEVAMeta());
        robotMetaTasks.add(new LoadVehicleGarageMeta());
        robotMetaTasks.add(new MaintenanceEVAMeta());
        robotMetaTasks.add(new MaintenanceMeta());
        robotMetaTasks.add(new ManufactureGoodMeta());
        robotMetaTasks.add(new PrepareDessertMeta());
        robotMetaTasks.add(new PrescribeMedicationMeta());
        robotMetaTasks.add(new ProduceFoodMeta());
        robotMetaTasks.add(new RepairEVAMalfunctionMeta());
        robotMetaTasks.add(new RepairMalfunctionMeta());
        robotMetaTasks.add(new ReturnLightUtilityVehicleMeta());
        robotMetaTasks.add(new SalvageBuildingMeta());
        robotMetaTasks.add(new SleepMeta());
        robotMetaTasks.add(new TendGreenhouseMeta());
        robotMetaTasks.add(new UnloadVehicleEVAMeta());
        robotMetaTasks.add(new UnloadVehicleGarageMeta());
        robotMetaTasks.add(new WalkMeta());

    }

   /**
    * Gets a list of all meta tasks.
    * @return list of meta tasks.
    */
   public static List<MetaTask> getMetaTasks() {

       // Lazy initialize meta tasks list if necessary.
       if (metaTasks == null) {
           initializeMetaTasks();
       }

       // Return copy of meta task list.
       return new ArrayList<MetaTask>(metaTasks);
   }

   /**
    * Gets a list of all work hour meta tasks.
    * @return list of work hour meta tasks.
    */
   public static List<MetaTask> getWorkHourTasks() {

       // Lazy initialize work hour meta tasks list if necessary.
       if (workHourTasks == null) {
           initWorkHourTasks();
       }

       // Return copy of work hour meta task list.
       return new ArrayList<MetaTask>(workHourTasks);
   }

   /**
    * Gets a list of all non work hour meta tasks.
    * @return list of work hour meta tasks.
    */
   public static List<MetaTask> getNonWorkHourTasks() {

       // Lazy initialize non work hour meta tasks list if necessary.
       if (nonWorkHourTasks == null) {
           initNonWorkHourTasks();
       }

       // Return copy of non work hour meta task list.
       return new ArrayList<MetaTask>(nonWorkHourTasks);
   }

   /**
    * Gets a list of all hour meta tasks.
    * @return list of all hour meta tasks.
    */
   public static List<MetaTask> getAllWorkHourTasks() {

       // Lazy initialize all hour meta tasks list if necessary.
       if (allHourTasks == null) {
           initAllHourTasks();
       }

       // Return copy of all hour meta task list.
       return new ArrayList<MetaTask>(allHourTasks);
   }

    /**
     * Converts a task name in String to Metatask
     * @return meta tasks.
     */
    public static MetaTask getMetaTask(String name) {
    	MetaTask metaTask = null;
    	Iterator<MetaTask> i = getMetaTasks().iterator();
    	while (i.hasNext()) {
    		MetaTask t = i.next();
    		if (t.getClass().getSimpleName().equals(name)) {
    			metaTask = t;
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
}