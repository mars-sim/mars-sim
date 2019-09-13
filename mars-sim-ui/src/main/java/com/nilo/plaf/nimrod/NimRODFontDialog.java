/*
 *                 (C) Copyright 2010 Nilo J. Gonzalez
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
 * Esta clase implementa un sencillo cuadro de dialogo de seleccion de fuentes
 * @author Nilo J. Gonzalez
 */
 package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class NimRODFontDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  protected JPanel fondo;
  protected JList lFont, lStyle, lSize;

  protected Font currentFont;
  protected JLabel lEjemplo;

  protected boolean bCancelado;

  public NimRODFontDialog( Frame papi) {
    this( papi, null);
  }
  
  public NimRODFontDialog( Frame papi, Font fIni) {
    super( papi, "Font", true);

    bCancelado = true;
    
    setDefaultCloseOperation( DISPOSE_ON_CLOSE);

    doPanel( fIni);

    this.getContentPane().add( fondo, BorderLayout.CENTER);

    pack();

    setResizable( false);
  }
  
  public boolean isCanceled() {
    return bCancelado;
  }

  public Font getSelectedFont() {
    return currentFont;
  }

  public void doPanel( Font fIni) {
    String[] styles = { "Normal", "Bold", "Italic", "Bold+Italic"};
    String[] sizes = new String[100];
    for ( int i = 1; i <= 100; i++) {
      sizes[i-1] = "" + i;
    }

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

    MIListSelectionListener milsl = new MIListSelectionListener();

    lFont = new JList( ge.getAvailableFontFamilyNames());
    lFont.setVisibleRowCount( 7);
    lFont.addListSelectionListener( milsl);

    lStyle = new JList( styles);
    lStyle.setVisibleRowCount( 7);
    lStyle.addListSelectionListener( milsl);

    lSize = new JList( sizes);
    lSize.setVisibleRowCount( 7);
    lSize.addListSelectionListener( milsl);

    JLabel jLabel1 = new JLabel( "Font");
    jLabel1.setBounds(10, 20, 41, 16);

    JLabel jLabel2 = new JLabel( "Style");
    jLabel2.setBounds(170, 20, 41, 16);

    JLabel jLabel3 = new JLabel( "Size");
    jLabel3.setBounds(270, 20, 41, 16);

    JScrollPane jsp1, jsp2, jsp3;
    jsp1 = new JScrollPane( lFont);
    jsp1.setBounds( 10, 40, 150, 110);
    jsp2 = new JScrollPane( lStyle);
    jsp2.setBounds( 170, 40, 90, 110);
    jsp3 = new JScrollPane( lSize);
    jsp3.setBounds( 270, 40, 70, 110);

    lEjemplo = new JLabel( "AaBbCcDdEeFfGgHhIiJjKkLlMm...XxYyZz", SwingConstants.CENTER);
    lEjemplo.setBounds(10, 160, 340, 49);

    JButton bOK = new JButton( "OK");
    bOK.addActionListener( milsl);
    bOK.setBounds(265, 230, 75, 25);
    JButton bCancel = new JButton( "Cancel");
    bCancel.addActionListener( milsl);
    bCancel.setBounds(180, 230, 75, 25);

    fondo = new JPanel( null);
    fondo.add( jLabel1);
    fondo.add( jLabel2);
    fondo.add( jLabel3);
    fondo.add( jsp1);
    fondo.add( jsp2);
    fondo.add( jsp3);
    fondo.add( lEjemplo);
    fondo.add( bOK);
    fondo.add( bCancel);

    fondo.setPreferredSize( new Dimension( 350, 265));

    currentFont = ( fIni == null ? lEjemplo.getFont() : fIni);
    decode( currentFont);
  }

  private void decode( Font ff) {
    String fontName = ff.getFontName();
    int style = ff.getStyle();
    int size = ff.getSize();

    lFont.setSelectedValue( fontName, true);
    lSize.setSelectedValue( "" + size, true);

    if ( ((style & Font.BOLD) != 0) && ((style & Font.ITALIC) != 0) ) {
      lStyle.setSelectedValue( "Bold+Italic", true);
    }
    else if ( (style & Font.BOLD) != 0  ) {
      lStyle.setSelectedValue( "Bold", true);
    }
    else if ( (style & Font.ITALIC) != 0 ) {
      lStyle.setSelectedValue( "Italic", true);
    }
    else {
      lStyle.setSelectedValue( "Normal", true);
    }
  }

  public void refresh() {
    lEjemplo.setFont( currentFont);
  }

  protected class MIListSelectionListener implements ListSelectionListener, ActionListener {

    public void actionPerformed( ActionEvent e) {
      if ( e.getActionCommand().equals( "OK") ) {
        bCancelado = false;
      }
      else if ( e.getActionCommand().equals( "Cancel") ) {
        bCancelado = true;
      }

      dispose();
    }

    /**
     * @param e  
     */
    public void valueChanged( ListSelectionEvent e) {
      String fontName = currentFont.getFontName();
      int style = currentFont.getStyle();
      int size = currentFont.getSize();
      
      if ( lFont.getSelectedValue() != null  ) {
        fontName = (String)lFont.getSelectedValue();
      }

      if ( lSize.getSelectedValue() != null  ) {
        size = Integer.decode( (String)lSize.getSelectedValue()).intValue();
      }

      if ( lStyle.getSelectedValue() != null  ) {
        style = 0;
        String ss = (String)lStyle.getSelectedValue();

        if ( ss.indexOf( "Normal") != -1 ) {
          style = Font.PLAIN;
        }
        else {
          if ( ss.indexOf( "Italic") != -1 ) {
            style = Font.ITALIC;
          }
          if ( ss.indexOf( "Bold") != -1 ) {
            style |= Font.BOLD;
          }
        }
      }

      currentFont = new Font( fontName, style, size);

      refresh();
    }

  }

}
