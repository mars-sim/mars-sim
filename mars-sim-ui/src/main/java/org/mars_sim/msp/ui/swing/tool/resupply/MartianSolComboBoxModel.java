/**
 * Mars Simulation Project
 * MartianSolComboBoxModel.java
 * @version 3.1.0 2017-02-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.DefaultComboBoxModel;

import org.mars_sim.msp.core.time.MarsClock;

/**
 * A combo box model for Martian sols.
 */
public class MartianSolComboBoxModel
extends DefaultComboBoxModel<Integer> {

	/** Data members. */
	private int maxSolNum;

	/**
	 * Constructor.
	 * @param month {@link Integer} the Martian month number.
	 * @param orbit {@link Integer} the Martian orbit number.
	 */
	public MartianSolComboBoxModel(int month, int orbit) {
		maxSolNum = MarsClock.getSolsInMonth(month, orbit);

		for (int x = 1; x <= maxSolNum; x++) {
			addElement(x);
		}
	}

	/**
	 * Update the items based on the number of sols in the month.
	 * @param month the Martian month number.
	 * @param orbit the Martian orbit number.
	 */
	public void updateSolNumber(int month, int orbit) {
		int newMaxSolNum = MarsClock.getSolsInMonth(month, orbit);
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