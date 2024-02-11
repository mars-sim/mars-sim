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

        var ess = simConfig.getSettlementConfiguration().getEssentialResources();

        int reviewDue = gm.getResourceReviewDue();
        assertEquals("Essential resoruces needing review at start", ess.keySet().size(), reviewDue);

        int reserved = gm.reserveResourceReview();

        // Add some gas
        s.storeAmountResource(reserved, 100D);
        reviewDue = gm.getResourceReviewDue();
        assertEquals("Essential resources needing review after reserve", ess.keySet().size()-1, reviewDue);

        double initialDemand = gm.getDemandValueWithID(reserved);
        gm.checkResourceDemand(reserved, 100D);
        double newDemand = gm.getDemandValueWithID(reserved);
        assertNotEquals("Demand has changed after review", initialDemand, newDemand);
    }
}
