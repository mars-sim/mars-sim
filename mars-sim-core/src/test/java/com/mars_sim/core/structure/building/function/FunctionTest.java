package com.mars_sim.core.structure.building.function;


import java.util.ArrayList;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.person.Person;
import com.mars_sim.mapdata.location.LocalPosition;

public class FunctionTest extends AbstractMarsSimUnitTest {

    /**
     *
     */
    private static final int X_OFFSET = 10;
    private static final int Y_OFFSET = 15;


    public void testBuildActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
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
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
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
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
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

    public void testActivitySpotPosition() {
        var home = buildSettlement("Test");
        var building1 = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 90D, 0);
        var spots1 = building1.getRecreation().getActivitySpots();
        
        // Check the 2 sets of Activy spots are offset according to the Building
        for(var as : spots1) {
            var asp1 = as.getPos();
            assertTrue("Activity spot in Build:" + asp1.getShortFormat(),
                    LocalAreaUtil.isPositionWithinLocalBoundedObject(asp1, building1));
        }
    }

    public void testCreateActivitySpot() {
        var home = buildSettlement("Test");
        var p1 = LocalPosition.DEFAULT_POSITION;
        var building1 = buildRecreation(home.getBuildingManager(), p1, 0D, 0).getRecreation();
        var spots1 = building1.getActivitySpots();

        var p2 = new LocalPosition(p1.getX() + X_OFFSET, p1.getY() + Y_OFFSET);
        var building2 = buildRecreation(home.getBuildingManager(), p2, 0D, 1).getRecreation();
        
        // Check the 2 sets of Activy spots are offset according to the Building
        for(var as : spots1) {
            var asp1 = as.getPos();
            var asp2 = new LocalPosition(asp1.getX() + X_OFFSET, asp1.getY() + Y_OFFSET);
            var as2 = building2.findActivitySpot(asp2);
            assertNotNull("Activity spot found:" + asp2.getShortFormat(), as2);
        }
    }

    public void testReassignActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 0);
        var b1 = building.getRecreation();
        var building2 = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D, 1);
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
