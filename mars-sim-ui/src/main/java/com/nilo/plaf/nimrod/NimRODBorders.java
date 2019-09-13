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
 * Esta clase se utiliza como repositorio de borders. Esa es su unica utilidad.
 * @author Nilo J. Gonzalez
 */
 
package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.*;


public class NimRODBorders extends MetalBorders {
  private static Border butBorder;
  private static Border popupMenuBorder;
  private static Border rolloverButtonBorder;
  private static Border scrollPaneBorder;
  private static Border internalFrameBorder;
  private static Border menuBarBorder;
  private static Border toolBarBorder;
  private static Border cellFocusBorder;
  private static Border genBorder;
  private static Border genEmptyBorder;
  private static Border genThinBorder;
  private static Border genMenuBorder;
  private static Border genTextFieldBorder;
  private static Border genComboEditorBorder;
  private static Border genComboButtonBorder;
  private static Border genToolTipBorder;
  
  
  
  public static Border getCellFocusBorder() {
    if ( cellFocusBorder == null ) {
      cellFocusBorder = new NimRODCellFocusBorder();
    }
    return cellFocusBorder;
  }
  
  public static Border getToolTipBorder() {
    if ( genToolTipBorder == null) {
      genToolTipBorder = new NimRODToolTipBorder();
    }
    return genToolTipBorder;
  }
  
  public static Border getInternalFrameBorder() {
    if ( internalFrameBorder == null) {
      internalFrameBorder = new NimRODInternalFrameBorder();
    }
    return internalFrameBorder;
  }

  public static Border getPopupMenuBorder() {
    if ( popupMenuBorder == null) {
  	  popupMenuBorder = new NimRODPopupMenuBorder();
    }
    return popupMenuBorder;
  }  
  
  public static Border getButtonBorder() {
    if ( butBorder == null) {
  	  butBorder = new BorderUIResource.CompoundBorderUIResource( new NimRODBorders.NimRODButtonBorder(),
                                                                 new BasicBorders.MarginBorder());
    }
    return butBorder;
  }

  public static Border getRolloverButtonBorder() {
    if ( rolloverButtonBorder == null) {
      rolloverButtonBorder = new NimRODRolloverButtonBorder();
    }
    return rolloverButtonBorder;
  }
  
  public static Border getScrollPaneBorder() {
    if ( scrollPaneBorder == null) {
      scrollPaneBorder = new NimRODScrollPaneBorder();
    }
    return scrollPaneBorder;
  }
  
  public static Border getMenuBarBorder() {
    if ( menuBarBorder == null) {
      menuBarBorder = new NimRODMenuBarBorder();
    }
    return menuBarBorder;
  }
  
  public static Border getToolBarBorder() {
    if ( toolBarBorder == null) {
      toolBarBorder = new NimRODToolBarBorder();
    }
    return toolBarBorder;
  }
  
  public static Border getGenMenuBorder() {
    if ( genMenuBorder == null) {
      genMenuBorder = new NimRODMenuBorder();
    }
    return genMenuBorder;
  }
  
  public static Border getComboEditorBorder() {
    if ( genComboEditorBorder == null) {
      genComboEditorBorder = new NimRODComboEditorBorder();
    }
    return genComboEditorBorder;
  }
  
  public static Border getComboButtonBorder() {
    if ( genComboButtonBorder == null) {
      genComboButtonBorder = new NimRODComboButtonBorder();
    }
    return genComboButtonBorder;
  }
  
  public static Border getGenBorder() {
    if ( genBorder == null) {
      genBorder = new NimRODGenBorder();
    }
    return genBorder;
  }
  
  public static Border getEmptyGenBorder() {
    if ( genEmptyBorder == null) {
      genEmptyBorder = new NimRODEmptyGenBorder();
    }
    return genEmptyBorder;
  }
  
  public static Border getThinGenBorder() {
    if ( genThinBorder == null) {
      genThinBorder = new NimRODThinGenBorder();
    }
    return genThinBorder;
  }
  
  public static Border getTextFieldBorder() {
    if ( genTextFieldBorder == null) {
      genTextFieldBorder = new NimRODTextFieldBorder();
    }
    return genTextFieldBorder;
  }
  
  
  public static class NimRODCellFocusBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -7363292672160449136L;
    
    protected static Insets borderInsets = new Insets( 1,1, 1,1);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int w, int h) {
      Color col = NimRODUtils.getColorTercio( NimRODLookAndFeel.getControlTextColor(),
                                              NimRODLookAndFeel.getFocusColor());
      g.setColor( col);
      g.drawRect( x, y, w-1, h-1);
    }
    
    public Insets getBorderInsets( Component c) {
      return borderInsets;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
	
	public static class NimRODButtonBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -2083885266582056467L;
    
    protected static Insets borderInsets = new Insets( 0,0, 0,0);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int w, int h) {
      if ( !((AbstractButton)c).isBorderPainted() ) {
        return;
      }
      
      g.translate( x, y);
      
      Graphics2D g2D = (Graphics2D)g;
      g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      g2D.setColor( NimRODLookAndFeel.getControlDarkShadow());
      g2D.drawRoundRect( 0,0, w-1,h-1, 8,8);
          
      if ( c instanceof JButton ) {
        JButton button = (JButton)c;
        //ButtonModel model = button.getModel();
  
        if ( button.isDefaultButton() ) {
          g2D.setColor( NimRODLookAndFeel.getControlDarkShadow().darker());
          g2D.drawRoundRect( 1,1, w-3,h-3, 7,7);
        }
        /*else if ( model.isPressed() && model.isArmed() ) {
          g.translate( x, y);
          g.setColor( NimRODLookAndFeel.getControlDarkShadow() );
          g.drawRoundRect( 0,0, w-1,h-1, 8,8);
        }*/
      }
      
      g2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
    
    public Insets getBorderInsets( Component c) {
      return borderInsets;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODPopupMenuBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -2083885266582056468L;
    
    protected static Insets borderInsets = new Insets( 1,1, 5,5);

    public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
	    g.translate( x, y);

      g.setColor( NimRODLookAndFeel.getControlDarkShadow());
	    g.drawRect( 0, 0, w-5, h-5);
      
      Icon icono = UIManager.getIcon( "BorderPopupMenu.SombraEsqIcon");
      icono.paintIcon( c, g, w-5,h-5);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraUpIcon");
      icono.paintIcon( c, g, w-5,0);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraIzqIcon");
      icono.paintIcon( c, g, 0,h-5);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraBajIcon");
      g.drawImage( ((ImageIcon)icono).getImage(), 5,h-5, w-10, icono.getIconHeight(), null);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraDerIcon");
      g.drawImage( ((ImageIcon)icono).getImage(), w-5,5, icono.getIconWidth(),h-10, null);
	   
      g.translate( -x, -y);
    }

    public Insets getBorderInsets( Component c ) {
      return borderInsets;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODToolTipBorder extends NimRODPopupMenuBorder implements UIResource {
    private static final long serialVersionUID = -7253367634568230481L;
  }
  
  public static class NimRODRolloverButtonBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -2083885266582056469L;
    
    protected static Insets borderInsets = new Insets( 3,2, 3,2);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int w, int h) {
      if ( !((AbstractButton)c).isBorderPainted() ) {
        return;
      }
      ButtonModel model = ((AbstractButton)c).getModel();
      
	    if ( model.isRollover() ) { //&& !( model.isPressed() && !model.isArmed() ) ) {
    		g.setColor( NimRODLookAndFeel.getControlDarkShadow());
  			g.drawRoundRect( 0,0, w-1,h-1, 8,8);
  			
    		RoundRectangle2D.Float boton = new RoundRectangle2D.Float(); 
        boton.x = 0;
        boton.y = 0;
        boton.width = c.getWidth();
        boton.height = c.getHeight();
        boton.arcwidth = 8;
        boton.archeight = 8;
        
        GradientPaint grad = null;
        if ( model.isPressed() ) {
        	grad = new GradientPaint( 0,0, NimRODUtils.getSombra(), 
                                    0,c.getHeight()/2, NimRODUtils.getBrillo());
    		}
    		else {
        	grad = new GradientPaint( 0,0, NimRODUtils.getBrillo(), 
                                    0,c.getHeight(), NimRODUtils.getSombra());
    		}
    		
    		Graphics2D g2D = (Graphics2D)g;
    		
        g2D.setPaint( grad);
        g2D.fill( boton);
      }
    }
    
    public Insets getBorderInsets( Component c ) {
      return borderInsets;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODInternalFrameBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -4691959764241705857L;
    
    private static final int grosor = 3;
    
    protected static Insets ins = new Insets( 0,grosor, 5+grosor, 5+grosor);

    public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
      g.translate( x, y);

      Graphics2D g2D = (Graphics2D)g.create();
      
      g2D.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, NimRODUtils.getFrameOpacityFloat()));
      
      // Elegimos el color del borde
      Color colIzq, colDer;
      GradientPaint grad;
      if ( ((JInternalFrame)c).isSelected() ) {
        grad = new GradientPaint( 0,0, NimRODLookAndFeel.getPrimaryControlDarkShadow(), 
                                  w,0, NimRODLookAndFeel.getPrimaryControl());
        colIzq = NimRODLookAndFeel.getPrimaryControlDarkShadow();
        colDer = NimRODLookAndFeel.getPrimaryControl();
      }
      else {
        grad = new GradientPaint( 0,0, NimRODLookAndFeel.getControl(), 
                                  w,0, NimRODLookAndFeel.getControlDarkShadow());
        colIzq = NimRODLookAndFeel.getControl();
        colDer = NimRODLookAndFeel.getControlDarkShadow();
      }
      
      g2D.setColor( colIzq);
      g2D.fillRect( 0, 0, grosor,h-ins.bottom);
      
      g2D.setPaint( grad);
      g2D.fillRect( 0, h-ins.bottom, w-ins.right+grosor, grosor);
      
      g2D.setColor(  colDer);
      g2D.fillRect( w-ins.right, 0, grosor, h-ins.bottom);

      g2D.dispose();
      
      g.setColor( NimRODLookAndFeel.getControlDarkShadow());
      g.drawRect( 0, 0, w-5, h-5);
      
      Icon icono = UIManager.getIcon( "BorderPopupMenu.SombraEsqIcon");
      icono.paintIcon( c, g, w-5,h-5);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraUpIcon");
      icono.paintIcon( c, g, w-5,0);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraIzqIcon");
      icono.paintIcon( c, g, 0,h-5);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraBajIcon");
      g.drawImage( ((ImageIcon)icono).getImage(), 5,h-5, w-10, icono.getIconHeight(), null);
      
      icono = UIManager.getIcon( "BorderPopupMenu.SombraDerIcon");
      g.drawImage( ((ImageIcon)icono).getImage(), w-5,5, icono.getIconWidth(),h-10, null);
      
      g.translate( -x, -y);
    }

    public Insets getBorderInsets( Component c ) {
      return ins;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODMenuBarBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = 116001977502172752L;

    protected static Insets ins = new Insets( 0,2, 0, 10);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      /*
      Icon icono = UIManager.getIcon( "BordeGenSup");
      y += height-icono.getIconHeight();
      g.drawImage( ((ImageIcon)icono).getImage(), 0,y, width, icono.getIconHeight(), null);
      */
      g.setColor( NimRODUtils.getSombra());
      g.drawLine( 0,height-2, width,height-2);
      g.setColor( NimRODUtils.getBrillo());
      g.drawLine( 0,height-1, width,height-1);
    }
    
    public Insets getBorderInsets( Component c) {
      return ins;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODToolBarBorder extends NimRODMenuBarBorder implements SwingConstants, UIResource {
    private static final long serialVersionUID = 116002347502172752L;

    private static int bumpWidth = 14;
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      Icon icon = null;
      int desp = 0;
      
      if ( ((JToolBar)c).isFloatable() ) {
        if ( ((JToolBar)c).getOrientation() == HORIZONTAL ) {
          icon = UIManager.getIcon( "ScrollBar.verticalThumbIconImage");
          desp = icon.getIconHeight();
          
          for ( int i = 0; i < 5; i++) {
            icon.paintIcon( c, g, x+1, y+1+(desp*i));
          }
        }
        else {
          icon = UIManager.getIcon( "ScrollBar.horizontalThumbIconImage");
          desp = icon.getIconWidth();
          
          for ( int i = 0; i < 5; i++) {
            icon.paintIcon( c, g, x+1+(desp*i), y+1);
          }
        }
      }
    }
    
    public Insets getBorderInsets( Component c) {
      return getBorderInsets( c, new Insets( 0,0,0,0));
    }

    public Insets getBorderInsets( Component c, Insets ins) {
      ins.top = ins.left = ins.bottom = ins.right = 3;

      if ( ((JToolBar)c).isFloatable() ) {
        if ( ((JToolBar)c).getOrientation() == HORIZONTAL ) {
          if (c.getComponentOrientation().isLeftToRight()) {
              ins.left += bumpWidth;
          } else {
              ins.right += bumpWidth;
          }
        } else {// vertical
          ins.top += bumpWidth;
        }
      }

      Insets margin = ((JToolBar)c).getMargin();

      if ( margin != null ) {
        ins.left   += margin.left;
        ins.top    += margin.top;
        ins.right  += margin.right;
        ins.bottom += margin.bottom;
      }

      return ins;
    }
  }
  
  public static class NimRODGenBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = 116001977502172752L;

    protected static Insets ins = new Insets( 3,2, 3,2);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      int wl = width - 8;
      int hl = height - 8;
      
      ImageIcon icono = (ImageIcon)UIManager.getIcon( "BordeGenSup");
      // Esto esta aqui porque cuando se cambia de look and feel no siempre se cambia
      // el borde de los textfield, siguen teniendo el nimrodborder y cuando van a cargar
      // el icono salta una nullpointerexception porque no lo encuentra 
      if ( icono == null ) return;
      
      g.translate( x,y);
      
      g.drawImage( icono.getImage(), 4,0, wl, icono.getIconHeight(), null);
      
      icono = (ImageIcon)UIManager.getIcon( "BordeGenInf");
      g.drawImage( icono.getImage(), 4,height-icono.getIconHeight(), wl, icono.getIconHeight(), null);
      
      icono = (ImageIcon)UIManager.getIcon( "BordeGenDer");
      g.drawImage( icono.getImage(), width-icono.getIconWidth(),4, icono.getIconWidth(), hl, null);
      
      icono = (ImageIcon)UIManager.getIcon( "BordeGenIzq");
      g.drawImage( icono.getImage(), 0,4, icono.getIconWidth(), hl, null);
      
      icono = (ImageIcon)UIManager.getIcon( "BordeGenSupIzq");
      icono.paintIcon( c, g, 0,0);
      icono = (ImageIcon)UIManager.getIcon( "BordeGenInfIzq");
      icono.paintIcon( c, g, 0,height-icono.getIconHeight());
      icono = (ImageIcon)UIManager.getIcon( "BordeGenSupDer");
      icono.paintIcon( c, g, width-icono.getIconWidth(),0);
      icono = (ImageIcon)UIManager.getIcon( "BordeGenInfDer");
      icono.paintIcon( c, g, width-icono.getIconWidth(),height-icono.getIconHeight());
      
      g.translate( -x,-y);
    }
    
    public Insets getBorderInsets( Component c) {
      return ins;
    }
    
    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODEmptyGenBorder extends NimRODGenBorder implements UIResource {
    private static final long serialVersionUID = 116002377502172752L;
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
    }
  }
  
  public static class NimRODThinGenBorder extends NimRODGenBorder implements UIResource {
    private static final long serialVersionUID = 116002982734987752L;
    
    protected static Insets ins = new Insets( 1,1, 1,1);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      g.setColor( NimRODLookAndFeel.getControlDarkShadow());
      g.drawRect( x, y, width-1, height-1);
    }
    
    public Insets getBorderInsets( Component c) {
      return ins;
    }
    
    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODTextFieldBorder extends NimRODGenBorder implements UIResource {
    private static final long serialVersionUID = -7253364063167310481L;
    
    protected static Insets ins = new Insets( 5,6,5,6);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      super.paintBorder( c, g, x+2, y+2, width-4, height-4);
    }
    
    public Insets getBorderInsets( Component c) {
      return ins;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  } 
  
  public static class NimRODMenuBorder extends NimRODGenBorder implements UIResource {
    private static final long serialVersionUID = -7253364063167610481L;
    
    protected static Insets ins = new Insets( 3,3, 3,3);
    
    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      JMenuItem b = (JMenuItem)c;
      ButtonModel model = b.getModel();
      
      if ( model.isArmed() || model.isSelected() ) {
        super.paintBorder( c, g, x, y, width, height-2);
      }
    }
    
    public Insets getBorderInsets( Component c) {
      return ins;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODComboEditorBorder extends NimRODTextFieldBorder implements UIResource {
    private static final long serialVersionUID = -7253364063167610483L;
  }
  
  public static class NimRODComboButtonBorder extends NimRODButtonBorder {
    private static final long serialVersionUID = -7253364063167610483L;
    
    protected static Insets ins = new Insets( 2,2,2,2);

    public void paintBorder( Component c, Graphics g, int x, int y, int width, int height) {
      super.paintBorder( c, g, x+2,y+2, width-4, height-4);
    }

    public Insets getBorderInsets( Component c) {
      return ins;
    }

    public Insets getBorderInsets( Component c, Insets insets) {
      Insets tmpIns = getBorderInsets( c);
      
      insets.top = tmpIns.top;
      insets.left = tmpIns.left;
      insets.bottom = tmpIns.bottom;
      insets.right = tmpIns.right;
      
      return insets;
    }
  }
  
  public static class NimRODScrollPaneBorder extends AbstractBorder implements UIResource {
    private static final long serialVersionUID = -6416636693876853556L;

  }
}

