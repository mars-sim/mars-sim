/**
 * Mars Simulation Project
 * MonitorFilter.java
 * @date 13-12-2025
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;


/**
 * The MonitorFilter provides a dialog that allows the user to change active filters
 * on a FilteredTableModel instance.
 */
@SuppressWarnings("serial")
class MonitorFilter extends JDialog
implements ActionListener {

	// Data members
	private FilteredTableModel model;

	/**
	 * Constructor.
	 * 
	 * @param model the event table model
	 * @param parent the parent frame
	 */
	public MonitorFilter(FilteredTableModel model, JFrame parent) {

		// Use JDialog constructor.
		super(parent, Msg.getString("MonitorFilter.title"), true); //$NON-NLS-1$

		// Initialize data members.
		this.model = model;

		// Prepare content pane
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(StyleManager.newEmptyBorder());
		setContentPane(mainPane);

		var filters = model.getActiveFilters();

		// Create category pane
		JPanel categoryPane = new JPanel(new GridLayout(filters.size(), 1));
		categoryPane.setBorder(new MarsPanelBorder());
		mainPane.add(categoryPane, BorderLayout.CENTER);

		// Create filter checboxes
		for(var f : filters) {
			addFilterCheckBox(categoryPane, f);
		}

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
		pack();	    
	}

	/**
	 * Creates a checkbox to control the event category filtering
	 */
	private void addFilterCheckBox(JPanel categoryPane, FilteredTableModel.Filter filter) {
		JCheckBox newCheck = new JCheckBox(filter.name());
		newCheck.setSelected(filter.isActive());
		newCheck.addActionListener(this);
		newCheck.setActionCommand(filter.id());
		categoryPane.add(newCheck);	
	}

	/**	
	 * Reacts to action event.
	 * 
	 * @see java.awt.event.ActionListener
	 * @param event the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

		JCheckBox check = (JCheckBox) event.getSource();

		String command = check.getActionCommand();
		model.setFilter(command, check.isSelected());
	}
}
