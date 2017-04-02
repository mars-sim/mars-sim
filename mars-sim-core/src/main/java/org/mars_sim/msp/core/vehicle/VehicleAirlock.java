/**
 * Mars Simulation Project
 * VehicleAirlock.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;

/**
 * This class represents an airlock for a vehicle.
 */
public class VehicleAirlock
extends Airlock {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(VehicleAirlock.class.getName());

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
		else if (!(vehicle instanceof LifeSupportType)) {
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
	protected void exitAirlock(Person person) {

		if (inAirlock(person)) {
			if (PRESSURIZED.equals(getState())) {
				if (LocationSituation.OUTSIDE == person.getLocationSituation()) {
					// Exit person to inside vehicle.
					vehicle.getInventory().storeUnit(person);
				}
				else if (LocationSituation.BURIED != person.getLocationSituation()) {
					throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notOutside",person.getName(),getEntityName())); //$NON-NLS-1$
				}
			}
			else if (DEPRESSURIZED.equals(getState())) {
				if (LocationSituation.IN_VEHICLE == person.getLocationSituation()) {
					// Exit person outside vehicle.
					vehicle.getInventory().retrieveUnit(person);
				}
				else if (LocationSituation.BURIED != person.getLocationSituation()) {
					throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notInside",person.getName(),getEntityName())); //$NON-NLS-1$
				}
			}
			else {
				logger.severe(Msg.getString("VehicleAirlock.error.badState",getState())); //$NON-NLS-1$
			}
		}
		else {
			throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notInAirlock",person.getName(),getEntityName())); //$NON-NLS-1$
		}
	}
	protected void exitAirlock(Robot robot) {

		if (inAirlock(robot)) {
			if (PRESSURIZED.equals(getState())) {
				if (LocationSituation.OUTSIDE == robot.getLocationSituation()) {
					// Exit robot to inside vehicle.
					vehicle.getInventory().storeUnit(robot);
				}
				else if (LocationSituation.BURIED != robot.getLocationSituation()) {
					throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notOutside",robot.getName(),getEntityName())); //$NON-NLS-1$
				}
			}
			else if (DEPRESSURIZED.equals(getState())) {
				if (LocationSituation.IN_VEHICLE == robot.getLocationSituation()) {
					// Exit robot outside vehicle.
					vehicle.getInventory().retrieveUnit(robot);
				}
				else if (LocationSituation.BURIED != robot.getLocationSituation()) {
					throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notInside",robot.getName(),getEntityName())); //$NON-NLS-1$
				}
			}
			else {
				logger.severe(Msg.getString("VehicleAirlock.error.badState",getState())); //$NON-NLS-1$
			}
		}
		else {
			throw new IllegalStateException(Msg.getString("VehicleAirlock.error.notInAirlock",robot.getName(),getEntityName())); //$NON-NLS-1$
		}
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

	@Override
	protected void exitAirlock(Unit occupant) {

        Person person = null;
        Robot robot = null;

        if (occupant instanceof Person) {
         	person = (Person) occupant;
         	exitAirlock(person);

        }
        else if (occupant instanceof Robot) {
        	robot = (Robot) occupant;
        	exitAirlock(robot);

        }

	}
}