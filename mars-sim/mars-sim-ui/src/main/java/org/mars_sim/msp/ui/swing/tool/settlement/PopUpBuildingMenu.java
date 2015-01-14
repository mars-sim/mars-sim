/**
 * Mars Simulation Project
 * PopUpBuildingMenu.java
 * @version 3.07 2015-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;


// TODO: is extending to JInternalFrame better?
public class PopUpBuildingMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	private JMenuItem itemOne, itemTwo;
    private Building building;
    private Settlement settlement;
	private MainDesktopPane desktop;
	private String buildingName ;
	
    public PopUpBuildingMenu(final SettlementWindow swindow, final Building building){
    	this.building = building;
    	this.settlement = swindow.getMapPanel().getSettlement();
        this.desktop = swindow.getDesktop();
    	itemOne = new JMenuItem(Msg.getString("PopUpBuildingMenu.itemOne"));
        itemTwo = new JMenuItem(Msg.getString("PopUpBuildingMenu.itemTwo"));       
        add(itemOne);
        add(itemTwo);
        
        buildingName = building.getNickName();
        	
        buildItemOne();
        buildItemTwo();
        /*
     // Determine what the GraphicsDevice can support.
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        boolean isPerPixelTranslucencySupported = 
            gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT);

        //If translucent windows aren't supported, exit.
        if (!isPerPixelTranslucencySupported) {
            System.out.println(
                "Per-pixel translucency is not supported");
                System.exit(0);
        }
        */

    }
       
	
    public void buildItemOne() {
    	
        itemOne.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e) {
            	setOpaque(false);
            	final JDialog d = new JDialog();

                d.setForeground(Color.YELLOW); // orange font
                d.setFont( new Font("Arial", Font.BOLD, 14 ) );
    		   	// 2014-11-27 Added building.getDescription() for loading text
			    String description = building.getDescription();
			    d.setSize(350, 300); // undecorated 301, 348 ; decorated : 303, 373
		        d.setResizable(false);
		        d.setUndecorated(true);
		        d.setBackground(new Color(0,0,0,0));
		        
			    BuildingInfoPanel b = new BuildingInfoPanel(desktop);
   	
			    b.init(buildingName, description);			    
			    d.add(b);
            	
            	// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                d.setLocation(location); 
                
                d.setVisible(true); 
				d.addWindowFocusListener(new WindowFocusListener() {            
				    public void windowLostFocus(WindowEvent e) {
				    	d.dispose();
				    }            
				    public void windowGainedFocus(WindowEvent e) {
				    }
				});	
				
			    //2014-11-27 Added ComponentMover Class
			    ComponentMover mover = new ComponentMover(d,b);
			    mover.registerComponent(b);	
	
             }
        });
    }
        
    
    public void buildItemTwo() {
        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//setOpaque(false);

            	final JDialog d = new JDialog();
            	
        		//2014-11-27 Added ComponentMover Class
                // Make panel drag-able
        		ComponentMover cm = new ComponentMover();
        		cm.registerComponent(d);
 
				final BuildingPanel buildingPanel = new BuildingPanel(true, "Building Detail", building, desktop);
               
	    		buildingPanel.setOpaque(false);
                buildingPanel.setBackground(new Color(0,0,0,150));
                buildingPanel.setTheme(true);
         		
	    		// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                d.setLocation(location);
				d.setUndecorated(true);
                d.setBackground(new Color(51,25,0,128)); // java.awt.IllegalComponentStateException: The dialog is decorated
                d.add(buildingPanel);
				d.setSize(300,335);  // undecorated: 300, 335; decorated: 310, 370
				d.setLayout(new FlowLayout()); 

				d.setVisible(true);
				d.getRootPane().setBorder( BorderFactory.createLineBorder(Color.orange) );

			    d.addWindowFocusListener(new WindowFocusListener() {            
					public void windowLostFocus(WindowEvent e) {
				    	//JWindow w = (JWindow) e.getSource();
				    	d.dispose();
				    	//w.dispose();
					}            
					public void windowGainedFocus(WindowEvent e) {
					}
				});

            }
        });
        
    }
    
	public void destroy() {
			settlement.destroy();
			building.destroy();
			itemOne = null;
			itemTwo = null;			
	}
}