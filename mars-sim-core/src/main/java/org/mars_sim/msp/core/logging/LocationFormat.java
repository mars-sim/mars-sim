/*
 * Mars Simulation Project
 * LocationFormat.java
 * @date 2021-10-07
 * @author Barry Evans
 */
package org.mars_sim.msp.core.logging;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This provides methods that can provide a String description of the location of
 * a Loggable entity.
 * Where possible the output will display the containment hierarchy including
 * the coordinates where appropriate.
 *
 */
public final class LocationFormat {

	/**
	 * Gets a description of the current location of an entity.
	 * 
	 * @param entity Entity in question.
	 */
	public static String getLocationDescription(Loggable entity) {
		Unit location;
		if (entity instanceof Building) {
			location = entity.getAssociatedSettlement();
		}
		else {
			location = entity.getContainerUnit();
		}	
		return getLocationDescription(entity, location);
	}
	
	/**
	 * Gets the description of an Entities location in a particular content.
	 * 
	 * @param entity
	 * @param context
	 * @return
	 */
	public static String getLocationDescription(Loggable entity, Unit context) {
				
		StringBuilder buffer = new StringBuilder();
		if (context != null) {
			if (context.getUnitType() == UnitType.PLANET) {
				outputCoordinates(entity, buffer);
			}
			else {
				locationDescription(context,  buffer);
			}
		}
		else {
			buffer.append("Unknown");
		}
		return buffer.toString();
	}
	
	/**
	 * Walks up the hierarchy of containment adding the name of each Unit it
	 * visits.
	 * 
	 * @param location
	 * @param outputMessage
	 */
	private static void locationDescription(Unit location, StringBuilder outputMessage) {
		Unit next = null;
		if (location instanceof Building) {
			next = location.getAssociatedSettlement();
		}
		else {
			next = location.getContainerUnit();
		}
		
		// Go up the chain if not surface
		if (next != null) {
			if (next.getUnitType() != UnitType.PLANET) {
				locationDescription(next, outputMessage);
				outputMessage.append(" - ");
			}
			else if (location.getUnitType() != UnitType.SETTLEMENT) {
				// On the surface so show coords if not a Settlement
				outputCoordinates(location, outputMessage);
				outputMessage.append(" - ");
			}
		}
		
		outputMessage.append(location.getNickName());
	}
	
	/**
	 * Outputs a coordinate.
	 * 
	 * @param location
	 * @param outputMessage
	 */
	private static void outputCoordinates(Loggable location, StringBuilder outputMessage) {
		Coordinates coords = location.getCoordinates();
		outputMessage.append(coords.getFormattedLatitudeString());
		outputMessage.append(' ');
		outputMessage.append(coords.getFormattedLongitudeString());
//		outputMessage.append(" - ");
//		outputMessage.append(CollectionUtils.getNearbyObjectName(coords));
	}
}
