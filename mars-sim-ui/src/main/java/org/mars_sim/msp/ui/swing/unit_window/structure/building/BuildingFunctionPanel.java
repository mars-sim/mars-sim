/**
 * Mars Simulation Project
 * BuildingFunctionPanel.java
 * @date 2021-10-06
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * The BuildingFunctionPanel class is a panel representing a function for a
 * settlement building.
 */
@SuppressWarnings("serial")
public abstract class BuildingFunctionPanel extends TabPanel {

	/** The building this panel is for. */
	protected Building building;

	private boolean isUIDone = false;
	
	/**
	 * Constructor.
	 * 
	 * @param building The building this panel is for.
	 * @param desktop  The main desktop.
	 */
	public BuildingFunctionPanel(String title, Building building, MainDesktopPane desktop) {
		// User JPanel constructor
		super(title, null, title, building, desktop);

		// Initialize data members
		this.building = building;
	}

	@Override
	public boolean isUIDone() {
		return isUIDone;
	}
	
	@Override
	public void initializeUI() {
		if (!isUIDone) {
			// Create label in top panel
//			WebLabel titleLabel = new WebLabel(getTabTitle(), WebLabel.CENTER);
//			titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
//			topContentPanel.add(titleLabel);

			buildUI(centerContentPanel);
			
			isUIDone = true;
		}	
	}
	
	/**
	 * Build the UI element using the 3 components.
	 * @param centerContentPanel
	 */
	protected void buildUI(JPanel centerContentPanel) {
		// TODO remove this once all building panels migrated
		throw new UnsupportedOperationException("No build UI logic defined");
	}

	/**
	 * Update this panel.
	 */
	@Override
	public void update() {
		// Nothing to update by default
	}

	/**
	 * Add a text field and label to a Panel. The layout should be Spring layout.
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field as an integer
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, String label, int content, String tooltip) {
		return addTextField(parent, label, Integer.toString(content), tooltip);
	}
	
	/**
	 * Add a text field and label to a Panel. The layout should be Spring layout.
	 * @param parent Parent panel
	 * @param label The fixed label
	 * @param content Initial content of the text field
	 * @param tooltip Optional tooltip
	 * @return The JTextField that can be updated.
	 */
	protected JTextField addTextField(JPanel parent, String label, String content, String tooltip) {
		parent.add(new WebLabel(label, WebLabel.RIGHT));
						
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		JTextField typeTF = new JTextField();
		typeTF.setText(content);
		typeTF.setEditable(false);
		typeTF.setColumns(10);
		if (tooltip != null) {
			typeTF.setToolTipText(tooltip);
		}
		wrapper3.add(typeTF);
		parent.add(wrapper3);
		return typeTF;
	}
}
