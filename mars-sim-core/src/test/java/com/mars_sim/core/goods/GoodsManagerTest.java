package com.mars_sim.core.goods;


import static org.junit.Assert.assertNotEquals;

import com.mars_sim.core.AbstractMarsSimUnitTest;
import com.mars_sim.core.goods.GoodsManager.CommerceType;

public class GoodsManagerTest extends AbstractMarsSimUnitTest {

    public void testResetCommerceFactor() {
        var s = buildSettlement();
        var gm = new GoodsManager(s);

        double newValue = 1.5D;
        gm.setCommerceFactor(CommerceType.BUILDING, newValue);
        assertEquals("Changed commerce", newValue, gm.getCommerceFactor(CommerceType.BUILDING));

        gm.resetCommerceFactors();
        assertEquals("Reset commerce", 1D, gm.getCommerceFactor(CommerceType.BUILDING));
    }

    public void testGetResourceReviewDue() {
        // Build a settlement with some people to generate demand
        var s = buildSettlement();
        buildPerson("P1", s);
        buildPerson("P2", s);

        var gm = new GoodsManager(s);

        var ess = getConfig().getSettlementConfiguration().getEssentialResources();

        int reviewDue = gm.getResourceReviewDue();
        assertEquals("Essential resources needing review at start", ess.keySet().size(), reviewDue);

        int reserved = gm.reserveResourceReview();

        // Add a resource with 1 kg
        s.storeAmountResource(reserved, 1D);
        reviewDue = gm.getResourceReviewDue();
        assertEquals("Essential resources needing review after reserve", ess.keySet().size()-1, reviewDue);

        double initialDemand = gm.getDemandScoreWithID(reserved);
        boolean passed = gm.moderateLifeResourceDemand(reserved) == 0;
        double newDemand = gm.getDemandScoreWithID(reserved);
        if (passed)
        	assertEquals("Demand remains the same after budget review", initialDemand, newDemand);
        else
        	assertNotEquals("Demand has changed after budget review", initialDemand, newDemand);
        
    }
}
