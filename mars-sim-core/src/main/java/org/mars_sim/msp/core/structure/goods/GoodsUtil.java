/**
 * Mars Simulation Project
 * GoodsUtil.java
 *
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.goods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.EquipmentFactory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Resource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * Utility class for goods information.
 */
public class GoodsUtil {

    /** default logger. */
    private static final SimLogger logger = SimLogger.getLogger(GoodsUtil.class.getName());

    private static final String HEAVY = "Vehicle Heavy";
    private static final String MID = "Vehicle Mid";
    private static final String SMALL = "Vehicle Small";

    
    private static final String ATTACHMENT = "Attachment";
    
    public static final String CHEMICAL = "Chemical";

    public static final String ELEMENT = "Element";
    
    public static final String COMPOUND = "Compound";
    
    public static final String VEHICLE_PART = "Vehicle Part";

    public static final String METALLIC = "Metallic";

    public static final String UTILITY = "Utility";

    public static final String INSTRUMENT = "Instrument";
    
    public static final String RAW = "Raw";

    public static final String ELECTRICAL = "Electrical";

    public static final String KITCHEN = "Kitchen";

    public static final String CONSTRUCTION = "Construction";

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
     * Calculates the cost of each good
     */
    private static void calculateGoodCost() {
        for (Good g : goodsList) {
            g.computeCost();
        }
    }


    private static Good createResourceGood(Resource resource) {
        if (resource == null) {
            logger.severe("resource is NOT supposed to be null.");
        }
        GoodCategory category = null;
        if (resource instanceof AmountResource)
            category = GoodCategory.AMOUNT_RESOURCE;
        else if (resource instanceof ItemResource)
            category = GoodCategory.ITEM_RESOURCE;
        return new Good(resource.getName(), resource.getID(), category);
    }

    /**
     * Gets a good object for a given resource.
     *
     * @param resource the resource.
     * @return good for the resource.
     */
    public static Good getResourceGood(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource is NOT supposed to be null.");
        }

        return getResourceGood(resource.getID());
    }

    /**
     * Gets a good object for a given resource.
     *
     * @param id the resource id.
     * @return good for the resource.
     */
    public static Good getResourceGood(int id) {
        return getGoodsMap().get(id);
    }

    private static Good createEquipmentGood(int id) {
        if (EquipmentType.convertID2Type(id) == EquipmentType.EVA_SUIT)
            return new Good(EquipmentType.convertID2Type(id).getName(), id, GoodCategory.EQUIPMENT);
        else
            return new Good(EquipmentType.convertID2Type(id).getName(), id, GoodCategory.CONTAINER);
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
            return getEquipmentGood(id);
        }

        return null;
    }


    /**
     * Gets a good object for a given equipment id
     *
     * @param id
     * @return
     */
    public static Good getEquipmentGood(int id) {
        return getGoodsMap().get(id);
    }

 
    public static String getVehicleCategory(VehicleType vehicleType) {
        if (vehicleType == VehicleType.CARGO_ROVER || vehicleType == VehicleType.TRANSPORT_ROVER)
            return HEAVY;
        else if (vehicleType == VehicleType.EXPLORER_ROVER)
            return MID;
        else if (vehicleType == VehicleType.LUV || vehicleType == VehicleType.DELIVERY_DRONE)
            return SMALL;
        return "";
    }

    private static Good createVehicleGood(int id) {
        return new Good(VehicleType.convertID2Type(id).getName(), id, GoodCategory.VEHICLE);
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

        goodsMap = newMap;
    }


    /**
     * Populates the goods list with all amount resources.
     * @param newMap
     * @param newList
     */
    private static Map<Integer, Good> populateAmountResources(Map<Integer, Good> newMap) {
        Iterator<AmountResource> i = ResourceUtil.getAmountResources().iterator();
        while (i.hasNext()) {
            AmountResource ar = i.next();
            newMap.put(ar.getID(), createResourceGood(ar));
        }
        return newMap;
    }

    /**
     * Populates the goods list with all item resources.
     */
    private static Map<Integer, Good> populateItemResources(Map<Integer, Good> newMap) {
        Iterator<Part> i = ItemResourceUtil.getItemResources().iterator();
        while (i.hasNext()) {
            Part p = i.next();
            newMap.put(p.getID(), createResourceGood(p));
        }
        return newMap;
    }

    /**
     * Populates the goods list with all equipment.
     * @param newMap
     * @param newList
     */
    private static Map<Integer, Good> populateEquipment(Map<Integer, Good> newMap) {
        List<String> equipmentNames = new ArrayList<>(EquipmentFactory.getEquipmentNames());
        Iterator<String> i = equipmentNames.iterator();
        while (i.hasNext()) {
            String name = i.next();
            int id = EquipmentType.convertName2ID(name);
            newMap.put(id, createEquipmentGood(id));
        }
        return newMap;
    }

    /**
     * Populates the goods list with all vehicles.
     * @param newMap
     * @param newList
     */
    private static Map<Integer, Good> populateVehicles(Map<Integer, Good> newMap) {
        Iterator<String> i = vehicleConfig.getVehicleTypes().iterator();
        while (i.hasNext()) {
            String name = i.next();
            int id = VehicleType.convertName2ID(name);
            newMap.put(id, createVehicleGood(id));
        }
        return newMap;
    }

    /**
     * Gets the mass per item for a good.
     *
     * @param good the good to check.
     * @return mass (kg) per item (or 1kg for amount resources).
     * @throws Exception if error getting mass per item.
     */
    public static double getGoodMassPerItem(Good good) {
        double result = 0D;

        if (GoodCategory.AMOUNT_RESOURCE == good.getCategory())
            result = 1D;
        else if (GoodCategory.ITEM_RESOURCE == good.getCategory())
            result = ItemResourceUtil.findItemResource(good.getID()).getMassPerItem();
        else if (GoodCategory.EQUIPMENT == good.getCategory()
                || GoodCategory.CONTAINER == good.getCategory())
            result = EquipmentFactory.getEquipmentMass(good.getEquipmentType());
        else if (GoodCategory.VEHICLE == good.getCategory()) {
            result = vehicleConfig.getEmptyMass(good.getName());
        }

        return result;
    }

    /**
     * Gets the good category name in the internationalized string
     * @param good
     * @return
     */
    public static String getGoodType(Good good) {

        GoodCategory cat = good.getCategory();

        if (cat == GoodCategory.AMOUNT_RESOURCE) {
            AmountResource ar = ResourceUtil.findAmountResource(good.getID());
            String type = ar.getType();
            if (type != null)
                return type;
            else
                return "";
        } else if (cat == GoodCategory.ITEM_RESOURCE) {
            String name = good.getName().toLowerCase();

            if (name.contains("eva ")
                    || name.equalsIgnoreCase("helmet visor")
                    || name.contains("suit")
                    || name.equalsIgnoreCase("coveralls"))
                return EVASuit.GOODTYPE;

            else if (name.contains("rover ")
                    || name.contains("wheel")
                    || name.contains("chassle")
                    || name.contains("vehicle frame")
                    || name.contains("utility vehicle")
            		)
                return VEHICLE_PART;

            else if (vehicleConfig.getAttachmentNames().contains(name))
                return ATTACHMENT;

            else if (name.contains("brick")
            		|| name.contains("fiberglass")
            		|| name.contains("sheet")
            		|| name.contains("roofing")
               		|| name.contains("truss")
            		) 
            	return CONSTRUCTION;
            
            else if (name.contains("pump")
                    || name.contains("tank")
                    || name.contains("gasket")
                    || name.contains("tube")
                    || name.contains("heat pipe")
                    || name.contains("plastic")
                    || name.contains("air")
                    || name.contains("glove")
                    || name.contains("decontamination")
                    || name.contains("heating")
                    || name.contains("bottle")
                    || name.contains("containment")
                    || name.contains("hose")
                    || name.contains("belt")
                    || name.contains("valve")
                    || name.contains("filter")
                    || name.contains("hammer")
                    || name.contains("wrench")
                    || name.contains("extinguisher")
                    || name.contains("winch")
            		)

                return UTILITY;

            else if (name.contains("wire")
                    || name.contains("copper")
                    || name.contains("aluminum")
                    || name.contains("steel")
                    || name.contains("iron")
                    || name.contains("ingot")
                    )
            	
                return METALLIC;

            else if (name.contains("resistor")
                    || name.contains("capacitor")
                    || name.contains("coil")
                    || name.contains("fuel cell")
                    || name.contains("diode")
                    || name.contains("motor")
                    || name.contains("lamp")
                    || name.contains("light")
                    || name.contains("transistor")
                    || name.contains("cable")
                    || name.contains("antenna")
                    || name.contains("satellite dish")
                    || name.contains("transformer")
                    || name.contains("solar")
                    || name.contains("spark")
                    || name.contains("generator")

            		)
                return ELECTRICAL;

            else if (name.contains("stove")
                    || name.contains("autoclave")
                    || name.contains("microwave")
                    || name.contains("refrigerator")
                    || name.contains("blender")
                    || name.contains("oven")
                    || name.contains("fan")
            		)
                return KITCHEN;

            else if (name.contains("printer")
                    || name.contains("laser")
                    || name.contains("circuit")
                    || name.contains("bore drill")
                    || name.contains("optical")
                    || name.contains("logic board")
                    || name.contains("microcontroller")
                    || name.contains("lens")
                    || name.contains("purification")
                    || name.contains("chromatograph")
                    || name.contains("spectrometer")
                    || name.contains("camera")
                    || name.contains("blade")
                    || name.contains("probe")
                    || name.contains("precipitator")
            		)
                return INSTRUMENT;

            else if (name.contains("wafer")
            		)
                return RAW;
            
            return Conversion.capitalize(cat.getMsgKey());
        } else if (cat == GoodCategory.CONTAINER) {
            return Conversion.capitalize(cat.getMsgKey());
        } else if (cat == GoodCategory.EQUIPMENT) {
            return EVASuit.GOODTYPE;
        } else if (cat == GoodCategory.VEHICLE) {
            return GoodsUtil.getVehicleCategory(VehicleType.convertNameToVehicleType(good.getName()));
        }

        return null;
    }

    public static int getGoodID(String name) {
        List<Good> list = getGoodsList();
        Good good = null;
        for (Good g : list) {
            if (g.getName().equalsIgnoreCase(name)) {
                good = g;
                break;
            }
        }

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
