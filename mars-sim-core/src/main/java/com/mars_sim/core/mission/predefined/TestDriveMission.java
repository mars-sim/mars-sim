/**
 * Mars Simulation Project
 * TestDriveMission.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.mission.MissionStep;
import com.mars_sim.core.mission.MissionTravelStep;
import com.mars_sim.core.mission.MissionVehicleProject;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.structure.Settlement;

/**
 * This is a Mission that executes a test drive/driver training mission.
 */
@SuppressWarnings("serial")
public class TestDriveMission extends MissionVehicleProject{

    // Distance in the test drive before turning round
    public static final double TRAVEL_DIST = 50D;

    public TestDriveMission(String name, Person leader) {
        super(name, MissionType.TRAVEL_TO_SETTLEMENT, 10, 2, leader);
        
        if (!isDone()) {
            Settlement base = leader.getAssociatedSettlement();
            Coordinates startingPlace = base.getCoordinates();
            Coordinates turningPoint = startingPlace.getNewLocation(new Direction(0), TRAVEL_DIST);

            List<MissionStep> plan = new ArrayList<>();
            plan.add(new MissionTravelStep(this, new NavPoint(turningPoint, "Turn around",
                                                                startingPlace)));
            plan.add(new MissionTravelStep(this, new NavPoint(base, turningPoint)));           
                                                                
            setSteps(plan);                                                   
        }
    }
}
