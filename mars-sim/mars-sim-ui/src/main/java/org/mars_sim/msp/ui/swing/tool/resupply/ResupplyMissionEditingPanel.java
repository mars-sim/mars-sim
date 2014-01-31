/**
 * Mars Simulation Project
 * ResupplyMissionEditingPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.interplanetary.transport.Transportable;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.interplanetary.transport.resupply.ResupplyUtil;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.resupply.SupplyTableModel.SupplyItem;

/**
 * A panel for creating or editing a resupply mission.
 */
public class ResupplyMissionEditingPanel extends TransportItemEditingPanel {

    /** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
    private Resupply resupply;
    private JComboBoxMW<Settlement> destinationCB;
    private JRadioButton arrivalDateRB;
    private JLabel arrivalDateTitleLabel;
    private JRadioButton timeUntilArrivalRB;
    private JLabel timeUntilArrivalLabel;
    private MartianSolComboBoxModel martianSolCBModel;
    private JLabel solLabel;
    private JComboBoxMW<?> solCB;
    private JLabel monthLabel;
    private JComboBoxMW<?> monthCB;
    private JLabel orbitLabel;
    private JComboBoxMW<?> orbitCB;
    private JTextField solsTF;
    private JLabel solInfoLabel;
    private JTextField immigrantsTF;
    private SupplyTableModel supplyTableModel;
    private JTable supplyTable;
    private JButton removeSupplyButton;
    
    public ResupplyMissionEditingPanel(Resupply resupply) {
        // User TransportItemEditingPanel constructor.
        super(resupply);
        
        // Initialize data members.
        this.resupply = resupply;
        
        setBorder(new MarsPanelBorder());
        setLayout(new BorderLayout(0, 0));
        
        // Create top edit pane.
        JPanel topEditPane = new JPanel(new BorderLayout(10, 10));
        add(topEditPane, BorderLayout.NORTH);

        // Create destination pane.
        JPanel destinationPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topEditPane.add(destinationPane, BorderLayout.NORTH);

        // Create destination title label.
        JLabel destinationTitleLabel = new JLabel("Destination: ");
        destinationPane.add(destinationTitleLabel);

        // Create destination combo box.
        Vector<Settlement> settlements = new Vector<Settlement>(
                Simulation.instance().getUnitManager().getSettlements());
        Collections.sort(settlements);
        destinationCB = new JComboBoxMW<Settlement>(settlements);
        if (resupply != null) {
            destinationCB.setSelectedItem(resupply.getSettlement());
        }
        destinationPane.add(destinationCB);

        // Create arrival date pane.
        JPanel arrivalDatePane = new JPanel(new GridLayout(2, 1, 10, 10));
        arrivalDatePane.setBorder(new TitledBorder("Arrival Date"));
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
        arrivalDateTitleLabel = new JLabel("Arrival Date:");
        arrivalDateSelectionPane.add(arrivalDateTitleLabel);

        // Get default resupply Martian time.
        MarsClock resupplyTime = Simulation.instance().getMasterClock().getMarsClock();
        if (resupply != null) {
            resupplyTime = resupply.getArrivalDate();
        }

        // Create sol label.
        solLabel = new JLabel("Sol");
        arrivalDateSelectionPane.add(solLabel);

        // Create sol combo box.
        martianSolCBModel = new MartianSolComboBoxModel(resupplyTime.getMonth(), resupplyTime.getOrbit());
        solCB = new JComboBoxMW<Integer>(martianSolCBModel);
        solCB.setSelectedItem(resupplyTime.getSolOfMonth());
        arrivalDateSelectionPane.add(solCB);

        // Create month label.
        monthLabel = new JLabel("Month");
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

        // Create orbit label.
        orbitLabel = new JLabel("Orbit");
        arrivalDateSelectionPane.add(orbitLabel);

        // Create orbit combo box.
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumIntegerDigits(2);
        String[] orbitValues = new String[20];
        int startOrbit = resupplyTime.getOrbit();
        for (int x = 0; x < 20; x++) {
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

        // Create time until arrival pane.
        JPanel timeUntilArrivalPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        arrivalDatePane.add(timeUntilArrivalPane);

        // Create time until arrival radio button.
        timeUntilArrivalRB = new JRadioButton();
        dateTypeRBGroup.add(timeUntilArrivalRB);
        timeUntilArrivalRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                JRadioButton rb = (JRadioButton) evt.getSource();
                setEnableTimeUntilArrivalPane(rb.isSelected());
                setEnableArrivalDatePane(!rb.isSelected());
            }
        });
        timeUntilArrivalPane.add(timeUntilArrivalRB);

        // create time until arrival label.
        timeUntilArrivalLabel = new JLabel("Sols Until Arrival:");
        timeUntilArrivalLabel.setEnabled(false);
        timeUntilArrivalPane.add(timeUntilArrivalLabel);

        // Create sols text field.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        int solsDiff = (int) Math.round((MarsClock.getTimeDiff(resupplyTime, currentTime) / 1000D));
        solsTF = new JTextField(6);
        solsTF.setText(Integer.toString(solsDiff));
        solsTF.setHorizontalAlignment(JTextField.RIGHT);
        solsTF.setEnabled(false);
        timeUntilArrivalPane.add(solsTF);

        // Create sol information label.
        solInfoLabel = new JLabel("(668 Sols = 1 Martian Orbit)");
        solInfoLabel.setEnabled(false);
        timeUntilArrivalPane.add(solInfoLabel);

        // Create immigrants panel.
        JPanel immigrantsPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topEditPane.add(immigrantsPane, BorderLayout.SOUTH);

        // Create immigrants label.
        JLabel immigrantsLabel = new JLabel("Number of Immigrants: ");
        immigrantsPane.add(immigrantsLabel);

        // Create immigrants text field.
        int immigrantsNum = 0;
        if (resupply != null) {
            immigrantsNum = resupply.getNewImmigrantNum();
        }
        immigrantsTF = new JTextField(6);
        immigrantsTF.setText(Integer.toString(immigrantsNum));
        immigrantsTF.setHorizontalAlignment(JTextField.RIGHT);
        immigrantsPane.add(immigrantsTF);

        // Create bottom edit pane.
        JPanel bottomEditPane = new JPanel(new BorderLayout(0, 0));
        bottomEditPane.setBorder(new TitledBorder("Supplies"));
        add(bottomEditPane, BorderLayout.CENTER);

        // Create supply table.
        supplyTableModel = new SupplyTableModel(resupply);
        supplyTable = new JTable(supplyTableModel);
        supplyTable.getColumnModel().getColumn(0).setMaxWidth(100);
        supplyTable.getColumnModel().getColumn(0).setCellEditor(new CategoryCellEditor());
        supplyTable.getColumnModel().getColumn(1).setMaxWidth(200);
        supplyTable.getColumnModel().getColumn(1).setCellEditor(new TypeCellEditor());
        supplyTable.getColumnModel().getColumn(2).setMaxWidth(150);
        supplyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    // If rows are selected, enable remove supply button.
                    boolean hasSelection = supplyTable.getSelectedRow() > -1;
                    removeSupplyButton.setEnabled(hasSelection);
                }
            }
        });

        // Create supply scroll pane.
        JScrollPane supplyScrollPane = new JScrollPane(supplyTable);
        supplyScrollPane.setPreferredSize(new Dimension(450, 200));
        bottomEditPane.add(supplyScrollPane, BorderLayout.CENTER);

        // Create supply button pane.
        JPanel supplyButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottomEditPane.add(supplyButtonPane, BorderLayout.SOUTH);

        // Create add supply button.
        JButton addSupplyButton = new JButton("Add");
        addSupplyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                // Add new supply row.
                addNewSupplyRow();
            }
        });
        supplyButtonPane.add(addSupplyButton);

        // Create remove supply button.
        removeSupplyButton = new JButton("Remove");
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
    }

    /**
     * Set the components of the time until arrival pane to be enabled or disabled.
     * @param enable true if enable components, false if disable components.
     */
    private void setEnableTimeUntilArrivalPane(boolean enable) {
        timeUntilArrivalLabel.setEnabled(enable);
        solsTF.setEnabled(enable);
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
    private class CategoryCellEditor extends AbstractCellEditor 
    implements TableCellEditor, ActionListener {

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
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
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
                    categoryCB.addItem(type);
                }
                typeCBMap.put(category, categoryCB);
            }
        }

        @Override
        public Object getCellEditorValue() {
            Object result = null;
            if (currentCB != null) result = currentCB.getSelectedItem();
            return result;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            // Get type combo box based on first column category value.
            String category = (String) table.getValueAt(row, 0);
            currentCB = typeCBMap.get(category);
            currentCB.setSelectedItem(table.getValueAt(row, column));
            return currentCB;
        }
    }
    
    @Override
    public boolean modifyTransportItem() {
        // Modify resupply mission.
        populateResupplyMission(resupply);
        resupply.commitModification();
        return true;
    }

    @Override
    public boolean createTransportItem() {
        // Create new resupply mission.
        Settlement destination = (Settlement) destinationCB.getSelectedItem();
        MarsClock arrivalDate = getArrivalDate();
        Resupply newResupply = new Resupply(arrivalDate, destination);
        populateResupplyMission(newResupply);
        Simulation.instance().getTransportManager().addNewTransportItem(newResupply);
        return true;
    }

    /**
     * Populates a resupply mission from the dialog info.
     * @param resupplyMission the resupply mission to populate.
     */
    private void populateResupplyMission(Resupply resupplyMission) {

        // Set destination settlement.
        Settlement destination = (Settlement) destinationCB.getSelectedItem();
        resupplyMission.setSettlement(destination);

        // Set arrival date.
        MarsClock arrivalDate = getArrivalDate();
        resupplyMission.setArrivalDate(arrivalDate);

        // Determine launch date.
        MarsClock launchDate = (MarsClock) arrivalDate.clone();
        launchDate.addTime(-1D * ResupplyUtil.AVG_TRANSIT_TIME * 1000D);
        resupplyMission.setLaunchDate(launchDate);

        // Set resupply state based on launch and arrival time.
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        String state = Transportable.PLANNED;
        if (MarsClock.getTimeDiff(currentTime, launchDate) > 0D) {
            state = Transportable.IN_TRANSIT;
            if (MarsClock.getTimeDiff(currentTime, arrivalDate) > 0D) {
                state = Transportable.ARRIVED;
            }
        }
        resupplyMission.setTransitState(state);

        // Set immigrant num.
        int immigrantNum = 0;
        try {
            immigrantNum = Integer.parseInt(immigrantsTF.getText());
            if (immigrantNum < 0) immigrantNum = 0;
            resupplyMission.setNewImmigrantNum(immigrantNum);
        }
        catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }

        // Commit any active editing cell in the supply table.
        if (supplyTable.isEditing()) {
            supplyTable.getCellEditor().stopCellEditing();
        }

        List<SupplyItem> supplyItems = supplyTableModel.getSupplyItems();

        // Set new buildings.
        List<String> newBuildings = new ArrayList<String>();
        Iterator<SupplyItem> i = supplyItems.iterator();
        while (i.hasNext()) {
            SupplyItem item = i.next();
            if (SupplyTableModel.BUILDING.equals(item.category.trim())) {
                int num = item.number.intValue();
                for (int x = 0; x < num; x++) {
                    newBuildings.add(item.type.trim());
                }
            }
        }
        resupplyMission.setNewBuildings(newBuildings);

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
                AmountResource resource = AmountResource.findAmountResource(type);
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
                Part part = (Part) Part.findItemResource(type);
                int num = item.number.intValue();
                if (newParts.containsKey(part)) {
                    num += newParts.get(part);
                }
                newParts.put(part, num);
            }
        }
        resupplyMission.setNewParts(newParts);
    }
    
    /**
     * Gets the arrival date from the dialog info.
     * @return arrival date.
     */
    private MarsClock getArrivalDate() {
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        MarsClock result = (MarsClock) currentTime.clone();

        if (arrivalDateRB.isSelected()) {
            // Determine arrival date from arrival date combo boxes.
            try {
                int sol = solCB.getSelectedIndex() + 1;
                int month = monthCB.getSelectedIndex() + 1;
                int orbit = Integer.parseInt((String) orbitCB.getSelectedItem());

                // Set millisols to current time if resupply is current date, otherwise 0.
                double millisols = 0D;
                if ((sol == currentTime.getSolOfMonth()) && (month == currentTime.getMonth()) && 
                        (orbit == currentTime.getOrbit())) {
                    millisols = currentTime.getMillisol();
                }

                result = new MarsClock(orbit, month, sol, millisols);
            }
            catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        }
        else if (timeUntilArrivalRB.isSelected()) {
            // Determine arrival date from time until arrival text field.
            try {
                int solsDiff = Integer.parseInt(solsTF.getText());
                if (solsDiff > 0) {
                    result.addTime(solsDiff * 1000D);
                }
                else {
                    result.addTime(currentTime.getMillisol());
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            }
        }

        return result;
    }
}