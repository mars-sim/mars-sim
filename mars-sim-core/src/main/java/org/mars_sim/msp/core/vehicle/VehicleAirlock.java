/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
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
	private static Logger logger = Logger.getLogger(VehicleAirlock.class.getName());
	private static String loggerName = logger.getName();
	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
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
		airlockInteriorPos = new Point2D.Double(interiorXLoc, interiorYLoc);

		// Determine airlock exterior position.
		airlockExteriorPos = new Point2D.Double(exteriorXLoc, exteriorYLoc);

		// Determine airlock inside position.
		airlockInsidePos = new Point2D.Double(xLoc, yLoc);
	}

	/**
	 * Causes a person within the airlock to exit either inside or outside.
	 *
	 * @param person the person to exit.
	 * @throws Exception if person is not in the airlock.
	 */
	protected boolean exitAirlock(Person person) {
    	boolean successful = false;
		// TODO: how to detect and bypass going through the airlock if a vehicle is inside a garage in a settlement
		// see exitingRoverGaragePhase() in Walk
		
		if (inAirlock(person)) {
			if (AirlockState.PRESSURIZED == getState()) {
				// check if the airlock has been sealed from outside and pressurized, ready to 
            	// open the inner door to release the person into the vehicle
				successful = stepIntoAirlock(person);
				
			}
			else if (AirlockState.DEPRESSURIZED == getState()) {
            	// check if the airlock has been de-pressurized, ready to open the outer door to 
            	// get exposed to the outside air and release the person
				successful = stepIntoMarsSurface(person);

			}
			else {
				logger.severe(Msg.getString("VehicleAirlock.error.badState", getState())); //$NON-NLS-1$
			}
		}
		else {
			throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notInAirlock",person.getName(),getEntityName())); //$NON-NLS-1$
		}
		
		return successful;
	}

	   /**
     * Steps back into an airlock of a settlement
     * 
     * @param person
     */
    public boolean stepIntoAirlock(Person person) {
    	boolean successful = false;
    	if (person.isOutside()) {						
            // 1.1. Transfer a person from the surface of Mars to the vehicle
    		successful = person.transfer(marsSurface, vehicle);
        
			if (successful)
				LogConsolidated.log(Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " had just stepped inside rover " + vehicle.getName());
			else
				LogConsolidated.log(Level.SEVERE, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] "
						+ person.getName() + " could not step inside rover " + vehicle.getName());

		}
		else if (person.isInSettlement()) {
			LogConsolidated.log(Level.SEVERE, 0, sourceName, 
					Msg.getString("VehicleAirlock.error.notOutside", person.getName(), getEntityName()));
			//throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notOutside",person.getName(),getEntityName())); //$NON-NLS-1$
		}
    	
		return successful;
    }
    
    /**
     * Gets outside of the airlock and step into the surface of Mars
     * 
     * @param person
     */
    public boolean stepIntoMarsSurface(Person person) {
    	boolean successful = false;
		if (person.isInVehicle()) {

            // 5.1. Transfer a person from the vehicle to the surface of Mars
			successful = person.transfer(vehicle, marsSurface);
			
			if (successful) {
				// 5.2 Set the person's coordinates to that of the settlement's
				person.setCoordinates(vehicle.getCoordinates());
						
				LogConsolidated.log(Level.FINER, 0, sourceName, 
					"[" + person.getLocationTag().getLocale() + "] "
					+ person.getName() + " had just stepped outside rover " + vehicle.getName());
			}
			else
				LogConsolidated.log(Level.SEVERE, 0, sourceName, 
						"[" + person.getLocationTag().getLocale() + "] "
						+ person.getName() + " could not step outside rover " + vehicle.getName());
		}
		else if (person.isOutside()) {
			LogConsolidated.log(Level.SEVERE, 0, sourceName, 
					Msg.getString("VehicleAirlock.error.notInside", person.getName(), getEntityName()));
			//throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notInside",person.getName(),getEntityName())); //$NON-NLS-1$
		}
		
		return successful;
    }
    
    
	/**
	 * Gets the name of the entity this airlock is attached to.
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
		return vehicle.getInventory();
	}

	@Override
	public Object getEntity() {
		return vehicle;
	}

	@Override
	public Point2D getAvailableInteriorPosition() {
		return LocalAreaUtil.getLocalRelativeLocation(airlockInteriorPos.getX(),airlockInteriorPos.getY(),vehicle);
	}

	@Override
	public Point2D getAvailableExteriorPosition() {
		return LocalAreaUtil.getLocalRelativeLocation(airlockExteriorPos.getX(),airlockExteriorPos.getY(),vehicle);
	}

	@Override
	public Point2D getAvailableAirlockPosition() {
		return LocalAreaUtil.getLocalRelativeLocation(airlockInsidePos.getX(),airlockInsidePos.getY(),vehicle);
	}
	
	public void destroy() {
	    vehicle = null; 
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	}
}