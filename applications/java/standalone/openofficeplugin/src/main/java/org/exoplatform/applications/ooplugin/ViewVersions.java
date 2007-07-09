/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.util.ArrayList;

import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;
import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp;
import org.exoplatform.frameworks.webdavclient.properties.CreationDateProp;
import org.exoplatform.frameworks.webdavclient.properties.CreatorDisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XToolkit;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ViewVersions extends PlugInDialog {

  private static final String DIALOG_NAME = "_ViewVersionsDialog";
  
  public static final String LST_VERSIONS = "lstVersions";
  public static final String LBL_TABLEHEAD = "lblTableHead";
  public static final String BTN_OPEN = "btnOpen"; 
  
  public static final int NAME_LEN = 14;
  public static final int SIZE_LEN = 24;
  public static final int CREATED_LEN = 45;
  
  
  private Thread launchThread;
  
  private String resourcePath = "/";
  
  private ArrayList<ResponseDoc> responses = new ArrayList<ResponseDoc>();
  
  private boolean isOpened = false;
  
  public ViewVersions(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit, String resourcePath) {
    super(config, xComponentContext, xFrame, xToolkit);
    dialogName = DIALOG_NAME;

    this.resourcePath = resourcePath;
    
    addHandler(LST_VERSIONS, Component.XTYPE_XLISTBOX, new DoSelectFileClick());
    addHandler(BTN_OPEN, Component.XTYPE_XBUTTON, new DoSelectFileClick());
    
    launchThread = new LaunchThread();
    launchThread.start();
  }
  
  public boolean launchBeforeOpen() {
    try {      
      XFixedText xLabelHead = (XFixedText)UnoRuntime.queryInterface(
          XFixedText.class, xControlContainer.getControl(LBL_TABLEHEAD));
      
      String headerValue = "Version Name";
      while (headerValue.length() < NAME_LEN) {
        headerValue += " ";
      }

      headerValue += "Size";
      while (headerValue.length() < SIZE_LEN) {
        headerValue += " ";
      }
      
      headerValue += "Created";
      while (headerValue.length() < CREATED_LEN) {
        headerValue += " ";
      }
      
      headerValue += "Owner";
      
      xLabelHead.setText(headerValue);
      
    } catch (Exception exc) {
      Log.info("Unhandled exception", exc);
    }
    
    return true;
  }  
  
  private class LaunchThread extends Thread {
    public void run() {
      try {
        while (!enabled) {
          Thread.sleep(100);
        }
        Thread.sleep(100);
        doReport();
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage());
      }
    }
  }
  
  public boolean createDialogEx() throws com.sun.star.uno.Exception {
    super.createDialog();
    return isOpened;
  }
  
  private boolean doReport() throws Exception {  
    DavReport davReport = new DavReport(config.getContext());
    davReport.setResourcePath(resourcePath);
    
    davReport.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davReport.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
    davReport.setRequiredProperty(Const.DavProp.CREATIONDATE);
    davReport.setRequiredProperty(Const.DavProp.CREATORDISPLAYNAME);
    
    int status = davReport.execute();
    if (status != Const.HttpStatus.MULTISTATUS) {
      showMessageBox("Can't open version list. ErrorCode: " + status);
      return false;
    }
    
    Multistatus multistatus = davReport.getMultistatus();    
    responses = multistatus.getResponses();

    XListBox xListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xControlContainer.getControl(LST_VERSIONS));
    xListBox.removeItems((short)0, xListBox.getItemCount());    
        
    for (int i = responses.size() - 1; i >= 0 ; i--) {
      ResponseDoc curResponse = responses.get(i);
      xListBox.addItem(formatLine(curResponse), (short)0);
    }
    
    return true;
  }
    
  private String formatLine(ResponseDoc response) {
    DisplayNameProp displayNameProperty = (DisplayNameProp)response.getProperty(Const.DavProp.DISPLAYNAME);    
    ContentLengthProp contentLengthProperty = (ContentLengthProp)response.getProperty(Const.DavProp.GETCONTENTLENGTH);
    CreationDateProp creationDateProperty = (CreationDateProp)response.getProperty(Const.DavProp.CREATIONDATE);
    CreatorDisplayNameProp creatorDisplayName = (CreatorDisplayNameProp)response.getProperty(Const.DavProp.CREATORDISPLAYNAME);
    
    String lineStr = displayNameProperty.getDisplayName();
    while (lineStr.length() < NAME_LEN) {
      lineStr += " ";
    }
    
    if (contentLengthProperty != null) {
      lineStr += contentLengthProperty.getContentLength();
    }
     
    while (lineStr.length() < SIZE_LEN) {
      lineStr += " ";
    }
    
    if (creationDateProperty != null) {
      lineStr += creationDateProperty.getCreationDate();
    }
    
    while (lineStr.length() < CREATED_LEN) {
      lineStr += " ";
    }
    
    if (creatorDisplayName != null) {
      lineStr += creatorDisplayName.getCreatorDisplayName();
    }
        
    return lineStr;
  }
  
  protected void doOpenVersion() {
    try {
      XListBox xListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xControlContainer.getControl(LST_VERSIONS));
      short selectedItem = xListBox.getSelectedItemPos();
      if (selectedItem < 0) {
        return;
      }
      
      ResponseDoc response = responses.get(selectedItem);
      String href = TextUtils.UnEscape(response.getHref(), '%');

      doOpenRemoteFile(href);
      isOpened = true;
      xDialog.endExecute();
      
    } catch (Exception exc) {
      Log.info("Unhandled exception.", exc);
      showMessageBox("Can't open selected version.");
    }
  }
  
  private class DoSelectFileClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      doOpenVersion();
    }
    
  }
  
}
