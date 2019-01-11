/**
 * Mars Simulation Project
 * BuildingPanelMaintenance.java
 * @version 3.1.0 2017-09-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import javax.swing.*;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

/**
 * The BuildingPanelMaintenance class is a building function panel representing
 * the maintenance state of a settlement building.
 */
public class BuildingPanelMaintenance
extends BuildingFunctionPanel {

	/** The malfunctionable building. */
	private Malfunctionable malfunctionable;
	/** Cached value for the wear condition. */
	private int wearConditionCache;
	/** The wear condition label. */
	private JLabel wearConditionLabel;
	/** The last completed label. */
	private JLabel lastCompletedLabel;
	/** The progress bar model. */
	private BoundedRangeModel progressBarModel;
	/** The time since last completed maintenance. */
	private int lastCompletedTime;
	/** Label for parts. */
	private JLabel partsLabel;

	/**
	 * Constructor.
	 * @param malfunctionable the malfunctionable building the panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelMaintenance(Malfunctionable malfunctionable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super((Building) malfunctionable, desktop);

		// Initialize data members.
		this.malfunctionable = malfunctionable;
		MalfunctionManager manager = malfunctionable.getMalfunctionManager();

		// Set the layout
		setLayout(new GridLayout(6, 1, 0, 0));

		// Create maintenance label.
		// 2014-11-21 Changed font type, size and color and label text
		// 2014-11-21 Added internationalization for the three labels
		JLabel maintenanceLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.title"), JLabel.CENTER);
		maintenanceLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//maintenanceLabel.setForeground(new Color(102, 51, 0)); // dark brown
		add(maintenanceLabel);

		// Create wear condition label.
		int wearConditionCache = (int) Math.round(manager.getWearCondition());
		wearConditionLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.wearCondition",
				wearConditionCache), JLabel.CENTER);
		wearConditionLabel.setToolTipText(Msg.getString("BuildingPanelMaintenance.wear.toolTip"));
		add(wearConditionLabel);

		// Create lastCompletedLabel.
		lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
		lastCompletedLabel = new JLabel(Msg.getString("BuildingPanelMaintenance.lastCompleted",
				lastCompletedTime), JLabel.CENTER);
		add(lastCompletedLabel);

		// Create maintenance progress bar panel.
		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		add(progressPanel);
		progressPanel.setOpaque(false);
		progressPanel.setBackground(new Color(0,0,0,128));

		// Prepare progress bar.
		JProgressBar progressBar = new JProgressBar();
		progressBarModel = progressBar.getModel();
		progressBar.setStringPainted(true);
		progressPanel.add(progressBar);

		// Set initial value for progress bar.
		double completed = manager.getMaintenanceWorkTimeCompleted();
		double total = manager.getMaintenanceWorkTime();
		int percentDone = (int) (100D * (completed / total));
		progressBarModel.setValue(percentDone);

		// Prepare maintenance parts label.
		partsLabel = new JLabel(getPartsString(false), JLabel.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		add(partsLabel);

	}

	/**
	 * Update this panel
	 */
	public void update() {

		MalfunctionManager manager = malfunctionable.getMalfunctionManager();

		// Update the wear condition label.
		int wearCondition = (int) Math.round(manager.getWearCondition());
		if (wearCondition != wearConditionCache) {
			wearConditionCache = wearCondition;
			wearConditionLabel.setText(Msg.getString("BuildingPanelMaintenance.wearCondition",
					wearConditionCache));
		}

		// Update last completed label.
		int lastComplete = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
		if (lastComplete != lastCompletedTime) {
			lastCompletedTime = lastComplete;
			lastCompletedLabel.setText(Msg.getString("BuildingPanelMaintenance.lastCompleted",
					lastCompletedTime));
		}

		// Update tool tip.
		lastCompletedLabel.setToolTipText(getToolTipString());

		// Update progress bar.
		double completed = manager.getMaintenanceWorkTimeCompleted();
		double total = manager.getMaintenanceWorkTime();
		int percentDone = (int) (100D * (completed / total));
		progressBarModel.setValue(percentDone);

	      // Update parts label.
        partsLabel.setText(getPartsString(false));
        // Update tool tip.
		partsLabel.setToolTipText("<html>" + getPartsString(true) + "</html>");

	}

	/**
	 * Gets the parts string.
	 * @return string.
	 */
	// 2015-03-06 Reformatted part list and capitalized part.getName()
	private String getPartsString(boolean useHtml) {
		StringBuilder buf = new StringBuilder("Needed Parts: ");

		Map<Integer, Integer> parts = malfunctionable.getMalfunctionManager().getMaintenanceParts();
		if (parts.size() > 0) {
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int number = parts.get(part);
				// 2014-11-21 Capitalized part.getName()
				if (useHtml) buf.append("<br>");
				buf.append(number).append(" ")
					.append(Conversion.capitalize(ItemResourceUtil.findItemResource(part).getName()));
				if (i.hasNext()) buf.append(", ");
				else {
					buf.append(".");
					if (useHtml) buf.append("<br>");
				}
			}
		}
		else buf.append("None.");

		return buf.toString();
	}

	/**
	 * Creates multi-line tool tip text.
	 */
	private String getToolTipString() {
		StringBuilder result = new StringBuilder("<html>");
		result.append("The Very Last Maintenance Was Completed ").append(lastCompletedTime).append(" Sols Ago<br>");
		result.append("</html>");
		return result.toString();
	}


}