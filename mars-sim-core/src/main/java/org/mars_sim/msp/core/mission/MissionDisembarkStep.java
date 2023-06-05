package org.mars_sim.msp.core.mission;

import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.project.Stage;

public class MissionDisembarkStep extends MissionStep {

    /**
     * @param parent Parent mission
     */
    public MissionDisembarkStep(MissionVehicleProject parent) {
        super(parent, Stage.CLOSEDOWN, "Disembark");
    }

    @Override
    protected boolean execute(Worker worker) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

}
