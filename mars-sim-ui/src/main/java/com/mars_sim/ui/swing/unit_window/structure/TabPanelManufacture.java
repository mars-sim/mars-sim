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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufacturingManager.QueuedProcess;
import com.mars_sim.core.manufacture.ManufacturingParameters;
import com.mars_sim.core.manufacture.SalvageProcess;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.JComboBoxMW;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ProcessInfoRenderer;
import com.mars_sim.ui.swing.utils.ProcessListPanel;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;
import com.mars_sim.ui.swing.utils.SalvagePanel;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * A tab panel displaying settlement manufacturing information.
 */
@SuppressWarnings("serial")
public class TabPanelManufacture extends TabPanel {
	
	private static final String MANU_ICON ="manufacture";
	private static final String BUTTON_TEXT = Msg.getString("TabPanelManufacture.button.createNewProcess"); // -NLS-1$
	private static final String SALVAGE = "Salvage";

	/** The Settlement instance. */
	private Settlement target;

	private ProcessQueueModel queueModel;
	private ProcessListPanel manufactureListPane;
	private JScrollPane manufactureScrollPane;

	/** Process selector. */
	private JComboBoxMW<ManufactureProcessInfo> processSelection;	
	private JComboBoxMW<String> outputSelection;	


	/**
	 * Constructor.
	 * 
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelManufacture(Settlement unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelManufacture.title"), //-NLS-1$
			ImageLoader.getIconByName(MANU_ICON),
			Msg.getString("TabPanelManufacture.title"), //-NLS-1$
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
		JPanel interactionPanel = new JPanel();
		interactionPanel.setLayout(new BoxLayout(interactionPanel, BoxLayout.Y_AXIS));

		queuePanel.add(interactionPanel, BorderLayout.NORTH);

		// Create output selection
		outputSelection = new JComboBoxMW<>();
		interactionPanel.add(outputSelection);
		var outputs = target.getManuManager().getPossibleOutputs();
		outputSelection.addItem(SALVAGE);
		outputs.forEach(p -> outputSelection.addItem(p));
		outputSelection.addActionListener(e -> changeProcessOptions());

		// Create new manufacture process selection.
		processSelection = new JComboBoxMW<>();
		processSelection.setRenderer(new ManufactureSelectionListCellRenderer("Select a Process"));
		processSelection.addActionListener(this::processSelectionChanged);
		interactionPanel.add(processSelection);

		// Create new process button.
		var newProcessButton = new JButton(BUTTON_TEXT); //-NLS-1$
		newProcessButton.setEnabled(false);
		newProcessButton.setToolTipText("Create a New Manufacturing Process or Salvage a Process"); //-NLS-1$
		newProcessButton.addActionListener(event -> createNewProcess());
		interactionPanel.add(newProcessButton);

		// Link the enabled button to the process selection
		processSelection.addItemListener(event -> newProcessButton.setEnabled(event.getStateChange() == ItemEvent.SELECTED));

		// Create parameter controls
		var pMgr = target.getPreferences();
		var parameterPanel = new AttributePanel();
		addParameter(parameterPanel, pMgr, ManufacturingParameters.NEW_MANU_VALUE, 500);
		addParameter(parameterPanel, pMgr, ManufacturingParameters.NEW_MANU_LIMIT, 10);
		addParameter(parameterPanel, pMgr, ManufacturingParameters.MAX_QUEUE_SIZE, 20);
		interactionPanel.add(parameterPanel);

		// Create 
		var scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		queuePanel.add(scrollPane,BorderLayout.CENTER);
		
		// Prepare table model.
		queueModel = new ProcessQueueModel();
		queueModel.update(target);
		
		// Prepare table.
		JTable table = new JTable(queueModel) {
			@Override
			public String getToolTipText(MouseEvent e) {
				return ToolTipTableModel.extractToolTip(e, this);
			}
		};
		table.setPreferredScrollableViewportSize(new Dimension(225, -1));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		// Add sorting
		table.setAutoCreateRowSorter(true);

		scrollPane.setViewportView(table);

		changeProcessOptions(); // Trigger to populate 2nd drop down
	}

	private void addParameter(AttributePanel panel, ParameterManager pMgr, String parmId, int maxValue) {
		var spec = ManufacturingParameters.INSTANCE.getSpec(parmId);

		int value = pMgr.getIntValue(ManufacturingParameters.INSTANCE, parmId, -1);
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, 0, maxValue, 1));
		spinner.setValue(value);
		spinner.addChangeListener(e -> {
			var v = (Integer)((JSpinner)e.getSource()).getValue();
			pMgr.putValue(ManufacturingParameters.INSTANCE, parmId, v);
		});

		panel.addLabelledItem(spec.displayName(), spinner);
	}

	/**
	 * Change the new process selection based on the output
	 */
	private void changeProcessOptions() {
		processSelection.removeAllItems();

		List<? extends ProcessInfo> processes = null;
		String output = (String) outputSelection.getSelectedItem();
		if (SALVAGE.equals(output)) {
			processes = target.getManuManager().getQueuableSalvageProcesses();
		}
		else {
			processes = target.getManuManager().getQueuableManuProcesses(output);
		}
		processes.forEach(p -> processSelection.addItem(p));
	}

	/**
	 * Creates a new process in a given building.
	 */
	private void createNewProcess() {

		Object selectedItem = processSelection.getSelectedItem();
		if (selectedItem instanceof ProcessInfo selectedProcess) {
			target.getManuManager().addProcessToQueue(selectedProcess);

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
		return target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE).stream()
								.map(b -> b.getManufacture().getProcesses())
								.flatMap(Collection::stream)
								.toList();
	}

	/**
	 * Gets all the salvage processes at the settlement.
	 * 
	 * @return list of salvage processes.
	 */
	private List<SalvageProcess> getActiveSalvaging() {
		return target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE).stream()
								.map(b -> b.getManufacture().getSalvageProcesses())
								.flatMap(Collection::stream)
								.toList();
	}

	/**
	 * The process selection has changed so update values
	 * @param e
	 * @return
	 */
	private void processSelectionChanged(ActionEvent e) {
		ProcessInfo value =  (ProcessInfo)processSelection.getSelectedItem();

		String tip = null;
		if (value instanceof ManufactureProcessInfo info) {
			tip = ProcessInfoRenderer.getToolTipString(info);
		} else if (value instanceof SalvageProcessInfo info) {
			tip = SalvagePanel.getToolTipString(null, info, null);
		}

		processSelection.setToolTipText(tip);
	}

	/**
	 * Model of the queued processes
	 */
	private static class ProcessQueueModel extends AbstractTableModel
		implements ToolTipTableModel {

		private static final int NAME_COL = 0;
		private static final int VALUE_COL = 1;
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
				case VALUE_COL: return item.getValue().getScore();
				case AVAILABLE_COL: return item.isResourcesAvailable();
				default: return null;
			}
		}

		/**
		 * Returns a detailed breakdownof the value property
		 * @param rowIndex
		 * @param columnIndex
		 * @return
		 */
		@Override
		public String getToolTipAt(int rowIndex, int columnIndex)  {
			String result = null;
			if (columnIndex == VALUE_COL) {
				var item = queue.get(rowIndex);
				StringBuilder builder = new StringBuilder();
				builder.append("<html>");
				builder.append(RatingScoreRenderer.getHTMLFragment(item.getValue()));
				builder.append("</html>");
	
				result = builder.toString();
			}
			return result;
		}

		private void update(Settlement s) {
			queue = new ArrayList<>(s.getManuManager().getQueue());
			fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case NAME_COL -> "Process";
				case SALVAGE_COL -> "Target";
				case VALUE_COL -> "Value";
				case AVAILABLE_COL -> "Resources";
				default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch(column) {
				case NAME_COL, SALVAGE_COL -> String.class;
				case VALUE_COL -> Double.class;
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
			}
			else {
				setText(prompt);
			}
			return result;
		}
	}
}
