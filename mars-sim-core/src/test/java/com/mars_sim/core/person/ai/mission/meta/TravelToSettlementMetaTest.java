package com.mars_sim.core.person.ai.mission.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.mission.MetaMission;
import com.mars_sim.core.mission.MissionCreationException;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.TravelToSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

class TravelToSettlementMetaTest extends MarsSimUnitTest{
    @Test
    void testConstructInstanceNoDest() {
        var start = buildSettlement("Start");
        var crew = createPreReqs(start);

        var meta = new TravelToSettlementMeta();
        var e = assertThrowsExactly(MissionCreationException.class, () -> meta.constructInstance(crew, false));

        assertEquals("mission.travelsettlement.dest", e.getContext().key(), "Message key should be correct");
    }

    @Test
    void testConstructInstance() throws MissionCreationException {
        var start = buildSettlement("Start", false);
        buildAccommodation(start.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);

        var crew = createPreReqs(start);

        // Create dest 1KM away
        var end = buildSettlement("End", false,
                    start.getCoordinates().getNewLocation(new Direction(0), 1));
        buildAccommodation(end.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);

        var meta = new TravelToSettlementMeta();
        var m = (TravelToSettlement) meta.constructInstance(crew, false);

        assertEquals(end, m.getDestinationSettlement(), "Destination should be correct");
    }

    private MetaMission.Roster createPreReqs(Settlement start) {
        var rover = buildRover(start, "Rover", LocalPosition.DEFAULT_POSITION, TRANSPORT_ROVER);

        List<Person> people = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            people.add(buildPerson("Person " + i, start));
        }

        var leader = buildPerson("Leader", start);

        return new MetaMission.Roster(leader, people, rover);
    }
}
