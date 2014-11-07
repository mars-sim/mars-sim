/**
 * Mars Simulation Project
 * MetaTaskUtil.java
 * @version 3.07 2014-11-06
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
    
    /**
     * Private constructor for utility class.
     */
    private MetaTaskUtil() {};
    
    /**
     * Lazy initialization of metaTasks list.
     */
    private static void initializeMetaTasks() {
        
        metaTasks = new ArrayList<MetaTask>(43);
        
        // Populate metaTasks list with all meta tasks.
        // 2014-11-06 Added MakeSoyMeta(), DrinkSoymilkMeta()
        metaTasks.add(new MakeSoyMeta());
        metaTasks.add(new DrinkSoymilkMeta());
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
        metaTasks.add(new MedicalAssistanceMeta());
        metaTasks.add(new ObserveAstronomicalObjectsMeta());
        metaTasks.add(new PeerReviewStudyPaperMeta());
        metaTasks.add(new PerformLaboratoryExperimentMeta());
        metaTasks.add(new PerformLaboratoryResearchMeta());
        metaTasks.add(new PerformMathematicalModelingMeta());
        metaTasks.add(new PrescribeMedicationMeta());
        metaTasks.add(new ProposeScientificStudyMeta());
        metaTasks.add(new RelaxMeta());
        metaTasks.add(new RepairEVAMalfunctionMeta());
        metaTasks.add(new RepairMalfunctionMeta());
        metaTasks.add(new RespondToStudyInvitationMeta());
        metaTasks.add(new ReturnLightUtilityVehicleMeta());
        metaTasks.add(new SalvageBuildingMeta());
        metaTasks.add(new SalvageGoodMeta());
        metaTasks.add(new SleepMeta());
        metaTasks.add(new StudyFieldSamplesMeta());
        metaTasks.add(new TeachMeta());
        metaTasks.add(new TendGreenhouseMeta());
        metaTasks.add(new ToggleFuelPowerSourceMeta());
        metaTasks.add(new ToggleResourceProcessMeta());
        metaTasks.add(new UnloadVehicleEVAMeta());
        metaTasks.add(new UnloadVehicleGarageMeta());
        metaTasks.add(new WalkMeta());
        metaTasks.add(new WorkoutMeta());
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
}