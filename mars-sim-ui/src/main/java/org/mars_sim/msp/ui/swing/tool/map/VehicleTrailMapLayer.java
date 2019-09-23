/**
 * Mars Simulation Project
 * VehicleTrailMapLayer.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The VehicleTrailMapLayer is a graphics layer to display vehicle trails.
 */
public class VehicleTrailMapLayer implements MapLayer {

	// Data members
	private Vehicle singleVehicle;

	/**
	 * Sets the single vehicle trail to display. Set to null if display all vehicle
	 * trails.
	 * 
	 * @param singleVehicle the vehicle to display trail.
	 */
	public void setSingleVehicle(Vehicle singleVehicle) {
		this.singleVehicle = singleVehicle;
	}

	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {

		// Set trail color
		if (TopoMarsMap.TYPE.equals(mapType))
			g.setColor(Color.black);
		else if (GeologyMarsMap.TYPE.equals(mapType))
			g.setColor(Color.black);
		else
			g.setColor(new Color(0, 96, 0));

		// Draw trail
		if (singleVehicle != null)
			displayTrail(singleVehicle, mapCenter, mapType, g);
		else {
			Iterator<Vehicle> i = unitManager.getVehicles().iterator();
			while (i.hasNext())
				displayTrail(i.next(), mapCenter, mapType, g);
		}
	}

	/**
	 * Displays the trail behind a vehicle.
	 * 
	 * @param vehicle   the vehicle to display.
	 * @param mapCenter the location of the center of the map.
	 * @param mapType   the type of map.
	 * @param g         the graphics context.
	 */
	private void displayTrail(Vehicle vehicle, Coordinates mapCenter, String mapType, Graphics g) {

		// Get map angle.
		double angle = CannedMarsMap.HALF_MAP_ANGLE;

		// Draw trail.
		IntPoint oldSpot = null;
		Iterator<Coordinates> j = (new ArrayList<Coordinates>(vehicle.getTrail())).iterator();
		while (j.hasNext()) {
			Coordinates trailSpot = j.next();
			if (trailSpot != null) {
				if (mapCenter.getAngle(trailSpot) < angle) {
					IntPoint spotLocation = MapUtils.getRectPosition(trailSpot, mapCenter, mapType);
					if ((oldSpot == null))
						g.drawRect(spotLocation.getiX(), spotLocation.getiY(), 1, 1);
					else if (!spotLocation.equals(oldSpot))
						g.drawLine(oldSpot.getiX(), oldSpot.getiY(), spotLocation.getiX(), spotLocation.getiY());
					oldSpot = spotLocation;
				}
			}
		}
	}
}