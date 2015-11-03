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
    private static List<MetaTask> anyHourTasks = null;

    private static List<MetaTask> robotMetaTasks = null;
    /**
     * Private constructor for utility class.
     */
    private MetaTaskUtil() {};

    /**
     * Lazy initialization of metaTasks list.
     */
    private static void initializeMetaTasks() {

    	if (metaTasks == null) {

    		metaTasks = new ArrayList<MetaTask>();

	        // should initialize any-hour tasks first before other tasks
	        initAnyHourTasks();
	        initWorkHourTasks();
	        initNonWorkHourTasks();

	        Set<MetaTask> s = new HashSet<>();
	        // TODO: NOT WORKING: fix the use of set to avoid duplicate tasks
	        // Using Set for adding below should prevent duplicate tasks when creating the task list
	        s.addAll(workHourTasks);
	        s.addAll(nonWorkHourTasks);
	        s.addAll(anyHourTasks);

	        metaTasks.addAll(s);

	        // 2015-09-28 Incorporate anyHourTasks into workHourTasks
	        workHourTasks.addAll(anyHourTasks);

	        // 2015-09-28 Incorporate anyHourTasks into nonWorkHourTasks
	        nonWorkHourTasks.addAll(anyHourTasks);

	        //System.out.println("done initializeMetaTasks()");

    	}
    }


    /**
     * Lazy initialization of any-hour metaTasks list.
     */
    private static void initAnyHourTasks() {

       	if (anyHourTasks == null) {

	        anyHourTasks = new ArrayList<MetaTask>();

	        anyHourTasks.add(new EatMealMeta());
	        anyHourTasks.add(new ListenToMusicMeta());
	        anyHourTasks.add(new LoadVehicleEVAMeta());
	        anyHourTasks.add(new LoadVehicleGarageMeta());
	        anyHourTasks.add(new ObserveAstronomicalObjectsMeta());
	        anyHourTasks.add(new RelaxMeta());
	        anyHourTasks.add(new RepairEVAMalfunctionMeta());
	        anyHourTasks.add(new RepairMalfunctionMeta());
	        anyHourTasks.add(new RequestMedicalTreatmentMeta());
	        anyHourTasks.add(new RestingMedicalRecoveryMeta());
	        anyHourTasks.add(new ReturnLightUtilityVehicleMeta());
	        anyHourTasks.add(new SelfTreatHealthProblemMeta());
	        anyHourTasks.add(new SleepMeta()); // if a person is having high fatigue, he/she may fall asleep at work
	        anyHourTasks.add(new WalkMeta());

	        //System.out.println("size of anyHourTasks : " + anyHourTasks.size());
       	}
    }



    /**
     * Lazy initialization of work-hour metaTasks list.
     */
    private static void initWorkHourTasks() {

       	if (workHourTasks == null) {

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
	        //tasks.add(new LoadVehicleEVAMeta());
	        //tasks.add(new LoadVehicleGarageMeta());
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
	        //tasks.add(new ReturnLightUtilityVehicleMeta());
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

	        //Set<MetaTask> s = new HashSet<>();
	        // TODO: NOT WORKING: fix the use of set to avoid duplicate tasks
	        // Using Set for adding below should prevent duplicate tasks when creating the task list
	        //s.addAll(tasks);
	        //s.addAll(anyHourTasks);

	        workHourTasks.addAll(tasks);
	        // Note: do NOT add anyHourTasks to workHourTasks at this point
	        //workHourTasks.addAll(anyHourTasks);

	        //System.out.println("size of workHourTasks : " + workHourTasks.size());
       	}
    }

    /**
     * Lazy initialization of non-work hour metaTasks list.
     */
    private static void initNonWorkHourTasks() {

       	if (nonWorkHourTasks == null) {

	    	nonWorkHourTasks = new ArrayList<MetaTask>();

	    	List<MetaTask> tasks = new ArrayList<MetaTask>();

	    	tasks.add(new ConnectWithEarthMeta());
	    	tasks.add(new HaveConversationMeta());
	    	tasks.add(new PlayHoloGameMeta());
	    	tasks.add(new ReadMeta());

	        //tasks.add(new SleepMeta());
	        tasks.add(new WorkoutMeta());
	        tasks.add(new YogaMeta());

	        //Set<MetaTask> s = new HashSet<>();
	        // TODO: NOT WORKING: fix the use of set to avoid duplicate tasks
	        // Using Set for adding below should prevent duplicate tasks when creating the task list
	        //s.addAll(tasks);
	        //s.addAll(anyHourTasks);

	        nonWorkHourTasks.addAll(tasks);
	        // Note: do NOT add anyHourTasks to nonWorkHourTasks at this point
	        //nonWorkHourTasks.addAll(anyHourTasks);

	        //System.out.println("size of nonWorkHourTasks : " + nonWorkHourTasks.size());
       	}
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

       //System.out.println("size of metaTasks : " + metaTasks.size());

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
       if (anyHourTasks == null) {
           initAnyHourTasks();
       }

       // Return copy of all hour meta task list.
       return new ArrayList<MetaTask>(anyHourTasks);
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