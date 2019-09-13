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
 * Esta clase implementa los campos de password.
 * Esta clase cambia los asteriscos habituales por unos cuadrados con bordes redondeados 
 * @see NimRODPasswordFieldUI
 * @author Nilo J. Gonzalez
 */ 
 
package com.nilo.plaf.nimrod;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;


public class NimRODPasswordView extends PasswordView {
  protected static int ancho = 9;
  protected static int hueco = 3;
  
  public NimRODPasswordView( Element elem) {
    super( elem);
  }
  
  protected int drawEchoCharacter( Graphics g, int x, int y, char c) {
    int w = getFontMetrics().charWidth( c);
    w = ( w < ancho ? ancho : w);
    int h = (getContainer().getHeight() - ancho) / 2;
    
    Graphics2D g2D = (Graphics2D)g;
    g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    g2D.fillOval( x, h+1, w, w);
    
    g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    
    return x + w + hueco;
  }
  
  public Shape modelToView( int pos, Shape a, Position.Bias b) throws BadLocationException {
  	Container c = getContainer();
  	if ( c instanceof JPasswordField ) {
  	  JPasswordField f = (JPasswordField)c;
  	  if ( !f.echoCharIsSet() ) {
  		  return super.modelToView( pos, a, b);
  	  }
  	    
      char echoChar = f.getEchoChar();
  	  int w = f.getFontMetrics( f.getFont()).charWidth( echoChar);
  	  w = ( w < ancho ? ancho : w) + hueco;
  	  
  	  Rectangle alloc = adjustAllocation( a).getBounds();
  	  int dx = (pos - getStartOffset()) * w;
  	  alloc.x += dx - 2;
      if ( alloc.x <= 5 ) {
        alloc.x = 6;
      }
  	  alloc.width = 1;
  	  
  	  return alloc;
  	}
  	
  	return null;
  }
  
  public int viewToModel( float fx, float fy, Shape a, Position.Bias[] bias) {
	  bias[0] = Position.Bias.Forward;
	  int n = 0;
	  Container c = getContainer();
	  if ( c instanceof JPasswordField ) {
	    JPasswordField f = (JPasswordField)c;
	    if ( !f.echoCharIsSet() ) {
		    return super.viewToModel( fx, fy, a, bias);
	    }
	    
	    char echoChar = f.getEchoChar();
	    int w = f.getFontMetrics( f.getFont()).charWidth( echoChar);
  	  w = ( w < ancho ? ancho : w) + hueco;
  	  
	    a = adjustAllocation( a);
	    Rectangle alloc = (a instanceof Rectangle) ? (Rectangle)a : a.getBounds();
	    n = ((int)fx - alloc.x) / w;
	    if (n < 0) {
		    n = 0;
	    }
	    else if ( n > (getStartOffset() + getDocument().getLength()) ) {
		    n = getDocument().getLength() - getStartOffset();
	    }
	  }
	  
    return getStartOffset() + n;
  }
}
