/*
 * Mars Simulation Project
 * MartianSolComboBoxModel.java
 * @date 2021-09-04
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.DefaultComboBoxModel;

import org.mars_sim.msp.core.time.MarsTimeFormat;

/**
 * A combo box model for Martian sols.
 */
@SuppressWarnings("serial")
public class MartianSolComboBoxModel
extends DefaultComboBoxModel<Integer> {

	/** Data members. */
	private int maxSolNum;

	// A standard month has 28 sols.
	// If the month number is divisible by 6, that month has 27 sols.
	// If that year is leap orbit and the month number is 24, that month has 28 sols.
	// An orbit has 24 months that can have either 27 or 28 Sols.

	/**
	 * Constructor.
	 * @param month {@link Integer} the Martian month number.
	 * @param orbit {@link Integer} the Martian orbit number.
	 */
	public MartianSolComboBoxModel(int month, int orbit) {
		maxSolNum = MarsTimeFormat.getSolsInMonth(month, orbit);
		for (int x = 1; x <= maxSolNum; x++) {
			addElement(x);
		}
	}

	/**
	 * Updates the items based on the number of sols in the month.
	 * 
	 * @param month the Martian month number.
	 * @param orbit the Martian orbit number.
	 */
	public void updateSolNumber(int month, int orbit) {
		int newMaxSolNum = MarsTimeFormat.getSolsInMonth(month, orbit);
		if (newMaxSolNum != maxSolNum) {
			int oldSelectedSol = (Integer) getSelectedItem();

			if (newMaxSolNum < maxSolNum) {
				removeElementAt(maxSolNum - 1);
				if (oldSelectedSol == maxSolNum) {
					setSelectedItem(newMaxSolNum);
				}
			}
			else {
				addElement(newMaxSolNum);
			}

			maxSolNum = newMaxSolNum;
		}
	}
}
