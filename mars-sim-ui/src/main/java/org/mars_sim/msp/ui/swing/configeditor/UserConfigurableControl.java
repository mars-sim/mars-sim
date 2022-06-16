/*
 * Mars Simulation Project
 * UserConfigurableControl.java
 * @date 2022-06-15
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.configuration.UserConfigurable;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;

import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;

/**
 * This is a UI Control panel that manages the UserConfigurable items of an existing
 * repository. It provides common CRUD functionality for the items.
 *
 * @param <T> The UserConfigurable item that is managed.
 */
public abstract class UserConfigurableControl<T extends UserConfigurable> implements ActionListener {

	private static final String LOAD = "load";
	private static final String SAVE_NEW = "new";
	private static final String SAVE = "save";
	private static final String UNDO = "undo";
	private static final String DELETE = "delete";
	
	private JButton saveButton;
	private JButton delButton;
	private JButton saveAsButton;

	private DefaultComboBoxModel<String> itemCB;

	private WebTextField descriptionTF;
	private UserConfigurableConfig<T> config;
	private JPanel buttonPane;
	private T selected;
	private String itemType;
	private Component parent;


	public UserConfigurableControl(Component parent, String itemType, UserConfigurableConfig<T> config) {
		this.config = config;
		this.itemType = itemType;
		this.parent = parent;

		this.buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.buttonPane.setBorder(BorderFactory.createTitledBorder(itemType));
		
		// Item name selection
		buttonPane.add(new WebLabel(Msg.getString("UserConfigurableControl.label.load") + " :"));
		itemCB = new DefaultComboBoxModel<>();
		itemCB.addAll(0, config.getItemNames());
		JComboBox<String> crewSelector = new JComboBox<>(itemCB) ;
		crewSelector.setToolTipText(Msg.getString("UserConfigurableControl.tooltip.load", itemType));
		crewSelector.addActionListener(this);
		crewSelector.setActionCommand(LOAD);
		buttonPane.add(crewSelector);
		
		// Description field
		buttonPane.add(new WebLabel(Msg.getString("UserConfigurableControl.label.description") + " : "));
		descriptionTF = new WebTextField(15);
		descriptionTF.setToolTipText(Msg.getString("UserConfigurableControl.tooltip.description", itemType));
		buttonPane.add(descriptionTF);
		
		// Create save button.
		saveButton = new JButton(Msg.getString("UserConfigurableControl.button.save"));
		saveButton.addActionListener(this);
		saveButton.setActionCommand(SAVE);
		saveButton.setToolTipText(Msg.getString("UserConfigurableControl.tooltip.save", itemType));
		buttonPane.add(saveButton);		

		// Create save as new button.
		saveAsButton = new JButton(Msg.getString("UserConfigurableControl.button.saveas"));
		saveAsButton.setActionCommand(SAVE_NEW);
		saveAsButton.setToolTipText(Msg.getString("UserConfigurableControl.tooltip.saveas", itemType));
		saveAsButton.addActionListener(this);
		buttonPane.add(saveAsButton);

		// Create delete button.
		delButton = new JButton(Msg.getString("UserConfigurableControl.button.delete"));
		delButton.setActionCommand(DELETE);
		delButton.setToolTipText(Msg.getString("UserConfigurableControl.tooltip.delete", itemType));
		delButton.addActionListener(this);
		buttonPane.add(delButton);
		
		// Create save new crew button.
		JButton undoButton = new JButton(Msg.getString("UserConfigurableControl.button.undo"));
		undoButton.setActionCommand(UNDO);
		undoButton.setToolTipText(Msg.getString("UserConfigurableControl.tooltip.undo"));
		undoButton.addActionListener(this);
		buttonPane.add(undoButton);
		
		String initialChoice = config.getItemNames().get(0);
		selectItem(initialChoice);
	}

	/**
	 * Change the selected to a new item.
	 * @param newSelection
	 */
	private void selectItem(String newName) {
		T newSelection = config.getItem(newName);
		if ((selected == null) || !newSelection.getName().equals(selected.getName())) {
			selected = newSelection;
		
			displayItem(selected);
			
			saveButton.setEnabled(!selected.isBundled());
			delButton.setEnabled(!selected.isBundled());
			itemCB.setSelectedItem(selected.getName());
			descriptionTF.setText(selected.getDescription());	
		}
	}

	/**
	 * What was the last selected item
	 * @return
	 */
	public T getSeletedItem() {
		return selected;
	}

	/**
	 * Displayed this new item which is now selected
	 * @param newDisplay
	 */
	protected abstract void displayItem(T newDisplay);

	/**
	 * Create a new item on the values from teh associaetd editor panel.
	 * @param newName
	 * @return
	 */
	protected abstract T createItem(String newName, String newDescription);

	/**
	 * Takes the actions from the button being clicked on
	 */
	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		switch (cmd) {
		case LOAD: 
			String loadItem = (String) itemCB.getSelectedItem();
			if (!selected.getName().equalsIgnoreCase(loadItem)) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				int result = JOptionPane.showConfirmDialog(parent, 
								"Are you sure you want to load the " + itemType + " '" + loadItem + "' ? " + System.lineSeparator()
								+ "All the changes made will be lost.",
								"Confirm Loading " + itemType,
								JOptionPane.YES_NO_OPTION);
				
				if (result == JOptionPane.YES_OPTION) {
					selectItem(loadItem);
				}
			}
			break;
			
		case UNDO: 
			String currentItem = (String) itemCB.getSelectedItem();
			JDialog.setDefaultLookAndFeelDecorated(true);
			int undoConfirm = JOptionPane.showConfirmDialog(parent, 
							"Are you sure you want to reload the " + itemType + " '" + currentItem + "' ? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Reloading " + itemType,
							JOptionPane.YES_NO_OPTION);
			
			if (undoConfirm == JOptionPane.YES_OPTION) {
				selected = null; // Clear selection to force reload
				selectItem(currentItem);
			}
			break;
			
		case DELETE:
			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(parent, 
							"Are you sure you want to delete the " + itemType + " '" + selected.getName() + "' ? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Delete " + itemType,
							JOptionPane.YES_NO_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				String oldItem = selected.getName();
				config.deleteItem(oldItem);
				
				String nextItem = config.getItemNames().get(0);
				selectItem(nextItem);
				itemCB.removeElement(oldItem);
			}
			break;
			
		case SAVE:
			JDialog.setDefaultLookAndFeelDecorated(true);
			int result2 = JOptionPane.showConfirmDialog(parent,
					"Are you sure you want to save the changes for " + selected.getName() + " ? " 
					+ System.lineSeparator() + System.lineSeparator()
					+ "Note : If you only want the changes to apply to " + System.lineSeparator()
					+ "the simulation you are setting up, choose " + System.lineSeparator()
					+ "'Commit Change' instead." + System.lineSeparator(),
					"Confirm Saving " + itemType,
					JOptionPane.YES_NO_OPTION);
			
			if (result2 == JOptionPane.YES_OPTION) {
				T newSelected = createItem(selected.getName(),
										   descriptionTF.getText());

				if (newSelected != null) {
					selected = newSelected;
					config.saveItem(selected);
				}
			}
			break;

		case SAVE_NEW:
			JDialog.setDefaultLookAndFeelDecorated(true);
			String newName = (String)JOptionPane.showInputDialog(
                   parent, "Enter the name of the new " + itemType);

			// Create new Crew
			if ((newName != null) && (newName.length() > 0)) {
				T newSelected = createItem(newName, descriptionTF.getText());
				if (newSelected != null) {
					config.saveItem(newSelected); 
					itemCB.addElement(newName);
					setSelectedItem(newName);
				}
			}
			break;
				
		default:
			break;
		}
	}

	/**
	 * Get the select item name.
	 * @return
	 */
	public String getSelectItemName() {
		return (String) itemCB.getSelectedItem();
	}

	/**
	 * Entered description
	 * @return
	 */
	public String getDescription() {
		return descriptionTF.getText();
	}
	
	public JPanel getPane() {
		return buttonPane;
	}

	/**
	 * Set the select item that is displayed
	 * @param newSelection
	 */
	public void setSelectedItem(String newSelection) {
		// Refresh display with item
		selectItem(newSelection);
		
		// Set the CB after the internal item to stop the dialog popping up
		itemCB.setSelectedItem(newSelection);
	}

	/**
	 * Control whether the current config can be saved
	 * @param b
	 */
	public void allowSaving(boolean saveAllowed) {
		saveButton.setEnabled(!selected.isBundled() && saveAllowed);
		saveAsButton.setEnabled(saveAllowed);
	}
	
	/**
	 * Reload any items from the config repository
	 */
	public void reload() {
		List<String> items = new ArrayList<>(config.getItemNames());
		for (int i = 0; i < itemCB.getSize(); i++) {
			items.remove(itemCB.getElementAt(i));
		}
		
		// What is left is new
		if (!items.isEmpty()) {
			itemCB.addAll(items);
		}
	}
}
