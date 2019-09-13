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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;


public class NimRODApplet extends JApplet {
  private static final long serialVersionUID = -6755578837264226535L;

  public void init() {
    try {
      NimRODMain.nf = new NimRODLookAndFeel();
      NimRODMain.nt = new NimRODTheme();
      NimRODLookAndFeel.setCurrentTheme( NimRODMain.nt);
      
      UIManager.setLookAndFeel( new NimRODLookAndFeel());
    } 
    catch ( Exception ex) {
      System.out.println( ex);
      ex.printStackTrace();
    }
   
    JFrame.setDefaultLookAndFeelDecorated( true);
    setLayout( new BorderLayout());
    
    JButton bot = new JButton( "Gogogo");
    bot.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent ev) {
        new NimRODMain();
      }
    });
    add( bot);
  }
}
