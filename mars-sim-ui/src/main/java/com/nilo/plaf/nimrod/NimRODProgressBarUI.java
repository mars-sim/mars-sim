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
 * Esta clase implementa las barras de progreso.
 * @author Nilo J. Gonzalez
 */ 
 

package com.nilo.plaf.nimrod;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODProgressBarUI extends BasicProgressBarUI {
	public static ComponentUI createUI( JComponent c) {
    return new NimRODProgressBarUI();
  }
	
  public void paintDeterminate( Graphics g, JComponent c) {
	  Graphics2D g2D = (Graphics2D)g;
	  
    Insets b = progressBar.getInsets();
    int largo = progressBar.getWidth() - (b.left + b.right);
	  int alto = progressBar.getHeight() - (b.top + b.bottom);
	  int len = getAmountFull(b, largo, alto);
    
    int xi = b.left;
    int yi = b.top;
    int xf = xi + largo;
    int yf = yi + alto;
    int xm = xi + len - 1;
    int ym = yf - len ;
    
    if ( progressBar.getOrientation() == JProgressBar.HORIZONTAL ) {
      g2D.setColor( progressBar.getForeground());
      g2D.fillRect( xi,yi, xm,yf);
      
      GradientPaint grad = new GradientPaint( xi,yi, NimRODUtils.getBrillo(), 
                                              xi,yf, NimRODUtils.getSombra());
      g2D.setPaint( grad);
      g2D.fillRect( xi,yi, xm,yf);
      
    	grad = new GradientPaint( xm+1,yi, NimRODUtils.getSombra(), 
                                xm+1,yf, NimRODUtils.getBrillo());
  		g2D.setPaint( grad);
      g2D.fillRect( xm+1,yi, xf,yf);
  	}
  	else {
      g2D.setColor( progressBar.getForeground());
      g2D.fillRect( xi,ym, xf,yf);
      
      GradientPaint grad = new GradientPaint( xi,yi, NimRODUtils.getSombra(), 
                                              xf,yi, NimRODUtils.getBrillo());
  		g2D.setPaint( grad);
      g2D.fillRect( xi,yi, xf,ym);
      
      grad = new GradientPaint( xi,ym, NimRODUtils.getBrillo(), 
                                xf,ym, NimRODUtils.getSombra());
      g2D.setPaint( grad);
      g2D.fillRect( xi,ym, xf,yf);
    }
    
    paintString(g, 0,0,0,0,0, b);
	}
	
	public void paintIndeterminate( Graphics g, JComponent c) {
	  Graphics2D g2D = (Graphics2D)g;
	  
	  Rectangle rec = new Rectangle();
    rec = getBox( rec);
    
    Insets b = progressBar.getInsets();
    int xi = b.left;
    int yi = b.top;
    int xf = c.getWidth() - b.right;
    int yf = c.getHeight() - b.bottom;
    
    g2D.setColor( progressBar.getForeground());
    g2D.fillRect( rec.x, rec.y, rec.width, rec.height);
    
    if ( progressBar.getOrientation() == JProgressBar.HORIZONTAL ) {
      GradientPaint grad = new GradientPaint( rec.x,rec.y, NimRODUtils.getBrillo(), 
                                              rec.x,rec.height, NimRODUtils.getSombra());
      g2D.setPaint( grad);
      g2D.fill( rec);
      
      grad = new GradientPaint( xi,yi, NimRODUtils.getSombra(), 
                                xi,yf, NimRODUtils.getBrillo());
  		g2D.setPaint( grad);
      g2D.fillRect( xi,yi, rec.x,yf);
      g2D.fillRect( rec.x + rec.width,yi, xf,yf);
    }
    else {
      GradientPaint grad = new GradientPaint( rec.x,rec.y, NimRODUtils.getBrillo(), 
                                              rec.width,rec.y, NimRODUtils.getSombra());
      g2D.setPaint( grad);
      g2D.fill( rec);

      
      grad = new GradientPaint( xi,yi, NimRODUtils.getSombra(), 
                                xf,yi, NimRODUtils.getBrillo());
  		g2D.setPaint( grad);
      g2D.fillRect( xi,yi, xf,rec.y);
      g2D.fillRect( xi,rec.y+rec.height, xf,yf);
    }
    
    paintString( g2D, 0,0,0,0, 0, b);
  }
  
  protected void paintString( Graphics g, int x, int y, int width, int height, int amountFull, Insets b) {
    if ( !progressBar.isStringPainted()) {
      return;
    }
    
    String text = progressBar.getString();
    
    Point point = getStringPlacement( g, text, b.left, b.top, 
                                               progressBar.getWidth() - b.left - b.right, 
                                               progressBar.getHeight() - b.top - b.bottom);
    g.setFont( progressBar.getFont().deriveFont( Font.BOLD));
    
    if ( progressBar.getOrientation() == JProgressBar.HORIZONTAL ) {
      if ( !progressBar.getComponentOrientation().isLeftToRight() ) {
        point.x += progressBar.getFontMetrics( g.getFont()).stringWidth( text);
      }
    }
    
    
    NimRODUtils.paintShadowTitle( g, text, point.x, point.y, Color.white, Color.black, 1, 
                                        NimRODUtils.FAT, progressBar.getOrientation());
  }
  
}