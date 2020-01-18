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
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;

/**
 * A wizard panel for selecting mission type.
 */
@SuppressWarnings("serial")
public class TypePanel extends WizardPanel implements ItemListener {

	/** The wizard panel name. */
	private final static String NAME = "Mission Type";
	
	// Private members.
	private JComboBoxMW<String> typeSelect;
	private WebLabel descriptionInfoLabel;
	private WebLabel descriptionLabel;
	private WebTextField descriptionTF;
	
	private String descriptionText;
	
	private static MissionManager missionManager;

	//private CreateMissionWizard wizard;
	
	
	/**
	 * Constructor.
	 * @param wizard {@link CreateMissionWizard} the create mission wizard.
	 */
	@SuppressWarnings("unchecked")
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
		MissionType[] missionTypes = MissionDataBean.getMissionTypes();
//		sortStringBubble(missionTypes);
//		MissionType[] displayMissionTypes = new MissionType[missionTypes.length];
		List<String> types = new ArrayList<>();
		int size = missionTypes.length;
		for (int i=0; i<size; i++) {
			types.add(missionTypes[i].getName());
		}
//		displayMissionTypes[0] = "";
//        System.arraycopy(missionTypes, 0, displayMissionTypes, 1, missionTypes.length);
		typeSelect = new JComboBoxMW<String>();
		Iterator<String> k = types.iterator();
		while (k.hasNext()) 
			typeSelect.addItem(k.next());
		typeSelect.setSelectedIndex(-1);
		
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
		descriptionTF = new WebTextField(20);
		descriptionTF.setEnabled(false);
		descriptionPane.add(descriptionTF);
		descriptionPane.setMaximumSize(new Dimension(Short.MAX_VALUE, descriptionTF.getPreferredSize().height));

		// Listen for changes in the text
		addChangeListener(descriptionTF, e -> {
			  descriptionText = descriptionTF.getText();
//			  System.out.println("descriptionText: " + descriptionText);
		});
		
		// Add a vertical glue.
		add(Box.createVerticalGlue());
	}
	

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 * 
	 * @see https://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield#3953219
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
	    Objects.requireNonNull(text);
	    Objects.requireNonNull(changeListener);
	    DocumentListener dl = new DocumentListener() {
	        private int lastChange = 0, lastNotifiedChange = 0;

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	            lastChange++;
	            SwingUtilities.invokeLater(() -> {
	                if (lastNotifiedChange != lastChange) {
	                    lastNotifiedChange = lastChange;
	                    changeListener.stateChanged(new ChangeEvent(text));
	                }
	            });
	        }
	    };
	    text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
	        Document d1 = (Document)e.getOldValue();
	        Document d2 = (Document)e.getNewValue();
	        if (d1 != null) d1.removeDocumentListener(dl);
	        if (d2 != null) d2.addDocumentListener(dl);
	        dl.changedUpdate(null);
	    });
	    Document d = text.getDocument();
	    if (d != null) d.addDocumentListener(dl);
	}
	
	
	/**
	 * Invoked when an item has been selected or deselected by the user.
	 * @param e the item event.
	 */
	public void itemStateChanged(ItemEvent e) {
		String selectedMission = (String)typeSelect.getSelectedItem();
		// Add SUFFIX to distinguish between different mission having the same mission type
		int suffix = 1;
	    List<Mission> missions = missionManager.getMissions();
		for (Mission m : missions) {
			if (m.getMissionType().getName().equalsIgnoreCase(selectedMission))
				suffix++;
		}
		String suffixString = " (" + suffix + ")";
		descriptionText = selectedMission + suffixString;
		descriptionTF.setText(descriptionText);
		boolean enableDescription = (typeSelect.getSelectedIndex() != -1);
		descriptionInfoLabel.setEnabled(enableDescription);
		descriptionLabel.setEnabled(enableDescription);
		descriptionTF.setEnabled(enableDescription);
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
		getWizard().getMissionData().setMissionType(MissionType.lookup((String) typeSelect.getSelectedItem()));	
		getWizard().getMissionData().setDescription(descriptionTF.getText());
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
	
	public String getDesignation() {
		return getWizard().getMissionData().getDesignation();
		//return descriptionField.getText();
	}
	
	public String getDescription() {
		return getWizard().getMissionData().getDescription();
		//return descriptionField.getText();
	}
}