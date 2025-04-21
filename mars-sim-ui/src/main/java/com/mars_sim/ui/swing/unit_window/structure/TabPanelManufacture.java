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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.manufacture.ManufacturingManager;
import com.mars_sim.core.manufacture.ManufacturingManager.QueuedProcess;
import com.mars_sim.core.manufacture.ManufacturingParameters;
import com.mars_sim.core.manufacture.WorkshopProcess;
import com.mars_sim.core.parameter.ParameterManager;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.ProcessInfoRenderer;
import com.mars_sim.ui.swing.utils.ProcessListPanel;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;
import com.mars_sim.ui.swing.utils.ToolTipTableModel;

/**
 * A tab panel displaying settlement manufacturing information.
 */
@SuppressWarnings("serial")
public class TabPanelManufacture extends TabPanel implements UnitListener {
	
	private static final String MANU_ICON ="manufacture";
	private static final String SALVAGE = "Salvage";

	/** The Settlement instance. */
	private Settlement target;

	private ProcessQueueModel queueModel;
	private ProcessListPanel manufactureListPane;
	private JScrollPane manufactureScrollPane;

	/** Process selector. */
	private JComboBox<ProcessInfo> processSelection;	
	private JComboBox<String> outputSelection;

	// Selected item
	private JSpinner userBonusSpinner;
	private JButton deleteButton;
	private QueuedProcess selection;

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
		manufactureListPane.update(getActiveManufacturing());
		
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
		outputSelection = new JComboBox<>();
		interactionPanel.add(outputSelection);
		var outputs = target.getManuManager().getPossibleOutputs();
		outputSelection.addItem(SALVAGE);
		outputs.forEach(p -> outputSelection.addItem(p));
		outputSelection.addActionListener(e -> changeProcessOptions());

		// Create new manufacture process selection.
		var addPanel = new JPanel(new BorderLayout());
		interactionPanel.add(addPanel);

		processSelection = new JComboBox<>();
		processSelection.setRenderer(new ManufactureSelectionListCellRenderer());
		processSelection.addActionListener(this::processSelectionChanged);
		addPanel.add(processSelection, BorderLayout.CENTER);

		// Create new process button.
		var newProcessButton = new JButton(ImageLoader.getIconByName("action/add"));
		newProcessButton.setEnabled(false);
		newProcessButton.setToolTipText("Add a the Process to the queue"); //-NLS-1$
		newProcessButton.addActionListener(event -> createNewProcess());
		addPanel.add(newProcessButton, BorderLayout.EAST);

		// Link the enabled button to the process selection
		processSelection.addItemListener(event -> newProcessButton.setEnabled(event.getStateChange() == ItemEvent.SELECTED));

		// Create parameter controls
		var pMgr = target.getPreferences();
		var parameterPanel = new AttributePanel();
		parameterPanel.setBorder(StyleManager.createLabelBorder("Controls"));
		addParameter(parameterPanel, pMgr, ManufacturingParameters.NEW_MANU_VALUE, 500);
		addParameter(parameterPanel, pMgr, ManufacturingParameters.NEW_MANU_LIMIT, 10);
		addParameter(parameterPanel, pMgr, ManufacturingParameters.MAX_QUEUE_SIZE, 20);
		interactionPanel.add(parameterPanel);

		// Row selection items
		var selectionPanel = new  JPanel(new BorderLayout());
		interactionPanel.add(selectionPanel);

		// Create user bonus spinner
		var spinLabel = new JLabel("User Bonus % :");
		spinLabel.setFont(StyleManager.getLabelFont());
		selectionPanel.add(spinLabel, BorderLayout.WEST);
		userBonusSpinner = new JSpinner(new SpinnerNumberModel(0, -100, 100, 1));
		userBonusSpinner.setEnabled(false);
		userBonusSpinner.addChangeListener(e -> {
			var value = (Integer)userBonusSpinner.getValue();
			queueModel.updateUserBonus(selection, value);
		});
		selectionPanel.add(userBonusSpinner, BorderLayout.CENTER);
		deleteButton = new JButton(ImageLoader.getIconByName("action/delete"));
		deleteButton.setEnabled(false);
		deleteButton.setToolTipText("Delete Process from the queue");
		deleteButton.addActionListener(event -> queueSelectionDeleted());
		selectionPanel.add(deleteButton, BorderLayout.EAST);

		// Create Table
		var scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		queuePanel.add(scrollPane,BorderLayout.CENTER);
		
		// Prepare table model.
		queueModel = new ProcessQueueModel(target);
		
		// Prepare table.
		var queueTable = new JTable(queueModel) {
			@Override
			public String getToolTipText(MouseEvent e) {
				return ToolTipTableModel.extractToolTip(e, this);
			}
		};
		queueTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		queueTable.getSelectionModel().addListSelectionListener(this::queueSelectionChanged);

		queueTable.setAutoCreateRowSorter(true);
		scrollPane.setViewportView(queueTable);

		changeProcessOptions(); // Trigger to populate 2nd drop down

		// Listener for changes
		target.addUnitListener(this);
	}

	/**
	 * Queued process has been selected
	 * @param e List selection event
	 */
	private void queueSelectionChanged(ListSelectionEvent e) {
		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		selection = null;

		if (!lsm.isSelectionEmpty()) {
			int index = lsm.getMinSelectionIndex();
			selection = queueModel.queue.get(index);
		}

		deleteButton.setEnabled(selection != null);
		userBonusSpinner.setEnabled(selection != null);

		if (selection != null) {
			double bonus = selection.getValue().getModifiers().get(ManufacturingManager.USER_BONUS);
			userBonusSpinner.setValue((int)((bonus - 1) * 100D));
		}
	}

	/**
	 * Selected process deleted
	 */
	private void queueSelectionDeleted() {
		if (selection != null) {
			target.getManuManager().removeProcessFromQueue(selection);
		}
	}	

	/**
	 * Something changed on the Settlement and process any events for the manufacturing
	 * @param e
	 */
	@Override
	public void unitUpdate(UnitEvent e) {
		switch(e.getType()) {
			case MANU_QUEUE_ADD -> queueModel.addItem((QueuedProcess) e.getTarget());
			case MANU_QUEUE_REMOVE -> queueModel.removeItem((QueuedProcess) e.getTarget());
			case MANE_QUEUE_REFRESH -> queueModel.refresh();
			default -> { /* Ignore */ }
		}
	}

	@Override
	public void destroy() {
		target.removeUnitListener(this);
		super.destroy();
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
		}
		processSelection.setSelectedIndex(-1);
	}


	@Override
	public void update() {

		// Update processes if necessary.
		manufactureListPane.update(getActiveManufacturing());
		manufactureScrollPane.validate();
	}

	/**
	 * Gets all the manufacture processes at the settlement.
	 * 
	 * @return list of manufacture processes.
	 */
	private List<WorkshopProcess> getActiveManufacturing() {
		return target.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE).stream()
								.map(b -> b.getManufacture().getProcesses())
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
		if (value == null)
			return;
		String tip = ProcessInfoRenderer.getToolTipString(value);

		processSelection.setToolTipText(tip);
	}

	/**
	 * Model of the queued processes
	 */
	private static class ProcessQueueModel extends AbstractTableModel
		implements ToolTipTableModel {

		private static final int NAME_COL = 0;
		private static final int VALUE_COL = 1;
		private static final int AVAILABLE_COL = 3;
		private static final int BONUS_COL = 2;
		private List<QueuedProcess> queue = Collections.emptyList();

		private ProcessQueueModel(Settlement s) {
			queue = new ArrayList<>(s.getManuManager().getQueue());
		}

		private void addItem(QueuedProcess item) {
			queue.add(item);
			int idx = queue.size() - 1;
			fireTableRowsInserted(idx, idx);
		}

		private void removeItem(QueuedProcess item) {
			int idx = queue.indexOf(item);
			if (idx >= 0) {
				queue.remove(idx);
				fireTableRowsDeleted(idx, idx);
			}
		}

		private void refresh() {
			// Just update the cell values
			fireTableRowsUpdated(0, queue.size() - 1);
		}

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
				case BONUS_COL:
					return (int)((item.getValue().getModifiers().get(ManufacturingManager.USER_BONUS) - 1) * 100D);
				case VALUE_COL: return item.getValue().getScore();
				case AVAILABLE_COL: return item.isResourcesAvailable();
				default: return null;
			}
		}

		/**
		 * Update the user bonus value. Needs converting back to a ratio
		 */
		public void updateUserBonus(QueuedProcess item, int value) {
				var rowIndex = queue.indexOf(item);
				if (rowIndex < 0) return;
				item.getValue().addModifier(ManufacturingManager.USER_BONUS, 1D +
											(value/100D));
				fireTableRowsUpdated(rowIndex, rowIndex);  // Refresh the full row
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
			else if (columnIndex == NAME_COL) {
				var item = queue.get(rowIndex);
				result = item.getInfo().getName();
			}
			return result;
		}

		@Override
		public String getColumnName(int column) {
			return switch(column) {
				case NAME_COL -> "Process";
				case BONUS_COL -> "% Bonus";
				case VALUE_COL -> "Value";
				case AVAILABLE_COL -> "Resources";
				default -> null;
			};
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch(column) {
				case NAME_COL -> String.class;
				case VALUE_COL -> Double.class;
				case BONUS_COL -> Integer.class;
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

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				var pinfo = (ProcessInfo) value;
				String processName = pinfo.getName();
				if (processName.length() > PROCESS_NAME_LENGTH)
					processName = processName.substring(0, PROCESS_NAME_LENGTH) + "...";

				result.setText(processName);
			}
			return result;
		}
	}
}
