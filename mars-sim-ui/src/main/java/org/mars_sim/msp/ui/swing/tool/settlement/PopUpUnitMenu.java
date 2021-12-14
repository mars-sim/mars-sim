/*
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @date 2021-11-28
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

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

	public static final int WIDTH_2 = UnitWindow.WIDTH - 130;
	public static final int HEIGHT_2 = UnitWindow.HEIGHT - 70;

	private static Map<Integer, WebInternalFrame> panels = new ConcurrentHashMap<>();

	private WebMenuItem itemOne, itemTwo, itemThree;
    private Unit unit;
    private Settlement settlement;
	private MainDesktopPane desktop;

    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
    	this.unit = unit;
    	desktop = swindow.getDesktop();
    	this.settlement = swindow.getMapPanel().getSettlement();

    	itemOne = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemOne"));
        itemTwo = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemTwo"));
        itemThree = new WebMenuItem(Msg.getString("PopUpUnitMenu.itemThree"));
        itemOne.setForeground(new Color(139,69,19));
        itemTwo.setForeground(new Color(139,69,19));
        itemThree.setForeground(new Color(139,69,19));

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
        // Note: for buildings and construction sites
        else { // if (unit.getUnitType() == UnitType.CONSTRUCTION) {
        	add(itemOne); // Description
        	add(itemTwo); // Details
//        	// Note : how to implement itemFour in such a way as to
        	// enable the use of arrow to move the construction site ?
        	buildItemOne(unit);
            buildItemTwo(unit);
//          // Implement buildItemFour(unit);
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
                else {
                	Building building = (Building) unit;
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getNickName();
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
    public void buildItemTwo(final Unit unit) {
        itemTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

	            if (unit.getUnitType() == UnitType.VEHICLE
	            		|| unit.getUnitType() == UnitType.PERSON
		            	|| unit.getUnitType() == UnitType.BUILDING	
	            		|| unit.getUnitType() == UnitType.ROBOT) {
	            	desktop.openUnitWindow(unit, false);
	            }
	         }
	    });

    }

    /**
     * Builds item three
     *
     * @param unit
     */
	public void buildItemThree(final Unit unit) {
        itemThree.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
	            Vehicle vehicle = (Vehicle) unit;
	            vehicle.relocateVehicle();
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
