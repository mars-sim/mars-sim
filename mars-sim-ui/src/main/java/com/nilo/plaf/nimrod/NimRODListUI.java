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
 * Esta clase implementa las listas.
 * @author Nilo J. Gonzalez
 */ 

package com.nilo.plaf.nimrod;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODListUI extends BasicListUI {
  public NimRODListUI( JComponent list) {
    super();
  }
  
  public static ComponentUI createUI( JComponent list) {
    return new NimRODListUI( list);
  }
  
  protected void paintCell( Graphics g, int row, Rectangle rowBounds, ListCellRenderer cellRenderer,
                            ListModel dataModel, ListSelectionModel selModel, int leadIndex) {
    
    rowBounds.x += 1;
    super.paintCell( g, row, rowBounds, cellRenderer, dataModel, selModel, leadIndex);
    rowBounds.x -= 1;
    
    if ( list.isSelectedIndex( row) ) {
      Color oldColor = g.getColor();
      
      g.translate( rowBounds.x, rowBounds.y);
      
      GradientPaint grad = new GradientPaint( 0,0, NimRODUtils.getBrillo(), 
                                              0,rowBounds.height, NimRODUtils.getSombra());
      Color bgColor = NimRODLookAndFeel.getMenuSelectedBackground();  
        
      Graphics2D g2D = (Graphics2D)g;
      g2D.setPaint( grad);
      g2D.fillRect( 0,0, rowBounds.width-1, rowBounds.height);
      
      g.setColor( bgColor.darker());
      g.drawRect( 0,0, rowBounds.width-1, rowBounds.height-1);
      
      g.translate( -rowBounds.x, -rowBounds.y);
      g.setColor( oldColor);
    }
  }
}
