/**
 * Mars Simulation Project
 * StormTrackingWindow.java
 * @version 3.08 2015-05-22
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * The window for tracking storms.
 */
public class StormTrackingWindow
extends JInternalFrame
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	private JPanel infoPane;

	public StormTrackingWindow(MainDesktopPane desktop) {
		// Use JInternalFrame constructor
        super("Storm Tracking Window", false, true, false, true);

		// Create info panel.
		infoPane = new JPanel(new CardLayout());
		infoPane.setBorder(new MarsPanelBorder());

		add(infoPane, BorderLayout.CENTER);

		setSize(new Dimension(400, 400));

		desktop.add(this);

		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    setVisible(true);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		//if (source == prevButton) buttonClickedPrev();
		//else if (source == nextButton) buttonClickedNext();
		//else if (source == finalButton) buttonClickedFinal();
	}


}
