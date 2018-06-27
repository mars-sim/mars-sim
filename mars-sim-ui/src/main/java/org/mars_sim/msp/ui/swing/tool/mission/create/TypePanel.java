/**
 * Mars Simulation Project
 * TypePanel.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

/**
 * A wizard panel for selecting mission type.
 */
public class TypePanel extends WizardPanel implements ItemListener {

	/** The wizard panel name. */
	private final static String NAME = "Mission Type";
	
	// Private members.
	private JComboBoxMW<?> typeSelect;
	private WebLabel descriptionInfoLabel;
	private WebLabel descriptionLabel;
	private WebTextField descriptionField;
	
	private static MissionManager missionManager;

	//private CreateMissionWizard wizard;
	/**
	 * Constructor.
	 * @param wizard {@link CreateMissionWizard} the create mission wizard.
	 */
	TypePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		this.wizard = wizard;
		
		missionManager = Simulation.instance().getMissionManager();
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the type info label.
		WebLabel typeInfoLabel = new WebLabel("Select Mission Type");
		typeInfoLabel.setFont(typeInfoLabel.getFont().deriveFont(Font.BOLD));
		typeInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typeInfoLabel);
		
		// Create the type panel.
		WebPanel typePane = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		typePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(typePane);
		
		// Create the type label.
		WebLabel typeLabel= new WebLabel("Type: ");
		typePane.add(typeLabel);
		
		// Create the mission types.
		String[] missionTypes = MissionDataBean.getMissionTypes();
		sortStringBubble(missionTypes);
		String[] displayMissionTypes = new String[missionTypes.length + 1];
		displayMissionTypes[0] = "";
        System.arraycopy(missionTypes, 0, displayMissionTypes, 1, missionTypes.length);
		typeSelect = new JComboBoxMW<Object>(displayMissionTypes);
		typeSelect.addItemListener(this);
        typeSelect.setMaximumRowCount(typeSelect.getItemCount());
		typePane.add(typeSelect);
		typePane.setMaximumSize(new Dimension(Short.MAX_VALUE, typeSelect.getPreferredSize().height));
		
		// Add a vertical strut to separate the display.
		add(Box.createVerticalStrut(10));
		
		// Create the description info label.
		descriptionInfoLabel = new WebLabel("Edit Mission Description (Optional)");
		descriptionInfoLabel.setFont(descriptionInfoLabel.getFont().deriveFont(Font.BOLD));
		descriptionInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionInfoLabel.setEnabled(false);
		add(descriptionInfoLabel);
		
		// Create the description panel.
		WebPanel descriptionPane = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		// Create the description label.
		descriptionLabel = new WebLabel("Description: ");
		descriptionLabel.setEnabled(false);
		descriptionPane.add(descriptionLabel);
		
		// Create the description text field.
		descriptionField = new WebTextField(20);
		descriptionField.setEnabled(false);
		descriptionPane.add(descriptionField);
		descriptionPane.setMaximumSize(new Dimension(Short.MAX_VALUE, descriptionField.getPreferredSize().height));
		
		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}
	
	public static void sortStringBubble( String  x [ ] )
    {
          int j;
          boolean flag = true;  // will determine when the sort is finished
          String temp;

          while ( flag )
          {
                flag = false;
                for ( j = 0;  j < x.length - 1;  j++ )
                {
                        if ( x [ j ].compareToIgnoreCase( x [ j+1 ] ) > 0 )
                        {                                             // ascending sort
                                    temp = x [ j ];
                                    x [ j ] = x [ j+1];     // swapping
                                    x [ j+1] = temp; 
                                    flag = true;
                         } 
                 } 
          } 
    } 
	
	/**
	 * Invoked when an item has been selected or deselected by the user.
	 * @param e the item event.
	 */
	public void itemStateChanged(ItemEvent e) {
		String selectedMission = (String) typeSelect.getSelectedItem();
		// 2015-12-15 Added "..."
		int num = 1;
	    //MissionManager manager = Simulation.instance().getMissionManager();
	    List<Mission> missions = missionManager.getMissions();
		for (Mission m : missions) {
			if (m.getName().equals(selectedMission))
				num++;
		}
		String suffix = " (" + num + ")";
		descriptionField.setText(MissionDataBean.getMissionDescription(selectedMission) + suffix);
		boolean enableDescription = (typeSelect.getSelectedIndex() != 0);
		descriptionInfoLabel.setEnabled(enableDescription);
		descriptionLabel.setEnabled(enableDescription);
		descriptionField.setEnabled(enableDescription);
		getWizard().setButtons(enableDescription);
	}
	
	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}
	
	/**
	 * Commits changes from this wizard panel.
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
		getWizard().getMissionData().setType((String) typeSelect.getSelectedItem());
		getWizard().getMissionData().setDescription(descriptionField.getText());
		getWizard().setFinalWizardPanels();
		return true;
	}
	
	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		// No previous panel to this one.
	}
	
	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		// No previous panel to this one.
	}
	
	public String getDescription() {
		return getWizard().getMissionData().getDescription();
		//return descriptionField.getText();
	}
}