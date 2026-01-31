/*
 * Mars Simulation Project
 * GeneralTabPanel.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.AbstractMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionPlanning;
import com.mars_sim.core.person.ai.mission.PlanType;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The tab displays the general properties of an Authority.
 */
@SuppressWarnings("serial")
class TabPanelGeneral extends EntityTabPanel<Mission> implements EntityListener {
	
	private static final int MAX_PHASE_LENGTH = 30;
	private JLabel phaseTextField;
	private JLabel designationTextField;
	private JLabel statusTextField;
	private LogTableModel logTableModel;
	private JDoubleLabel planScore;
	private JLabel planStatus;
	private JProgressBar planPerc;
	private JButton approveButton;
	private JButton rejectButton;
	private EntityLabel planReviewer;

	/**
	 * Constructor.
	 * 
	 * @param mission the Mission.
	 * @param context the UI context.
	 */
	public TabPanelGeneral(Mission mission, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),
			GENERAL_TOOLTIP,
			context, mission
		);
	}

	@Override
	protected void buildUI(JPanel content) {

		var mission = getEntity();

		var detailsPane = initDetailsPane(mission);
		var logPane = initLogPane(mission);

		// Is there a mission planning
		if (mission.getPlan() != null) {
			var planPane = initPlanPane(mission.getPlan());

			// 3 panel so use a top pane to hold attributes and plan
			var topPane = new JPanel(new BorderLayout());
			topPane.add(detailsPane, BorderLayout.NORTH);
			topPane.add(planPane, BorderLayout.SOUTH);

			content.add(topPane, BorderLayout.NORTH);
		}
		else {
			// Just 2 panels: attributes at top, log at center.
			content.add(detailsPane, BorderLayout.NORTH);
		}
		content.add(logPane, BorderLayout.CENTER);
		
		updateFields(mission);
	}

	private JPanel initDetailsPane(Mission mission) {
		var pane = new JPanel(new BorderLayout());
		pane.setBorder(SwingHelper.createLabelBorder("Details"));

		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
		
		phaseTextField = attributePanel.addTextField(Msg.getString("mission.phase"), "", null);
		designationTextField = attributePanel.addTextField(Msg.getString("mission.designation"), "",null);

		var context = getContext();
		attributePanel.addLabelledItem(Msg.getString("mission.leader"), new EntityLabel(mission.getStartingPerson(), context));
		statusTextField = attributePanel.addTextField(Msg.getString("mission.status"), "", null);
		pane.add(attributePanel, BorderLayout.CENTER);

		// Approval buttons
		var buttonPane = new JPanel(new BorderLayout());
		pane.add(buttonPane, BorderLayout.EAST);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		var abortButton = new JButton(ImageLoader.getIconByName("action/cancel"));
		abortButton.setToolTipText("Abort mission");
		abortButton.addActionListener(e -> abortMission());
		buttonPane.add(abortButton, BorderLayout.NORTH);

		return pane;
	}

	private void abortMission() {
		var m = getEntity();
		m.abortMission(AbstractMission.MISSION_ABORTED_BY_PLAYER);
		updateFields(m);
	}

	private JPanel initPlanPane(MissionPlanning plan) {
		var pane = new JPanel(new BorderLayout());
		pane.setBorder(SwingHelper.createLabelBorder("Planning"));
		var planDetails = new AttributePanel();
		pane.add(planDetails, BorderLayout.CENTER);

		planStatus = planDetails.addTextField("Status", null, null); 
		planPerc = new JProgressBar(0, 100);
		planPerc.setStringPainted(true);
		planDetails.addLabelledItem("%age Complete", planPerc);
		planReviewer = new EntityLabel(getContext());
		planDetails.addLabelledItem("Reviewer", planReviewer);
		planScore = new JDoubleLabel(StyleManager.DECIMAL_PLACES1);
		planDetails.addLabelledItem("Score", planScore);
		planDetails.addLabelledItem("Passing Score", new JDoubleLabel(StyleManager.DECIMAL_PLACES1,
										plan.getPassingScore()));
		
		// Approval buttons
		var buttonPane = new JPanel(new GridLayout(2, 1));
		var outerPane = new JPanel(new BorderLayout());
		outerPane.add(buttonPane, BorderLayout.NORTH);
		pane.add(outerPane, BorderLayout.EAST);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		approveButton = new JButton(ImageLoader.getIconByName("action/approve"));
		approveButton.setToolTipText("Approve mission");
		approveButton.addActionListener(e -> reviewMission(true));
		buttonPane.add(approveButton);
		rejectButton = new JButton(ImageLoader.getIconByName("action/cancel"));
		rejectButton.setToolTipText("Reject mission");
		rejectButton.addActionListener(e -> reviewMission(false));
		buttonPane.add(rejectButton);

		updatePlanFields();

		return pane;
	}

	private void updatePlanFields() {
		var plan = getEntity().getPlan();
		planPerc.setValue(plan.getPercentComplete());
		planScore.setValue(plan.getScore());
		planStatus.setText(plan.getStatus().getName());
		planReviewer.setEntity(plan.getActiveReviewer());

		var active = plan.getStatus() != PlanType.APPROVED && plan.getStatus() != PlanType.NOT_APPROVED;
		approveButton.setEnabled(active);
		rejectButton.setEnabled(active);
	}

	/**
	 * Initializes the phase log pane.
	 * 
	 * @return
	 */
	private JComponent initLogPane(Mission mission) {

		// Create member table model.
		logTableModel = new LogTableModel(mission);

		// Create member table.
		JTable logTable = new JTable(logTableModel);
		logTable.getColumnModel().getColumn(0).setPreferredWidth(70);
		logTable.getColumnModel().getColumn(1).setPreferredWidth(90);
		logTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		
		return SwingHelper.createScrollBorder("Phase Log", logTable, new Dimension(200, 150));
	}

	/**
	 * Update the dynamic fields
	 * @param mission
	 */
	private void updateFields(Mission mission) {
		phaseTextField.setText(Conversion.trim(mission.getPhaseDescription(), MAX_PHASE_LENGTH));
		designationTextField.setText(mission.getFullMissionDesignation());

		var status = mission.getMissionStatus().stream()
				.map(s -> s.getName())
				.collect(Collectors.joining(", "));
		statusTextField.setText(status);

		logTableModel.update();
	}

	@Override
	public void entityUpdate(EntityEvent event) {
		if (event.getSource() instanceof Mission m && m.equals(getEntity())) {
			switch (event.getType()) {
				case MissionPlanning.PLAN_REVIEWER_EVENT,
						MissionPlanning.PLAN_STATE_EVENT -> updatePlanFields();
				default -> updateFields(m);
			}
		}
	}

	/**
	 * Approve a mission being review
	 * @param approved true to approve, false to not approve
	 */
	private void reviewMission(boolean approved) {
		var mission = getEntity();
		MissionPlanning plan = mission.getPlan();
		if ((plan != null) && plan.getStatus() == PlanType.PENDING) {
			getContext().getSimulation().getMissionManager().approveMissionPlan(plan, (approved ?
								PlanType.APPROVED : PlanType.NOT_APPROVED));
			updateFields(mission); // Force a full refresh
		}
	}
	/**
	 * Adapter for the mission log
	 */
	private static class LogTableModel extends AbstractTableModel {
		
		private Mission mission;
		private int lastSize = 0;
		private List<MissionLog.MissionLogEntry> entries;
	
		/**
		 * Constructor.
		 */
		private LogTableModel(Mission mission) {
			this.mission = mission;
			entries = new ArrayList<>();
			lastSize = 0;
		}

		public void update() {
			var newEntries = mission.getLog().getEntries();
			if (newEntries.size() != lastSize) {
				entries = newEntries;
				lastSize = newEntries.size();
				fireTableDataChanged();
			}
		}
		
		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		@Override
		public int getRowCount() {
			return entries.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		@Override
		public int getColumnCount() {
			return 3;
		}

		/**
		 * Gets the column name at a given index.
		 *
		 * @param columnIndex the column's index.
		 * @return the column name.
		 */
		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> "Date";
				case 1 -> "Entry";
				case 2 -> "Logged by";
				default -> null;
			};
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
				
			if (row < entries.size()) {
				return switch (column) {
					case 0 -> entries.get(row).getTime().getTruncatedDateTimeStamp();
					case 1 -> entries.get(row).getEntry();
					default -> entries.get(row).getEnterBy();
				};
			}
			return null;
		}
	}
}