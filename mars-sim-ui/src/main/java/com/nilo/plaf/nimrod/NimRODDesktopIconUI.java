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
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODDesktopIconUI extends BasicDesktopIconUI {
  boolean hasFocus;
  
  private int width = UIManager.getInt( "NimRODDesktopIcon.width");
  private int height = UIManager.getInt( "NimRODDesktopIcon.height");
  
  private int bigWidth = UIManager.getInt( "NimRODDesktopIconBig.width");
  private int bigHeight = UIManager.getInt( "NimRODDesktopIconBig.height");
  
  private HackML hackML;
  private Icon resizeIcon, antIcon;

  public static ComponentUI createUI( JComponent c) {
    return new NimRODDesktopIconUI();
  }

  public NimRODDesktopIconUI() {
    super();
    
    hackML = new HackML();
  }

  protected void installDefaults() {
    super.installDefaults();
    
    LookAndFeel.uninstallBorder( desktopIcon);
  }

  protected void installComponents() {}
  
  protected void uninstallComponents() {}

  protected void installListeners() {
    super.installListeners();
    
    if ( frame != null ) {
      desktopIcon.addMouseListener( hackML);
      desktopIcon.addMouseMotionListener( hackML);
    }
  }

  protected void uninstallListeners() {
    super.uninstallListeners();
    
    desktopIcon.removeMouseListener( hackML);
    desktopIcon.removeMouseMotionListener( hackML);
  }
  
  public void update( Graphics g, JComponent c) {
    paint( g, c);
  }
  
  public void paint( Graphics g, JComponent c) {
    if ( frame.getFrameIcon() != antIcon ) {
      antIcon = frame.getFrameIcon();
      resizeIcon = NimRODUtils.reescala( antIcon, bigWidth, bigHeight);
    }
    String title = frame.getTitle();
    
    int x = 0;
    if ( resizeIcon != null ) { 
      x = ( width - resizeIcon.getIconWidth() ) / 2;
      resizeIcon.paintIcon( c, g, x, 2);
    }

    g.setFont( UIManager.getFont( "DesktopIcon.font"));
    FontMetrics fm = g.getFontMetrics();
    
    if ( hasFocus ) {
      int y = 0;
      String auxTit = getTitle( title, fm, width-10);    // Los anglos se mearan de risa al ver el nombre de esta variable...
      while ( auxTit.length() > 0 ) {
        if ( auxTit.endsWith( "...") ) {
          auxTit = auxTit.substring( 0, auxTit.length()-3);
        }
        
        Rectangle2D rect = fm.getStringBounds( auxTit, g);
        x = (int)( width - rect.getWidth() ) / 2;
        y += rect.getHeight();
        
        NimRODUtils.paintShadowTitleFat( g, auxTit, x, y, Color.white);
        
        title = title.substring( auxTit.length());
        auxTit = getTitle( title, fm, width-10);
      }
    }
    else {
      title = getTitle( title, fm, width-10);
      Rectangle2D rect = fm.getStringBounds( title, g);
      x = (int)( width - rect.getWidth() ) / 2;
      NimRODUtils.paintShadowTitleFat( g, title, x, height-NimRODUtils.MATRIX_FAT, Color.white);
    }
  }

  protected String getTitle( String title, FontMetrics fm, int len) {
    if ( title == null || title.equals( "") ) {
      return "";
    }
    
    int lTit = fm.stringWidth( title);
    if ( lTit <= len ) {
      return title;
    }
    
    int lPuntos = fm.stringWidth( "...");
    if ( len - lPuntos <= 0 ) {
      return "";
    }
    
    int i = 1;
    do {
      String aux = title.substring( 0, i++) + "...";
      lPuntos = fm.stringWidth( aux);
    } while ( lPuntos < len);
    
    return title.substring( 0, i-1) + "...";
  }

  public Dimension getPreferredSize( JComponent c) {
    return getMinimumSize( c);
  }

  public Dimension getMaximumSize( JComponent c) {
    return getMinimumSize( c);
  }
  
  public Dimension getMinimumSize( JComponent c) {
    return new Dimension( width, height);
  }

  //******************************+
  private class HackML extends MouseInputAdapter {
    public void mouseReleased( MouseEvent ev) {
      dodo( ev);
    }
    
    public void mousePressed( MouseEvent ev) {
      dodo( ev);
    }
    
    public void mouseExited( MouseEvent ev) {
      hasFocus = false;
      dodo( ev);
    }
    
    public void mouseEntered( MouseEvent ev) {
      hasFocus = true;
      dodo( ev);
    }
    
    public void mouseDragged( MouseEvent ev) {
      dodo( ev);
    }
    
    void dodo( MouseEvent ev) {
      if ( desktopIcon != null ) { 
        desktopIcon.getDesktopPane().updateUI();
      }
    }
  }
}
