package com.mars_sim.core.goods;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.equipment.BinType;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.vehicle.VehicleType;

class TestGoodsUtil {


	@BeforeEach
    void setUp() {
        SimulationConfig.loadConfig();
  
        // Don't need a full GoodsManager initialisation
        GoodsManager.initializeInstances(null, null, null);
    }

	@Test
    void testGoodsListNotNull() {
        List<Good> goodsList = GoodsUtil.getGoodsList();
        assertNotNull(goodsList);
	}

	@Test
	void testGoodsListNotZero() {
        List<Good> goodsList = GoodsUtil.getGoodsList();
		assertTrue(goodsList.size() > 0);
	}

	@Test
	void testGoodsListContainsWater() {
		Good waterGood = GoodsUtil.getGood(ResourceUtil.WATER_ID);
		assertNotNull(waterGood, "Found water Good");
	}

	@Test
	void testGoodsListContainsBag() {
		Good bagGood = GoodsUtil.getEquipmentGood(EquipmentType.BAG);
		assertNotNull(bagGood, "Found Bag Good");
	}

	@Test
	void testGoodsListContainsPot() {
		Good potGood = GoodsUtil.getBinGood(BinType.POT);
		assertNotNull(potGood, "Found Pot Good");
	}
	
	@Test
	void testGoodsListContainsExplorerRover() {
		// "Explorer Rover" is a valid vehicle type
        String typeName = "Explorer Rover";
		Good explorerRoverGood = getVehicleGood(typeName);
		assertNotNull(explorerRoverGood, "Found good vehicleType " +  typeName);
	}

	@Test
	void testGoodsListDoesntContainFalseRover() {
		// "False Rover" is not a valid vehicle type
		Good falseRoverGood = getVehicleGood("False Rover");
		assertNull(falseRoverGood, "Non-Existent Vehicle Good not found");
	}

	    
    /**
     * Gets a good object for the given vehicle type.
     *
     * @param vehicleType the vehicle type string.
     * @return good for the vehicle type.
     */
    private static Good getVehicleGood(String vehicleType) {
        int id = VehicleType.convertName2ID(vehicleType);
        return GoodsUtil.getGoodsMap().get(id);
    }

}