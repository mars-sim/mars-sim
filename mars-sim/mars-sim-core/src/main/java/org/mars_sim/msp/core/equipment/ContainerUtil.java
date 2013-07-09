/**
 * Mars Simulation Project
 * ContainerUtil.java
 * @version 3.05 2013-07-01
 * @author Scott Davis
 */
package org.mars_sim.msp.core.equipment;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Phase;

/**
 * A utility class for containers.
 */
public final class ContainerUtil {

    private static final Coordinates coordinates = new Coordinates(0D, 0D);
    
    /**
     * Private constructor for utility class.
     */
    private ContainerUtil() {};
    
    /**
     * Gets the type of container needed to hold a particular resource.
     * @param resource the amount resource to hold.
     * @return type of container or null if none found.
     */
    public static Class<? extends Container> getContainerClassToHoldResource(
            AmountResource resource) {
        
        Class<? extends Container> result = null;
        
        Phase phase = resource.getPhase();
        
        if (Phase.GAS.equals(phase)) {
            result = GasCanister.class;
        }
        else if (Phase.LIQUID.equals(phase)) {
            result = Barrel.class;
        }
        else if (Phase.SOLID.equals(phase)) {
            result = Bag.class;
        }
        
        return result;
    }
    
    /**
     * Gets the capacity of the container.
     * @param containerClass the container class.
     * @return capacity (kg).
     */
    public static double getContainerCapacity(Class<? extends Container> containerClass) {
        
        double result = 0D;
        
        Class<? extends Equipment> equipmentClass = (Class<? extends Equipment>) containerClass;
        
        Container container = (Container) EquipmentFactory.getEquipment(equipmentClass, coordinates, true);
        if (container != null) {
            result = container.getTotalCapacity();
        }
        
        return result;
    }
}