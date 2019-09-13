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
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.Properties;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

class NimRODUtils {
  
  protected static Color rollColor;
  
  static final int THIN = 0;
  static final int FAT = 1;
  
  static final int MATRIX_FAT = 5;  // Esto define el grosor de la sombra de los titulos
  static Kernel kernelFat;
  
  static final int MATRIX_THIN = 3;  // Esto define el grosor de la sombra de los menus
  static Kernel kernelThin;

  //static Color brillo = new Color( 255,255,255, 64);
  //static Color sombra = new Color( 20,20,20, 50);
  
  static Color getSombra() {
    return getColorAlfa( getColorTercio( NimRODLookAndFeel.getControlDarkShadow(), Color.black),
                         64);
  }
  
  static Color getBrillo() {
    return getColorAlfa( getColorTercio( NimRODLookAndFeel.getControlHighlight(), Color.white),
                         64);
  }
  
  static Color getSombraMenu() {
    return new Color( 20,20,20, 50);
  }
  
  static Color getBrilloMenu() {
    return new Color( 255,255,255, 64);
  }
  
  static NimRODTheme iniCustomColors( NimRODTheme nt, Properties props) {
    String p1, p2, p3, s1, s2, s3, selection, background, w, b, opMenu, opFrame;
    
    selection = props.getProperty( "nimrodlf.selection");
    background = props.getProperty( "nimrodlf.background");
    
    p1 = props.getProperty( "nimrodlf.p1");
    p2 = props.getProperty( "nimrodlf.p2");
    p3 = props.getProperty( "nimrodlf.p3");
    
    s1 = props.getProperty( "nimrodlf.s1");
    s2 = props.getProperty( "nimrodlf.s2");
    s3 = props.getProperty( "nimrodlf.s3");
    
    w = props.getProperty( "nimrodlf.w");
    b = props.getProperty( "nimrodlf.b");
    
    opMenu = props.getProperty( "nimrodlf.menuOpacity");
    opFrame = props.getProperty(  "nimrodlf.frameOpacity");
      
    return iniCustomColors( nt, selection, background, p1, p2, p3, s1, s2, s3, w, b, opMenu, opFrame);
  }
  
  /**
   * Esta funcion se usa para inicializar los colores del tema segun los argumentos que se le pasen.
   */
  static NimRODTheme iniCustomColors( NimRODTheme nt, String selection, String background, 
                                      String p1, String p2, String p3, 
                                      String s1, String s2, String s3, 
                                      String w, String b, String opMenu, String opFrame) {
    if ( selection != null ) {
      nt.setPrimary( Color.decode( selection));
    }
    if ( background != null ) {
      nt.setSecondary( Color.decode( background));
    }
    
    if ( p1 != null ) { nt.setPrimary1( Color.decode( p1)); }
    if ( p2 != null ) { nt.setPrimary2( Color.decode( p2)); }
    if ( p3 != null ) { nt.setPrimary3( Color.decode( p3)); }
    
    if ( s1 != null ) { nt.setSecondary1( Color.decode( s1)); }
    if ( s2 != null ) { nt.setSecondary2( Color.decode( s2)); }
    if ( s3 != null ) { nt.setSecondary3( Color.decode( s3)); }
    
    if ( w != null ) { nt.setWhite( Color.decode( w)); }
    if ( b != null ) { nt.setBlack( Color.decode( b)); }
    
    if ( opMenu != null) { nt.setMenuOpacity( Integer.parseInt( opMenu)); }
    if ( opFrame != null) { nt.setFrameOpacity( Integer.parseInt( opFrame)); }
    
    return nt;
  }
  
  static ImageIcon loadRes( String fich) {
    try {
      return new ImageIcon( Toolkit.getDefaultToolkit().createImage( readStream( NimRODLookAndFeel.class.getResourceAsStream( fich))));
    }
    catch ( Exception ex) {
      ex.printStackTrace();
      System.out.println( "No se puede cargar el recurso " + fich);
      return null;
    }
  }

  static byte[] readStream( InputStream input) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    int read;
    byte[] buffer = new byte[256];
    
    while ( (read = input.read( buffer, 0, 256)) != -1 ) {
      bytes.write( buffer, 0, read);
    }
    
    return bytes.toByteArray();
  }

  /**
   * Esta funcion se usa para pintar la barra de seleccion de los menus. Esta aqui
   * para no repetirla en todas partes...
   */
  static void pintaBarraMenu( Graphics g, JMenuItem menuItem, Color bgColor) {
    ButtonModel model = menuItem.getModel();
    Color oldColor = g.getColor();
  
    int menuWidth = menuItem.getWidth();
    int menuHeight = menuItem.getHeight();
  
    if ( menuItem.isOpaque() ) {
      g.setColor( menuItem.getBackground());
      g.fillRect( 0,0, menuWidth, menuHeight);
    }
    
    if ( (menuItem instanceof JMenu && !(((JMenu)menuItem).isTopLevelMenu()) && model.isSelected())
         ||
         model.isArmed() ) {
      RoundRectangle2D.Float boton = new RoundRectangle2D.Float(); 
      boton.x = 1;
      boton.y = 0;
      boton.width = menuWidth - 3;
      boton.height = menuHeight - 1;
      boton.arcwidth = 8;
      boton.archeight = 8;
      
      GradientPaint grad = new GradientPaint( 1,1, getBrilloMenu(), 
                                              0,menuHeight, getSombraMenu());
      
      Graphics2D g2D = (Graphics2D)g;
      g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      g.setColor( bgColor);
      g2D.fill( boton);
      
      g.setColor( bgColor.darker());
      g2D.draw( boton);
      
      g2D.setPaint( grad);
      g2D.fill( boton);
      
      g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
  
    g.setColor( oldColor);
  }

  static void paintFocus( Graphics g, int x, int y, int width, int height, int r1, int r2, Color color) {
    paintFocus( g, x, y, width, height, r1, r2, 2.0f, color);
  }
  
  static void paintFocus( Graphics g, int x, int y, int width, int height, int r1, int r2, float grosor, Color color) {
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Stroke oldStroke = g2d.getStroke();
    
    g2d.setColor( color);
    g2d.setStroke( new BasicStroke( grosor));
    if ( r1 == 0 && r2 == 0 ) {
      g.drawRect( x, y, width, height);
    }
    else {
      g.drawRoundRect( x,y, width-1,height-1, r1,r2);
    }
    
    g2d.setStroke( oldStroke);
    
    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
  }
  
  static Color getRolloverColor() {
    if ( rollColor == null ) {
      rollColor = getColorAlfa( UIManager.getColor( "Button.focus"), 40);
    }
    
    return rollColor;
  }
  
  static Color getColorAlfa( Color col, int alfa) {
    return new Color( col.getRed(), col.getGreen(), col.getBlue(), alfa);
  }
  
  static Color getColorMedio( Color a, Color b) {
    return new Color( propInt( a.getRed(), b.getRed(), 2),
                      propInt( a.getGreen(), b.getGreen(), 2),
                      propInt( a.getBlue(), b.getBlue(), 2));
  }

  static ColorUIResource getColorTercio( Color a, Color b) {
    return new ColorUIResource( propInt( a.getRed(), b.getRed(), 3),
                                propInt( a.getGreen(), b.getGreen(), 3),
                                propInt( a.getBlue(), b.getBlue(), 3));
  }
  
  private static int propInt( int a, int b, int prop) {
    return b + ((a-b) / prop);
  }
  
  
  static void paintShadowTitleFat( Graphics g, String title, int x, int y, Color frente) {
    paintShadowTitle( g, title, x, y, frente, Color.black, 1, FAT, SwingConstants.HORIZONTAL);
  }

  static void paintShadowTitleFat( Graphics g, String title, int x, int y, Color frente, Color shadow) {
    paintShadowTitle( g, title, x, y, frente, shadow, 1, FAT, SwingConstants.HORIZONTAL);
  }

  static void paintShadowTitleFat( Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
    paintShadowTitle( g, title, x, y, frente, shadow, desp, FAT, SwingConstants.HORIZONTAL);
  }

  static void paintShadowTitleThin( Graphics g, String title, int x, int y, Color frente) {
    paintShadowTitle( g, title, x, y, frente, Color.black, 1, THIN, SwingConstants.HORIZONTAL);
  }

  static void paintShadowTitleThin( Graphics g, String title, int x, int y, Color frente, Color shadow) {
    paintShadowTitle( g, title, x, y, frente, shadow, 1, THIN, SwingConstants.HORIZONTAL);
  }

  static void paintShadowTitleThin( Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
    paintShadowTitle( g, title, x, y, frente, shadow, desp, THIN, SwingConstants.HORIZONTAL);
  }

  static void paintShadowTitleFatV( Graphics g, String title, int x, int y, Color frente) {
    paintShadowTitle( g, title, x, y, frente, Color.black, 1, FAT, SwingConstants.VERTICAL);
  }

  static void paintShadowTitleFatV( Graphics g, String title, int x, int y, Color frente, Color shadow) {
    paintShadowTitle( g, title, x, y, frente, shadow, 1, FAT, SwingConstants.VERTICAL);
  }

  static void paintShadowTitleFatV( Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
    paintShadowTitle( g, title, x, y, frente, shadow, desp, FAT, SwingConstants.VERTICAL);
  }

  static void paintShadowTitleThinV( Graphics g, String title, int x, int y, Color frente) {
    paintShadowTitle( g, title, x, y, frente, Color.black, 1, THIN, SwingConstants.VERTICAL);
  }

  static void paintShadowTitleThinV( Graphics g, String title, int x, int y, Color frente, Color shadow) {
    paintShadowTitle( g, title, x, y, frente, shadow, 1, THIN, SwingConstants.VERTICAL);
  }

  static void paintShadowTitleThinV( Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
    paintShadowTitle( g, title, x, y, frente, shadow, desp, THIN, SwingConstants.VERTICAL);
  }

  static void paintShadowTitle( Graphics g, String title, 
                                int x, int y, Color frente, Color shadow, 
                                int desp, int tipo, int orientation) {
    
    // Si hay que rotar la fuente, se rota 
    Font f = g.getFont();
    if ( orientation == SwingConstants.VERTICAL ) {
      AffineTransform rotate = AffineTransform.getRotateInstance( Math.PI / 2);
      f = f.deriveFont( rotate);
    }
    
    // Si hay que pintar sombra, se hacen un monton de cosas
    if ( shadow != null ) {
      int matrix = ( tipo == THIN ? MATRIX_THIN : MATRIX_FAT);
      
      Rectangle2D rect = g.getFontMetrics().getStringBounds( title, g);
      
      int w, h;
      if ( orientation == SwingConstants.HORIZONTAL ) {
        w = (int)rect.getWidth() + 6*matrix;    // Hay que dejar espacio para las sombras y el borde
        h = (int)rect.getHeight() + 6*matrix;   // que ConvolveOp ignora por el EDGE_NO_OP
      }
      else {
        h = (int)rect.getWidth() + 6*matrix;    // Hay que dejar espacio para las sombras y el borde
        w = (int)rect.getHeight() + 6*matrix;   // que ConvolveOp ignora por el EDGE_NO_OP
      }
      
      // La sombra del titulo
      BufferedImage iTitulo = new BufferedImage( w,h, BufferedImage.TYPE_INT_ARGB);
      BufferedImage iSombra = new BufferedImage( w,h, BufferedImage.TYPE_INT_ARGB);
      
      Graphics2D g2 = iTitulo.createGraphics();
      g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      
      g2.setFont( f);
      g2.setColor( shadow);
      g2.drawString( title, 3*matrix, 3*matrix);  // La pintamos en el centro
  
      ConvolveOp cop = new ConvolveOp( ( tipo == THIN ? kernelThin : kernelFat), ConvolveOp.EDGE_NO_OP, null);
      cop.filter( iTitulo, iSombra);              // A ditorsionar
      
      // Por fin, pintamos el jodio titulo
      g.drawImage( iSombra, 
                   x - 3*matrix + desp,              // Lo llevamos a la posicion original y le sumamos 1          
                   y - 3*matrix + desp,              // para que la sombra quede pelin desplazada 
                   null); 
    }
    
    // Si hay que pintar el frente, se pinta
    if ( frente != null ) {
      g.setFont( f);
      g.setColor( frente);
      g.drawString( title, x, y);
    }
  }

  static Icon reescala( Icon ic, int maxW, int maxH) {
    if ( ic == null ) {
      return null;
    }
    if ( ic.getIconHeight() == maxH && ic.getIconWidth() == maxW ) {
      return ic;
    }
    
    BufferedImage bi = new BufferedImage( ic.getIconHeight(), ic.getIconWidth(), BufferedImage.TYPE_INT_ARGB);
    
    Graphics g = bi.createGraphics();
    ic.paintIcon( null, g, 0, 0);
    g.dispose();
    
    Image bf = bi.getScaledInstance( maxW, maxH, Image.SCALE_SMOOTH);
    
    return new ImageIcon( bf);
  }

  static int getOpacity() {
    return getMenuOpacity();
  }

  static int getMenuOpacity() {
    try {
      NimRODTheme th = (NimRODTheme)NimRODLookAndFeel.theme;
      return th.getMenuOpacity();
    }
    catch ( Throwable ex) {
      return NimRODTheme.DEFAULT_MENU_OPACITY;
    }
  }

  static float getMenuOpacityFloat() {
    return getMenuOpacity() / 255f;
  }

  static int getFrameOpacity() {
    try {
      NimRODTheme th = (NimRODTheme)NimRODLookAndFeel.theme;
      return th.getFrameOpacity();
    }
    catch ( Throwable ex) {
      return NimRODTheme.DEFAULT_FRAME_OPACITY;
    }
  }

  static float getFrameOpacityFloat() {
    return getFrameOpacity() / 255f;
  }

}
