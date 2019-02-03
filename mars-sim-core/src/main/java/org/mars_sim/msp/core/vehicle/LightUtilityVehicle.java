/**
 * Mars Simulation Project
 * Medical.java
 * @version 3.1.0 2017-10-10
 * @author Sebastien Venot
 */
package org.mars_sim.msp.core.vehicle;

import java.util.Collection;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A light utility vehicle that can be used for construction, loading and mining.
 */
public class LightUtilityVehicle extends GroundVehicle implements Crewable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Vehicle name. */
    public static final String NAME = "Light Utility Vehicle";

    /** The amount of work time to perform maintenance (millisols) */
    public static final double MAINTENANCE_WORK_TIME = 200D;

    // Data members.
    /** The LightUtilityVehicle's capacity for crewmembers. */
    private int crewCapacity = 0;
    private int robotCrewCapacity = 0;

    private Collection<Part> attachments = null;
    private int slotNumber  = 0;

    public LightUtilityVehicle(String name, String type, Settlement settlement) {
        // Use GroundVehicle constructor.
        super(name, type, settlement, MAINTENANCE_WORK_TIME);

        // Get vehicle configuration.
        VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();

        // Add scope to malfunction manager.
        //malfunctionManager.addScopeString("Crewable");
        //malfunctionManager.addScopeString(description);
        //if (config.hasLab(description)) malfunctionManager.addScopeString("Laboratory");
        //if (config.hasSickbay(description)) malfunctionManager.addScopeString("Sickbay");

        if (config.hasPartAttachments(type)) {
            attachments = config.getAttachableParts(type);
            slotNumber = config.getPartAttachmentSlotNumber(type);
        }

        crewCapacity = config.getCrewSize(type);
        robotCrewCapacity = config.getCrewSize(type);

        Inventory inv = getInventory();
        inv.addGeneralCapacity(config.getTotalCapacity(type));

        // Set rover terrain modifier
        setTerrainHandlingCapability(0D);
    }

    @Override
    public int getFuelType() {
    	return ResourceUtil.methaneID;
    }

    @Override
    public boolean isAppropriateOperator(VehicleOperator operator) {
    	boolean result = false;
    	if (operator instanceof Person)
        	result = (operator instanceof Person) && (getInventory().containsUnit((Unit) operator));
//    	else if (operator instanceof Robot)
//        	result = (operator instanceof Robot) && (getInventory().containsUnit((Unit) operator));
    	return result ;
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

	@Override
	public Collection<Robot> getRobotCrew() {
        return CollectionUtils.getRobot(getInventory().getContainedUnits());
	}

	@Override
	public int getRobotCrewCapacity() {
        return robotCrewCapacity;
	}

	@Override
	public int getRobotCrewNum() {
        return getRobotCrew().size();
	}

	@Override
	public boolean isRobotCrewmember(Robot robot) {
        return getInventory().containsUnit(robot);
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
    public void timePassing(double time) {
        super.timePassing(time);
        // Add active time if crewed.
        if (getCrewNum() > 0 || getRobotCrewNum() > 0 ) malfunctionManager.activeTimePassing(time);
    }



	@Override
	public Collection<Unit> getUnitCrew() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNickName() {
		return getName();
	}

    @Override
    public void destroy() {
        super.destroy();

        attachments.clear();
        attachments = null;
    }


	public Vehicle getVehicle() {
		if (getContainerUnit() instanceof Vehicle)
			return (Vehicle) getContainerUnit();
		return null;
	}
}