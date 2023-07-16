/*
 * Mars Simulation Project
 * ResupplyMissionEditingPanel.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mars.sim.mapdata.location.BoundedObject;
import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MarsTimeFormat;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.resupply.SupplyTableModel.SupplyItem;


/**
 * A panel for creating or editing a resupply mission.
 */
@SuppressWarnings("serial")
public class ResupplyMissionEditingPanel extends TransportItemEditingPanel {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ResupplyMissionEditingPanel.class.getName());
			
	private static final int MAX_FUTURE_ORBITS = 10;
	private static final int MAX_IMMIGRANTS = 48;
	private static final int MAX_BOTS = 48;

	// Data members
	private int num = 0;
	private String errorString = new String();
	private boolean validation_result = true;
	private Number[] quantity = new Number[100000];
	
	private JComboBox<Settlement> destinationCB;
	private JRadioButton arrivalDateRB;
	private JRadioButton solsUntilArrivalRB;
	private MartianSolComboBoxModel martianSolCBModel;
	private JLabel arrivalDateTitleLabel;
	private JLabel solsUntilArrivalLabel;
	private JLabel solLabel;
	private JLabel monthLabel;
	private JLabel orbitLabel;
	private JLabel errorLabel;
	private JSpinner solsUntilCB;

	private JComboBox<Integer> solCB;

	private JSpinner botsCB;

	private JSpinner immigrantsCB;
	private JComboBox<String> monthCB, orbitCB;
	private SupplyTableModel supplyTableModel;
	private JTable supplyTable;
	private JButton removeSupplyButton;

	private Resupply resupply;
	private NewTransportItemDialog newTransportItemDialog = null;
	private ModifyTransportItemDialog modifyTransportItemDialog = null;
    private MasterClock master;

	/** constructor. */
	public ResupplyMissionEditingPanel(Resupply resupply, ResupplyWindow resupplyWindow,
			ModifyTransportItemDialog modifyTransportItemDialog, NewTransportItemDialog newTransportItemDialog) {
		// User TransportItemEditingPanel constructor.
		super(resupply);

		// Initialize data members.
		this.resupply = resupply;
		this.newTransportItemDialog = newTransportItemDialog;
		this.modifyTransportItemDialog = modifyTransportItemDialog;
		this.master = resupplyWindow.getDesktop().getSimulation().getMasterClock();

		setBorder(new MarsPanelBorder());
		setLayout(new BorderLayout(0, 0));

		// Create top edit pane.
		JPanel topEditPane = new JPanel(new BorderLayout(10, 10));
		add(topEditPane, BorderLayout.NORTH);

		// Create destination pane.
		JPanel destinationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topEditPane.add(destinationPane, BorderLayout.NORTH);

		// Create destination title label.
		JLabel destinationTitleLabel = new JLabel("Destination : ");
		destinationPane.add(destinationTitleLabel);

		// Create destination combo box.
		UnitManager unitManager = resupplyWindow.getDesktop().getSimulation().getUnitManager();
		Vector<Settlement> settlements = new Vector<>(
				unitManager.getSettlements());
		Collections.sort(settlements);
		destinationCB = new JComboBox<>(settlements);
		if (resupply != null) {
			destinationCB.setSelectedItem(resupply.getSettlement());
		} else {
			// this.settlement = (Settlement) destinationCB.getSelectedItem();
		}
		destinationPane.add(destinationCB);

		// Create arrival date pane.
		JPanel arrivalDatePane = new JPanel(new GridLayout(4, 1, 10, 10));
		arrivalDatePane.setBorder(new TitledBorder("Arrival"));
		topEditPane.add(arrivalDatePane, BorderLayout.CENTER);

		// Create data type radio button group.
		ButtonGroup dateTypeRBGroup = new ButtonGroup();

		// Create arrival date selection pane.
		JPanel arrivalDateSelectionPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(arrivalDateSelectionPane);

		// Create arrival date radio button.
		arrivalDateRB = new JRadioButton();
		dateTypeRBGroup.add(arrivalDateRB);
		arrivalDateRB.setSelected(true);
		arrivalDateRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JRadioButton rb = (JRadioButton) evt.getSource();
				setEnableArrivalDatePane(rb.isSelected());
				setEnableTimeUntilArrivalPane(!rb.isSelected());
			}
		});
		arrivalDateSelectionPane.add(arrivalDateRB);

		// Create arrival date title label.
		arrivalDateTitleLabel = new JLabel("Arrival Date : ");
		arrivalDateSelectionPane.add(arrivalDateTitleLabel);
		
		// Get default resupply Martian time.
		MarsTime resupplyTime = null;
		if (resupply != null) {
			resupplyTime = resupply.getArrivalDate();
		} else {
			resupplyTime = master.getMarsTime();
			resupplyTime.addTime(ResupplyUtil.getAverageTransitTime() * 1000D);
		}
		
		martianSolCBModel = new MartianSolComboBoxModel(resupplyTime.getMonth(), resupplyTime.getOrbit());

		JPanel comboBoxPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		arrivalDateSelectionPane.add(comboBoxPane);
		
		// Create orbit label.
		orbitLabel = new JLabel("Orbit :", SwingConstants.CENTER);
		comboBoxPane.add(orbitLabel);

		// Create orbit combo box.
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumIntegerDigits(2);
		String[] orbitValues = new String[MAX_FUTURE_ORBITS];
		int startOrbit = resupplyTime.getOrbit();
		for (int x = 0; x < MAX_FUTURE_ORBITS; x++) {
			orbitValues[x] = formatter.format(1L * startOrbit + x);
		}
		orbitCB = new JComboBox<>(orbitValues);
		orbitCB.setSelectedItem(formatter.format(startOrbit));
		orbitCB.addActionListener(e -> {
			// Update the solCB based on orbit and month			
			martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
					Integer.parseInt((String) orbitCB.getSelectedItem()));
			// Remove error string
			errorString = null;
			errorLabel.setText(errorString);
			// Reenable Commit/Create button
			enableButton(true);
		});
		comboBoxPane.add(orbitCB);

		// Create month label.
		monthLabel = new JLabel("Month :", SwingConstants.CENTER);
		comboBoxPane.add(monthLabel);

		// Create month combo box.
		monthCB = new JComboBox<>(MarsTimeFormat.getMonthNames());
		monthCB.setSelectedItem(resupplyTime.getMonthName());
		monthCB.addActionListener(e -> {
			// Update the solCB based on orbit and month
			martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
						Integer.parseInt((String) orbitCB.getSelectedItem()));
			// Remove error string
			errorString = null;
			errorLabel.setText(errorString);
			// Reenable Commit/Create button
			enableButton(true);
		});
		comboBoxPane.add(monthCB);

		// Create sol label.
		solLabel = new JLabel("Sol :", SwingConstants.CENTER);
		comboBoxPane.add(solLabel);

		// Create sol combo box.
		solCB = new JComboBox<>(martianSolCBModel);
		solCB.setSelectedItem(resupplyTime.getSolOfMonth());
		comboBoxPane.add(solCB);

		// Create sol until arrival pane.
		JPanel solsUntilArrivalPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(solsUntilArrivalPane);

		// Create sol until arrival radio button.
		solsUntilArrivalRB = new JRadioButton();
		solsUntilArrivalRB.setSelected(false);
		dateTypeRBGroup.add(solsUntilArrivalRB);
		solsUntilArrivalRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JRadioButton rb = (JRadioButton) evt.getSource();
				setEnableTimeUntilArrivalPane(rb.isSelected());
				setEnableArrivalDatePane(!rb.isSelected());
				// Remove error string
				errorString = null;
				errorLabel.setText(errorString);
				// Reenable Commit/Create button
				enableButton(true);
			}
		});
		solsUntilArrivalPane.add(solsUntilArrivalRB);

		// create the sols until arrival label.
		solsUntilArrivalLabel = new JLabel("Sols Until Arrival : ");
		solsUntilArrivalLabel.setToolTipText("(668 Sols = 1 Martian Orbit for a non-leap year)");
		solsUntilArrivalLabel.setEnabled(false);
		solsUntilArrivalPane.add(solsUntilArrivalLabel);

		// Create sols text field.
		MarsTime current = master.getMarsTime();
		int solsDiff = (int) Math.round((resupplyTime.getTimeDiff(current) / 1000D));
		
		int t = ResupplyUtil.getAverageTransitTime();
		SpinnerModel model = new SpinnerNumberModel(solsDiff, 1, t+ResupplyUtil.MAX_NUM_SOLS_PLANNED, 1);
		solsUntilCB = new JSpinner(model);
		solsUntilCB.setEnabled(false);
		solsUntilCB.setValue(solsDiff);
		solsUntilCB.requestFocus(false);
		solsUntilArrivalPane.add(solsUntilCB);

		// Create sol information label.
		JLabel limitLabel = new JLabel("  Note : there is a minimum 10-msol delay for a resupply mission to be executed.");
		limitLabel.setEnabled(true);
		limitLabel.setForeground(new Color(139, 69, 19));
		arrivalDatePane.add(limitLabel);

		// Create error pane.
		JPanel errorPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		arrivalDatePane.add(errorPane);

		// Create error label
		errorLabel = new JLabel(new String());
		errorLabel.setForeground(Color.RED);
		errorPane.add(errorLabel);

		////////////////////////////////////////////
		
		JPanel immigrantsOverviewPane = new JPanel();
		immigrantsOverviewPane.setLayout(new BoxLayout(immigrantsOverviewPane, BoxLayout.Y_AXIS));

		topEditPane.add(immigrantsOverviewPane, BorderLayout.SOUTH);
		
		////////////////////////////////////////////
		
		// Create immigrants panel.
		JPanel immigrantsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		immigrantsOverviewPane.add(immigrantsPane);

		// Create immigrants label.
		JLabel immigrantsLabel = new JLabel("Number of Immigrants : ");
		immigrantsPane.add(immigrantsLabel);

		// Create immigrants text field.
		int immigrantsNum = 0;
		if (resupply != null) {
			immigrantsNum = resupply.getNewImmigrantNum();
		}

		immigrantsCB = new JSpinner(new SpinnerNumberModel(immigrantsNum, 0, MAX_IMMIGRANTS, 1));
		immigrantsCB.setValue(immigrantsNum);
		immigrantsPane.add(immigrantsCB);

		// Create bots panel.
		JPanel botsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		immigrantsOverviewPane.add(botsPane);
		
		// Create bots label.
		JLabel botsLabel = new JLabel("Number of Bots : ");
		botsPane.add(botsLabel);

		// Create bots text field.
		int botsNum = 0;
		if (resupply != null) {
			botsNum = resupply.getNewBotNum();
		}

		botsCB = new JSpinner(new SpinnerNumberModel(botsNum, 0, MAX_BOTS, 1));
		botsCB.setValue(botsNum);
		botsPane.add(botsCB);
		
		////////////////////////////////////////////
		
		// Create bottom edit pane.
		JPanel bottomEditPane = new JPanel(new BorderLayout(0, 0));
		bottomEditPane.setBorder(new TitledBorder("Supplies"));
		add(bottomEditPane, BorderLayout.CENTER);

		// Create supply table.
		supplyTableModel = new SupplyTableModel(resupply);
		supplyTable = new JTable(supplyTableModel);
		supplyTable.getColumnModel().getColumn(0).setMaxWidth(150);
		supplyTable.getColumnModel().getColumn(0).setCellEditor(new CategoryCellEditor());
		supplyTable.getColumnModel().getColumn(1).setMaxWidth(250);
		supplyTable.getColumnModel().getColumn(1).setCellEditor(new TypeCellEditor());
		supplyTable.getColumnModel().getColumn(2).setMaxWidth(100);
		supplyTable.getColumnModel().getColumn(2).setCellEditor(new QuantityCellEditor());
		supplyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				if (!evt.getValueIsAdjusting()) {
					// If rows are selected, enable remove supply button.
					boolean hasSelection = supplyTable.getSelectedRow() > -1;
					removeSupplyButton.setEnabled(hasSelection);
				}
			}
		});

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		supplyTable.getColumnModel().getColumn(2).setCellRenderer(renderer);

		// Create supply scroll pane.
		JScrollPane supplyScrollPane = new JScrollPane(supplyTable);
		supplyScrollPane.setPreferredSize(new Dimension(450, 200));
		bottomEditPane.add(supplyScrollPane, BorderLayout.CENTER);

		// Create supply button pane.
		JPanel supplyButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		bottomEditPane.add(supplyButtonPane, BorderLayout.SOUTH);

		// Create add supply button.
		JButton addSupplyButton = new JButton("Add");
		addSupplyButton.addActionListener(e ->
				// Add new supply row.
				addNewSupplyRow()
		);
		supplyButtonPane.add(addSupplyButton);

		// Create remove supply button.
		removeSupplyButton = new JButton("Remove");
		removeSupplyButton.addActionListener(e ->
				// Remove selected supply rows.
				removeSelectedSupplyRows()
		);
		removeSupplyButton.setEnabled(false);
		supplyButtonPane.add(removeSupplyButton);
	}

	/**
	 * Set the components of the arrival date pane to be enabled or disabled.
	 *
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableArrivalDatePane(boolean enable) {
		arrivalDateTitleLabel.setEnabled(enable);
		solLabel.setEnabled(enable);
		solCB.setEnabled(enable);
		monthLabel.setEnabled(enable);
		monthCB.setEnabled(enable);
		orbitLabel.setEnabled(enable);
		orbitCB.setEnabled(enable);
		errorLabel.setEnabled(!enable);
	}

	/**
	 * Sets the components of the time until arrival pane to be enabled or disabled.
	 *
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableTimeUntilArrivalPane(boolean enable) {
		solsUntilArrivalLabel.setEnabled(enable);
		solsUntilCB.setEnabled(enable);

		if (enable) {
			MarsTime resupplyTime = null;
			int solsDiff = 0;
			if (resupply != null) {
				resupplyTime = resupply.getArrivalDate();
				solsDiff = (int) Math.round((resupplyTime.getTimeDiff(master.getMarsTime()) / 1000D));
			} else {
				getArrivalDate();
			}

			solsUntilCB.setValue(solsDiff);
		}
	}

	/**
	 * Adds a new supply row to the supply table.
	 */
	private void addNewSupplyRow() {
		// Add new supply row.
		supplyTableModel.addNewSupplyItem();

		// Select new row.
		int index = supplyTable.getRowCount() - 1;
		supplyTable.setRowSelectionInterval(index, index);

		// Scroll to bottom of table.
		supplyTable.scrollRectToVisible(supplyTable.getCellRect(index, 0, true));
	}

	/**
	 * Removes the selected supply rows.
	 */
	private void removeSelectedSupplyRows() {

		// Get all selected row indexes and remove items from table.
		int[] removedIndexes = supplyTable.getSelectedRows();
		supplyTableModel.removeSupplyItems(removedIndexes);
	}

	/**
	 * Inner class for editing the Category cell with a combo box.
	 */
	private class CategoryCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private JComboBox<String> categoryCB;
		private int editingRow;
		private String previousCategory;

		/**
		 * Constructor
		 */
		private CategoryCellEditor() {
			super();
			categoryCB = new JComboBox<>();
			Iterator<String> i = SupplyTableModel.getCategoryList().iterator();
			while (i.hasNext()) {
				categoryCB.addItem(i.next());
			}
			categoryCB.addActionListener(this);
		}

		@Override
		public Object getCellEditorValue() {
			return categoryCB.getSelectedItem();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			editingRow = row;
			previousCategory = (String) table.getValueAt(row, column);
			categoryCB.setSelectedItem(previousCategory);
			return categoryCB;
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			String category = (String) categoryCB.getSelectedItem();
			if ((editingRow > -1) && (!category.equals(previousCategory))) {
				supplyTable.setValueAt(category, editingRow, 0);

				// Update supply type cell in row if category has changed.
				String defaultType = SupplyTableModel.getCategoryTypeMap().get(category).get(0);
				// supplyTable.setValueAt(Conversion.capitalize(defaultType), editingRow, 1);
				supplyTable.setValueAt(defaultType, editingRow, 1);
			}
		}
	}

	/**
	 * Inner class for editing the Type cell with a combo box.
	 */
	private class TypeCellEditor extends AbstractCellEditor implements TableCellEditor {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private Map<String, JComboBox<String>> typeCBMap;
		private JComboBox<String> currentCB;

		/**
		 * Constructor
		 */
		private TypeCellEditor() {

			Map<String, List<String>> categoryTypeMap = SupplyTableModel.getCategoryTypeMap();
			typeCBMap = new HashMap<>(categoryTypeMap.keySet().size());
			Iterator<String> i = categoryTypeMap.keySet().iterator();
			while (i.hasNext()) {
				String category = i.next();
				JComboBox<String> categoryCB = new JComboBox<>();
				List<String> types = categoryTypeMap.get(category);
				Iterator<String> j = types.iterator();
				while (j.hasNext()) {
					String type = j.next();
					categoryCB.addItem(type);
				}
				typeCBMap.put(category, categoryCB);
			}
		}

		@Override
		public Object getCellEditorValue() {
			Object result = null;
			if (currentCB != null)
				result = currentCB.getSelectedItem();
			return result;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {

			// Get type combo box based on first column category value.
			String category = (String) table.getValueAt(row, 0);
			currentCB = typeCBMap.get(category);
			currentCB.setSelectedItem(table.getValueAt(row, column));
			return currentCB;
		}
	}

	/**
	 * Inner class for editing the quantity cell with a combo box.
	 */
	private class QuantityCellEditor extends AbstractCellEditor implements TableCellEditor {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private JComboBox<Number> quantityCB;

		/**
		 * Constructor
		 */
		private QuantityCellEditor() {

			int size = quantity.length;
			for (int i = 0; i < size; i++) {
				quantity[i] = i + 1;
			}

			quantityCB = new JComboBox<>(quantity);

		}

		@Override
		public Object getCellEditorValue() {
			Object result = null;
			if (quantityCB != null)
				result = quantityCB.getSelectedItem();
			return result;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			quantityCB.setSelectedItem(table.getValueAt(row, column));
			return quantityCB;
		}
	}

	@Override
	public boolean modifyTransportItem() {
		// Modify resupply mission.
		populateResupplyMission(resupply);
		return true;
	}

	@Override
	public boolean createTransportItem() {
		// Create new resupply mission.
		Settlement destination = (Settlement) destinationCB.getSelectedItem();
		MarsTime arrivalDate = getArrivalDate();

		if (!validation_result)
			return false;

		else if (arrivalDate != null) {
			String name = "Template " + num++;
			Resupply newResupply = new Resupply(name, arrivalDate, destination);
			modifyResupplyMission(newResupply, arrivalDate);
			Simulation.instance().getTransportManager().addNewTransportItem(newResupply);
			return true;
		}
		
		return false;
	}

	/**
	 * Populates a resupply mission from the dialog info.
	 *
	 * @param resupplyMission the resupply mission to populate.
	 */
	private boolean populateResupplyMission(Resupply resupplyMission) {
		// Set destination settlement.
		Settlement destination = (Settlement) destinationCB.getSelectedItem();
		resupplyMission.setSettlement(destination);
		// Set arrival date.
		MarsTime arrivalDate = getArrivalDate();

		if (!validation_result) {
			return false;
		} else {
			modifyResupplyMission(resupplyMission, arrivalDate);
			return true;
		}
	}

	/**
	 * Modifies a resupply mission.
	 * 
	 * @param resupplyMission
	 * @param arrivalDate
	 */
	private void modifyResupplyMission(Resupply resupplyMission, MarsTime arrivalDate) {

		resupplyMission.setArrivalDate(arrivalDate);

		// Set immigrant num.
		int immigrantNum = ((SpinnerNumberModel) immigrantsCB.getModel()).getNumber().intValue();
		resupplyMission.setNewImmigrantNum(immigrantNum);


		// Set bot num.
		int botNum = ((SpinnerNumberModel) botsCB.getModel()).getNumber().intValue();
		resupplyMission.setNewBotNum(botNum);

		
		// Commit any active editing cell in the supply table.
		if (supplyTable.isEditing()) {
			supplyTable.getCellEditor().stopCellEditing();
		}

		List<SupplyItem> supplyItems = supplyTableModel.getSupplyItems();

		// Set new buildings.
		if (resupplyMission.getBuildings() != null) {
			// Modify resupply mission buildings from table.
			modifyNewBuildings(resupplyMission, supplyItems);
		} else {
			// Create new buildings from table in resupply mission.
			List<BuildingTemplate> newBuildings = new ArrayList<>();
			for(SupplyItem item : supplyItems) {
				if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
					int num = item.number.intValue();
					for (int x = 0; x < num; x++) {
						String type = item.type.trim();
						// NOTE: The parameters does NOT mater right now. When a building arrive,
						// the parameters for each building's template will be re-assembled
						newBuildings.add(new BuildingTemplate(0, 0, type, type, new BoundedObject(0D, 0D, -1D, -1D, 0D)));
					}
				}
			}
			resupplyMission.setBuildings(newBuildings);
		}

		// Set new vehicles.
		Map<String, Integer> newVehicles = new HashMap<>();
		Map<String, Integer> newEquipment = new HashMap<>();
		Map<Part, Integer> newParts = new HashMap<>();
		Map<AmountResource, Double> newResources = new HashMap<>();

		for(SupplyItem item : supplyItems) {
			String cat = item.category.trim();
			switch (cat) {
				case SupplyTableModel.VEHICLE: {
					String type = item.type.trim();
					int num = item.number.intValue();
					if (newVehicles.containsKey(type)) {
						num += newVehicles.get(type);
					}
					newVehicles.put(type, num);
				} break;
				
				case SupplyTableModel.EQUIPMENT: {
						String type = item.type.trim();
						int num = item.number.intValue();
						if (newEquipment.containsKey(type)) {
							num += newEquipment.get(type);
						}
						newEquipment.put(type, num);
					} break;

				case SupplyTableModel.RESOURCE: {
						String type = item.type.trim();
						AmountResource resource = ResourceUtil.findAmountResource(type);
						double amount = item.number.doubleValue();
						if (newResources.containsKey(resource)) {
							amount += newResources.get(resource);
						}
						newResources.put(resource, amount);
					} break;

				case SupplyTableModel.PART: {
						String type = item.type.trim();
						Part part = (Part) ItemResourceUtil.findItemResource(type);
						int num = item.number.intValue();
						if (newParts.containsKey(part)) {
							num += newParts.get(part);
						}
						newParts.put(part, num);
					} break;
				
				default:
			}
		}
		resupplyMission.setVehicles(newVehicles);
		resupplyMission.setEquipment(newEquipment);
		resupplyMission.setResources(newResources);
		resupplyMission.setParts(newParts);
	}

	/**
	 * Modifies existing resupply mission new buildings based on supply table.
	 *
	 * @param resupplyMission resupply mission.
	 * @param supplyItems     the supply items from the supply table.
	 */
	private void modifyNewBuildings(Resupply resupplyMission, List<SupplyItem> supplyItems) {

		List<BuildingTemplate> newBuildings = new ArrayList<>(resupplyMission.getBuildings());

		// Create map of resupply mission's buildings and numbers.
		Map<String, Integer> oldBuildings = new HashMap<>();
		Iterator<BuildingTemplate> i = newBuildings.iterator();
		while (i.hasNext()) {
			BuildingTemplate template = i.next();
			String type = template.getBuildingType();
			if (oldBuildings.containsKey(type)) {
				int num = oldBuildings.get(type);
				oldBuildings.put(type, num + 1);
			} else {
				oldBuildings.put(type, 1);
			}
		}

		// Go through every building row in the supply table.
		Iterator<SupplyItem> j = supplyItems.iterator();
		while (j.hasNext()) {
			SupplyItem item = j.next();
			if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
				int num = item.number.intValue();
				String type = item.type.trim();

				int existingNum = 0;
				if (oldBuildings.containsKey(type)) {
					existingNum = oldBuildings.get(type);
				}

				if (num > existingNum) {
					// Add new building templates.
					int diff = num - existingNum;
					for (int x = 0; x < diff; x++) {
						// Added a dummy type parameter

						// NOTE: currently building id = 0
						// May need to assemble the buildingNickName
						// by obtaining the next building id and settlement id

						// NOTE: determine why specifying the coordinate below is needed for
						// the Command and Control building to be placed properly

						// NOTE: The parameters does NOT mater right now. When a building arrive,
						// the parameters for each building's template will be re-assembled

						newBuildings.add(new BuildingTemplate(0, 0, type, type,
											new BoundedObject(0D, 38D, 7D, 9D, 270D)));
					}
					
				} else if (num < existingNum) {
					// Remove old building templates.
					int diff = existingNum - num;
					for (int x = 0; x < diff; x++) {
						Iterator<BuildingTemplate> k = newBuildings.iterator();
						while (k.hasNext()) {
							BuildingTemplate template = k.next();
							if (template.getBuildingType().equalsIgnoreCase(type)) {
								k.remove();
								break;
							}
						}
					}
				}
			}
		}

		// Go through all of the old buildings in the map to make sure they exist in the
		// supply table.
		Iterator<String> k = oldBuildings.keySet().iterator();
		while (k.hasNext()) {
			String type = k.next();
			boolean exists = false;
			Iterator<SupplyItem> l = supplyItems.iterator();
			while (l.hasNext() && !exists) {
				SupplyItem item = l.next();
				if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
					if (type.equals(item.type.trim())) {
						exists = true;
					}
				}
			}

			// Remove building from new buildings if it doesn't exist in supply table.
			if (!exists) {
				Iterator<BuildingTemplate> m = newBuildings.iterator();
				while (m.hasNext()) {
					BuildingTemplate template = m.next();
					if (template.getBuildingType().equalsIgnoreCase(type)) {
						m.remove();
					}
				}
			}
		}
		resupplyMission.setBuildings(newBuildings);
	}

	/**
	 * Gets the arrival date from the dialog info.
	 *
	 * @return {@link MarsTime} arrival date.
	 */
	private MarsTime getArrivalDate() {
		errorString = null;
		MarsTime arrivalTime = null;

		if (arrivalDateRB.isSelected()) {

			// Determine arrival date from arrival date combo boxes.
			try {
				int sol = solCB.getSelectedIndex() + 1;
				int month = monthCB.getSelectedIndex() + 1;
				int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

				// Set millisols to current time plus the delay if resupply is current date, otherwise 0.
				MarsTime now = master.getMarsTime();
				double millisols = now.getMillisol();
				arrivalTime = new MarsTime(orbit, month, sol, millisols, now.getMissionSol());
				
				if (arrivalTime.getTimeDiff(now) < 0) {
					// if the player selects a sol before today
					arrivalTime = null;
					// Remove error string
					errorString = "Cannot pick a date that's in the past. Try again !";
					errorLabel.setText(errorString);
					logger.severe(errorString);
					enableButton(false);
					validation_result = false;
				}
			} catch (NumberFormatException e) {
				String msg = "Can't create marsCurrentTime: " + e.getMessage(); 
				logger.severe(msg);
				errorLabel.setText(msg);
				enableButton(false);
				validation_result = false;
			}
		}

		else if (solsUntilArrivalRB.isSelected()) {
			arrivalTime = validateSolsUntilArrival();
		}

		return arrivalTime;
	}

	/**
	 * Validates the sols until arrival
	 */
	public MarsTime validateSolsUntilArrival() {
		errorString = null;
		solsUntilCB.setEnabled(true);
		int inputSol = ((SpinnerNumberModel) solsUntilCB.getModel()).getNumber().intValue();

		MarsTime arrivalTime = null;
		try {
			boolean good = true;

			if (good) {
				
				// Remove error string
				errorString = null;
				errorLabel.setText(errorString);
				// Reenable Commit/Create button
				enableButton(true);
				
				validation_result = true;

				arrivalTime = master.getMarsTime();
				if (inputSol == 0)
					arrivalTime = arrivalTime.addTime(arrivalTime.getMillisol());
				else
					arrivalTime = arrivalTime.addTime(inputSol * 1000D);
			}

		} catch (NumberFormatException e) {
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidSols"); //$NON-NLS-1$
			errorLabel.setText(errorString);
			validation_result = false;
			enableButton(false);
			logger.severe("Invalid entry for Sols: " + e.getMessage());
		}

		return arrivalTime;
	}

	public void enableButton(boolean value) {
		if (modifyTransportItemDialog != null)
			modifyTransportItemDialog.setCommitButton(value);
		else if (newTransportItemDialog != null)
			newTransportItemDialog.setCreateButton(value);
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
		// http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void changedUpdate(DocumentEvent e) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};
		text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document) e.getOldValue();
			Document d2 = (Document) e.getNewValue();
			if (d1 != null)
				d1.removeDocumentListener(dl);
			if (d2 != null)
				d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = text.getDocument();
		if (d != null)
			d.addDocumentListener(dl);
	}
}
