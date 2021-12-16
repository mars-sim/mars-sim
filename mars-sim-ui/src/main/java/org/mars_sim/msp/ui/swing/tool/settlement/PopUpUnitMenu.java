/*
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @date 2021-11-28
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.ConstructionSitesPanel;
import com.alee.laf.desktoppane.WebInternalFrame;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.window.WebDialog;
import com.alee.managers.style.StyleId;

public class PopUpUnitMenu extends WebPopupMenu {

	private static final long serialVersionUID = 1L;

	public static final int WIDTH_0 = 350;

	public static final int WIDTH_1 = WIDTH_0;
	public static final int HEIGHT_1 = 300;

	public static final int WIDTH_2 = UnitWindow.WIDTH - 130;
	public static final int HEIGHT_2 = UnitWindow.HEIGHT - 70;

	private static Map<Integer, WebInternalFrame> panels = new ConcurrentHashMap<>();

	private WebMenuItem itemOne, itemTwo, itemThree, itemFour;
    private Unit unit;
    private Settlement settlement;
	private MainDesktopPane desktop;

    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
    	this.unit = unit;
    	desktop = swindow.getDesktop();
    	this.settlement = swindow.getMapPanel().getSettlement();

    	// Description
    	itemOne = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemOne"));
    	// Details
        itemTwo = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemTwo"));
        // Relocate
        itemThree = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemThree"));
        // Manual
        itemFour = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemFour"));
        
        itemOne.setForeground(new Color(139,69,19));
        itemTwo.setForeground(new Color(139,69,19));
        itemThree.setForeground(new Color(139,69,19));
        itemFour.setForeground(new Color(139,69,19));
        
        if (unit.getUnitType() == UnitType.PERSON) {
        	add(itemTwo); // Details
        	buildItemTwo(unit);
        }

        else if (unit.getUnitType() == UnitType.VEHICLE) {
        	add(itemOne); // Description
        	add(itemTwo); // Details
        	add(itemThree); // Relocate
        	buildItemOne(unit);
            buildItemTwo(unit);
            buildItemThree(unit);
        }

        else if (unit.getUnitType() == UnitType.BUILDING) {
        	add(itemOne); // Description
        	add(itemTwo); // Details
        	buildItemOne(unit);
            buildItemTwo(unit);
        }
        // Note: for construction sites
        else { // if (unit.getUnitType() == UnitType.CONSTRUCTION) {
        	add(itemOne); // Description
        	add(itemTwo); // Details
        	add(itemFour); // Manual Rel
//        	// Note : how to implement itemFour in such a way as to
        	// enable the use of arrow to move the construction site ?
        	buildItemOne(unit);
            buildItemTwo(unit);
            buildItemFour(unit);
        }
        
     // Note: for JavaFX, determine what the GraphicsDevice can support.
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


    /**
     * Builds item one
     *
     * @param unit
     */
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

                if (unit.getUnitType() == UnitType.VEHICLE) {
                	Vehicle vehicle = (Vehicle) unit;
                	description = vehicle.getDescription(vehicle.getVehicleTypeString());
                	type = Conversion.capitalize(vehicle.getVehicleTypeString());
                	name = Conversion.capitalize(vehicle.getName());
                }
                else if (unit.getUnitType() == UnitType.BUILDING) {
                	Building building = (Building) unit;
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getNickName();
                }
                else {
                	ConstructionSite site = (ConstructionSite) unit;
                	description = site.getStageInfo().getName();
                	type = site.getStageInfo().getType();
                	name = site.getName();
                }
                
				d.setSize(WIDTH_1, HEIGHT_1);
		        d.setResizable(false);

			    UnitInfoPanel b = new UnitInfoPanel(desktop);

			    b.init(name, type, description);
	           	b.setOpaque(false);
		        b.setBackground(new Color(0,0,0,128));

			    d.add(b);

            	// Make it to appear at the mouse cursor
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


    /**
     * Builds item two
     *
     * @param unit
     */
    private void buildItemTwo(final Unit unit) {
        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	            if (unit.getUnitType() == UnitType.VEHICLE
	            		|| unit.getUnitType() == UnitType.PERSON
		            	|| unit.getUnitType() == UnitType.BUILDING	
	            		|| unit.getUnitType() == UnitType.ROBOT) {
	            	desktop.openUnitWindow(unit, false);
	            }
	            
	            // TODO Why is this not a dedicated class ?
	            else if (unit.getUnitType() == UnitType.CONSTRUCTION) {
	            	buildConstructionWindow();
	            }
	         }
	    });
    }

    private void buildConstructionWindow() {
    	int newID = unit.getIdentifier();

    	if (!panels.isEmpty()) {
        	Iterator<Integer> i = panels.keySet().iterator();
			while (i.hasNext()) {
				int oldID = i.next();
				WebInternalFrame f = panels.get(oldID);
        		if (newID == oldID && (f.isShowing() || f.isVisible())) {
        			f.dispose();
        			panels.remove(oldID);
        		}
        	}
    	}
    	
       	ConstructionSite site = (ConstructionSite) unit;

       	ConstructionManager manager = settlement.getConstructionManager();
       	
		final ConstructionSitesPanel sitePanel = new ConstructionSitesPanel(manager);

        WebInternalFrame d = new WebInternalFrame(StyleId.internalframe,
        		unit.getSettlement().getName() + " - " + site,
        		true,  //resizable
                false, //not closable
                true, //not maximizable
                false); //iconifiable);

        d.setIconifiable(false);
        d.setClosable(true);
		d.setFrameIcon(MainWindow.getLanderIcon());
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		WebPanel panel = new WebPanel(new BorderLayout(1, 1));
		panel.setBorder(new MarsPanelBorder());
		panel.setBorder(new EmptyBorder(1, 1, 1, 1));

		panel.add(sitePanel, BorderLayout.CENTER);

		d.add(panel);
		desktop.add(d);

		d.setMaximumSize(new Dimension(WIDTH_2, HEIGHT_2));
		d.setPreferredSize(new Dimension(WIDTH_2, HEIGHT_2));
		d.setSize(WIDTH_2, HEIGHT_2); // undecorated: 300, 335; decorated: 310, 370
		d.setLayout(new FlowLayout());

		// Create compound border
		Border border = new MarsPanelBorder();
		Border margin = new EmptyBorder(1,1,1,1);
		d.getRootPane().setBorder(new CompoundBorder(border, margin));

        // Save this panel into the map
        panels.put(site.getIdentifier(), d);

        d.setVisible(true);
    }
    
    /**
     * Builds item three
     *
     * @param unit
     */
	private void buildItemThree(final Unit unit) {
        itemThree.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	            Vehicle vehicle = (Vehicle) unit;
	            vehicle.relocateVehicle();
	    		repaint();
            }
        });
	}
	
    /**
     * Builds item four
     *
     * @param unit
     */
	public void buildItemFour(final Unit unit) {
        itemFour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	            ConstructionSite site = (ConstructionSite) unit;
	            site.relocate();
	    		repaint();
            }
        });
	}
	

	/**
	 * Sets the icon image for the main window.
	 */
	public Image getIconImage() {
		return MainWindow.getIconImage();
	}

	public void destroy() {
		panels.clear();
		panels = null;
		settlement = null;
		settlement.destroy();
		unit = null;
		unit.destroy();
		itemOne = null;
		itemTwo = null;
		itemThree = null;
	}

}
