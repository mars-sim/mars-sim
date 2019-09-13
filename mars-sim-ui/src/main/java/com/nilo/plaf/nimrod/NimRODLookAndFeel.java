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
 * 
 * And, last but not least, these people improved the code:
 *   Fritz Elfert
 *   Eduardo
 *   Norbert Seekircher 
 */
 
/**
 * The main class for the NimROD Look&Feel.
 *
 * To use this Look&Feel, simply include these two lines into your code:<br>
 * <code>
 * NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
 * UIManager.setLookAndFeel( NimRODLF);
 * </code>
 * You can change the default theme color in two ways.
 * 
 * You can create theme and change de colours with this lines:<br>
 *   <code>
 *   NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
 *   NimRODTheme nt = new NimRODTheme();
 *   nt.setX( Color);
 *   ....
 *   nt.setX( Color);
 *   NimRODLF.setCurrentTheme( nt);
 *   UIManager.setLookAndFeel( NimRODLF);
 *   <code><br>
 * This way is good if you can change the sources and you want the program works *only* with NimRODLF. 
 *
 * If you don't have the sources you can change the theme color including same properties in your command line and the look and feel
 * will do its best... This couldn't work if the application changes the system properties, but, well, if you don't have the sources...<br>
 * For example:<br>
 *   <code>java -Dnimrodlf.selection=0x00cc00 XXX.YOUR.APP.XXX</code> will colour with green the selected widgets<br>
 *   <code>java java -Dnimrodlf.s1=0xdde8ee -Dnimrodlf.s2=0xb7daec -Dnimrodlf.s3=0x74bfe6 XXX.YOUR.APP.XXX</code> will colour with blue the background of the widgets<br>
 * The values are in the tipical HTML format (0xRRGGBB) with the red, green and blue values encoded in hexadecimal format.<br> 
 * These are the admited properties:
 * <ul>   
 * <li>nimrodlf.selection: this is the selection color</li>
 * <li>nimrodlf.background: this is the background color</li>
 * <li>nimrodlf.p1: this is the primary1 color (¿Don't you understand? Patience?</li>
 * <li>nimrodlf.p2: this is the primary2 color</li>
 * <li>nimrodlf.p3: this is the primary3 color</li>
 * <li>nimrodlf.s1: this is the secondary1 color</li>
 * <li>nimrodlf.s2: this is the secondary2 color</li>
 * <li>nimrodlf.s3: this is the secondary3 color</li>
 * <li>nimrodlf.b: this is the black color</li>
 * <li>nimrodlf.w: this is the white color</li>
 * <li>nimrodlf.menuOpacity: this is the menu opacity</li>
 * <li>nimrodlf.frameOpacity: this is the frame opacity</li>
 * </ul>
 * ¿Primary color? ¿Secondary? ¿What the...? Cool. <a href='http://java.sun.com/products/jlf/ed1/dg/higg.htm#62001'>Here</a> you can learn what 
 * i'm talking about. Swing applications have only 8 colors, named PrimaryX, SecondaryX, White and Black, and <a href='http://java.sun.com/products/jlf/ed1/dg/higg.htm#62001'>here</a>
 * you hava a table with the who-is-who.<br>
 * You don't need to write all the values, you only must write those values you want to change. There are two shorthand properties, selection and background.
 * If you write nimrodlf.selection or nimrodlf.background the NimRODLF will calculate the colors around (darker and lighter) your choose.<br>
 * If nimrodlf.selection is writen, pX, sX, b and w are ignored. 
 * Ahh!! One more thing. 0xRRGGBB is equal #RRGGBB. 
 * @see NimRODTheme
 * @see http://java.sun.com/products/jlf/ed1/dg/higg.htm#62001
 * @author Nilo J. Gonzalez 
 */
 
package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.image.Kernel;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

import java.util.*;

public class NimRODLookAndFeel extends MetalLookAndFeel {
  private static final long serialVersionUID = 7191199335214123414L;
  
  String fichTheme = "";
  
  protected static MetalTheme theme;
  
  public NimRODLookAndFeel() {
    super();
    
    NimRODTheme nt = new NimRODTheme( "NimRODThemeFile.theme");
    
    String p1, p2, p3, s1, s2, s3, selection, background, w, b, opMenu, opFrame;
    
    
    // Vamos a ver si han puesto por linea de comandos un fichero de tema...
    
    // Este codigo esta aqui gracias a Fritz Elfert, que descubrio que esto cascaba miserablemente
    // cuando se usaba en un applet.
    String nomFich = null;
    String nomURL = null;
    try {
      nomFich = System.getProperty( "nimrodlf.themeFile");
      nomURL = System.getProperty( "nimrodlf.themeURL");
    } 
    catch ( Exception ex) {
      // If used in an applet, this could throw a SecurityException.
    }
    
    // ... o tenemos que tirar del fichero por defecto
    nomFich = ( nomFich == null ? "NimRODThemeFile.theme" : nomFich);

    
    try {
      // Primero, vamos a ver si lo sacamos de una url
      if ( nomURL != null ) {
        try {
          NimRODTheme ntt = new NimRODTheme( new java.net.URL( nomURL));  // Esto funcionara si el fichero esta en la URL y ademas esta bien
          nt = ntt;
        }
        catch ( Exception ex) {
          System.err.println( nomURL + " theme file is wrong or doesn't exist...");
          System.err.println( ex);
        }
      }
      else {
        try {
          NimRODTheme ntt = new NimRODTheme( nomFich);  // Esto funcionara si el fichero esta en la URL y ademas esta bien
          nt = ntt;
        }
        catch ( Exception ex) {
          System.err.println( nomFich + " theme file is wrong or doesn't exist...");
          System.err.println( ex);
        }
      }
      
      fichTheme = nomFich;
    }
    catch ( Exception ex) {    // Si no se puede leer el fichero o el fichero esta malamente,
      nt = new NimRODTheme( "NimRODThemeFile.theme");  // no le hacemos ni caso.
    }
    
    try {
      // Ahora vamos a ver si se expecifican los colores por linea de comandos.
      selection = System.getProperty( "nimrodlf.selection");
      background = System.getProperty( "nimrodlf.background");
      
      p1 = System.getProperty( "nimrodlf.p1");
      p2 = System.getProperty( "nimrodlf.p2");
      p3 = System.getProperty( "nimrodlf.p3");
      
      s1 = System.getProperty( "nimrodlf.s1");
      s2 = System.getProperty( "nimrodlf.s2");
      s3 = System.getProperty( "nimrodlf.s3");
      
      w = System.getProperty( "nimrodlf.w");
      b = System.getProperty( "nimrodlf.b");
      
      opMenu = System.getProperty( "nimrodlf.menuOpacity");
      opFrame = System.getProperty(  "nimrodlf.frameOpacity");
        
      nt = NimRODUtils.iniCustomColors( nt, selection, background, p1, p2, p3, s1, s2, s3, w, b, opMenu, opFrame);
    }
    catch ( Exception ex ) {
      // Este codigo esta aqui gracias a Fritz Elfert, que descubrio que esto cascaba miserablemente
      // cuando se usaba en un applet. Un gran tipo Fritz...
      if ( fichTheme.length() == 0 ) {
        nt = new NimRODTheme( "NimRODThemeFile.theme");
      }
    }
    
    setCurrentTheme( nt);
    
    float[] elements = new float[NimRODUtils.MATRIX_FAT*NimRODUtils.MATRIX_FAT];
    for ( int i = 0; i < elements.length; i++ ) {
      elements[i] = 0.1f;
    }
    int mid = NimRODUtils.MATRIX_FAT/2+1;
    elements[mid*mid] = .2f;
    
    NimRODUtils.kernelFat = new Kernel( NimRODUtils.MATRIX_FAT,NimRODUtils.MATRIX_FAT, elements);
    
    elements = new float[NimRODUtils.MATRIX_THIN*NimRODUtils.MATRIX_THIN];
    for ( int i = 0; i < elements.length; i++ ) {
      elements[i] = 0.1f;
    }
    mid = NimRODUtils.MATRIX_THIN/2+1;
    elements[mid*mid] = .2f;
    
    NimRODUtils.kernelThin = new Kernel( NimRODUtils.MATRIX_THIN,NimRODUtils.MATRIX_THIN, elements);
  }

  /*
   * Este codigo esta aqui para copiar los atajos de teclado de look and feel del sistema.
   * Principalmente sirve para que los usuarios de Macs puedan hacer el copy-paste usando
   * las teclas a las que estan acostumbrados.
   * Lo hizo Norbert Seekircher
   */
  public void initialize() {
    try {
      LookAndFeel laf = (LookAndFeel)Class.forName( UIManager.getSystemLookAndFeelClassName()).newInstance();
      laf.initialize();
      UIDefaults systemDefaults = laf.getDefaults();
      Enumeration keys = systemDefaults.keys();
      String key;

      while ( keys.hasMoreElements() ) {
        key = keys.nextElement().toString();

        if ( key.contains( "InputMap") ) {
          UIManager.getDefaults().put( key, systemDefaults.get( key));
        }
      }
    } 
    catch ( Exception ex) {
      //ex.printStackTrace();
    }
  }

  
  public String getID() {
    return "NimROD";
  }

  public String getName() {
    return "NimROD";
  }

  public String getDescription() {
    return "Look and Feel NimROD, by Nilo J. Gonzalez 2005-2007";
  }

  public boolean isNativeLookAndFeel() {
    return false;
  }

  public boolean isSupportedLookAndFeel() {
    return true;
  }
  
  /**
   * Este metodo devuelve false porque para dar bordes como es debido a la ventana principal hay que
   * fusilarse la clase MetalRootPaneUI enterita porque la mayoria de sus metodos son privados...
   * Ademas, no es mala idea que la decoracion de la ventana principal la ponga el sistema operativo
   * para que sea igual que todas (y si tiene transparencias, mejor)
   */
  public boolean getSupportsWindowDecorations() {
    return false;
  }

  /* Esta mierda es debida a que quiero que esto funcione en la version 1.4 de Java y ademas, 
     que el sitio adecuado para dejar la transparencia de los menus es el theme. Bueno, pues
     en la version 1.4 eso no se puede hacer, porque la funcion getCurrentTheme es privada de
     MetalLookAndFeel, asi que no hay manera de saber que tema se esta usando y por tanto no
     se puede recuperar la opacidad (ni ninugna otra caracteristica de los temas que no sea 
     estandar). Asi que hay que replicar la funcion setCurrentTheme aqui, guardar el tema en 
     una variable local y devolverlo despues en la funcion getOpacity
  */
  public static void setCurrentTheme( MetalTheme t) {
    MetalLookAndFeel.setCurrentTheme( t);
    
    theme = t;
    NimRODUtils.rollColor = null;
  }
  
  protected void initClassDefaults( UIDefaults table) {
    super.initClassDefaults( table);
    
    table.put( "ButtonUI", "com.nilo.plaf.nimrod.NimRODButtonUI");
    table.put( "ToggleButtonUI", "com.nilo.plaf.nimrod.NimRODToggleButtonUI");
    table.put( "TextFieldUI", "com.nilo.plaf.nimrod.NimRODTextFieldUI");
    table.put( "TextAreaUI", "com.nilo.plaf.nimrod.NimRODTextAreaUI");
    table.put( "PasswordFieldUI", "com.nilo.plaf.nimrod.NimRODPasswordFieldUI");
    table.put( "CheckBoxUI", "com.nilo.plaf.nimrod.NimRODCheckBoxUI");
    table.put( "RadioButtonUI", "com.nilo.plaf.nimrod.NimRODRadioButtonUI");
    table.put( "FormattedTextFieldUI", "com.nilo.plaf.nimrod.NimRODFormattedTextFieldUI");
    table.put( "SliderUI", "com.nilo.plaf.nimrod.NimRODSliderUI");
    table.put( "SpinnerUI", "com.nilo.plaf.nimrod.NimRODSpinnerUI");
    
    table.put( "ListUI", "com.nilo.plaf.nimrod.NimRODListUI");
    table.put( "ComboBoxUI", "com.nilo.plaf.nimrod.NimRODComboBoxUI");
    table.put( "ScrollBarUI", "com.nilo.plaf.nimrod.NimRODScrollBarUI");
    table.put( "ToolBarUI", "com.nilo.plaf.nimrod.NimRODToolBarUI");
    table.put( "ProgressBarUI", "com.nilo.plaf.nimrod.NimRODProgressBarUI");
    table.put( "ScrollPaneUI", "com.nilo.plaf.nimrod.NimRODScrollPaneUI");
    
    table.put( "TabbedPaneUI", "com.nilo.plaf.nimrod.NimRODTabbedPaneUI");
    table.put( "TableHeaderUI", "com.nilo.plaf.nimrod.NimRODTableHeaderUI");
    table.put( "SplitPaneUI", "com.nilo.plaf.nimrod.NimRODSplitPaneUI");
    
    table.put( "InternalFrameUI", "com.nilo.plaf.nimrod.NimRODInternalFrameUI");
    table.put( "DesktopIconUI", "com.nilo.plaf.nimrod.NimRODDesktopIconUI");
    
    table.put( "ToolTipUI", "com.nilo.plaf.nimrod.NimRODToolTipUI");
    
    // Todo esto, es para sacar un triste menu    
    table.put( "MenuBarUI", "com.nilo.plaf.nimrod.NimRODMenuBarUI");
    table.put( "MenuUI", "com.nilo.plaf.nimrod.NimRODMenuUI");
    table.put( "SeparatorUI", "com.nilo.plaf.nimrod.NimRODSeparatorUI");
    table.put( "PopupMenuUI", "com.nilo.plaf.nimrod.NimRODPopupMenuUI");
    table.put( "PopupMenuSeparatorUI", "com.nilo.plaf.nimrod.NimRODPopupMenuSeparatorUI");
    table.put( "MenuItemUI", "com.nilo.plaf.nimrod.NimRODMenuItemUI");
    table.put( "CheckBoxMenuItemUI", "com.nilo.plaf.nimrod.NimRODCheckBoxMenuItemUI");
    table.put( "RadioButtonMenuItemUI", "com.nilo.plaf.nimrod.NimRODRadioButtonMenuItemUI");

    /*
    for( Enumeration en = table.keys(); en.hasMoreElements(); ) {
      System.out.println( "[" + en.nextElement() + "]");
    }
    */
  }

  protected void initSystemColorDefaults( UIDefaults table) {
    super.initSystemColorDefaults( table);
    
    // Esto es para que todo lo que este seleccionado tenga el mismo color.
    table.put( "textHighlight", getMenuSelectedBackground());
    
    // Y esto, para que se vean bien los textos inactivados.
    table.put( "textInactiveText", getInactiveSystemTextColor().darker());
    
    /*
    for( Enumeration en = table.keys(); en.hasMoreElements(); ) {
      System.out.println( "[" + (String)en.nextElement() + "]");
    }
    */
  }


  protected void initComponentDefaults( UIDefaults table) {
    super.initComponentDefaults( table);

    try {
      ColorUIResource cFore = (ColorUIResource)table.get( "MenuItem.disabledForeground");
      ColorUIResource cBack = (ColorUIResource)table.get( "MenuItem.foreground");
      
      ColorUIResource col = NimRODUtils.getColorTercio( cBack, cFore);
      table.put(  "MenuItem.disabledForeground", col);
      table.put(  "Label.disabledForeground", col);
      table.put(  "CheckBoxMenuItem.disabledForeground", col);
      table.put(  "Menu.disabledForeground", col);
      table.put(  "RadioButtonMenuItem.disabledForeground", col);
      table.put(  "ComboBox.disabledForeground", col);
      table.put(  "Button.disabledText", col);
      table.put(  "ToggleButton.disabledText", col);
      table.put(  "CheckBox.disabledText", col);
      table.put(  "RadioButton.disabledText", col);
      
      ColorUIResource col2 = NimRODUtils.getColorTercio( NimRODLookAndFeel.getWhite(),
                                                         (Color)table.get( "TextField.inactiveBackground"));
      table.put( "TextField.inactiveBackground", col2);
    }
    catch ( Exception ex) {
      ex.printStackTrace();
    }
    
    table.put( "MenuBar.border", NimRODBorders.getMenuBarBorder());

    Font fontMenu = ((Font)table.get( "Menu.font")).deriveFont( Font.BOLD);
    //table.put( "Menu.font", fontMenu);
    //table.put( "MenuItem.font", fontMenu);
    //table.put( "PopupMenu.font", fontMenu);
    //table.put( "RadioButtonMenuItem.font", fontMenu);
    //table.put( "CheckBoxMenuItem.font", fontMenu);
    table.put( "MenuItem.acceleratorFont", fontMenu);
    table.put( "RadioButtonMenuItem.acceleratorFont", fontMenu);
    table.put( "CheckBoxMenuItem.acceleratorFont", fontMenu);
    
    ColorUIResource colAcce = NimRODUtils.getColorTercio( (ColorUIResource)table.get( "MenuItem.foreground"),
                                                          (ColorUIResource)table.get( "MenuItem.acceleratorForeground")
                                                        );

    table.put( "MenuItem.acceleratorForeground", colAcce);
    table.put( "RadioButtonMenuItem.acceleratorForeground", colAcce);
    table.put( "CheckBoxMenuItem.acceleratorForeground", colAcce);
    
    // Para la sombra de los popupmenus
    table.put( "BorderPopupMenu.SombraBajIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/SombraMenuBajo.png"));
    table.put( "BorderPopupMenu.SombraDerIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/SombraMenuDer.png"));
    table.put( "BorderPopupMenu.SombraEsqIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/SombraMenuEsq.png"));
    table.put( "BorderPopupMenu.SombraUpIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/SombraMenuUp.png"));
    table.put( "BorderPopupMenu.SombraIzqIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/SombraMenuIzq.png"));
    
    // Para el JTree
    table.put( "Tree.collapsedIcon", NimRODIconFactory.getTreeCollapsedIcon());
    table.put( "Tree.expandedIcon", NimRODIconFactory.getTreeExpandedIcon());
    table.put( "Tree.closedIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/TreeDirCerrado.png"));
    table.put( "Tree.openIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/TreeDirAbierto.png"));
    table.put( "Tree.leafIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/TreeFicheroIcon.png"));
    table.put( "Tree.PelotillaIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/TreePelotilla.png"));
    
    // Los dialogos de ficheros
    table.put( "FileView.directoryIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogDirCerrado.png"));
    table.put( "FileView.fileIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogFicheroIcon.png"));
    table.put( "FileView.floppyDriveIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogFloppyIcon.png"));
    table.put( "FileView.hardDriveIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogHDIcon.png"));
    table.put( "FileChooser.newFolderIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogNewDir.png"));
    table.put( "FileChooser.homeFolderIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogHome.png"));
    table.put( "FileChooser.upFolderIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogDirParriba.png"));
    table.put( "FileChooser.detailsViewIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogDetails.png"));
    table.put( "FileChooser.listViewIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/DialogList.png"));
    
    // Para los muchos CheckBox y RadioButtons
    table.put( "CheckBoxMenuItem.checkIcon", NimRODIconFactory.getCheckBoxMenuItemIcon());
    table.put( "RadioButtonMenuItem.checkIcon", NimRODIconFactory.getRadioButtonMenuItemIcon());
    
    // La flechica de los combos...
    table.put( "ComboBox.flechaIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/ComboButtonDown.png"));
    table.put( "ComboBox.buttonDownIcon", NimRODIconFactory.getComboFlechaIcon());
    
    // Los iconos de los menus
    table.put( "Menu.checkIcon", NimRODIconFactory.getBandaMenuItemIcon());
    table.put( "MenuItem.checkIcon", NimRODIconFactory.getBandaMenuItemIcon());
    table.put( "MenuCheckBox.iconBase", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/MenuCheckBoxBase.png"));
    table.put( "MenuCheckBox.iconTick", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/MenuCheckBoxTick.png"));
    table.put( "MenuRadioButton.iconBase", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/MenuRadioBase.png"));
    table.put( "MenuRadioButton.iconTick", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/MenuRadioTick.png"));
    table.put( "CheckBox.iconBase", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/CheckBoxBase.png"));
    table.put( "CheckBox.iconTick", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/CheckBoxTick.png"));
    table.put( "RadioButton.iconBase", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/RadioButtonBase.png"));
    table.put( "RadioButton.iconTick", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/RadioButtonTick.png"));
    
    // Iconos para los borders generales
    table.put( "BordeGenSup", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenSup.png"));
    table.put( "BordeGenSupDer", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenSupDer.png"));
    table.put( "BordeGenDer", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenDer.png"));
    table.put( "BordeGenInfDer", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenInfDer.png"));
    table.put( "BordeGenInf", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenInf.png"));
    table.put( "BordeGenInfIzq", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenInfIzq.png"));
    table.put( "BordeGenIzq", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenIzq.png"));
    table.put( "BordeGenSupIzq", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/BordeGenSupIzq.png"));
    
    // Bordes generales
    table.put( "List.border", NimRODBorders.getGenBorder());
    table.put( "ScrollPane.viewportBorder", NimRODBorders.getGenBorder());
    table.put( "Menu.border", NimRODBorders.getGenMenuBorder());
    table.put( "ToolBar.border", NimRODBorders.getToolBarBorder());
    table.put( "TextField.border", NimRODBorders.getTextFieldBorder());
    table.put( "TextArea.border", NimRODBorders.getTextFieldBorder());
    table.put( "FormattedTextField.border", NimRODBorders.getTextFieldBorder());
    table.put( "PasswordField.border", NimRODBorders.getTextFieldBorder());
    table.put( "ToolTip.border", NimRODBorders.getToolTipBorder());
    
    table.put( "Table.focusCellHighlightBorder", NimRODBorders.getCellFocusBorder());
    
    // Esto realmente no es necesario porque no se sobrecarga la clase ScrollPaneUI, pero si no se sobrecarga
    // el borde de ScrollPane, NetBeans 5.5 se queda tieso cuando cierras todas las pestañas del panel principal... 
    table.put( "ScrollPane.border", NimRODBorders.getScrollPaneBorder());
    
    // Para los ToolTips
    ColorUIResource col2 = NimRODUtils.getColorTercio( NimRODLookAndFeel.getFocusColor(),
                                                      (Color)table.get( "TextField.inactiveBackground"));
    table.put( "ToolTip.background", col2);
    table.put( "ToolTip.font", ((Font)table.get( "Menu.font")));
    
    // Para los Spinners
    table.put( "Spinner.editorBorderPainted", new Boolean( false));
    table.put( "Spinner.border", NimRODBorders.getTextFieldBorder());
    table.put( "Spinner.arrowButtonBorder", BorderFactory.createEmptyBorder());
    table.put( "Spinner.nextIcon", NimRODIconFactory.getSpinnerNextIcon());
    table.put( "Spinner.previousIcon", NimRODIconFactory.getSpinnerPreviousIcon());
    
    // Los iconillos de los dialogos
    table.put( "OptionPane.errorIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/Error.png"));
    table.put( "OptionPane.informationIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/Inform.png"));
    table.put( "OptionPane.warningIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/Warn.png"));
    table.put( "OptionPane.questionIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/Question.png"));
    
    // Para el JSlider
    table.put( "Slider.horizontalThumbIcon", NimRODIconFactory.getSliderHorizontalIcon());
    table.put( "Slider.verticalThumbIcon", NimRODIconFactory.getSliderVerticalIcon());
    table.put( "Slider.horizontalThumbIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/HorizontalThumbIconImage.png"));
    table.put( "Slider.verticalThumbIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/VerticalThumbIconImage.png"));
    
    // Para las scrollbars
    table.put( "ScrollBar.horizontalThumbIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/HorizontalScrollIconImage.png"));
    table.put( "ScrollBar.verticalThumbIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/VerticalScrollIconImage.png"));
    table.put( "ScrollBar.northButtonIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/ScrollBarNorthButtonIconImage.png"));
    table.put( "ScrollBar.southButtonIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/ScrollBarSouthButtonIconImage.png"));
    table.put( "ScrollBar.eastButtonIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/ScrollBarEastButtonIconImage.png"));
    table.put( "ScrollBar.westButtonIconImage", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/ScrollBarWestButtonIconImage.png"));
    table.put( "ScrollBar.northButtonIcon", NimRODIconFactory.getScrollBarNorthButtonIcon());
    table.put( "ScrollBar.southButtonIcon", NimRODIconFactory.getScrollBarSouthButtonIcon());
    table.put( "ScrollBar.eastButtonIcon", NimRODIconFactory.getScrollBarEastButtonIcon());
    table.put( "ScrollBar.westButtonIcon", NimRODIconFactory.getScrollBarWestButtonIcon());
    
    // Margenes de los botones
    table.put( "Button.margin", new InsetsUIResource( 5,14, 5,14));
    table.put( "ToggleButton.margin", new InsetsUIResource( 5,14, 5,14));
    
    // Para los InternalFrames y sus iconillos
    table.put( "Desktop.background", table.get( "MenuItem.background"));
    table.put( "InternalFrame.border", NimRODBorders.getInternalFrameBorder());
    
    table.put( "InternalFrame.NimCloseIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameClose.png"));
    table.put( "InternalFrame.NimCloseIconRoll", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameCloseRoll.png"));
    table.put( "InternalFrame.NimCloseIconPush", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameClosePush.png"));
    
    table.put( "InternalFrame.NimMaxIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameMaximiza.png"));
    table.put( "InternalFrame.NimMaxIconRoll", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameMaximizaRoll.png"));
    table.put( "InternalFrame.NimMaxIconPush", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameMaximizaPush.png"));
    
    table.put( "InternalFrame.NimMinIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameMinimiza.png"));
    table.put( "InternalFrame.NimMinIconRoll", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameMinimizaRoll.png"));
    table.put( "InternalFrame.NimMinIconPush", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameMinimizaPush.png"));
    
    table.put( "InternalFrame.NimResizeIcon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameResize.png"));
    table.put( "InternalFrame.NimResizeIconRoll", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameResizeRoll.png"));
    table.put( "InternalFrame.NimResizeIconPush", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/FrameResizePush.png"));
    
    table.put( "InternalFrame.closeIcon", NimRODIconFactory.getFrameCloseIcon());
    table.put( "InternalFrame.minimizeIcon", NimRODIconFactory.getFrameAltMaximizeIcon());
    table.put( "InternalFrame.maximizeIcon", NimRODIconFactory.getFrameMaxIcon());
    table.put( "InternalFrame.iconifyIcon", NimRODIconFactory.getFrameMinIcon());
    table.put( "InternalFrame.icon", NimRODUtils.loadRes( "/com/nilo/plaf/nimrod/icons/Frame.png"));
    table.put( "NimRODInternalFrameIconLit.width", new Integer( 20));
    table.put( "NimRODInternalFrameIconLit.height", new Integer( 20));
    
    Font fontIcon = ((Font)table.get( "InternalFrame.titleFont")).deriveFont( Font.BOLD);
    table.put( "DesktopIcon.font", fontIcon);
    table.put( "NimRODDesktopIcon.width", new Integer( 80));
    table.put( "NimRODDesktopIcon.height", new Integer( 60));
    table.put( "NimRODDesktopIconBig.width", new Integer( 48));
    table.put( "NimRODDesktopIconBig.height", new Integer( 48));
    
    // Esto no se usa dentro del codigo de NimROD LaF, pero SWTSwing y el plugin EoS de Eclipse si lo usan
    table.put( "InternalFrame.activeTitleBackground", getMenuSelectedBackground());
    table.put( "InternalFrame.activeTitleGradient", getMenuSelectedBackground().darker());
    table.put( "InternalFrame.inactiveTitleBackground", getMenuBackground().brighter());
    table.put( "InternalFrame.inactiveTitleGradient", getMenuBackground().darker());
    
    //Esto es solo para saber que hay en la tabla
    /*
    for( Enumeration en = table.keys(); en.hasMoreElements(); ) {
      System.out.println( "[" + en.nextElement() + "]");
    }
    */
  }
}