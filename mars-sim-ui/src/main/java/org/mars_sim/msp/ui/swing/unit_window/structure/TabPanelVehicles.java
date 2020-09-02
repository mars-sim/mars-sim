/**
 * Mars Simulation Project
 * TabPanelVehicles.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;

/** 
 * The TabPanelVehicles is a tab panel for parked vehicles and vehicles on mission.
 */
@SuppressWarnings("serial")
public class TabPanelVehicles
extends TabPanel
//implements MouseListener 
{

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private ParkedVehicleListModel parkedVehicleModel;
	private JList<Vehicle> parkedVehicles;
	private JScrollPane scrollPane0;

	private MissionVehicleListModel missionVehicleModel;
	private JList<Vehicle> missionVehicles;
	private JScrollPane scrollPane1;
	
	/**
	 * Constructor.
	 * @param unit the unit to display
	 * @param desktop the main desktop.
	 */
	public TabPanelVehicles(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelVehicles.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelVehicles.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		settlement = (Settlement) unit;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Create tab title label
		JLabel tabTitle = new JLabel(Msg.getString("TabPanelVehicles.title"), JLabel.CENTER); //$NON-NLS-1$
		tabTitle.setFont(new Font("Serif", Font.BOLD, 16));
		//label.setForeground(new Color(102, 51, 0)); // dark brown
		topContentPanel.add(tabTitle);
		
		JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(emptyPanel);
		
		//////////////////////////////
		// Create parked vehicle panel
		JPanel parkedVehiclePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(parkedVehiclePanel);
		
		// Create vehicle label
//		JLabel label = new JLabel(Msg.getString("TabPanelVehicles.parked.vehicles"), JLabel.CENTER); //$NON-NLS-1$
//		label.setFont(new Font("Serif", Font.ITALIC, 14));
//		//label.setForeground(new Color(102, 51, 0)); // dark brown
//		parkedVehiclePanel.add(label);

		WebLabel parkedLabel = new WebLabel("    " + Msg.getString("TabPanelVehicles.parked.vehicles") + "    ", JLabel.CENTER); //$NON-NLS-1$
		parkedLabel.setUI(new VerticalLabelUI(false));
		parkedLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		parkedLabel.setBorder(new MarsPanelBorder());

		// Create vehicle display panel
		JPanel parkedVehicleDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		vehicleDisplayPanel.setBorder(new MarsPanelBorder());
		parkedVehiclePanel.add(parkedVehicleDisplayPanel);
		parkedVehicleDisplayPanel.add(parkedLabel);
		
		// Create scroll panel for vehicle list.
		scrollPane0 = new JScrollPane();
		scrollPane0.setPreferredSize(new Dimension(175, 200));
		parkedVehicleDisplayPanel.add(scrollPane0);

		
		// Create vehicle list model
		parkedVehicleModel = new ParkedVehicleListModel(settlement);

		// Create vehicle list
		parkedVehicles = new JList<Vehicle>(parkedVehicleModel);
//		parkedVehicles.addMouseListener(this);
		scrollPane0.setViewportView(parkedVehicles);
		
		parkedVehicles.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
//		        JList<?> list = (JList<?>)evt.getSource();
		        if (evt.getClickCount() == 2) {

		            // Double-click detected
//		            int index = list.locationToIndex(evt.getPoint());
		            
					Vehicle vehicle = (Vehicle) parkedVehicles.getSelectedValue();
					if (vehicle != null) {
						desktop.openUnitWindow(vehicle, false);
					}
		        }
		       
//		        else if (evt.getClickCount() == 3) {
//
//		            // Triple-click detected
//		            int index = list.locationToIndex(evt.getPoint());
//		        }
		    }
		});
		
		topContentPanel.add(emptyPanel);
		
		///////////////////////////////
		// Create mission vehicle panel
		JPanel missionVehiclePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		centerContentPanel.add(missionVehiclePanel);
		
		// Create mission vehicle label
//		JLabel missionTitle = new JLabel(Msg.getString("TabPanelVehicles.mission.vehicles"), JLabel.CENTER); //$NON-NLS-1$
//		missionTitle.setFont(new Font("Serif", Font.ITALIC, 14));
//		//label.setForeground(new Color(102, 51, 0)); // dark brown
//		missionVehiclePanel.add(missionTitle);
		
		WebLabel missionLabel = new WebLabel(" " + Msg.getString("TabPanelVehicles.mission.vehicles") + " ", JLabel.CENTER); //$NON-NLS-1$
		missionLabel.setUI(new VerticalLabelUI(true));
		missionLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		missionLabel.setBorder(new MarsPanelBorder());

		// Create vehicle display panel
		JPanel missionvehicleDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//		vehicleDisplayPanel.setBorder(new MarsPanelBorder());
		missionVehiclePanel.add(missionvehicleDisplayPanel);
		
		// Create scroll panel for mission vehicle list.
		scrollPane1 = new JScrollPane();
		scrollPane1.setPreferredSize(new Dimension(175, 200));
		missionvehicleDisplayPanel.add(scrollPane1);

		missionvehicleDisplayPanel.add(missionLabel);
		
		// Create mission vehicle list model
		missionVehicleModel = new MissionVehicleListModel(settlement);

		// Create mission vehicle list
		missionVehicles = new JList<Vehicle>(missionVehicleModel);
//		missionVehicles.addMouseListener(this);
		scrollPane1.setViewportView(missionVehicles);
		
		missionVehicles.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
//		        JList<?> list = (JList<?>)evt.getSource();
		        if (evt.getClickCount() == 2) {

		            // Double-click detected
//		            int index = list.locationToIndex(evt.getPoint());
		            
					Vehicle vehicle = (Vehicle) missionVehicles.getSelectedValue();
					if (vehicle != null) {
						desktop.openUnitWindow(vehicle, false);
					}
		        }
		       
//		        else if (evt.getClickCount() == 3) {
//
//		            // Triple-click detected
//		            int index = list.locationToIndex(evt.getPoint());
//		        }
		    }
		});
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// Update vehicle list
		parkedVehicleModel.update();
		scrollPane0.validate();
		missionVehicleModel.update();
		scrollPane1.validate();
	}
	
	/**
     * List model for settlement parked vehicles.
     */
    private class ParkedVehicleListModel extends AbstractListModel<Vehicle> {

        /** default serial id. */
        private static final long serialVersionUID = 1L;
        
        private Settlement settlement;
        private List<Vehicle> vehicleList;
        
        private ParkedVehicleListModel(Settlement settlement) {
            this.settlement = settlement;
            
            vehicleList = new ArrayList<Vehicle>(settlement.getParkedVehicles());
            Collections.sort(vehicleList);
        }
        
        @Override
        public Vehicle getElementAt(int index) {
            
            Vehicle result = null;
            
            if ((index >= 0) && (index < vehicleList.size())) {
                result = vehicleList.get(index);
            }
            
            return result;
        }

        @Override
        public int getSize() {
            return vehicleList.size();
        }
        
        /**
         * Update the list model.
         */
        public void update() {
            
            if (!vehicleList.containsAll(settlement.getParkedVehicles()) || 
                    !settlement.getParkedVehicles().containsAll(vehicleList)) {
                
                List<Vehicle> old = vehicleList;
                
                List<Vehicle> temp = new ArrayList<Vehicle>(settlement.getParkedVehicles());
                Collections.sort(temp);
                
                vehicleList = temp;
                fireContentsChanged(this, 0, getSize());
                
                old.clear();
            }
        }
    }

	/**
     * List model for settlement parked vehicles.
     */
    private class MissionVehicleListModel extends AbstractListModel<Vehicle> {

        /** default serial id. */
        private static final long serialVersionUID = 1L;
        
        private Settlement settlement;
        private List<Vehicle> vehicleList;
        
        private MissionVehicleListModel(Settlement settlement) {
            this.settlement = settlement;
            
            vehicleList = new ArrayList<Vehicle>(settlement.getMissionVehicles());
            Collections.sort(vehicleList);
        }
        
        @Override
        public Vehicle getElementAt(int index) {
            
            Vehicle result = null;
            
            if ((index >= 0) && (index < vehicleList.size())) {
                result = vehicleList.get(index);
            }
            
            return result;
        }

        @Override
        public int getSize() {
            return vehicleList.size();
        }
        
        /**
         * Update the population list model.
         */
        public void update() {
            
            if (!vehicleList.containsAll(settlement.getMissionVehicles()) || 
                    !settlement.getMissionVehicles().containsAll(vehicleList)) {
                
                List<Vehicle> old = vehicleList;
                
                List<Vehicle> temp = new ArrayList<Vehicle>(settlement.getMissionVehicles());
                Collections.sort(temp);
                
                vehicleList = temp;
                fireContentsChanged(this, 0, getSize());
                
                old.clear();
            }
        }
    }
    
//	/** 
//	 * Mouse clicked event occurs.
//	 * @param event the mouse event
//	 */
//	public void mouseClicked(MouseEvent event) {
//		// If double-click, open person window.
//		if (event.getClickCount() >= 2) {
//			Vehicle vehicle = (Vehicle) parkedVehicles.getSelectedValue();
//			if (vehicle != null) {
//				desktop.openUnitWindow(vehicle, false);
//			}
//		}
//	}
//
//	public void mousePressed(MouseEvent event) {}
//	public void mouseReleased(MouseEvent event) {}
//	public void mouseEntered(MouseEvent event) {}
//	public void mouseExited(MouseEvent event) {}
	
	/**
     * Prepare object for garbage collection.
     */
    public void destroy() {
    	parkedVehicleModel = null;
    	parkedVehicles = null;
    	scrollPane0 = null;
    	missionVehicleModel = null;
    	missionVehicles = null;
    	scrollPane1 = null;
    }
}
