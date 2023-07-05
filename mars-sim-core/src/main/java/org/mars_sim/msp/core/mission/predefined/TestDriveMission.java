/**
 * Mars Simulation Project
 * TestDriveMission.java
 * @date 2023-06-03
 * @author Barry Evans
 */
package org.mars_sim.msp.core.mission.predefined;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.Direction;
import org.mars_sim.msp.core.mission.MissionStep;
import org.mars_sim.msp.core.mission.MissionTravelStep;
import org.mars_sim.msp.core.mission.MissionVehicleProject;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * This is a Mission that executes a test drive/driver training mission.
 */
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
