/**
 * Mars Simulation Project
 * MalfunctionPanel.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoundedRangeModel;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;

/**
 * The MalfunctionPanel class displays info about a malfunction.
 */
public class MalfunctionPanel
extends WebPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The malfunction. */
	private Malfunction malfunction;
	/** The name label. */
	private WebLabel nameLabel;
	/** The repair bar model. */
	private BoundedRangeModel repairBarModel;
	/** The repair parts label. */
	private WebLabel partsLabel;

	/**
	 * Constructs a MalfunctionPanel object with a name prefix.
	 * @param malfunction the malfunction to display
	 */
	public MalfunctionPanel(Malfunction malfunction) {

		// Call JPanel constructor.
		super();

		// Initialize data members.
		this.malfunction = malfunction;

		// Set layout
		setLayout(new GridLayout(3, 1, 0, 0));

		// Set border
		setBorder(new MarsPanelBorder());
		setOpaque(false);
		setBackground(new Color(0,0,0,128));

		// Prepare name label.
		nameLabel = new WebLabel(malfunction.getName(), WebLabel.CENTER);
		if (malfunction.getCompletedEmergencyWorkTime() < malfunction.getEmergencyWorkTime()) {
			nameLabel.setText(malfunction.getName() + " - Emergency");
			nameLabel.setForeground(Color.red);
		}
		add(nameLabel);

		// Prepare repair pane.
		WebPanel repairPane = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		add(repairPane);

		// Prepare repair progress bar.
		WebProgressBar repairBar = new WebProgressBar();
		repairBarModel = repairBar.getModel();
		repairBar.setStringPainted(true);
		repairPane.add(repairBar);

		// Set initial value for repair progress bar.
		double totalRequiredWork = malfunction.getEmergencyWorkTime() + malfunction.getWorkTime()
				+ malfunction.getEVAWorkTime();
		double totalCompletedWork = malfunction.getCompletedEmergencyWorkTime() +
				malfunction.getCompletedWorkTime() + malfunction.getCompletedEVAWorkTime();
		int percentComplete = 0;
		if (totalRequiredWork > 0D) percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
		repairBarModel.setValue(percentComplete);

		// Prepare repair parts label.
		partsLabel = new WebLabel(getPartsString(), WebLabel.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		add(partsLabel);

		// Add tooltip.
		setToolTipText(getToolTipString());
	}

	/**
	 * Updates the panel's information.
	 */
	public void update() {

		// Update name label.
		int eva = 0;
		int emer = 0;
		if (malfunction.getCompletedEmergencyWorkTime() < malfunction.getEmergencyWorkTime()) {
			emer = 1;		
		}
		
		if (malfunction.getCompletedEVAWorkTime() < malfunction.getEVAWorkTime()) {
			eva = 1;
		}
		
		if (eva == 1 && emer == 1) {
			nameLabel.setText(malfunction.getName() + "- Emergency & EVA");
			nameLabel.setForeground(Color.red);
		}
		else if (eva == 1) {
			nameLabel.setText(malfunction.getName() + "- EVA");
			nameLabel.setForeground(Color.blue);
		}		
		else if (emer == 1) {
			nameLabel.setText(malfunction.getName() + "- Emergency");	
			nameLabel.setForeground(Color.red);
		}
		else {
			nameLabel.setText(malfunction.getName());
			nameLabel.setForeground(Color.black);
		}

		// Update progress bar.
		double totalRequiredWork = malfunction.getEmergencyWorkTime() + malfunction.getWorkTime()
				+ malfunction.getEVAWorkTime();
		double totalCompletedWork = malfunction.getCompletedEmergencyWorkTime() +
				malfunction.getCompletedWorkTime() + malfunction.getCompletedEVAWorkTime();
		int percentComplete = 0;
		
		if (totalRequiredWork > 0D) 
			percentComplete = (int) (100D * (totalCompletedWork / totalRequiredWork));
		
		if (percentComplete > 100)
			percentComplete = 100;
		
		repairBarModel.setValue(percentComplete);

		// Update parts label.
		partsLabel.setText(getPartsString());
	}

	/**
	 * Gets the parts string.
	 * @return string.
	 */
	private String getPartsString() {
		StringBuilder buf = new StringBuilder("Parts: ");

		Map<Integer, Integer> parts = malfunction.getRepairParts();
		if (parts.size() > 0) {
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer id = i.next();
    			Part p = ItemResourceUtil.findItemResource(id);
				int number = parts.get(id);
				buf.append(number).append(" ").append(p.getName());
				if (i.hasNext()) buf.append(", ");
			}
		}
		else buf.append("none");

		return buf.toString();
	}

	/**
	 * Gets the malfunction.
	 *
	 * @return malfunction
	 */
	public Malfunction getMalfunction() {
		return malfunction;
	}

	/**
	 * Creates multi-line tool tip text.
	 */
	private String getToolTipString() {
		StringBuilder result = new StringBuilder("<html>");
		result.append(malfunction.getName()).append("<br>");
		result.append("General repair time: ").append((int) malfunction.getWorkTime()).append(" milliols<br>");
		result.append("EVA repair time: ").append((int) malfunction.getEVAWorkTime()).append(" milliols<br>");
		result.append("Emergency repair time: ").append((int) malfunction.getEmergencyWorkTime()).append(" milliols<br>");
		result.append("Repair ").append(getPartsString().toLowerCase());
		result.append("</html>");

		return result.toString();
	}
}