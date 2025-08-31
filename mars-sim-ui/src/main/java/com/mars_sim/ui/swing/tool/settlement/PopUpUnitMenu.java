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
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
import com.mars_sim.ui.swing.utils.SwingHelper;


public class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH_0 = 350;

	public static final int WIDTH_1 = WIDTH_0;
	public static final int HEIGHT_1 = 300;

	public static final int WIDTH_2 = UnitWindow.WIDTH - 130;
	public static final int HEIGHT_2 = UnitWindow.HEIGHT - 70;

    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
		add(unit.getUnitType().getName() + " : " + unit.getName());
		addSeparator();
    	MainDesktopPane desktop = swindow.getDesktop();
    	
    	switch (unit) {
			case Person p:
        		add(buildDetailsItem(p, desktop));
				break;
        	
			case Vehicle v: 
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				add(buildVehicleRelocate(v));
				add(buildVehicleToMaintain(v));
				break;

        	case Building b:
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				add(buildSalvageItem(b));
				break;

        	// Note: for construction sites
			case ConstructionSite cs:
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				if (cs.isUnstarted()) {
					add(relocateSite(cs));
				}
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
		        
		        JDialog d = SwingHelper.createPopupWindow(b, WIDTH_1, HEIGHT_1, 0, 0);

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
     * Salvage building
     *
     * @param unit
     * @param mainDesktopPane
     */
    private JMenuItem buildSalvageItem(final Building b) {
		JMenuItem detailsItem = new JMenuItem("Demolish");

        detailsItem.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(null,
             				"Confirm the demolition of " + b.getName(), "Confirm demolish",
			 				JOptionPane.YES_NO_OPTION)
			 		== JOptionPane.OK_OPTION) {
					b.getAssociatedSettlement().getConstructionManager().createNewSalvageConstructionSite(b);
			}
	    });

		return detailsItem;
    }
	
    /**
     * Builds item two.
     *
     * @param unit
     * @param mainDesktopPane
     */
    private JMenuItem buildDetailsItem(final Unit unit, final MainDesktopPane desktop) {
		JMenuItem detailsItem = new JMenuItem(Msg.getString("PopUpUnitMenu.details"));

        detailsItem.addActionListener(e -> desktop.showDetails(unit));

		return detailsItem;
    }
    
    /**
     * Builds item for vehicle relocation.
     *
     * @param unit
     */
	private JMenuItem buildVehicleRelocate(Vehicle v) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu.relocate"));

        relocateItem.addActionListener(e -> {
	            v.relocateVehicle();
	    		repaint();
        });

		return relocateItem;
	}
	
    /**
     * Builds item for maintenance tagging.
     *
     * @param unit
     */
	private JMenuItem buildVehicleToMaintain(Vehicle v) {
		JMenuItem item = new JMenuItem(Msg.getString("PopUpUnitMenu.maintain"));

		item.addActionListener(e -> {
	            v.maintainVehicle();
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
}
