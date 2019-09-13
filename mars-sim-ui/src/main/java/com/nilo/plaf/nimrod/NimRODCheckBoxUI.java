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
 * Esta clase implementa los CheckBox.
 * En realidad lo unico que hace es asociar los iconos que se usaran en cada estado, y el trabajo
 * duro lo hace la clase NimRODIconFactory. 
 * @author Nilo J. Gonzalez
 */
 
package com.nilo.plaf.nimrod;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class NimRODCheckBoxUI extends MetalCheckBoxUI {
//  protected MiML miml;
  boolean oldOpaque;
  
  public static ComponentUI createUI( JComponent c) {
    return new NimRODCheckBoxUI();
  }

//  public void installListeners( AbstractButton b) {
//    super.installListeners( b);
//    
//    //b.addMouseListener( new MiML( b));
//  }
//  
//  protected void uninstallListeners( AbstractButton b) {
//    super.uninstallListeners( b);
//    
//    //b.removeMouseListener( miml);
//  }
  
  public void installDefaults( AbstractButton b) {
    super.installDefaults( b);

    oldOpaque = b.isOpaque();
    b.setOpaque( false);
    
    icon = NimRODIconFactory.getCheckBoxIcon();
  } 
  
  public void uninstallDefaults( AbstractButton b) {
    super.uninstallDefaults( b);
    
    b.setOpaque( oldOpaque);
    icon = MetalIconFactory.getCheckBoxIcon();
  }
  
  public synchronized void paint( Graphics g, JComponent c) {
    // Si esta dentro de una tabla o una lista, hay que pintarle el fondo de forma expresa, porque al ser transparente, el CellRendererPane no lo pinta 
    if ( oldOpaque ) {
      Dimension size = c.getSize();
      Object papi = c.getParent();
      
      // Esto esta aqui para resolver un bug descubierto por Marcelo J. Ruiz que ocurre dentro de Netbeans
      if ( papi != null ) {
        if ( papi.getClass() == CellRendererPane.class ) {
          g.setColor( c.getBackground());
          g.fillRect( 0,0, size.width, size.height);
        }
        else if ( papi instanceof JTable ) {
          g.setColor( ((JTable)papi).getSelectionBackground());
          g.fillRect( 0,0, size.width, size.height);
        }
        else if ( papi instanceof JList ) {
          g.setColor( ((JList)papi).getSelectionBackground());
          g.fillRect( 0,0, size.width, size.height);
        }
      }
    }
        
    super.paint( g, c);
  }
  
  
  protected void paintFocus( Graphics g, Rectangle t, Dimension d){
    NimRODUtils.paintFocus( g, 1, 1, d.width-2, d.height-2, 8,8, NimRODLookAndFeel.getFocusColor());
  }
  
  /////////////////////////////////////
  
//  public class MiML extends MouseInputAdapter {
//    private AbstractButton papi;
//    
//    MiML( AbstractButton b) {
//      papi = b;
//    }
//    
//    void refresh() {
//      papi.getParent().repaint( papi.getX()-5, papi.getY()-5, 
//                                papi.getWidth()+10, papi.getHeight()+10);
//    }
//    
//    public void  mouseEntered( MouseEvent e) {
//      papi.getModel().setRollover( true);
//      refresh();
//    }
//
//    public void  mouseExited( MouseEvent e) {
//      papi.getModel().setRollover( false);
//      refresh();
//    }
//
//    public void mousePressed( MouseEvent e) {
//      refresh();
//    }
//    
//    public void mouseClicked( MouseEvent e) {
//      refresh();
//    }
//  }
}

