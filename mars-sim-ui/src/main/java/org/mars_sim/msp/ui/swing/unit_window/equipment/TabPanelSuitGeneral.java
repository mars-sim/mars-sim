/*
 * Mars Simulation Project
 * TabPanelSuitGeneral.java
 * @date 2023-07-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.equipment;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * This tab displays general information about an EVA Suit.
 */
@SuppressWarnings("serial")
public class TabPanelSuitGeneral extends TabPanel {

	private static final String ID_ICON = "info"; //$NON-NLS-1$
	
	/** The suit instance. */
	private EVASuit suit;
	
	private JLabel registeredOwnerLabel;

	private JLabel containerUnitLabel;

	private JLabel topContainerUnitLabel;
	
	private String pOwnerCache;
	
	private String cUnitCache;
	
	private String topContainerUnitCache;
	
	/**
	 * Constructor.
	 * 
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSuitGeneral(EVASuit suit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSuitGeneral.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(ID_ICON),		
			Msg.getString("TabPanelSuitGeneral.title"), //$NON-NLS-1$
			desktop
		);

		this.suit = suit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel(3);
		
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare registered owner label
		pOwnerCache = "";
		Person p = suit.getRegisteredOwner();	
		if (p != null) {
			pOwnerCache = suit.getRegisteredOwner().getName();
		}		
		registeredOwnerLabel = infoPanel.addTextField(Msg.getString("TabPanelSuitGeneral.regOwner"), //$NON-NLS-1$
				pOwnerCache, null);
	
		// Prepare container unit label
		topContainerUnitCache = suit.getTopContainerUnit().getName();
		topContainerUnitLabel = infoPanel.addTextField(Msg.getString("TabPanelSuitGeneral.topContainerUnit"), //$NON-NLS-1$
				topContainerUnitCache, null);
		
		// Prepare container unit label
		cUnitCache = suit.getContainerUnit().getName();
		containerUnitLabel = infoPanel.addTextField(Msg.getString("TabPanelSuitGeneral.containerUnit"), //$NON-NLS-1$
				cUnitCache, null);
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		String pOwner = "";
		Person p = suit.getRegisteredOwner();	
		if (p != null) {
			pOwner = suit.getRegisteredOwner().getName();
		}
		if (!pOwnerCache.equalsIgnoreCase(pOwner)) {
			pOwnerCache = pOwner;
			registeredOwnerLabel.setText(pOwner); 
		}
			
		String topContainerUnit = suit.getTopContainerUnit().getName();
		if (!topContainerUnitCache.equalsIgnoreCase(topContainerUnit)) {
			topContainerUnitCache = topContainerUnit;
			topContainerUnitLabel.setText(topContainerUnit); 
		}
		
		String cUnit = suit.getContainerUnit().getName();
		if (!cUnitCache.equalsIgnoreCase(cUnit)) {
			cUnitCache = cUnit;
			containerUnitLabel.setText(cUnit); 
		}
	}
}
