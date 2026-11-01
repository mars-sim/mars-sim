package com.mars_sim.core.goods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mars_sim.core.TestEntityListener;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.test.MarsSimUnitTest;

class GoodsManagerTest extends MarsSimUnitTest {
	
    @Test
    void testResetCommerceFactor() {
            var s = buildSettlement("mock", true);
        var gm = new GoodsManager(s);

        double newValue = 1.5D;
        gm.setCommerceFactor(CommerceType.BUILDING, newValue);
        assertEquals(newValue, gm.getCommerceFactor(CommerceType.BUILDING), "Changed commerce");

        gm.resetCommerceFactors();
        assertEquals(1D, gm.getCommerceFactor(CommerceType.BUILDING), "Reset commerce");
    }
    
    @Test
    void testPartGoodDemand() {
        Part sheet = (Part) ItemResourceUtil.findItemResource("Steel sheet");

        int previousNum = 1;
       
        int needNum = 2;
        var s = buildSettlement("mock", true);
        var gm = new GoodsManager(s);

        s.getEquipmentInventory().storeItemResource(sheet.getID(), previousNum);
        
        PartGood pg = new PartGood(sheet);
        
        double previousDemand = 10;

        double constructionDemand = pg.computeNewConstructionDemand(previousNum, needNum, 5, previousDemand);
		
		double maintDemand = pg.getMaintenancePartsDemand(previousNum, s, sheet, previousDemand); // newDemand is 1xxx

		double newDemand = maintDemand + constructionDemand - previousDemand;
	
        assertTrue(newDemand > previousDemand); // Demand has increased;

        double injectedDemand = pg.calculateInjectPartDemand(sheet, gm, needNum, 5); 

        int storedNum = s.getEquipmentInventory().getItemResourceStored(sheet.getID());
 
        assertEquals(previousNum, storedNum, "Stored number matches previous number");

        assertEquals(newDemand, injectedDemand, "Injected demand matches new demand");
        
        gm.setDemandScore(pg, injectedDemand);
        
        double submittedDemand = gm.getDemandScore(pg);
  
        assertEquals(newDemand, submittedDemand, "Submitted demand score matches new demand");
    }
    
    @Test
    void testGetResourceReviewDue() {
        var s = buildSettlement("mock", true);
        var gm = new GoodsManager(s);

        // Build a settlement with some people to generate demand
        buildPerson("P1", s);
        buildPerson("P2", s);

        var ess = getConfig().getSettlementConfiguration().getEssentialResources();

        int reviewDue = gm.getResourceReviewDue();
        assertEquals(ess.keySet().size(), reviewDue, "Essential resources needing review at start");

        int reserved = gm.selectResourceForReview();

        // Add a resource with 1 kg
        s.getEquipmentInventory().storeAmountResource(reserved, 1D);
        reviewDue = gm.getResourceReviewDue();
        assertEquals(ess.keySet().size()-1, reviewDue, "Essential resources needing review after reserve");

        double initialDemand = gm.getDemandScoreWithID(reserved);
        boolean needDemandInjection = gm.moderateLifeResourceDemand(reserved) == 0D;
        double newDemand = gm.getDemandScoreWithID(reserved);
         if (!needDemandInjection)
        	assertEquals(initialDemand, newDemand, "Demand remains the same after budget review");
        else
        	assertNotEquals(initialDemand, newDemand, "Demand has changed after budget review");
        
    }

    @Test 
    void testGoodsUpdate() {
        var s = buildSettlement("mock", true);

        var listener = new TestEntityListener();
        s.addEntityListener(listener);

        var gm = new GoodsManager(s);

        // Perform some update operations on goods manager
        gm.updatedMetrics();

        assertTrue(listener.getEventsReceived() > 0, "Listener should have been notified after goods update");

    }
}
