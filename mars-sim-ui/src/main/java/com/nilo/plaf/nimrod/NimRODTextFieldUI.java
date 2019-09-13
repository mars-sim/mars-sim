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
 * Esta clase implementa los TextField.
 * Esta clase se usa desde un monton de sitios (Combos, PasswordField...), asi que extenderla
 * tiene resultados mas alla de los campos de texto.
 * @author Nilo J. Gonzalez
 */ 
 
package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;


public class NimRODTextFieldUI extends BasicTextFieldUI {
  private boolean rollover = false;
  private boolean focus = false;
  private MiTextML miTextML;
  
  protected boolean oldOpaque, canijo;
  
  NimRODTextFieldUI( JComponent c) {
    super();
  }

  public static ComponentUI createUI( JComponent c) {
    return new NimRODTextFieldUI( c);
  }
  
  protected void installDefaults() {
    super.installDefaults();
    
    oldOpaque = getComponent().isOpaque();
    getComponent().setOpaque( false);
  }

  protected void uninstallDefaults() {
    super.uninstallDefaults();
    
    getComponent().setOpaque( oldOpaque);
  }
  
  protected  void installListeners() {
    super.installListeners();

    miTextML = new MiTextML();
    getComponent().addMouseListener( miTextML);
    getComponent().addFocusListener( miTextML);
  }
  
  protected  void uninstallListeners() {
    super.uninstallListeners();

    getComponent().removeMouseListener( miTextML);
    getComponent().removeFocusListener( miTextML);
  }
  
  public boolean isFocus() {
    return focus;
  }

  public boolean isRollover() {
    return rollover;
  }
  
  protected void paintSafely( Graphics g) {
    paintFocus( g);
    
    paintTodo( g);
    
    super.paintSafely( g);
  }
  
  protected void paintTodo( Graphics g) {
    JTextComponent c = getComponent();

    Border bb = c.getBorder();
    
    if ( bb != null && bb instanceof NimRODBorders.NimRODGenBorder ) {
      Insets ins = NimRODBorders.getTextFieldBorder().getBorderInsets( c);
      
      // Si cabe todo, le ponemos un borde guay. Si no, pues le dejamos un borde cutrecillo
      if ( c.getSize().height+2 < (c.getFont().getSize() + ins.top + ins.bottom) ) {
        c.setBorder( NimRODBorders.getThinGenBorder());
        canijo = true;
      }
      else {
        c.setBorder( NimRODBorders.getTextFieldBorder());
        canijo = false;
      }
      
      if ( !c.isEditable() || !c.isEnabled() ) {
        g.setColor( UIManager.getColor( "TextField.inactiveBackground"));
      }
      else {
        g.setColor( c.getBackground());
      }
      
      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      g.fillRoundRect( 2,2, c.getWidth()-4, c.getHeight()-4, 7,7);
      
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
    else {
      super.paintBackground( g);   // TODO Esto puede que termine llamando al de abajo
    }
  }
  
  protected void paintBackground( Graphics g) {  // TODO esto estaba vacio, pero por el bug de faku igual termina teiendo codigo y todo
    JTextComponent c = getComponent();
    g.setColor( c.getBackground());
    
    g.fillRect( 0, 0, c.getWidth(), c.getHeight());
    System.out.println( "toy");
  }
  
  protected void paintFocus( Graphics g) {
    JTextComponent c = getComponent();
    
    if ( c.isEnabled() && c.isEditable() && !canijo ) {
      if ( focus ) {
        NimRODUtils.paintFocus( g, 1,1, c.getWidth()-2, c.getHeight()-2, 4,4, 3, NimRODLookAndFeel.getFocusColor());
      }
      else if ( rollover ) {
        NimRODUtils.paintFocus( g, 1,1, c.getWidth()-2, c.getHeight()-2, 4,4, 3, NimRODUtils.getColorAlfa( NimRODLookAndFeel.getFocusColor(), 150));
      }
    }
  }
  
  //////////////////////////
  
  class MiTextML extends MouseAdapter implements FocusListener {
    protected void refresh() {
      if ( getComponent().getParent() != null ) {
        Component papi = getComponent();

        papi.getParent().repaint( papi.getX()-5, papi.getY()-5, 
                                  papi.getWidth()+10, papi.getHeight()+10);
      }
    }
    
    public void mouseExited( MouseEvent e) {
      rollover = false;
      refresh();
    }
    
    public void mouseEntered( MouseEvent e) {
      rollover = true;
      refresh();
    }
    
    public void focusGained( FocusEvent e) {
      focus = true;
      refresh();
    }
      
    public void focusLost( FocusEvent e) {
      focus = false;
      refresh();
    }
  }
}
