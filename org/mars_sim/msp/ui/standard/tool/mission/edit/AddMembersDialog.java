package org.mars_sim.msp.ui.standard.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

class AddMembersDialog extends JDialog {
	
	Mission mission;
	DefaultListModel memberListModel;
	DefaultListModel availableListModel;
	JList availableList;
	JButton addButton;
	
	public AddMembersDialog(Dialog owner, Mission mission, DefaultListModel memberListModel, PersonCollection availablePeople) {
		// Use JDialog constructor
		super(owner, "Add Members", true);
		
		this.mission = mission;
		this.memberListModel = memberListModel;
		
		setLayout(new BorderLayout(5, 5));
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
		
		JLabel headerLabel = new JLabel("Select available people to add to the mission.");
		add(headerLabel, BorderLayout.NORTH);
		
		JPanel availablePeoplePane = new JPanel(new BorderLayout(0, 0));
		add(availablePeoplePane, BorderLayout.CENTER);
		
        // Create scroll panel for available list.
        JScrollPane availableScrollPane = new JScrollPane();
        availableScrollPane.setPreferredSize(new Dimension(100, 100));
        availablePeoplePane.add(availableScrollPane, BorderLayout.CENTER);
        
        // Create available list model
        availableListModel = new DefaultListModel();
        PersonIterator i = availablePeople.iterator();
        while (i.hasNext()) availableListModel.addElement(i.next());
        
        // Create member list
        availableList = new JList(availableListModel);
        availableList.addListSelectionListener(
        		new ListSelectionListener() {
        			public void valueChanged(ListSelectionEvent e) {
        				addButton.setEnabled(availableList.getSelectedValues().length > 0);
        			}
        		}
        	);
        availableScrollPane.setViewportView(availableList);
		
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		add(buttonPane, BorderLayout.SOUTH);
		
		addButton = new JButton("Add");
		addButton.setEnabled(availableList.getSelectedValues().length > 0);
		addButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				addPeople();
        				dispose();
        			}
				});
		buttonPane.add(addButton);
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				dispose();
        			}
				});
		buttonPane.add(cancelButton);
		
		// Finish and display dialog.
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
		setVisible(true);
	}
	
	private void addPeople() {
		int[] selectedIndexes = availableList.getSelectedIndices();
		for (int x = 0; x < selectedIndexes.length; x++) {
			if (memberListModel.getSize() < mission.getMissionCapacity())
				memberListModel.addElement(availableListModel.elementAt(selectedIndexes[x]));
		}
	}
}