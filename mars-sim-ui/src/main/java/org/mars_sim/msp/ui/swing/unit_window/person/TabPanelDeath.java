/*
 * Mars Simulation Project
 * TabPanelDeath.java
 * @date 2022-07-09
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;


/**
 * The TabPanelDeath is a tab panel with info about a person's death.
 */
@SuppressWarnings("serial")
public class TabPanelDeath
extends TabPanel
implements ActionListener {

	private static final String RIP_ICON = "rip";

	/** The Person instance. */
	private Person person = null;
	
	private JTextField causeTF, timeTF, malTF;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelDeath(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(RIP_ICON),
			Msg.getString("TabPanelDeath.title"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;		

	}

	protected void buildUI(JPanel content) {
			
		PhysicalCondition condition = person.getPhysicalCondition();
		DeathInfo death = condition.getDeathDetails();

		// Prepare death label panel
		JPanel deathLabelPanel = new JPanel(new SpringLayout());//GridLayout(3, 2, 0, 0));
		content.add(deathLabelPanel, BorderLayout.NORTH);

		// Prepare cause label
		JLabel causeLabel = new JLabel(Msg.getString("TabPanelDeath.cause"), JLabel.LEFT); //$NON-NLS-1$
		deathLabelPanel.add(causeLabel);

		JPanel wrapper1 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
        causeTF = new JTextField();
        causeTF.setText(death.getIllness().getName());
        causeTF.setEditable(false);
        causeTF.setColumns(20);
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
        wrapper3.add(malTF);//, BorderLayout.CENTER);
        deathLabelPanel.add(wrapper3);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(deathLabelPanel,
		                                3, 2, //rows, cols
		                                50, 5,        //initX, initY
		                                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad

		// Prepare bottom content panel
		JPanel bottomContentPanel = new JPanel(new BorderLayout(5, 5));
		content.add(bottomContentPanel, BorderLayout.CENTER);

		JPanel innerPanel = new JPanel();//new FlowLayout(FlowLayout.CENTER));
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		bottomContentPanel.add(innerPanel, BorderLayout.CENTER);

		// Prepare location label panel
		JPanel locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		innerPanel.add(locationLabelPanel);

		// Prepare center map button
		final Icon centerIcon = ImageLoader.getIconByName(NavigatorWindow.ICON);
		JButton centerMapButton = new JButton(centerIcon);
		centerMapButton.setMargin(new Insets(1, 1, 1, 1));
		centerMapButton.addActionListener(this);
		centerMapButton.setToolTipText(Msg.getString("TabPanelDeath.tooltip.centerMap"));
		locationLabelPanel.add(centerMapButton);

		// Prepare location label
		JLabel locationLabel = new JLabel("  " + Msg.getString("TabPanelDeath.placeOfDeath") + "  ", JLabel.CENTER); //$NON-NLS-1$
		locationLabelPanel.add(locationLabel);

		if (death.getContainerUnit() != null) {
			// Prepare top container button
			JButton topContainerButton = new JButton(death.getContainerUnit().getName());
			topContainerButton.setHorizontalAlignment(SwingConstants.CENTER);
			topContainerButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					DeathInfo death = ((Person) getUnit()).getPhysicalCondition().getDeathDetails();
					if (!(death.getContainerUnit() instanceof MarsSurface))
						getDesktop().openUnitWindow(death.getContainerUnit(), false);
				}
			});
			locationLabelPanel.add(topContainerButton);
		}
		else {
			JPanel wrapper4 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
			JTextField TF4 = new JTextField();
	        TF4.setText(death.getPlaceOfDeath());
	        TF4.setEditable(false);
	        TF4.setColumns(20);
	        wrapper4.add(TF4);//, BorderLayout.CENTER);
	        locationLabelPanel.add(wrapper4);
		}

		// Prepare location panel
		JPanel springPanel = new JPanel(new SpringLayout());//BorderLayout());
		//springPanel.setBorder(new MarsPanelBorder());
		//springPanel.setSize(300, 100);
		//bottomContentPanel.add(springPanel, BorderLayout.SOUTH);
		innerPanel.add(springPanel);

		// Initialize location cache
		Coordinates deathLocation = death.getLocationOfDeath();

		JLabel label0 = new JLabel(Msg.getString("TabPanelDeath.latitude"), JLabel.RIGHT); //$NON-NLS-1$
		springPanel.add(label0);

		// Prepare latitude label
		JLabel latitudeLabel = new JLabel(deathLocation.getFormattedLatitudeString(), JLabel.LEFT); //$NON-NLS-1$
		springPanel.add(latitudeLabel);

		JLabel label1 = new JLabel(Msg.getString("TabPanelDeath.longitude"), JLabel.RIGHT); //$NON-NLS-1$
		springPanel.add(label1);

		// Prepare longitude label
		JLabel longitudeLabel = new JLabel(deathLocation.getFormattedLongitudeString(), JLabel.LEFT); //$NON-NLS-1$
		springPanel.add(longitudeLabel);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(springPanel,
		                                2, 2, //rows, cols
		                                0, 0,        //initX, initY
		                                10, 10);       //xPad, yPad

		// Prepare empty panel
		JPanel lastWordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		innerPanel.add(lastWordPanel);

		JLabel label2 = new JLabel(Msg.getString("TabPanelDeath.lastWord")); //$NON-NLS-1$
		lastWordPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
		lastWordPanel.add(label2);

		// Prepare longitude label
		JTextArea lastWordTA = new JTextArea(5, 25);
		//lastWordTA.setSize(300, 150);
		lastWordTA.append(death.getLastWord());
		lastWordTA.setEditable(false);
		lastWordTA.setWrapStyleWord(true);
		lastWordTA.setLineWrap(true);
		lastWordTA.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(lastWordTA);
		lastWordPanel.add(scrollPane);
	}

	/**
	 * Action event occurs.
	 * @param event the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		// Update navigator tool.
		getDesktop().centerMapGlobe(getUnit().getCoordinates());
	}
}

