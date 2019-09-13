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
import javax.swing.border.Border;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class NimRODToolTipUI extends MetalToolTipUI {
  protected JToolTip tooltip;
  
  public NimRODToolTipUI( JComponent c) {
    super();
    
    tooltip = (JToolTip)c;
    tooltip.setOpaque( false);
  }
  
  public static ComponentUI createUI( JComponent c) {
    return new NimRODToolTipUI( c);
  }
  
  public void paint( Graphics g, JComponent c) {
    int w = tooltip.getWidth();
    int h = tooltip.getHeight();
    
    Border bb = tooltip.getBorder();
    if ( bb != null ) {
      w -= bb.getBorderInsets( tooltip).right;
      h -= bb.getBorderInsets( tooltip).bottom;
    }
    
    g.setColor( tooltip.getBackground());
    g.fillRect( 0,0, w,h);

    super.paint( g, c);
  }
}

