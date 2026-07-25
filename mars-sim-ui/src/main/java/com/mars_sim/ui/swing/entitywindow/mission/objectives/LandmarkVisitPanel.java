/*
 * Mars Simulation Project
 * LandmarkVisitPanel.java
 * @date 2026-07-19
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission.objectives;

import java.awt.BorderLayout;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.objectives.LandmarkObjective;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.utils.SurfacePOILabel;

/**
 * This panel displays the details of a Landmark visit objective.
 */
public class LandmarkVisitPanel extends JPanel 
    implements EntityListener {
    
    private DefaultListModel<String> viewingModel;
    private LandmarkObjective objective;

    /**
	 * Constructor
	 * 
	 * @param objective the landmark objective.
	 * @param context the UI context.
	 */
	public LandmarkVisitPanel(LandmarkObjective objective, UIContext context) {
        setLayout(new BorderLayout());

        setName(objective.getName());
        var landmark = objective.getLandmark();
        var attributePanel = new AttributePanel();
        this.objective = objective;

        attributePanel.addLabelledItem("Landmark", new SurfacePOILabel(landmark, context));
        attributePanel.addTextField("Type", landmark.getType().getName(), "Landmark type");
        attributePanel.addTextField("Time at site", String.valueOf(objective.getMSolAtSite()) + " mSol", "Time at landmark");
        attributePanel.addTextField("Viewing time", String.valueOf(objective.getMSolViewing()) + " mSol", "Time spent viewing the landmark");
        
        add(attributePanel, BorderLayout.NORTH);

        viewingModel = new DefaultListModel<String>();
		add(CollectResourcePanel.createList(viewingModel, "Viewing Time"), BorderLayout.CENTER);
        loadModel();
    }

    private void loadModel() {
        viewingModel.clear();
        objective.getEVATimes().entrySet().stream()
            .map(e -> e.getKey() + ": " + StyleManager.DECIMAL_MSOL.format(e.getValue()))
            .sorted()
            .forEach(viewingModel::addElement);
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        if (event.getType().equals(MissionObjective.CHANGE_EVENT)) {
            loadModel();
        }
    }
}
