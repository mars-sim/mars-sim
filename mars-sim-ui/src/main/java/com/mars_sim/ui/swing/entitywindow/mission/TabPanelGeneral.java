/*
 * Mars Simulation Project
 * GeneralTabPanel.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
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

		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
		attributePanel.setBorder(SwingHelper.createLabelBorder("Details"));
		content.add(attributePanel, BorderLayout.NORTH);
		
		var mission = getEntity();
		phaseTextField = attributePanel.addTextField(Msg.getString("mission.phase"), "", null);
		designationTextField = attributePanel.addTextField(Msg.getString("mission.designation"), "",null);

		var context = getContext();
		attributePanel.addLabelledItem(Msg.getString("mission.leader"), new EntityLabel(mission.getStartingPerson(), context));
		statusTextField = attributePanel.addTextField(Msg.getString("mission.status"), "", null);

		content.add(initLogPane(mission), BorderLayout.CENTER);
		
		updateFields(mission);
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
			updateFields(m);
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