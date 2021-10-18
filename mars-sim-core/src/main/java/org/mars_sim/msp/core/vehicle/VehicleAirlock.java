/*
 * Mars Simulation Project
 * VehicleAirlock.java
 * @date 2021-09-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;

/**
 * This class represents an airlock for a vehicle.
 */
public class VehicleAirlock
extends Airlock {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(VehicleAirlock.class.getName());
		
	// Data members.
	/** The vehicle this airlock is for. */
	private Vehicle vehicle;
	private Point2D airlockInsidePos;
	private Point2D airlockInteriorPos;
	private Point2D airlockExteriorPos;

	/**
	 * Constructor.
	 * @param vehicle the vehicle this airlock of for.
	 * @param capacity number of people airlock can hold.
	 */
	public VehicleAirlock(
		Vehicle vehicle, int capacity, double xLoc, double yLoc,
		double interiorXLoc, double interiorYLoc, double exteriorXLoc,
		double exteriorYLoc
	) {
		// User Airlock constructor
		super(capacity);

		if (vehicle == null) {
			throw new IllegalArgumentException(Msg.getString("VehicleAirlock.error.null")); //$NON-NLS-1$
		}
		else if (!(vehicle instanceof Crewable)) {
			throw new IllegalArgumentException(Msg.getString("VehicleAirlock.error.notCrewable")); //$NON-NLS-1$
		}
		else if (!(vehicle instanceof LifeSupportInterface)) {
			throw new IllegalArgumentException(Msg.getString("VehicleAirlock.error.noLifeSupport")); //$NON-NLS-1$
		}
		else {
			this.vehicle = vehicle;
		}

		// Determine airlock interior position.
		airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, vehicle);

		// Determine airlock exterior position.
		airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, vehicle);

		// Determine airlock inside position.
		airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(xLoc, yLoc, vehicle);

	}

    @Override
    protected boolean egress(Person person) {
        return stepOnMars(person);
    }

    @Override
    protected boolean ingress(Person person) {
        return stepInside(person);
    }
    
//   @Override
//    protected boolean egress(Person person) {
//    	boolean successful = false;
//
//        if (inAirlock(person)) {
//            // check if the airlock has been de-pressurized, ready to open the outer door to 
//            // get exposed to the outside air and release the person
//            successful = stepOnMars(person);
//        }
//        else {
//            throw new IllegalStateException(person.getName() + " not in " + getEntityName() + ".");
//        }
//        
//        return successful;
//    }
//
//    @Override
//    protected boolean ingress(Person person) {
//    	boolean successful = false;
//  	
//        if (inAirlock(person)) {
//            // check if the airlock has been sealed from outside and pressurized, ready to 
//            // open the inner door to release the person into the settlement
//            successful = stepInside(person);
//        }
//
//        else {
//            throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName() + ".");
//        }
//        
//        return successful;
//    }
	    
	   /**
     * Steps back into the airlock of a vehicle
     * 
     * @param person
     */
    public boolean stepInside(Person person) {
    	boolean successful = false;
    	if (person.isOutside()) {						
            // 1.1. Transfer a person from the surface of Mars to the vehicle
    		successful = person.transfer(marsSurface, vehicle);
        
			if (successful)
				logger.log(person, Level.FINER, 0,
					"Just stepped inside rover " + vehicle.getName() + ".");
			else
				logger.log(person, Level.SEVERE, 0,
					"Could not step inside rover " + vehicle.getName() + ".");

		}
    	
		else if (person.isInSettlement()) {
			logger.log(person, Level.SEVERE, 0, 
					Msg.getString("VehicleAirlock.error.notOutside", person.getName(), getEntityName()) + ".");
		}
    	
		return successful;
    }
    
    /**
     * Goes outside of the airlock and step into the surface of Mars
     * 
     * @param person
     */
    public boolean stepOnMars(Person person) {
    	boolean successful = false;
		if (person.isInVehicle()) {

            // 5.1. Transfer a person from the vehicle to the surface of Mars
			successful = person.transfer(vehicle, marsSurface);
			
			if (successful) {
				// 5.2 Set the person's coordinates to that of the settlement's
				person.setCoordinates(vehicle.getCoordinates());
						
				logger.log(person, Level.FINER, 0, 
					"Just stepped outside rover " + vehicle.getName() + ".");
			}
			else
				logger.log(person, Level.SEVERE, 0,
					"Could not step outside rover " + vehicle.getName() + ".");
		}
		else if (person.isOutside()) {
			logger.log(person, Level.SEVERE, 0,
					Msg.getString("VehicleAirlock.error.notInside", person.getName(), getEntityName()) + ".");
		}
		
		return successful;
    }
	
	/**
	 * Gets the name of the entity this airlock is attached to.
	 * 
	 * @return name {@link String}
	 */
	public String getEntityName() {
		return vehicle.getName();
	}

	/**
	 * Gets the inventory of the entity this airlock is attached to.
	 * @return inventory {@link Inventory}
	 */
	@Override
	public Inventory getEntityInventory() {
		logger.warning("VehicleAirlock no longer needs Inventory instance.");
		return null;
	}

	@Override
	public Object getEntity() {
		return vehicle;
	}

	@Override
	public String getLocale() {
		return vehicle.getLocale();
	}

	@Override
	public Point2D getAvailableInteriorPosition(boolean value) {
		return getAvailableInteriorPosition();
	}

	@Override
	public Point2D getAvailableExteriorPosition(boolean value) {
		return getAvailableExteriorPosition();
	}
	
	@Override
	public Point2D getAvailableInteriorPosition() {
		return airlockInteriorPos;
	}

	@Override
	public Point2D getAvailableExteriorPosition() {
		return airlockExteriorPos;
	}

	@Override
	public Point2D getAvailableAirlockPosition() {
		return airlockInsidePos;
	}
	
	/**
	 * Gets the person having this particular id
	 * 
	 * @param id
	 * @return
	 */
	public Person getAssociatedPerson(int id) {
		return vehicle.getAssociatedSettlement().getAssociatedPerson(id);
	}
	
	public void destroy() {
	    vehicle = null; 
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	}
}
