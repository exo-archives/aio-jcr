/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SettingsDialog extends PlugInDialog {
  
  public static final String NAME = "_SettingsDialog";
  
  public static final String BTN_SAVE = "btnSave";
  
  public static final String EDT_SERVERNAME = "edtServerName";
  public static final String EDT_PORT = "edtPort";
  public static final String EDT_SERVLET = "edtServlet";
  public static final String EDT_WORKSPACE = "edtWorkSpace";
  public static final String EDT_USER = "edtUserName";
  public static final String EDT_PASS = "edtPassword";
  
  private Thread launchThread;

  public SettingsDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit) {
    super(config, xComponentContext, xFrame, xToolkit);
    dialogName = NAME;
    
    addHandler(BTN_SAVE, Component.XTYPE_XBUTTON, new SaveClick());
    
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
        
        setTextBoxValue(EDT_SERVERNAME, config.getHost());
        setTextBoxValue(EDT_PORT, "" + config.getPort());
        setTextBoxValue(EDT_SERVLET, config.getServlet());
        setTextBoxValue(EDT_WORKSPACE, config.getWorkSpace());
        setTextBoxValue(EDT_USER, config.getUserId());
        setTextBoxValue(EDT_PASS, config.getUserPass());
                
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage());
      }
    }
  }  
  
  protected void setTextBoxValue(String componentName, String textValue) {
    XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
        XTextComponent.class, xControlContainer.getControl(componentName));
    xComboText.setText(textValue);          
  }
  
  protected String getTextBoxValue(String componentName) {
    XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
        XTextComponent.class, xControlContainer.getControl(componentName));
    return xComboText.getText();      
  }
  
  private class SaveClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      try {
        String host = getTextBoxValue(EDT_SERVERNAME);
        int port = new Integer(getTextBoxValue(EDT_PORT));
        String path = getTextBoxValue(EDT_SERVLET);
        String workSpace = getTextBoxValue(EDT_WORKSPACE);
        String userId = getTextBoxValue(EDT_USER);
        String userPass = getTextBoxValue(EDT_PASS);
        
        config.setHost(host);
        config.setPort(port);
        config.setServlet(path);
        config.setWorkSpace(workSpace);
        config.setUserId(userId);
        config.setUserPath(userPass);
        
        config.saveConfig();        
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
        showMessageBox("Parameters incorrect!!!");
      }
      
      xDialog.endExecute();      
    }

  }  
  
}
