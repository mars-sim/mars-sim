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
 * Esta clase implementa los RadioButtons.
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

public class NimRODRadioButtonUI extends MetalRadioButtonUI {
  //protected MiML miml;
  boolean oldOpaque;
  
  public void installDefaults( AbstractButton b) {
    super.installDefaults( b);

    oldOpaque = b.isOpaque();
    b.setOpaque( false);
    
    icon = NimRODIconFactory.getRadioButtonIcon();
  }  
  
  public void uninstallDefaults( AbstractButton b) {
    super.uninstallDefaults( b);
    
    b.setOpaque( oldOpaque);
    icon = MetalIconFactory.getRadioButtonIcon();
  }
  
//  public void installListeners( AbstractButton b) {
//    super.installListeners( b);
//    
//    //miml = new MiML( b);
//    //b.addMouseListener( miml);
//  }
  
  public static ComponentUI createUI( JComponent c) {
    return new NimRODRadioButtonUI();
  }
  
//  public synchronized void paint( Graphics g, JComponent c) {
//    super.paint( g, c);
//    
//    //ButtonModel abs = ((JRadioButton)c).getModel();
//    //if ( !c.hasFocus() && abs.isRollover() ) {
//      // No queda del todo bien...
//      //NimRODUtils.paintFocus( g, 1, 1, c.getWidth()-2, c.getHeight()-2, 8,8, NimRODUtils.getColorAlfa( NimRODLookAndFeel.getFocusColor(), 150));
//    //}
//  }
  
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
//  }  
}
