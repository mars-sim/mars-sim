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
 * Esta clase implementa los botones de las barras de scroll.
 * @author Nilo J. Gonzalez
 */ 
 

package com.nilo.plaf.nimrod;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;

public class NimRODScrollButton extends MetalScrollButton {
  private static final long serialVersionUID = 1L;
  
  public NimRODScrollButton( int direction, int width, boolean freeStanding) {
    super( direction, width+1, freeStanding);
  }

  public void paint( Graphics g) {
    Rectangle rec = new Rectangle( 0,0, getWidth(),getHeight());
    
    Graphics2D g2D = (Graphics2D)g;
    GradientPaint grad = null;
    
    if (getDirection() == SwingConstants.EAST || getDirection() == SwingConstants.WEST) {
      if ( getModel().isPressed() || getModel().isSelected() ) {
        grad = new GradientPaint( 0,0, NimRODUtils.getSombra(), 
                                  0,rec.height, NimRODUtils.getBrillo());
      }
      else {
        grad = new GradientPaint( 0,0, NimRODUtils.getBrillo(), 
                                  0,rec.height, NimRODUtils.getSombra());
      }
    } 
    else {
      if ( getModel().isPressed() || getModel().isSelected() ) {
        grad = new GradientPaint( 0,0, NimRODUtils.getSombra(), 
                                  rec.width,0, NimRODUtils.getBrillo());
      }
      else {
        grad = new GradientPaint( 0,0, NimRODUtils.getBrillo(), 
                                  rec.width,0, NimRODUtils.getSombra());
      }
    }
    
    g2D.setColor( NimRODLookAndFeel.getControl());
    g2D.fillRect( rec.x, rec.y, rec.width, rec.height);
    
    g2D.setPaint( grad);
    g2D.fillRect( rec.x, rec.y, rec.width, rec.height);
    
    if ( getModel().isRollover() ) {
      g2D.setColor( NimRODUtils.getRolloverColor());
      g2D.fillRect( rec.x, rec.y, rec.width, rec.height);
    }

    g2D.setColor( NimRODLookAndFeel.getControlDarkShadow());
    g2D.drawRect( rec.x, rec.y, rec.width-1, rec.height-1);
    
    Icon icon = null;
    switch ( getDirection() ) {
      case SwingConstants.EAST :  icon = UIManager.getIcon( "ScrollBar.eastButtonIcon"); break;
      case SwingConstants.WEST :  icon = UIManager.getIcon( "ScrollBar.westButtonIcon"); break;
      case SwingConstants.NORTH : icon = UIManager.getIcon( "ScrollBar.northButtonIcon"); break;
      case SwingConstants.SOUTH : icon = UIManager.getIcon( "ScrollBar.southButtonIcon"); break;
    }
    icon.paintIcon( this, g2D, rec.x, rec.y);
  }
}
