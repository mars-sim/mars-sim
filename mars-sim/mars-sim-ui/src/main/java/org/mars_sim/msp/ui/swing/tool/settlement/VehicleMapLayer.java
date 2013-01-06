/**
 * Mars Simulation Project
 * VehicleMapLayer.java
 * @version 3.04 2012-12-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A settlement map layer for displaying vehicles.
 */
public class VehicleMapLayer implements SettlementMapLayer {

    // Static members
    private static final Color VEHICLE_COLOR = Color.RED;
    
    // Data members
    private SettlementMapPanel mapPanel;
    private Map<Double, Map<GraphicsNode, BufferedImage>> svgImageCache;
    private double scale;
    
    /**
     * Constructor
     * @param mapPanel the settlement map panel.
     */
    public VehicleMapLayer(SettlementMapPanel mapPanel) {
        
        // Initialize data members.
        this.mapPanel = mapPanel;
        svgImageCache = new HashMap<Double, Map<GraphicsNode, BufferedImage>>(21);
        
        // Set Apache Batik library system property so that it doesn't output: 
        // "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
        System.setProperty("org.apache.batik.warn_destination", "false");
    }
    
	@Override
	public void displayLayer(Graphics2D g2d, Settlement settlement,
			double xPos, double yPos, int mapWidth, int mapHeight,
			double rotation, double scale) {

        this.scale = scale;
        
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
        drawVehicles(g2d, settlement);
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
	}
	
    /**
     * Draw all of the vehicles parked at the settlement.
     * @param g2d the graphics context.
     * @param settlement the settlement.
     */
	private void drawVehicles(Graphics2D g2d, Settlement settlement) {
		
		if (settlement != null) {
		    // Draw all vehicles parked at the settlement.
			Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
			    Vehicle vehicle = i.next();
				drawVehicle(vehicle, g2d);
				
                // Draw any towed vehicles.
                if (vehicle instanceof Rover) {
                    Rover rover = (Rover) vehicle;
                    Vehicle towedVehicle = rover.getTowedVehicle();
                    if (towedVehicle != null) {
                        drawVehicle(towedVehicle, g2d);
                    }
                }
			}
			
			// Draw all vehicles used in construction sites at the settlement.
			Iterator<Mission> j = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
			while (j.hasNext()) {
			    Mission mission = j.next();
			    if (mission instanceof BuildingConstructionMission) {
			        Iterator<GroundVehicle> k = ((BuildingConstructionMission) mission).getConstructionVehicles().iterator();
			        while (k.hasNext()) {
			            drawVehicle(k.next(), g2d);
			        }
			    }
			    else if (mission instanceof BuildingSalvageMission) {
			        Iterator<GroundVehicle> k = ((BuildingSalvageMission) mission).getConstructionVehicles().iterator();
                    while (k.hasNext()) {
                        drawVehicle(k.next(), g2d);
                    }
			    }
			}
		}
	}
	
    /**
     * Draws a vehicle on the map.
     * @param vehicle the vehicle.
     * @param g2d the graphics context.
     */
    private void drawVehicle(Vehicle vehicle, Graphics2D g2d) {
        
        // Use SVG image for vehicle if available.
        GraphicsNode svg = SVGMapUtil.getVehicleSVG(vehicle.getDescription().toLowerCase());
        if (svg != null) {
            drawSVGVehicle(g2d, vehicle.getXLocation(), vehicle.getYLocation(), 
            		vehicle.getWidth(), vehicle.getLength(), vehicle.getFacing(), svg);
            
            if (vehicle instanceof LightUtilityVehicle) {
                drawSVGPartAttachments(g2d, (LightUtilityVehicle) vehicle);
            }
        }
        else {
            // Otherwise draw colored rectangle for vehicle.
            drawRectangleVehicle(g2d, vehicle.getXLocation(), vehicle.getYLocation(), 
            		vehicle.getWidth(), vehicle.getLength(), vehicle.getFacing(), 
                    VEHICLE_COLOR);
        }
    }
	
    /**
     * Draws a vehicle as a SVG image on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the vehicle width (meters).
     * @param length the vehicle length (meters).
     * @param facing the vehicle facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     */
    private void drawSVGVehicle(Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg) {
        
        drawVehicle(true, g2d, xLoc, yLoc, width, length, facing, svg, null);
    }
    
    /**
     * Draws a vehicle as a rectangle on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the vehicle width (meters).
     * @param length the vehicle length (meters).
     * @param facing the vehicle facing (degrees from North clockwise).
     * @param color the color to draw the rectangle.
     */
    private void drawRectangleVehicle(Graphics2D g2d, double xLoc, double yLoc, 
            double width, double length, double facing, Color color) {
        
        drawVehicle(false, g2d, xLoc, yLoc, width, length, facing, null, color);
    }
    
    /**
     * Draws a vehicle on the map.
     * @param isSVG true if using a SVG image.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the vehicle width (meters).
     * @param length the vehicle length (meters).
     * @param facing the vehicle facing (degrees from North clockwise).
     * @param svg the SVG graphics node.
     * @param color the color to display the rectangle if no SVG image.
     */
    private void drawVehicle(boolean isSVG, Graphics2D g2d, double xLoc, double yLoc,
            double width, double length, double facing, GraphicsNode svg, Color color) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Determine bounds.
        Rectangle2D bounds = null;
        if (isSVG) bounds = svg.getBounds();
        else bounds = new Rectangle2D.Double(0, 0, width, length);
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        double boundsPosX = bounds.getX() * scalingWidth;
        double boundsPosY = bounds.getY() * scalingLength;
        double centerX = width * scale / 2D;
        double centerY = length * scale / 2D;
        double translationX = (-1D * xLoc * scale) - centerX - boundsPosX;
        double translationY = (-1D * yLoc * scale) - centerY - boundsPosY;
        double facingRadian = facing / 180D * Math.PI;
        
        // Apply graphic transforms for vehicle.
        AffineTransform newTransform = new AffineTransform();
        newTransform.translate(translationX, translationY);
        newTransform.rotate(facingRadian, centerX + boundsPosX, centerY + boundsPosY);        
        
        if (isSVG) {
            // Draw SVG image.
//            newTransform.scale(scalingWidth, scalingLength);
//            svg.setTransform(newTransform);
//            svg.paint(g2d);
            
            
            // Draw buffered image of vehicle.
            BufferedImage image = getBufferedImage(svg, width, length);
            if (image != null) {
                g2d.transform(newTransform);
                g2d.drawImage(image, 0, 0, mapPanel);
            }
        }
        else {
            // Draw filled rectangle.
            newTransform.scale(scalingWidth, scalingLength);
            g2d.transform(newTransform);
            g2d.setColor(color);
            g2d.fill(bounds);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Draws the parts attached to a light utility vehicle.
     * @param g2d the graphics context
     * @param vehicle the light utility vehicle.
     */
    private void drawSVGPartAttachments(Graphics2D g2d, LightUtilityVehicle vehicle) {
        Iterator<Part> i = vehicle.getPossibleAttachmentParts().iterator();
        while (i.hasNext()) {
            Part part = i.next();
            if (vehicle.getInventory().getItemResourceNum(part) > 0) {
                // Use SVG image for part if available.
                GraphicsNode partSvg = SVGMapUtil.getAttachmentPartSVG(part.getName().toLowerCase());
                GraphicsNode vehicleSvg = SVGMapUtil.getVehicleSVG(vehicle.getDescription().toLowerCase());
                if ((partSvg != null) && (vehicleSvg != null)) {
                    drawPartAttachment(g2d, vehicle.getXLocation(), vehicle.getYLocation(),
                            vehicle.getWidth(), vehicle.getLength(), vehicle.getFacing(), vehicleSvg, partSvg);
                }
            }
        }
    }
    
    /**
     * Draws a vehicle part attachment on the map.
     * @param g2d the graphics2D context.
     * @param xLoc the X location from center of settlement (meters).
     * @param yLoc the y Location from center of settlement (meters).
     * @param width the vehicle width (meters).
     * @param length the vehicle length (meters).
     * @param facing the vehicle facing (degrees from North clockwise).
     * @param vehicleSvg the vehicle SVG graphics node.
     * @param partSvg the part SVG graphics node.
     */
    private void drawPartAttachment(Graphics2D g2d, double xLoc, double yLoc,
            double vehicleWidth, double vehicleLength, double facing, GraphicsNode vehicleSvg, 
            GraphicsNode partSvg) {
        
        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
        
        // Determine bounds.
        Rectangle2D partBounds = partSvg.getBounds();
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
        BufferedImage image = getBufferedImage(partSvg, partWidth, partLength);
        if (image != null) {
            g2d.transform(newTransform);
            g2d.drawImage(image, 0, 0, mapPanel);
        }
        
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
    }
    
    /**
     * Gets a buffered image for a given graphics node.
     * @param svg the graphics node.
     * @param width the vehicle width.
     * @param length the vehicle length.
     * @return buffered image.
     */
    private BufferedImage getBufferedImage(GraphicsNode svg, double width, double length) {
        
        // Get image cache for current scale or create it if it doesn't exist.
        Map<GraphicsNode, BufferedImage> imageCache = null;
        if (svgImageCache.containsKey(scale)) {
            imageCache = svgImageCache.get(scale);
        }
        else {
            imageCache = new HashMap<GraphicsNode, BufferedImage>(100);
            svgImageCache.put(scale, imageCache);
        }
        
        // Get image from image cache or create it if it doesn't exist.
        BufferedImage image = null;
        if (imageCache.containsKey(svg)) image = imageCache.get(svg);
        else {
            image = createBufferedImage(svg, width, length);
            imageCache.put(svg, image);
        }
        
        return image;
    }
    
    /**
     * Creates a buffered image from a SVG graphics node.
     * @param svg the SVG graphics node.
     * @param width the width of the produced image.
     * @param length the length of the produced image.
     * @return the created buffered image.
     */
    private BufferedImage createBufferedImage(GraphicsNode svg, double width, double length) {
        
        BufferedImage bufferedImage = new BufferedImage((int) (width * scale), (int) (length * scale), 
                BufferedImage.TYPE_INT_ARGB);
        
        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        
        // Draw the SVG image on the buffered image.
        Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.setTransform(AffineTransform.getScaleInstance(scalingWidth, scalingLength));
        svg.paint(g2d);

        // Cleanup and return image
        g2d.dispose();
        
        return bufferedImage;
    }

    @Override
    public void destroy() {
        // Clear all buffered image caches.
        Iterator<Map<GraphicsNode, BufferedImage>> i = svgImageCache.values().iterator();
        while (i.hasNext()) {
            i.next().clear();
        }
        svgImageCache.clear();
    }
}