/**
 * Mars Simulation Project
 * MarqueeWindow.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;




/**
 * The container window for holding the marquee news ticker.
 */
public class MarqueeWindow
extends JInternalFrame
implements InternalFrameListener, ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private JButton add;

	private Unit unit;

	private JPanel topPanel;


	public MarqueeWindow(MainDesktopPane desktop) {
		// Use JInternalFrame constructor
        super("Marquee News Ticker", false, false, false, true);

		addInternalFrameListener(this);

		setLayout(new BorderLayout());
        //topPanel = new JPanel(new BorderLayout());

        //add(topPanel);

		init();

		pack();

		desktop.add(this);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);//.DISPOSE_ON_CLOSE);

		setSize(new Dimension(1024, 50));

		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = 20; //(desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) ;
	    setLocation(width, height);

	    setVisible(true);

	    //System.out.println("done with setting up MarqueeWindow");
	}

	public void init() {

		//JPanel panel = new JPanel(new GridBagLayout());
		//panel.setLayout(new GridBagLayout());

		//MarqueeTicker marqueeTicker = new MarqueeTicker();
		//Component c = marqueeTicker.getDemoPanel();
		
		//add(c, BorderLayout.NORTH);
		//topPanel.add(c);
		//topPanel.add(panel, BorderLayout.NORTH);

	}

	public void addItem(JPanel p, JComponent c, int x, int y, int w, int h, int align) {

		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = w;
		gc.gridheight = h;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.insets = new Insets(2,2,2,2);
		gc.anchor = align;
		gc.fill = GridBagConstraints.NONE;
		p.add(c,gc);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		//if (source == prevButton) buttonClickedPrev();
		//else if (source == nextButton) buttonClickedNext();
		//else if (source == finalButton) buttonClickedFinal();
	}


	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameClosing(InternalFrameEvent e) {

		//System.out.println("internalFrameClosing()");
	}


	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		//System.out.println("internalFrameClosed()");
	}


	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		
		// Update if necessary.
	}

}
