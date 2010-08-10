/**
 * Mars Simulation Project
 * SearchWindow.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.search;  
 
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/** 
 * The SearchWindow is a tool window that allows the user to search
 * for individual units by name and category.
 */
public class SearchWindow extends ToolWindow {

	// Tool name
	public static final String NAME = "Search Tool";
	
    // Data members
    private JComboBox searchForSelect; // Category selecter
    private JList unitList; // List of selectable units
    private DefaultListModel unitListModel; // Model for unit select list
    private JTextField selectTextField; // Selection text field
    private JLabel statusLabel; // Status label for displaying warnings.
    private JCheckBox openWindowCheck; // Checkbox to indicate if unit window is to be opened.
    private JCheckBox centerMapCheck; // Checkbox to indicate if map is to be centered on unit.
    private boolean lockUnitList; // True if unitList selection events should be ignored.
    private boolean lockSearchText; // True if selectTextField events should be ignored.
    private String[] unitCategoryNames; // Array of category names.

    /** 
     * Constructor
     *
     * @param desktop the desktop pane
     */
    public SearchWindow(MainDesktopPane desktop) {

        // Use ToolWindow constructor
        super(NAME, desktop);

        // Initialize locks
        lockUnitList = false;
        lockSearchText = false;

        // Initialize unitCategoryNames
        unitCategoryNames = new String[3];
        unitCategoryNames[0] = "Person";
        unitCategoryNames[1] = "Settlement";
        unitCategoryNames[2] = "Vehicle";

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new MarsPanelBorder());
        setContentPane(mainPane);

        // Create search for panel
        JPanel searchForPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainPane.add(searchForPane, "North");

        // Create search for label
        JLabel searchForLabel = new JLabel("Search for: ");
        searchForPane.add(searchForLabel);

        // Create search for select
        String[] categoryStrings = { "People", "Settlements", "Vehicles" };
        searchForSelect = new JComboBox(categoryStrings);
        searchForSelect.setSelectedIndex(0);
        searchForSelect.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent event) {
            	changeCategory((String) searchForSelect.getSelectedItem());
            }
        });
        searchForPane.add(searchForSelect);

        // Create select unit panel
        JPanel selectUnitPane = new JPanel(new BorderLayout());
        mainPane.add(selectUnitPane, "Center");

        // Create select text field
        selectTextField = new JTextField();
        selectTextField.getDocument().addDocumentListener(new DocumentListener() {
        	public void changedUpdate(DocumentEvent event) {}
            public void insertUpdate(DocumentEvent event) {
                searchTextChange();
            }
            public void removeUpdate(DocumentEvent event) {
                searchTextChange();
            }
        });
        selectUnitPane.add(selectTextField, "North");

        // Create unit list
        unitListModel = new DefaultListModel();
        UnitManager unitManager = Simulation.instance().getUnitManager();
        Collection persons = CollectionUtils.sortByName(unitManager.getPeople());
        Iterator people = persons.iterator();
        
        while (people.hasNext()) unitListModel.addElement(people.next());
        unitList = new JList(unitListModel);
        unitList.setSelectedIndex(0);
        unitList.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent event) {
                if (event.getClickCount() == 2) search();
                else if (!lockUnitList) {
                    // Change search text to selected name.
                    String selectedUnitName = ((Unit) unitList.getSelectedValue()).getName();
                    lockSearchText = true;
                    if (!selectTextField.getText().equals(selectedUnitName))
                        selectTextField.setText(selectedUnitName);
                    lockSearchText = false;
                }
            }
        });
        selectUnitPane.add(new JScrollPane(unitList), "Center");
    
        // Create bottom panel
        JPanel bottomPane = new JPanel(new BorderLayout());
        mainPane.add(bottomPane, "South");

        // Create select options panel
        JPanel selectOptionsPane = new JPanel(new GridLayout(2, 1));
        bottomPane.add(selectOptionsPane, "North");

        // Create open window option check box
        openWindowCheck = new JCheckBox("Open Detail Window");
        openWindowCheck.setSelected(true);
        selectOptionsPane.add(openWindowCheck);

        // Create center map option
        centerMapCheck = new JCheckBox("Recenter Mars Navigator");
        selectOptionsPane.add(centerMapCheck);

        // Create status label
        statusLabel = new JLabel(" ", JLabel.CENTER);
        statusLabel.setBorder(new EtchedBorder());
        bottomPane.add(statusLabel, "Center");

        // Create search button panel
        JPanel searchButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPane.add(searchButtonPane, "South");

        // Create search button
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
                search();
            }
        });
        searchButtonPane.add(searchButton);

        // Pack window
        pack();
    }

    /**
     * Search for named unit when button is pushed.
     * Retrieve info on all units of selected category.
     */
    private void search() {
        Collection<? extends Unit> units = null;
        String category = (String) searchForSelect.getSelectedItem();
        UnitManager unitManager = Simulation.instance().getUnitManager();
        if (category.equals("People")) {
            Collection<Person> people = unitManager.getPeople();
            units = CollectionUtils.sortByName(people);
        }
        else if (category.equals("Settlements")) {
            Collection<Settlement> settlement = unitManager.getSettlements();
            units = CollectionUtils.sortByName(settlement);
        }
        else if (category.equals("Vehicles")) {
            Collection<Vehicle> vehicle = unitManager.getVehicles();
            units = CollectionUtils.sortByName(vehicle); 
        }
  
        Iterator<? extends Unit> unitI = units.iterator();

        // If entered text equals the name of a unit in this category, take appropriate action.
        boolean foundUnit = false;
        while (unitI.hasNext()) {
            Unit unit = unitI.next();
            if (selectTextField.getText().equalsIgnoreCase(unit.getName())) {
                foundUnit = true;
                if (openWindowCheck.isSelected()) desktop.openUnitWindow(unit, false);
                if (centerMapCheck.isSelected())
                    desktop.centerMapGlobe(unit.getCoordinates());
            }
        }

        String tempName = unitCategoryNames[searchForSelect.getSelectedIndex()];

        // If not found, display "'Category' Not Found" in statusLabel.
        if (!foundUnit) statusLabel.setText(tempName + " Not Found");

        // If there is no text entered, display "Enter The Name of a 'Category'" in statusLabel.
        if (selectTextField.getText().equals(""))
            statusLabel.setText("Enter The Name of a " + tempName);
    }

    /**
     * Change the category of the unit list.
     * @param category
     */
    private void changeCategory(String category) {
        // Change unitList to the appropriate category list
        unitListModel.clear();
        Collection<? extends Unit> units = null;
        UnitManager unitManager = Simulation.instance().getUnitManager();
        if (category.equals("People")) {
            Collection<Person> people = unitManager.getPeople();
            units = CollectionUtils.sortByName(people);   
        }
        else if (category.equals("Settlements")) {
            Collection<Settlement> settlement = unitManager.getSettlements();
            units = CollectionUtils.sortByName(settlement);
        }
        else if (category.equals("Vehicles")) {
            Collection<Vehicle> vehicle= unitManager.getVehicles();
            units = CollectionUtils.sortByName(vehicle);
        }

        Iterator<? extends Unit> unitI = units.iterator();

        lockUnitList = true;
        while (unitI.hasNext()) unitListModel.addElement(unitI.next());
        unitList.setSelectedIndex(0);
        unitList.ensureIndexIsVisible(0);
        lockUnitList = false;

        // Clear statusLabel.
        statusLabel.setText(" ");
    }

    /** 
     * Make selection in list depending on what unit names 
     * begin with the changed text. 
     */
    private void searchTextChange() {
        if (!lockSearchText) {
            String searchText = selectTextField.getText().toLowerCase();
            int fitIndex = 0;
            boolean goodFit = false;
            for (int x = unitListModel.size() - 1; x > -1; x--) {
                Unit unit = (Unit) unitListModel.elementAt(x);
                String unitString = unit.getName().toLowerCase();
                if (unitString.startsWith(searchText)) {
                    fitIndex = x;
                    goodFit = true;
                }
            }
            if (goodFit) {
                lockUnitList = true;
                unitList.setSelectedIndex(fitIndex);
                unitList.ensureIndexIsVisible(fitIndex);
                lockUnitList = false;
            }
        }

        // Clear statusLabel
        statusLabel.setText(" ");
    }
}