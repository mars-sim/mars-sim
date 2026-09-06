/**
 * Mars Simulation Project
 * RobotMapLayer.java
 * @date 2023-11-06
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.UIConfig;

/**
 * A settlement map layer for displaying Robots.
 */
public class RobotMapLayer extends WorkerMapLayer<Robot> {
	private static final String ROBOT_LABELS_PROP = "ROBOT_LABELS";

	private static final ColorChoice ROBOT_UNSELECTED = new ColorChoice(new Color(85, 77, 0), Color.white);
	private static final ColorChoice ROBOT_SELECTED = new ColorChoice(new Color(196, 178, 71), Color.white);

	// Data members
	private SettlementMapPanel mapPanel;
	private boolean showLabel;

	/**
	 * Constructor.
	 * 
	 * @param mapPanel the settlement map panel.
	 */
	public RobotMapLayer(SettlementMapPanel mapPanel, Properties userSettings) {
		// Initialize data members.
		this.mapPanel = mapPanel;		
		this.showLabel = UIConfig.extractBoolean(userSettings, ROBOT_LABELS_PROP, false);
	}


	@Override
	public Collection<? extends MapHotspot<?>> displayLayer(Settlement settlement, MapViewPoint viewpoint) {

		Collection<Robot> robots = CollectionUtils.getAssociatedRobotsInSettlementVicinity(settlement);
		Robot selectedRobot = mapPanel.getSelectedRobot();

		return drawWorkers(robots, selectedRobot, showLabel, viewpoint);
	}

	@Override
	public List<JMenuItem> getFilterControls() {
		return List.of(createDisplayToggle("robot_labels", showLabel,
				selected -> {
					showLabel = selected;
					mapPanel.repaint();
					return null;
				}));
	}

	@Override
	public void saveUIProperties(Properties props) {
		props.setProperty(ROBOT_LABELS_PROP, Boolean.toString(showLabel));
	}

	/**
	 * Identifies the best colour to render this Robot in the Settlement Map.
	 * 
	 * @param r Robot
	 * @param selected Are they selected
	 * @return
	 */
	@Override
    protected ColorChoice getColor(Robot r, boolean selected) {
		return (selected ? ROBOT_SELECTED : ROBOT_UNSELECTED);
	}
}
