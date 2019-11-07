package org.mars_sim.msp.ui.swing.tool;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.WindowConstants;

import com.alee.extended.menu.DynamicMenuType;
import com.alee.extended.menu.WebDynamicMenu;
import com.alee.extended.menu.WebDynamicMenuItem;
import com.alee.extended.panel.GroupPanel;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.text.WebTextField;
import com.alee.laf.window.WebFrame;
import com.alee.utils.SwingUtils;
import com.alee.utils.swing.IntTextDocument;

public class DynamicMenu extends WebFrame {
    private final WebComboBox type;
    private final WebComboBox hidingType;
    private final WebTextField radius;
    private final WebTextField amount;

    public DynamicMenu () {
        super ();

        // Custom display event
        getContentPane ().addMouseListener ( new MouseAdapter ()
        {
            @Override
            public void mousePressed ( final MouseEvent e )
            {
                if ( SwingUtils.isMiddleMouseButton ( e ) )
                {
                    // Menu with custom elements
                    createMenu ().showMenu ( e.getComponent (), e.getPoint () );
                }
            }
        } );

        setLayout ( new FlowLayout ( FlowLayout.CENTER, 15, 15 ) );

        type = new WebComboBox ( DynamicMenuType.values (), DynamicMenuType.shutter );
        add ( new GroupPanel ( 5, new WebLabel ( "Display animation:" ), type ) );

        hidingType = new WebComboBox ( DynamicMenuType.values (), DynamicMenuType.shutter );
        add ( new GroupPanel ( 5, new WebLabel ( "Hide animation:" ), hidingType ) );

        radius = new WebTextField ( new IntTextDocument (), "70", 4 );
        add ( new GroupPanel ( 5, new WebLabel ( "Menu radius:" ), radius ) );

        amount = new WebTextField ( new IntTextDocument (), "5", 4 );
        add ( new GroupPanel ( 5, new WebLabel ( "Items amount:" ), amount ) );

        setDefaultCloseOperation ( WindowConstants.EXIT_ON_CLOSE );
        setSize ( 800, 600 );
        setLocationRelativeTo ( null );
        setVisible ( true );
    }

    protected WebDynamicMenu createMenu ()
    {
        final WebDynamicMenu menu = new WebDynamicMenu ();
        menu.setType ( ( DynamicMenuType ) type.getSelectedItem () );
        menu.setHideType ( ( DynamicMenuType ) hidingType.getSelectedItem () );
        menu.setRadius ( Integer.parseInt ( radius.getText () ) );
        menu.setFadeStepSize ( 0.07f );

        final int items = Integer.parseInt ( amount.getText () );
        for ( int i = 1; i <= items; i++ )
        {
            final ImageIcon icon = WebLookAndFeel.getIcon ( 24 );
            final ActionListener action = new ActionListener ()
            {
                @Override
                public void actionPerformed ( final ActionEvent e )
                {
                    System.out.println ( icon );
                }
            };
            final WebDynamicMenuItem item = new WebDynamicMenuItem ( icon, action );
            item.setMargin ( new Insets ( 8, 8, 8, 8 ) );
            menu.addItem ( item );
        }

        return menu;
    }

    public static void main ( final String[] args )
    {
        WebLookAndFeel.install ();
        new DynamicMenu ();
    }
}