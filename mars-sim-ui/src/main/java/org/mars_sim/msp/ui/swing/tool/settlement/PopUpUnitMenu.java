/**
 * Mars Simulation Project
 * PopUpUnitMenu.java
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
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;


import org.apache.commons.lang3.text.WordUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;


// TODO: is extending to JInternalFrame better?
public class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	private JMenuItem itemOne, itemTwo, itemThree;
    //private Building building;
    //private Vehicle vehicle;
    private Unit unit;
    private Settlement settlement;
	private MainDesktopPane desktop;
	
    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
    	//this.building = building;
    	//this.vehicle = vehicle;
    	this.unit = unit;
    	this.settlement = swindow.getMapPanel().getSettlement();
        this.desktop = swindow.getDesktop();
        
        UIResource res = new BorderUIResource.LineBorderUIResource(Color.orange);
        UIManager.put("PopupMenu.border", res);
        //force to the Heavyweight Component or able for AWT Components
        this.setLightWeightPopupEnabled(false); 
             
    	itemOne = new JMenuItem(Msg.getString("PopUpUnitMenu.itemOne"));
        itemTwo = new JMenuItem(Msg.getString("PopUpUnitMenu.itemTwo"));  
        itemThree = new JMenuItem(Msg.getString("PopUpUnitMenu.itemThree"));
        itemOne.setForeground(new Color(139,69,19));
        itemTwo.setForeground(new Color(139,69,19));
        itemThree.setForeground(new Color(139,69,19));
        
        if (unit instanceof Person) {
        	add(itemTwo);
        	buildItemTwo(unit);
        }
        
        else if (unit instanceof Vehicle) {
        	add(itemOne);
        	add(itemTwo);
        	add(itemThree);
        	buildItemOne(unit);
            buildItemTwo(unit);
            buildItemThree(unit);
        }
        else {
            add(itemOne);
        	add(itemTwo);
        	buildItemOne(unit);
            buildItemTwo(unit);
        }
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
       
	
    public void buildItemOne(final Unit unit) {
    	
        itemOne.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e) {
            	setOpaque(false);
            	final JDialog d = new JDialog();
                d.setForeground(Color.YELLOW); // orange font
                d.setFont( new Font("Arial", Font.BOLD, 14 ) );
                
                String description;
                String type;
                String name;
                
                if (unit instanceof Vehicle) {
                	Vehicle vehicle = (Vehicle) unit;
                	description = vehicle.getDescription(vehicle.getVehicleType());
                	type = WordUtils.capitalize(vehicle.getVehicleType());
                	name = WordUtils.capitalize(vehicle.getName());
                }
                else {
                	Building building = (Building) unit;
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getNickName();
                }
                
			    
			    d.setSize(350, 300); // undecorated 301, 348 ; decorated : 303, 373
		        d.setResizable(false);
		        d.setUndecorated(true);
		        d.setBackground(new Color(0,0,0,0));
		        
			    UnitInfoPanel b = new UnitInfoPanel(desktop);
			    b.init(name, type, description);		
			    
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
     
    
    public void buildItemTwo(final Unit unit) {
        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	
	            if (unit instanceof Vehicle) {
	            	Vehicle vehicle = (Vehicle) unit;
	            	desktop.openUnitWindow(vehicle, false);
	            }
	            else if (unit instanceof Person) {
	            	Person person =(Person) unit;
	            	desktop.openUnitWindow(person, false);
	            }
	            else {
                	Building building = (Building) unit;
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
	         }
	    });
 
    }
    
	public void buildItemThree(final Unit unit) {
	        itemThree.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	//if (unit instanceof Vehicle) {
		            Vehicle vehicle = (Vehicle) unit;
		            vehicle.determinedSettlementParkedLocationAndFacing();
		    		repaint();
	            }
	        });
	}
    
	public void destroy() {
		settlement = null;
		settlement.destroy();
		unit = null;
		unit.destroy();
		itemOne = null;
		itemTwo = null;			
	}
}