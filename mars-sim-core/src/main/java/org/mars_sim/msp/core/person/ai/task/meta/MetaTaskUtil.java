/**
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @version 3.08 2015-05-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility task for getting the list of meta tasks.
 */
public class MetaTaskUtil {

    // Static values.
    private static List<MetaTask> metaTasks = null;
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
        
        // Populate metaTasks list with all meta tasks.
        metaTasks.add(new AssistScientificStudyResearcherMeta());
        metaTasks.add(new CompileScientificStudyResultsMeta());
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
        metaTasks.add(new RelaxMeta());
        metaTasks.add(new RepairEVAMalfunctionMeta());
        metaTasks.add(new RepairMalfunctionMeta());
        metaTasks.add(new RequestMedicalTreatmentMeta());
        metaTasks.add(new RespondToStudyInvitationMeta());
        metaTasks.add(new RestingMedicalRecoveryMeta());
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
    
    public static List<MetaTask> getRobotMetaTasks() {
        
        // Lazy initialize meta tasks list if necessary.
        if (robotMetaTasks == null) {
            initializeRobotMetaTasks();
        }
        
        // Return copy of meta task list.
        return new ArrayList<MetaTask>(robotMetaTasks);
    }
}