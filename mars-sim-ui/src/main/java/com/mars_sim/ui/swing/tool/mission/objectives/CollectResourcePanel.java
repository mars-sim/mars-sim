/*
 * Mars Simulation Project
 * CollectResourcePanel.java
 * @date 2025-06-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.objectives.CollectResourceObjective;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * A panel for displaying collect resources objectives information.
 */
@SuppressWarnings("serial")
public class CollectResourcePanel extends JPanel implements EntityListener {

	private CollectResourceObjective objective;

	private DefaultListModel<String> resourceModel;
	private DefaultListModel<String> siteModel;

	
	/**
	 * Constructor.
	 */
	public CollectResourcePanel(CollectResourceObjective objective) {
		super();

		this.objective = objective;
		setName(objective.getName());

		// Set layout.
		setLayout(new BorderLayout());

		var attrs = new AttributePanel(1);
	  	attrs.addRow("Max. per site", StyleManager.DECIMAL_KG.format(objective.getSiteResourceGoal()));
		add(attrs, BorderLayout.NORTH);

		var horz = new JPanel(new GridLayout(1, 2));
		add(horz, BorderLayout.CENTER);

		resourceModel = new DefaultListModel<>();
		horz.add(createList(resourceModel, "Resources Collected"));

		siteModel = new DefaultListModel<>();
		horz.add(createList(siteModel, "Sites"));

		updateCollectionValueLabel();
	}


	static Component createList(DefaultListModel<String> model, String title) {
		var list = new JList<>(model); 
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);


		return StyleManager.createScrollBorder(title, list);
	}


	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.INVENTORY_RESOURCE_EVENT == event.getType()) {
			updateCollectionValueLabel();
		}
	}

	/**
	 * Updates the collection value label.
	 */
	private void updateCollectionValueLabel() {
		resourceModel.clear();
		objective.getResourcesCollected().entrySet().stream()
			.map(e -> ResourceUtil.findAmountResourceName(e.getKey()) + ": " + StyleManager.DECIMAL_KG.format(e.getValue()))
			.sorted()
			.forEach(resourceModel::addElement);

		siteModel.clear();
		objective.getCollectedAtSites().entrySet().stream()
			.map(e -> "Site #" + Integer.toString(e.getKey()) + ": " + StyleManager.DECIMAL_KG.format(e.getValue()))
			.sorted()
			.forEach(siteModel::addElement);
	}
}