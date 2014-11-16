/**
 * Mars Simulation Project
 * ConstructionMissionCustomInfoPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.construction.ConstructionEvent;
import org.mars_sim.msp.core.structure.construction.ConstructionListener;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A panel for displaying construction custom mission information.
 * TODO externalize strings for internationalization
 */
public class ConstructionMissionCustomInfoPanel
extends MissionCustomInfoPanel 
implements ConstructionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private MainDesktopPane desktop;
	private BuildingConstructionMission mission;
	private ConstructionSite site;
	private JLabel stageLabel;
	private BoundedRangeModel progressBarModel;
	private JButton settlementButton;

	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public ConstructionMissionCustomInfoPanel(MainDesktopPane desktop) {
		// Use MissionCustomInfoPanel constructor.
		super();

		// Initialize data members.
		this.desktop = desktop;

		// Set layout.
		setLayout(new BorderLayout());

		JPanel contentsPanel = new JPanel(new GridLayout(4, 1));
		add(contentsPanel, BorderLayout.NORTH);

		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contentsPanel.add(titlePanel);

		JLabel titleLabel = new JLabel("Building Construction Site");
		titlePanel.add(titleLabel);

		JPanel settlementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentsPanel.add(settlementPanel);

		JLabel settlementLabel = new JLabel("Settlement: ");
		settlementPanel.add(settlementLabel);

		settlementButton = new JButton("   ");
		settlementPanel.add(settlementButton);
		settlementButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mission != null) {
					Settlement settlement = mission.getAssociatedSettlement();
					if (settlement != null) getDesktop().openUnitWindow(settlement, false);
				}
			}
		});

		JPanel stagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentsPanel.add(stagePanel);

		stageLabel = new JLabel("Stage:");
		stagePanel.add(stageLabel);

		JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		contentsPanel.add(progressBarPanel);

		JProgressBar progressBar = new JProgressBar();
		progressBarModel = progressBar.getModel();
		progressBar.setStringPainted(true);
		progressBarPanel.add(progressBar);

		// Add tooltip.
		setToolTipText(getToolTipString());
	}

	@Override
	public void updateMission(Mission mission) {
		// Remove as construction listener if necessary.
		if (site != null) site.removeConstructionListener(this);

		if (mission instanceof BuildingConstructionMission) {
			this.mission = (BuildingConstructionMission) mission;
			site = this.mission.getConstructionSite();
			if (site != null) site.addConstructionListener(this);

			settlementButton.setText(mission.getAssociatedSettlement().getName());
			stageLabel.setText(getStageString());
			updateProgressBar();

			// Update the tool tip string.
			setToolTipText(getToolTipString());
		}
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		stageLabel.setText(getStageString());
	}

	/**
	 * Catch construction update event.
	 * @param event the mission event.
	 */
	public void constructionUpdate(ConstructionEvent event) {
		if (ConstructionStage.ADD_CONSTRUCTION_WORK_EVENT.equals(event.getType())) {
			updateProgressBar();

			// Update the tool tip string.
			setToolTipText(getToolTipString());
		}
	}

	/**
	 * Gets the stage label string.
	 * @return stage string.
	 */
	private String getStageString() {
		StringBuilder stageString = new StringBuilder("Stage: ");
		if (mission != null) {
			ConstructionStage stage = mission.getConstructionStage();
			if (stage != null) stageString.append(stage.getInfo().getName());
		}

		return stageString.toString();
	}

	/**
	 * Updates the progress bar.
	 */
	private void updateProgressBar() {
		int workProgress = 0;
		if (mission != null) {
			ConstructionStage stage = mission.getConstructionStage();
			if (stage != null) {
				double completedWork = stage.getCompletedWorkTime();
				double requiredWork = stage.getRequiredWorkTime();
				if (requiredWork > 0D) workProgress = (int) (100D * completedWork / requiredWork);
			}
		}
		progressBarModel.setValue(workProgress);
	}

	/**
	 * Gets the main desktop.
	 * @return desktop.
	 */
	private MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Gets a tool tip string for the panel.
	 */
	private String getToolTipString() {
		StringBuilder result = new StringBuilder(Msg.HTML_START);

		ConstructionStage stage = null;
		if (site != null) stage = site.getCurrentConstructionStage();
		if (stage != null) {
			ConstructionStageInfo info = stage.getInfo();
			result.append("Status: building ").append(info.getName()).append(Msg.BR);
			result.append("Stage Type: ").append(info.getType()).append(Msg.BR);
			if (stage.isSalvaging()) result.append("Work Type: salvage").append(Msg.BR);
			else result.append("Work Type: Construction").append(Msg.BR);
			DecimalFormat formatter = new DecimalFormat("0.0");
			String requiredWorkTime = formatter.format(stage.getRequiredWorkTime() / 1000D);
			result.append("Work Time Required: ").append(requiredWorkTime).append(" Sols").append(Msg.BR);
			String completedWorkTime = formatter.format(stage.getCompletedWorkTime() / 1000D);
			result.append("Work Time Completed: ").append(completedWorkTime).append(" Sols").append(Msg.BR);
			result.append("Architect Construction Skill Required: ").append(info.getArchitectConstructionSkill()).append(Msg.BR);

			// Add construction resources.
			if (info.getResources().size() > 0) {
				result.append(Msg.BR).append("Construction Resources:").append(Msg.BR);
				Iterator<AmountResource> i = info.getResources().keySet().iterator();
				while (i.hasNext()) {
					AmountResource resource = i.next();
					double amount = info.getResources().get(resource);
					result.append(Msg.NBSP).append(Msg.NBSP).append(resource.getName()).append(": ").append(amount).append(" kg").append(Msg.BR);
				}
			}

			// Add construction parts.
			if (info.getParts().size() > 0) {
				result.append(Msg.BR).append("Construction Parts:").append(Msg.BR);
				Iterator<Part> j = info.getParts().keySet().iterator();
				while (j.hasNext()) {
					Part part = j.next();
					int number = info.getParts().get(part);
					result.append(Msg.NBSP).append(Msg.NBSP).append(part.getName()).append(": ").append(number).append(Msg.BR);
				}
			}

			// Add construction vehicles.
			if (info.getVehicles().size() > 0) {
				result.append(Msg.BR).append("Construction Vehicles:").append(Msg.BR);
				Iterator<ConstructionVehicleType> k = info.getVehicles().iterator();
				while (k.hasNext()) {
					ConstructionVehicleType vehicle = k.next();
					result.append(Msg.NBSP).append(Msg.NBSP).append("Vehicle Type: ").append(vehicle.getVehicleType()).append(Msg.BR);
					result.append(Msg.NBSP).append(Msg.NBSP).append("Attachment Parts:").append(Msg.BR);
					Iterator<Part> l = vehicle.getAttachmentParts().iterator();
					while (l.hasNext()) {
						result.append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP).append(Msg.NBSP).append(l.next().getName()).append(Msg.BR);
					}
				}
			}
		}

		result.append(Msg.HTML_STOP);

		return result.toString();
	}
}