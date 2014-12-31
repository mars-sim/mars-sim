/**
 * Mars Simulation Project
 * PopUpBuildingMenu.java
 * @version 3.07 2014-11-27
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.peer.ComponentPeer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;

import com.sun.jna.platform.WindowUtils;

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
 
            	final JDialog dialog = new JDialog();
              	if (!"Mac OS X".equals(System.getProperty("os.name"))) {
              		dialog.setVisible(true);
            	}
                if (WindowUtils.isWindowAlphaSupported()) {
                    WindowUtils.setWindowAlpha(dialog, .2f);
                }
            	//2014-11-27 Added ComponentMover Class
        		ComponentMover cm = new ComponentMover();
        		cm.registerComponent(dialog);
	    		// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                dialog.setLocation(location);
                
            	JLabel dialogLabel = new JLabel(buildingName, JLabel.CENTER);
			    dialogLabel.setFont(new Font("Serif", Font.ITALIC, 16));
			   	// 2014-11-27 Added building.getDescription() for loading text
			    String str = building.getDescription();
			    JTextArea ta = new JTextArea();

			   	ta.setOpaque(false);
				ta.setFont(new Font("AvantGarde", Font.PLAIN, 14));
				//ta.setForeground(new Color(102, 51, 0)); // dark brown
			    ta.setText(str);
			    ta.setEditable(false);
			    ta.setLineWrap(true);
			    ta.setWrapStyleWord(true);
			    
			    JPanel panel = new JPanel(new BorderLayout());
			    panel.add(dialogLabel, BorderLayout.NORTH);
			    panel.add(ta, BorderLayout.CENTER);
			    panel.setOpaque(false);
			    //panel.setBackground( new Color(255, 0, 0, 20) );
			    dialog.add(panel);			    
		        setBorder(new MarsPanelBorder());
				dialog.setSize(220,360);
				dialog.setLayout(new FlowLayout()); 
				dialog.setModal(false);
				//dialog.setUndecorated(false);
				dialog.getRootPane().setBorder( BorderFactory.createLineBorder(Color.orange) );
				dialog.setVisible(true);
				setOpaque(false);
				dialog.addWindowFocusListener(new WindowFocusListener() {            
				    public void windowLostFocus(WindowEvent e) {
				    	dialog.dispose();
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
        		final JDialog d = new JDialog();
             	if (!"Mac OS X".equals(System.getProperty("os.name"))) {
              		d.setVisible(true);
            	}
                if (WindowUtils.isWindowAlphaSupported()) {
                    WindowUtils.setWindowAlpha(d, .2f);
                }

        		//2014-11-27 Added ComponentMover Class
    	        // Make panel drag-able
        		ComponentMover cm = new ComponentMover();
        		cm.registerComponent(d);
		
        		final BuildingPanel buildingPanel;
	    		buildingPanel = new BuildingPanel("Default", building, desktop);				
	    		
	    		// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                d.setLocation(location);
			    d.add(buildingPanel);
				//dialog.setResizable(true);
				d.setSize(310,370);  // if undecorated, add 20 to height
				d.setLayout(new FlowLayout()); 
				//d.setUndecorated(true);
				d.setVisible(true);

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
	   
	public void destroy() {
			settlement.destroy();
			building.destroy();
			itemOne = null;
			itemTwo = null;			
	}
}