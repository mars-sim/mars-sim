/*
 * Mars Simulation Project
 * GeneralTabPanel.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.authority;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.svg.SVGIcon;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The tab displays the general properties of an Authority.
 */
@SuppressWarnings("serial")
class TabPanelGeneral extends EntityTabPanel<Authority> {
	
	private static final int EMBLEM_WIDTH = 200;
	private static final String AGENCY_FOLDER = "agency/";
	private static final String TAB_ICON = "info";
	
	/**
	 * Constructor.
	 * 
	 * @param ra the Authority.
	 * @param context the UI context.
	 */
	public TabPanelGeneral(Authority ra, UIContext context) {
		super(
			Msg.getString("EntityGeneral.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(TAB_ICON),
			null,
			context, ra
		);
	}

	@Override
	protected void buildUI(JPanel content) {

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		content.add(mainPanel, BorderLayout.CENTER);
		var ra = getEntity();		
		String agencyShortName = ra.getName();

		// Agency emblem
		JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		iconPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(iconPanel);

		Icon icon = ImageLoader.getIconByName(AGENCY_FOLDER + agencyShortName);
		JLabel agencyLabel = null;
		if (icon instanceof SVGIcon) {
			agencyLabel = new JLabel(icon);
		}
		else {
			Image img = (ImageLoader.getImage(AGENCY_FOLDER + agencyShortName));
			if (img != null) {
				int originalWidth = img.getWidth(null);
				int originalHeight = img.getHeight(null);
				int newWidth = EMBLEM_WIDTH; // Desired width
				int newHeight = (int) ((double) originalHeight / originalWidth * newWidth);
				
				Image newImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
				agencyLabel = new JLabel(new ImageIcon(newImg));
			}
			else {
				agencyLabel = new JLabel();
			}
		}
		iconPanel.add(agencyLabel);
				
		// Base details
		var attrPanel = new AttributePanel();
		attrPanel.setBorder(SwingHelper.createLabelBorder("Details"));
		mainPanel.add(attrPanel);

		attrPanel.addTextField("Code", agencyShortName, null);
		attrPanel.addTextField(Msg.getString("Entity.name"), ra.getDescription(), null);
		attrPanel.addTextField("Corporation", Boolean.toString(ra.isCorporation()), null);

		// Country names
		String cText = String.join(", ", ra.getCountries());
		var countryList = SwingHelper.createTextBlock("Countries Supported", cText);
		mainPanel.add(countryList);
	}
}