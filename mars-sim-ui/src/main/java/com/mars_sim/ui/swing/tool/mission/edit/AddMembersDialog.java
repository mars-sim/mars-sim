/**
 * Mars Simulation Project
 * AddMembersDialog.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.Worker;

/**
 * A dialog window for adding members to the mission for the mission tool.
 */
class AddMembersDialog extends JDialog {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members.
	private Mission mission;
	
	private DefaultListModel<Worker> memberListModel;
	private DefaultListModel<Worker> availableListModel;
	private JList<Worker> availableList;
	private JButton addButton;
	
	/**
	 * Constructor.
	 * @param parent {@link Frame} the parent frame.
	 * @param mission {@link Mission} the mission to add to.
	 * @param memberListModel {@link DefaultListModel}<{@link Worker}> the member list model in the edit mission dialog.
	 * @param availableMembers {@link Collection}<{@link Worker}> the available members to add.
	 */
	public AddMembersDialog(Frame parent, Mission mission, 
	        DefaultListModel<Worker> memberListModel, Collection<Worker> availableMembers) {
		// Use JDialog constructor
        super(parent, "Add Members", true); // true for modal
       		
		// Initialize data members.
		this.mission = mission;
		this.memberListModel = memberListModel;
		
		// Set the layout.
		setLayout(new BorderLayout(5, 5));
		
		// Create the header label.
		JLabel headerLabel = new JLabel("Select available people to add to the mission.");
		add(headerLabel, BorderLayout.NORTH);
		
		// Create the available people panel.
		JPanel availablePeoplePane = new JPanel(new BorderLayout(0, 0));
		add(availablePeoplePane, BorderLayout.CENTER);
		
        // Create scroll panel for available list.
		JScrollPane availableScrollPane = new JScrollPane();
        availableScrollPane.setPreferredSize(new Dimension(100, 100));
        availablePeoplePane.add(availableScrollPane, BorderLayout.CENTER);
        
        // Create available list model
        availableListModel = new DefaultListModel<>();
        Iterator<Worker> i = availableMembers.iterator();
        while (i.hasNext()) availableListModel.addElement(i.next());
        
        // Create member list
        availableList = new JList<>(availableListModel);
        availableList.addListSelectionListener(
        		e -> addButton.setEnabled(!availableList.getSelectedValuesList().isEmpty())
        	);
        availableScrollPane.setViewportView(availableList);
		
        // Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(buttonPane, BorderLayout.SOUTH);
		
		// Create add button.
		addButton = new JButton("Add");
		addButton.setEnabled(!availableList.getSelectedValuesList().isEmpty());
		addButton.addActionListener(
				e -> {
					// Add members to the edit mission dialog and dispose this dialog.
					addMembers();
					dispose();
				});
		buttonPane.add(addButton);
		
		// Create cancel button.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				e -> dispose());
		buttonPane.add(cancelButton);
		   
	    
        var dim = new Dimension(700, 550);
		setSize(dim);
		setPreferredSize(dim);
		
		// Set dialog behavior
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
	}
	
	/**
	 * Add members to edit mission dialog.
	 */
	private void addMembers() {
		int[] selectedIndexes = availableList.getSelectedIndices();
        for (int selectedIndexe : selectedIndexes) {
            if (memberListModel.getSize() < mission.getMissionCapacity()) {
                memberListModel.addElement(availableListModel.elementAt(selectedIndexe));
            }
        }
	}
}
