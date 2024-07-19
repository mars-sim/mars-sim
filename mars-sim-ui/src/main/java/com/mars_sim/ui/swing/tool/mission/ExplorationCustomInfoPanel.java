/*
 * Mars Simulation Project
 * ExplorationCustomInfoPanel.java
 * @date 2024-07-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionEventType;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.AttributePanel;


/**
 * A panel for displaying exploration mission information.
 */
@SuppressWarnings("serial")
public class ExplorationCustomInfoPanel
extends MissionCustomInfoPanel implements UnitListener  {

	// Data members
	private Set<AmountResource> resourcesCollected = new HashSet<>();
	private Map<String, ExplorationSitePanel> sitePanes;
	
	private JLabel[] amountLabels = null;
	private Box mainPane;
	
	private Rover missionRover;
	private Exploration mission;
	

	/**
	 * Constructor.
	 */
	public ExplorationCustomInfoPanel(int [] resourceIds) {
		// Use JPanel constructor
		super();

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new GridLayout(2, 1));
		add(topPanel, BorderLayout.NORTH);
		
		// Create the main scroll panel.
		AttributePanel collectionPanel = new AttributePanel(resourceIds.length/2, 2);
		collectionPanel.setBorder(StyleManager.createLabelBorder("Resource Collected"));
		topPanel.add(collectionPanel);
				
		amountLabels = new JLabel[resourceIds.length];
		for (int i=0; i<resourceIds.length; i++) {
			AmountResource ar = ResourceUtil.findAmountResource(resourceIds[i]);
			resourcesCollected.add(ar);
			amountLabels[i] = collectionPanel.addRow(ar.getName(), StyleManager.DECIMAL_KG.format(0D));
		}
		
		JPanel sitePanel = new JPanel(new BorderLayout(10, 10));
		sitePanel.setBorder(StyleManager.createLabelBorder("Site Exploration"));
		topPanel.add(sitePanel);

		JScrollPane mainScrollPane = new JScrollPane();
		sitePanel.add(mainScrollPane, BorderLayout.CENTER);

		// Create main panel.
		mainPane = Box.createVerticalBox();
		mainScrollPane.setViewportView(mainPane);

		sitePanes = new HashMap<>(5);
	}

	@Override
	public void updateMission(Mission mission) {
		if (mission instanceof Exploration ex) {
			
			if (!mission.equals(this.mission)) {
				
				// Set the mission and mission rover.
				this.mission = ex;
	
				// Remove as unit listener to any existing rovers.
				if (missionRover != null) {
					missionRover.removeUnitListener(this);
				}

				if (this.mission.getRover() != null) {
					missionRover = this.mission.getRover();
					// Register as unit listener for mission rover.
					missionRover.addUnitListener(this);
				}

				// Update the collection value label.
				updateCollectionValueLabel();
				
				// Clear site panels.
				sitePanes.clear();
				mainPane.removeAll();

				// Create new site panels.
				Map<String, Double> explorationSites = this.mission.getExplorationSiteCompletion();
				TreeSet<String> treeSet = new TreeSet<>(explorationSites.keySet());
				Iterator<String> i = treeSet.iterator();
				while (i.hasNext()) {
					String siteName = i.next();
					double completion = explorationSites.get(siteName);
					ExplorationSitePanel panel = new ExplorationSitePanel(siteName, completion);
					sitePanes.put(siteName, panel);
					mainPane.add(panel);
				}

				mainPane.add(Box.createVerticalGlue());
		
				repaint();
			}
			else {
				
				// Update the collection value label.
				updateCollectionValueLabel();
				
				// Update existing site completion levels.
				Map<String, Double> explorationSites = this.mission.getExplorationSiteCompletion();
				TreeSet<String> treeSet = new TreeSet<>(explorationSites.keySet());
				Iterator<String> i = treeSet.iterator();
				while (i.hasNext()) {
					String siteName = i.next();
					double completion = explorationSites.get(siteName);
					if (sitePanes.containsKey(siteName)) {
						sitePanes.get(siteName).updateCompletion(completion);
					}
				}
			}
		}
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		if (MissionEventType.SITE_EXPLORATION_EVENT == e.getType()) {
			Exploration mission = (Exploration) e.getSource();
			String siteName = (String) e.getTarget();
			double completion = mission.getExplorationSiteCompletion().get(siteName);
			if (sitePanes.containsKey(siteName)) {
				sitePanes.get(siteName).updateCompletion(completion);
			}
		}
	}

	@Override
	public void unitUpdate(UnitEvent event) {
		if (UnitEventType.INVENTORY_RESOURCE_EVENT == event.getType()) {
			Object source = event.getTarget();
			if (source instanceof AmountResource) {
				if (resourcesCollected.contains(source)){
					updateCollectionValueLabel();
				}
			}
		}
	}

	/**
	 * Updates the collection value label.
	 */
	private void updateCollectionValueLabel() {

		Map<Integer, Double> collected = mission.getCumulativeCollectedByID();

		int i = 0;
		for (AmountResource resourceId : resourcesCollected) {
			double amount = collected.getOrDefault(resourceId.getID(), 0D);
			amountLabels[i++].setText(StyleManager.DECIMAL_KG.format(amount));
		}
	}
	
	/**
	 * Inner class panel for displaying exploration site info.
	 */
	private class ExplorationSitePanel
	extends JPanel {

		// Data members
		private double completion;
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

			this.completion = completion;

			setLayout(new GridLayout(1, 2, 10, 10));

			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
			namePanel.setAlignmentX(CENTER_ALIGNMENT);
			namePanel.setAlignmentY(CENTER_ALIGNMENT);
			add(namePanel);

			JLabel nameLabel = new JLabel("  " + Conversion.capitalize(siteName), SwingConstants.RIGHT);
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
			completionBar.setValue((int) (completion * 100D));
			barPanel.add(completionBar);
		}

		/**
		 * Updates the completion.
		 * 
		 * @param completion the site completion level.
		 */
		void updateCompletion(double completion) {
			if (this.completion != completion) {
				this.completion = completion;
				completionBar.setValue((int) (completion * 100D));
			}
		}
	}
}
