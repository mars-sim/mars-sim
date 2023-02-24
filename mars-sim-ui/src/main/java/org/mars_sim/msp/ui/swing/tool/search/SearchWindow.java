/*
 * Mars Simulation Project
 * SearchWindow.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;


/**
 * The SearchWindow is a tool window that allows the user to search
 * for individual units by name and category.
 */
@SuppressWarnings("serial")
public class SearchWindow
extends ToolWindow {
	
	private static final Logger logger = Logger.getLogger(SearchWindow.class.getName());
	
	/** Tool name. */
	public static final String NAME = Msg.getString("SearchWindow.title"); //$NON-NLS-1$
	public static final String ICON = "action/find";

	/** Unit categories enum. */
	enum UnitCategory {
		PEOPLE 			(Msg.getString("SearchWindow.category.people")), //$NON-NLS-1$	
		SETTLEMENTS 	(Msg.getString("SearchWindow.category.settlements")), //$NON-NLS-1$
		VEHICLES 		(Msg.getString("SearchWindow.category.vehicles")), //$NON-NLS-1$
		BOTS 			(Msg.getString("SearchWindow.category.bots")); //$NON-NLS-1$
		
		private String name;
		private UnitCategory(String name) {
			this.name = name;
		}
		public String getName() {
			return this.name;
		}
		public static UnitCategory fromName(String name) {
		    if (name != null) {
		        for (UnitCategory b : UnitCategory.values()) {
		            if (name.equalsIgnoreCase(b.name)) {
		                return b;
		            }
		        }
		    }
		    throw new IllegalArgumentException("No UnitCategory with name " + name + " found");
		}
	}

	/** True if unitList selection events should be ignored. */
	private boolean lockUnitList;
	/** True if selectTextField events should be ignored. */
	private boolean lockSearchText;
	/** Array of category names. */
	private String[] unitCategoryNames;
	
	/** Category selector. */
	private JComboBoxMW<?> searchForSelect;
	/** List of selectable units. */
	private JList<Unit> unitList;
	/** Model for unit select list. */
	private UnitListModel unitListModel;
	/** Selection text field. */
	private JTextField selectTextField;
	/** Status label for displaying warnings. */
	private JLabel statusLabel;
	/** Checkbox to indicate if unit window is to be opened. */
	private JCheckBox openWindowCheck;
	/** Checkbox to indicate if mars navigator map is to be centered on unit. */
	private JCheckBox marsNavCheck;
	/** Checkbox to indicate if the settlement map is to be centered on unit. */
	private JCheckBox settlementCheck;
	/** Button to execute the search of the selected unit. */
	private JButton searchButton;

	private UnitManager unitManager;

	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public SearchWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);
		unitManager = desktop.getSimulation().getUnitManager();
	
		// Initialize locks
		lockUnitList = false;
		lockSearchText = false;

		// Initialize unitCategoryNames
		unitCategoryNames = new String[4];
		unitCategoryNames[0] = UnitCategory.PEOPLE.getName();
		unitCategoryNames[1] = UnitCategory.SETTLEMENTS.getName();
		unitCategoryNames[2] = UnitCategory.VEHICLES.getName();
		unitCategoryNames[3] = UnitCategory.BOTS.getName();
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create search for panel
		JPanel searchForPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		searchForPane.setPreferredSize(new Dimension(240, 26));
		mainPane.add(searchForPane, BorderLayout.NORTH);

		// Create search for label
		JLabel searchForLabel = new JLabel(Msg.getString("SearchWindow.searchFor")); //$NON-NLS-1$
		searchForPane.add(searchForLabel);

		// Create search for select
		String[] categoryStrings = {
			UnitCategory.PEOPLE.getName(),
			UnitCategory.SETTLEMENTS.getName(),
			UnitCategory.VEHICLES.getName(),
			UnitCategory.BOTS.getName()
		};
		searchForSelect = new JComboBoxMW<Object>(categoryStrings);
		searchForSelect.setSelectedIndex(0);
		searchForSelect.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				changeCategory((String) searchForSelect.getSelectedItem());
			}
		});
		searchForPane.add(searchForSelect);

		// Create select unit panel
		JPanel selectUnitPane = new JPanel(new BorderLayout());
		mainPane.add(selectUnitPane, BorderLayout.CENTER);

		// Create select text field
		selectTextField = new JTextField();
		selectTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent event) {
				// Not needed
			}
			public void insertUpdate(DocumentEvent event) {
				searchTextChange();
				searchButton.setEnabled(true);
			}
			public void removeUpdate(DocumentEvent event) {
				searchTextChange();
				searchButton.setEnabled(false);
			}
		});
		selectUnitPane.add(selectTextField, BorderLayout.NORTH);

		// Create unit list
		unitListModel = new UnitListModel(UnitCategory.PEOPLE);
		unitList = new JList<>(unitListModel);
		unitList.setSelectedIndex(0);
		unitList.addMouseListener(new MouseAdapter() {
			@Override
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
		selectUnitPane.add(new JScrollPane(unitList), BorderLayout.CENTER);

		// Create bottom panel
		JPanel bottomPane = new JPanel(new BorderLayout());
		mainPane.add(bottomPane, BorderLayout.SOUTH);

		// Create select options panel
		JPanel selectOptionsPane = new JPanel(new GridLayout(2, 1));
		bottomPane.add(selectOptionsPane, BorderLayout.NORTH);

		// Create open the unit window
		openWindowCheck = new JCheckBox(Msg.getString("SearchWindow.openWindow")); //$NON-NLS-1$
		openWindowCheck.setSelected(true);
		selectOptionsPane.add(openWindowCheck);

		// Create open the mars navigator
		marsNavCheck = new JCheckBox(Msg.getString("SearchWindow.openNav")); //$NON-NLS-1$
		selectOptionsPane.add(marsNavCheck);

		// Create open the settlement map
		settlementCheck = new JCheckBox(Msg.getString("SearchWindow.openSettlement")); //$NON-NLS-1$
		selectOptionsPane.add(settlementCheck);

		// Create status label
		statusLabel = new JLabel(" ", JLabel.CENTER); //$NON-NLS-1$
		statusLabel.setBorder(new EtchedBorder());
		bottomPane.add(statusLabel, BorderLayout.CENTER);

		// Create search button panel
		JPanel searchButtonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPane.add(searchButtonPane, BorderLayout.SOUTH);

		// Create search button
		searchButton = new JButton(Msg.getString("SearchWindow.button.search")); //$NON-NLS-1$
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				search();
			}
		});
		searchButton.setEnabled(false);
		searchButton.setToolTipText(Msg.getString("SearchWindow.button.toolTip"));
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
		if (category.equals(UnitCategory.PEOPLE.getName())) {
			Collection<Person> people = unitManager.getPeople();
			units = CollectionUtils.sortByName(people);
		}
		else if (category.equals(UnitCategory.SETTLEMENTS.getName())) {
			Collection<Settlement> settlement = unitManager.getSettlements();
			units = CollectionUtils.sortByName(settlement);
		}
		else if (category.equals(UnitCategory.VEHICLES.getName())) {
			Collection<Vehicle> vehicle = unitManager.getVehicles();
			units = CollectionUtils.sortByName(vehicle);
		}
		else if (category.equals(UnitCategory.BOTS.getName())) {
			Collection<Robot> bots = unitManager.getRobots();
			units = CollectionUtils.sortByName(bots);
		}
		
		Iterator<? extends Unit> unitI = units.iterator();

		// If entered text equals the name of a unit in this category, take appropriate action.
		boolean foundUnit = false;
		while (unitI.hasNext()) {
			Unit unit = unitI.next();
			if (selectTextField.getText().equalsIgnoreCase(unit.getName())) {
				foundUnit = true;
				if (openWindowCheck.isSelected()) desktop.openUnitWindow(unit, false);
				
				if (marsNavCheck.isSelected()) {
					NavigatorWindow nw = (NavigatorWindow) desktop.openToolWindow(NavigatorWindow.NAME);
					nw.updateCoords(unit.getCoordinates());
				}
				
				if (settlementCheck.isSelected())
					 openUnit(unit);
			}
		}

		String tempName = unitCategoryNames[searchForSelect.getSelectedIndex()];

		// If not found, display "'Category' Not Found" in statusLabel.
		if (!foundUnit) statusLabel.setText(Msg.getString("SearchWindow.unitNotFound",tempName)); //$NON-NLS-1$

		// If there is no text entered, display "Enter The Name of a 'Category'" in statusLabel.
		if (selectTextField.getText().length() == 0)
			statusLabel.setText(Msg.getString("SearchWindow.defaultSearch",tempName)); //$NON-NLS-1$
	}

	public void openUnit(Unit u) {
		
		if (u.isInSettlement()) {
			
			showPersonRobot(u);
		}

		else if (u.isInVehicle()) {

			Vehicle vv = u.getVehicle();

			if (vv.getSettlement() == null) {
				// person is on a mission on the surface of Mars 
				NavigatorWindow nw = (NavigatorWindow) desktop.openToolWindow(NavigatorWindow.NAME);
				nw.updateCoords(vv.getCoordinates());
			} 
			
			else {
				// still parked inside a garage or within the premise of a settlement
				showPersonRobot(u);
			}
		}

		else if (u.isOutside()) {
			Vehicle vv = u.getVehicle();

			if (vv == null) {
				// if it's not in a vehicle
				showPersonRobot(u);			
			}
			
			else {
				// if it's in a vehicle			
				if (vv.getSettlement() != null) {
					// if the vehicle is in a settlement
					showPersonRobot(u);
				}
				
				else {
					// person is on a mission on the surface of Mars 
					desktop.openToolWindow(NavigatorWindow.NAME);
					// he's stepped outside a vehicle
					desktop.centerMapGlobe(u.getCoordinates());
				}
			}
		}
	}
	
	private void showPersonRobot(Unit u) {
		// person just happens to step outside the settlement at its
		// vicinity temporarily

		SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
		
		if (u instanceof Person) {
			Person p = (Person) u;
			sw.displayPerson(p);
		} 
		
		else { 
			Robot r = (Robot)u;
			sw.displayRobot(r);
		}
}
	
	
	/**
	 * Change the category of the unit list.
	 * @param category
	 */
	private void changeCategory(String category) {
		// Change unitList to the appropriate category list
		lockUnitList = true;
		unitListModel.updateCategory(UnitCategory.fromName(category));
		unitList.setSelectedIndex(0);
		unitList.ensureIndexIsVisible(0);
		lockUnitList = false;

		// Clear statusLabel.
		statusLabel.setText(" "); //$NON-NLS-1$
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
				Unit unit = unitListModel.elementAt(x);
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
		statusLabel.setText(" "); //$NON-NLS-1$
	}

	@Override
	public void destroy() {
		super.destroy();
		
		if (unitListModel != null) {
			unitListModel.clear();
			unitListModel = null;
		}
	}

	/**
	 * Inner class list model for categorized units.
	 */
	private class UnitListModel
	extends DefaultListModel<Unit> {

		private static final long serialVersionUID = 1L;
		// Data members.
		private UnitCategory category;

		/**
		 * Constructor
		 * @param initialCategory the initial category to display.
		 */
		public UnitListModel(UnitCategory initialCategory) {

			// Use DefaultListModel constructor.
			super();

			// Initialize data members.
			this.category = initialCategory;

			updateList();
		}

		/**
		 * Updates the category.
		 * @param category the list category
		 */
		private void updateCategory(UnitCategory category) {
			if (!this.category.equals(category)) {
				this.category = category;

				updateList();
			}
		}

		/**
		 * Updates the list items.
		 */
		private void updateList() {

			clear();

			Collection<? extends Unit> units = null;

			if (category.equals(UnitCategory.PEOPLE)) {
				Collection<Person> people = unitManager.getPeople();
				units = CollectionUtils.sortByName(people);
			}
			else if (category.equals(UnitCategory.SETTLEMENTS)) {
				Collection<Settlement> settlement = unitManager.getSettlements();
				units = CollectionUtils.sortByName(settlement);
			}
			else if (category.equals(UnitCategory.VEHICLES)) {
				Collection<Vehicle> vehicle = unitManager.getVehicles();
				units = CollectionUtils.sortByName(vehicle);
			}
			else if (category.equals(UnitCategory.BOTS)) {
				Collection<Robot> bots = unitManager.getRobots();
				units = CollectionUtils.sortByName(bots);
			}
			
			if (units != null && !units.isEmpty()) {
				Iterator<? extends Unit> unitI = units.iterator();
	
				while (unitI.hasNext()) {
					addElement(unitI.next());
				}
			}
		}
	}
}
