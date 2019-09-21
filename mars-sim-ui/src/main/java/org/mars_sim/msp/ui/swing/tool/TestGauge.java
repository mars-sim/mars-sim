package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.ui.steelseries.gauges.DisplayCircular;
import org.mars_sim.msp.ui.steelseries.tools.BackgroundColor;
import org.mars_sim.msp.ui.steelseries.tools.FrameDesign;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.LedColor;


public class TestGauge {
    @SuppressWarnings("serial")
	private static void createAndShowUI() {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);

        JPanel panel = new JPanel() {
            @Override 
            public Dimension getPreferredSize() {
                return new Dimension(300, 300);
            }
        };

        int min = -1;
        int max = 5;
        
        final DisplayCircular gauge = new DisplayCircular();
        //final RadialQuarterN gauge = new RadialQuarterN();
        //DigitialRadial gauge = new DigitialRadial();
        
        //final DigitalRadial gauge = new DigitalRadial();
        gauge.setDigitalFont(true);
        gauge.setFrameDesign(FrameDesign.ANTHRACITE);//.GOLD);
        //gauge.setPointerType(PointerType.TYPE5);
       // alt.setTextureColor(Color.yellow);//, Texture_Color BRUSHED_METAL and PUNCHED_SHEET);
        gauge.setUnitString("km");
        gauge.setTitle("Elevation");
        gauge.setMinValue(min);
        gauge.setMaxValue(max);
        //gauge.setTicklabelsVisible(true);
        //gauge.setMaxNoOfMajorTicks(10);
        //gauge.setMaxNoOfMinorTicks(10);
        gauge.setBackgroundColor(BackgroundColor.NOISY_PLASTIC);//.BRUSHED_METAL);
        gauge.setGlowColor(Color.orange);//.yellow);
        gauge.setLcdColor(LcdColor.BEIGE_LCD);//.BLACK_LCD);
        //alt.setLcdInfoString("Elevation");
        //alt.setLcdUnitString("km");
        gauge.setLcdValueAnimated(3.50);
        gauge.setValueAnimated(3.5);
        //gauge.setValue(elevationCache);
        gauge.setLcdDecimals(3);
        
        panel.setLayout(new BorderLayout());
        panel.add(gauge, BorderLayout.CENTER);
        frame.add(panel);

        JPanel buttonsPanel = new JPanel();
        JLabel valueLabel = new JLabel("Value:");

        final JTextField valueField = new JTextField(7);
        valueField.setText("30");
        JButton button = new JButton("Set");
        button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.valueOf(valueField.getText());
                    gauge.setValueAnimated(value);
                    gauge.setBackgroundColor(BackgroundColor.WHITE);
                    gauge.setLcdColor(LcdColor.STANDARD_GREEN_LCD);
                    //gauge.setKnobStyle(KnobStyle.BRASS);
                    //gauge.setColor(ColorDef.BLUE);
                    gauge.setLedColor(LedColor.BLUE);
                } catch(NumberFormatException ex) { 
                    //TODO - handle invalid input 
                    System.err.println("invalid input");
                }
            }
        });

        buttonsPanel.add(valueLabel);
        buttonsPanel.add(valueField);
        buttonsPanel.add(button);

        frame.add(buttonsPanel, BorderLayout.NORTH);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowUI();
            }
        });
    }
}