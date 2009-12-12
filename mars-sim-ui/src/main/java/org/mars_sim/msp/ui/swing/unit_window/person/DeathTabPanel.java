/**
 * Mars Simulation Project
 * DeathTabPanel.java
 * @version 2.84 2008-05-24
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;



/** 
 * The DeathTabPanel is a tab panel with info about a person's death.
 */
public class DeathTabPanel extends TabPanel implements ActionListener {
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public DeathTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Death", null, "Death Info", unit, desktop);
        
        Person person = (Person) unit;
        PhysicalCondition condition = person.getPhysicalCondition();
        DeathInfo death = condition.getDeathDetails();
        
        // Create death info label panel.
        JPanel deathInfoLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topContentPanel.add(deathInfoLabelPanel);
        
        // Prepare death info label
        JLabel deathInfoLabel = new JLabel("Death Info", JLabel.CENTER);
        deathInfoLabelPanel.add(deathInfoLabel);
        
        // Prepare death label panel
        JPanel deathLabelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
        deathLabelPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(deathLabelPanel, BorderLayout.NORTH);
        
        // Prepare cause label
        JLabel causeLabel = new JLabel("Cause: " + death.getIllness(), JLabel.LEFT);
        deathLabelPanel.add(causeLabel);
        
        // Prepare time label
        JLabel timeLabel = new JLabel("Time: " + death.getTimeOfDeath(), JLabel.LEFT);
        deathLabelPanel.add(timeLabel);
        
        // Prepare malfunction label
        JLabel malfunctionLabel = new JLabel("Malfunction (if any): " + death.getMalfunction(), JLabel.LEFT);
        deathLabelPanel.add(malfunctionLabel);
        
        // Prepare bottom content panel
        JPanel bottomContentPanel = new JPanel(new BorderLayout(0, 0));
        centerContentPanel.add(bottomContentPanel, BorderLayout.CENTER);
        
        // Prepare location panel
        JPanel locationPanel = new JPanel(new BorderLayout());
        locationPanel.setBorder(new MarsPanelBorder());
        bottomContentPanel.add(locationPanel, BorderLayout.NORTH);
        
        // Prepare location label panel
        JPanel locationLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(locationLabelPanel, BorderLayout.NORTH);
        
        // Prepare center map button
        JButton centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(this);
        centerMapButton.setToolTipText("Locate in Mars Navigator");
        locationLabelPanel.add(centerMapButton);
        
        // Prepare location label
        JLabel locationLabel = new JLabel("Location: ", JLabel.CENTER);
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
        
        // Prepare location coordinates panel
        JPanel locationCoordsPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        locationPanel.add(locationCoordsPanel, "Center");

        // Initialize location cache
        Coordinates deathLocation = death.getLocationOfDeath();
        
        // Prepare latitude label
        JLabel latitudeLabel = new JLabel("Latitude: " + deathLocation.getFormattedLatitudeString(), JLabel.LEFT);
        locationCoordsPanel.add(latitudeLabel);

        // Prepare longitude label
        JLabel longitudeLabel = new JLabel("Longitude: " + deathLocation.getFormattedLongitudeString(), JLabel.LEFT);
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
     *
     * @param event the action event
     */
    public void actionPerformed(ActionEvent event) {
        
        // Update navigator tool.
        desktop.centerMapGlobe(unit.getCoordinates());
    }
}
       
