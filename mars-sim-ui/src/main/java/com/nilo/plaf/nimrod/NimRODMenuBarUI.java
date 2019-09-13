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


/**
 * Esta clase implementa las barras de menu.
 * @author Nilo J. Gonzalez
 */ 
 
package com.nilo.plaf.nimrod;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;


public class NimRODMenuBarUI extends BasicMenuBarUI {
  public static ComponentUI createUI( JComponent x) {
	  return new NimRODMenuBarUI();
  }

  public void paint( Graphics g, JComponent c) {
    super.paint( g, c);
    
    /*
    GradientPaint grad = new GradientPaint( 0,0, NimRODUtils.getBrillo(), 
                                            0,c.getBounds().height, NimRODUtils.getSombra());

    Graphics2D g2D = (Graphics2D)g;
    g2D.setPaint( grad);
    g2D.fillRect( 0,0, c.getBounds().width-1, c.getBounds().height-1);
    */
  }
}

