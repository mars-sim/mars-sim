package com.mars_sim.core.building.function;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


import java.util.ArrayList;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.LocalAreaUtil;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;

public class FunctionTest extends MarsSimUnitTest {

    /**
     *
     */
    private static final int X_OFFSET = 10;
    private static final int Y_OFFSET = 15;


    @Test
    public void testBuildActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var b = building.getRecreation();
        assertNotNull(b, "Builidng has Recreation");

        var spots = b.getActivitySpots();
        assertFalse(spots.isEmpty(), "Has Activity spots");
        assertEquals(spots.size(), b.getNumEmptyActivitySpots(), "Unoccupied count");
        assertEquals(0, b.getNumOccupiedActivitySpots(), "Occupied count");

        for(var as : spots) {
            var as1 = b.findActivitySpot(as.getPos());
            assertEquals(as, as1, "Found Activity spot");
        }

    }

    @Test
    public void testClaimActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var b = building.getRecreation();

        Person p = buildPerson("Worker", home);

        var spots = b.getActivitySpots();
        var as = (new ArrayList<ActivitySpot>(spots)).get(0); // Bad code
        b.claimActivitySpot(as.getPos(), p);

        var allocation = p.getActivitySpot();
        assertNotNull(allocation, "Activity spot owned");
        var claimed = allocation.getAllocated();
        assertEquals(as, claimed, "Correct activity spot");
        assertFalse(claimed.isEmpty(), "Activity spot empty");
        assertEquals(p.getIdentifier(), claimed.getID(), "Activity spot owned by Person");
        assertEquals(spots.size()-1, b.getNumEmptyActivitySpots(), "Unoccupied count");
        assertEquals(1, b.getNumOccupiedActivitySpots(), "Occupied count");
    }

    @Test
    public void testLeaveActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var b = building.getRecreation();

        Person p = buildPerson("Worker", home);

        var spots = b.getActivitySpots();
        var as = (new ArrayList<ActivitySpot>(spots)).get(0); // Bad code
        b.claimActivitySpot(as.getPos(), p);

        var claimed = p.getActivitySpot().getAllocated();
        p.setActivitySpot(null);
        assertNull(p.getActivitySpot(), "No activity spot allocated");
        assertTrue(claimed.isEmpty(), "Activity spot released");
        assertEquals(spots.size(), b.getNumEmptyActivitySpots(), "Unoccupied count");
        assertEquals(0, b.getNumOccupiedActivitySpots(), "Occupied count");
    }

    @Test
    public void testReleaseActivitySpot() {
        var home = buildSettlement("Test");

        Person p = buildPerson("Worker", home);

        var spot = new ActivitySpot("S", LocalPosition.DEFAULT_POSITION);
        var allocated = spot.claim(p, true, null);

        assertTrue(!spot.isEmpty(), "Activity spot allocated");

        // Leave spot
        allocated.leave(p, false);
        assertTrue(!spot.isEmpty(), "Activity spot still allocated");

        // Release spot
        allocated.leave(p, true);
        assertTrue(spot.isEmpty(), "Activity spot release");
    }

    @Test
    public void testActivitySpotPosition() {
        var home = buildSettlement("Test");
        var building1 = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 90D);
        var spots1 = building1.getRecreation().getActivitySpots();
        
        // Check the 2 sets of Activy spots are offset according to the Building
        for(var as : spots1) {
            var asp1 = as.getPos();
            assertTrue(LocalAreaUtil.isPositionWithinLocalBoundedObject(asp1, building1), "Activity spot in Build:" + asp1.getShortFormat());
        }
    }

    @Test
    public void testCreateActivitySpot() {
        var home = buildSettlement("Test");
        var p1 = LocalPosition.DEFAULT_POSITION;
        var building1 = buildRecreation(home.getBuildingManager(), p1, 0D).getRecreation();
        var spots1 = building1.getActivitySpots();

        var p2 = new LocalPosition(p1.getX() + X_OFFSET, p1.getY() + Y_OFFSET);
        var building2 = buildRecreation(home.getBuildingManager(), p2, 0D).getRecreation();
        
        // Check the 2 sets of Activy spots are offset according to the Building
        for(var as : spots1) {
            var asp1 = as.getPos();
            var asp2 = new LocalPosition(asp1.getX() + X_OFFSET, asp1.getY() + Y_OFFSET);
            var as2 = building2.findActivitySpot(asp2);
            assertNotNull(as2, "Activity spot found:" + asp2.getShortFormat());
        }
    }

    @Test
    public void testReassignActivitySpot() {
        var home = buildSettlement("Test");
        var building = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var b1 = building.getRecreation();
        var building2 = buildRecreation(home.getBuildingManager(), LocalPosition.DEFAULT_POSITION, 0D);
        var b2 = building2.getRecreation();
        Person p = buildPerson("Worker", home);

        // Assign spot in 1st building
        var as1 = (new ArrayList<ActivitySpot>(b1.getActivitySpots())).get(0); // Bad code
        b1.claimActivitySpot(as1.getPos(), p);

        // Claim spot in 2nd building
        var as2 = (new ArrayList<ActivitySpot>(b2.getActivitySpots())).get(0); // Bad code
        b2.claimActivitySpot(as2.getPos(), p);

        assertTrue(as1.isEmpty(), "Previous spot released");
        assertEquals(0, b1.getNumOccupiedActivitySpots(), "Previous spots all empty");

        var allocation = p.getActivitySpot();
        assertNotNull(allocation, "2nd activity spot allocated");
        var claimed = allocation.getAllocated();
        assertEquals(as2, claimed, "Person owns correct 2nd spot");
        assertEquals(1, b2.getNumOccupiedActivitySpots(), "Occupied spots in 2nd building");
    }
}
