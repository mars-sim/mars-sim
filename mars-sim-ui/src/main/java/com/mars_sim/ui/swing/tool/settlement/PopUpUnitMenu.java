/*
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @date 2021-11-28
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
import com.mars_sim.ui.swing.utils.SwingHelper;


public class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(PopUpUnitMenu.class.getName());
	
	public static final int WIDTH_0 = 350;

	public static final int WIDTH_1 = WIDTH_0;
	public static final int HEIGHT_1 = 300;

	public static final int WIDTH_2 = UnitWindow.WIDTH - 130;
	public static final int HEIGHT_2 = UnitWindow.HEIGHT - 70;

    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
		add(unit.getUnitType().getName() + " : " + unit.getName());
		addSeparator();
    	MainDesktopPane desktop = swindow.getDesktop();
    	
    	switch (unit.getUnitType()) {
			case PERSON:
        		add(buildDetailsItem(unit, desktop));
				break;
        	
			case VEHICLE: 
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				add(buildVehicleRelocate(unit));
				add(buildVehicleToMaintain(unit));
				break;

        	case BUILDING:
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				break;

        	// Note: for construction sites
			case CONSTRUCTION:
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				add(relocateSite((ConstructionSite)unit));
				add(rotateSite((ConstructionSite)unit));
				break;

			default:
				add(buildDetailsItem(unit, desktop));
				break;
        }
    }


    /**
     * Builds item one.
     *
     * @param unit
     */
    private JMenuItem buildDescriptionitem(final Unit unit, final MainDesktopPane desktop) {
        
		JMenuItem descriptionItem = new JMenuItem(Msg.getString("PopUpUnitMenu.description"));

        descriptionItem.addActionListener(e -> {

                String description = null;
                String type = null;
                String name = null;

                if (unit.getUnitType() == UnitType.VEHICLE) {
                	Vehicle vehicle = (Vehicle) unit;
                	description = vehicle.getDescription();
                	type = vehicle.getVehicleType().getName();
                	name = vehicle.getName();
                }
                else if (unit.getUnitType() == UnitType.BUILDING) {
                	Building building = (Building) unit;
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getName();
                }
                else if (unit.getUnitType() == UnitType.CONSTRUCTION) {
                	ConstructionSite site = (ConstructionSite) unit;
					var stageInfo = site.getCurrentConstructionStage().getInfo();
                	description = stageInfo.getName();
                	type = stageInfo.getType().name().toLowerCase();
                	name = site.getName();
                }
                else
                	return;

				UnitInfoPanel b = new UnitInfoPanel(desktop);

			    b.init(name, type, description);
	           	b.setOpaque(false);
		        b.setBackground(new Color(0,0,0,128));

				final JDialog d = SwingHelper.createPopupWindow(b, WIDTH_1, HEIGHT_1, 0, 0);

				d.setForeground(Color.WHITE); // orange font
				d.setFont(new Font("Arial", Font.BOLD, 14));

            	d.setOpacity(0.75f);
		        d.setBackground(new Color(0,0,0,128));
                d.setVisible(true);
             }
        );

		return descriptionItem;
    }

	
    /**
     * Builds item two.
     *
     * @param unit
     * @param mainDesktopPane
     */
    private JMenuItem buildDetailsItem(final Unit unit, final MainDesktopPane desktop) {
		JMenuItem detailsItem = new JMenuItem(Msg.getString("PopUpUnitMenu.details"));

        detailsItem.addActionListener(e -> {
			desktop.showDetails(unit);
	    });

		return detailsItem;
    }
    
    /**
     * Builds item for vehicle relocation.
     *
     * @param unit
     */
	private JMenuItem buildVehicleRelocate(Unit unit) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu.relocate"));

        relocateItem.addActionListener(e -> {
	            ((Vehicle) unit).relocateVehicle();
	    		repaint();
        });

		return relocateItem;
	}
	
    /**
     * Builds item for maintenance tagging.
     *
     * @param unit
     */
	private JMenuItem buildVehicleToMaintain(Unit unit) {
		JMenuItem item = new JMenuItem(Msg.getString("PopUpUnitMenu.maintain"));

		item.addActionListener(e -> {
	            ((Vehicle) unit).maintainVehicle();
	    		repaint();
        });

		return item;
	}
	
    /**
     * Builds item for relocating a construction site.
     *
     * @param unit
     */
	private JMenuItem relocateSite(ConstructionSite site) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu.relocate"));
		
        relocateItem.setForeground(new Color(139,69,19));
        relocateItem.addActionListener(e -> {
        		site.relocateSite();
	    		repaint();
        });

		return relocateItem;
	}
	
	/**
     * Builds item five.
     *
     * @param unit
     */
	private JMenuItem rotateSite(ConstructionSite site) {
		JMenuItem rotateItem = new JMenuItem(Msg.getString("PopUpUnitMenu.rotate"));

		rotateItem.setForeground(new Color(139,69,19));
		rotateItem.addActionListener(e -> {
			int siteAngle = (int) site.getFacing();
			siteAngle += 90;
			if (siteAngle >= 360)
				siteAngle = 0;
			//site.setFacing(siteAngle);
			logger.info(site, "Just set facing to " + siteAngle + ".");
			repaint();
        });

		return rotateItem;
	}
}
