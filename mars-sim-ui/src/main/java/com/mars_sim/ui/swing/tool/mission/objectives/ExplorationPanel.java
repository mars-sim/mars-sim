/*
 * Mars Simulation Project
 * ExplorationPanel.java
 * @date 2024-07-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.ui.swing.StyleManager;


/**
 * A panel for displaying exploration mission information.
 */
@SuppressWarnings("serial")
public class ExplorationPanel extends JPanel 
	implements EntityListener, EntityListener  {

	// Data members
	private Map<String, ExplorationSitePanel> sitePanes;
	
	private DefaultListModel<String> resourceModel;
	private ExplorationObjective objective;

	private JPanel sitesPane;
	

	/**
	 * Constructor.
	 */
	public ExplorationPanel(ExplorationObjective objective) {
		super();

		this.objective = objective;
		setName(objective.getName());

		// Set layout.
		setLayout(new BorderLayout());

		var horz = new JPanel(new GridLayout(1, 2));
		add(horz, BorderLayout.CENTER);

		resourceModel = new DefaultListModel<>();
		horz.add(CollectResourcePanel.createList(resourceModel, "Rocks Collected"));

		sitesPane = new JPanel();
		sitesPane.setLayout(new BoxLayout(sitesPane, BoxLayout.PAGE_AXIS));

		horz.add(StyleManager.createScrollBorder("Sites", sitesPane));

		sitePanes = new HashMap<>();

		// Load the panels with current state
		updateCollectionValueLabel();
		
		objective.getCompletion().keySet().forEach(this::updateSitePanel);
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.INVENTORY_RESOURCE_EVENT == event.getType()) {
			updateCollectionValueLabel();
		}
	}

	@Override
	public void missionUpdate(EntityEvent e) {
		if (EntityEventType.MISSION_SITE_EXPLORATION_EVENT.equals(e.getType())) {
			updateSitePanel((String) e.getTarget());
		}
	}

	private void updateSitePanel(String siteName) {
		var result = sitePanes.get(siteName);
		if (result == null) {
			result = new ExplorationSitePanel(siteName, 0D);
			sitesPane.add(result);

			sitePanes.put(siteName, result);
		}
		
		double completion = objective.getCompletion().getOrDefault(siteName, 0D);
		result.updateCompletion(completion);
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
	}
	
	/**
	 * Inner class panel for displaying exploration site info.
	 */
	private class ExplorationSitePanel extends JPanel {

		private JProgressBar completionBar;

		/**
		 * Constructor.
		 * 
		 * @param siteName the site name.
		 * @param completion the completion level.
		 */
		ExplorationSitePanel(String siteName, double completion) {
			// Use JPanel constructor.
			super();

			setLayout(new GridLayout(1, 2, 10, 10));

			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			namePanel.setAlignmentX(CENTER_ALIGNMENT);
			namePanel.setAlignmentY(CENTER_ALIGNMENT);
			add(namePanel);

			JLabel nameLabel = new JLabel(siteName, SwingConstants.RIGHT);
			nameLabel.setAlignmentX(CENTER_ALIGNMENT);
			nameLabel.setAlignmentY(CENTER_ALIGNMENT);
			namePanel.add(nameLabel);

			JPanel barPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
			barPanel.setAlignmentX(CENTER_ALIGNMENT);
			barPanel.setAlignmentY(CENTER_ALIGNMENT);
			add(barPanel);

			completionBar = new JProgressBar(0, 100);
			completionBar.setAlignmentX(CENTER_ALIGNMENT);
			completionBar.setAlignmentY(CENTER_ALIGNMENT);
			completionBar.setStringPainted(true);
			barPanel.add(completionBar);

			updateCompletion(completion);
		}

		/**
		 * Updates the completion.
		 * 
		 * @param completion the site completion level.
		 */
		void updateCompletion(double completion) {
			completionBar.setValue((int) (completion * 100D));
		}
	}
}
