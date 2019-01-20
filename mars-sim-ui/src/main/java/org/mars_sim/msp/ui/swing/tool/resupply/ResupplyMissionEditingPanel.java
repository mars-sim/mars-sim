/**
 * Mars Simulation Project
 * ResupplyMissionEditingPanel.java
 * @version 3.1.0 2016-11-23
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JTable;
//import javax.swing.JTextField;
import javax.swing.ListModel;
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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.interplanetary.transport.TransitState;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.interplanetary.transport.settlement.ArrivingSettlement;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.resupply.SupplyTableModel.SupplyItem;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.radiobutton.WebRadioButton;
import com.alee.laf.scroll.WebScrollPane;

//import eu.hansolo.enzo.radialmenu.RadialMenu.ItemEvent;

/**
 * A panel for creating or editing a resupply mission. TODO externalize strings
 */
public class ResupplyMissionEditingPanel extends TransportItemEditingPanel {

	private static final Integer[] EMPTY_STRING_ARRAY = new Integer[0];
	private static final int MAX_FUTURE_ORBITS = 20;
	private static final int MAX_IMMIGRANTS = 48;

	// Data members
	private String errorString = new String();
	private boolean validation_result = true;
	private Integer[] sols = new Integer[ResupplyUtil.MAX_NUM_SOLS_PLANNED];
	private Number[] quantity = new Number[100000];
	private Integer[] immigrants = new Integer[MAX_IMMIGRANTS];

	private JComboBoxMW<Settlement> destinationCB;
	private WebRadioButton arrivalDateRB;
	private WebRadioButton timeUntilArrivalRB;
	private MartianSolComboBoxModel martianSolCBModel;
	private WebLabel arrivalDateTitleLabel;
	private WebLabel timeUntilArrivalLabel;
	private WebLabel solLabel;
	private WebLabel monthLabel;
	private WebLabel orbitLabel;
	private WebLabel solInfoLabel;
	private WebLabel errorLabel;
	private JComboBoxMW<?> solCB, solsFromCB, immigrantsCB, monthCB, orbitCB;
	private SupplyTableModel supplyTableModel;
	private JTable supplyTable;
	private WebButton removeSupplyButton;

	private Resupply resupply;
	private NewTransportItemDialog newTransportItemDialog = null;
	private ModifyTransportItemDialog modifyTransportItemDialog = null;
	private ResupplyWindow resupplyWindow;

	private MarsClock marsCurrentTime;
	
	protected static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();

	/** constructor. */
	public ResupplyMissionEditingPanel(Resupply resupply, ResupplyWindow resupplyWindow,
			ModifyTransportItemDialog modifyTransportItemDialog, NewTransportItemDialog newTransportItemDialog) {
		// User TransportItemEditingPanel constructor.
		super(resupply);

		// Initialize data members.
		this.resupply = resupply;
		this.newTransportItemDialog = newTransportItemDialog;
		this.modifyTransportItemDialog = modifyTransportItemDialog;
		this.resupplyWindow = resupplyWindow;

		setBorder(new MarsPanelBorder());
		setLayout(new BorderLayout(0, 0));

		// Create top edit pane.
		WebPanel topEditPane = new WebPanel(new BorderLayout(10, 10));
		add(topEditPane, BorderLayout.NORTH);

		// Create destination pane.
		WebPanel destinationPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topEditPane.add(destinationPane, BorderLayout.NORTH);

		// Create destination title label.
		WebLabel destinationTitleLabel = new WebLabel("Destination : ");
		destinationPane.add(destinationTitleLabel);

		// Create destination combo box.
		Vector<Settlement> settlements = new Vector<Settlement>(
				unitManager.getSettlements());
		Collections.sort(settlements);
		destinationCB = new JComboBoxMW<Settlement>(settlements);
		if (resupply != null) {
			destinationCB.setSelectedItem(resupply.getSettlement());
		} else {
			// this.settlement = (Settlement) destinationCB.getSelectedItem();
		}
		destinationPane.add(destinationCB);

		// Create arrival date pane.
		WebPanel arrivalDatePane = new WebPanel(new GridLayout(4, 1, 10, 10));
		arrivalDatePane.setBorder(new TitledBorder("Arrival Date"));
		topEditPane.add(arrivalDatePane, BorderLayout.CENTER);

		// Create data type radio button group.
		ButtonGroup dateTypeRBGroup = new ButtonGroup();

		// Create arrival date selection pane.
		WebPanel arrivalDateSelectionPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(arrivalDateSelectionPane);

		// Create arrival date radio button.
		arrivalDateRB = new WebRadioButton();
		dateTypeRBGroup.add(arrivalDateRB);
		arrivalDateRB.setSelected(true);
		arrivalDateRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				WebRadioButton rb = (WebRadioButton) evt.getSource();
				setEnableArrivalDatePane(rb.isSelected());
				setEnableTimeUntilArrivalPane(!rb.isSelected());
			}
		});
		arrivalDateSelectionPane.add(arrivalDateRB);

		// Create arrival date title label.
		arrivalDateTitleLabel = new WebLabel("Arrival Date : ");
		arrivalDateSelectionPane.add(arrivalDateTitleLabel);

		// Get default resupply Martian time.
		MarsClock resupplyTime = null;
		if (resupply != null) {
			resupplyTime = resupply.getArrivalDate();
		} else {
			resupplyTime = (MarsClock) marsClock.clone();
			resupplyTime.addTime(ResupplyUtil.getAverageTransitTime() * 1000D);
		}

		martianSolCBModel = new MartianSolComboBoxModel(resupplyTime.getMonth(), resupplyTime.getOrbit());

		// Create orbit label.
		orbitLabel = new WebLabel("Orbit");
		arrivalDateSelectionPane.add(orbitLabel);

		// Create orbit combo box.
		NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumIntegerDigits(2);
		String[] orbitValues = new String[MAX_FUTURE_ORBITS];
		int startOrbit = resupplyTime.getOrbit();
		for (int x = 0; x < MAX_FUTURE_ORBITS; x++) {
			orbitValues[x] = formatter.format(startOrbit + x);
		}
		orbitCB = new JComboBoxMW<Object>(orbitValues);
		orbitCB.setSelectedItem(formatter.format(startOrbit));
		orbitCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Update sol combo box values.
				martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
						Integer.parseInt((String) orbitCB.getSelectedItem()));
			}
		});
		arrivalDateSelectionPane.add(orbitCB);

		// Create month label.
		monthLabel = new WebLabel("Month");
		arrivalDateSelectionPane.add(monthLabel);

		// Create month combo box.
		monthCB = new JComboBoxMW<Object>(MarsClock.getMonthNames());
		monthCB.setSelectedItem(resupplyTime.getMonthName());
		monthCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Update sol combo box values.
				martianSolCBModel.updateSolNumber(monthCB.getSelectedIndex() + 1,
						Integer.parseInt((String) orbitCB.getSelectedItem()));
			}
		});
		arrivalDateSelectionPane.add(monthCB);

		// Create sol label.
		solLabel = new WebLabel("Sol");
		arrivalDateSelectionPane.add(solLabel);

		// Create sol combo box.
		solCB = new JComboBoxMW<Integer>(martianSolCBModel);
		solCB.setSelectedItem(resupplyTime.getSolOfMonth());
		arrivalDateSelectionPane.add(solCB);

		// Create time until arrival pane.
		WebPanel timeUntilArrivalPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		arrivalDatePane.add(timeUntilArrivalPane);

		// Create time until arrival radio button.
		timeUntilArrivalRB = new WebRadioButton();
		timeUntilArrivalRB.setSelected(false);
		dateTypeRBGroup.add(timeUntilArrivalRB);
		timeUntilArrivalRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				WebRadioButton rb = (WebRadioButton) evt.getSource();
				setEnableTimeUntilArrivalPane(rb.isSelected());
				setEnableArrivalDatePane(!rb.isSelected());
			}
		});
		timeUntilArrivalPane.add(timeUntilArrivalRB);

		// create time until arrival label.
		timeUntilArrivalLabel = new WebLabel("Sols Until Arrival : ");
		timeUntilArrivalLabel.setEnabled(false);
		timeUntilArrivalPane.add(timeUntilArrivalLabel);

		// Create sols text field.
//		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		int solsDiff = (int) Math.round((MarsClock.getTimeDiff(resupplyTime, marsClock) / 1000D));

//		solsTF = new JTextField(6);
//		solsTF.setText(Integer.toString(solsDiff));
//		solsTF.setHorizontalAlignment(JTextField.RIGHT);
//		solsTF.setEnabled(false);
//		solsTF.setEditable(true);
//
//		timeUntilArrivalPane.add(solsTF);
//		// Implemented addChangeListener() to validate solsTF.
//		addChangeListener(solsTF, e -> validateSolsTF());

		// Switch to using ComboBoxMW for sols
		int size = sols.length;
		// int max = ResupplyUtil.MAX_NUM_SOLS_PLANNED;
		int t = ResupplyUtil.getAverageTransitTime();
		for (int i = t + 1; i < size + t + 1; i++) {
			if (i > t)
				sols[i - t - 1] = i;
		}

		updateSolsCB();
		solsFromCB.setSelectedItem(solsDiff);
		solsFromCB.requestFocus(false);
		timeUntilArrivalPane.add(solsFromCB);

		// Create sol information label.
		solInfoLabel = new WebLabel("(668 Sols = 1 Martian Orbit for a non-leap year)");
		solInfoLabel.setEnabled(false);
		timeUntilArrivalPane.add(solInfoLabel);

		// Create sol information label.
		WebLabel limitLabel = new WebLabel("  Note : cannot have more than one resupply per sol.");
		limitLabel.setEnabled(true);
		limitLabel.setForeground(Color.ORANGE);
		arrivalDatePane.add(limitLabel);

		// Create error pane.
		WebPanel errorPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		arrivalDatePane.add(errorPane);// , BorderLayout.SOUTH);

		// Create error label
		errorLabel = new WebLabel(new String());
		errorLabel.setForeground(Color.RED);
		errorPane.add(errorLabel);

		// Create immigrants panel.
		WebPanel immigrantsPane = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		topEditPane.add(immigrantsPane, BorderLayout.SOUTH);

		// Create immigrants label.
		WebLabel immigrantsLabel = new WebLabel("Number of Immigrants : ");
		immigrantsPane.add(immigrantsLabel);

		// Create immigrants text field.
		int immigrantsNum = 0;
		if (resupply != null) {
			immigrantsNum = resupply.getNewImmigrantNum();
		}

//		immigrantsTF = new JTextField(6);
//		immigrantsTF.setText(Integer.toString(immigrantsNum));
//		immigrantsTF.setHorizontalAlignment(JTextField.RIGHT);
//		immigrantsPane.add(immigrantsTF);

		// Switch to using ComboBoxMW for immigrants
		int size1 = immigrants.length;
		for (int i = 0; i < size1; i++) {
			immigrants[i] = i;
		}
		immigrantsCB = new JComboBoxMW<Integer>(immigrants);
		immigrantsCB.setSelectedItem(immigrantsNum);
		immigrantsPane.add(immigrantsCB);

		// Create bottom edit pane.
		WebPanel bottomEditPane = new WebPanel(new BorderLayout(0, 0));
		bottomEditPane.setBorder(new TitledBorder("Supplies"));
		add(bottomEditPane, BorderLayout.CENTER);

		// Create supply table.
		supplyTableModel = new SupplyTableModel(resupply);
		supplyTable = new JTable(supplyTableModel);
		TableStyle.setTableStyle(supplyTable);
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
		WebScrollPane supplyScrollPane = new WebScrollPane(supplyTable);
		supplyScrollPane.setPreferredSize(new Dimension(450, 200));
		bottomEditPane.add(supplyScrollPane, BorderLayout.CENTER);

		// Create supply button pane.
		WebPanel supplyButtonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		bottomEditPane.add(supplyButtonPane, BorderLayout.SOUTH);

		// Create add supply button.
		WebButton addSupplyButton = new WebButton("Add");
		addSupplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Add new supply row.
				addNewSupplyRow();
			}
		});
		supplyButtonPane.add(addSupplyButton);

		// Create remove supply button.
		removeSupplyButton = new WebButton("Remove");
		removeSupplyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Remove selected supply rows.
				removeSelectedSupplyRows();
			}
		});
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
	 * Set the components of the time until arrival pane to be enabled or disabled.
	 * 
	 * @param enable true if enable components, false if disable components.
	 */
	private void setEnableTimeUntilArrivalPane(boolean enable) {
		timeUntilArrivalLabel.setEnabled(enable);
		// solsTF.setEnabled(enable);
		// solsTF.setEditable(true);
		// solsFromCB.setEnabled(enable);
//		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		MarsClock resupplyTime = null;
		int solsDiff = 0;
		if (resupply != null) {
			resupplyTime = resupply.getArrivalDate();
			solsDiff = (int) Math.round((MarsClock.getTimeDiff(resupplyTime, marsClock) / 1000D));
		} else {
			getArrivalDate();
		}
		solsFromCB.setSelectedItem(solsDiff);
		solInfoLabel.setEnabled(enable);
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
	 * Remove the selected supply rows.
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
		private JComboBoxMW<String> categoryCB;
		private int editingRow;
		private String previousCategory;

		/**
		 * Constructor
		 */
		private CategoryCellEditor() {
			super();
			categoryCB = new JComboBoxMW<String>();
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

//		public void updateSolsCB() {
//			List<Integer> list = new ArrayList<Integer>();
//			Collections.addAll(list, sols);
//			// Remove dates that have been chosen for other resupply missions.
//			list.removeAll(Arrays.asList(getMissionSols()));
//			sols = list.toArray(EMPTY_STRING_ARRAY);
//			solsFromCB = new JComboBoxMW<Integer>(sols);
//		}

	}

	/**
	 * Inner class for editing the Type cell with a combo box.
	 */
	private class TypeCellEditor extends AbstractCellEditor implements TableCellEditor {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members.
		private Map<String, JComboBoxMW<String>> typeCBMap;
		private JComboBoxMW<String> currentCB;

		/**
		 * Constructor
		 */
		private TypeCellEditor() {

			Map<String, List<String>> categoryTypeMap = SupplyTableModel.getCategoryTypeMap();
			typeCBMap = new HashMap<String, JComboBoxMW<String>>(categoryTypeMap.keySet().size());
			Iterator<String> i = categoryTypeMap.keySet().iterator();
			while (i.hasNext()) {
				String category = i.next();
				JComboBoxMW<String> categoryCB = new JComboBoxMW<String>();
				List<String> types = categoryTypeMap.get(category);
				Iterator<String> j = types.iterator();
				while (j.hasNext()) {
					String type = j.next();
					categoryCB.addItem(Conversion.capitalize(type));
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
		private JComboBoxMW<Number> quantityCB;

		/**
		 * Constructor
		 */
		private QuantityCellEditor() {

			int size = quantity.length;
			for (int i = 0; i < size; i++) {
				quantity[i] = i + 1;
			}

			quantityCB = new JComboBoxMW<Number>(quantity);

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
		resupply.commitModification();
		updateSolsCB();
		return true;
	}

	public void updateSolsCB() {
		List<Integer> list = new ArrayList<Integer>();
		Collections.addAll(list, sols);
		// Remove dates that have been chosen for other resupply missions.
		list.removeAll(getMissionSols());
		sols = list.toArray(EMPTY_STRING_ARRAY);
		solsFromCB = new JComboBoxMW<Integer>(sols);
		solsFromCB.requestFocus(false);
		solsFromCB.putClientProperty("JComboBox.isTableCellEditor", Boolean.FALSE);
		solsFromCB.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				timeUntilArrivalRB.requestFocus(true);
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				arrivalDateRB.requestFocus(true);
			}

		});

		solsFromCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				// JComboBoxMW<Integer> cb = (JComboBoxMW<Integer>) evt.getSource();
				// Object item = evt.getItem();
				if (evt.getStateChange() == ItemEvent.SELECTED) {
					timeUntilArrivalRB.requestFocus(true);
				} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
					arrivalDateRB.requestFocus(true);
				}
			}
		});

		// solsFromCB.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent evt) {
		// if (solsFromCB.isfo
		// timeUntilArrivalRB.requestFocusInWindow(true);
		// }
		// });

	}

	@Override
	public boolean createTransportItem() {
		// Create new resupply mission.
		Settlement destination = (Settlement) destinationCB.getSelectedItem();
		MarsClock arrivalDate = null;
		if (marsCurrentTime == null)
			arrivalDate = getArrivalDate();
		else
			arrivalDate = marsCurrentTime;

		if (!validation_result)
			return false;

		else {
			Resupply newResupply = new Resupply(arrivalDate, destination);
			modifyResupplyMission(newResupply, arrivalDate);
			// boolean good = populateResupplyMission(newResupply);
			// if (!good)
			// return false;
			// else {
			Simulation.instance().getTransportManager().addNewTransportItem(newResupply);
			return true;
			// }
		}
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
		MarsClock arrivalDate = null;
		if (marsCurrentTime == null)
			arrivalDate = getArrivalDate();
		else
			arrivalDate = marsCurrentTime;

		if (!validation_result) {
			return false;
		} else {
			modifyResupplyMission(resupplyMission, arrivalDate);
			return true;
		}
	}

	private void modifyResupplyMission(Resupply resupplyMission, MarsClock arrivalDate) {

		resupplyMission.setArrivalDate(arrivalDate);

		// Determine launch date.
		MarsClock launchDate = (MarsClock) arrivalDate.clone();
		launchDate.addTime(-1D * ResupplyUtil.getAverageTransitTime() * 1000D);
		resupplyMission.setLaunchDate(launchDate);

		// Set resupply state based on launch and arrival time.
//		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
		TransitState state = TransitState.PLANNED;
		if (MarsClock.getTimeDiff(marsClock, launchDate) > 0D) {
			state = TransitState.IN_TRANSIT;
			if (MarsClock.getTimeDiff(marsClock, arrivalDate) > 0D) {
				state = TransitState.ARRIVED;
			}
		}
		resupplyMission.setTransitState(state);

		// Set immigrant num.
		int immigrantNum = 0;
		try {
			immigrantNum = (Integer) immigrantsCB.getSelectedItem();
			if (immigrantNum < 0)
				immigrantNum = 0;
			resupplyMission.setNewImmigrantNum(immigrantNum);
		} catch (NumberFormatException e) {
			e.printStackTrace(System.err);
		}

		// Commit any active editing cell in the supply table.
		if (supplyTable.isEditing()) {
			supplyTable.getCellEditor().stopCellEditing();
		}

		List<SupplyItem> supplyItems = supplyTableModel.getSupplyItems();

		// Set new buildings.
		if (resupplyMission.getNewBuildings() != null) {
			// Modify resupply mission buildings from table.
			modifyNewBuildings(resupplyMission, supplyItems);
		} else {
			// Create new buildings from table in resupply mission.
			List<BuildingTemplate> newBuildings = new ArrayList<BuildingTemplate>();
			Iterator<SupplyItem> i = supplyItems.iterator();
			while (i.hasNext()) {
				SupplyItem item = i.next();
				if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
					int num = item.number.intValue();
					for (int x = 0; x < num; x++) {
						String type = item.type.trim();

						// int scenarioID = destination.getID();
						// String scenario = getCharForNumber(scenarioID + 1);
						// System.out.println("ResupplyMissionEditingPanel.java Line 548: scenario is "
						// + scenario);
						// System.out.println("ResupplyMissionEditingPanel.java Line 549:
						// buildingNickName is " + buildingNickName);

						// BuildingTemplate template = new BuildingTemplate(0, scenario, type, type,
						// -1D, -1D, 0D, 0D, 0D);
						// BuildingTemplate template = new BuildingTemplate(scenarioID, scenario, type,
						// type, 7D, 9D, 0D, 38D, 270D);

						// NOTE: The parameters does NOT mater right now. When a building arrive,
						// the parameters for each building's template will be re-assembled
						newBuildings.add(new BuildingTemplate(null, 0, null, type, type, -1D, -1D, 0D, 0D, 0D));
					}
				}
			}
			resupplyMission.setNewBuildings(newBuildings);
		}

		// Set new vehicles.
		List<String> newVehicles = new ArrayList<String>();
		Iterator<SupplyItem> j = supplyItems.iterator();
		while (j.hasNext()) {
			SupplyItem item = j.next();
			if (SupplyTableModel.VEHICLE.equals(item.category.trim())) {
				int num = item.number.intValue();
				for (int x = 0; x < num; x++) {
					newVehicles.add(item.type.trim());
				}
			}
		}
		resupplyMission.setNewVehicles(newVehicles);

		// Set new equipment.
		Map<String, Integer> newEquipment = new HashMap<String, Integer>();
		Iterator<SupplyItem> k = supplyItems.iterator();
		while (k.hasNext()) {
			SupplyItem item = k.next();
			if (SupplyTableModel.EQUIPMENT.equals(item.category.trim())) {
				String type = item.type.trim();
				int num = item.number.intValue();
				if (newEquipment.containsKey(type)) {
					num += newEquipment.get(type);
				}
				newEquipment.put(type, num);
			}
		}
		resupplyMission.setNewEquipment(newEquipment);

		// Set new resources.
		Map<AmountResource, Double> newResources = new HashMap<AmountResource, Double>();
		Iterator<SupplyItem> l = supplyItems.iterator();
		while (l.hasNext()) {
			SupplyItem item = l.next();
			if (SupplyTableModel.RESOURCE.equals(item.category.trim())) {
				String type = item.type.trim();
				AmountResource resource = ResourceUtil.findAmountResource(type);
				double amount = item.number.doubleValue();
				if (newResources.containsKey(resource)) {
					amount += newResources.get(resource);
				}
				newResources.put(resource, amount);
			}
		}
		resupplyMission.setNewResources(newResources);

		// Set new parts.
		Map<Part, Integer> newParts = new HashMap<Part, Integer>();
		Iterator<SupplyItem> m = supplyItems.iterator();
		while (m.hasNext()) {
			SupplyItem item = m.next();
			if (SupplyTableModel.PART.equals(item.category.trim())) {
				String type = item.type.trim();
				Part part = (Part) ItemResourceUtil.findItemResource(type);
				int num = item.number.intValue();
				if (newParts.containsKey(part)) {
					num += newParts.get(part);
				}
				newParts.put(part, num);
			}
		}
		resupplyMission.setNewParts(newParts);

		// return true;
	}

	/**
	 * Maps a number to an alphabet
	 * 
	 * @param a number
	 * @return a String
	 */
	private String getCharForNumber(int i) {
		// NOTE: i must be > 1, if i = 0, return null
		return i > 0 && i < 27 ? String.valueOf((char) (i + 'A' - 1)) : null;
	}

	/**
	 * Modify existing resupply mission new buildings based on supply table.
	 * 
	 * @param resupplyMission resupply mission.
	 * @param supplyItems     the supply items from the supply table.
	 */
	private void modifyNewBuildings(Resupply resupplyMission, List<SupplyItem> supplyItems) {

		List<BuildingTemplate> newBuildings = resupplyMission.getNewBuildings();

		// Create map of resupply mission's buildings and numbers.
		Map<String, Integer> oldBuildings = new HashMap<String, Integer>();
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
						// TODO: currently building id = 0
						// May need to assemble the buildingNickName
						// by obtaining the next building id and settlement id

						// newBuildings.add(new BuildingTemplate(0, null, type, type, -1D, -1D, -0D, 0D,
						// 0D));

						// TODO: determine why specifying the coordinate below is needed for
						// the Command and Control building to be placed properly

						// NOTE: The parameters does NOT mater right now. When a building arrive,
						// the parameters for each building's template will be re-assembled

						newBuildings.add(new BuildingTemplate(null, 0, null, type, type, 7D, 9D, 0D, 38D, 270D));

					}
				} else if (num < existingNum) {
					// Remove old building templates.
					int diff = existingNum - num;
					for (int x = 0; x < diff; x++) {
						Iterator<BuildingTemplate> k = newBuildings.iterator();
						while (k.hasNext()) {
							BuildingTemplate template = k.next();
							if (template.getBuildingType().equals(type)) {
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
					if (template.getBuildingType().equals(type)) {
						m.remove();
					}
				}
			}
		}
	}

	/**
	 * Gets the arrival date from the dialog info.
	 * 
	 * @return {@link MarsClock} arrival date.
	 */
	private MarsClock getArrivalDate() {
		// String errorString = new String();
		errorString = null;

		// MarsClock currentTime =
		// Simulation.instance().getMasterClock().getMarsClock();
		// marsCurrentTime = (MarsClock) currentTime.clone();

		if (arrivalDateRB.isSelected()) {
//			MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
			marsCurrentTime = (MarsClock) marsClock.clone();

			// Determine arrival date from arrival date combo boxes.
			try {
				int sol = solCB.getSelectedIndex() + 1;
				int month = monthCB.getSelectedIndex() + 1;
				int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

				// Set millisols to current time if resupply is current date, otherwise 0.
				double millisols = 0D;
				if ((sol == marsClock.getSolOfMonth()) && (month == marsClock.getMonth())
						&& (orbit == marsClock.getOrbit())) {
					millisols = marsClock.getMillisol();
				}
				// validation_result = true;

				marsCurrentTime = new MarsClock(orbit, month, sol, millisols, -1);
			} catch (NumberFormatException e) {
				e.printStackTrace(System.err);
			}
		}

		else if (timeUntilArrivalRB.isSelected()) {
			marsCurrentTime = validateSolsFrom();

			// Implemented addChangeListener() to validate solsTF.
//			String timeArrivalString = solsTF.getText().trim();
//			if (timeArrivalString.isEmpty()) {
//				validation_result = false;
//				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noSols"); //$NON-NLS-1$
//				errorLabel.setText(errorString);
//				enableButton(false);
//				System.out.println("Invalid Sols. It cannot be empty.");
//			}
//			else {
//				//System.out.println("calling validateSolsTF()");
//				marsCurrentTime = validateSolsTF();
//			}

		}
		return marsCurrentTime;
	}

	/*** Implemented validation of textfield. */
	public MarsClock validateSolsFrom() { // String timeArrivalString) {
		// System.out.println("running validateSolsTF()");
		errorString = null;
		solsFromCB.setEditable(true);
		solsFromCB.setSelectedIndex(0);
//		String timeArrivalString = (String) solsFromCB.getSelectedItem();//solsTF.getText().trim();
//		if (timeArrivalString.isEmpty()) {
//			validation_result = false;
//			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.noSols"); //$NON-NLS-1$
//			errorLabel.setText(errorString);
//			//marsCurrentTime = null;
//			enableButton(false);
//			System.out.println("Invalid entry in Sols. It cannot be empty.");
//		}
//		else {

		// Determine arrival date from time until arrival text field.
		// if (timeArrivalString.equals("-"))
		// timeArrivalString = "-1";
		// int timeArrival = (Integer) solsFromCB.getSelectedItem();
		int inputSol = (Integer) solsFromCB.getSelectedItem();
		if (inputSol == 0) {
			solsFromCB.remove(0);
			solsFromCB.setSelectedIndex(0);
		}
		// int inputSols = Integer.parseInt(solsTF.getText());
//			if (inputSol < 0D) {
//			//if (inputSols < 0D) {
//				errorString = Msg.getString("ArrivingSettlementEditingPanel.error.negativeSols"); //$NON-NLS-1$
//				errorLabel.setText(errorString);
//				//marsCurrentTime = null;
//				validation_result = false;
//				enableButton(false);
//				System.out.println("Invalid entry in Sols. It cannot be less than zero.");
//			}
//			else {
		try {
			boolean good = true;

			List<Integer> solsList = getMissionSols();

			Iterator<Integer> i = solsList.iterator();
			while (i.hasNext()) {
				int sol = i.next();
				if (sol == inputSol) {
					System.out.println("Invalid entry in 'Sols Until Arrival' since sol " + sol
							+ " has already been chosen in another resupply mission.");
					enableButton(false);
					errorString = Msg.getString("ResupplyMissionEditingPanel.error.duplicatedSol", sol); //$NON-NLS-1$
					errorLabel.setText(errorString);
					validation_result = false;
					good = false;
					break;
				}
			}

			if (good) {
				errorString = null;
				errorLabel.setText(errorString);
				// enableButton(true);
				validation_result = true;
				// System.out.println("inputSols is " + inputSols);
//				MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
				marsCurrentTime = (MarsClock) marsClock.clone();
				if (inputSol == 0)
					marsCurrentTime.addTime(marsCurrentTime.getMillisol());
				else
					marsCurrentTime.addTime(inputSol * 1000D);
			}

		} catch (NumberFormatException e) {
			errorString = Msg.getString("ArrivingSettlementEditingPanel.error.invalidSols"); //$NON-NLS-1$
			errorLabel.setText(errorString);
			e.printStackTrace(System.err);
			// marsCurrentTime = null;
			validation_result = false;
			enableButton(false);
			System.out.println("ResupplyMissionEditingPanel : Invalid entry for Sols ");
		}
		// }
		// }
		return marsCurrentTime;
	}

	public List<Integer> getMissionSols() {
		List<Integer> solsList = new ArrayList<>();
		// Added checking if this particular sol has already been chosen for a resupply
		// mission
		// Note: it makes sense to impose the limitation of having one resupply mission
		// per sol
		JList<?> jList = resupplyWindow.getIncomingListPane().getIncomingList();
		ListModel<?> model = jList.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			Transportable transportItem = (Transportable) model.getElementAt(i);

			if ((transportItem != null)) {
				if (transportItem instanceof Resupply) {
					// Create modify resupply mission dialog.
					Resupply newR = (Resupply) transportItem;
					if (!newR.equals(resupply)) {
						MarsClock arrivingTime = newR.getArrivalDate();
//						MarsClock nowTime = Simulation.instance().getMasterClock().getMarsClock();
						int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
						solsList.add(solsDiff);
					}
				} else if (transportItem instanceof ArrivingSettlement) {
					// Create modify arriving settlement dialog.
					ArrivingSettlement settlement = (ArrivingSettlement) transportItem;
					MarsClock arrivingTime = settlement.getArrivalDate();
//					MarsClock nowTime = Simulation.instance().getMasterClock().getMarsClock();
					int solsDiff = (int) Math.round((MarsClock.getTimeDiff(arrivingTime, marsClock) / 1000D));
					solsList.add(solsDiff);
				}
			}
		}
		// System.out.println("sols.size() : " + sols.size() );

		return solsList;
	}

	public void enableButton(boolean value) {
		if (modifyTransportItemDialog != null)
			modifyTransportItemDialog.setCommitButton(value);
		else if (newTransportItemDialog != null)
			newTransportItemDialog.setCreateButton(value);
	}

//	/**
//	 * Installs a listener to receive notification when the text of any
//	 * {@code JTextComponent} is changed. Internally, it installs a
//	 * {@link DocumentListener} on the text component's {@link Document},
//	 * and a {@link PropertyChangeListener} on the text component to detect
//	 * if the {@code Document} itself is replaced.
//	 *
//	 * @param text any text component, such as a {@link JTextField}
//	 *        or {@link JTextArea}
//	 * @param changeListener a listener to receieve {@link ChangeEvent}s
//	 *        when the text is changed; the source object for the events
//	 *        will be the text component
//	 * @throws NullPointerException if either parameter is null
//	 */
	// see
	// http://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
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
				// System.out.println("calling addChangeListener()'s changedUpdate()");
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

	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {

		Arrays.fill(sols, null);
		sols = null;
		Arrays.fill(quantity, null);
		quantity = null;
		Arrays.fill(immigrants, null);
		immigrants = null;
		destinationCB = null;
		arrivalDateRB = null;
		arrivalDateTitleLabel = null;
		timeUntilArrivalRB = null;
		timeUntilArrivalLabel = null;
		martianSolCBModel = null;
		solLabel = null;
		solCB = null;
		solsFromCB = null;
		immigrantsCB = null;
		monthLabel = null;
		monthCB = null;
		orbitLabel = null;
		orbitCB = null;
		solInfoLabel = null;
		supplyTableModel = null;
		supplyTable = null;
		removeSupplyButton = null;
		errorLabel = null;
		resupply = null;
		newTransportItemDialog = null;
		modifyTransportItemDialog = null;
		resupplyWindow = null;
		marsCurrentTime = null;
	}

}