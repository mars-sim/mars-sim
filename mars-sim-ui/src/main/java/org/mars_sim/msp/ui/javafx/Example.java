/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mars_sim.msp.ui.javafx;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.window.TestFrame;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.separator.WebSeparator;
import com.alee.laf.text.WebTextArea;
import com.alee.managers.UIManagers;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.hotkey.HotkeyRunnable;
import com.alee.managers.style.CustomSkin;
import com.alee.managers.style.StyleManager;

import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

/**
 * @author Mikle Garin
 */

public class Example
{
    public static void main ( final String[] args )
    {
    	
    	SwingUtilities.invokeLater(() ->  {
    		
	        WebLookAndFeel.install();
	        UIManagers.initialize();
		    
	        final CustomSkin defaultSkin = new CustomSkin ( Example.class, "ExampleSkin.xml" );
	        final CustomSkin darkSkin = new CustomSkin ( Example.class, "DarkExampleSkin.xml" );
	        StyleManager.setSkin ( defaultSkin );
	        HotkeyManager.registerHotkey ( Hotkey.CTRL_SPACE, new HotkeyRunnable ()
	        {
	            @Override
	            public void run ( final KeyEvent e )
	            {
	                StyleManager.setSkin ( StyleManager.getSkin () == defaultSkin ? darkSkin : defaultSkin );
	            }
	        } );
	
	        final WebPanel panel = new WebPanel ( ExampleStyles.shaded, new VerticalFlowLayout ( true, true ) );
	
	        final WebLabel title = new WebLabel ( ExampleStyles.title.at ( panel ), "Panel Title" );
	        panel.add ( title );
	
	        final WebSeparator separator = new WebSeparator ( ExampleStyles.line.at ( panel ) );
	        panel.add ( separator );
	
	        final WebScrollPane scrollPane = new WebScrollPane ( ExampleStyles.scroll.at ( panel ) );
	        scrollPane.setHorizontalScrollBarPolicy ( WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
	        scrollPane.getViewport ().setView ( new WebTextArea ( ExampleStyles.text.at ( scrollPane ), 3, 20 ) );
	        panel.add ( scrollPane );
	
	        TestFrame.show ( panel );
	        
    	});

    }
}