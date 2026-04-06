/*
 * Mars Simulation Project
 * PopUpUnitMenu.java
 * @date 2021-11-28
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.mars_sim.core.Unit;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.events.ScheduledEventHandler;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.SwingHelper;


public class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	public static final int WIDTH_0 = 350;

	public static final int WIDTH_1 = WIDTH_0;
	public static final int HEIGHT_1 = 300;

    public PopUpUnitMenu(final Unit unit, UIContext context){
		add(unit.getUnitType().getName() + " : " + unit.getName());
		addSeparator();
    	
    	switch (unit) {
			case Person p:
        		add(buildDetailsItem(p, context));
				break;
        	
			case Vehicle v: 
				add(buildDescriptionitem(unit));
				add(buildDetailsItem(unit, context));
				add(createItem("relocate", v, Vehicle::relocateVehicle));
				add(createItem("maintain", v, Vehicle::maintainVehicle));
				break;

        	case Building b:
				add(buildDescriptionitem(unit));
				add(buildDetailsItem(unit, context));
				if (b.getAssociatedSettlement().getConstructionManager().canDemolish(b)) {
					add(createItem("demolish", b, this::triggerDemolish));
				}
				break;

        	// Note: for construction sites
			case ConstructionSite cs:
				add(buildDescriptionitem(unit));
				add(buildDetailsItem(unit, context));
				if (cs.isProposed()) {
					add(createItem("relocate", cs, t -> t.relocateSite()));
					add(createItem("delete", cs,
								 t -> t.getAssociatedSettlement().getConstructionManager().removeSite(t)));
				}
				break;

			default:
				add(buildDetailsItem(unit, context));
				break;
        }
    }

    /**
     * Builds item one.
     *
     * @param unit
     */
    private JMenuItem buildDescriptionitem(final Unit unit) {
        
		return createItem("description", unit, t -> {

            String description = null;
            String type = null;
            String name = null;

			switch (t) {
				case Vehicle vehicle -> {
                	description = vehicle.getDescription();
                	type = vehicle.getVehicleType().getName();
                	name = vehicle.getName();
                }
                case Building building -> {
                	description = building.getDescription();
                	type = building.getBuildingType();
                	name = building.getName();
                }
                case ConstructionSite site -> {
					var stageInfo = site.getCurrentConstructionStage().getInfo();
                	description = stageInfo.getName();
                	type = stageInfo.getType().name().toLowerCase();
                	name = site.getName();
                }
                default -> {
                	return;
				}
			}

			UnitInfoPanel b = new UnitInfoPanel(name, type, description);
			b.setOpaque(false);
			b.setBackground(new Color(0,0,0,128));
			
			JDialog d = SwingHelper.createPopupWindow(b, WIDTH_1, HEIGHT_1, 0, 0);

			d.setForeground(Color.WHITE); // orange font
			d.setFont(new Font("Arial", Font.BOLD, 14));

			d.setOpacity(0.75f);
			d.setBackground(new Color(0,0,0,128));
			d.setVisible(true);
		});
    }

	
	/**
	 * Class to operatino the demolish of a Building async to avoid the removal causing a problem with 
	 * the active simulation logic.
	 */
	@SuppressWarnings("serial")
	private class DemolishHandler implements ScheduledEventHandler {
		private Building b;

		public DemolishHandler(Building b) {
			this.b = b;
		}

		@Override
		public String getEventDescription() {
			return "Start demolishing of " + b.getName();
		}

		@Override
		public int execute(MarsTime currentTime) {
			b.getAssociatedSettlement().getConstructionManager().createNewSalvageConstructionSite(b);
			return 0;
		}
	}

	private void triggerDemolish(Building b) {
		if (JOptionPane.showConfirmDialog(null,
						"Confirm the demolition of " + b.getName(), "Confirm demolish",
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
			var fm = b.getAssociatedSettlement().getFutureManager();

			var handler = new DemolishHandler(b);
			fm.addEvent(1, handler);
		}
	}

    /**
     * Builds item two.
     *
     * @param unit
     * @param mainDesktopPane
     */
    private JMenuItem buildDetailsItem(final Unit unit, final UIContext context) {
		return createItem("details", unit, context::showDetails);
    }
 
	/**
     * Create a menu item
     *
     * @param unit
     */
	private <T> JMenuItem createItem(String name, T target, Consumer<T> action) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu." + name));
		
        relocateItem.addActionListener(e -> {
			action.accept(target);
			repaint();
        });

		return relocateItem;
	}
}
