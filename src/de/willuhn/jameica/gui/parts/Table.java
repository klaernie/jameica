/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/parts/Attic/Table.java,v $
 * $Revision: 1.10 $
 * $Date: 2004/03/18 01:24:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.gui.parts;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.Jameica;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.util.Style;
import de.willuhn.util.I18N;

/**
 * Erzeugt eine Standard-Tabelle.
 * TODO Spaltenbreiten und Sortierung beim Verlassen speichern
 * @author willuhn
 */
public class Table
{

  private DBIterator list;
  private AbstractControl controller;
  private ArrayList fields = new ArrayList();
  private HashMap formatter = new HashMap();
  private Enumeration list2;
  
  private org.eclipse.swt.widgets.Table table;

  /**
   * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
   * @param list Liste mit Objekten, die angezeigt werden soll.
   * @param controller der die ausgewaehlten Daten dieser Liste empfaengt.
   */
  public Table(DBIterator list, AbstractControl controller)
  {
    this.list = list;
    this.controller = controller;
  }
  
	/**
	 * Erzeugt eine neue Standard-Tabelle auf dem uebergebenen Composite.
	 * @param list Liste mit Objekten, die angezeigt werden sollen.
	 * @param controller der die ausgewaehlten Daten dieser Liste empfaengt.
	 */
	public Table(Enumeration list, AbstractControl controller)
	{
		this.list2 = list;
		this.controller = controller;
	}
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   */
  public void addColumn(String title, String field)
  {
    this.fields.add(new String[]{title,field});
  }
  
  /**
   * Fuegt der Tabelle eine neue Spalte hinzu und dazu noch einen Formatierer.
   * @param title Name der Spaltenueberschrift.
   * @param field Name des Feldes aus dem dbObject, der angezeigt werden soll.
   * @param f Formatter, der fuer die Anzeige des Wertes verwendet werden soll.
   */
  public void addColumn(String title, String field, Formatter f)
  {
    addColumn(title,field);
    
    if (field != null && f != null)
      formatter.put(field,f);
  }

  
  /**
   * Malt die Tabelle auf das uebergebene Composite.
   * @param parent das Composite, auf dem die Tabelle plaziert werden soll.
   * @throws RemoteException
   */
  public void paint(Composite parent) throws RemoteException
  {

		I18N i18n = PluginLoader.getPlugin(Jameica.class).getResources().getI18N();

    Composite comp = new Composite(parent,SWT.NONE);
    // final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
		final GridData gridData = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
    comp.setLayoutData(gridData);
    comp.setBackground(Style.COLOR_BORDER);
    
    comp.setLayout(new FormLayout());

    FormData comboFD = new FormData();
    comboFD.left = new FormAttachment(0, 1);
    comboFD.top = new FormAttachment(0, 1);
    comboFD.right = new FormAttachment(100, -1);
    comboFD.bottom = new FormAttachment(100, -1);


    int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

    table = new org.eclipse.swt.widgets.Table(comp, style);
    table.setLayoutData(comboFD);
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    
    // Das hier ist eigentlich nur noetig um die Ausrichtung der Spalten zu ermitteln
    DBObject _o = null;

		try {
			_o = list.next();
		}
		catch (Exception e)
		{
			// nicht weiter tragisch, wenn das fehlschlaegt
		}

    for (int i=0;i<this.fields.size();++i)
    {
      final TableColumn col = new TableColumn(table, SWT.NONE);
      String[] fieldData = (String[]) fields.get(i);
      String title = fieldData[0];
      String field = fieldData[1];
      if (title == null) title = "";
      try {
        String type = _o.getFieldType(field);
        if (
          DBObject.FIELDTYPE_DOUBLE.equalsIgnoreCase(type) ||
          DBObject.FIELDTYPE_DECIMAL.equalsIgnoreCase(type)
        )
          col.setAlignment(SWT.RIGHT);
      }
      catch (Exception e)
      {
        // nicht weiter tragisch, wenn das fehlschlaegt
      }
      col.setText(title);

			// Sortierung
			final int p = i;
			col.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					orderBy(p);
				}
			});

    }

    try {
      list.previous(); // zurueckblaettern nicht vergessen
    }
    catch (Exception e) {
      // nicht weiter tragisch, wenn das fehlschlaegt
    }


    // Iterieren ueber alle Elemente der Liste
		if (list != null)
		{
			while (list.hasNext())
			{
				final TableItem item = new TableItem(table, SWT.NONE);
				final DBObject o = list.next();
				for (int i=0;i<this.fields.size();++i)
				{
					String[] fieldData = (String[]) fields.get(i);
					String field = fieldData[1];
					item.setData(o);
					Object value = o.getField(field);
					if (value == null)
					{
						// Wert ist null. Also zeigen wir einen Leerstring an
						item.setText(i,"");
					}
					else if (value instanceof DBObject)
					{
						// Wert ist ein Fremdschluessel. Also zeigen wir dessn Wert an
						DBObject dbo = (DBObject) value;
						item.setText(i,dbo.getField(dbo.getPrimaryField()).toString());
					}
					else
					{
						// Regulaerer Wert.
						// Wir schauen aber noch, ob wir einen Formatter haben
						Formatter f = (Formatter) formatter.get(field);
						if (f != null)
						{
							item.setText(i,f.format(value));
						}
						else 
							item.setText(i,value.toString());
					}
				}
			}
		}
		else if (list2 != null)
		{
			while (list2.hasMoreElements())
			{
				final TableItem item = new TableItem(table, SWT.NONE);
				final Object o = list2.nextElement();
				if (o == null)
					item.setText(0,"");
				else
					item.setText(0,o.toString());
					item.setData(o.toString());
			}
		}

    // noch der Listener fuer den Doppelklick drauf.
    table.addListener(SWT.MouseDoubleClick,
      new Listener(){
        public void handleEvent(Event e){
        		if (controller == null) return;
          TableItem item = table.getItem( new Point(e.x,e.y));
            if (item == null) return;
            Object o = item.getData();
            if (o == null) return;
            controller.handleOpen(o);
        }
      }
    );

    // und jetzt fuegen wir noch die Kontext-Menues hinzu,
    Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
    table.setMenu(menu);
    {
      MenuItem editItem = new MenuItem(menu, SWT.PUSH);
      editItem.setText(i18n.tr("Neu..."));
      editItem.addListener (SWT.Selection, new Listener () {
        public void handleEvent (Event e) {
          controller.handleCreate();
        }
      });
    }
    {
      MenuItem editItem = new MenuItem(menu, SWT.PUSH);
      editItem.setText(i18n.tr("�ffnen"));
      editItem.addListener (SWT.Selection, new Listener () {
        public void handleEvent (Event e) {
          int i = table.getSelectionIndex();
          if (i == -1)
            return;
          TableItem item = table.getItem(i);
          if (item == null) return;
          Object o = item.getData();
          if (o == null) return;
          controller.handleOpen(o);
        }
      });
    }

    // Jetzt tun wir noch die Spaltenbreiten neu berechnen.
    int cols = table.getColumnCount();
    for (int i=0;i<cols;++i)
    {
      TableColumn col = table.getColumn(i);
      col.pack();
    }
    //table.pack();

    // So, und jetzt malen wir noch ein Label mit der Anzahl der Treffer drunter.
		if (list != null)
		{
			Label summary = new Label(parent,SWT.NONE);
			summary.setBackground(Style.COLOR_BG);
			summary.setText(list.size() + " " + (list.size() == 1 ? i18n.tr("Datensatz") : i18n.tr("Datens�tze")) + ".");
		}

    // Und jetzt rollen wir noch den Pointer der Tabelle zurueck.
    // Damit kann das Control wiederverwendet werden ;) 
    try {
			if (list != null)
	      this.list.begin();
    }
    catch (RemoteException e)
    {
      Application.getLog().warn("unable to restore list back to first element. this list will not be reusable.");
    }

  }


	/**
	 * Sortiert die Tabelle nach der angegebenen Spaltennummer.
   * @param index Spaltennummer.
   */
  public void orderBy(int index)
	{
		final TableItem[] items = table.getItems();
		Collator collator = Collator.getInstance(Application.getConfig().getLocale());

		for (int s = 1; s < items.length; ++s)
		{

			String value1 = table.getItem(s).getText(index);

			for (int j = 0; j < s; ++j)
			{

				String value2 = table.getItem(j).getText(index);

				if (collator.compare(value1, value2) < 0)
				{
					final TableItem item = new TableItem(table, SWT.NONE, j);
					for (int r=0;r<table.getColumnCount();++r)
					{
						item.setText(r,items[s].getText(r));
						// TODO: Hier werden die Events und data Objekte vergessen
					}
					items[s].dispose();
					break;
				}

			}
		}
	}
}

/*********************************************************************
 * $Log: Table.java,v $
 * Revision 1.10  2004/03/18 01:24:47  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/11 08:56:55  willuhn
 * @C some refactoring
 *
 * Revision 1.8  2004/03/06 18:24:23  willuhn
 * @D javadoc
 *
 * Revision 1.7  2004/03/03 22:27:10  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.6  2004/02/26 18:47:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/02/24 22:46:53  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/22 20:05:21  willuhn
 * @N new Logo panel
 *
 * Revision 1.3  2004/02/18 17:14:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/18 01:40:29  willuhn
 * @N new white style
 *
 * Revision 1.1  2004/01/28 20:51:24  willuhn
 * @C gui.views.parts moved to gui.parts
 * @C gui.views.util moved to gui.util
 *
 * Revision 1.19  2004/01/23 00:29:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/01/08 20:50:32  willuhn
 * @N database stuff separated from jameica
 *
 * Revision 1.17  2004/01/06 20:11:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/01/06 01:27:30  willuhn
 * @N table order
 *
 * Revision 1.15  2004/01/04 19:51:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/01/03 18:08:06  willuhn
 * @N Exception logging
 * @C replaced bb.util xml parser with nanoxml
 *
 * Revision 1.13  2003/12/29 20:07:19  willuhn
 * @N Formatter
 *
 * Revision 1.12  2003/12/26 21:43:30  willuhn
 * @N customers changable
 *
 * Revision 1.11  2003/12/19 01:43:27  willuhn
 * @N added Tree
 *
 * Revision 1.10  2003/12/11 21:00:54  willuhn
 * @C refactoring
 *
 * Revision 1.9  2003/12/10 00:47:12  willuhn
 * @N SearchDialog done
 * @N FatalErrorView
 *
 * Revision 1.8  2003/12/08 15:41:09  willuhn
 * @N searchInput
 *
 * Revision 1.7  2003/12/05 17:12:23  willuhn
 * @C SelectInput
 *
 * Revision 1.6  2003/11/27 00:22:18  willuhn
 * @B paar Bugfixes aus Kombination RMI + Reflection
 * @N insertCheck(), deleteCheck(), updateCheck()
 * @R AbstractDBObject#toString() da in RemoteObject ueberschrieben (RMI-Konflikt)
 *
 * Revision 1.5  2003/11/24 23:01:58  willuhn
 * @N added settings
 *
 * Revision 1.4  2003/11/24 17:27:50  willuhn
 * @N Context menu in table
 *
 * Revision 1.3  2003/11/24 16:25:53  willuhn
 * @N AbstractDBObject is now able to resolve foreign keys
 *
 * Revision 1.2  2003/11/24 11:51:41  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2003/11/22 20:43:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/