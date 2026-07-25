/**
 * Mars Simulation Project
 * VisitLandmarkStep.java
 * @date 2026-07-18
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.mission.objectives.LandmarkObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.project.Stage;
import com.mars_sim.core.resource.SuppliesManifest;

/**
 * Mission step that involves visiting a Landmark and allowing the crew to explore it.
 * The step will be complete when all crew members have completed their EVA to the Landmark 
 * or the time limit for the step has been reached.
 */
class VisitLandmarkStep extends MissionStep {

    private static final int SITE_TIME = 100; // mSol
    private static final int VIEWING_TIME = 30; // mSol
    private static final long serialVersionUID = 1L;
    private LandmarkObjective objective;

    public VisitLandmarkStep(MissionVehicleProject parent, Landmark landmark) {
        super(parent, Stage.ACTIVE, "Explore " + landmark.getName());

        objective = new LandmarkObjective(landmark, SITE_TIME, VIEWING_TIME);
    }

    /**
     * Calculates what resources are needed for this step.
     * 
     * @param includeOptionals Add optional resources to the manifest
     * @param resources Place to hold the order
     */
    @Override
    protected void getRequiredResources(SuppliesManifest resources, boolean includeOptionals) {
        addLifeSupportResource(objective.getMSolAtSite(), includeOptionals, resources);

        // Add EVA suits
        resources.addEquipment(EquipmentType.EVA_SUIT.getResourceID(), getMission().getMembers().size(), true);
    }

    @Override
    protected void start() {
        super.start();

        // Add a zero entry for each member
        getMission().getMembers().forEach(w -> objective.recordEVA(w, 0D));
        getMission().fireMissionUpdate(MissionObjective.CHANGE_EVENT, null);
    }

    /**
     * A Worker wants to execute some work for this mission step.
     * @param worker The Worker that is active
     * @return true if the Worker did some work, false if there was nothing to do
     */
    @Override
    protected boolean execute(Worker worker) {
        if (getStepDuration() > objective.getMSolAtSite()) {
            complete();
            return false;
        }

        // Must be a Person
        if (worker instanceof Person p && !objective.isEVADone(worker)
            && p.getPhysicalCondition().isEVAFit()) {
            // Do EVA to visit the landmark
            var newTask = new LandmarkEVA(p, getMission(), objective);
            assignTask(worker, newTask);
            return true;
        }

        // nothing to do
        return false;
    }
    
    /**
     * Return the objective for visiting a site.
     */
    @Override
    public MissionObjective getObjective() {
        return objective;
    }
}