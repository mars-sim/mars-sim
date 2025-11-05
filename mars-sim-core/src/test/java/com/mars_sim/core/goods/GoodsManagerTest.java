package com.mars_sim.core.goods;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


import static org.junit.Assert.assertNotEquals;

import com.mars_sim.core.test.MarsSimUnitTest;
import com.mars_sim.core.goods.GoodsManager.CommerceType;

public class GoodsManagerTest extends MarsSimUnitTest {

    @Test
    public void testResetCommerceFactor() {
        var s = buildSettlement("mock");
        var gm = new GoodsManager(s);

        double newValue = 1.5D;
        gm.setCommerceFactor(CommerceType.BUILDING, newValue);
        assertEquals(newValue, gm.getCommerceFactor(CommerceType.BUILDING), "Changed commerce");

        gm.resetCommerceFactors();
        assertEquals(1D, gm.getCommerceFactor(CommerceType.BUILDING), "Reset commerce");
    }

    @Test
    public void testGetResourceReviewDue() {
        // Build a settlement with some people to generate demand
        var s = buildSettlement("mock");
        buildPerson("P1", s);
        buildPerson("P2", s);

        var gm = new GoodsManager(s);

        var ess = getConfig().getSettlementConfiguration().getEssentialResources();

        int reviewDue = gm.getResourceReviewDue();
        assertEquals(ess.keySet().size(), reviewDue, "Essential resources needing review at start");

        int reserved = gm.reserveResourceReview();

        // Add a resource with 1 kg
        s.storeAmountResource(reserved, 1D);
        reviewDue = gm.getResourceReviewDue();
        assertEquals(ess.keySet().size()-1, reviewDue, "Essential resources needing review after reserve");

        double initialDemand = gm.getDemandScoreWithID(reserved);
        boolean passed = gm.moderateLifeResourceDemand(reserved) == 0;
        double newDemand = gm.getDemandScoreWithID(reserved);
        if (passed)
        	assertEquals(initialDemand, newDemand, "Demand remains the same after budget review");
        else
        	assertNotEquals("Demand has changed after budget review", initialDemand, newDemand);
        
    }
}
