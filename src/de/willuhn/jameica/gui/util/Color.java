/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/util/Color.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/10/05 11:24:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.util;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Settings;

/**
 * Farbwerte.
 * Wir haben die SWT-Farben hier nochmal gekapselt, damit wir sie
 * in Properties-Dateien speichern koennen.
 */
public class Color {
	
	/**
	 * Hintergrundfarbe von Widgets.
	 * @deprecated Stattdessen sollte nur noch Color.BACKGROUND verwendet werden.
	 */
	public final static Color WIDGET_BG   = new Color(null,	                new RGB(255,255,255));

  /**
   * Vordergrundfarbe von Widgets.
   * @deprecated Stattdessen sollte nur noch Color.FOREGROUND verwendet werden.
   */
  public final static Color WIDGET_FG   = new Color(null,           		  new RGB(0,0,0));

  /**
   * Hintergrundfarbe der Views.
   * Nicht mehr konfigurierbar, weil es eine Reihe
   * von Widgets gibt, bei denen das ohnehin nicht funktioniert
   */
  public final static Color BACKGROUND 	= new Color(null,		              GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB());

  /**
   * Vordergrundfarbe der Views.
   * Nicht konfigurierbar, damit das auch auf dunklen Themes funktioniert.
   */
  public final static Color FOREGROUND  = new Color(null,                 GUI.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB());

  /**
   * Weiss.
   */
  public final static Color WHITE       = new Color(null,                 GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE).getRGB());

  /**
   * Schwarz.
   */
  public final static Color BLACK       = new Color(null,                 GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK).getRGB());

  /**
   * Rahmenfarbe von Elementen (zB Tabellen).
   */
  public final static Color BORDER 			= new Color("color.border",				new RGB(0,0,0));

  /**
   * Farbe von Kommentaren.
   */
  public final static Color COMMENT 		= new Color("color.comment",			new RGB(140,140,140));

  /**
   * Farbe von Fehlertexten.
   */
  public final static Color ERROR				= new Color("color.error",				new RGB(250,10,10));

  /**
   * Farbe von Erfolgsmeldungen.
   */
  public final static Color SUCCESS			= new Color("color.success",			new RGB(0,130,32));

  /**
   * Farbe von Links.
   */
  public final static Color LINK				= new Color("color.link",					new RGB(0,0,100));

  /**
   * Farbe von aktiven Links.
   */
  public final static Color LINK_ACTIVE	= new Color("color.link.active",	new RGB(0,0,200));

  /**
   * Hintergrundfarbe von Pflichtfeldern.
   */
  public final static Color MANDATORY_BG = new Color("color.mandatory.bg",  new RGB(255,240,220));

  private String name;
	private RGB value;
	private RGB defaultValue;
	private static Settings settings = new Settings(Color.class);
  
  private static HashMap<String, org.eclipse.swt.graphics.Color> colorCache = new HashMap<String, org.eclipse.swt.graphics.Color>();
  
  /**
	 * ct.
   * @param name Alias-Name der Farbe.
   * @param defaultValue Default-Farbwert.
   */
  public Color(String name,RGB defaultValue)
	{
		this.name = name;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Liefert das zugehoerige SWT-Color-Objekt.
   * @return die zugehoerige SWT-Farbe.
   */
  public final org.eclipse.swt.graphics.Color getSWTColor()
	{
		if (value == null)
  		value = name != null ? settings.getRGB(name,defaultValue) : defaultValue;
      
    org.eclipse.swt.graphics.Color c = colorCache.get(value.toString());
    if (c != null && !c.isDisposed())
      return c;
    
    c = new org.eclipse.swt.graphics.Color(GUI.getDisplay(),value);
    colorCache.put(value.toString(),c);
    return c;
	}
	
  /**
   * Speichert einen neuen Farbwert.
   * @param newColor die neue Farbe.
   */
  public final void setSWTColor(org.eclipse.swt.graphics.Color newColor)
	{
		if (newColor == null || newColor.isDisposed())
			return;
		value = newColor.getRGB();
    
    if (name != null)
      settings.setAttribute(name,value);
    
    colorCache.remove(value.toString());
	}

	/**
   * Setzt die Farbe wieder auf den Default-Wert zurueck.
   */
  public final void reset()
	{
		value = defaultValue;
    if (name != null)
      settings.setAttribute(name,value);

    colorCache.remove(value.toString());
  }
}
