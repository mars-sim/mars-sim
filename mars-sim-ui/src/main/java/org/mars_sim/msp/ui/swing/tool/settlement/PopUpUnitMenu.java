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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.structure.ConstructionSitesPanel;
import org.mars_sim.msp.ui.swing.utils.SwingHelper;


public class PopUpUnitMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	
	// default logger.
	private static final SimLogger logger = SimLogger.getLogger(PopUpUnitMenu.class.getName());
	
	public static final int WIDTH_0 = 350;

	public static final int WIDTH_1 = WIDTH_0;
	public static final int HEIGHT_1 = 300;

	public static final int WIDTH_2 = UnitWindow.WIDTH - 130;
	public static final int HEIGHT_2 = UnitWindow.HEIGHT - 70;

	private static Map<Integer, JInternalFrame> panels = new ConcurrentHashMap<>();

    public PopUpUnitMenu(final SettlementWindow swindow, final Unit unit){
    	MainDesktopPane desktop = swindow.getDesktop();

    	switch (unit.getUnitType()) {
			case PERSON:
        		add(buildDetailsItem(unit, desktop));
				break;
        	
			case VEHICLE: 
				add(buildDescriptionitem(unit, desktop));
				add(buildDetailsItem(unit, desktop));
				add(buildVehicleRelocate(unit));
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
				add(confirmSite((ConstructionSite)unit));
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

        descriptionItem.setForeground(new Color(139,69,19));
        descriptionItem.addActionListener(e -> {

	           	setOpaque(false);
		        setBackground(new Color(0,0,0,128));

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
                	name = building.getNickName();
                }
                else if (unit.getUnitType() == UnitType.CONSTRUCTION) {
                	ConstructionSite site = (ConstructionSite) unit;
                	description = site.getStageInfo().getName();
                	type = site.getStageInfo().getType();
                	name = site.getName();
                }
                else
                	return;

				UnitInfoPanel b = new UnitInfoPanel(desktop);

			    b.init(name, type, description);
	           	b.setOpaque(false);
		        b.setBackground(new Color(0,0,0,128));

				final JDialog d = SwingHelper.createPoupWindow(b, WIDTH_1, HEIGHT_1, 0, 0);

				d.setForeground(Color.WHITE); // orange font
                d.setFont(new Font("Arial", Font.BOLD, 14));

            	d.setOpacity(0.75f);
		        d.setBackground(new Color(0,0,0,128));
                d.setVisible(true);

                // Make panel drag-able
			    ComponentMover mover = new ComponentMover(d, desktop);
			    mover.registerComponent(b);

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

        detailsItem.setForeground(new Color(139,69,19));
        detailsItem.addActionListener(e -> {
	            if (unit.getUnitType() == UnitType.VEHICLE
	            		|| unit.getUnitType() == UnitType.PERSON
		            	|| unit.getUnitType() == UnitType.BUILDING	
	            		|| unit.getUnitType() == UnitType.ROBOT) {
	            	desktop.showDetails(unit);
	            }
	            
	            // TODO Why is this not a dedicated class ?
	            else if (unit.getUnitType() == UnitType.CONSTRUCTION) {
	            	buildConstructionWindow(unit, desktop);
	            }
	    });

		return detailsItem;
    }

    private void buildConstructionWindow(final Unit unit, final MainDesktopPane desktop) {
    	int newID = unit.getIdentifier();

    	if (!panels.isEmpty()) {
        	Iterator<Integer> i = panels.keySet().iterator();
			while (i.hasNext()) {
				int oldID = i.next();
				JInternalFrame f = panels.get(oldID);
        		if (newID == oldID && (f.isShowing() || f.isVisible())) {
        			f.dispose();
        			panels.remove(oldID);
        		}
        	}
    	}
    	
       	ConstructionSite site = (ConstructionSite) unit;

       	ConstructionManager manager = site.getAssociatedSettlement().getConstructionManager();
       	
		final ConstructionSitesPanel sitePanel = new ConstructionSitesPanel(manager);

        JInternalFrame d = new JInternalFrame(
        		unit.getSettlement().getName() + " - " + site,
        		true,  //resizable
                false, //not closable
                true, //not maximizable
                false); //iconifiable);

        d.setIconifiable(false);
        d.setClosable(true);
		d.setFrameIcon(MainWindow.getLanderIcon());
		d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout(1, 1));
		panel.setBorder(new MarsPanelBorder());
		panel.setBorder(new EmptyBorder(1, 1, 1, 1));

		panel.add(sitePanel, BorderLayout.CENTER);

		String phase = site.getPhase().getName();
		JLabel label = new JLabel("Mission Phase : " + phase, JLabel.CENTER);
		
		panel.add(label, BorderLayout.SOUTH);
		
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
	private JMenuItem buildVehicleRelocate(Unit unit) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu.relocate"));

        relocateItem.setForeground(new Color(139,69,19));
        relocateItem.addActionListener(e -> {
	            ((Vehicle) unit).relocateVehicle();
	    		repaint();
        });

		return relocateItem;
	}
	
    /**
     * Builds item four.
     *
     * @param unit
     */
	private JMenuItem relocateSite(ConstructionSite site) {
		JMenuItem relocateItem = new JMenuItem(Msg.getString("PopUpUnitMenu.relocate"));

		List<GroundVehicle> vehicles = site.getVehicles();
		
        relocateItem.setForeground(new Color(139,69,19));
        relocateItem.addActionListener(e -> {
        		site.relocateSite();
        		
        		if (vehicles != null && !vehicles.isEmpty()) {
	        		Coordinates coord = site.getCoordinates();
	        		for (Vehicle v: vehicles) {
	        			v.setCoordinates(coord);
	        		}
        		}
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
			site.setFacing(siteAngle);
			logger.info(site, "Just set facing to " + (int)Math.round(siteAngle) + ".");
			repaint();
        });

		return rotateItem;
	}
	
	/**
     * Builds item six.
     *
     * @param unit
     */
	private JMenuItem confirmSite(ConstructionSite site) {
		JMenuItem confirmItem = new JMenuItem(Msg.getString("PopUpUnitMenu.confirmSite"));

		confirmItem.setForeground(new Color(139,69,19));
		confirmItem.addActionListener(e -> {

			boolean isConfirm = site.isSitePicked();
			if (!isConfirm) {
				site.setSitePicked(!isConfirm);
//				String s = site.isSitePicked() + "";
//				s = s.toLowerCase();
//				s = Conversion.capitalize(s);
				logger.info(site, "Just confirmed the site location. Ready to go to the next phase.");
				repaint();
			}
			else {
				logger.info(site, "The site has already been confirmed at this point.");
			}
			
        });

		return confirmItem;
	}
	
	
	public void destroy() {
		panels.clear();
		panels = null;
	}

}
