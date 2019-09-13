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
 * Esta clase implementa los JTabbedPane.
 * Practicamente todo el esfuerzo se centra en pintar la pestaña.
 * @author Nilo J. Gonzalez
 */ 

package com.nilo.plaf.nimrod;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODTabbedPaneUI extends BasicTabbedPaneUI {
  private Color selectColor;
  private int inclTab = 12;
  //private int anchoFocoV = inclTab;
  private int anchoFocoH = 0;
  private int anchoCarpetas = 18;
  private int rollover = -1;
  private int antRollover = -1;
  
  private MiML miml;
  /**
   * En este poligono se guarda la forma de la pestaña. Es muy importante. 
   */
  private Polygon shape;

  
  public static ComponentUI createUI( JComponent c) {
    return new NimRODTabbedPaneUI();
  }

  protected void installDefaults() {
    super.installDefaults();
    
    rollover = -1;
    selectColor = NimRODLookAndFeel.getFocusColor();
    tabAreaInsets.right = anchoCarpetas;
  }
  
  protected  void installListeners() {
    super.installListeners();

    miml = new MiML();
    tabPane.addMouseMotionListener( miml);
    tabPane.addMouseListener( miml);
  }

  protected void uninstallListeners() {
    super.uninstallListeners();
    
    tabPane.removeMouseMotionListener( miml);
    tabPane.removeMouseListener( miml);
  }
  
  protected void layoutLabel( int tabPlacement, FontMetrics metrics, int tabIndex,
                              String title, Icon icon, Rectangle tabRect, Rectangle iconRect,
                              Rectangle textRect, boolean isSelected) {
    Rectangle tabRectPeq = new Rectangle( tabRect);
    tabRectPeq.width -= inclTab;
    super.layoutLabel( tabPlacement, metrics, tabIndex, title, icon, tabRectPeq, iconRect,
                       textRect, isSelected);
  }
  
  protected void paintTabArea( Graphics g, int tabPlacement, int selectedIndex) {
    if ( runCount > 1 ) {
      int lines[] = new int[runCount];
      for ( int i = 0; i < runCount; i++) {
        lines[i] = rects[ tabRuns[i]].y + ( tabPlacement == TOP ? maxTabHeight : 0);
      }
      
      Arrays.sort( lines);
      
      Graphics2D g2D = (Graphics2D)g;
      g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      if ( tabPlacement == TOP ) {
        int fila = runCount;
        for ( int i = 0; i < lines.length-1; i++, fila--) {
          Polygon carp = new Polygon();
          carp.addPoint( 0, lines[i]);
          carp.addPoint( tabPane.getWidth()-2*fila-2, lines[i]);
          carp.addPoint( tabPane.getWidth()-2*fila, lines[i]+3);
  
          if ( i < lines.length-2 ) {
            carp.addPoint( tabPane.getWidth()-2*fila, lines[i+1]);
            carp.addPoint( 0, lines[i+1]);
          }
          else {
            carp.addPoint( tabPane.getWidth()-2*fila, lines[i] + rects[selectedIndex].height);
            carp.addPoint( 0, lines[i] + rects[selectedIndex].height);
          }
          
          carp.addPoint( 0, lines[i]);
          
          g2D.setColor( hazAlfa( fila));
          g2D.fillPolygon( carp);
          
          g2D.setColor( darkShadow.darker());
          g2D.drawPolygon( carp);
        }
      }
      else {
        int fila = 0;
        for ( int i = 0; i < lines.length-1; i++, fila++) {
          Polygon carp = new Polygon();
          carp.addPoint( 0, lines[i]);
          carp.addPoint( tabPane.getWidth()-2*fila-1, lines[i]);
          
          carp.addPoint( tabPane.getWidth()-2*fila-1, lines[i+1]-3);
          carp.addPoint( tabPane.getWidth()-2*fila-3, lines[i+1]);
          carp.addPoint( 0, lines[i+1]);
          
          carp.addPoint( 0, lines[i]);
          
          g2D.setColor( hazAlfa( fila+2));
          g2D.fillPolygon( carp);
          
          g2D.setColor( darkShadow.darker());
          g2D.drawPolygon( carp);
        }
      }
      
      g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
    
    super.paintTabArea( g, tabPlacement, selectedIndex);
  }

  
  protected void paintTabBackground( Graphics g, int tabPlacement,
                                     int tabIndex,
                                     int x, int y, int w, int h,
                                     boolean isSelected ) {
    // Este es el primer metodo al que se llama, asi que aqui preparamos el shape que dibujara despues todo...
    Graphics2D g2D = (Graphics2D)g;
    g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    GradientPaint gradientShadow;

    int xp[] = null;  // Para la forma
    int yp[] = null;
    switch( tabPlacement ) {
      case LEFT:
        xp = new int[]{ x, x,     x+w,   x+w, x};
        yp = new int[]{ y, y+h-3, y+h-3, y,   y};
        gradientShadow = new GradientPaint( x, y, NimRODUtils.getBrillo(),
                                            x, y+h, NimRODUtils.getSombra());
        break;
      case RIGHT:
        
        xp = new int[]{ x, x,     x+w-2, x+w-2, x};
        yp = new int[]{ y, y+h-3, y+h-3, y,     y};
        gradientShadow = new GradientPaint( x, y, NimRODUtils.getBrillo(),
                                            x, y+h, NimRODUtils.getSombra());
        break;
      case BOTTOM:
        xp = new int[]{ x, x,     x+3, x+w-inclTab-6, x+w-inclTab-2, x+w-inclTab, x+w-3, x};
        yp = new int[]{ y, y+h-3, y+h, y+h,           y+h-1,         y+h-3,       y,     y};
        gradientShadow = new GradientPaint( x, y, NimRODUtils.getBrillo(),
                                            x, y+h, NimRODUtils.getSombra());
        break;
      case TOP:
      default:
        xp = new int[]{ x,   x,   x+3, x+w-inclTab-6, x+w-inclTab-2, x+w-inclTab, x+w, x};
        yp = new int[]{ y+h, y+3, y,   y,             y+1,           y+3,         y+h, y+h};
        gradientShadow = new GradientPaint( x, y, NimRODUtils.getBrillo(),
                                            x, y+h, NimRODUtils.getSombra());
        break;
    };

    shape = new Polygon( xp, yp, xp.length);

    // Despues ponemos el color que toque    
    if ( isSelected ) {
      g2D.setColor( selectColor );
    }
    else {
      g2D.setColor( tabPane.getBackgroundAt( tabIndex));
    }
    
    // Encima, pintamos la pestaña con el color que sea
    g2D.fill( shape);
    
    // Encima, pintamos la pestaña con el color que le corresponde por profundidad
    if ( runCount > 1 ) {
      g2D.setColor( hazAlfa( getRunForTab( tabPane.getTabCount(), tabIndex)-1));
      g2D.fill( shape);
    }
    
    // Encima, pintamos un colorin si el raton esta por encima
    if ( tabIndex == rollover ) {
      g2D.setColor( NimRODUtils.getRolloverColor());
      g2D.fill( shape);
    }
    
    // Y despues, le damos un sombreado que hace que parezca curbada (¿A que duele ver algunas faltas de ortografia?)
    g2D.setPaint( gradientShadow);
    g2D.fill( shape);
    
    // Y al final le pintamos un bordecito para definir mejor la pestaña
    g2D.setColor( NimRODUtils.getSombra());
    g2D.draw( shape);
    
    g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
  }

   /**
    * Este metodo devuelve un tamaño mas grande de lo necesario, haciendoer hueco para
    * la decoracion.
    */
  protected int calculateTabWidth( int tabPlacement, int tabIndex, FontMetrics metrics) {
    return 8 + inclTab + super.calculateTabWidth( tabPlacement, tabIndex, metrics);
  }

   /**
    * Este metodo devuelve un tamaño mas grande de lo necesario, haciendo el hueco para
    * la decoracion.
    */
  protected int calculateTabHeight( int tabPlacement, int tabIndex, int fontHeight) {
    if ( tabPlacement == LEFT || tabPlacement == RIGHT ) {
      return super.calculateTabHeight( tabPlacement, tabIndex, fontHeight);
    }
    else {
      return anchoFocoH + super.calculateTabHeight( tabPlacement, tabIndex, fontHeight);
    }
  }

   /**
    * Este metodo dibuja el borde.
    */
  protected void paintTabBorder( Graphics g, int tabPlacement, int tabIndex,
                                 int x, int y, int w, int h, boolean isSelected) {
  }

   /**
    * Este metodo dibuja una señal amarilla en la solapa que tiene el foco
    */
  protected void paintFocusIndicator( Graphics g, int tabPlacement,
                                      Rectangle[] rects, int tabIndex,
                                      Rectangle iconRect, Rectangle textRect,
                                      boolean isSelected) {
    if ( tabPane.hasFocus() && isSelected) {
      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Stroke oldStroke = g2d.getStroke();
      g2d.setStroke( new BasicStroke( 2.0f));
      g2d.setColor( UIManager.getColor( "ScrollBar.thumbShadow"));
      g2d.drawPolygon( shape);
      g2d.setStroke( oldStroke);
      g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
  }

  /**
   * Esta funcion devuelve una sombra mas opaca cuanto mas arriba este la fila. 
   * A partir de valores de fila superiores a 7 siempre devuelve el mismo color 
   * @param fila int la fila a pintar
   */
  protected Color hazAlfa( int fila) {
    int alfa = 0;
    if ( fila >= 0 ) {
      alfa = 50 + (fila > 7 ? 70 : 8*fila);
    }
    
    return new Color( 0,0,0, alfa);
  }

  
  /////////////////////////////////////
  
  public class MiML extends MouseAdapter implements MouseMotionListener  {
    public void mouseExited( MouseEvent e) {
      rollover = -1;
      tabPane.repaint();
    }
    
    public void mouseDragged( MouseEvent e) {}
    
    public void mouseMoved( MouseEvent e) {
      rollover = tabForCoordinate( tabPane, e.getX(), e.getY());
      
      // Esto es para limitar el numero de veces que se redibuja el panel
      // Un boton se puede pintar muchas veces porque es pequeño y gasta poco, pero esto es un panel que puede tener cienes y cienes de controles,
      // asi que cada vez que se repinta gasta lo suyo 
      if ( (rollover == -1) && (antRollover == rollover) ) {
        return;
      }
      
      tabPane.repaint();
      antRollover = rollover;
    }
  }
  
  

}