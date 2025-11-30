/**
 * Mars Simulation Project
 * EventFilter.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
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

import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;


/**
 * The EventFilter class is a dialog window for filtering 
 * historical events by category in the EventTab.
 */
@SuppressWarnings("serial")
public class EventFilter extends JDialog
implements ActionListener {

	// Data members
	private EventTableModel model;

	/**
	 * Constructor.
	 * 
	 * @param model the event table model
	 * @param parent the parent frame
	 */
	public EventFilter(EventTableModel model, JFrame parent) {

		// Use JDialog constructor.
		super(parent, Msg.getString("EventFilter.title"), true); //$NON-NLS-1$

		// Initialize data members.
		this.model = model;

		// Prepare content pane
		JPanel mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(StyleManager.newEmptyBorder());
		setContentPane(mainPane);

		// Create category pane
		JPanel categoryPane = new JPanel(new GridLayout(5, 1));
		categoryPane.setBorder(new MarsPanelBorder());
		mainPane.add(categoryPane, BorderLayout.CENTER);

		// Create filter checboxes
		for(HistoricalEventCategory cat : HistoricalEventCategory.values()) {
			addCategoryCheckbox(categoryPane, model, cat);
		}

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
		pack();	    
	}

	/**
	 * Creates a checkbox to control the event category filtering
	 */
	private void addCategoryCheckbox(JPanel categoryPane, EventTableModel model2,
									 HistoricalEventCategory category) {
		JCheckBox newCheck = new JCheckBox(category.getName());
		newCheck.setSelected(model2.isDisplayed(category));
		newCheck.addActionListener(this);
		newCheck.setActionCommand(category.name());
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
		HistoricalEventCategory category = HistoricalEventCategory.valueOf(command);
		model.setDisplayed(category, check.isSelected());
	}
}
