/**
 * Mars Simulation Project
 * SearchWindow.java
 * @version 2.75 2003-09-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.search;  
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/** 
 * The SearchWindow is a tool window that allows the user to search
 * for individual units by name and category.
 */
public class SearchWindow extends ToolWindow implements ActionListener,
        ItemListener, MouseListener, DocumentListener {

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
        super("Search Tool", desktop);

        // Initialize locks
        lockUnitList = false;
        lockSearchText = false;

        // Initialize unitCategoryNames
        unitCategoryNames = new String[3];
        unitCategoryNames[0] = "Person";
        unitCategoryNames[1] = "Settlement";
        unitCategoryNames[2] = "Vehicle";

        // Initialize data members
        this.desktop = desktop;

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
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
        searchForSelect.addItemListener(this);
        searchForPane.add(searchForSelect);

        // Create select unit panel
        JPanel selectUnitPane = new JPanel(new BorderLayout());
        mainPane.add(selectUnitPane, "Center");

        // Create select text field
        selectTextField = new JTextField();
        selectTextField.getDocument().addDocumentListener(this);
        selectUnitPane.add(selectTextField, "North");

        // Create unit list
        unitListModel = new DefaultListModel();
        UnitManager unitManager = desktop.getMainWindow().getMars().getUnitManager();
        PersonIterator people = unitManager.getPeople().sortByName().iterator();
        while (people.hasNext()) unitListModel.addElement(people.next());
        unitList = new JList(unitListModel);
        unitList.setSelectedIndex(0);
        unitList.addMouseListener(this);
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
        searchButton.addActionListener(this);
        searchButtonPane.add(searchButton);

        // Pack window
        pack();
    }

    /**
     * Search for named unit when button is pushed.
     * Retrieve info on all units of selected category.
     */
    private void search() {
        UnitCollection units = new UnitCollection();
        String category = (String) searchForSelect.getSelectedItem();
        UnitManager unitManager = desktop.getMainWindow().getMars().getUnitManager();
        if (category.equals("People"))
            units.mergePeople(unitManager.getPeople().sortByName());
        if (category.equals("Settlements"))
            units.mergeSettlements(unitManager.getSettlements().sortByName());
        if (category.equals("Vehicles"))
            units.mergeVehicles(unitManager.getVehicles().sortByName());
        UnitIterator unitI = units.iterator();

        // If entered text equals the name of a unit in this category, take appropriate action.
        boolean foundUnit = false;
        while (unitI.hasNext()) {
            Unit unit = unitI.next();
            if (selectTextField.getText().equalsIgnoreCase(unit.getName())) {
                foundUnit = true;
                if (openWindowCheck.isSelected()) desktop.openUnitWindow(unit);
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

    /** ItemListener method overridden */
    public void itemStateChanged(ItemEvent event) {

        // Change unitList to the appropriate category list
        unitListModel.clear();
        UnitCollection units = new UnitCollection();
        String category = (String) searchForSelect.getSelectedItem();
        UnitManager unitManager = desktop.getMainWindow().getMars().getUnitManager();
        if (category.equals("People"))
            units.mergePeople(unitManager.getPeople().sortByName());
        if (category.equals("Settlements"))
            units.mergeSettlements(unitManager.getSettlements().sortByName());
        if (category.equals("Vehicles"))
            units.mergeVehicles(unitManager.getVehicles().sortByName());
        UnitIterator unitI = units.iterator();

        lockUnitList = true;
        while (unitI.hasNext()) unitListModel.addElement(unitI.next());
        unitList.setSelectedIndex(0);
        unitList.ensureIndexIsVisible(0);
        lockUnitList = false;

        // Clear statusLabel.
        statusLabel.setText(" ");
    }

    /** ActionListener method overridden */
    public void actionPerformed(ActionEvent event) {
        search();
    }

    // MouseListener methods overridden
    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    public void mouseReleased(MouseEvent event) {
        if(event.getClickCount() == 2) {
            search();
            return;
        }

        if (!lockUnitList) {
            // Change search text to selected name.
            String selectedUnitName = ((Unit) unitList.getSelectedValue()).getName();
            lockSearchText = true;
            if (!selectTextField.getText().equals(selectedUnitName))
                selectTextField.setText(selectedUnitName);
            lockSearchText = false;
        }
    }

    // DocumentListener methods overridden
    public void changedUpdate(DocumentEvent event) {}
    public void insertUpdate(DocumentEvent event) {
        searchTextChange();
    }
    public void removeUpdate(DocumentEvent event) {
        searchTextChange();
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
