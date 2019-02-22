/**
 * Mars Simulation Project
 * AstronomicalObservationBuildingPanel.java
 * @version 3.1.0 2017-09-15
 * @author Sebastien Venot
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * A panel for the astronomical observation building function.
 */
public class BuildingPanelAstronomicalObservation
extends BuildingFunctionPanel {

	// Data members
	private int currentObserversAmount;

	private WebLabel observersLabel;
	//private BalloonToolTip balloonToolTip = new BalloonToolTip();
	
	private AstronomicalObservation function;
	private OrbitViewer orbitViewer;
	
	/**
	 * Constructor.
	 * @param observatory the astronomical observatory building function.
	 * @param desktop the main desktop.
	 */
	public BuildingPanelAstronomicalObservation(AstronomicalObservation observatory, 
			MainDesktopPane desktop) {
		// User BuildingFunctionPanel constructor.
		super(observatory.getBuilding(), desktop);

		function = observatory;
		currentObserversAmount = function.getObserverNum();

		// Set panel layout
		setLayout(new BorderLayout());
		
		// Prepare label panelAstronomicalObservation
		WebPanel labelPanel = new WebPanel(new GridLayout(5, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);

		// Astronomy top label
		// 2014-11-21 Changed font type, size and color and label text
		WebLabel astronomyLabel = new WebLabel(Msg.getString("BuildingPanelAstronomicalObservation.title"), WebLabel.CENTER); //$NON-NLS-1$
		astronomyLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//astronomyLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(astronomyLabel);

		// Observer number label
		// 2014-11-21 Fixed currentObserversAmount
		observersLabel = new WebLabel(Msg.getString("BuildingPanelAstronomicalObservation.numberOfObservers", currentObserversAmount), WebLabel.CENTER); //$NON-NLS-1$
		observersLabel.setHorizontalAlignment(WebLabel.CENTER);
		update();
		labelPanel.add(observersLabel);

		// Observer capacityLabel
		WebLabel observerCapacityLabel = new WebLabel(
			Msg.getString(
				"BuildingPanelAstronomicalObservation.observerCapacity", //$NON-NLS-1$
				function.getObservatoryCapacity()
			),WebLabel.CENTER
		);
		labelPanel.add(observerCapacityLabel);
		
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
		astronomyLabel.setOpaque(false);
		astronomyLabel.setBackground(new Color(0,0,0,128));
		observersLabel.setOpaque(false);
		observersLabel.setBackground(new Color(0,0,0,128));
		
      	// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		
		// Create the orbit viewer button.
		WebButton button = new WebButton("Orbit Viewer");
		
		//balloonToolTip.createBalloonTip(button, "Click to open the solar system orbit viewer"); 
		//button.setToolTipText("Click to open the solar system orbit viewer");
		button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
//					openOrbitViewer();
				}
			});
		buttonPane.add(button);
		labelPanel.add(buttonPane);
	}

	@Override
	public void update() {
		if (currentObserversAmount != function.getObserverNum()) {
			currentObserversAmount = function.getObserverNum();
			observersLabel.setText(
				Msg.getString(
					"BuildingPanelAstronomicalObservation.numberOfObservers", //$NON-NLS-1$
					currentObserversAmount
				)
			);
		}
	}
	
    public void setViewer(OrbitViewer orbitViewer) {
    	this.orbitViewer = orbitViewer;
    }
    
//	/**
//	 * Open orbit viewer
//	 */
//    // 2015-11-04 Added openOrbitViewer()
//	private void openOrbitViewer() {
//
//		MainWindow mw = desktop.getMainWindow();
//		if (mw != null)  {
//			if (orbitViewer == null && !desktop.isOrbitViewerOn())
//				orbitViewer = new OrbitViewer(desktop, this);
//		}
//
//		MainScene ms = desktop.getMainScene();
//		
//		if (ms != null)  {
//			if (orbitViewer == null && !desktop.isOrbitViewerOn()) {
//				orbitViewer = new OrbitViewer(desktop, this);
//			}
//		}
//	}

}