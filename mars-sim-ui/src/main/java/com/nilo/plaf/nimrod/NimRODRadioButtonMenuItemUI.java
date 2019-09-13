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
 * Esta clase implementa los RadioButtons dentro de los menus desplegables.
 * En realidad lo unico que hace es pintar el fondo, que pinta liso o como una barra de color
 * segun este seleccionado o no. Los iconos se han cargado ya en los defaults al cargar
 * el LookAndFeel en la clase NimRODLookAndFeel 
 * @author Nilo J. Gonzalez
 */

package com.nilo.plaf.nimrod;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.*;

public class NimRODRadioButtonMenuItemUI extends BasicRadioButtonMenuItemUI {
  public static ComponentUI createUI( JComponent x) {
    return new NimRODRadioButtonMenuItemUI();
  }
  
  protected void installDefaults() {
    super.installDefaults();
    
    menuItem.setBorderPainted( false);
    menuItem.setOpaque( false);
    
    defaultTextIconGap = 3;
  }
  
  protected void uninstallDefaults() {
    super.uninstallDefaults();
    
    menuItem.setOpaque( true);
  }
  
  protected void paintBackground( Graphics g, JMenuItem menuItem, Color bgColor) {
    NimRODUtils.pintaBarraMenu( g, menuItem, bgColor);
  }
}
