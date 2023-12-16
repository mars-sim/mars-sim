/*
 * Mars Simulation Project
 * VehicleMapLayer.java
 * @date 2023-04-19
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.LoadingController;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.LocalBoundedObject;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

/**
 * A settlement map layer for displaying vehicles.
 */
public class VehicleMapLayer extends AbstractMapLayer {

	// Static members
	private static final Color RECT_COLOR = new Color(208, 224, 242); // pale grey color
	private static final ColorChoice VEHICLE_COLOR = new ColorChoice(Color.YELLOW.darker(), Color.BLACK);
	private static final Font LABEL_FONT = new Font(Font.SERIF, Font.PLAIN, 10); // Note size doesn;t matter

	// Data members
	private SettlementMapPanel mapPanel;

	/**
	 * Constructor.
	 * 
	 * @param mapPanel the settlement map panel.
	 */
	public VehicleMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;
	}


	@Override
	public void displayLayer(Graphics2D g2d, Settlement settlement,
			double xPos, double yPos, int mapWidth, int mapHeight,
			double rotation, double scale) {

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		// Get the map center point.
		double mapCenterX = mapWidth / 2D;
		double mapCenterY = mapHeight / 2D;

		// Translate map from settlement center point.
		g2d.translate(mapCenterX + (xPos * scale), mapCenterY + (yPos * scale));

		// Rotate map from North.
		g2d.rotate(rotation, 0D - (xPos * scale), 0D - (yPos * scale));

		// Draw all vehicles.
		// Draw all parked vehicles at this settlement location
		for(Vehicle v : CollectionUtils.getVehiclesInSettlementVicinity(settlement)) {
			drawVehicle(v, g2d, rotation, scale, mapPanel.isShowVehicleLabels());
		}

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}


	/**
	 * Draws a vehicle on the map.
	 * 
	 * @param vehicle the vehicle.
	 * @param g2d the graphics context.
	 */
	private void drawVehicle(Vehicle vehicle, Graphics2D g2d,
							double rotation, double scale,
							boolean showLabel) {

		// Use SVG image for vehicle if available.
		GraphicsNode svg = SVGMapUtil.getVehicleSVG(vehicle.getBaseImage());
		if (svg != null) {
			// Draw base SVG image for vehicle.
			drawSVGVehicle(g2d, scale, vehicle, svg);

			// Draw overlay if the vehicle is being maintained or repaired.
			if (isVehicleRepairOrMaintenance(vehicle)) {
				drawSVGRepairMaint(g2d, scale, vehicle);
			}

			// Draw overlay if the vehicle is being loaded or unloaded.
			if (isVehicleLoading(vehicle)) {
				drawSVGLoading(g2d, scale, vehicle);
			}

			// Draw attachment parts for light utility vehicle.
			if (vehicle instanceof LightUtilityVehicle luv) {
				drawSVGPartAttachments(g2d, scale, luv);
			}
		}
		else {
			// Otherwise draw colored rectangle for vehicle.
			drawRectangle(g2d, scale, vehicle, RECT_COLOR, null);
		}

		if (showLabel) {
			drawCenteredLabel(g2d, vehicle.getName(), LABEL_FONT, vehicle.getPosition(),
							VEHICLE_COLOR, 0, rotation, scale);
		}
	}

	/**
	 * Checks if the vehicle is currently being repaired or maintained.
	 * 
	 * @param vehicle the vehicle
	 * @return true if vehicle is being repaired or maintained.
	 */
	private boolean isVehicleRepairOrMaintenance(Vehicle vehicle) {
		boolean result = vehicle.isReservedForMaintenance();

		// Check if vehicle is reserved for maintenance.

        // Check if vehicle has malfunction.
		if (vehicle.getMalfunctionManager().hasMalfunction()) {
			result = true;
		}

		return result;
	}

	/**
	 * Draws the SVG repair/maint overlay on the vehicle.
	 * 
	 * @param g2d the graphics context.
	 * @param vehicle the vehicle.
	 */
	private void drawSVGRepairMaint(Graphics2D g2d, double scale, Vehicle vehicle) {
		// Use SVG image for vehicle maintenance overlay if available.
		GraphicsNode maintOverlaySvg = SVGMapUtil.getMaintenanceOverlaySVG(vehicle.getBaseImage());
		GraphicsNode vehicleSvg = SVGMapUtil.getVehicleSVG(vehicle.getBaseImage());
		if ((maintOverlaySvg != null) && (vehicleSvg != null)) {
			drawVehicleOverlay(g2d, scale, vehicle, vehicleSvg, maintOverlaySvg);
		}
	}

	/**
	 * Checks if the vehicle is currently being loaded or unloaded.
	 * 
	 * @param vehicle the vehicle
	 * @return true if vehicle is being loaded or unloaded.
	 */
	private boolean isVehicleLoading(Vehicle vehicle) {
		boolean result = false;

		// For vehicle missions, check if vehicle is loading or unloading for the mission.
		Mission mission = vehicle.getMission();
		if ((mission != null) && (mission instanceof VehicleMission vm)) {
			LoadingController lp = vm.getLoadingPlan();
			result = (lp != null) && !lp.isCompleted();
		}

		return result;
	}

	/**
	 * Draws the SVG loading/unloading overlay on the vehicle.
	 * 
	 * @param g2d the graphics context.
	 * @param vehicle the vehicle.
	 */
	private void drawSVGLoading(Graphics2D g2d, double scale, Vehicle vehicle) {

		// Use SVG image for vehicle loading overlay if available.
		GraphicsNode loadOverlaySvg = SVGMapUtil.getLoadingOverlaySVG(vehicle.getBaseImage());
		GraphicsNode vehicleSvg = SVGMapUtil.getVehicleSVG(vehicle.getBaseImage());
		if ((loadOverlaySvg != null) && (vehicleSvg != null)) {
			drawVehicleOverlay(g2d, scale, vehicle, vehicleSvg, loadOverlaySvg);
		}
	}

	/**
	 * Draws a vehicle as a SVG image on the map.
	 * 
	 * @param g2d the graphics2D context.
	 * @param scale Map scale
	 * @param vehiclePlacement Vehicle location
	 * @param svg the SVG graphics node.
	 */
	private void drawSVGVehicle(Graphics2D g2d, double scale, LocalBoundedObject vehiclePlacement, GraphicsNode svg) {
		drawStructure(g2d, scale, vehiclePlacement, svg, null, null);
	}

	/**
	 * Draws the parts attached to a light utility vehicle.
	 * 
	 * @param g2d the graphics context
	 * @param vehicle the light utility vehicle.
	 */
	private void drawSVGPartAttachments(Graphics2D g2d, double scale, LightUtilityVehicle vehicle) {
		Iterator<Part> i = vehicle.getPossibleAttachmentParts().iterator();
		while (i.hasNext()) {
			Part part = i.next();
			if (vehicle.getItemResourceStored(part.getID()) > 0) {
				// Use SVG image for part if available.
				GraphicsNode partSvg = SVGMapUtil.getAttachmentPartSVG(part.getName().toLowerCase());
				GraphicsNode vehicleSvg = SVGMapUtil.getVehicleSVG(vehicle.getBaseImage());
				if ((partSvg != null) && (vehicleSvg != null)) {
					drawVehicleOverlay(g2d, scale, vehicle, vehicleSvg, partSvg);
				}
			}
		}
	}

	/**
	 * Draws an overlay for a vehicle on the map.
	 * 
	 * @param g2d the graphics2D context.
	 * @param scale Map scale
	 * @param vehiclePlacement Vehicle position
	 * @param vehicleSvg the vehicle SVG graphics node.
	 * @param overlaySvg the overlay SVG graphics node.
	 */
	private void drawVehicleOverlay(Graphics2D g2d, double scale, LocalBoundedObject vehiclePlacement, GraphicsNode vehicleSvg, 
			GraphicsNode overlaySvg) {
		double xLoc = vehiclePlacement.getXLocation();
		double yLoc = vehiclePlacement.getYLocation();
		double vehicleWidth = vehiclePlacement.getWidth();
		double vehicleLength = vehiclePlacement.getLength();
		double facing = vehiclePlacement.getFacing();

		// Save original graphics transforms.
		AffineTransform saveTransform = g2d.getTransform();

		// Determine bounds.
		Rectangle2D partBounds = overlaySvg.getBounds();
		Rectangle2D vehicleBounds = vehicleSvg.getBounds();

		// Determine part width and length.
		double partWidth = (partBounds.getWidth() / vehicleBounds.getWidth()) * vehicleWidth;
		double partLength = (partBounds.getHeight() / vehicleBounds.getHeight()) * vehicleLength;

		// Determine transform information.
		double scalingWidth = partWidth / partBounds.getWidth() * scale;
		double scalingLength = partLength / partBounds.getHeight() * scale;
		double boundsPosX = partBounds.getX() * scalingWidth;
		double boundsPosY = partBounds.getY() * scalingLength;
		double centerX = partWidth * scale / 2D;
		double centerY = partLength * scale / 2D;
		double translationX = (-1D * xLoc * scale) - centerX - boundsPosX;
		double translationY = (-1D * yLoc * scale) - centerY - boundsPosY;
		double facingRadian = facing / 180D * Math.PI;

		// Apply graphic transforms for vehicle part.
		AffineTransform newTransform = new AffineTransform();
		newTransform.translate(translationX, translationY);
		newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);        

		// Draw buffered image of vehicle.
		BufferedImage image = getBufferedImage(overlaySvg, partWidth, partLength, null, scale);
		if (image != null) {
			g2d.transform(newTransform);
			g2d.drawImage(image, 0, 0, mapPanel);
		}

		// Restore original graphic transforms.
		g2d.setTransform(saveTransform);
	}
}
