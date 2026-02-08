/**
 * Mars Simulation Project
 * TypePanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardPane;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;


/**
 * A wizard panel for selecting mission type.
 */
@SuppressWarnings("serial")
class TypePanel extends WizardStep<MissionDataBean> implements ItemListener {
	
	public static final String ID = "Type";

    // Private members.
	private JComboBox<MissionType> typeSelect;

	private JTextField descriptionTF;
	
	/**
	 * Constructor.
	 * @param wizard {@link CreateMissionWizard} the create mission wizard.
	 */
	TypePanel(WizardPane<MissionDataBean> parent) {
		super(ID, parent);
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Create the type panel.
		JPanel typePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		typePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typePane);
		
		// Create the type label.
		JLabel typeLabel= new JLabel("Type: ");
		typePane.add(typeLabel);
		
		// Create the mission types.
		typeSelect = new JComboBox<>();
		typeSelect.setRenderer(new NamedListCellRenderer());
		for(var v : MissionCreate.availableTypes()) {
			typeSelect.addItem(v);
		}
		typeSelect.setSelectedIndex(-1);
		typeSelect.addItemListener(this);

        typeSelect.setMaximumRowCount(typeSelect.getItemCount());
		typePane.add(typeSelect);
		typePane.setMaximumSize(new Dimension(Short.MAX_VALUE, typeSelect.getPreferredSize().height));
		
		// Add a vertical strut to separate the display.
		add(Box.createVerticalStrut(10));
		
		// Create the description info label.
		add(new JLabel("Edit Mission Description (Optional)"));
		
		// Create the description panel.
		JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		// Create the description label.
		descriptionPane.add(new JLabel("Description: "));
		
		// Create the description text field.
		descriptionTF = new JTextField(20);
		descriptionPane.add(descriptionTF);
		descriptionPane.setMaximumSize(new Dimension(Short.MAX_VALUE, descriptionTF.getPreferredSize().height));

		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}
	
	/**
	 * Invoked when an item has been selected or deselected by the user.
	 * @param e the item event.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		var selectedMission = (MissionType) typeSelect.getSelectedItem();
		setMandatoryDone(selectedMission != null);
	}

	@Override
	public void updateState(MissionDataBean state) {
		var type = (MissionType)typeSelect.getSelectedItem();
		state.setMissionType(type);
		state.setDescription(descriptionTF.getText());

		getWizard().setStepSequence(MissionCreate.getSteps(type));
	}
}
