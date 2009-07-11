package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.simulation.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.ui.standard.MainDesktopPane;

public class AstronomicalObservationBuildingPanel extends BuildingFunctionPanel {
	
	private static final long serialVersionUID = 1L;
	
    private static String CLASS_NAME = 
   "org.mars_sim.msp.ui.standard.unit_window.structure.building.AstronomicalObservationBuildingPanel";

   private static Logger s_log = Logger.getLogger(CLASS_NAME);
   
   private int currentObserversAmount;
   
   private AstronomicalObservation function;
   
   private JLabel observersLabel;
   
	public AstronomicalObservationBuildingPanel(AstronomicalObservation observatory,
			MainDesktopPane desktop) {
	  	super(observatory.getBuilding(), desktop);
		
			// Set panel layout
			setLayout(new BorderLayout());
			
			function = observatory;
			
			currentObserversAmount = function.getCurrentObserversNumber();
			
			// Prepare label panelAstronomicalObservation
			JPanel labelPanel = new JPanel(new GridLayout(4, 1, 0, 0));
			add(labelPanel, BorderLayout.NORTH);
			
			// Astronomy top label
			JLabel astronomyLabel = new JLabel("Astronomy Observation", JLabel.CENTER);
			labelPanel.add(astronomyLabel);
			
 
			// Observer number label
			observersLabel = new JLabel("Number of Observers: " 
					       + currentObserversAmount, JLabel.CENTER);
			labelPanel.add(observersLabel);
			
			// Observer capacityLabel
			JLabel observerCapacityLabel = new JLabel("Observer Capacity: " 
					+ function.getObservatoryCapacity(),JLabel.CENTER);
			labelPanel.add(observerCapacityLabel);
		
	}


	public void update() {
       if(currentObserversAmount != function.getCurrentObserversNumber()){
    	   currentObserversAmount = function.getCurrentObserversNumber();
    	   observersLabel.setText("Number of Observers: " + currentObserversAmount);
       }
		
	}

}
