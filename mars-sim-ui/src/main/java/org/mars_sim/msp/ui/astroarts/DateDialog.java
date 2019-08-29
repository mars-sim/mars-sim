/* Mars Simulation Project
 * DateDialog.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 * Original work by Osamu Ajiki and Ron Baalke (NASA/JPL)
 * http://www.astroarts.com/products/orbitviewer/
 * http://neo.jpl.nasa.gov/
 */

package org.mars_sim.msp.ui.astroarts;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.astroarts.ATime;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;


/**
*  Date Setting Dialog
*/
public class DateDialog extends ModalInternalFrame {
			
	protected JTextField		tfYear;
	protected JTextField		tfDate;
	protected JComboBox<String>		monthCB;
	
	protected JButton		buttonOk;
	protected JButton		buttonCancel;
	protected JButton		buttonToday;
	
	protected OrbitViewer	viewer;
	
	public DateDialog(OrbitViewer viewer, ATime atime) {
		super("Input Date");//, false, true, false, false);
        
		this.viewer = viewer;
		
		// Set the layout.
		//setLayout(new BorderLayout());//0, 0));
				
		// Layout
		setLayout(new GridLayout(2, 3, 4, 4));
		//JPanel panel = new JPanel(new FlowLayout());//GridLayout(2, 3, 0, 0));
		
		//setContentPane(panel);
		//setBorder(new EmptyBorder(2, 3, 2, 3));
		
        //add(panel, BorderLayout.CENTER);
		
		//setFont(new Font("Dialog", Font.PLAIN, 14));
		
		// Controls
		monthCB = new JComboBox<>();
		for (int i = 0; i < 12; i++) {
			monthCB.addItem(ATime.getMonthAbbr(i + 1));
		}
		monthCB.setSelectedIndex(atime.getMonth()-1);
		add(monthCB);
		
		Integer iDate = Integer.valueOf(atime.getDay());
		tfDate = new JTextField(iDate.toString(), 2);
		add(tfDate);
		
		Integer iYear = Integer.valueOf(atime.getYear());
		tfYear = new JTextField(iYear.toString(), 4);
		add(tfYear);
		
		buttonToday = new JButton("Today");
		add(buttonToday);
		
		ActionListener listener0 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
    				ATime atime = null;
    				JComponent source = (JComponent) evt.getSource();

    		        if (source == buttonToday) {
    					EarthClock clock = Simulation.instance().getMasterClock().getEarthClock();
    					monthCB.setSelectedIndex(clock.getMonth());
    					tfDate.setText(Integer.toString(clock.getDayOfMonth()));
    					tfYear.setText(Integer.toString(clock.getYear()));// + 1900));	    					
    					//return false;
    				}
    				//dispose();
    				viewer.endDateDialog(atime);
    				viewer.repaint();
    				//return true;
    			}
		};
		
		buttonToday.addActionListener(listener0);
			
		
		buttonOk = new JButton("OK");
		add(buttonOk);
		
		ActionListener listener1 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
    				ATime atime = null;
    				JComponent source = (JComponent) evt.getSource();

    		        if (source == buttonOk) {
    					int nYear = Integer.valueOf(tfYear.getText()).intValue();
    					int nMonth = monthCB.getSelectedIndex() + 1;
    					int nDate  = Integer.valueOf(tfDate.getText()).intValue();
    					if (1600 <= nYear && nYear <= 2199 &&
    								1 <= nMonth && nMonth <= 12 &&
    								1 <= nDate  && nDate  <= 31) {
    						atime = new ATime(nYear, nMonth, (double)nDate, 0.0);
    					}
    				
    				}
    				dispose();
    				viewer.endDateDialog(atime);
    				viewer.repaint();
    				//return true;
    			}
		};
		
		buttonOk.addActionListener(listener1);
			
		
		buttonCancel = new JButton("Cancel");
		add(buttonCancel);
		
		ActionListener listener2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
    				ATime atime = null;
    				JComponent source = (JComponent) evt.getSource();
    				if (source != buttonCancel) {
    					//return false;
    				}
    				dispose();
    				viewer.endDateDialog(atime);
    				//return true;
    			}
		};
		
		buttonCancel.addActionListener(listener2);
			
		viewer.getDesktop().add(this);
		
		Dimension desktopSize = viewer.getDesktop().getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

		setSize(new Dimension(250, 100));
		setPreferredSize(new Dimension(250, 100));		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);	

	    setVisible(true);      
	    
	    setModal(true);
	    viewer.repaint();
	    //validate();

	}
		
//	/**
//	 * Event Handler
//	 */
//    public boolean handleEvent(Event evt) {
//		if (evt.id == Event.ACTION_EVENT) {
//			ATime atime = null;
//			if (evt.target == buttonOk) {
//				int nYear = Integer.valueOf(tfYear.getText()).intValue();
//				int nMonth = monthCB.getSelectedIndex() + 1;
//				int nDate  = Integer.valueOf(tfDate.getText()).intValue();
//				if (1600 <= nYear && nYear <= 2199 &&
//							1 <= nMonth && nMonth <= 12 &&
//							1 <= nDate  && nDate  <= 31) {
//					atime = new ATime(nYear, nMonth, (double)nDate, 0.0);
//				}
//			} else if (evt.target == buttonToday) {
//				EarthClock clock = Simulation.instance().getMasterClock().getEarthClock();
//				monthCB.setSelectedIndex(clock.getMonth()-1);
//				tfDate.setText(Integer.toString(clock.getDayOfMonth()));
//				tfYear.setText(Integer.toString(clock.getYear()));// + 1900));
//				
//				return false;
//			} else if (evt.target != buttonCancel) {
//				return false;
//			}
//			dispose();
//			viewer.endDateDialog(atime);
//			return true;
//		}
//		return false;	// super.handleEvent(evt);
//	}

	}