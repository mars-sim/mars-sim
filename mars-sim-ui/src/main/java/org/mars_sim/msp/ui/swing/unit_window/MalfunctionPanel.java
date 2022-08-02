/*
 * Mars Simulation Project
 * MalfunctionPanel.java
 * @date 2022-08-01
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
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionRepairWork;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The MalfunctionPanel class displays info about a malfunction.
 */
@SuppressWarnings("serial")
public class MalfunctionPanel
extends WebPanel {

	private static final String REPAIR_WORK_REQUIRED = "Repair Work Required :";
	private static final String REPAIR_PARTS_NEEDED = "Repair Parts Needed : ";
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
	
	// Data members
	public int progressCache = 0;
	public String partsCache = "";
	public String workCache = "";
	
	/** The work label. */
	private WebLabel workLabel;
	/** The repair parts label. */
	private WebLabel partsLabel;
	/** The malfunction label. */
	private WebLabel malfunctionLabel;
	
	/** The repair bar model. */
	private BoundedRangeModel repairBarModel;
	
	/** The malfunction. */
	private Malfunction malfunction;
	
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
		setLayout(new GridLayout(4, 1, 0, 0));

		// Set border
		setBorder(new MarsPanelBorder());
		setOpaque(false);
		setBackground(new Color(0,0,0,128));

		// Prepare the malfunction label.
		malfunctionLabel = new WebLabel(malfunction.getName(), SwingConstants.CENTER);
		malfunctionLabel.setForeground(Color.red);
		add(malfunctionLabel);
		
		// Prepare name label.
		workLabel = new WebLabel("", SwingConstants.CENTER);
		workLabel.setForeground(Color.blue);
		add(workLabel);

		// Prepare repair pane.
		WebPanel repairPane = new WebPanel(new BorderLayout(0, 0));
		add(repairPane, BorderLayout.CENTER);

		// Prepare repair progress bar.
		JProgressBar repairBar = new JProgressBar();
		repairBarModel = repairBar.getModel();
		repairBar.setStringPainted(true);
		repairPane.add(repairBar, BorderLayout.CENTER);

		// Set initial value for repair progress bar.
		repairBarModel.setValue((int)malfunction.getPercentageFixed());

		// Prepare repair parts label.
		partsLabel = new WebLabel(getPartsString(), SwingConstants.CENTER);
		partsLabel.setPreferredSize(new Dimension(-1, -1));
		add(partsLabel);

		// Add tooltip.
		TooltipManager.setTooltip(this, MalfunctionPanel.getToolTipString(malfunction), TooltipWay.right);
	
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
		
		// Update parts label.
		String parts = getPartsString(malfunction.getRepairParts(), false);
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
		return getPartsString(parts, false).toString();
	}

	/**
	 * Gets the parts string.
	 * 
	 * @return string.
	 */
	public static String getPartsString(Map<Integer, Integer> parts, boolean useHtml) {

		StringBuilder buf = new StringBuilder(REPAIR_PARTS_NEEDED);
		if (parts.size() > 0) {
			Iterator<Integer> i = parts.keySet().iterator();
			while (i.hasNext()) {
				Integer part = i.next();
				int number = parts.get(part);
				if (useHtml)
					buf.append(BR);
				buf.append(number).append(ONE_SPACE)
						.append(Conversion.capitalize(ItemResourceUtil.findItemResource(part).getName()));
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

		result.append(MalfunctionPanel.getPartsString(malfunction.getRepairParts(), false));
		result.append(HTML_END);

		return result.toString();
	}
}
