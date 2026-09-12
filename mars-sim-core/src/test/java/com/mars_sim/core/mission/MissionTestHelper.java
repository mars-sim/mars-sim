package com.mars_sim.core.mission;

import java.util.ArrayList;
import java.util.Map;

import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.equipment.EquipmentFactory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

/**
 * Helper for mission unit tests.
 */
public class MissionTestHelper {

    private MissionTestHelper() {
        // Prevent instantiation
    }
    
    /**
     * Build a Roster for a mission with the specified number of workers and a vehicle.
     * 
     * @param context The simulation context
     * @param s The settlement where the mission is based
     * @param workerNum The number of workers to include in the roster
     * @param roverType The type of rover to use for the mission
     * @param onVehicle Whether the workers should be placed on the vehicle initially
     * @return A MetaMission.Roster containing the leader, members, and vehicle
     */
    public static MetaMission.Roster buildRoster(MarsSimContext context, Settlement s, int workerNum,
                            String roverType, boolean onVehicle) {
        var r = context.buildRover(s, "Rover", LocalPosition.DEFAULT_POSITION, roverType);
        
        var l = context.buildPerson("Leader", s);
        var workers  = new ArrayList<Worker>();
        for(var i = 0; i < workerNum; i++) {
             workers.add(context.buildPerson("Worker " + i, s));
        }
    
        if (onVehicle) {
            workers.forEach(w -> w.transfer(r));
            l.transfer(r);
        }
    
        return new MetaMission.Roster(l, workers, r);
    }

    /**
     * This builds a Roster crew for an EVA mission needing an optional set of Containers.
     * @param context The test context to build the crew.
     * @param s The settlement to build the crew in.
     * @param containterType The type of container to build for the mission; optional if containerCount is 0.
     * @param containerCount The number of containers to build for the mission.
     * @param roverType The type of rover to build for the mission.
     * @return A Roster of crew members for the EVA mission.
     */
    public static MetaMission.Roster buildEVACrew(MarsSimContext context, Settlement s, EquipmentType containterType,
        int containerCount, String roverType) {
    
        var crew = buildRoster(context, s, 1, roverType, true);
    
        // Build EVA suite to support mission
        for(int i = 0; i < crew.members().size() + 1; i++) {
            EquipmentFactory.createEquipment(EquipmentType.EVA_SUIT, s);
        }
    
        var resources = Map.of(ResourceUtil.OXYGEN_ID, 200D,
                ResourceUtil.WATER_ID, 200D,
                ResourceUtil.FOOD_ID, 200D,
                ResourceUtil.METHANOL_ID, 200D);
        MarsSimUnitTest.loadAmounts(s.getEquipmentInventory(), resources);
    
        for(int c = 0; c < containerCount; c++) {
            EquipmentFactory.createEquipment(containterType, s);
        }
    
        return crew;
    }

}
