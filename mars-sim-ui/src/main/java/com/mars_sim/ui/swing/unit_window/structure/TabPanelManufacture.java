/*
 * Mars Simulation Project
 * TabPanelManufacture.java
 * @date 2024-09-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.manufacture.ManufacturingManager.QueuedProcess;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.ProcessInfoRenderer;
import com.mars_sim.ui.swing.utils.ProcessListPanel;
import com.mars_sim.ui.swing.utils.SalvagePanel;

/**
 * A tab panel displaying settlement manufacturing information.
 */
@SuppressWarnings("serial")
public class TabPanelManufacture extends TabPanel {
	
	private static final String MANU_ICON ="manufacture";
	private static final String BUTTON_TEXT = Msg.getString("TabPanelManufacture.button.createNewProcess"); // $NON-NLS-1$
	
	/** The Settlement instance. */
	private Settlement target;

	private ProcessQueueModel queueModel;
	private ProcessListPanel manufactureListPane;
	private JScrollPane manufactureScrollPane;

	/** Process selector. */
	private JComboBoxMW<ManufactureProcessInfo> processSelection;	

	/**
	 * Constructor.
	 * 
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelManufacture(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelManufacture.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(MANU_ICON),
			Msg.getString("TabPanelManufacture.title"), //$NON-NLS-1$
			unit, desktop
		);

		target = unit;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected void buildUI(JPanel content) {
		// Create scroll panel for manufacture list pane.
		manufactureScrollPane = new JScrollPane();
		// increase vertical mousewheel scrolling speed for this one
		manufactureScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		manufactureScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		manufactureScrollPane.setPreferredSize(new Dimension(220, 215));

		// Prepare manufacture outer list pane.
		JPanel manufactureOuterListPane = new JPanel(new BorderLayout(0, 0));
		manufactureScrollPane.setViewportView(manufactureOuterListPane);

		// Prepare manufacture list pane.
		manufactureListPane = new ProcessListPanel(true);
		manufactureOuterListPane.add(manufactureListPane, BorderLayout.NORTH);

		// Create the process panels.
		manufactureListPane.update(getActiveManufacturing(), getActiveSalvaging());
		
		// CReate tabbed pane and add Active
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Active", manufactureScrollPane);
		content.add(tabbedPane, BorderLayout.CENTER);

		// Create queue panel
		JPanel queuePanel = new JPanel(new BorderLayout());
		tabbedPane.addTab("Queue", queuePanel);

		// Create control panel.
		JPanel interactionPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		queuePanel.add(interactionPanel, BorderLayout.NORTH);

		// Create new manufacture process selection.
		var processSelectionCache = getAvailableProcesses();
		processSelection = new JComboBoxMW<>();
		processSelectionCache.forEach(p -> processSelection.addItem(p));
		processSelection.setSelectedIndex(-1);
		processSelection.setRenderer(new ManufactureSelectionListCellRenderer("Select a Process"));
		processSelection.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.selectAvailableProcess")); //$NON-NLS-1$
		interactionPanel.add(processSelection);

		// Add available salvage processes.
		var salvageSelectionCache = getAvailableSalvageProcesses();
		salvageSelectionCache.forEach(k -> processSelection.addItem(k));

		// Create new process button.
		var newProcessButton = new JButton(BUTTON_TEXT); //$NON-NLS-1$
		newProcessButton.setEnabled(false);
		newProcessButton.setToolTipText("Create a New Manufacturing Process or Salvage a Process"); //$NON-NLS-1$
		newProcessButton.addActionListener(event -> createNewProcess());
		interactionPanel.add(newProcessButton);

		// Link the enabled button to the process selection
		processSelection.addItemListener(event -> newProcessButton.setEnabled(event.getStateChange() == ItemEvent.SELECTED));

		// Create manufacturing override check box.
		var controlPanel = new JPanel();
		
		var overrideManuCheckbox = new JCheckBox(Msg.getString("TabPanelManufacture.checkbox.overrideManufacturing")); //$NON-NLS-1$
		overrideManuCheckbox.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.overrideManufacturing")); //$NON-NLS-1$
		overrideManuCheckbox.addActionListener(arg0 ->
						setOverride(OverrideType.MANUFACTURE, overrideManuCheckbox.isSelected()));
		overrideManuCheckbox.setSelected(target.getProcessOverride(OverrideType.MANUFACTURE));
		controlPanel.add(overrideManuCheckbox);
		
		// Create salvaging override check box.
		JCheckBox overrideSalvageCheckbox = new JCheckBox(Msg.getString("TabPanelManufacture.checkbox.overrideSalvaging")); //$NON-NLS-1$
		overrideSalvageCheckbox.setToolTipText(Msg.getString("TabPanelManufacture.tooltip.overrideSalvaging")); //$NON-NLS-1$
		overrideSalvageCheckbox.addActionListener(arg0 ->
						setOverride(OverrideType.SALVAGE, overrideSalvageCheckbox.isSelected()));
		overrideSalvageCheckbox.setSelected(target.getProcessOverride(OverrideType.SALVAGE));
		controlPanel.add(overrideSalvageCheckbox);
		interactionPanel.add(controlPanel);

		// Create 
		var scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		queuePanel.add(scrollPane,BorderLayout.CENTER);
		
		// Prepare table model.
		queueModel = new ProcessQueueModel();
		queueModel.update(target);
		
		// Prepare table.
		JTable table = new JTable(queueModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Add sorting
		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);
	}

	/**
	 * Creates a new process in a given building.
	 */
	private void createNewProcess() {

		Object selectedItem = processSelection.getSelectedItem();
		if (selectedItem instanceof ManufactureProcessInfo selectedProcess) {
			target.getManuManager().addManufacturing(selectedProcess);

			update();

		}
		processSelection.setSelectedIndex(-1);
	}


	@Override
	public void update() {

		// Update processes if necessary.
		manufactureListPane.update(getActiveManufacturing(), getActiveSalvaging());
		manufactureScrollPane.validate();

		queueModel.update(target);
	}

	/**
	 * Gets all the manufacture processes at the settlement.
	 * 
	 * @return list of manufacture processes.
	 */
	private List<ManufactureProcess> getActiveManufacturing() {
		List<ManufactureProcess> result = new ArrayList<>();

		for(var i : target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE)) {
			Manufacture manufacture = i.getManufacture();
			result.addAll(manufacture.getProcesses());
		}

		return result;
	}

	/**
	 * Gets all the salvage processes at the settlement.
	 * 
	 * @return list of salvage processes.
	 */
	private List<SalvageProcess> getActiveSalvaging() {
		List<SalvageProcess> result = new ArrayList<>();

		for (var i : target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE)) {	
			Manufacture manufacture = i.getManufacture();
			result.addAll(manufacture.getSalvageProcesses());
		}

		return result;
	}

	/**
	 * Gets all manufacturing processes available at Settlement
	 * 
	 * @return vector of processes.
	 */
	private List<ManufactureProcessInfo> getAvailableProcesses() {
		return target.getManuManager().getQueuableManuProcesses();
	}

	/**
	 * Gets all salvage processes available at the workshop.
	 * 
	 * @param manufactureBuilding the manufacturing building.
	 * @return vector of processes.
	 */
	private List<SalvageProcessInfo> getAvailableSalvageProcesses() {
		return Collections.emptyList();
	}

	/**
	 * Sets the settlement override flag.
	 * 
	 * @param override the override flag.
	 */
	private void setOverride(OverrideType type, boolean override) {
		target.setProcessOverride(type, override);
	}

	/**
	 * Model of the queued processes
	 */
	private static class ProcessQueueModel extends AbstractTableModel {

		private static final int NAME_COL = 0;
		private static final int PRIORITY_COL = 1;
		private static final int AVAILABLE_COL = 2;
		private static final int SALVAGE_COL = 3;
		private List<QueuedProcess> queue = Collections.emptyList();

		@Override
		public int getRowCount() {
			return queue.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			var item = queue.get(rowIndex);
			switch(columnIndex) {
				case NAME_COL: return item.getInfo().getName();
				case SALVAGE_COL:
					var target = item.getTarget();
					return (target != null ? target.getName() : null);
				case PRIORITY_COL: return item.getPriority();
				case AVAILABLE_COL: return item.isResourcesAvailable();
				default: return null;
			}
		}

		private void update(Settlement s) {
			queue = new ArrayList<>(s.getManuManager().getQueue());
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case NAME_COL -> "Process";
				case SALVAGE_COL -> "Salvage";
				case PRIORITY_COL -> "Pri.";
				case AVAILABLE_COL -> "Resources";
				default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch(column) {
				case NAME_COL, SALVAGE_COL -> String.class;
				case PRIORITY_COL -> Integer.class;
				case AVAILABLE_COL -> Boolean.class;
				default -> null;
			};
		}
	}

	/**
	 * Inner class for the manufacture selection list cell renderer.
	 */
	private static class ManufactureSelectionListCellRenderer extends DefaultListCellRenderer {

		private static final int PROCESS_NAME_LENGTH = 70;
		private String prompt;

		/*
		 * Set the text to display when no item has been selected.
		 */
		public ManufactureSelectionListCellRenderer(String prompt) {
			this.prompt = prompt;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				var pinfo = (ProcessInfo) value;
				String processName = pinfo.getName();
				if (processName.length() > PROCESS_NAME_LENGTH)
					processName = processName.substring(0, PROCESS_NAME_LENGTH)
							+ Msg.getString("TabPanelManufacture.cutOff"); //$NON-NLS-1$

				result.setText(processName);
				
				if (value instanceof ManufactureProcessInfo info) {
					result.setToolTipText(ProcessInfoRenderer.getToolTipString(info));
				} else if (value instanceof SalvageProcessInfo info) {
					result.setToolTipText(SalvagePanel.getToolTipString(null, info, null));
				}
			}
			else {
				setText(prompt);
			}
			return result;
		}
	}
}
