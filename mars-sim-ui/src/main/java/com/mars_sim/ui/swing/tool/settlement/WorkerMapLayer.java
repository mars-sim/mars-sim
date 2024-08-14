/**
 * Mars Simulation Project
 * WorkerMapLayer.java
 * @date 2023-12-10
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.util.Collection;

import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * This provides a map layer that can draw Workers on the Map panel. 
 * It is aware of selected and nonselected Workers and draw accordingly.
 */
public abstract class WorkerMapLayer<T extends Worker> extends AbstractMapLayer {
    private static final int LABEL_XOFFSET = 4;
    private static final Font NAME_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 8); 
    private static final Font DETAILS_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 6); 

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
		for(T w : workers) {
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

        drawOval(pos, color, viewpoint);

        if (showLabels) {
            // Draw label
            drawRightLabel(false, w.getName(), pos, color,
                        NAME_FONT, LABEL_XOFFSET, 0, viewpoint);
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
        
        drawOval(pos, color, viewpoint);

        // Draw label
        drawRightLabel(true, w.getName(), pos, color,
                    NAME_FONT, LABEL_XOFFSET, 0, viewpoint);
            
        float yoffset = (float)((33 + viewpoint.scale()) / 3);

        // Draw task.
        String taskDesc = w.getTaskDescription();
        if (!taskDesc.equals("")) {
            String taskString = "- " + taskDesc; 

            drawRightLabel(true, taskString, w.getPosition(), color,
                DETAILS_FONT, LABEL_XOFFSET, yoffset, viewpoint);
        }
            
        // Draw mission.
        Mission mission = w.getMission();
        if (mission != null) {
            String missionString = "-- " + mission.getName(); 
            drawRightLabel(true, missionString, w.getPosition(), color,
                DETAILS_FONT, LABEL_XOFFSET, 2f * yoffset, viewpoint);
        }
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
