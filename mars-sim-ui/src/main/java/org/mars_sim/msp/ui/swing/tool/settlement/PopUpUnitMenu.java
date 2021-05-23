/**
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.LineBreakPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;

import com.alee.laf.desktoppane.WebInternalFrame;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.window.WebDialog;
import com.alee.managers.style.StyleId;

public class PopUpUnitMenu extends WebPopupMenu {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH_0 = 350;

	public static final int WIDTH_1 = WIDTH_0;
	public static final int HEIGHT_1 = 300;
	
	public static final int WIDTH_2 = UnitWindow.WIDTH - 136;
	public static final int HEIGHT_2 = UnitWindow.HEIGHT - 133;
	
	private WebMenuItem itemOne, itemTwo, itemThree;
    private Unit unit;
    private Settlement settlement;
	private MainDesktopPane desktop;
	
    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
    	this.unit = unit;
    	desktop = swindow.getDesktop();
    	this.settlement = swindow.getMapPanel().getSettlement();
      
//       	setOpaque(false);
       	
//        UIResource res = new BorderUIResource.LineBorderUIResource(Color.orange);
//        UIManager.put("PopupMenu.border", res);
//        //force to the Heavyweight Component or able for AWT Components
//        this.setLightWeightPopupEnabled(false); 
             
    	itemOne = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemOne"));
        itemTwo = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemTwo"));  
        itemThree = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemThree"));
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

     // Determine what the GraphicsDevice can support.
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice gd = ge.getDefaultScreenDevice();
//        boolean isPerPixelTranslucencySupported = 
//            gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT);
//
//        //If translucent windows aren't supported, exit.
//        if (!isPerPixelTranslucencySupported) {
//            System.out.println(
//                "Per-pixel translucency is not supported");
//                System.exit(0);
//        }

    }
       
	
    public void buildItemOne(final Unit unit) {
    	
        itemOne.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e) {
            	final WebDialog<?> d = new WebDialog<>(StyleId.dialogTransparent);//.dialogDecorated);
         	
	           	setOpaque(false);
		        setBackground(new Color(0,0,0,128));
//		        
		        d.setForeground(Color.WHITE); // orange font
                d.setFont(new Font("Arial", Font.BOLD, 14));
                
		        d.setUndecorated(true);
            	d.setOpacity(0.75f);
		        d.setBackground(new Color(0,0,0,128));
		        
                String description;
                String type;
                String name;
                
                if (unit instanceof Vehicle) {
                	Vehicle vehicle = (Vehicle) unit;
                	description = vehicle.getDescription(vehicle.getVehicleType());
                	type = Conversion.capitalize(vehicle.getVehicleType());
                	name = Conversion.capitalize(vehicle.getName());
                }
                else {
                	Building building = (Building) unit;
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getNickName();
                }
//                d.setMaximumSize(new Dimension(F_WIDTH, D_HEIGHT));
//    			d.setPreferredSize(new Dimension(F_WIDTH, D_HEIGHT));
				d.setSize(WIDTH_1, HEIGHT_1); 
//			    d.setSize(350, 300); // undecorated 301, 348 ; decorated : 303, 373
				
		        d.setResizable(false);
	        
			    UnitInfoPanel b = new UnitInfoPanel(desktop);
//		        b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		        
			    b.init(name, type, description);		
	           	b.setOpaque(false);
		        b.setBackground(new Color(0,0,0,128));
		        
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
				
                // Make panel drag-able
			    ComponentMover mover = new ComponentMover(d, desktop);//d.getContentPane());
			    mover.registerComponent(b);	
	
             }
        });
    }
     
    
    public void buildItemTwo(final Unit unit) {
        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	
	            if (unit instanceof Vehicle
	            		|| unit instanceof Person
	            		|| unit instanceof Robot) {
	            	desktop.openUnitWindow(unit, false);
	            }
	            
	            else {
                	Building building = (Building) unit;
	 
					final BuildingPanel buildingPanel = new BuildingPanel(true, "Building Detail", building, desktop);      
//		    		buildingPanel.setOpaque(false);
//	                buildingPanel.setBackground(new Color(0,0,0,150));
//	                buildingPanel.setTheme(true);
         		       
//	                final WebDialog<?> d = new WebDialog();//StyleId.dialogDecorated);
	                final WebInternalFrame d = new WebInternalFrame(StyleId.internalframe);
	                
	                d.setIconifiable(false);
	                d.setClosable(true);
	        		d.setFrameIcon(MainWindow.getLanderIcon());

	                d.add(buildingPanel);
	                
	    			d.setMaximumSize(new Dimension(WIDTH_2, HEIGHT_2));
	    			d.setPreferredSize(new Dimension(WIDTH_2, HEIGHT_2));
					d.setSize(WIDTH_2, HEIGHT_2); // undecorated: 300, 335; decorated: 310, 370
					d.setLayout(new FlowLayout()); 
	
	            	// Make the buildingPanel to appear at the mouse cursor
	                Point location = MouseInfo.getPointerInfo().getLocation();
	                d.setLocation(location); 
	                
					// Create compound border
//					Border border = new MarsPanelBorder();
//					Border margin = new EmptyBorder(5,5,5,5);
//					d.getRootPane().setBorder(new CompoundBorder(border, margin));//BorderFactory.createLineBorder(Color.orange));
	
//				    d.addWindowFocusListener(new WindowFocusListener() {            
//						public void windowLostFocus(WindowEvent e) {
//					    	//JWindow w = (JWindow) e.getSource();
//					    	d.dispose();
//					    	//w.dispose();
//						}            
//						public void windowGainedFocus(WindowEvent e) {
//						}
//					});
				    
	                // Make panel drag-able
//	        		ComponentMover mover = new ComponentMover();
//	        		mover.registerComponent(d);
	                
	                desktop.add(d);
	                
					d.setVisible(true);
	            }
	         }
	    });
 
    }
    
	public void buildItemThree(final Unit unit) {
	        itemThree.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	//if (unit instanceof Vehicle) {
		            Vehicle vehicle = (Vehicle) unit;
		            vehicle.relocateVehicle();
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
