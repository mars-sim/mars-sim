/**
 * Mars Simulation Project
 * UserConfigurableControl.java
 * @version 3.3.0 2021-08-20
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	private static final String DELETE = "delete";
	
	private JButton saveButton;
	private JButton delButton;
	
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
		
		// Crew name selection
		buttonPane.add(new WebLabel("Loaded :"));
		itemCB = new DefaultComboBoxModel<>();
		itemCB.addAll(0, config.getItemNames());
		JComboBox<String> crewSelector = new JComboBox<>(itemCB) ;
		crewSelector.addActionListener(this);
		crewSelector.setActionCommand(LOAD);
		buttonPane.add(crewSelector);
		
		// Description field
		buttonPane.add(new WebLabel("   Description : "));
		descriptionTF = new WebTextField(15);
		buttonPane.add(descriptionTF);
		
		// Create save crew button.
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(SAVE);
		buttonPane.add(saveButton);		

		// Create save new crew button.
		JButton newButton = new JButton("Save as New");
		newButton.setActionCommand(SAVE_NEW);
		newButton.addActionListener(this);
		buttonPane.add(newButton);

		// Create save new crew button.
		delButton = new JButton("Delete");
		delButton.setActionCommand(DELETE);
		delButton.addActionListener(this);
		buttonPane.add(delButton);

		String initialChoice = config.getItemNames().get(0);
		selectItem(initialChoice);
	}

	/**
	 * Change the selected to a new item.
	 * @param newSelection
	 */
	private void selectItem(String newName) {
		T newSelection = config.getItem(newName);
		if ((selected== null) || !newSelection.getName().equals(selected.getName())) {
			selected = newSelection;
		
			displayItem(selected);
			
			saveButton.setEnabled(!selected.isBundled());
			delButton.setEnabled(!selected.isBundled());
			itemCB.setSelectedItem(selected.getName());
			descriptionTF.setText(selected.getDescription());	
		}
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
	protected abstract T createItem(String newName);

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
								"Are you sure you want to reload the " + itemType + " '" + loadItem + "' ? " + System.lineSeparator()
								+ "All the changes made will be lost.",
								"Confirm Reloading " + itemType,
								JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (result == JOptionPane.YES_OPTION) {
					selectItem(loadItem);
				}
			}
			break;
			
		case DELETE:
			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(parent, 
							"Are you sure you want to delete the " + itemType + " '" + selected.getName() + "' ? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Delete " + itemType,
							JOptionPane.YES_NO_CANCEL_OPTION);
			
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
					JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result2 == JOptionPane.YES_OPTION) {
				T newSelected = createItem(selected.getName());

				if (newSelected != null) {
					newSelected.setDescription(descriptionTF.getText());

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
				T newSelected = createItem(newName);
				if (newSelected != null) {
					newSelected.setDescription(descriptionTF.getText());
					config.saveItem(newSelected); 

					itemCB.addElement(newName);
					selectItem(newName);
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
}
