/*
 * Mars Simulation Project
 * TabPanelEVA.java
 * @date 2024-06-24
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.VehicleAirlock;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AirlockPanel;

/**
 * The TabPanelEVA class represents the EVA airlock function of a vehicle.
 */
@SuppressWarnings("serial")
class TabPanelEVA extends EntityTabPanel<Rover> implements TemporalComponent{

	private static final String SUIT_ICON = "eva"; 
	
	private VehicleAirlock vehicleAirlock;

	private AirlockPanel airlockPanel;

    /**
     * Constructor.
     * 
     * @param vehicle the vehicle.
     * @param context The UI context.
     */
    public TabPanelEVA(Rover vehicle, UIContext context) {
        // Use the TabPanel constructor
        super(
            Msg.getString("TabPanelEVA.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(SUIT_ICON),        	
        	Msg.getString("TabPanelEVA.tooltip"), //$NON-NLS-1$
        	context, vehicle
        );

        vehicleAirlock = (VehicleAirlock)vehicle.getAirlock();
    }
    
	/**
	 * Builds the UI.
	 * 
	 * @param content
	 */
    @Override
    protected void buildUI(JPanel content) {
	
		airlockPanel = new AirlockPanel(vehicleAirlock, getContext());
        content.add(airlockPanel, BorderLayout.CENTER);
    }

	/**
	 * Update airlock counts
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		airlockPanel.clockUpdate(pulse);
    }

    @Override
    public void destroy() {
    	airlockPanel.unregister();
        super.destroy();
    }
}