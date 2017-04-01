/**
 * Mars Simulation Project
 * TabPanelDeath.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * The TabPanelDeath is a tab panel with info about a person's death.
 */
public class TabPanelDeath
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private JTextField causeTF, timeTF, malTF;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelDeath(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelDeath.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelDeath.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;
		PhysicalCondition condition = person.getPhysicalCondition();
		DeathInfo death = condition.getDeathDetails();

		// Create death info label panel.
		JPanel deathInfoLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(deathInfoLabelPanel);

		// Prepare death info label
		JLabel deathInfoLabel = new JLabel(Msg.getString("TabPanelDeath.label"), JLabel.CENTER); //$NON-NLS-1$
		deathInfoLabel.setFont(new Font("Serif", Font.BOLD, 16));
		deathInfoLabelPanel.add(deathInfoLabel);

		// Prepare death label panel
		JPanel deathLabelPanel = new JPanel(new SpringLayout());//GridLayout(3, 2, 0, 0));
		deathLabelPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(deathLabelPanel, BorderLayout.NORTH);

		// Prepare cause label
		JLabel causeLabel = new JLabel(Msg.getString("TabPanelDeath.cause"), JLabel.LEFT); //$NON-NLS-1$
		deathLabelPanel.add(causeLabel);

		JPanel wrapper1 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        causeTF = new JTextField();
        causeTF.setText(death.getIllness().getName());
        causeTF.setEditable(false);
        causeTF.setColumns(20);
        causeTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper1.add(causeTF);//, BorderLayout.CENTER);
        deathLabelPanel.add(wrapper1);

		// Prepare time label
		JLabel timeLabel = new JLabel(Msg.getString("TabPanelDeath.time"), JLabel.LEFT); //$NON-NLS-1$
		deathLabelPanel.add(timeLabel);

		JPanel wrapper2 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        timeTF = new JTextField();
        timeTF.setText(death.getTimeOfDeath());
        timeTF.setEditable(false);
        timeTF.setColumns(20);
        timeTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper2.add(timeTF);//, BorderLayout.CENTER);
        deathLabelPanel.add(wrapper2);

		// Prepare malfunction label
		JLabel malfunctionLabel = new JLabel(Msg.getString("TabPanelDeath.malfunctionIfAny"), JLabel.LEFT); //$NON-NLS-1$
		deathLabelPanel.add(malfunctionLabel);

		JPanel wrapper3 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        malTF = new JTextField();
        malTF.setText(death.getMalfunction());
        malTF.setEditable(false);
        malTF.setColumns(20);
        malTF.setFont(new Font("Serif", Font.PLAIN, 12));
        wrapper3.add(malTF);//, BorderLayout.CENTER);
        deathLabelPanel.add(wrapper3);

		// 2017-03-31 Prepare SpringLayout
		SpringUtilities.makeCompactGrid(deathLabelPanel,
		                                2, 2, //rows, cols
		                                50, 5,        //initX, initY
		                                10, 1);       //xPad, yPad

		// Prepare bottom content panel
		JPanel bottomContentPanel = new JPanel(new BorderLayout(0, 0));
		centerContentPanel.add(bottomContentPanel, BorderLayout.CENTER);

		// Prepare location panel
		JPanel locationPanel = new JPanel(new BorderLayout());
		locationPanel.setBorder(new MarsPanelBorder());
		bottomContentPanel.add(locationPanel, BorderLayout.NORTH);

		// Prepare location label panel
		JPanel locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		locationPanel.add(locationLabelPanel, BorderLayout.NORTH);

		// Prepare center map button
		JButton centerMapButton = new JButton(ImageLoader.getIcon(Msg.getString("img.centerMap"))); //$NON-NLS-1$
		centerMapButton.setMargin(new Insets(1, 1, 1, 1));
		centerMapButton.addActionListener(this);
		centerMapButton.setToolTipText(Msg.getString("TabPanelDeath.tooltip.centerMap")); //$NON-NLS-1$
		locationLabelPanel.add(centerMapButton);

		// Prepare location label
		JLabel locationLabel = new JLabel(Msg.getString("TabPanelDeath.location"), JLabel.CENTER); //$NON-NLS-1$
		locationLabelPanel.add(locationLabel);

		if (death.getContainerUnit() != null) {
			// Prepare top container button
			JButton topContainerButton = new JButton(death.getContainerUnit().getName());
			topContainerButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					DeathInfo death = ((Person) getUnit()).getPhysicalCondition().getDeathDetails();
					getDesktop().openUnitWindow(death.getContainerUnit(), false);
				}
			});
			locationLabelPanel.add(topContainerButton);
		}
		else {
			JLabel locationLabel2 = new JLabel(death.getPlaceOfDeath(), JLabel.CENTER);
			locationLabelPanel.add(locationLabel2);
		}

		// Prepare location coordinates panel
		JPanel locationCoordsPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		locationPanel.add(locationCoordsPanel, BorderLayout.CENTER);

		// Initialize location cache
		Coordinates deathLocation = death.getLocationOfDeath();

		// Prepare latitude label
		JLabel latitudeLabel = new JLabel(Msg.getString("TabPanelDeath.latitude")  + " " + deathLocation.getFormattedLatitudeString(), JLabel.CENTER); //$NON-NLS-1$
		locationCoordsPanel.add(latitudeLabel);

		// Prepare longitude label
		JLabel longitudeLabel = new JLabel(Msg.getString("TabPanelDeath.longitude")  + " " + deathLocation.getFormattedLongitudeString(), JLabel.CENTER); //$NON-NLS-1$
		locationCoordsPanel.add(longitudeLabel);

		// Add empty panel
		bottomContentPanel.add(new JPanel(), BorderLayout.CENTER);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {}

	/**
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// Update navigator tool.
		desktop.centerMapGlobe(unit.getCoordinates());
	}
}

