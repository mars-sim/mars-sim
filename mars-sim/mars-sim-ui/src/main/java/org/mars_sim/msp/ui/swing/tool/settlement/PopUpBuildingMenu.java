/**
 * Mars Simulation Project
 * PopUpBuildingMenu.java
 * @version 3.07 2014-11-27
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ComponentMover;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanel;

// TODO: is extending to JInternalFrame better?
public class PopUpBuildingMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	private JMenuItem itemOne, itemTwo;
    private Building building;
    private Settlement settlement;
	private MainDesktopPane desktop;
	private Color THEME_COLOR = Color.ORANGE;
	
    public PopUpBuildingMenu(final SettlementWindow swindow, final Building building){
    	this.building = building;
    	this.settlement = swindow.getMapPanel().getSettlement();
        this.desktop = swindow.getDesktop();
    	itemOne = new JMenuItem(Msg.getString("PopUpBuildingMenu.itemOne"));
        itemTwo = new JMenuItem(Msg.getString("PopUpBuildingMenu.itemTwo"));       
        add(itemOne);
        add(itemTwo);
        final String buildingName = building.getNickName();

        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        		final JDialog dialog = new JDialog();
        		//2014-11-27 Added ComponentMover Class
        		ComponentMover cm = new ComponentMover();
        		cm.registerComponent(dialog);
            	//final JInternalFrame dialog = new JInternalFrame();			
				JPanel panel = new JPanel();
				final BuildingPanel buildingPanel = new BuildingPanel("Default", building, desktop);				
				panel.add(buildingPanel);	
		        panel.setForeground(new Color(102, 51, 0)); // dark brown
				
                Point location = MouseInfo.getPointerInfo().getLocation();
                dialog.setLocation(location);
				dialog.add(panel);			
				//dialog.setResizable(true);
				dialog.setSize(280,320);  // if undecorated, add 20 to height
				dialog.setLayout(new FlowLayout()); 
				//dialog.setModal(false);
				dialog.setTitle(buildingName);
				//removeButtons(dialog);
				dialog.setUndecorated(true);
				dialog.getRootPane().setBorder(BorderFactory.createLineBorder(Color.orange) );
				dialog.setVisible(true);
				/*
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						//fireTableStructureChanged();
						buildingPanel.update();
			        	 dialog.repaint();
			        	 dialog.revalidate();
			        	 dialog.setVisible(true);
					}
				});
				*/
				Runnable r = new Runnable() {
			         public void run() {
			        	 //dialog.setVisible(false);
			        	 buildingPanel.update();
			        	 dialog.repaint();
			        	 dialog.revalidate();
			        	 dialog.setVisible(true);
			        	 try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			         }
			     };
			     new Thread(r).start();
			     dialog.addWindowFocusListener(new WindowFocusListener() {            
					    public void windowLostFocus(WindowEvent e) {
					    	//dialog.setVisible(false);
					    	dialog.dispose();
					    }            
					    public void windowGainedFocus(WindowEvent e) {
					    }
					});
            }
        });
        
        itemOne.addActionListener(new ActionListener() {
       	 
            public void actionPerformed(ActionEvent e) {
				//if (settlement != null) {
				//	desktop.openUnitWindow(settlement, false);
				//}	
            	final JDialog dialog = new JDialog();
            	//2014-11-27 Added ComponentMover Class
        		ComponentMover cm = new ComponentMover();
        		cm.registerComponent(dialog);
                Point location = MouseInfo.getPointerInfo().getLocation();
                dialog.setLocation(location);
            	JLabel dialogLabel = new JLabel(buildingName, JLabel.CENTER);
				dialogLabel.setOpaque(false);
				//dialog.setResizable(true);
			    dialogLabel.setFont(new Font("Serif", Font.ITALIC, 16));
			    dialog.setForeground(new Color(102, 51, 0)); // dark brown
			   	// 2014-11-27 Added building.getDescription() for loading text
			    String str = building.getDescription();
			    JTextArea ta = new JTextArea();
			   	//label2.setText("<html>"+ str +"</html>");
			   	ta.setOpaque(true);
			   	//ta.setForeground(Color.orange);
				ta.setFont(new Font("AvantGarde", Font.PLAIN, 14));
				//ta.setForeground(new Color(102, 51, 0)); // dark brown
			    ta.setText(str);
			    ta.setEditable(false);
			    ta.setOpaque(false);
			    ta.setLineWrap(true);
			    ta.setWrapStyleWord(true);
			    
			    JPanel panel = new JPanel(new BorderLayout());
			    panel.add(dialogLabel, BorderLayout.NORTH);
			    panel.add(ta, BorderLayout.CENTER);
			    panel.setPreferredSize(new Dimension(180, 300));
			    //JPanel panel = new JPanel(new GridLayout(2,1,0,0));
				panel.setOpaque(false);
		        //panel.setBorder(MainDesktopPane.newEmptyBorder());
		        //panel.setPreferredSize(new Dimension(120, 300));
				dialog.setBackground(THEME_COLOR);
		        setBorder(new MarsPanelBorder());
				dialog.add(panel);		
				dialog.setForeground(Color.orange);
				dialog.setSize(200,320); // panel size is 180,300
				dialog.setLayout(new FlowLayout()); 
				dialog.setModal(false);
				//removeButtons(dialog);
				dialog.setUndecorated(true);
				dialog.getRootPane().setBorder( BorderFactory.createLineBorder(Color.orange) );
				dialog.setVisible(true);
				dialog.addWindowFocusListener(new WindowFocusListener() {            
				    public void windowLostFocus(WindowEvent e) {
				    	//dialog.setVisible(false);
				    	dialog.dispose();
				    }            
				    public void windowGainedFocus(WindowEvent e) {
				    }
				});
           
            	
             }
        });
        
        //itemThree....
        
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
		}
}