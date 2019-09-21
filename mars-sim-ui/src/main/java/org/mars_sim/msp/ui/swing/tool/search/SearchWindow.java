/**
 * Mars Simulation Project
 * SearchWindow.java
 * @version 3.1.0 2019-09-20
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

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;

/**
 * The SearchWindow is a tool window that allows the user to search
 * for individual units by name and category.
 */
@SuppressWarnings("serial")
public class SearchWindow
extends ToolWindow {

	/** Tool name. */
	public static final String NAME = Msg.getString("SearchWindow.title"); //$NON-NLS-1$

	/** Unit categories enum. */
	enum UnitCategory {
		PEOPLE (Msg.getString("SearchWindow.category.people")), //$NON-NLS-1$
		SETTLEMENTS (Msg.getString("SearchWindow.category.settlements")), //$NON-NLS-1$
		VEHICLES (Msg.getString("SearchWindow.category.vehicles")); //$NON-NLS-1$
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

	// Data members
	/** Category selector. */
	private JComboBoxMW<?> searchForSelect;
	/** List of selectable units. */
	private JList<Unit> unitList;
	/** Model for unit select list. */
	private UnitListModel unitListModel;
	/** Selection text field. */
	private WebTextField selectTextField;
	/** Status label for displaying warnings. */
	private WebLabel statusLabel;
	/** Checkbox to indicate if unit window is to be opened. */
	private WebCheckBox openWindowCheck;
	/** Checkbox to indicate if map is to be centered on unit. */
	private WebCheckBox centerMapCheck;
	/** True if unitList selection events should be ignored. */
	private boolean lockUnitList;
	/** True if selectTextField events should be ignored. */
	private boolean lockSearchText;
	/** Array of category names. */
	private String[] unitCategoryNames;

	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public SearchWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);

		// Initialize locks
		lockUnitList = false;
		lockSearchText = false;

		// Initialize unitCategoryNames
		unitCategoryNames = new String[3];
		unitCategoryNames[0] = UnitCategory.PEOPLE.getName();
		unitCategoryNames[1] = UnitCategory.SETTLEMENTS.getName();
		unitCategoryNames[2] = UnitCategory.VEHICLES.getName();

		// Get content pane
		WebPanel mainPane = new WebPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create search for panel
		WebPanel searchForPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		searchForPane.setPreferredSize(new Dimension(240, 26));
		mainPane.add(searchForPane, BorderLayout.NORTH);

		// Create search for label
		WebLabel searchForLabel = new WebLabel(Msg.getString("SearchWindow.searchFor")); //$NON-NLS-1$
		searchForPane.add(searchForLabel);

		// Create search for select
		String[] categoryStrings = {
			UnitCategory.PEOPLE.getName(),
			UnitCategory.SETTLEMENTS.getName(),
			UnitCategory.VEHICLES.getName()
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
		WebPanel selectUnitPane = new WebPanel(new BorderLayout());
		mainPane.add(selectUnitPane, BorderLayout.CENTER);

		// Create select text field
		selectTextField = new WebTextField();
		selectTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent event) {}
			public void insertUpdate(DocumentEvent event) {
				searchTextChange();
			}
			public void removeUpdate(DocumentEvent event) {
				searchTextChange();
			}
		});
		selectUnitPane.add(selectTextField, BorderLayout.NORTH);

		// Create unit list
		unitListModel = new UnitListModel(UnitCategory.PEOPLE);
		unitList = new JList<Unit>(unitListModel);
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
		selectUnitPane.add(new WebScrollPane(unitList), BorderLayout.CENTER);

		// Create bottom panel
		WebPanel bottomPane = new WebPanel(new BorderLayout());
		mainPane.add(bottomPane, BorderLayout.SOUTH);

		// Create select options panel
		WebPanel selectOptionsPane = new WebPanel(new GridLayout(2, 1));
		bottomPane.add(selectOptionsPane, BorderLayout.NORTH);

		// Create open window option check box
		openWindowCheck = new WebCheckBox(Msg.getString("SearchWindow.openDetailWindow")); //$NON-NLS-1$
		openWindowCheck.setSelected(true);
		selectOptionsPane.add(openWindowCheck);

		// Create center map option
		centerMapCheck = new WebCheckBox(Msg.getString("SearchWindow.recenterMap")); //$NON-NLS-1$
		selectOptionsPane.add(centerMapCheck);

		// Create status label
		statusLabel = new WebLabel(" ", WebLabel.CENTER); //$NON-NLS-1$
		statusLabel.setBorder(new EtchedBorder());
		bottomPane.add(statusLabel, BorderLayout.CENTER);

		// Create search button panel
		WebPanel searchButtonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		bottomPane.add(searchButtonPane, BorderLayout.SOUTH);

		// Create search button
		WebButton searchButton = new WebButton(Msg.getString("SearchWindow.button.search")); //$NON-NLS-1$
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
//		UnitManager unitManager = Simulation.instance().getUnitManager();
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
		if (!foundUnit) statusLabel.setText(Msg.getString("SearchWindow.unitNotFound",tempName)); //$NON-NLS-1$

		// If there is no text entered, display "Enter The Name of a 'Category'" in statusLabel.
		if (selectTextField.getText().length() == 0)
			statusLabel.setText(Msg.getString("SearchWindow.defaultSearch",tempName)); //$NON-NLS-1$
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
		statusLabel.setText(" "); //$NON-NLS-1$
	}

	@Override
	public void destroy() {} {

		if (unitListModel != null) {
//			UnitManager manager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(unitListModel);
			unitListModel.clear();
			unitListModel = null;
		}
	}

	/**
	 * Inner class list model for categorized units.
	 */
	private class UnitListModel
	extends DefaultListModel<Unit>
	implements UnitManagerListener {

		/**
		 *
		 */
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

			// Add model as unit manager listener.
			unitManager.addUnitManagerListener(this);
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
//			UnitManager unitManager = unitManager;
			if (category.equals(UnitCategory.PEOPLE)) {

				if (unitManager.getTotalNumPeople() == 0) {
					Thread.yield();
					try {
						Thread.sleep(2L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					Collection<Person> people = unitManager.getPeople();
					units = CollectionUtils.sortByName(people);
				}
			}
			else if (category.equals(UnitCategory.SETTLEMENTS)) {
				Collection<Settlement> settlement = unitManager.getSettlements();
				units = CollectionUtils.sortByName(settlement);
			}
			else if (category.equals(UnitCategory.VEHICLES)) {
				Collection<Vehicle> vehicle = unitManager.getVehicles();
				units = CollectionUtils.sortByName(vehicle);
			}

			if (units != null && !units.isEmpty()) {
				Iterator<? extends Unit> unitI = units.iterator();
	
				while (unitI.hasNext()) {
					addElement(unitI.next());
				}
			}
		}

		@Override
		public void unitManagerUpdate(UnitManagerEvent event) {

			Unit selectedUnit = (Unit) unitList.getSelectedValue();
			lockUnitList = true;

			updateList();

			if (selectedUnit != null) {
				int index = indexOf(selectedUnit);
				if (index >= 0) {
					unitList.setSelectedIndex(index);
					unitList.ensureIndexIsVisible(index);
				}
			}

			lockUnitList = false;
		}
	}
}