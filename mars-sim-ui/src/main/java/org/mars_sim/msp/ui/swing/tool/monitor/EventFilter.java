/**
 * Mars Simulation Project
 * EventFilter.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;


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
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
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
	 * Create a checkbox to control teh Event category filtering
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
	 * React to action event.
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
