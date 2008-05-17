/**
 * Mars Simulation Project
 * Medical.java
 * @version 2.84 2008-05-1
 * @author Sebastien Venot
 */
package org.mars_sim.msp.simulation.vehicle;

import java.util.Collection;

import org.mars_sim.msp.simulation.CollectionUtils;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.structure.Settlement;


public class LightUtilityVehicle extends GroundVehicle implements Crewable{
    
    private int crewCapacity = 0; // The LightUtilityVehicle's capacity for crewmembers.
    private Collection<Part> attachments = null;
    private int slotNumber  = 0;
    
    public LightUtilityVehicle(String name, String description, Settlement settlement)
    throws Exception{
	super(name,description,settlement);
	
	//Get vehicle configuration.
	VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
	
	//Add scope to malfunction manager.
	malfunctionManager.addScopeString("LightUtilityVehicle");
	malfunctionManager.addScopeString("Crewable");
	malfunctionManager.addScopeString(description);
	if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
	if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");
	
	if( config.hasPartAttachments(description)) {
	    attachments = config.getAttachableParts(description);
	    slotNumber = config.getPartAttachmentSlotNumber(description);
	}
	
	crewCapacity = config.getCrewSize(description);
	
	Inventory inv = getInventory();
	inv.addGeneralCapacity(config.getTotalCapacity(description));
	
	
	//Set rover terrain modifier
	setTerrainHandlingCapability(0D);
	
    }

    /* 
     * 
     */
    @Override
    public AmountResource getFuelType() {
	// TODO Auto-generated method stub
	return null;
    }

    /* 
     * 
     */
    @Override
    public boolean isAppropriateOperator(VehicleOperator operator) {
	if ((operator instanceof Person) && (getInventory().containsUnit((Unit) operator))) return true;
    	else return false;
    }

    /* 
     * 
     */
    public Collection<Person> getCrew() {
	  return CollectionUtils.getPerson(getInventory().getContainedUnits());
    }

    /* 
     * 
     */
    public int getCrewCapacity() {
	return crewCapacity;
    }

    /* 
     * 
     */
    public int getCrewNum() {
	return getCrew().size();
    }

    /* 
     * 
     */
    public boolean isCrewmember(Person person) {
	return getInventory().containsUnit(person);
    }

    public Collection<Part> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<Part> attachments) {
        this.attachments = attachments;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    
    

}
