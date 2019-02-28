/**
 * Mars Simulation Project
 * MarqueeBanner.java
 * @version 3.1.0 2019-02-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/** @see http://stackoverflow.com/questions/3617326 */
public class MarqueeBanner extends JInternalFrame {

	private static final long serialVersionUID = -3982731599287212804L;
	private MainDesktopPane desktop;
	private String s;

	public MarqueeBanner(MainDesktopPane desktop) {
		super("Marquee Banner", false, true, false, true); //$NON-NLS-1$
		this.desktop = desktop;

	}

	void display() {
		JFrame f = new JFrame("MarqueeTest");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String s = "Tomorrow, and tomorrow, and tomorrow, " + "creeps in this petty pace from day to day, "
				+ "to the last syllable of recorded time; ... " + "It is a tale told by an idiot, full of "
				+ "sound and fury signifying nothing.";
		MarqueePanel mp = new MarqueePanel(s, 52);
		f.add(mp);
		f.pack();
		f.setLocationRelativeTo(desktop);
		f.setVisible(true);
		f.setAlwaysOnTop(true);
		mp.start();
	}

	public void centerLocation(JInternalFrame jif) {
		Dimension desktopSize = desktop.getSize();
		Dimension jInternalFrameSize = jif.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		jif.setLocation(width, height);
		jif.setVisible(true);
	}

	/** Side-scroll n characters of s. */
	@SuppressWarnings("serial")
	class MarqueePanel extends JPanel implements ActionListener {

		private static final int RATE = 12;
		private final Timer timer = new Timer(1000 / RATE, this);
		private final JLabel label = new JLabel();
		private final String s;
		private final int n;
		private int index;

		public MarqueePanel(String s, int n) {
			if (s == null || n < 1) {
				throw new IllegalArgumentException("Null string or n < 1");
			}
			StringBuilder sb = new StringBuilder(n);
			for (int i = 0; i < n; i++) {
				sb.append(' ');
			}
			this.s = sb + s + sb;
			this.n = n;
			label.setFont(new Font("Serif", Font.ITALIC, 26));
			label.setText(sb.toString());
			this.add(label);
		}

		public void start() {
			System.out.println("Marquee start()");
			timer.start();
		}

		public void stop() {
			timer.stop();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			index++;
			if (index > s.length() - n) {
				index = 0;
			}
			label.setText(s.substring(index, index + n));
		}
	}

	/**
	 * Sets the announcement text for the window.
	 * 
	 * @param announcement
	 *            the announcement text.
	 */
	public void setAnnouncement(String newText) {

		if (!newText.equals(""))
			s = newText;
	}
}