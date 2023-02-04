/*
 * Mars Simulation Project
 * DateDialog.java
 * @date 2022-07-10
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
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.astroarts.ATime;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;


/**
*  Date Setting Dialog
*/
@SuppressWarnings("serial")
public class DateDialog extends ModalInternalFrame {
			
	protected JTextField		tfYear;
	protected JTextField		tfDate;
	protected JComboBox<String>		monthCB;
	
	protected JButton		buttonOk;
	protected JButton		buttonCancel;

	protected JButton		buttonSimDate;
	protected JButton		buttonToday;
	protected JButton		button1986;
	
	protected OrbitViewer	viewer;
	
	public DateDialog(OrbitViewer viewer, ATime atime) {
		super("Input Date", false, // resizable
				false, // closable
				false, // maximizable
				false); // iconifiable
				
//		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		this.viewer = viewer;
		
		// Set the layout.
		//setLayout(new BorderLayout());//0, 0));
		setLayout(new GridLayout(2, 4, 4, 4));
		
		super.setFrameIcon(MainWindow.getLanderIcon());

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
	
		///////////////////////////////////////
		
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
			
		//////////////////////////////////////////////////////////
		
		buttonSimDate = new JButton("Sim Date");
		add(buttonSimDate);	
		buttonSimDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				ATime atime = null;
//				JComponent source = (JComponent) e.getSource();
//
//		        if (source == buttonSimDate) {
					EarthClock clock = Simulation.instance().getMasterClock().getEarthClock();
					monthCB.setSelectedIndex(clock.getMonth() - 1);
					tfDate.setText(Integer.toString(clock.getDayOfMonth()));
					tfYear.setText(Integer.toString(clock.getYear()));    					
//				}
//				dispose();
//				viewer.endDateDialog(atime);
				viewer.repaint();
			};
		});
		
		//////////////////////////////////////////////////////////
		
		buttonToday = new JButton("Today");
		add(buttonToday);	
		buttonToday.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				ATime atime = null;
//				JComponent source = (JComponent) e.getSource();
//
//		        if (source == buttonToday) {
		        	LocalDate currentdate = LocalDate.now();
					monthCB.setSelectedIndex(currentdate.getMonth().getValue() - 1);
					tfDate.setText(Integer.toString(currentdate.getDayOfMonth()));
					tfYear.setText(Integer.toString(currentdate.getYear()));    					
//				}
//				dispose();
//				viewer.endDateDialog(atime);
				viewer.repaint();
			};
		});
		
		//////////////////////////////////////////////////////////
		
		button1986 = new JButton("1986 Halley");
		add(button1986);
		
		ActionListener listener00 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
//    				ATime atime = null;
//    				JComponent source = (JComponent) evt.getSource();
    				//1986-02-09
//    		        if (source == button1986) {
    		        	monthCB.setSelectedIndex(2);
    					tfDate.setText(9 + "");
    					tfYear.setText(1986 + ""); 
//    				}
//    				dispose();
//    				viewer.endDateDialog(atime);
    				viewer.repaint();
    			}
		};
		
		button1986.addActionListener(listener00);
		
		//////////////////////////////////////////////////////////
		
		buttonCancel = new JButton("Cancel");
		add(buttonCancel);
		
		ActionListener listener2 = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
//    				ATime atime = null;
//    				JComponent source = (JComponent) evt.getSource();
//    				if (source != buttonCancel) {
//    					//return false;
//    				}
//    				dispose();
    				setVisible(false);
//    				viewer.endDateDialog(atime);
    				//return true;
    			}
		};
		
		buttonCancel.addActionListener(listener2);
			
		setSize(new Dimension(350, 110));
		setPreferredSize(new Dimension(350, 110));		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);	
		
//		if (viewer.getDesktop() != null) {
			viewer.getDesktop().add(this);
			
			Dimension desktopSize = viewer.getDesktop().getParent().getSize();
		    Dimension jInternalFrameSize = this.getSize();
		    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		    int height = (int)((desktopSize.height - jInternalFrameSize.height) / 1.4);
		    setLocation(width, height);
		    
//		}
//		else {
//			viewer.getContentPane().add(this);
//			setLocation(0, 0);
//		}
			
	    setVisible(true);

//	    setModal(true);
	    viewer.repaint();
	    //validate();

		}
	}
