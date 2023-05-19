/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2023-02-25
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.batik.gvt.GraphicsNode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.svg.SVGGraphicNodeIcon;
import org.mars_sim.msp.ui.swing.tool.svg.SVGMapUtil;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This tab shows the general details of the Vehicle type.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String ID_ICON = "info";
	
	private Vehicle v;

	/**
	 * Constructor.
	 */
	public TabPanelGeneral(Vehicle v, MainDesktopPane desktop) {
		super(
			Msg.getString("BuildingPanelGeneral.title"),
			ImageLoader.getIconByName(ID_ICON), 
			Msg.getString("BuildingPanelGeneral.title"),
			v, desktop);
		this.v = v;
	}

	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		// Add SVG Image loading for the building
		GraphicsNode svg = SVGMapUtil.getVehicleSVG(v.getBaseImage());
		SVGGraphicNodeIcon svgIcon = new SVGGraphicNodeIcon(svg, 128, 64, true);
		JLabel svgLabel = new JLabel(svgIcon);
		JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		svgPanel.add(svgLabel);
		topPanel.add(svgPanel, BorderLayout.NORTH);
		
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(8);
		topPanel.add(infoPanel, BorderLayout.CENTER);

		infoPanel.addTextField( "Type", v.getVehicleType().getName(), null);
		infoPanel.addTextField( "Specification", v.getSpecName(), null);
		infoPanel.addTextField( "Cargo Capacity", StyleManager.DECIMAL_KG.format(v.getCargoCapacity()), null);
		infoPanel.addTextField( "Base Mass", StyleManager.DECIMAL_KG.format(v.getBaseMass()), "The base mass of this vehicle");
		infoPanel.addTextField( "Fuel", ResourceUtil.findAmountResourceName(v.getFuelType()), null);
		infoPanel.addTextField( "Fuel Capacity", StyleManager.DECIMAL_KG.format(v.getFuelCapacity()), null);
		infoPanel.addTextField( "Base Range", StyleManager.DECIMAL_KM.format(v.getBaseRange()), null);
		infoPanel.addTextField( "Estimated Range", StyleManager.DECIMAL_KM.format(v.getRange()), null);
	}
}
