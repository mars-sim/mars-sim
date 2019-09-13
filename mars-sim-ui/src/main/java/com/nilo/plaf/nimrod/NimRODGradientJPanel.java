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

/**
 * Solo para hacer pruebas...
 * 
 * @author Nilo J. Gonzalez 2007
 */
class NimRODGradientJPanel extends JPanel implements SwingConstants {
  private static final long serialVersionUID = 3064942006323344159L;

  protected int direction;
  protected Color colIni, colFin;
  
  public NimRODGradientJPanel() {
    super();
    
    init();
  }

  public NimRODGradientJPanel( boolean isDoubleBuffered) {
    super( isDoubleBuffered);
    
    init();
  }

  public NimRODGradientJPanel( LayoutManager layout, boolean isDoubleBuffered) {
    super( layout, isDoubleBuffered);
    
    init();
  }

  public NimRODGradientJPanel( LayoutManager layout) {
    super( layout);
    
    init();
  }

  protected void init() {
    direction = VERTICAL;
    colIni = NimRODLookAndFeel.getControl();
    colFin = colIni.darker();
  }
  
  public void setGradientDirection( int dir) {
    direction = dir;
  }
  
  public void setGradientColors( Color ini, Color fin) {
    colIni = ini;
    colFin = fin;
  }
  
  protected void paintComponent( Graphics g) {
    Graphics2D g2D = (Graphics2D)g;
    GradientPaint grad = null;
    
    if ( direction == HORIZONTAL ) {
      grad = new GradientPaint( 0,0, colIni, 
                                getWidth(),0, colFin);
    }
    else {
      grad = new GradientPaint( 0,0, colIni, 
                                0,getHeight(), colFin);
    }
    
    g2D.setPaint( grad);
    g2D.fillRect( 0,0, getWidth(), getHeight());
  }
}
