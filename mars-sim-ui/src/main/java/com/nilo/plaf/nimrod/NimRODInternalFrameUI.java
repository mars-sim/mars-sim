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
 * Esta clase implementa los internalframes
 * @author Nilo J. Gonzalez
 */

package com.nilo.plaf.nimrod;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class NimRODInternalFrameUI extends MetalInternalFrameUI {
  NimRODInternalFrameTitlePane titlePane;
  
  
  public NimRODInternalFrameUI( JInternalFrame arg0) {
    super( arg0);
  }

  public static ComponentUI createUI( JComponent c) {
    return new NimRODInternalFrameUI( (JInternalFrame)c);
  }

  protected JComponent createNorthPane( JInternalFrame w) {
    super.createNorthPane( w);
    
    titlePane = new NimRODInternalFrameTitlePane( w);
    return titlePane;
  }

  public void update( Graphics g, JComponent c) {
    paint( g, c);
  }
  
  
}
