package com.mars_sim.core.structure.goods;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.goods.GoodsUtil;
import com.mars_sim.core.resource.ResourceUtil;

public class TestGoodsUtil {


	@BeforeEach
    void setUp() {
        SimulationConfig config = SimulationConfig.loadConfig();
  
        // Don't need a full GoodsManager initialisation
        GoodsManager.initializeInstances(config, null, null, null);
    }

	@Test
    public void testGoodsListNotNull() {
        List<Good> goodsList = GoodsUtil.getGoodsList();
        assertNotNull(goodsList);
	}

	@Test
	public void testGoodsListNotZero() {
        List<Good> goodsList = GoodsUtil.getGoodsList();
		assertTrue(goodsList.size() > 0);
	}

	@Test
	public void testGoodsListContainsWater() {
		Good waterGood = GoodsUtil.getGood(ResourceUtil.WATER_ID);
		assertNotNull(waterGood, "Found water Good");
	}

	@Test
	public void testGoodsListContainsBag() {
		Good bagGood = GoodsUtil.getEquipmentGood(EquipmentType.BAG);
		assertNotNull(bagGood, "Found Bag Good");
	}

	@Test
	public void testGoodsListContainsPot() {
		Good potGood = GoodsUtil.getBinGood(BinType.POT);
		assertNotNull(potGood, "Found Pot Good");
	}
	
	@Test
	public void testGoodsListContainsExplorerRover() {
		// "Explorer Rover" is a valid vehicle type
        String typeName = "Explorer Rover";
		Good explorerRoverGood = GoodsUtil.getVehicleGood(typeName);
		assertNotNull(explorerRoverGood, "Found good vehicleType " +  typeName);
	}

	@Test
	public void testGoodsListDoesntContainFalseRover() {
		// "False Rover" is not a valid vehicle type
		Good falseRoverGood = GoodsUtil.getVehicleGood("False Rover");
		assertNull(falseRoverGood, "Non-Existent Vehicle Good not found");
	}
}