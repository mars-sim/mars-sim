/**
 * Mars Simulation Project
 * EVAMissionStep.java
 * @date 2026-08-29
 * @author Barry Evans
 */
package com.mars_sim.core.mission.steps;

import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resource.SuppliesManifest;

/**
 * This is a specialised Mission step that supports Workers performing EVA. 
 * It checks the validity of the situation before allowing any EVA to start such as time on site.
 * It ensures that all members have completed EVA before the step can be completed.
 */
public abstract class EVAMissionStep extends MissionStep {

    private static SimLogger logger = SimLogger.getLogger(EVAMissionStep.class.getName());
    
    private int maxSiteTime;
    private boolean evaAllowed = true;

    protected EVAMissionStep(MissionVehicleProject parent, Stage active, String description,
                            int maxSiteTime) {
        super(parent, active, description);

        this.maxSiteTime = maxSiteTime;
    }

    /**
     * Get the maximum time on site in mSols.
     * @return the maximum time on site in mSols
     */
    public int getMaxSiteTime() {
        return maxSiteTime;
    }

    /**
     * Calculates what resources are needed for this step.
     * 
     * @param includeOptionals Add optional resources to the manifest
     * @param resources Place to hold the order
     */
    @Override
    protected void getRequiredResources(SuppliesManifest resources, boolean includeOptionals) {
        addLifeSupportResource(maxSiteTime, includeOptionals, resources);

        // Add EVA suits including oxygen to load
        var numSuits = getMission().getMembers().size();
        resources.addAmount(ResourceUtil.OXYGEN_ID, 3D * numSuits, true);
        resources.addAmount(ResourceUtil.WATER_ID, 3D * numSuits, true);

        resources.addEquipment(EquipmentType.EVA_SUIT.getResourceID(), numSuits, true);
    }

    /**
     * A Worker wants to execute some work for this mission step.
     * @param worker The Worker that is active
     * @return true if the Worker did some work, false if there was nothing to do
     */
    @Override
    protected boolean execute(Worker worker) {

        if (getStepDuration() > maxSiteTime) {
            endAllEVAs();
            return false;
        }
        else if (!evaAllowed) {
            return false;
        }
        // Currently only Persons can do EVA
        else if (worker instanceof Person p && !p.getPhysicalCondition().isEVAUnFit()) {
            return executeEVA(p);
        }
        return false;
    }

    /**
     * Attempt to start an EVA for this worker. This is called from the execute method if the EVA is allowed.
     * @param worker The Worker attempting to start the EVA
     * @return true if the EVA was successfully started, false otherwise
     */
    protected abstract boolean executeEVA(Person worker);

    /**
     * End all active EVA operations. 
     * if all crew members are on board, the mission step will be completed.
     * If any crew members are not on board, the EVA will be ended and the step will remain active until all crew members are on board.
     * Crew members are informed to end EVA via the {@link EVAOperation#requestEndEVA()} method.
     */
    protected void endAllEVAs() {
        var ms = getMission();
        if (evaAllowed) {
            // First time through so log the end of EVA and add a mission log entry
            logger.info(ms, "Stopping all EVAs for mission step");
            ms.addMissionLog("Stopping EVAs", null);

            evaAllowed = false;
        }

        boolean allOnBoard = true;
        for(var member : ms.getMembers()) {
            if (!member.isInVehicle()) {
                allOnBoard = false;

                var task = member.getTaskManager().getTask();
                if (task instanceof EVAOperation eo) {
                    logger.info(member, "Ordered to end EVA for " + this);
                    eo.requestEndEVA();
                }
            }
        }

        if (allOnBoard) {
            logger.info(ms, "All crew members are on board, completing EVA mission step");
            complete();
        }    
    }
}
