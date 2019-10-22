package org.mars_sim.msp.ui.swing.tool.guide;

import static java.lang.System.err;
import static java.lang.System.out;

import java.awt.Container;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


    final class TestHTMLRendering {
        // ------------------------------ CONSTANTS ------------------------------

        /**
         * height of frame in pixels
         */
        private static final int height = 1000;

        /**
         * width of frame in pixels
         */
        private static final int width = 1000;

//        private static final String RELEASE_DATE = "2007-10-04";

        /**
         * title for frame
         */
        private static final String TITLE_STRING = "HTML Rendering";

        /**
         * URL of page we want to display
         */
        private static final String URL = "https://github.com/mars-sim/mars-sim"; //"https://www.google.com";//"file:///C:\\print.html";

        /**
         * program version
         */
        private static final String VERSION_STRING = "1.0";

        // --------------------------- main() method ---------------------------

        /**
         * Debugging harness for a JFrame
         *
         * @param args command line arguments are ignored.
         */
        public static void main( String args[] )
            {
            // Invoke the run method on the Swing event dispatch thread
            // Sun now recommends you call ALL your GUI methods on the Swing
            // event thread, even the initial setup.
            // Could also use invokeAndWait and catch exceptions
            SwingUtilities.invokeLater( new Runnable()
            {
            /**
             * } fire up a JFrame on the Swing thread
             */
            public void run()
                {
                out.println( "Starting" );
                final JFrame jframe =
                        new JFrame( TITLE_STRING + " " + VERSION_STRING );
                Container contentPane = jframe.getContentPane();
                jframe.setSize( width, height );

//                contentPane.setBackground( Color.YELLOW );
//                contentPane.setForeground( Color.BLUE );
                jframe.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
                try
                    {
                    out.println( "acquiring URL" );
                    JEditorPane jep = new JEditorPane( URL );
                    jep.setContentType("text/html");
                    jep.setEditable(false);
                    out.println( "URL acquired" );
                    JScrollPane jsp =
                            new JScrollPane( jep,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
                    contentPane.add( jsp );
                    }
                catch ( IOException e )
                    {
                    err.println( "can't find URL" );
                    contentPane.add( new JLabel( "can't find URL" ) );
                    }
                jframe.validate();
                jframe.setVisible( true );
                // Shows page, with HTML comments erroneously displayed.
                // The links are not clickable.
                }
            } );
            }// end main
        }// end TestHTMLRendering