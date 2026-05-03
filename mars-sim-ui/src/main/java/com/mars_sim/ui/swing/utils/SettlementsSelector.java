/*
 * Mars Simulation Project
 * SettlementsSelector.java
 * @date 2026-04-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityManagerListener;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.structure.Settlement;

/**
 * This is a component that allows the user to select a settlement, authority, or all settlements.
 * It displays the selected item and provides a way to get the corresponding set of settlements for the current selection.
 * It also listens for new settlements being added and updates the selection options accordingly.
 */
public class SettlementsSelector extends JPanel {
    private static final String ALL = "All";

    private UnitManager unitMgr;
    private EntityManagerListener umListener;

    private Map<Authority, Set<Settlement>> authorities;
    private JComboBox<Object> selectionCombo;
    private JLabel selectionDescription;
    private Set<Settlement> selection;

    private ActionListener selectionListener;
    private String actionCommand;

    /**
     * Constructor.
     * @param unitMgr Unit manager to get settlements and listen for changes.
     * @param selected The name of the initially selected settlement or authority, or null for no selection.
     * @param showDescription Whether to show the description label.
     */
    public SettlementsSelector(UnitManager unitMgr, String selected, boolean showDescription) {
        super();

        this.unitMgr = unitMgr;

        var choices = setupSelectionChoices();
        
        buildUI(choices, selected, showDescription);

        // 	// Listen for new Settlements
		umListener = new EntityManagerListener() {
			@Override
			public void entityAdded(Entity newEntity) {
				if (newEntity instanceof Settlement s) {
					addNewSettlement(s);
				}
			}

			@Override
			public void entityRemoved(Entity removedEntity) {
				// Settlements are never removed
			}
		};
		unitMgr.addEntityManagerListener(UnitType.SETTLEMENT, umListener);

        // Setup initial selection
        changeSelection(selectionCombo.getSelectedItem());
    }

    private void buildUI(List<Entity> choices, String selectedName, boolean showDescription) {
		SortedComboBoxModel<Object> model = new SortedComboBoxModel<>(choices, new SelectionComparator());
		model.addElement(ALL);

		Object selectedItem = ALL;
        if ((selectedName != null) && !selectedName.equals(ALL)) {
			// Search for a matching name
			var found = choices.stream()
								.filter(e -> e.getName().equals(selectedName))
								.findFirst()
								.orElse(null);
			if (found != null) {                   
				selectedItem = found;
			}
		}

		model.setSelectedItem(selectedItem);
		selectionCombo = new JComboBox<>(model);
		selectionCombo.setOpaque(false);
	
		// Add renderer
		selectionCombo.setRenderer(new SelectionComboRenderer());
		
		// Set the item listener only after the setup is done
		selectionCombo.addItemListener(e -> changeSelection(e.getItem()));

        add(selectionCombo);

		if (showDescription) {
        	selectionDescription = new JLabel("");
			add(selectionDescription);
		}
    }

    /**
	 * Reacts to a change in the Combo selection. 
	 * 
	 * @param selectedObject The newly selected object.
	 */
	private void changeSelection(Object selectedObject) {
        String description = "";

		Set<Settlement> newSelection = null;
        switch (selectedObject) {
			case Settlement s -> newSelection = Set.of(s);
			case Authority a -> {
                newSelection = authorities.get(a);
				description = newSelection.stream()
										.map(Settlement::getName)
										.sorted()
										.collect(Collectors.joining(", ", "(", ")"));
	        }
			case String str when str.equals(ALL) ->
                    newSelection = new HashSet<>(getAllSettlements());
			default -> Collections.emptySet();   // Should never happen
		}

        if (newSelection != null) {
            // Update the selection 
            selection = newSelection;
			if (selectionDescription != null) {
            	selectionDescription.setText(description);
			}

            if (selectionListener != null) {
                selectionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand));
            }
        }
    }

    /**
     * A new Settlement has been added.
     * @param s New settlement.
     */
    private void addNewSettlement(Settlement s) {
        SortedComboBoxModel<Object> ms = (SortedComboBoxModel<Object>) selectionCombo.getModel();
        ms.addElement(s);

        var ra = s.getReportingAuthority();
        authorities.computeIfAbsent(ra, k -> {
            ms.addElement(ra);
            return new HashSet<>();
        });
        authorities.get(ra).add(s);

        // If the parent authority is selected, then simulate a change selection to update the selection set
        if (selectionCombo.getSelectedItem() instanceof Authority a && a.equals(ra)) {
            changeSelection(a);
        }
    }

    /**
     * Get the actual selected item.
     * @return The selected item, which can be a Settlement, Authority, or String (for "All").
     */
    public Object getSelectedItem() {
        return selectionCombo.getSelectedItem();
    }

    /**
     * Get the set of settlements corresponding to the current selection. This will be a single settlement if a settlement is selected, all settlements under an authority if an authority is selected, or all settlements if "All" is selected.
     * @return The set of settlements corresponding to the current selection.
     */
    public Set<Settlement> getSelectedSettlements() {
        return selection;
    }

	/**
	 * Add a listener that gets notified when the selection changes.
	 * @param actionCommand The action command to be used in the ActionEvent.
	 * @param listener The listener to be notified.
	 */
    public void setSelectionListener(String actionCommand, ActionListener listener) {
        this.selectionListener = listener;
        this.actionCommand = actionCommand;
    }

	/**
	 * Get all valid settlements based on the current game mode.
	 * @return
	 */
	private Collection<Settlement> getAllSettlements() {
		return (GameManager.getGameMode() == GameMode.COMMAND) ?
			unitMgr.getCommanderSettlements() : unitMgr.getSettlements();
	}

    /**
	 * Sets up a list of settlements and associated authorities.
	 *
	 * @return Map of authority to settlements
	 */
	private List<Entity> setupSelectionChoices() {

		Collection<Settlement> settlements = getAllSettlements();

		List<Entity> choices = new ArrayList<>(settlements);
		
		// Create the Authority maps
		authorities = new HashMap<>();
		for (var s : settlements) {
			var ra = s.getReportingAuthority();
			authorities.computeIfAbsent(ra, k -> new HashSet<>()).add(s);
		}	

		choices.addAll(authorities.keySet());

		return choices;
	}

    /**
	 * This is a comparator that sorts in the following order:
	 * 1) String
	 * 2) Authority
	 * 3) Settlement
	 */
	private static class SelectionComparator implements Comparator<Object> {

		@Override
		public int compare(Object o1, Object o2) {
			// String are always first
			if (o1 instanceof String) {
				return -1;
			}
			else if (o2 instanceof String) {
				return 1;
			}
			if ((o1 instanceof Settlement) && (o2 instanceof Authority)) {
				return 1;
			}
			else if ((o1 instanceof Authority) && (o2 instanceof Settlement)) {
				return -1;
			}
			else if ((o1 instanceof Entity e1) && (o2 instanceof Entity e2)) {
				return e1.getName().compareTo(e2.getName());
			}

			// Should never get here
			return 0;
		}
	}

    /**
     * Renders items in the selection combo box, showing settlement names and authority names with settlement counts.
     */
    private class SelectionComboRenderer extends JLabel implements
        ListCellRenderer<Object> {

		public SelectionComboRenderer() {

			setOpaque(true);
			setVerticalAlignment(CENTER);

		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends Object> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {

			// Center horizontally
			setHorizontalAlignment(CENTER); 
			
			this.setFont(list.getFont());

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			
			if (value instanceof Settlement s) {
				this.setText(s.getName());
			}
			else if (value instanceof Authority a) {
				var children = authorities.get(a);
				this.setText(a.getName() + " (" + (children != null ? children.size() : 0) + ")");
			}
			else if (value instanceof String s) {
				this.setText(s);
            }
			
			return this;
		}
	}

    /**
     * Unregister this component from any listeners
     */
    public void unregister() {
        unitMgr.removeEntityManagerListener(UnitType.SETTLEMENT, umListener);
    }
}
