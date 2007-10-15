/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;
import org.exoplatform.applications.ooplugin.events.ItemListener;
import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.VersionNameProp;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.ItemEvent;
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

public class OpenDialog extends BrowseDialog {
    
  private static final String DIALOGNAME = "_OpenDialog";
  
  public static final String BTN_VERSIONS = "btnVersions";
  public static final String BTN_OPEN = "btnOpen";
  
    
  private Thread launchThread;
  private Thread viewVersionEnableThread;
  
  public OpenDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit) {
    super(config, xComponentContext, xFrame, xToolkit);
    dialogName = DIALOGNAME;
    
    addHandler(BTN_OPEN, Component.XTYPE_XBUTTON, new OpenClick());
    
    addHandler(COMBO_PATH, Component.XTYPE_XCOMBOBOX, new PathChanged());
    addHandler(BTN_VERSIONS, Component.XTYPE_XBUTTON, new VersionsClick());
    
    launchThread = new LaunchThread();
    launchThread.start();
  }  
  
  private class LaunchThread extends Thread {
    public void run() {
      try {
        while (!enabled) {
          Thread.sleep(100);
        }
        Thread.sleep(100);
        
        viewVersionEnableThread = new ViewVersionsButtonEnableThread();
        viewVersionEnableThread.start();
        
        doPropFind();
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage());
      }
    }
  }

  protected void enableVersionView(boolean isEnabled) {
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(BTN_VERSIONS))).setEnable(isEnabled);    
  }
  
  private class ViewVersionsButtonEnableThread extends Thread {
    
    public void run() {
      while (true) {
        try {
          Thread.sleep(100);          
          int selectedPos = getSelectedItemPos();
          
          if (selectedPos >= 0) {
            ((XWindow)UnoRuntime.queryInterface(
                XWindow.class, xControlContainer.getControl(BTN_OPEN))).setEnable(true);
            
            ResponseDoc response = responses.get(selectedPos);
            VersionNameProp versionNameProperty = 
                (VersionNameProp)response.getProperty(Const.DavProp.VERSIONNAME);
            if (versionNameProperty != null && 
                versionNameProperty.getStatus() == Const.HttpStatus.OK) {
              enableVersionView(true);
              continue;
            }
            
          } else {
            ((XWindow)UnoRuntime.queryInterface(
                XWindow.class, xControlContainer.getControl(BTN_OPEN))).setEnable(false);
          }
          
        } catch (Exception exc) {
        }

        enableVersionView(false);
      }
      
    }
    
  }
  
  protected void disableAll() {
    super.disableAll();
//    ((XWindow)UnoRuntime.queryInterface(
//        XWindow.class, xControlContainer.getControl(BTN_OPEN))).setEnable(false);    
  }
  
  protected void enableAll() {
    super.enableAll();
//    ((XWindow)UnoRuntime.queryInterface(
//        XWindow.class, xControlContainer.getControl(BTN_OPEN))).setEnable(true);    
  }  
  
  private class PathChanged extends ItemListener {
    
    public void itemStateChanged(ItemEvent arg0) {
      XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
          XTextComponent.class, xControlContainer.getControl(COMBO_PATH));
      String path = xComboText.getText();
      
      String serverPrefix = config.getContext().getServerPrefix();
      
      if (!path.startsWith(serverPrefix)) {
        Log.info("Can't connect remote WebDav server!!!");
        return;
      }
      
      path = path.substring(serverPrefix.length());
      if ("".equals(path)) {
        path = "/";
      }
      
      currentPath = path;
      doPropFind();
    }    
  }
  
  private class OpenClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      doSelectItem();
    }

  }

  private class VersionsClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      
      try {
        int selectedPos = getSelectedItemPos();
        
        if (selectedPos < 0) {
          return;
        }
        
        ResponseDoc response = responses.get(selectedPos);
        String href = TextUtils.UnEscape(response.getHref(), '%');
        
        if (!href.startsWith(config.getContext().getServerPrefix())) {
          showMessageBox("Can't load version list.");
          return;
        }
        
        String remoteHref = href.substring(config.getContext().getServerPrefix().length());
        
        prepareTmpPath(currentPath);
        
        ViewVersions opVersions = new ViewVersions(config, xComponentContext, xFrame, xToolkit, remoteHref);
        boolean needClose = opVersions.createDialogEx();
        
        if (needClose) {
          xDialog.endExecute();
        }
        
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }      
      
    }

  }
  
}
