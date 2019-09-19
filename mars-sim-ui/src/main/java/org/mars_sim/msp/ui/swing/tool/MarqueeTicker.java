/**
 * Mars Simulation Project
 * MarqueeTicker.java
 * @version 3.1.0 2017-01-19
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import com.alee.laf.panel.WebPanel;
import com.jidesoft.swing.MarqueePane;
import com.jidesoft.swing.StyleRange;
import com.jidesoft.swing.StyledLabel;

public class MarqueeTicker extends WebPanel {

	private static final long serialVersionUID = 1L;

	private static final int NUM_OF_RESOURCES = Settlement.NUM_CRITICAL_RESOURCES;

	private static final int WIDTH = 250;

	private static final int NUM_STEPS = 5;

	/*
	 * Updates the resource amounts every 10 real secs at the start of sim, minimal is 1 secs
	 * When the time ratio changes, updateInterval will change accordingly
	 */
	public int updateIntervalCache = 10, updateInterval = 10;

	private int subscript = -1, numLetters = 0, num0 = 0, num1 = 0, num3 = 0, num10 = 0;

	private int[] steps = new int[NUM_OF_RESOURCES * (NUM_STEPS+1)];

	private boolean firstTime = true;
	private boolean is10SolsVisible = false;
	private boolean is3SolsVisible = false;
	private boolean is1SolVisible = false;

	private String sp3 = "   ",	sp10 = "          ";
	private String label_1sol = " (1 sol)", label_3sols = " (3 sols)", label_10sols = " (10 sols)";

	private MarqueePane _horizonMarqueeLeft;
	private StyledLabel _styledLabel;
	private SettlementWindow settlementWindow;
	private Settlement settlement;
	private MarsClock marsClock;
	private MasterClock masterClock;
	private MainDesktopPane desktop;

	private DecimalFormat formatter = new DecimalFormat("##,###,##0.0");

	private ActionListener timeListener = null;
	private javax.swing.Timer updateTimer = null;

	private Map<Integer, Double> resourceCache = new HashMap<>();


	/*
	 * The constructor for MarqueeTicker class
	 */
	public MarqueeTicker(SettlementWindow settlementWindow) {
		super();
		this.settlementWindow = settlementWindow;
		//this.settlement = settlementWindow.getMapPanel().getSettlement();
		this.desktop = (MainDesktopPane) settlementWindow.getDesktop();

		masterClock = Simulation.instance().getMasterClock();
		marsClock = masterClock.getMarsClock();

		updateInterval = renewUpdateInterval();

		setLayout(new BorderLayout());
	    setOpaque(false);
		setBackground(Color.BLACK);

		settlement = (Settlement) Simulation.instance().getUnitManager().getSettlements().toArray()[0];
		settlement.sampleAllResources();

		createMarqueePanel();

		add(_horizonMarqueeLeft, BorderLayout.CENTER);

		if (timeListener == null) {
			timeListener = new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent evt) {
			    	updateStyledlabel();
			    	//System.out.println("inside ActionListener");
			    }
			};
		}

		if (updateTimer == null) {
			updateTimer = new javax.swing.Timer(updateInterval, timeListener);
			updateTimer.start();
    	}

		// leave a reference in desktop so that TimeWindow can retrieve an instance of MarqueeTicker for pausing/unpausing the ticker
//		desktop.setMarqueeTicker(this);
	}


	/*
	 * Obtains the availability information of a particular resource
	 */
	public String getOneResource(int resourceType) {
		StringBuffer s = new StringBuffer(""); // note: this creates a "null" at the beginning of s. will need to remove it later
		AmountResource ar = null;
		String resource = null;

		if (resourceType == 0) {
			//name = StyledLabelBuilder.createStyledLabel("O{2:sb}");
			//s = new StringBuffer("O{2:sb}");
			//s.append("O{2:sb}");
			s.append(sp10).append(sp10).append("O2");
			resource = LifeSupportInterface.OXYGEN;
			numLetters = 22; // including 20 whitespaces
			subscript = 21; // including 20 whitespaces
		}
		else if (resourceType == 1) {
			//name = StyledLabelBuilder.createStyledLabel("H{2:sb}");
			//s = new StringBuffer("H{2:sb}");
			//s.append("H{2:sb}");
			s.append("H2");
			resource = "hydrogen";
			numLetters = 2;//7;
			subscript = 1;
		}
		else if (resourceType == 2) {
			//name = StyledLabelBuilder.createStyledLabel("CO{2:sb}");
			//s = new StringBuffer("CO{2:sb}");
			//s.append("CO{2:sb}");
			s.append("CO2");
			resource = "carbon dioxide";
			numLetters = 3;//8;
			subscript = 2;
		}
		else if (resourceType == 3) {
			//name = StyledLabelBuilder.createStyledLabel("CH{4:sb}");
			//s = new StringBuffer("CH{4:sb}");
			//s.append("CH{4:sb}");
			s.append("CH4");
			resource = "methane";
			numLetters = 3;//8;
			subscript = 2;
			}
		else if (resourceType == 4) {
			//name = StyledLabelBuilder.createStyledLabel("H{2:sb}0");
			//s = new StringBuffer("H{2:sb}0");
			//s.append("H{2:sb}O");
			s.append("H2O");
			resource = LifeSupportInterface.WATER;
			numLetters = 3;//8;
			subscript = 1;
		}
		else if (resourceType == 5) {
			//name = StyledLabelBuilder.createStyledLabel("Grey H{2:sb}0");
			//s = new StringBuffer("Grey H{2:sb}0");
			//s.append("Grey H{2:sb}O");
			s.append("Grey H2O");
			resource = "grey water";
			numLetters = 8;//13;
			subscript = 6;
		}
		else if (resourceType == 6) {
			//name = StyledLabelBuilder.createStyledLabel("Black H{2:sb}0");
			//s = new StringBuffer("Black H{2:sb}0");
			//s.append("Black H{2:sb}O");
			s.append("Black H2O");
			resource = "black water";
			numLetters = 9;//14;
			subscript = 7;
		}
		else if (resourceType == 7) {
			//name = StyledLabelBuilder.createStyledLabel("Rock");
			//s = new StringBuffer("Rock");
			s.append("Rock Samples");
			resource = "rock samples";
			numLetters = 12;
			subscript = -1;
		}
		else if (resourceType == 8) {
			//name = StyledLabelBuilder.createStyledLabel("Ice");
			//s = new StringBuffer("Ice");
			s.append("Ice");
			resource = "ice";
			numLetters = 3;
			subscript = -1;
		}

		s.append(sp3);

		Settlement thisSettlement = settlement;

		double todayAverage = thisSettlement.getAverage(0, resourceType);
		//System.out.println("todayAverage is " + todayAverage);

		String ave = formatter.format(todayAverage);
		int size_ave = ave.length();

		num0 = size_ave;
		s.append(ave).append(sp3);

		// CALCULATE THE DIFFERENCE SINCE 1 SOL AGO
		// Subtract yesterday's average from today's average to obtain the delta
		double delta_1sol = 0;
		String delta1 = "---";
		if (thisSettlement.getSolCache() > 1) {
			is1SolVisible = true;
			delta_1sol = todayAverage - thisSettlement.getAverage(-1, resourceType);

			//System.out.println(settlement + " : " + resource + "  todayAverage : " + todayAverage + "  delta1sol : " + delta1sol);
			delta1 = formatter.format(delta_1sol);
			int size_delta1 = delta1.length();

			if (delta_1sol > 0){
				s.append("+");
				size_delta1++;
			}
			else if (delta_1sol == 0){
				s.append("+");
				size_delta1++;
			}
			else if (delta_1sol < 0) {
				;
			}
			num1 = size_delta1;
		}
		else {
			num1 = delta1.length();
		}
		// Add the 1 sol delta figure and the "(1 sol)" label and 3 whitespaces
		s.append(delta1).append(label_1sol);
		s.append(sp3);


		// 2015-12-22 Added CALCULATE THE DIFFERENCE SINCE 3 SOL AGO
		// Subtract the average of 3 sols ago from today's to obtain the delta
		double delta_3sols = 0;
		String delta3 = "---";
		if (thisSettlement.getSolCache() > 3) {
			is3SolsVisible = true;
			delta_3sols = todayAverage - thisSettlement.getAverage(-3, resourceType);

			//System.out.println(settlement + " : " + resource + "  todayAverage : " + todayAverage + "  delta1sol : " + delta1sol);
			delta3 = formatter.format(delta_3sols);
			int size_delta3 = delta3.length();

			if (delta_3sols > 0){
				s.append("+");
				size_delta3++;
			}
			else if (delta_3sols == 0){
				s.append("+");
				size_delta3++;
			}
			else if (delta_3sols < 0) {
				;
			}
			num3 = size_delta3;
		}
		else {
			num3 = delta3.length();
		}

		// Add the 3 sols delta figure and the "(3 sols)" label
		s.append(delta3).append(label_3sols);
		s.append(sp3);


		// 2015-12-22 Added CALCULATE THE DIFFERENCE SINCE 10 SOLS AGO
		// Subtract the average of 10 sols ago from today's to obtain the delta
		double delta_10sols = 0;
		String delta10 = "---";
		if (thisSettlement.getSolCache() > 10) {
			is10SolsVisible = true;
			delta_10sols = todayAverage - thisSettlement.getAverage(-10, resourceType);

			//System.out.println(settlement + " : " + resource + "  todayAverage : " + todayAverage + "  delta1sol : " + delta1sol);
			delta10 = formatter.format(delta_10sols);
			int size_delta10 = delta10.length();

			if (delta_10sols > 0){
				s.append("+");
				size_delta10++;
			}
			else if (delta_10sols == 0){
				s.append("+");
				size_delta10++;
			}
			else if (delta_10sols < 0) {
				;
			}
			num10 = size_delta10;
		}
		else {
			num10 = delta10.length();
		}

		// Add the 10 sols delta figure and the "(10 sols)" label
		s.append(delta10).append(label_10sols);


		// ADD 10 WHITESPACES
		if (resourceType != NUM_OF_RESOURCES - 1)
			s.append(sp10);

		//System.out.println("s is " + s);
		return s.toString();
	}



    /*
     * Creates the steps array and the resource ticker
     */
    private void createResourceTicker() {

    	String s = null;

        for (int i= 0; i < NUM_OF_RESOURCES; i++) {
        	s += getOneResource(i);
        	steps[(NUM_STEPS+1) * i ] = numLetters;
        	steps[(NUM_STEPS+1) * i + 1] = subscript;
        	steps[(NUM_STEPS+1) * i + 2] = num0;
        	steps[(NUM_STEPS+1) * i + 3] = num1;
        	steps[(NUM_STEPS+1) * i + 4] = num3;
        	steps[(NUM_STEPS+1) * i + 5] = num10;
        }

        //Remove the extra null created at the beginning of the Stringbuffer string
        s = s.replace("null", "");

        setStyledLabel(s);
    }

    /*
     * Creates a display styled label that comprises all resources
     * @param s display string
     */
    public void setStyledLabel(String s) {
       	//StyledLabel styledLabel = new StyledLabel();
        _styledLabel.setText(s);

        _styledLabel.setOpaque(false);
        _styledLabel.setBackground(new Color(0,0,0,128));

/*        System.out.print("steps[");
        for (int i = 0; i < size; i++) {
        	if (i != size - 1)
        		System.out.print(steps[i] + ", ");
        	else
        		System.out.println(steps[i] + "]");
        }
 */
        int index = 0;
       	int sp_1sol = label_1sol.length();
       	int sp_3sols = label_3sols.length();
       	int sp_10sols = label_10sols.length();

       	int spaces3 = sp3.length();
    	int spaces10 = sp10.length();
        int size = steps.length;
        for (int i = 0; i < size; i++) {
           	//System.out.println("i = " + i);
        	//System.out.println("i % 4 = " + i%4);
        	if (i % (NUM_STEPS+1) == 0) { // create the style for the resource name
               	//System.out.println("index = " + index);
            	//System.out.println("length = " + steps[i]);
                if (steps[i+1] != -1) {
	            	//System.out.println("subscript is at index " + (index + steps[i+1]));
                	// create the style for the first part of the resource name
                	StyleRange styleRange0 = new StyleRange(index, steps[i+1], Font.BOLD, Color.WHITE, Color.BLACK, 0, Color.WHITE);
                    // create the style for the subscript font in the resource name
                	StyleRange styleRange1 = new StyleRange(index + steps[i+1], 1, Font.BOLD, Color.WHITE, Color.BLACK, StyleRange.STYLE_SUBSCRIPT);
                	// create the style for the second part of the resource name                    //
                	StyleRange styleRange2 = new StyleRange(index + steps[i+1] + 1, steps[i] - 1 - steps[i+1] + spaces3, Font.BOLD, Color.WHITE, Color.BLACK, 0, Color.WHITE);
                	_styledLabel.addStyleRange(styleRange0);
                	_styledLabel.addStyleRange(styleRange1);
                	_styledLabel.addStyleRange(styleRange2);
                }
                else { // create the style for a resource's current average, num0
                    StyleRange styleRange3 = new StyleRange(index, steps[i]  + spaces3, Font.BOLD, Color.WHITE, Color.BLACK, 0, Color.WHITE);
                    _styledLabel.addStyleRange(styleRange3);
                }

                index += steps[i] + spaces3;
        	}

        	else if (i % (NUM_STEPS+1) == 1) { // create the style for the subscript was done in the previous if (i % 4 == 0)
        		;
        	}

        	else if (i % (NUM_STEPS+1) == 2) { // create the style for num0
            	//System.out.println("index = " + index );
            	//System.out.println("length = " + steps[i]);
        		_styledLabel.addStyleRange(new StyleRange(index, steps[i]  + spaces3, Font.ITALIC, Color.WHITE, Color.BLACK, 0, Color.WHITE));
                index += steps[i] + spaces3;
        	}

            else if (i % (NUM_STEPS+1) == 3) { //create the style for the first delta, num2
            	//System.out.println("index = " + index);
            	//System.out.println("length = " + steps[i]);
            	//System.out.println("styledLabel.getText() is " + styledLabel.getText());
                if (!is1SolVisible) {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_1sol + spaces3 , Font.PLAIN, Color.GRAY, Color.BLACK, 0, Color.WHITE));
                }
                else if (_styledLabel.getText().charAt(index) == '-') {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_1sol + spaces3 , Font.PLAIN, Color.RED, Color.BLACK, 0, Color.WHITE));
                }
                else {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_1sol + spaces3 , Font.PLAIN, Color.GREEN, Color.BLACK, 0, Color.WHITE));
                }
                index += steps[i] + sp_1sol + spaces3;
            }

            else if (i % (NUM_STEPS+1) == 4) { //create the style for the second delta, num3
            	//System.out.println("index = " + index);
            	//System.out.println("length = " + steps[i]);
            	//System.out.println("styledLabel.getText() is " + styledLabel.getText());
                if (!is3SolsVisible) {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_3sols + spaces3 , Font.PLAIN, Color.GRAY, Color.BLACK, 0, Color.WHITE));
                }
                else if (_styledLabel.getText().charAt(index) == '-') {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_3sols + spaces3 , Font.PLAIN, Color.RED, Color.BLACK, 0, Color.WHITE));
                }
                else {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_3sols + spaces3 , Font.PLAIN, Color.GREEN, Color.BLACK, 0, Color.WHITE));
                }
                index += steps[i] + sp_3sols + spaces3;
            }

            else if (i % (NUM_STEPS+1) == 5) { //create the style for the third delta, num10
            	//System.out.println("index = " + index);
            	//System.out.println("length = " + steps[i]);
            	//System.out.println("styledLabel.getText() is " + styledLabel.getText());
                if (!is10SolsVisible) {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_10sols + spaces10, Font.PLAIN, Color.GRAY, Color.BLACK, 0, Color.WHITE));
                }

                else if (_styledLabel.getText().charAt(index) == '-') {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_10sols + spaces10, Font.PLAIN, Color.RED, Color.BLACK, 0, Color.WHITE));
                }
                else {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + sp_10sols + spaces10, Font.PLAIN, Color.GREEN, Color.BLACK, 0, Color.WHITE));
                }
                index += steps[i] + sp_10sols + spaces10;
            }
        }

    }


    /*
	 * Updates the display values to the latest one from resourcesCache when the ticker reaches the end of the display
	 */
    public Component getUpdate() {
        JPanel panel = new JPanel();//new FlowLayout(FlowLayout.CENTER,0,0));
        panel.setBackground(Color.BLACK);
        JButton button = new JButton(new AbstractAction("Refresh") {
            private static final long serialVersionUID = 3433227364758002853L;

            public void actionPerformed(ActionEvent e) {
            	updateStyledlabel();
            }
        });
        Font myFont = new Font("Dialog", Font.BOLD,9);
        button.setFont(myFont);
        button.setVerticalAlignment(SwingConstants.BOTTOM);
        panel.add(button);
        return panel;
    }

    /*
     * Updates the Styledlabel and the updateInterval
     */
    public void updateStyledlabel() {
		updateInterval = renewUpdateInterval();
		//System.out.println("updateStyledlabel is called");
    	if (updateIntervalCache != updateInterval) {
    		updateTimer.stop();
    		updateTimer = new javax.swing.Timer(updateInterval, timeListener);
			updateTimer.start();
			updateIntervalCache = updateInterval;
    	}
    	//else {

	    	_styledLabel.clearStyleRanges();
			//_horizonMarqueeLeft.stopAutoScrolling();
			updateTimer.stop();
			createResourceTicker();
			_horizonMarqueeLeft.updateUI();
			updateTimer.start();
			//_horizonMarqueeLeft.startAutoScrolling();
    	//}
    }

    public Component getFreeze() {
        JPanel panel = new JPanel();//new FlowLayout(FlowLayout.CENTER,0,0));//GridLayout(1,2,0,0));
        //panel.setBackground(Color.BLACK);
        JCheckBox freezeCheckBox = new JCheckBox("Freeze");
        Font myFont = new Font("Dialog", Font.BOLD,9);
        freezeCheckBox.setFont(myFont);
        freezeCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    _horizonMarqueeLeft.stopAutoScrolling();
                }
                else {
                    _horizonMarqueeLeft.startAutoScrolling();
                }
            }
        });
        panel.add(freezeCheckBox);
        return panel;
    }

    /*
     * Assembles the marguee ticker
     */
    public void createMarqueePanel() { //Component getMarqueePanel() {
    	_styledLabel = new StyledLabel();
    	createResourceTicker();

        MarqueePane horizonMarqueeLeft = new MarqueePane(_styledLabel);

        horizonMarqueeLeft.setPreferredSize(new Dimension(WIDTH, 38));

        _horizonMarqueeLeft = horizonMarqueeLeft;

    }


	public void addItem(JPanel p, Component c, int x, int y, int w, int h, int align, int fill) {

		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = w;
		gc.gridheight = h;
		gc.weightx = 150.0;
		gc.weighty = 2.0;
		gc.insets = new Insets(2,2,2,2);
		gc.anchor = align;
		gc.fill = fill;
		p.add(c,gc);

	}

	public void updateSettlement(Settlement newSettlement) {
		settlement = newSettlement;

		updateStyledlabel();

		if (updateTimer != null) {
			;
		}
		else {
			//_horizonMarqueeLeft.stopAutoScrolling();
			updateTimer = new javax.swing.Timer(updateInterval, timeListener);

		}
	}

	public void pauseMarqueeTimer(boolean value) {
		if (value){
			updateTimer.stop();
			_horizonMarqueeLeft.stopAutoScrolling();
		}
		else {
			updateTimer.start();
			_horizonMarqueeLeft.startAutoScrolling();
		}
	}

	/*
	 * Computes the new updateInterval for the news ticker based upon the current time ratio
	 */
	public int renewUpdateInterval() {
		double result = 0;
		double timeRatio = (int) (masterClock.getTimeRatio());

		if (timeRatio == 500)
			result = 10_000;
		else if (timeRatio > 500)
			result = timeRatio * .8738 + 9563.1058;
		else if (timeRatio < 500)
			result = timeRatio * 100.2004 + 59900D;

		return (int) result;
	}

	public void destroy() {
		settlement = null;
		settlementWindow = null;
		updateTimer.stop();
		updateTimer = null;
		//horizonMarqueeLeft = null;
		_horizonMarqueeLeft = null;
		_styledLabel = null;
	}
}
