/*
 *                 (C) Copyright 2005 Nilo J. Gonzalez
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Gereral Public Licence as published by the Free
 * Software Foundation; either version 2 of the Licence, or (at your opinion) any
 * later version.
 * 
 * This library is distributed in the hope that it will be usefull, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of merchantability or fitness for a
 * particular purpose. See the GNU Lesser General Public Licence for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public Licence along
 * with this library; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, Ma 02111-1307 USA.
 *
 * http://www.gnu.org/licenses/lgpl.html (English)
 * http://gugs.sindominio.net/gnu-gpl/lgpl-es.html (Español)
 *
 *
 * Original author: Nilo J. Gonzalez
 */

package com.nilo.plaf.nimrod;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODSpinnerUI extends BasicSpinnerUI {
  protected boolean oldOpaque;
  
  public static ComponentUI createUI( JComponent c) {
    return new NimRODSpinnerUI();
  }
  
  protected void installDefaults() {
    super.installDefaults();

    oldOpaque = spinner.isOpaque();
    spinner.setOpaque( false);
  }
  
  protected void uninstallDefaults() {
    super.uninstallDefaults();
    
    spinner.setOpaque( oldOpaque);
  }
  
  protected Component createPreviousButton() {
    Component c = createArrowButton( SwingConstants.SOUTH);
    c.setName( "Spinner.previousButton");
    installPreviousButtonListeners(c);
    
    return c;
  }
  
  protected Component createNextButton() {
    Component c = createArrowButton( SwingConstants.NORTH);
    c.setName( "Spinner.nextButton");
    installNextButtonListeners(c);
    
    return c;
  }
  
  private Component createArrowButton( int direction) {
    JButton b = new NimRODArrowButton(direction);

    b.setInheritsPopupMenu( true);
    
    return b;
  }
  
  
  public void paint( Graphics g, JComponent c) {
    super.paint( g, c);
    
    g.setColor( c.getBackground());
    g.fillRect( 2,3, c.getWidth()-4, c.getHeight()-6);
    g.drawLine( 3,2, c.getWidth()-4, 2);
    g.drawLine( 3,c.getHeight()-3, c.getWidth()-4, c.getHeight()-3);
    
    JComponent editor = spinner.getEditor();
    if ( editor instanceof JSpinner.DefaultEditor ) {
      JTextField text = ((JSpinner.DefaultEditor)editor).getTextField();
      text.setBackground( c.getBackground());
    }
  }


  /****************************************************************************************/
  class NimRODArrowButton extends JButton {
    private static final long serialVersionUID = 3031842923932443184L;

    private int dir;
    
    public NimRODArrowButton( int direction) {
      super();
    
      setRequestFocusEnabled( false);
      
      dir = direction;
      if ( direction == SwingConstants.NORTH ) {
        setIcon( UIManager.getIcon( "Spinner.nextIcon"));
      }
      else {
        setIcon( UIManager.getIcon( "Spinner.previousIcon"));
      }
      
      setOpaque( false);
    }
    
    // Esto esta aqui gracias a Christopher J. Huey, que no solo hizo pruebas y mas pruebas, hasta un nivel al que ni yo
    // ni nadie habia llegado, ¡¡si no que encima el tio va y manda parches!! La verdad es que no se como agradecer 
    // estas cosas...
    // This is a fix from Christopher J. Huey 
    public boolean isFocusTraversable() {
      return false;
    }
    
    public Dimension getPreferredSize() {
      return new Dimension( 15, getIcon().getIconHeight());
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public void paint( Graphics g) {
      Icon icon = getIcon();
      
      int w = getWidth()-1;
      int h = getHeight()-1;
      
      int x = (w-icon.getIconWidth())/2;
      int y = (h-icon.getIconHeight())/2;
     
      int yf = y;
      
      Border bb = ((JSpinner)getParent()).getBorder();
      if ( bb != null ) {
        if ( dir == SwingConstants.NORTH ) {
          y += bb.getBorderInsets( this).top / 2;
          yf = bb.getBorderInsets( this).top - 2;
        }
        else {
          yf = getHeight() - bb.getBorderInsets( this).bottom;
        }
        
        w -= 3;
      }

      icon.paintIcon( this, g, x, y);
      
      if ( dir == SwingConstants.NORTH ) {
        g.setColor( NimRODUtils.getBrillo());
        g.drawLine( 1,yf, 1,h);
        
        g.setColor( NimRODUtils.getSombra());
        g.drawLine( 1, h, w, h);
        g.drawLine( 0,yf, 0,h);
      }
      else {
        g.setColor( NimRODUtils.getBrillo());
        g.drawLine( 1,0, w,0);
        g.drawLine( 1,0, 1,yf);
        
        g.setColor( NimRODUtils.getSombra());
        g.drawLine( 0,0, 0,yf);
      }
    }
  }
}
