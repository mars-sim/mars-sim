package com.mars_sim.core.mission.objectives;

import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.vehicle.Vehicle;

public class RescueVehicleObjective implements MissionObjective {

    private static final long serialVersionUID = 1L;

    private Vehicle recoverVehicle;

    private boolean rescue;
    

    public RescueVehicleObjective(Vehicle recoverVehicle, boolean isRescue) {
        this.recoverVehicle = recoverVehicle;
        this.rescue = isRescue;
    }

    public Vehicle getRecoverVehicle() {
        return recoverVehicle;
    }

    public boolean isRescue() {
        return rescue;
    }

    @Override
    public String getName() {
        return (rescue ? "Rescue " : "Salvage ") + "Vehicle " + recoverVehicle.getName();
    }   
}
