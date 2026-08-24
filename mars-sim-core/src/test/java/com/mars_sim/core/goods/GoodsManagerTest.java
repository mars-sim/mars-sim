package com.mars_sim.core.goods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.test.MarsSimUnitTest;

class GoodsManagerTest extends MarsSimUnitTest {
	
    private Settlement s = null;
    private GoodsManager gm = null;
    
	@BeforeEach
    void setUp() {
        s = buildSettlement("mock");
        gm = new GoodsManager(s);
	}
	
    @Test
    public void testResetCommerceFactor() {

        double newValue = 1.5D;
        gm.setCommerceFactor(CommerceType.BUILDING, newValue);
        assertEquals(newValue, gm.getCommerceFactor(CommerceType.BUILDING), "Changed commerce");

        gm.resetCommerceFactors();
        assertEquals(1D, gm.getCommerceFactor(CommerceType.BUILDING), "Reset commerce");
    }
    
    @Test
    public void testPartGoodDemand() {
        Part sheet = (Part) ItemResourceUtil.findItemResource("Steel sheet");

        int previousNum = 1;
       
        int needNum = 2;
        
        s.getEquipmentInventory().storeItemResource(sheet.getID(), previousNum);
        
        PartGood pg = new PartGood(sheet);
        
        double previousDemand = 10;

        double constructionDemand = pg.computeNewDemand(previousNum, needNum, 5, previousDemand);
		
		double maintDemand = pg.getMaintenancePartsDemand(previousNum, s, sheet, previousDemand); // newDemand is 1xxx

		double newDemand = maintDemand + constructionDemand;
		 
        assertTrue(newDemand > previousDemand); // Demand has increased;

        double tempDemand = pg.injectPartDemand(sheet, gm, needNum, 5); 
        
        int storedNum = s.getEquipmentInventory().getItemResourceStored(sheet.getID());
        
        double demandScore = gm.getDemandScore(pg);
        
        assertEquals(previousNum, storedNum, "Stored number matches previous number");
        
        assertEquals(newDemand, demandScore, "Demand score matches new demand");
    }
    
    @Test
    public void testGetResourceReviewDue() {
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
}
