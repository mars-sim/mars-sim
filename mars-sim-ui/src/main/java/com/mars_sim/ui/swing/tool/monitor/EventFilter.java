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
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;


/**
 * The EventFilter class is a internal dialog window for filtering 
 * historical events by category in the EventTab.
 */
@SuppressWarnings("serial")
public class EventFilter extends JInternalFrame
implements ActionListener {

	// Data members
	private EventTableModel model;

	/**
	 * Constructor.
	 * 
	 * @param model the event table model
	 * @param desktop the main desktop
	 */
	public EventFilter(EventTableModel model, MainDesktopPane desktop) {

		// Use JInternalFrame constructor.
		super(Msg.getString("EventFilter.title"), false, true); //$NON-NLS-1$

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

		pack();
		
        desktop.add(this);
	    
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
