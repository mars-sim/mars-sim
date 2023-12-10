/**
 * Mars Simulation Project
 * WorkerMapLayer.java
 * @date 2023-12-10
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Collection;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.Msg;

/**
 * This provides a map layer that can draw Workers on the Map panel. 
 * It is aware of selected and nonselected Workers and draw accordingly.
 */
public abstract class WorkerMapLayer<T extends Worker> extends AbstractMapLayer {
    private static final int LABEL_XOFFSET = 5;
    private static final Font NAME_FONT = new Font("Arial", Font.PLAIN, 10); 
    private static final Font DETAILS_FONT = new Font("Arial", Font.PLAIN, 8); 

    /**
	 * Draws workers at a settlement.
	 * 
     * @param workers Workers to draw
     * @param selected The selected Worker
     * @param showLabels Show the labels
	 * @param g2d the graphics context.
	 */
	protected void drawWorkers(Collection<T> workers, T selected, boolean showLabels,
                            Graphics2D g2d,
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


		// Draw all workers except selected person.
		for(T w : workers) {
			if (!w.equals(selected)) {
				drawUnselectedWorker(g2d, w, showLabels, rotation, scale);
			}
		}

		// Draw selected person.
		if (workers.contains(selected)) {
            drawSelectedWorker(g2d, selected, rotation, scale);
		}

        // Restore original graphic transforms.
        g2d.setTransform(saveTransform);
	}

    private void drawUnselectedWorker(Graphics2D g2d, T w, boolean showLabels, double rotation, double scale) {
        ColorChoice color = getColor(w, false);
        LocalPosition pos = w.getPosition();

        drawOval(g2d, pos, color, rotation, scale);

        if (showLabels) {
            // Draw label
            drawRightLabel(g2d, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, 0, rotation, scale);
        }
    }

    /**
     * Draw a selected worker on the Map. Selected works also shows labels.
     * @param g2d
     * @param w
     * @param rotation
     * @param scale
     */
    private void drawSelectedWorker(Graphics2D g2d, T w, double rotation, double scale) {
        ColorChoice color = getColor(w, true);

        LocalPosition pos = w.getPosition();
        
        drawOval(g2d, pos, color, rotation, scale);

        // Draw label
        drawRightLabel(g2d, w.getName(), pos, color,
                    NAME_FONT, LABEL_XOFFSET, 0, rotation, scale);
            
        float yoffset = (float)(scale / 2.0);

        // Draw task.
        String taskDesc = w.getTaskDescription();
        if (!taskDesc.equals("")) {
            String taskString = "- " + Msg.getString("LabelMapLayer.activity", taskDesc); 

            drawRightLabel(
                g2d, taskString, w.getPosition(), color,
                DETAILS_FONT, LABEL_XOFFSET, yoffset, rotation, scale);
        }
            
        // Draw mission.
        Mission mission = w.getMission();
        if (mission != null) {
            String missionString = "-- " + Msg.getString("LabelMapLayer.mission", mission.getName(), mission.getPhaseDescription()); 
            drawRightLabel(
                g2d, missionString, w.getPosition(), color,
                DETAILS_FONT, LABEL_XOFFSET, 2f * yoffset, rotation, scale);
        }
    }

    /**
     * Get the appropriate Color to render a Worker
     * @param w The worker
     * @param b Are they selected
     * @return
     */
    protected abstract ColorChoice getColor(T w, boolean b);
}
