/**
 * Mars Simulation Project
 * MarqueeTicker.java
 * @version 3.08 2015-10-24
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import com.jidesoft.swing.JideTitledBorder;
import com.jidesoft.swing.MarqueePane;
import com.jidesoft.swing.PartialEtchedBorder;
import com.jidesoft.swing.PartialSide;
import com.jidesoft.swing.StyleRange;
import com.jidesoft.swing.StyledLabel;
import com.jidesoft.swing.StyledLabelBuilder;

public class MarqueeTicker extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int NUM_OF_RESOURCES = Settlement.NUM_CRITICAL_RESOURCES;

	public static final int TIME_DELAY = 5000; // update the values every 5 real secs

	//private static final String TEXT1 = "GOOG   429.11   -6.51          DIA   87.64   -0.1          FXI   39.19   +1.12          GLD   93.62   -0.21          USO   39   +0.81          MSFT   22.25   +0.17";
/*	private static final String TEXT1 =
			"H2O	  500.00   -5.55          "
    		+"O2   87.64   -0.1          "
    		+"CO2   39.19   +1.12          "
    		+"CO   93.62   -0.21          "
    		+"CH4   39   +0.81          "
    		+"H2   22.25   +0.17          ";

	private static final String TEXT2 =
			"H2O	  431.11   -6.51          "
    		+"O2   87.64   -0.1          "
    		+"CO2   39.19   +1.12          "
    		+"CO   93.62   -0.21          "
    		+"CH4   39   +0.81          "
    		+"H2   22.25   +0.17          ";
*/
	private static final int WIDTH = 250;

	//private double O2Cache = 0, H2Cache = 0, CO2Cache = 0, CH4Cache = 0, H2OCache = 0, GreyCache = 0, BlackCache = 0, RockCache = 0, IceCache = 0;

	//private int init = 0;
	private int subscript = -1, num0 = 0, num1 = 0, num2 = 0;

	private int[] steps = new int[NUM_OF_RESOURCES * 4];
	//private int solCache;
	private boolean firstTime = true;

	private String sp3 = "   ",	sp10 = "          ";

	private MarqueePane _horizonMarqueeLeft;
	private StyledLabel _styledLabel;

	private SettlementWindow settlementWindow;

	private Settlement settlement;

	private MarsClock marsClock;

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
		this.settlement = settlementWindow.getMapPanel().getSettlement();
		this.desktop = settlementWindow.getDesktop();
		//this.solCache = 1;

		marsClock = Simulation.instance().getMasterClock().getMarsClock();

		setLayout(new BorderLayout());
	    setOpaque(false);
		//setBackground(new Color(255, 255, 255, 0));
		setBackground(Color.BLACK);
	    //setBackground(new Color(0,0,0,128));
	    //getRootPane().setOpaque(false);
	    //getRootPane().setBackground(new Color(0,0,0,128));
		//getContentPane().setBackground(Color.BLACK);

		settlement.sampleAllResources();

		createMarqueePanel();

		add(_horizonMarqueeLeft, BorderLayout.CENTER);
/*
        JPanel tickerPanel = new JPanel(new GridBagLayout());//new BorderLayout(0, 0));
        tickerPanel.setBackground(Color.BLACK);
		add(tickerPanel, BorderLayout.CENTER);
        //addItem(tickerPanel, getUpdate(), 0, 0, 1, 1, GridBagConstraints.WEST);
        addItem(tickerPanel, horizonMarqueeLeft, 0, 0, 20, 10, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL);
        //addItem(tickerPanel, getFreeze(), 2, 0, 1, 1, GridBagConstraints.EAST);
*/
        //add(getMarqueePanel(), BorderLayout.BEFORE_FIRST_LINE);
		//add(horizonMarqueeLeft, BorderLayout.BEFORE_FIRST_LINE);
		//_horizonMarqueeLeft.startAutoScrolling();


		if (timeListener == null) {
			timeListener = new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent evt) {
			    	updateStyledlabel();
			    }
			};
		}

		if (updateTimer == null) {
			updateTimer = new javax.swing.Timer(TIME_DELAY, timeListener);
			updateTimer.start();
    	}

		// leave a reference in desktop so that TimeWindow can retrieve an instance of MarqueeTicker for pausing/unpausing the ticker
		desktop.setMarqueeTicker(this);
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
			s.append("O2");
			resource = LifeSupportType.OXYGEN;
			num0 = 2;//7;
			subscript = 1;
		}
		else if (resourceType == 1) {
			//name = StyledLabelBuilder.createStyledLabel("H{2:sb}");
			//s = new StringBuffer("H{2:sb}");
			//s.append("H{2:sb}");
			s.append("H2");
			resource = "hydrogen";
			num0 = 2;//7;
			subscript = 1;
		}
		else if (resourceType == 2) {
			//name = StyledLabelBuilder.createStyledLabel("CO{2:sb}");
			//s = new StringBuffer("CO{2:sb}");
			//s.append("CO{2:sb}");
			s.append("CO2");
			resource = "carbon dioxide";
			num0 = 3;//8;
			subscript = 2;
		}
		else if (resourceType == 3) {
			//name = StyledLabelBuilder.createStyledLabel("CH{4:sb}");
			//s = new StringBuffer("CH{4:sb}");
			//s.append("CH{4:sb}");
			s.append("CH4");
			resource = "methane";
			num0 = 3;//8;
			subscript = 2;
			}
		else if (resourceType == 4) {
			//name = StyledLabelBuilder.createStyledLabel("H{2:sb}0");
			//s = new StringBuffer("H{2:sb}0");
			//s.append("H{2:sb}O");
			s.append("H2O");
			resource = LifeSupportType.WATER;
			num0 = 3;//8;
			subscript = 1;
		}
		else if (resourceType == 5) {
			//name = StyledLabelBuilder.createStyledLabel("Grey H{2:sb}0");
			//s = new StringBuffer("Grey H{2:sb}0");
			//s.append("Grey H{2:sb}O");
			s.append("Grey H2O");
			resource = "grey water";
			num0 = 8;//13;
			subscript = 6;
		}
		else if (resourceType == 6) {
			//name = StyledLabelBuilder.createStyledLabel("Black H{2:sb}0");
			//s = new StringBuffer("Black H{2:sb}0");
			//s.append("Black H{2:sb}O");
			s.append("Black H2O");
			resource = "black water";
			num0 = 9;//14;
			subscript = 7;
		}
		else if (resourceType == 7) {
			//name = StyledLabelBuilder.createStyledLabel("Rock");
			//s = new StringBuffer("Rock");
			s.append("Rock Samples");
			resource = "rock samples";
			num0 = 12;
			subscript = -1;
		}
		else if (resourceType == 8) {
			//name = StyledLabelBuilder.createStyledLabel("Ice");
			//s = new StringBuffer("Ice");
			s.append("Ice");
			resource = "ice";
			num0 = 3;
			subscript = -1;
		}

		s.append(sp3);

		Settlement thisSettlement = settlement;

		double todayAverage = thisSettlement.getAverage(0, resourceType);
		//System.out.println("todayAverage is " + todayAverage);

		String na = formatter.format(todayAverage);
		int size_na = na.length();

		num1 = size_na;
		s.append(na).append(sp3);

		// Subtract yesterday's average from today's average to obtain the delta
		double delta1sol = 0;

		if (thisSettlement.getSolCache() != 1) {
			delta1sol = todayAverage - thisSettlement.getAverage(-1, resourceType);
		}

		//System.out.println(settlement + " : " + resource + "  todayAverage : " + todayAverage + "  delta1sol : " + delta1sol);

		String ch = formatter.format(delta1sol);
		int size_ch = ch.length();
		num2 = size_ch;

		if (delta1sol > 0){
			s.append("+").append(ch);
			size_ch++;
		}
		else if (delta1sol == 0){
			s.append("+").append(ch);
			size_ch++;
		}
		else if (delta1sol < 0) {
			s.append(ch);
		}
		num2 = size_ch;

		if (resourceType != NUM_OF_RESOURCES - 1)
			s.append(sp10);

		//System.out.println("s is " + s);
		return s.toString();
	}


	/*
	 * Calculates the difference between the current storage value and its cache

	public double calculateChange(int resourceType, double newAmount) {
		double result = 0;

		Settlement thisSettlement = settlement;
		if (thisSettlement != null) {
			resourceCache = thisSettlement.getResourceMapCache();
			double oldAmount = resourceCache.get(resourceType);
			result = newAmount - oldAmount;
			thisSettlement.setOneResourceCache(resourceType, newAmount);
		}
		else
			//return result; // equals zero
			System.err.println("Can't initialize resource cache in " + settlement.getName());

		return result;
	}
*/

    /*
     * Creates the steps array and the resource ticker
     */
    private void createResourceTicker() {

    	String s = null;

        for (int i= 0; i < NUM_OF_RESOURCES; i++) {
        	s += getOneResource(i);
        	steps[4 * i ] = num0;
        	steps[4 * i + 1] = subscript;
        	steps[4 * i + 2] = num1;
        	steps[4 * i + 3] = num2;
        }

        //System.out.println("getResourceString(): resources = "+ resources);
        //remove the extra null created at the beginning of the Stringbuffer string
        s = s.replace("null", "");

        //System.out.println("steps.length = " + size);
        //System.out.println("s = " + s);
       	//StyledLabel styledLabel = null;
        //styledLabel =  StyledLabelBuilder.createStyledLabel(s);

        setStyledLabel(s);
    }

    /*
     * Creates a display styled label that comprises all resources
     * @param s display string
     */
    public void setStyledLabel(String s) {
       	//StyledLabel styledLabel = new StyledLabel();
        _styledLabel.setText(s);
        //_styledLabel = styledLabel;

        _styledLabel.setOpaque(false);
        _styledLabel.setBackground(new Color(0,0,0,128));
        //styledLabel.setBackground(Color.BLACK);


/*        System.out.print("steps[");
        for (int i = 0; i < size; i++) {
        	if (i != size - 1)
        		System.out.print(steps[i] + ", ");
        	else
        		System.out.println(steps[i] + "]");
        }
 */
        int index = 0;
       	int spaces3 = sp3.length();
    	int spaces10 = sp10.length();
        int size = steps.length;
        for (int i = 0; i < size; i++) {
           	//System.out.println("i = " + i);
        	//System.out.println("i % 4 = " + i%4);
        	if (i % 4 == 0) {
               	//System.out.println("index = " + index);
            	//System.out.println("length = " + steps[i]);
                if (steps[i+1] != -1) {
	            	//System.out.println("subscript is at index " + (index + steps[i+1]));
                    StyleRange styleRange0 = new StyleRange(index, steps[i+1], Font.BOLD, Color.WHITE, Color.BLACK, 0, Color.WHITE);
                	StyleRange styleRange1 = new StyleRange(index + steps[i+1], 1, Font.BOLD, Color.WHITE, Color.BLACK, StyleRange.STYLE_SUBSCRIPT);
                    StyleRange styleRange2 = new StyleRange(index + steps[i+1] + 1, steps[i] - 1 - steps[i+1] + spaces3, Font.BOLD, Color.WHITE, Color.BLACK, 0, Color.WHITE);
                	_styledLabel.addStyleRange(styleRange0);
                	_styledLabel.addStyleRange(styleRange1);
                	_styledLabel.addStyleRange(styleRange2);
                }
                else {
                    StyleRange styleRange3 = new StyleRange(index, steps[i]  + spaces3, Font.BOLD, Color.WHITE, Color.BLACK, 0, Color.WHITE);
                    _styledLabel.addStyleRange(styleRange3);
                }

                index += steps[i] + spaces3;
        	}
        	else if (i % 4 == 1) {
/*        		if (steps[i] != -1) {
        			int sub_index = (index - steps[i-1] - spaces3 + steps[i]);
        			StyleRange styleRange2 = new StyleRange(sub_index, 1, StyleRange.STYLE_SUBSCRIPT);
	            	//System.out.println("subscript is at index " + sub_index);
	            	//System.out.println("length = " + 1);
	        		char sub = styledLabel.getText().charAt(sub_index);
	        		System.out.println("subscript is " + sub);
	            	styledLabel.clearStyleRange(styleRange2);
	        		styledLabel.addStyleRange(styleRange2);//new StyleRange((index - steps[i-1] - spaces3 + steps[i]), 1, StyleRange.STYLE_SUBSCRIPT));
	        		//StyleRange(int start, int length, int fontStyle, Color fontColor, Color backgroundColor, int additionalStyle)
	        		//styledLabel.setStyleRange((index - steps[i-1] - spaces3 + steps[i]), 1, StyleRange.STYLE_SUBSCRIPT, Color.WHITE, Color.BLACK, 0);
	        		//index += steps[i];
	        	}
*/        	}
        	else if (i % 4 == 2) {
            	//System.out.println("index = " + index );
            	//System.out.println("length = " + steps[i]);
        		_styledLabel.addStyleRange(new StyleRange(index, steps[i]  + spaces3, Font.ITALIC, Color.WHITE, Color.BLACK, 0, Color.WHITE));
                index += steps[i] + spaces3;
        	}
            else {
            	//System.out.println("index = " + index);
            	//System.out.println("length = " + steps[i]);
            	//System.out.println("styledLabel.getText() is " + styledLabel.getText());
                if (_styledLabel.getText().charAt(index) == '-') {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + spaces10, Font.PLAIN, Color.RED, Color.BLACK, 0, Color.WHITE));
                }
                else {
                    _styledLabel.addStyleRange(new StyleRange(index, steps[i] + spaces10, Font.PLAIN, Color.GREEN, Color.BLACK, 0, Color.WHITE));
                }
                index += steps[i] + spaces10;
            }

        }

        //return styledLabel;
    }


    /*
	 * Updates the diplay values to the latest one from resourcesCache when the ticker reaches the end of the display
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

    public void updateStyledlabel() {
    	_styledLabel.clearStyleRanges();
		_horizonMarqueeLeft.stopAutoScrolling();
		updateTimer.stop();
		createResourceTicker();
		_horizonMarqueeLeft.updateUI();
		updateTimer.start();
		_horizonMarqueeLeft.startAutoScrolling();
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
        //JPanel panel = new JPanel();
        //StyledLabel styledLabel = new StyledLabel();
    	_styledLabel = new StyledLabel();
    	createResourceTicker();//new StyledLabel();
        //customizeStyledLabel(styledLabel);
        //_styledLabel = styledLabel;

        //styledLabel.setBackground(Color.BLACK);

        MarqueePane horizonMarqueeLeft = new MarqueePane(_styledLabel);

        //horizonMarqueeLeft.setOpaque(false);
        //horizonMarqueeLeft.setBackground(new Color(0,0,0,128));
        //horizonMarqueeLeft.setBackground(Color.BLACK);

        //horizonMarqueeLeft.setPreferredSize(new Dimension(WIDTH, 40));

        //int width = (int) horizonMarqueeLeft.getPreferredSize().getWidth(); //600;//settlementWindow.getWidth();
        //if (settlementWindow.getDesktop().getMainScene() != null)
        //	width = settlementWindow.getDesktop().getMainScene().getStage().getScene().getWidth();

        horizonMarqueeLeft.setPreferredSize(new Dimension(WIDTH, 38));
        //horizonMarqueeLeft.setBorder(BorderFactory.createCompoundBorder(new JideTitledBorder(
        //		new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), "Scroll Left", JideTitledBorder.LEADING, JideTitledBorder.ABOVE_TOP),
        //        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

 /*
        JPanel demoPanel = new JPanel(new GridLayout(1,3,0,0));//new BorderLayout(0, 0));
        demoPanel.setBackground(Color.BLACK);
        demoPanel.add(horizonMarqueeLeft, BorderLayout.CENTER);//BEFORE_FIRST_LINE);
        JPanel miscPanel = new JPanel(new GridLayout(1,2,0,0));
        demoPanel.add(miscPanel, BorderLayout.EAST);
        miscPanel.add(getFreeze());//, BorderLayout.EAST);
        miscPanel.add(getUpdate());//, BorderLayout.CENTER);
	    //System.out.println("done with setting up getDemoPanel()");
 */
        _horizonMarqueeLeft = horizonMarqueeLeft;

    }

/*
	public void paintComponent(Graphics g){
	    super.paintComponent(g);
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, horizonMarqueeLeft.getWidth(), horizonMarqueeLeft.getHeight());  //getX(), getY()
	}
*/

	@Override
    protected void paintComponent(Graphics g) {
        // Allow super to paint
        super.paintComponent(g);

        // Apply our own painting effect
        Graphics2D g2d = (Graphics2D) g.create();
        // 50% transparent Alpha
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        //g2d.setColor(getBackground());
        g2d.setColor(Color.BLACK);
        g2d.fill(getBounds());

        g2d.dispose();

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

    	//System.out.println("updateSettlement() : just called _styledLabel = getResourceString()");
        //_styledLabel.setOpaque(false);
        //_styledLabel.setBackground(new Color(0,0,0,128));

		if (updateTimer != null) {
			updateStyledlabel();
		}
		else {
			//_horizonMarqueeLeft.stopAutoScrolling();
			updateTimer = new javax.swing.Timer(TIME_DELAY, timeListener);
			updateStyledlabel();
		}
	}
/*
	public class ContentPane extends JPanel {
	    public ContentPane() {
	        setOpaque(false);
	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        // Allow super to paint
	        super.paintComponent(g);
	        // Apply our own painting effect
	        Graphics2D g2d = (Graphics2D) g.create();
	        // 50% transparent Alpha
	        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
	        g2d.setColor(getBackground());
	        g2d.fill(getBounds());
	        g2d.dispose();
	    }
	}
*/
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
