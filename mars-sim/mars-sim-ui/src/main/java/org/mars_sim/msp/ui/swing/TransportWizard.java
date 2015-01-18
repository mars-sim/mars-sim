/**
 * Mars Simulation Project
 * TransportWizard.java
 * @version 3.07 2015-01-17

 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/** 
 * The TransportWizard class is an internal frame for building transport event.
 * 
 */
public class TransportWizard
extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(TransportWizard.class.getName());

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 10D;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 10D;
	private String buildingNickName;
	
	private JPanel mainPane ;
	private JLabel announcementLabel;
	
	private BuildingManager mgr;
	private MainDesktopPane desktop;
	private Settlement settlement;
	private SettlementWindow settlementWindow;
	private SettlementMapPanel mapPanel;
	private Resupply resupply;
	//private Building building;
	


	/** 
	 * Constructor .
	 * @param desktop the main desktop pane.
	 */
	public TransportWizard(final MainDesktopPane desktop) {
		super("Transport Wizard", false, false, false, false); //$NON-NLS-1$
		this.desktop = desktop;		
		//Simulation.instance().getMasterClock().setPaused(false);
		//createGUI();
	}

	public void createGUI(Building newBuilding) {
		
		settlement = newBuilding.getBuildingManager().getSettlement();
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconifiable(true);
		setClosable(true);
		Point location = MouseInfo.getPointerInfo().getLocation();
		setLocation(location);	
		//setTitle();
		// Create the main panel
		mainPane = new JPanel();
		mainPane.setLayout(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(10, 20, 10, 20));
		setContentPane(mainPane);

		announcementLabel = new JLabel("", JLabel.CENTER); //$NON-NLS-1$
		announcementLabel.setText("Do you like this location for the new building " + newBuilding + " ?");
		announcementLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.add(announcementLabel, BorderLayout.NORTH);
		//mainPane.setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
		
		JPanel btnPane = new JPanel();
		mainPane.add(btnPane, BorderLayout.CENTER);
		
		JButton b1 = new JButton("Accept");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//resupply.setUserAcceptance(true);
				//desktop.getMainWindow().unpauseSimulation();
			}
		});
		btnPane.add(b1);
		
		/*
		JButton b2 = new JButton("More Time");
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//BuildingManager manager = mgr;
				mgr.setPauseLonger(true);
			}
		});
		btnPane.add(b1);
		*/
		
		JButton b3 = new JButton("Next Location");
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//resupply.setUserAcceptance(false);				
				//desktop.getMainWindow().unpauseSimulation();
			}
		});
		btnPane.add(b3);
		
	}
	
	
	public void initialize(BuildingManager mgr) {//, Building building) {
		this.mgr = mgr;
		//this.building = building;
		//settlement = newBuilding.getBuildingManager().getSettlement();
		this.settlement = mgr.getSettlement();
		this.resupply = mgr.getResupply();
		this.settlementWindow = desktop.getSettlementWindow();
		this.mapPanel = settlementWindow.getMapPanel();
	}
	
	/**
     * Delivers supplies to the destination settlement.
     */
	// 2015-01-02 Added keyword synchronized to avoid JOption crash
    public synchronized void deliverBuildings() {  
        List<BuildingTemplate> orderedBuildings = resupply.orderNewBuildings();
        // 2014-12-23 Added sorting orderedBuildings according to its building id
        //Collections.sort(orderedBuildings);
        Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator(); 
        int size = orderedBuildings.size();
        int i = 0;

        while (buildingI.hasNext()) {
           BuildingTemplate template = buildingI.next();  
            // Check if building template position/facing collides with any existing buildings/vehicles/construction sites.
            if (resupply.checkBuildingTemplatePosition(template)) {
                // Correct length and width in building template.
                
                int buildingID = settlement.getBuildingManager().getUniqueBuildingIDNumber();
    
                // Replace width and length defaults to deal with variable width and length buildings.
                double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(template.getBuildingType());
                if (template.getWidth() > 0D) {
                    width = template.getWidth();
                }
                if (width <= 0D) {
                    width = DEFAULT_VARIABLE_BUILDING_WIDTH;
                }
                
                double length = SimulationConfig.instance().getBuildingConfiguration().getLength(template.getBuildingType());
                if (template.getLength() > 0D) {
                    length = template.getLength();
                }
                if (length <= 0D) {
                    length = DEFAULT_VARIABLE_BUILDING_LENGTH;
                }
                
                // 2015-01-16 Added getScenario()
                int scenarioID = settlement.getID();
                String scenario = getCharForNumber(scenarioID + 1);
                //String scenario = template.getScenario(); // Note: scenario is null since template does NOT have a scenario string yet
                buildingNickName = template.getBuildingType() + " " + scenario + buildingID;            
                //System.out.println("TransportWizard.java Line 173: scenario is " + scenario);
                //System.out.println("TransportWizard.java Line 174: buildingNickName is " + buildingNickName);        
                BuildingTemplate correctedTemplate = new BuildingTemplate(buildingID, scenario, template.getBuildingType(), buildingNickName, width, 
                        length, template.getXLoc(), template.getYLoc(), template.getFacing());

                confirmBuildingLocation(correctedTemplate, true);
 
            } // end of if (checkBuildingTemplatePosition(template)) {
            
            else { // when the building is not from the default MD Phase 1 Resupply Mission (NO pre-made template is available)
            	   // or when the building's designated location has already been occupied 
            		confirmBuildingLocation(template, false);    
            } // end of else {  
            i++;
	        if (i == size) {
	            Building aBuilding = mgr.getBuildings().get(0);
	        	settlement.fireUnitUpdate(UnitEventType.FINISH_BUILDING_PLACEMENT_EVENT, aBuilding);  
	        }
	    } // end of while (buildingI.hasNext())
		try { this.setClosed(true); }
		catch (java.beans.PropertyVetoException e) { }
    }
    
	/**
	 * Maps a number to an alphabet
	 * @param a number
	 * @return a String
	 */
	private String getCharForNumber(int i) {
		// NOTE: i must be > 1, if i = 0, return null
	    return i > 0 && i < 27 ? String.valueOf((char)(i + 'A' - 1)) : null;
	}
	
    
    /**
     * Asks user to confirm the location of the new building.
     * @param template
     * @param buildingManager 
     * @param isMarsDirectResupplyMission
     */
	public synchronized void confirmBuildingLocation(BuildingTemplate template, boolean isMarsDirectResupplyMission) {
  
		BuildingTemplate positionedTemplate ; // should NOT be null
		Building newBuilding ;
	    //final int TIME_OUT = 20;
	    //int count = TIME_OUT;
	    //pauseTimer = new Timer();
		// Hold off 10 seconds 
		//int seconds = 10;

         // Determine location and facing for the new building.
		if (isMarsDirectResupplyMission) {
			positionedTemplate = template;
			newBuilding = settlement.getBuildingManager().addOneBuilding(template, resupply, true);
		}
		else {
			positionedTemplate = resupply.positionNewResupplyBuilding(template.getBuildingType());
			//buildingManager.setBuildingArrived(true);
			newBuilding = settlement.getBuildingManager().addOneBuilding(positionedTemplate, resupply, true);
		}
		createGUI(newBuilding);
	   	//System.out.println("TransportWizard : new Building is " + newBuilding);

  		// set settlement based on where this building is located
  		// important for MainDesktopPane to look up this settlement variable when placing/transporting building 
  		settlement = newBuilding.getBuildingManager().getSettlement();

		double xLoc = newBuilding.getXLocation();
		double yLoc = newBuilding.getYLocation();
		double scale = mapPanel.getScale();
		//System.out.println("scale is " + scale + "  xLoc is "+ xLoc + "  yLoc is "+yLoc);
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		mapPanel.setShowBuildingLabels(true);
		repaint();
		//desktop.getMainWindow().pauseSimulation();

		//String name = newBuilding.getNickName();
        String message = "Do you like to place " + buildingNickName + " at this location on the map?";
        String title = "Transport Wizard";
		int reply = JOptionPane.showConfirmDialog(desktop.getAnnouncementWindow(), message, title, JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
            logger.info("Building in Placed : " + newBuilding.toString());
		}
		else { //userAcceptance = false;
			settlement.getBuildingManager().removeBuilding(newBuilding);
			confirmBuildingLocation(template, false);
			//try {Thread.sleep(1000);} catch (InterruptedException e1) {}
		}
		
		//SwingUtilities.invokeLater(new TransportWizardLauncher(newBuilding, template));
	}

	/**
	 * Internal class for launching the JOption window.
	
	private class TransportWizardLauncher implements Runnable {
	    private Building newBuilding;  
	    private BuildingTemplate template;
		private String name = newBuilding.getNickName();
	    private TransportWizardLauncher(Building newBuilding, BuildingTemplate template) {
	        this.newBuilding = newBuilding;
	        this.template = template;
	    }
	    
	    public void run() {
	        //Thread.sleep(50);
	        String message = "Do you like to place " + name + " at this location on the map?";
	        String title = "Transport Wizard";
			int reply = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
			if (reply == JOptionPane.YES_OPTION) {
	            logger.info("Building in Placed : " + newBuilding.toString());
			}
			else { //userAcceptance = false;
				mgr.removeBuilding(newBuilding);
				confirmBuildingLocation(template, false);
				//try {Thread.sleep(1000);} catch (InterruptedException e1) {}
			}
			// Note: adding try-catch can cause UI significant slow down here
	    }
	}
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		mgr = null;
		desktop = null;
		settlement = null;
		settlementWindow = null;
		mapPanel = null;
		resupply = null;
		//newBuilding = null;

	}
			 			
}