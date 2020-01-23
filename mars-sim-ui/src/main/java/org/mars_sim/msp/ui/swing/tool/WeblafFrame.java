package org.mars_sim.msp.ui.swing.tool;

import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;

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
//                WebLookAndFeel.install ();
//                UIManagers.initialize();

        		try {
        			// use the weblaf skin
        			WebLookAndFeel.install();
        			UIManagers.initialize();
        		} catch (Exception ex) {
        			ex.printStackTrace();
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