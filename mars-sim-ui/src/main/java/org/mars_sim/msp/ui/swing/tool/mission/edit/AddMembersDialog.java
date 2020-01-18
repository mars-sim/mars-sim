/**
 * Mars Simulation Project
 * AddMembersDialog.java
 * @version 3.1.0 2019-02-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * A dialog window for adding members to the mission for the mission tool.
 */
class AddMembersDialog extends ModalInternalFrame {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members.
	private Mission mission;
	protected MainDesktopPane desktop;
	
	private DefaultListModel<MissionMember> memberListModel;
	private DefaultListModel<MissionMember> availableListModel;
	private JList<MissionMember> availableList;
	private WebButton addButton;
	
	/**
	 * Constructor.
	 * @param owner {@link Dialog} the owner dialog.
	 * @param mission {@link Mission} the mission to add to.
	 * @param memberListModel {@link DefaultListModel}<{@link MissionMember}> the member list model in the edit mission dialog.
	 * @param availableMembers {@link Collection}<{@link MissionMember}> the available members to add.
	 */
	public AddMembersDialog(JInternalFrame owner, MainDesktopPane desktop, Mission mission, 
	        DefaultListModel<MissionMember> memberListModel, Collection<MissionMember> availableMembers) {
		// Use JDialog constructor
		//super(owner, "Add Members", true);
		// Use JInternalFrame constructor
        super("Add Members");
       		
		// Initialize data members.
		this.mission = mission;
		this.memberListModel = memberListModel;
		this.desktop = desktop;
		
		// Set the layout.
		setLayout(new BorderLayout(5, 5));
		
		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
		
		// Create the header label.
		WebLabel headerLabel = new WebLabel("Select available people to add to the mission.");
		add(headerLabel, BorderLayout.NORTH);
		
		// Create the available people panel.
		WebPanel availablePeoplePane = new WebPanel(new BorderLayout(0, 0));
		add(availablePeoplePane, BorderLayout.CENTER);
		
        // Create scroll panel for available list.
		WebScrollPane availableScrollPane = new WebScrollPane();
        availableScrollPane.setPreferredSize(new Dimension(100, 100));
        availablePeoplePane.add(availableScrollPane, BorderLayout.CENTER);
        
        // Create available list model
        availableListModel = new DefaultListModel<MissionMember>();
        Iterator<MissionMember> i = availableMembers.iterator();
        while (i.hasNext()) availableListModel.addElement(i.next());
        
        // Create member list
        availableList = new JList<MissionMember>(availableListModel);
        availableList.addListSelectionListener(
        		new ListSelectionListener() {
        			public void valueChanged(ListSelectionEvent e) {
        				// Enable the add button if there are available members.
        				addButton.setEnabled(availableList.getSelectedValuesList().size() > 0);
        			}
        		}
        	);
        availableScrollPane.setViewportView(availableList);
		
        // Create button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.RIGHT));
		add(buttonPane, BorderLayout.SOUTH);
		
		// Create add button.
		addButton = new WebButton("Add");
		addButton.setEnabled(availableList.getSelectedValuesList().size() > 0);
		addButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Add members to the edit mission dialog and dispose this dialog.
        				addMembers();
        				dispose();
        			}
				});
		buttonPane.add(addButton);
		
		// Create cancel button.
		WebButton cancelButton = new WebButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Dispose the dialog.
        				dispose();
        			}
				});
		buttonPane.add(cancelButton);
		 
		// Finish and display dialog.
		//pack();
		//setLocationRelativeTo(owner);
		//setResizable(false);
		//setVisible(true);
		
	    desktop.add(this);	    
	    
        setSize(new Dimension(700, 550));
		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    setVisible(true);
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