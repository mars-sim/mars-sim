/*
 * Mars Simulation Project
 * WorkerMapLayer.java
 * @date 2025-08-01
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
    private static final int LABEL_XOFFSET = 1;
    private static final int LABEL_YOFFSET = -1;
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
     * Gets the appropriate Color to render a Worker.
     * 
     * @param w The worker
     * @param b Are they selected
     * @return
     */
    protected abstract ColorChoice getColor(T w, boolean b);
}
