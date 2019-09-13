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
 * Esta clase implementa los menus.
 * @author Nilo J. Gonzalez
 */ 

package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODPopupMenuUI extends BasicPopupMenuUI {
  private static Pantalla pantallas[] = null;
  
  
  private static Kernel kernel = null;
  private BufferedImage fondo = null;
  private BufferedImage blurFondo = null;
  private MiPL mipl;
  
  private static final int MATRIX = 3;
  
  public static ComponentUI createUI( JComponent c) {
    if ( pantallas == null ) {
      try {
        GraphicsDevice[] gda = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        
        pantallas = new Pantalla[ gda.length];
        
        for ( int i = 0; i < gda.length; i++) {
          pantallas[i] = new Pantalla( new Robot( gda[i]), gda[i].getDefaultConfiguration().getBounds());
        }
        
      } 
      catch ( Exception ex) {
        ex.printStackTrace();
      }
    }
    
    if ( kernel == null ) {
      float[] elements = new float[MATRIX*MATRIX];
      for ( int i = 0; i < elements.length; i++ ) {
        elements[i] = .1f;
      }
      int mid = MATRIX/2+1;
      elements[mid*mid] = .2f;
      
      kernel = new Kernel( MATRIX,MATRIX, elements);
    }
    
    return new NimRODPopupMenuUI();
  }
  
  public void installDefaults() {
    super.installDefaults();
    
    popupMenu.setBorder( NimRODBorders.getPopupMenuBorder());
    popupMenu.setOpaque( false);
  }
  
  public void uninstallDefaults() {
    super.uninstallDefaults();
    
    LookAndFeel.installBorder( popupMenu, "PopupMenu.border");
    popupMenu.setOpaque( true);
  }
  
  public void installListeners() {
    super.installListeners();

    mipl = new MiPL( popupMenu);
    popupMenu.addPopupMenuListener( mipl);
  }
  
  public void uninstallListeners() {
    super.uninstallListeners();

    popupMenu.removePopupMenuListener( mipl);
  }
  
  public void update( Graphics g, JComponent c) {
    if ( blurFondo != null ) {
      g.drawImage( blurFondo, 0, 0, null);
    }

    if ( NimRODUtils.getMenuOpacity() > 5 ) {
      Color cFondo = new Color( c.getBackground().getRed(), 
                                c.getBackground().getGreen(), 
                                c.getBackground().getBlue(),
                                NimRODUtils.getMenuOpacity());
      g.setColor( cFondo);
      g.fillRect( 0,0, c.getWidth()-4,c.getHeight()-4);
    }
  }
  
  /**
   * Este metodo esta aqui solo para **MINIMIZAR** el problema de usar la clase ROBOT. Esta clase tiene ciertas
   * restricciones de seguridad (a parte de que en el JDK de alguna distro de Linux de esas que van de guays el
   * programa nativo que hace el trabajo se instala sin permisos de ejecucion) que obligan a que el jar tenga
   * que ir firmado al usarse en applets.
   * Este metodo hace la llamada a la clase robot para capturar el fondo, y si salta una excepcion (en realidad
   * cualquier cosa pues se captura Throwable), se devuelve una imagen tan transparente como se le pida. Esto
   * se cargara el efecto de blur (blurrear algo liso es liso) de los menus, pero al menos habra cierta transparencia
   * y pintara una buena sombra si no se firma el applet o se usa una distro chapucera.
   * Por cierto, si el menu se sale de la ventana tendra un fondo opaco, y por tanto no habra transparencia y la
   * sombra quedara fatal
   * @param pop
   * @param rect
   * @param transparencia
   * @return
   */
  protected BufferedImage pillaFondo( JPopupMenu pop, Rectangle rect, int transparencia) {
    BufferedImage img =  null;
    
    try {
      Robot robot = null;
      
      for ( int i = 0; i < pantallas.length; i++) {
        if ( pantallas[i].rect.contains( rect.x, rect.y)) {
          robot = pantallas[i].robot;
          
          // Esto creaba artefactos en entornos de varias pantallas. La verdad es que no recuerdo porque
          // lo puse, pero lo quito el 02/12/2015 para solucionar un bug informado por Jose Antonio Perez Torne
          //rect.x -= pantallas[i].rect.x;
          //rect.y -= pantallas[i].rect.y;
        }
      }
      // Si llega aqui y ninguna pantalla sirve, robot=null y dara un NullPointerExcption, entrara en el catch y mostrara un 
      // fondo transparente sin blur igual que en los applets. 
      
      img = robot.createScreenCapture( rect);
    }
    catch ( Throwable ex) {
      img = new BufferedImage( rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
      Graphics g = img.getGraphics();
      g.setColor( NimRODUtils.getColorAlfa( pop.getBackground(), transparencia));
      g.fillRect( 0,0, rect.width,rect.height);
      g.dispose();
    } 
    
    return img;
  }

  public Popup getPopup( JPopupMenu pop, int x, int y) {
    Dimension dim = pop.getPreferredSize();
    
    if ( dim.width <= 0 || dim.height <= 0 ) {
      return super.getPopup( pop, x,y);
    }
    
    Rectangle rect = new Rectangle( x, y, dim.width, dim.height);
    fondo = pillaFondo( pop, rect, 0);
    
    if ( NimRODUtils.getMenuOpacity() > 250 ) {
      blurFondo = fondo;
    }
    else {
      Rectangle rectAmp = new Rectangle( x-MATRIX, y-MATRIX, dim.width+2*MATRIX, dim.height+2*MATRIX);
      
      BufferedImage clearFondo = pillaFondo( pop, rectAmp, NimRODUtils.getMenuOpacity());
      
      blurFondo = new BufferedImage( dim.width,dim.height, BufferedImage.TYPE_INT_ARGB);
      BufferedImage tempFondo = clearFondo.getSubimage( 0,0, clearFondo.getWidth(),clearFondo.getHeight());
      
      ConvolveOp cop = new ConvolveOp( kernel, ConvolveOp.EDGE_NO_OP, null);
      cop.filter( clearFondo, tempFondo);              // A ditorsionar
      cop.filter( tempFondo, clearFondo);              // A ditorsionar, otra vez
      cop.filter( clearFondo, tempFondo);              // A ditorsionar, y otra mas
      
      Graphics g =  blurFondo.getGraphics();
      g.drawImage( fondo, 0,0, null);
      g.drawImage( tempFondo.getSubimage( MATRIX,MATRIX, dim.width - 5, dim.height - 5), 
                   0,0, null);
    }

    return super.getPopup( pop, x,y);
  }

  /////////////////////////////////
  private class MiPL implements PopupMenuListener {
    JPopupMenu papi;
    
    public MiPL( JPopupMenu pop) {
      papi = pop;
    }
    
    public void popupMenuWillBecomeInvisible( PopupMenuEvent ev) {
      if ( fondo == null ) {
        return;
      }

      Graphics g = papi.getRootPane().getGraphics();
      
      Point p = papi.getLocationOnScreen();
      Point r = papi.getRootPane().getLocationOnScreen();
      
      g.drawImage( fondo, p.x - r.x, p.y - r.y, null);
      fondo = null;
    }

    public void popupMenuCanceled( PopupMenuEvent ev) {}
    public void popupMenuWillBecomeVisible( PopupMenuEvent ev) {}
  }
  
  
  static class Pantalla {
    Robot robot;
    Rectangle rect;
    
    public Pantalla( Robot rob, Rectangle r) {
      robot = rob;
      rect = r;
    }
  }
}
