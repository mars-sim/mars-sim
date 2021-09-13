/*
 * Mars Simulation Project
 * WeblafFrame.java
 * @date 2021-09-05
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.alee.laf.WebLookAndFeel;
import com.alee.managers.UIManagers;

public class WeblafFrame
{
    public static void main ( String[] args )
    {
        SwingUtilities.invokeLater ( new Runnable ()
        {
            public void run ()
            {

        		try {
        			// use the weblaf skin
        			WebLookAndFeel.install();
        			UIManagers.initialize();
        		} catch (Exception ex) {
        			System.out.println("Error in initiating weblaf: " + ex);
        		}
        		
                JFrame frame = new JFrame ( "Test" );
                frame.add ( new JButton ( "Test" ) );
                frame.setSize ( 500, 500 );
                frame.setLocationRelativeTo ( null );
                frame.setVisible ( true );
            }
        } );
    }
}
