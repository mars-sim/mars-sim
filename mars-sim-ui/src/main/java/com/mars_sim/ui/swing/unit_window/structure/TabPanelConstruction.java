/*
 * Mars Simulation Project
 * TabPanelConstruction.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.construction.ConstructionConfig;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionManager.BuildingSchedule;
import com.mars_sim.core.building.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.MarsTimeTableCellRenderer;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

@SuppressWarnings("serial")
public class TabPanelConstruction extends TabPanel {

	private static final String CONST_ICON = "construction";
	
	// Data members
	/** The Settlement instance. */
	private Settlement settlement;
	private ConstructionSitesPanel sitesPanel;
	private QueueTableModel queue;
	private JCheckBox overrideCheckbox;

	private JButton deleteButton;

	private JComboBox<String> buildingChoice;

	private JTable queueTable;

	private ConstructionConfig cConfig;

	/**
	 * Constructor.
	 * @param unit the unit the tab panel is for.
	 * @param desktop the desktop.
	 */
	public TabPanelConstruction(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelConstruction.title"), //-NLS-1$
			ImageLoader.getIconByName(CONST_ICON),
			Msg.getString("TabPanelConstruction.title"), //-NLS-1$
			desktop
		);

		settlement = (Settlement) unit;
		cConfig = desktop.getSimulation().getConfig().getConstructionConfiguration();
	}
	
	@Override
	protected void buildUI(JPanel content) {
		
		ConstructionManager manager = settlement.getConstructionManager();

		// Create override panel.
		JPanel overridePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		content.add(overridePanel, BorderLayout.NORTH);

		// Create override check box.
		overrideCheckbox = new JCheckBox(Msg.getString("TabPanelConstruction.checkbox.overrideConstructionAndSalvage")); //-NLS-1$
		overrideCheckbox.setToolTipText(Msg.getString("TabPanelConstruction.tooltip.overrideConstructionAndSalvage")); //-NLS-1$
		overrideCheckbox.addActionListener(a -> 
				settlement.setProcessOverride(OverrideType.CONSTRUCTION, overrideCheckbox.isSelected()));
		overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.CONSTRUCTION));
		overridePanel.add(overrideCheckbox);
		
		JPanel mainContentPanel = new JPanel(new GridLayout(2, 1));
		content.add(mainContentPanel, BorderLayout.CENTER);

		sitesPanel = new ConstructionSitesPanel(manager);
		mainContentPanel.add(sitesPanel);

		var queuePanel = new JPanel();
		queuePanel.setLayout(new BorderLayout());
		queuePanel.setBorder(StyleManager.createLabelBorder("Construction Q"));
		mainContentPanel.add(queuePanel);

		var selectionPanel = new  JPanel(new BorderLayout());
		selectionPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		queuePanel.add(selectionPanel, BorderLayout.NORTH);

		// Create user building selection
		var label = new JLabel("New Building :");
		label.setFont(StyleManager.getLabelFont());
		selectionPanel.add(label, BorderLayout.WEST);
		
		// Create a combo showing add BUILDING stages
		buildingChoice = new JComboBox<>();
		cConfig.getConstructionStageInfoList(Stage.BUILDING).stream()
			.map(c-> c.getName())
			.sorted()
			.forEach(c -> buildingChoice.addItem(c));
		selectionPanel.add(buildingChoice, BorderLayout.CENTER);

		var buttonPanel = new JPanel(new GridLayout(1, 2));
		var addButton = new JButton(ImageLoader.getIconByName("action/add"));
		addButton.setToolTipText("Add building to the queue");
		addButton.addActionListener(event -> addBuildingToQueue());
		buttonPanel.add(addButton);

		deleteButton = new JButton(ImageLoader.getIconByName("action/delete"));
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText("Delete building from queue");
		deleteButton.addActionListener(event -> removeBuilding());
		buttonPanel.add(deleteButton);
		selectionPanel.add(buttonPanel, BorderLayout.EAST);

		queue = new QueueTableModel(manager.getBuildingSchedule());
		queueTable = new JTable(queue) {
			@Override
            public String getToolTipText(MouseEvent e) {
                return ToolTipTableModel.extractToolTip(e, this);
            }
		};
		queueTable.getColumnModel().getColumn(1).setCellRenderer(new MarsTimeTableCellRenderer());
		queueTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		queueTable.getSelectionModel().addListSelectionListener(this::queueItemSelected);

		var scrollPane =  new JScrollPane(queueTable);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		queuePanel.add(scrollPane, BorderLayout.CENTER);
	}

	private void queueItemSelected(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		deleteButton.setEnabled(!lsm.isSelectionEmpty());
	}

	private void addBuildingToQueue() {
		String selectedBuilding = (String) buildingChoice.getSelectedItem();
		settlement.getConstructionManager().addBuildingToQueue(selectedBuilding, null);

		updateQueueModel();
	}

	private void removeBuilding() {
		int selectedIdx = queueTable.getSelectedRow();
		if (selectedIdx >= 0) {
			var queueItem = queue.getValueAt(selectedIdx);
			settlement.getConstructionManager().removeBuildingFromQueue(queueItem);

			updateQueueModel();
		}
	}

	private void updateQueueModel() {
		queue.update(settlement.getConstructionManager().getBuildingSchedule());
	}

	@Override
	public void update() {
		sitesPanel.update();
		updateQueueModel();

		// Update construction override check box if necessary.
		if (settlement.getProcessOverride(OverrideType.CONSTRUCTION) != overrideCheckbox.isSelected()) 
			overrideCheckbox.setSelected(settlement.getProcessOverride(OverrideType.CONSTRUCTION));
	}

	private class QueueTableModel extends AbstractTableModel
			implements ToolTipTableModel {
				
		private List<BuildingSchedule> queue;

		public QueueTableModel(List<BuildingSchedule> buildingSchedule) {
			this.queue = new ArrayList<>(buildingSchedule);
		}

		public void update(List<BuildingSchedule> buildingSchedule) {
			if (queue.size() != buildingSchedule.size()) {
				queue = new ArrayList<>(buildingSchedule);
				fireTableDataChanged();
			}
		}

		@Override
		public int getRowCount() {
			return queue.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		public BuildingSchedule getValueAt(int rowIndex) {
			return queue.get(rowIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var row = queue.get(rowIndex);

			return switch(columnIndex) {
				case 0 -> row.getBuildingType();
				case 1 -> row.getStart();
				default -> null;
			};
		}

		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case 0 -> "Building";
				case 1 -> "Start on";
				default -> null;
			};
		}

		@Override
		public String getToolTipAt(int row, int col) {
			var selected = getValueAt(row);
			var stageInfo = cConfig.getConstructionStageInfoByName(selected.getBuildingType());

			return ConstructionStageFormat.getTooltip(stageInfo);
		}
	}
}
