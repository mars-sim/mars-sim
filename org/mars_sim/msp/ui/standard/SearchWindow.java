/**
 * Mars Simulation Project
 * SearchWindow.java
 * @version 2.71 2000-10-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import org.mars_sim.msp.simulation.*;  
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** The SearchWindow is a tool window that allows the user to search
 *  for individual units by name and category.
 */
public class SearchWindow extends ToolWindow implements ActionListener,
        ItemListener, MouseListener, DocumentListener {

    // Data members
    private MainDesktopPane desktop; // Desktop pane
    private UnitUIProxy[] peopleProxies; // List of person UI proxies
    private UnitUIProxy[] settlementProxies; // List of settlement UI proxies
    private UnitUIProxy[] vehicleProxies; // List of vehicle proxies
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

    /** Constructs a SearchWindow object 
     *  @param desktop the desktop pane
     */
    public SearchWindow(MainDesktopPane desktop) {

        // Use ToolWindow constructor
        super("Search Tool");

        // Initialize locks
        lockUnitList = false;
        lockSearchText = false;

        // Initialize unitCategoryNames
        unitCategoryNames = new String[3];
        unitCategoryNames[0] = "Person";
        unitCategoryNames[1] = "Settlement";
        unitCategoryNames[2] = "Vehicle";

        // Set internal frame listener
        addInternalFrameListener(new ViewFrameListener());

        // Initialize data members
        this.desktop = desktop;

        // Initialize unit UI proxy lists
        UIProxyManager proxyManager = desktop.getProxyManager();
        peopleProxies = proxyManager.getOrderedPeopleProxies();
        settlementProxies = proxyManager.getOrderedSettlementProxies();
        vehicleProxies = proxyManager.getOrderedVehicleProxies();

        // Get content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);

        // Create search for panel
        JPanel searchForPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainPane.add(searchForPane, "North");

        // Create search for label
        JLabel searchForLabel = new JLabel("Search for: ");
        searchForLabel.setForeground(Color.black);
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
        for (int x = 0; x < peopleProxies.length; x++)
            unitListModel.addElement(peopleProxies[x].getUnit().getName());
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
        statusLabel.setForeground(Color.red);
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

    /** ActionListener method overridden */
    public void actionPerformed(ActionEvent event) {

        // Search for named unit when button is pushed.
        // Retrieve info on all units of selected category.
        UnitUIProxy[] unitProxies = new UnitUIProxy[0];
        String category = (String) searchForSelect.getSelectedItem();
        if (category.equals("People"))
            unitProxies = peopleProxies;
        if (category.equals("Settlements"))
            unitProxies = settlementProxies;
        if (category.equals("Vehicles"))
            unitProxies = vehicleProxies;

        // If entered text equals the name of a unit in this category, take appropriate action.
        boolean foundUnit = false;
        for (int x = 0; x < unitProxies.length; x++) {
            if (selectTextField.getText().equalsIgnoreCase(unitProxies[x].getUnit().getName())) {
                foundUnit = true;
                if (openWindowCheck.isSelected())
                    desktop.openUnitWindow(unitProxies[x]);
                if (centerMapCheck.isSelected())
                    desktop.centerMapGlobe(unitProxies[x].getUnit().getCoordinates());
            }
        }

        String tempName = unitCategoryNames[searchForSelect.getSelectedIndex()];

        // If not found, display "'Category' Not Found" in statusLabel.
        if (!foundUnit)
            statusLabel.setText(tempName + " Not Found");

        // If there is no text entered, display "Enter The Name of a 'Category'" in statusLabel.
        if (selectTextField.getText().equals(""))
            statusLabel.setText("Enter The Name of a " + tempName);
    }

    /** ItemListener method overridden */
    public void itemStateChanged(ItemEvent event) {

        // Change unitList to the appropriate category list
        unitListModel.clear();
        UnitUIProxy[] unitProxies = new UnitUIProxy[0];

        String category = (String) searchForSelect.getSelectedItem();
        if (category.equals("People"))
            unitProxies = peopleProxies;
        if (category.equals("Settlements"))
            unitProxies = settlementProxies;
        if (category.equals("Vehicles"))
            unitProxies = vehicleProxies;

        lockUnitList = true;
        for (int x = 0; x < unitProxies.length; x++)
            unitListModel.addElement(unitProxies[x].getUnit().getName());
        unitList.setSelectedIndex(0);
        unitList.ensureIndexIsVisible(0);
        lockUnitList = false;

        // Clear statusLabel.
        statusLabel.setText(" ");
    }

    // MouseListener methods overridden
    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}

    public void mouseReleased(MouseEvent event) {
        if (!lockUnitList) {
            // Change search text to selected name.
            String selectedUnitName = (String) unitList.getSelectedValue();
            lockSearchText = true;
            if (!selectTextField.getText().equals(selectedUnitName)) {
                selectTextField.setText(selectedUnitName);
            }
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

    /** Make selection in list depending on what unit names begin with the changed text. */
    private void searchTextChange() {
        if (!lockSearchText) {
            String searchText = selectTextField.getText().toLowerCase();
            int fitIndex = 0;
            boolean goodFit = false;
            for (int x = unitListModel.size() - 1; x > -1; x--) {
                String unitString =
                        ((String) unitListModel.elementAt(x)).toLowerCase();
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
