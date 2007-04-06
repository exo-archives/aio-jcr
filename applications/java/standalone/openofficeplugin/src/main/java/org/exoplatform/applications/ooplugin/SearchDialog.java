/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SearchDialog extends BrowseDialog {
  
  public static final String DIALOG_NAME = "_SearchDialog"; 
  
  public static final String EDT_TEXT = "edtText";
  public static final String BTN_SEARCH = "btnSearch";
  public static final String BTN_OPEN = "btnOpen";
  
  private String searchContent = "";
  private Thread searchThread; 

  public SearchDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit) {
    super(config, xComponentContext, xFrame, xToolkit);
    dialogName = DIALOG_NAME;
    
    addHandler(LST_ITEMS, Component.XTYPE_XLISTBOX, new ListItemsClick());
    addHandler(BTN_SEARCH, Component.XTYPE_XBUTTON, new SearchClick());
    addHandler(BTN_OPEN, Component.XTYPE_XBUTTON, new OpenClick());
    isNeedAddHandlers = false;
  }

  protected void disableAll() {
    super.disableAll();
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(BTN_SEARCH))).setEnable(true);        
  }
  
  protected void enableAll() {
    super.enableAll();
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(BTN_SEARCH))).setEnable(true);          
  }
  
  private class SearchClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      
      try {
        disableAll();
        
        XTextComponent xEdtText = (XTextComponent)UnoRuntime.queryInterface(
            XTextComponent.class, xControlContainer.getControl(EDT_TEXT));
        searchContent = xEdtText.getText();
        
        searchThread = new SearchThread();
        searchThread.start();
        
      } catch (Exception exc) {
        Log.info("Unhandled exception", exc);
      }
      
    }

  }
  
  private class SearchThread extends Thread {
    public void run() {
      try {
        DavSearch davSearch = new DavSearch(config.getContext());
        davSearch.setResourcePath("/");
        
        SQLQuery sqlQuery = new SQLQuery();
        sqlQuery.setQuery("select * from nt:base where contains(*, '" + searchContent + "')");
        
        davSearch.setQuery(sqlQuery);
        
        int status = davSearch.execute();
        
        if (status != Const.HttpStatus.MULTISTATUS) {
          showMessageBox("Search error! Code: " + status);
          return;
        }
        
        Multistatus multistatus = davSearch.getMultistatus();
        responses = multistatus.getResponses();
        
        fillItemsList();
        if (responses.size() == 0) {
          showMessageBox("No files found!");
        }

        enableAll();
      } catch (java.lang.Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
      
    }
  }
  
  private boolean tryOpenSelected() throws Exception {
    XListBox xListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xControlContainer.getControl(LST_ITEMS));
    int selectedPos = xListBox.getSelectedItemPos();
    if (selectedPos < 0) {
      return false;
    }

    ResponseDoc response = responses.get(selectedPos);
    
    if (isCollection(response)) {
      return false ;
    }

    String href = TextUtils.UnEscape(response.getHref(), '%');
    doOpenRemoteFile(href);              
    return true;
  }
  
  private class ListItemsClick extends ActionListener {

    public void actionPerformed(ActionEvent arg0) {
      try {
        tryOpenSelected();
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
      
    }
    
  }  
  
  private class OpenClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      try {
        tryOpenSelected();
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
      
    }

  }  
  
}
