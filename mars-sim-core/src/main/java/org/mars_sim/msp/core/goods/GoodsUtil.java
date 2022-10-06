/*
 * Mars Simulation Project
 * GoodsUtil.java
 * @date 2022-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.goods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleSpec;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for all goods.
 */
public class GoodsUtil {

    /** default logger. */
    private static final SimLogger logger = SimLogger.getLogger(GoodsUtil.class.getName());

    private static final String HEAVY = "vehicle heavy";
    private static final String MEDIUM = "vehicle medium";
    private static final String SMALL = "vehicle small";

    // Data members
    private static Map<Integer, Good> goodsMap = null;
    private static List<Good> goodsList = null;

    private static VehicleConfig vehicleConfig = SimulationConfig.instance().getVehicleConfiguration();

    /**
     * Private constructor for utility class.
     */
    private GoodsUtil() {
    }

    /**
     * Gets a list of all goods in the simulation.
     *
     * @return list of goods
     */
    public static List<Good> getGoodsList() {
        if (goodsList == null) {
            getGoodsMap();
        }
        return goodsList;
    }

    /**
     * Gets a list of all goods in the simulation.
     *
     * @return list of goods
     */
    static Map<Integer, Good> getGoodsMap() {
        if (goodsMap == null) {
            populateGoods();
            goodsList = new ArrayList<>(goodsMap.values());
            calculateGoodCost();
        }

        return goodsMap;
    }

    /**
     * Calculates the cost of each good.
     */
    private static void calculateGoodCost() {
        for (Good g : goodsList) {
            g.computeCost();
        }
    }
	
    /**
     * Gets a good object for a given equipment class.
     *
     * @param equipmentClass the equipment class.
     * @return good for the resource class or null if none.
     */
    public static Good getEquipmentGood(EquipmentType equipmentClass) {
        if (equipmentClass == null) {
            logger.severe("equipmentClass is NOT supposed to be null.");
        }
        int id = EquipmentType.getResourceID(equipmentClass);
        if (id > 0) {
            return getGood(id);
        }

        return null;
    }


    /**
     * Gets the string type for a given vehicle type.
     * 
     * @param vehicleType
     * @return
     */
    public static String getVehicleCategory(VehicleType vehicleType) {
        if (vehicleType == VehicleType.CARGO_ROVER || vehicleType == VehicleType.TRANSPORT_ROVER)
            return HEAVY;
        else if (vehicleType == VehicleType.EXPLORER_ROVER)
            return MEDIUM;
        else if (vehicleType == VehicleType.LUV || vehicleType == VehicleType.DELIVERY_DRONE)
            return SMALL;
        return "";
    }

    /**
     * Gets a good object for the given vehicle type.
     *
     * @param vehicleType the vehicle type string.
     * @return good for the vehicle type.
     */
    public static Good getVehicleGood(String vehicleType) {
        if ((vehicleType == null) || vehicleType.trim().length() == 0) {
            logger.severe("vehicleType is NOT supposed to be blank or null.");
        }

        int id = VehicleType.convertName2ID(vehicleType);
        return getGoodsMap().get(id);
    }

    /**
     * Gets a good object for the given vehicle type.
     *
     * @param vehicleType the vehicle type.
     * @return good for the vehicle type.
     */
    public static Good getVehicleGood(VehicleType vehicleType) {
        if (vehicleType == null) {
            logger.severe("vehicleType is NOT supposed to be blank or null.");
        }

        int id = VehicleType.getVehicleID(vehicleType);
        return getGoodsMap().get(id);
    }

    /**
     * Populates the goods list with all goods.
     */
    private static synchronized void populateGoods() {
        if (goodsMap != null) {
            // Another thread has created the lists
            return;
        }

        // Only updated here so don't need to be thread safe
        Map<Integer, Good> newMap = new HashMap<>();

        // Populate amount resources.
        newMap = populateAmountResources(newMap);

        // Populate item resources.
        newMap = populateItemResources(newMap);

        // Populate equipment.
        newMap = populateEquipment(newMap);

        // Populate vehicles.
        newMap = populateVehicles(newMap);

        // Populate robots.
        newMap = populateRobots(newMap);
        
        goodsMap = newMap;
    }


    /**
     * Populates the goods list with all amount resources.
     * 
     * @param newMap
     * @param newList
     */
    private static Map<Integer, Good> populateAmountResources(Map<Integer, Good> newMap) {
        for (AmountResource ar :  ResourceUtil.getAmountResources()) {
            newMap.put(ar.getID(), new AmountResourceGood(ar));
        }
        return newMap;
    }

    /**
     * Populates the goods list with all item resources.
     * 
     * @param newMap
     * @return
     */
    private static Map<Integer, Good> populateItemResources(Map<Integer, Good> newMap) {
        for(Part p : ItemResourceUtil.getItemResources()) {
            newMap.put(p.getID(), new PartGood(p));
        }
        return newMap;
    }

    /**
     * Populates the goods list with all equipment.
     * 
     * @param newMap
     * @param newList
     */
    private static Map<Integer, Good> populateEquipment(Map<Integer, Good> newMap) {
        for(EquipmentType type : EquipmentType.values()) {
            Good newGood = new EquipmentGood(type);
            newMap.put(newGood.getID(), newGood);
    
        }
        return newMap;
    }

    /**
     * Populates the goods list with all vehicles.
     * 
     * @param newMap
     * @param newList
     */
    private static Map<Integer, Good> populateVehicles(Map<Integer, Good> newMap) {
        for(String name : vehicleConfig.getVehicleTypes()) {
            VehicleSpec vs = vehicleConfig.getVehicleSpec(name);
            Good newGood = new VehicleGood(name, vs);
            newMap.put(newGood.getID(), newGood);
        }
        return newMap;
    }
    
    /**
     * Populates the goods list with all robots.
     * 
     * @param newMap
     * @return
     */
    private static Map<Integer, Good> populateRobots(Map<Integer, Good> newMap) {
    	 for (RobotType type : RobotType.values()) {
             Good newGood = new RobotGood(type);
             newMap.put(newGood.getID(), newGood);
         }
         return newMap;
    }

    /**
     * Gets a good object for a given resource id.
     *
     * @param id the resource id.
     * @return good for the resource.
     */
    public static Good getGood(int id) {
        return getGoodsMap().get(id);
    }

    /**
     * Gets the good id.
     * 
     * @param name
     * @return
     */
    public static Good getGood(String name) {
        List<Good> list = getGoodsList();
        for (Good g : list) {
            if (g.getName().equalsIgnoreCase(name)) {
                return g;
            }
        }

        return null;
    }
    
    /**
     * Gets the good id.
     * 
     * @param name
     * @return
     */
    public static int getGoodID(String name) {
        Good good = getGood(name);
        if (good != null)
            return good.getID();

        return -1;
    }

    /**
     * Destroys the current goods list and maps.
     */
    public static void destroyGoods() {
        if (goodsMap != null) {
            goodsMap.clear();
        }

        goodsMap = null;
    }
}
