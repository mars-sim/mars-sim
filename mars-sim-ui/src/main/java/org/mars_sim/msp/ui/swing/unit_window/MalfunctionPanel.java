/*
 * Mars Simulation Project
 * MalfunctionPanel.java
 * @date 2022-08-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.ui.swing.StyleManager;


/**
 * The MalfunctionPanel class displays info about a malfunction.
 */
@SuppressWarnings("serial")
public class MalfunctionPanel extends JPanel {

	private static final String REPAIR_WORK_REQUIRED = "Repair Work Required :";
	private static final String NONE = "None.";
	private static final String ONE_SPACE = " ";
	private static final String COMMA = ", ";
	private static final String DOT = ".";
	private static final String BR = "<br>";
	private static final String HTML_START = "<html>";
	private static final String REPAIR_TIME = " Repair Time: ";
	private static final String SLASH = " / ";
	private static final String MILLISOLS = " millisols";
	private static final String HTML_END = "</html>";
	
	private static final String REPAIR_PARTS_NEEDED = "Parts Needed:";
	
	// Data members
	public int progressCache = 0;
	public String partsCache = "";
	public String workCache = "";
	
	/** The work label. */
	private JLabel workLabel;
	/** The repair parts label. */
	private JLabel partsLabel;
	/** The malfunction label. */
	private JLabel malfunctionLabel;
	
	/** The repair bar model. */
	private BoundedRangeModel repairBarModel;
	
	/** The malfunction. */
	private Malfunction malfunction;
	
	/**
	 * Constructs a MalfunctionPanel object with a name prefix.
	 * 
	 * @param malfunction the malfunction to display
	 * @param building    the building the malfunction is in.
	 */
	public MalfunctionPanel(Malfunction malfunction, Building building) {

		// Call JPanel constructor.
		super();

		// Initialize data members.
		this.malfunction = malfunction;
		
		// Prepare the building label.
		if (building != null) {
			// Set layout and border.
			setLayout(new GridLayout(5, 1, 0, 0));
			JLabel buildingLabel = new JLabel(building.getName(), SwingConstants.LEFT);
			StyleManager.applySubHeading(buildingLabel);
			add(buildingLabel);
		}
		else {
			// Set layout and border.
			setLayout(new GridLayout(4, 1, 0, 0));
		}	
		
		setOpaque(false);
		setBackground(new Color(0,0,0,128));

		// Prepare the malfunction label.
		malfunctionLabel = new JLabel(malfunction.getName(), SwingConstants.CENTER);
		malfunctionLabel.setForeground(Color.red);
		add(malfunctionLabel);
		
		// Prepare name label.
		workLabel = new JLabel("", SwingConstants.CENTER);
		workLabel.setForeground(Color.blue);
		add(workLabel);

		// Prepare repair pane.
		JPanel repairPane = new JPanel(new BorderLayout(2, 2));
		add(repairPane, BorderLayout.CENTER);

		// Prepare repair progress bar.
		JProgressBar repairBar = new JProgressBar();
		repairBarModel = repairBar.getModel();
		repairBar.setStringPainted(true);
		repairPane.add(repairBar, BorderLayout.CENTER);

		// Set initial value for repair progress bar.
		repairBarModel.setValue((int)malfunction.getPercentageFixed());

		// Prepare repair parts label.
		partsLabel = new JLabel(getPartsString(), SwingConstants.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		add(partsLabel);

		// Add tooltip.
		setToolTipText(MalfunctionPanel.getToolTipString(malfunction));
	
		updateMalfunctionPanel();
	}

	/**
	 * Updates the malfunction panel.
	 */
	public void updateMalfunctionPanel() {
		
		String work = REPAIR_WORK_REQUIRED;
		String text = "";
		
		for (MalfunctionRepairWork workType : MalfunctionRepairWork.values()) {
			if (malfunction.getWorkTime(workType) > 0) {
				text += "  [" + workType + "]";
			}
		}
		
		if (!workCache.equalsIgnoreCase(text)) {
			workCache = text;
			workLabel.setText(work + text);
		}
		
		// Update progress bar.
		int percentComplete = (int)malfunction.getPercentageFixed();
		if (progressCache != percentComplete) {
			progressCache = percentComplete;
			repairBarModel.setValue(percentComplete);
		}
		
		// Update parts label.REPAIR_PARTS_NEEDED
		String parts = getPartsString(REPAIR_PARTS_NEEDED, malfunction.getRepairParts(), false);
		if (partsCache.equalsIgnoreCase(parts)) {
			partsCache = parts;
			partsLabel.setText(parts);
		}
		
	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	private String getPartsString() {
		Map<Integer, Integer> parts = malfunction.getRepairParts();
		return getPartsString(REPAIR_PARTS_NEEDED, parts, false).toString();
	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	public static String getPartsString(String title, Map<Integer, Integer> parts, boolean useHtml) {

		StringBuilder buf = new StringBuilder(title);
		if (parts.size() > 0) {
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int number = parts.get(part);
				if (useHtml)
					buf.append(BR);
				buf.append(number).append(ONE_SPACE)
						.append(ItemResourceUtil.findItemResource(part).getName());
				if (i.hasNext())
					buf.append(COMMA);
				else {
					buf.append(DOT);
					if (useHtml)
						buf.append(BR);
				}
			}
		} else
			buf.append(NONE);

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
	public static String getToolTipString(Malfunction malfunction) {
		StringBuilder result = new StringBuilder(HTML_START);
		result.append(malfunction.getName()).append(BR);
		for (MalfunctionRepairWork workType : MalfunctionRepairWork.values()) {
			if (malfunction.getWorkTime(workType) > 0) {
				result.append(workType.getName()).append(REPAIR_TIME)
					  .append((int) malfunction.getCompletedWorkTime(workType))
					  .append(SLASH)
					  .append((int) malfunction.getWorkTime(workType))
					  .append(MILLISOLS + BR);
			}
		}

		result.append(MalfunctionPanel.getPartsString(REPAIR_PARTS_NEEDED, malfunction.getRepairParts(), false));
		result.append(HTML_END);

		return result.toString();
	}
}
