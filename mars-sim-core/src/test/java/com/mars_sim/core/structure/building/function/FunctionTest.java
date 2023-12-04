package com.mars_sim.core.structure.building.function;


import java.util.ArrayList;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.person.Person;

public class FunctionTest extends AbstractMarsSimUnitTest {

    public void testBuildActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), null, 0D, 0);
        var b = building.getRecreation();

        var spots = b.getActivitySpots();
        assertFalse("Has Activity spots", spots.isEmpty());
        assertEquals("Unoccupied count", spots.size(), b.getNumEmptyActivitySpots());
        assertEquals("Occupied count", 0, b.getNumOccupiedActivitySpots());

        for(var as : spots) {
            var as1 = b.findActivitySpot(as.getPos());
            assertEquals("Found Activity spot", as, as1);
        }

    }

    public void testClaimActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), null, 0D, 0);
        var b = building.getRecreation();

        Person p = buildPerson("Worker", home);

        var spots = b.getActivitySpots();
        var as = (new ArrayList<ActivitySpot>(spots)).get(0); // Bad code
        b.claimActivitySpot(as.getPos(), p);

        ActivitySpot claimed = p.getActivitySpot();
        assertNotNull("Activity spot owned", claimed);
        assertEquals("Correct activity spot", as, claimed);
        assertFalse("Activity spot empty", claimed.isEmpty());
        assertEquals("Activity spot owned by Person", p.getIdentifier(), claimed.getID());
        assertEquals("Unoccupied count", spots.size()-1, b.getNumEmptyActivitySpots());
        assertEquals("Occupied count", 1, b.getNumOccupiedActivitySpots());
    }

    public void testReleaseActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), null, 0D, 0);
        var b = building.getRecreation();

        Person p = buildPerson("Worker", home);

        var spots = b.getActivitySpots();
        var as = (new ArrayList<ActivitySpot>(spots)).get(0); // Bad code
        b.claimActivitySpot(as.getPos(), p);

        ActivitySpot claimed = p.getActivitySpot();
        p.setActivitySpot(null);
        assertNull("No activity spot allocated", p.getActivitySpot());
        assertTrue("Activity spot released", claimed.isEmpty());
        assertEquals("Unoccupied count", spots.size(), b.getNumEmptyActivitySpots());
        assertEquals("Occupied count", 0, b.getNumOccupiedActivitySpots());
    }

    public void testReassignActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), null, 0D, 0);
        var b1 = building.getRecreation();
        var building2 = buildRecreation(home.getBuildingManager(), null, 0D, 1);
        var b2 = building2.getRecreation();
        Person p = buildPerson("Worker", home);

        // Assign spot in 1st building
        var as1 = (new ArrayList<ActivitySpot>(b1.getActivitySpots())).get(0); // Bad code
        b1.claimActivitySpot(as1.getPos(), p);

        // Claim spot in 2nd building
        var as2 = (new ArrayList<ActivitySpot>(b2.getActivitySpots())).get(0); // Bad code
        b2.claimActivitySpot(as2.getPos(), p);

        assertTrue("Previous spot released", as1.isEmpty());
        assertEquals("Previous spots all empty", 0, b1.getNumOccupiedActivitySpots());

        ActivitySpot claimed = p.getActivitySpot();
        assertNotNull("2nd activity spot allocated", claimed);
        assertEquals("Person owns correct 2nd spot", as2, claimed);
        assertEquals("Occupied spots in 2nd building", 1, b2.getNumOccupiedActivitySpots());
    }
}
