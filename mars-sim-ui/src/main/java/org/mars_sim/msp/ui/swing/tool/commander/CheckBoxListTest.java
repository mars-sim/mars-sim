/*
 * Mars Simulation Project
 * CheckBoxListTest.java
 * @date 2021-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.commander;

import java.awt.event.KeyEvent;

import com.alee.api.annotations.NotNull;
import com.alee.extended.debug.SwingTest;
import com.alee.extended.debug.TestFrame;
import com.alee.extended.list.CheckBoxCellData;
import com.alee.extended.list.CheckBoxListModel;
import com.alee.extended.list.WebCheckBoxList;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.style.StyleId;
import com.alee.utils.swing.extensions.KeyEventRunnable;

public class CheckBoxListTest
{
    public static void main ( final String[] args )
    {
        SwingTest.run ( new Runnable ()
        {
            @Override
            public void run ()
            {
                final CheckBoxListModel<String> model = new CheckBoxListModel<String> ();
                model.add ( new CheckBoxCellData<String> ( "Sample 1" ) );
                model.add ( new CheckBoxCellData<String> ( "Sample 2" ) );
                model.add ( new CheckBoxCellData<String> ( "Sample 3" ) );

                final WebCheckBoxList<String> list = new WebCheckBoxList<String> ( StyleId.checkboxlist, model );

                list.setCheckBoxSelected ( 0, true );
                list.setCheckBoxSelected ( 1, false );
                list.setCheckBoxSelected ( 2, true );

                TestFrame.show ( list );

                list.onKeyPress ( Hotkey.CTRL_A, new KeyEventRunnable ()
                {
                    @Override
                    public void run ( @NotNull final KeyEvent e )
                    {
                        for ( int i = 0; i < list.getModel ().getSize (); i++ )
                        {
                            list.setCheckBoxSelected ( i, false );
                        }
                    }
                } );
            }
        } );
    }
}
