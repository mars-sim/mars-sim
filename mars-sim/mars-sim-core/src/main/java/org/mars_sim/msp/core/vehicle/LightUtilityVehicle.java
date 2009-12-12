/**
 * Mars Simulation Project
 * Medical.java
 * @version 2.84 2008-06-04
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.vehicle;

import java.util.Collection;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A light utility vehicle that can be used for construction, loading and mining.
 */
public class LightUtilityVehicle extends GroundVehicle implements Crewable {
    
	// Vehicle name.
	public static final String NAME = "Light Utility Vehicle";
	
	// Data members.
    private int crewCapacity = 0; // The LightUtilityVehicle's capacity for crewmembers.
    private Collection<Part> attachments = null;
    private int slotNumber  = 0;
    
    public LightUtilityVehicle(String name, String description, Settlement settlement)
    		throws Exception {
    	// Use GroundVehicle constructor.
    	super(name, description, settlement);
	
    	// Get vehicle configuration.
    	VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
	
    	// Add scope to malfunction manager.
    	malfunctionManager.addScopeString("Crewable");
    	malfunctionManager.addScopeString(description);
    	if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
    	if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");
	
    	if (config.hasPartAttachments(description)) {
    		attachments = config.getAttachableParts(description);
    		slotNumber = config.getPartAttachmentSlotNumber(description);
    	}
	
    	crewCapacity = config.getCrewSize(description);
	
    	Inventory inv = getInventory();
    	inv.addGeneralCapacity(config.getTotalCapacity(description));
	
    	// Set rover terrain modifier
    	setTerrainHandlingCapability(0D);
	}

    @Override
    public AmountResource getFuelType() {
    	return null;
    }

    @Override
    public boolean isAppropriateOperator(VehicleOperator operator) {
    	if ((operator instanceof Person) && (getInventory().containsUnit((Unit) operator))) 
    		return true;
    	else return false;
    }

    /**
     * Gets a collection of the crewmembers.
     * @return crewmembers as Collection
     */
    public Collection<Person> getCrew() {
    	return CollectionUtils.getPerson(getInventory().getContainedUnits());
    }

    /**
     * Gets the number of crewmembers the vehicle can carry.
     * @return capacity
     */
    public int getCrewCapacity() {
    	return crewCapacity;
    }

    /**
     * Gets the current number of crewmembers.
     * @return number of crewmembers
     */
    public int getCrewNum() {
    	return getCrew().size();
    }

    /**
     * Checks if person is a crewmember.
     * @param person the person to check
     * @return true if person is a crewmember
     */
    public boolean isCrewmember(Person person) {
    	return getInventory().containsUnit(person);
    }

    /**
     * Gets a collection of parts that can be attached to this vehicle.
     * @return collection of parts.
     */
    public Collection<Part> getPossibleAttachmentParts() {
        return attachments;
    }

    /**
     * Gets the number of part slots in the vehicle.
     * @return number of part slots.
     */
    public int getAtachmentSlotNumber() {
        return slotNumber;
    }
    
    @Override
    public void timePassing(double time) throws Exception {
    	super.timePassing(time);
    	
    	// Add active time if crewed.
    	if (getCrewNum() > 0) malfunctionManager.activeTimePassing(time);
    }
}