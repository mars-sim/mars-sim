/*
 * Mars Simulation Project
 * WorkerMapLayer.java
 * @date 2025-08-15
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;

import org.apache.batik.gvt.GraphicsNode;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;

/**
 * This provides a map layer that can draw Workers on the Map panel. 
 * It is aware of selected and nonselected Workers and draw accordingly.
 */
public abstract class WorkerMapLayer<T extends Worker> extends AbstractMapLayer {
	
    private static final int LABEL_XOFFSET = 1;
    private static final int LABEL_YOFFSET = -1;

    private static final double width = .45;
    private static final double length = .4;
 
    private static final BasicStroke STROKE = new BasicStroke(1.5f);
 
    private static final Font NAME_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 6); 
    private static final Font TASK_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 6); 
    private static final Font MISSION_FONT = new Font(Font.SANS_SERIF, Font.ITALIC, 6); 
    /**
	 * Draws workers at a settlement.
	 * 
     * @param workers Workers to draw
     * @param selected The selected Worker
     * @param showLabels Show the labels
	 * @param viewpoint Map viewpoint for rendering
	 */
	protected void drawWorkers(Collection<T> workers, T selected, boolean showLabels,
                                MapViewPoint viewpoint) {
                                
        // Save original graphics transforms.
        AffineTransform saveTransform = viewpoint.prepareGraphics();


		// Draw all workers except selected person.
		for (T w : workers) {
			if (!w.equals(selected)) {
				drawUnselectedWorker(w, showLabels, viewpoint);
			}
		}

		// Draw selected person.
		if (workers.contains(selected)) {
            drawSelectedWorker(selected, viewpoint);
		}

        // Restore original graphic transforms.
        viewpoint.graphics().setTransform(saveTransform);
	}

    private void drawUnselectedWorker(T w, boolean showLabels, MapViewPoint viewpoint) {
        ColorChoice color = getColor(w, false);
        LocalPosition pos = w.getPosition();

        // Use SVG image for the worker if available.
       	GraphicsNode svg = SVGMapUtil.getUnitSVG(w.getStringType());
       	if (svg != null) {
       		// Draw base SVG image for vehicle.
       		drawUnit(pos, color, false, svg, viewpoint);
       	}
       	else    	
       		drawOval(pos, color, viewpoint);

        if (showLabels) {
            // Draw label
            drawRightLabel(false, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, LABEL_YOFFSET, viewpoint);
        }
    }

    /**
     * Draws a selected worker on the Map. Selected works also shows labels.
     * 
     * @param g2d
     * @param w
     */
    private void drawSelectedWorker(T w, MapViewPoint viewpoint) {
        ColorChoice color = getColor(w, true);

        LocalPosition pos = w.getPosition();
        
        // Use SVG image for the worker if available.
     	GraphicsNode svg = SVGMapUtil.getUnitSVG(w.getStringType());
     	if (svg != null) {
     		// Draw base SVG image for vehicle.
     		drawUnit(pos, color, true, svg, viewpoint);
     	}
     	else    	
       		drawOval(pos, color, viewpoint);
 
        String taskDesc = w.getTaskDescription();
        Mission mission = w.getMission();
        
        if (taskDesc.equals("")) {
        	if (mission != null) {
                drawRightLabel(true, "-- " + mission.getName(), w.getPosition(), color,
                	MISSION_FONT, LABEL_XOFFSET, .95f * LABEL_YOFFSET, viewpoint);
        		drawRightLabel(true, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, 1.9f * LABEL_YOFFSET, viewpoint);
        	}
        	else {
        		drawRightLabel(true, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, LABEL_YOFFSET, viewpoint);
        	}
        }
        else {
        	if (mission != null) {
                drawRightLabel(true, "-- " + mission.getName(), w.getPosition(), color,
                    	MISSION_FONT, LABEL_XOFFSET, .95f * LABEL_YOFFSET, viewpoint);
       		 	drawRightLabel(true, "- " + taskDesc, w.getPosition(), color,
       	                TASK_FONT, LABEL_XOFFSET, 1.9f * LABEL_YOFFSET, viewpoint);
        		drawRightLabel(true, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, 3 * LABEL_YOFFSET, viewpoint);
        	}
        	else {
       		 	drawRightLabel(true, "- " + taskDesc, w.getPosition(), color,
       	                TASK_FONT, LABEL_XOFFSET, .95f * LABEL_YOFFSET, viewpoint);
        		drawRightLabel(true, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, 1.9f * LABEL_YOFFSET, viewpoint);
        	}
       	} 
    }

	/**
     * Draws a unit using SVG on the map.
     * 
     * @param pos LocalPosition
     * @param color ColorChoice
     * @param isSelected is being selected
     * @param svg the SVG graphics node.
     * @param patternSVG the pattern SVG graphics node (null if no pattern).
	 * @param viewpoint MapViewPoint
     */
    protected void drawUnit(LocalPosition pos, ColorChoice color, boolean isSelected, GraphicsNode svg,
								MapViewPoint viewpoint) {
		
		var g2d = viewpoint.graphics();
		double scale = viewpoint.scale();

        // Save original graphics transforms.
        AffineTransform saveTransform = g2d.getTransform();
   
        // Determine bounds.
        Rectangle2D bounds = svg.getBounds();
        
        // Determine transform information.
        double scalingWidth = width / bounds.getWidth() * scale;
        double scalingLength = length / bounds.getHeight() * scale;
        double boundsPosX = bounds.getX() * scalingWidth;
        double boundsPosY = bounds.getY() * scalingLength;
        double centerX = width * scale / 2D;
        double centerY = length * scale / 2D;
        double translationX = (-1D * pos.getX() * scale) - centerX - boundsPosX;
        double translationY = (-1D * pos.getY() * scale) - centerY - boundsPosY;

        AffineTransform newTransform = new AffineTransform();
        
		// Draw buffered image of structure.
		BufferedImage image = getBufferedImage(svg, width, length, null, scale);
		
		if (image != null) {
			// Apply graphic transforms.		
			newTransform.translate(translationX, translationY);
		
			g2d.transform(newTransform);
			
			g2d.drawImage(image, 0, 0, null);
		}	
	
		if (isSelected) {  
			// Save original stroke
        	Stroke oldStroke = g2d.getStroke();
			// Draw the dashed border over the selected 
			g2d.setStroke(STROKE); 
	
			g2d.setPaint(color.text());
			
			g2d.draw(new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
			// Restore the stroke
			g2d.setStroke(oldStroke);
		}
		
		image.flush();
		
        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);    
    }
    
    /**
     * Gets the appropriate Color to render a Worker.
     * 
     * @param w The worker
     * @param b Are they selected
     * @return
     */
    protected abstract ColorChoice getColor(T w, boolean b);
}
