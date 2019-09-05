/**
 * Mars Simulation Project
 * PopUpMenu.java
 * @version 3.1.0 2018-07-23
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.navigator;


import java.awt.Color;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ComponentMover;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.UnitInfoPanel;


// TODO: is extending to JInternalFrame better?
public class PopUpMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;

	private JMenuItem itemOne;
    private Unit unit;
    private Settlement settlement;
	private MainDesktopPane desktop;

    public PopUpMenu(final SettlementWindow swindow, final Unit unit){
    	this.unit = unit;
    	this.settlement = swindow.getMapPanel().getSettlement();
        this.desktop = (MainDesktopPane) swindow.getDesktop();

        UIResource res = new BorderUIResource.LineBorderUIResource(Color.orange);
        UIManager.put("PopupMenu.border", res);

        //force to the Heavyweight Component or able for AWT Components
        this.setLightWeightPopupEnabled(false);

    	itemOne = new JMenuItem(Msg.getString("PopUpUnitMenu.itemOne"));
        itemOne.setForeground(new Color(139,69,19));

       	add(itemOne);

       	buildItemOne(unit);
    
//     // Determine what the GraphicsDevice can support.
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
            	setOpaque(false);
            	JFrame f = new JFrame();
            	f.setAlwaysOnTop (true);
            	f.setFocusable(true);
            	//JInternalFrame d = new JInternalFrame();
            	//final JDialog d = new JDialog();
                f.setForeground(Color.YELLOW); // orange font
                f.setFont( new Font("Arial", Font.BOLD, 14 ) );

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

                double num = description.length() * 1.3D + 130D;
			    if (num > 450)
			    	num = 450;
                int frameHeight = (int) num;

			    f.setSize(350, frameHeight); // undecorated 301, 348 ; decorated : 303, 373
		        f.setResizable(false);
		        f.setUndecorated(true);
//		        f.setBackground(new Color(0,0,0,128)); // not working for decorated jframe

			    UnitInfoPanel b = new UnitInfoPanel(desktop);
			    b.init(name, type, description);

			    f.add(b);

			    ComponentMover mover = new ComponentMover(f, b, f.getContentPane());
			    mover.registerComponent(b);


            	// Make the buildingPanel to appear at the mouse cursor
                Point location = MouseInfo.getPointerInfo().getLocation();
                f.setLocation(location);

                f.setVisible(true);
				f.addWindowFocusListener(new WindowFocusListener() {
				    public void windowLostFocus(WindowEvent e) {
				    	if (!mover.isMousePressed())
				    		f.dispose();
				    }
				    public void windowGainedFocus(WindowEvent e) {
				    	//f.setVisible(true);
				    }
				});

             }
        });
    }

	public void destroy() {
		settlement = null;
		settlement.destroy();
		unit = null;
		unit.destroy();
		itemOne = null;
	}
}