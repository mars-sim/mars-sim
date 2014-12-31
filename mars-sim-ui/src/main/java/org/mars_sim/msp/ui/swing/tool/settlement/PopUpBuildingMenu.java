/**
 * Mars Simulation Project
 * PopUpBuildingMenu.java
 * @version 3.07 2014-12-31
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.peer.ComponentPeer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	//private Color THEME_COLOR = Color.ORANGE;
	
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
    }
       

    public void buildItemOne() {
    	
        itemOne.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e) {
            	setOpaque(false);
            	/*
                ((JComponent) d.getContentPane()).setOpaque(false);
                //setOpaque(false);
                //d.getContentPane();
                //WindowUtils.setWindowTransparent(d, true); // 
                */
    		   	// 2014-11-27 Added building.getDescription() for loading text
			    String description = building.getDescription();
			    final BuildingInfoDialog d = new BuildingInfoDialog(buildingName, description);
	
          /*
              	if (!"Mac OS X".equals(System.getProperty("os.name"))) {
              		d.setVisible(true);
            	}
                if (WindowUtils.isWindowAlphaSupported()) {
                    WindowUtils.setWindowAlpha(d, .2f);
                }
                */
			    //2014-11-27 Added ComponentMover Class
			    ComponentMover cm = new ComponentMover();
            	cm.registerComponent(d);
            	
            	// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                d.setLocation(location); 
				d.getRootPane().setBorder( BorderFactory.createLineBorder(Color.orange) );
				d.addWindowFocusListener(new WindowFocusListener() {            
				    public void windowLostFocus(WindowEvent e) {
				    	d.dispose();
				    }            
				    public void windowGainedFocus(WindowEvent e) {
				    }
				});	
             }
        });
    }
        
    
    public void buildItemTwo() {
        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setOpaque(false);
            	//final JWindow d = new JWindow();
            	final JDialog d = new JDialog();
            	
        		//2014-11-27 Added ComponentMover Class
    	        // Make panel drag-able
        		ComponentMover cm = new ComponentMover();
        		cm.registerComponent(d);
		
        		final BuildingPanel buildingPanel;
	    		buildingPanel = new BuildingPanel("Default", building, desktop);				
                buildingPanel.setOpaque(false);
                //WindowUtils.setWindowTransparent(buildingPanel, true);
	    		
	    		// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                d.setLocation(location);
			    d.add(buildingPanel);
				//dialog.setResizable(true);
				d.setSize(310,370);  // if undecorated, add 20 to height
				d.setLayout(new FlowLayout()); 
				d.setUndecorated(true);
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
    

    public static void setWindowAlpha(Window w, float alpha) {
        ComponentPeer peer = w.getPeer();
        if (peer == null) {
            return;
        }
        Class< ? extends ComponentPeer> peerClass = peer.getClass();

        //noinspection EmptyCatchBlock
        try {
            Class< ?> nativeClass = Class.forName("apple.awt.CWindow");
            if (nativeClass.isAssignableFrom(peerClass)) {
                Method setAlpha = nativeClass.getMethod(
                        "setAlpha", float.class);
                setAlpha.invoke(peer, Math.max(0.0f, Math.min(alpha, 1.0f)));
          }
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }
    
    /*
	 public void removeButtons(Component comp) {
	        if(comp instanceof AbstractButton) 
	            comp.getParent().remove(comp);
	        if (comp instanceof Container) {
	            Component[] comps = ((Container)comp).getComponents();
	            for(int x=0, y=comps.length; x<y; x++) {
	                removeButtons(comps[x]);
	            }
	        }
	}
	 */  
	public void destroy() {
			settlement.destroy();
			building.destroy();
			itemOne = null;
			itemTwo = null;			
	}
}