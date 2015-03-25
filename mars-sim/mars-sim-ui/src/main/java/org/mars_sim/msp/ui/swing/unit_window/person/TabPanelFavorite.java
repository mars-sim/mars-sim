/**
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @version 3.07 2015-02-27 

 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.text.WordUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelFavorite is a tab panel for general information about a person.
 */
public class TabPanelFavorite
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelFavorite(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelFavorite.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelFavorite.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		Person person = (Person) unit;

		// Create Favorite label panel.
		JPanel favoriteLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(favoriteLabelPanel);

		// Prepare  Favorite label
		JLabel favoriteLabel = new JLabel(Msg.getString("TabPanelFavorite.label"), JLabel.CENTER); //$NON-NLS-1$
		favoriteLabelPanel.add(favoriteLabel);

		// Prepare info panel.
		JPanel infoPanel = new JPanel(new GridLayout(4, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare main dish name label
		JLabel mainDishNameLabel = new JLabel(Msg.getString("TabPanelFavorite.mainDish"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(mainDishNameLabel);

		// Prepare main dish label
		String mainDish = person.getFavorite().getFavoriteMainDish();
		JLabel mainDishLabel = new JLabel(mainDish, JLabel.RIGHT);
		infoPanel.add(mainDishLabel);

		// Prepare side dish name label
		JLabel sideDishNameLabel = new JLabel(Msg.getString("TabPanelFavorite.sideDish"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(sideDishNameLabel);

		// Prepare side dish label
		String sideDish = person.getFavorite().getFavoriteSideDish();
		JLabel sideDishLabel = new JLabel(sideDish, JLabel.RIGHT);
		infoPanel.add(sideDishLabel);

		// Prepare dessert name label
		JLabel dessertNameLabel = new JLabel(Msg.getString("TabPanelFavorite.dessert"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(dessertNameLabel);

		// Prepare dessert label
		String dessert = person.getFavorite().getFavoriteDessert();
		JLabel dessertLabel = new JLabel(WordUtils.capitalize(dessert), JLabel.RIGHT);
		infoPanel.add(dessertLabel);
		
		// Prepare activity name label
		JLabel activityNameLabel = new JLabel(Msg.getString("TabPanelFavorite.activity"), JLabel.LEFT); //$NON-NLS-1$
		infoPanel.add(activityNameLabel);

		// Prepare activity label
		String activity = person.getFavorite().getFavoriteActivity();
		JLabel activityLabel = new JLabel(WordUtils.capitalize(activity), JLabel.RIGHT);
		infoPanel.add(activityLabel);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}