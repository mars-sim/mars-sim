/*
 * Mars Simulation Project
 * SearchWindow.java
 * @date 2023-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.formdev.flatlaf.FlatClientProperties;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * The SearchWindow is a tool window that allows the user to search
 * for individual units by name and category.
 */
@SuppressWarnings("serial")
public class SearchWindow
extends ToolWindow {

	/** Tool name. */
	public static final String NAME = "search";
	public static final String ICON = "action/find";
    public static final String TITLE = Msg.getString("SearchWindow.title");

	
	/** Category selector. */
	private JComboBox<UnitType> searchForType;
	/** List of selectable units. */
	private JList<Unit> unitList;
	/** Model for unit select list. */
	private UnitListModel unitListModel;
	/** Selection text field. */
	private JTextField searchForName;

	private UnitManager unitManager;
	private EntityLabel selectedUnit;

	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the desktop pane
	 */
	public SearchWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, TITLE, desktop);
		unitManager = desktop.getSimulation().getUnitManager();
	
		
		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		setContentPane(mainPane);

		// Create search for panel
		JPanel searchForPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		searchForPane.setPreferredSize(new Dimension(240, 26));
		mainPane.add(searchForPane, BorderLayout.NORTH);

		// Create search for label
		JLabel searchForLabel = new JLabel(Msg.getString("SearchWindow.searchFor")); //-NLS-1$
		searchForPane.add(searchForLabel);

		// Create search for select
		UnitType[] categories = {
			UnitType.PERSON,
			UnitType.SETTLEMENT,
			UnitType.VEHICLE,
			UnitType.ROBOT,
			UnitType.EVA_SUIT
		};
		searchForType = new JComboBox<>(categories);
		searchForType.setRenderer(new UnitTypeRenderer());
		searchForType.setSelectedIndex(0);
		searchForType.addItemListener(event -> changeCategory((UnitType) searchForType.getSelectedItem()));
		searchForPane.add(searchForType);

		// Create select unit panel
		JPanel selectUnitPane = new JPanel(new BorderLayout());
		mainPane.add(selectUnitPane, BorderLayout.CENTER);
		
		// Create select text field
		searchForName = new JTextField();
		searchForName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent event) {
				// Not needed
			}
			public void insertUpdate(DocumentEvent event) {
				searchTextChange();
			}
			public void removeUpdate(DocumentEvent event) {
				searchTextChange();
			}
		});
		selectUnitPane.add(searchForName, BorderLayout.NORTH);
		
		// Add leading/trailing icons to text fields
		searchForName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search");
		searchForName.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		
		// Create unit list
		unitListModel = new UnitListModel(UnitType.PERSON);
		unitList = new JList<>(unitListModel);
		unitList.setSelectedIndex(0);
		unitList.addListSelectionListener(e -> selectedUnit.setEntity(unitList.getSelectedValue()));
		selectUnitPane.add(new JScrollPane(unitList), BorderLayout.CENTER);

		// Create bottom panel
		var bottomPanel = new JPanel();
		JLabel title = new JLabel("Selection", SwingConstants.RIGHT);
        title.setFont(StyleManager.getLabelFont());
		bottomPanel.add(title);
		selectedUnit = new EntityLabel(desktop);
		bottomPanel.add(selectedUnit);
		mainPane.add(bottomPanel, BorderLayout.SOUTH);

		// Pack window
		pack();
	}

	/**
	 * Changes the category of the unit list.
	 * 
	 * @param category
	 */
	private void changeCategory(UnitType category) {
		// Change unitList to the appropriate category list
		unitListModel.updateCategory(category);
	}

	/**
	 * Makes selection in list depending on what unit names.
	 * Begins with the changed text.
	 */
	private void searchTextChange() {
		unitListModel.setNameFilter(searchForName.getText());
	}

	@Override
	public void destroy() {
		super.destroy();
		
		if (unitListModel != null) {
			unitListModel.clear();
			unitListModel = null;
		}
	
	}
	private static class UnitTypeRenderer extends BasicComboBoxRenderer {
        @SuppressWarnings("rawtypes")
		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof UnitType ut) {
                setText(ut.getName());
            }
            return this;
        }
    }

	/**
	 * Inner class list model for categorized units.
	 */
	private class UnitListModel
	extends DefaultListModel<Unit> {

		private static final long serialVersionUID = 1L;
		// Data members.
		private UnitType category;
		private String nameFilter = "";

		/**
		 * Constructor
		 * @param initialCategory the initial category to display.
		 */
		public UnitListModel(UnitType initialCategory) {

			// Use DefaultListModel constructor.
			super();

			// Initialize data members.
			this.category = initialCategory;
			updateList();
		}

		public void setNameFilter(String text) {
			nameFilter = (text == null ? "" : text.toLowerCase());
			updateList();
		}

		/**
		 * Updates the category.
		 * @param category the list category
		 */
		private void updateCategory(UnitType category) {
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

			Collection<? extends Unit> units = switch(category) {
				case PERSON -> unitManager.getPeople();
				case SETTLEMENT -> unitManager.getSettlements();
				case VEHICLE -> unitManager.getVehicles();
				case ROBOT -> unitManager.getRobots();
				case EVA_SUIT -> unitManager.getEVASuits();
				default -> Collections.emptyList();
			};
			
			// Apply name filter
			var selected = units.stream()
							.filter(e -> e.getName().toLowerCase().contains(nameFilter))
							.sorted(Comparator.comparing(Unit::getName))
							.toList();
			
			selected.forEach(u -> addElement(u));
		}
	}
}


