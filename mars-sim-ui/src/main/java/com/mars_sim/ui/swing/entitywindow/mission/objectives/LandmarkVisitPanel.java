/*
 * Mars Simulation Project
 * LandmarkVisitPanel.java
 * @date 2026-07-19
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission.objectives;

import javax.swing.JPanel;

import com.mars_sim.core.mission.objectives.LandmarkObjective;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.utils.SurfacePOILabel;

/**
 * This panel displays the details of a Landmark visit objective.
 */
public class LandmarkVisitPanel extends JPanel {
    
    /**
	 * Constructor
	 * 
	 * @param objective the landmark objective.
	 * @param context the UI context.
	 */
	public LandmarkVisitPanel(LandmarkObjective objective, UIContext context) {
        setName(objective.getName());
        var landmark = objective.getLandmark();
        var attributePanel = new AttributePanel();

        attributePanel.addLabelledItem("Landmark", new SurfacePOILabel(landmark, context));
        attributePanel.addTextField("Type", landmark.getType().getName(), "Landmark type");
        attributePanel.addTextField("mSols on site", String.valueOf(objective.getMSolOnSite()), "Time at landmark");
        
        add(attributePanel);
    }
}
