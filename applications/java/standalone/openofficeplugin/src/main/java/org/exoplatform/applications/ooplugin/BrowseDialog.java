/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;
import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.Log;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.CommonProp;
import org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.LastModifiedProp;
import org.exoplatform.frameworks.webdavclient.properties.ResourceTypeProp;
import org.exoplatform.frameworks.webdavclient.properties.VersionNameProp;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XComboBox;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class BrowseDialog extends PlugInDialog {
  
  public static final int VNAME_LEN = 3;
  public static final int NAME_LEN = 26;
  public static final int SIZE_LEN = 38;
  public static final int LASTMODOFIED_SIZE = 69;
  public static final int MIMETYPE_SIZE = 89;
  
  public static final String BTN_PREV = "btnPrev";
  public static final String LST_ITEMS = "lstItems";
  public static final String COMBO_PATH = "comboPath";
  public static final String BTN_CANCEL = "btnCancel";
  
  public static final String LBL_TABLEHEAD = "lblTableHead";
  
  public static final String JCR_MIMETYPE = "jcr:mimeType";
  
  public static final String JCR_NAMESPACE = "http://www.jcp.org/jcr/1.0";
  
  protected String currentPath = "/";
  
  protected Thread openThread;  
  
  protected ArrayList<ResponseDoc> responses = new ArrayList<ResponseDoc>();
  
  protected boolean isNeedAddHandlers = true;
  
  public BrowseDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit) {
    super(config, xComponentContext, xFrame, xToolkit);    
  }
  
  public void createDialog() throws com.sun.star.uno.Exception {
    if (isNeedAddHandlers) {
      addHandler(LST_ITEMS, Component.XTYPE_XLISTBOX, new ListItemsClick());
      addHandler(BTN_PREV, Component.XTYPE_XBUTTON, new PrevClick());      
    }
    
    super.createDialog();
  }
  
  public boolean launchBeforeOpen() {
    try {      
      XFixedText xLabelHead = (XFixedText)UnoRuntime.queryInterface(
          XFixedText.class, xControlContainer.getControl(LBL_TABLEHEAD));
      
      String headerValue = "*";
      while (headerValue.length() < VNAME_LEN) {
        headerValue += " ";
      }

      headerValue += "Name";
      while (headerValue.length() < NAME_LEN) {
        headerValue += " ";
      }
      
      headerValue += "Size";
      while (headerValue.length() < SIZE_LEN) {
        headerValue += " ";
      }
      
      headerValue += "Last Modified";
      while (headerValue.length() < LASTMODOFIED_SIZE) {
        headerValue += " ";
      }
      
      headerValue += "Mime-Type";
      while (headerValue.length() < MIMETYPE_SIZE) {
        headerValue += " ";
      }
      
      headerValue += "Comments";
      
      xLabelHead.setText(headerValue);
      
    } catch (Exception exc) {
      Log.info("Unhandled exception", exc);
    }
    
    return true;
  }  
  
  protected void disableAll() {
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(LST_ITEMS))).setEnable(false);    
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(BTN_CANCEL))).setEnable(false);
    
    if (xControlContainer.getControl(BTN_PREV) != null) {
      ((XWindow)UnoRuntime.queryInterface(
          XWindow.class, xControlContainer.getControl(BTN_PREV))).setEnable(false);          
    }
    
    if (xControlContainer.getControl(COMBO_PATH) != null) {
      ((XWindow)UnoRuntime.queryInterface(
          XWindow.class, xControlContainer.getControl(COMBO_PATH))).setEnable(false);          
    }    
  }
  
  protected void enableAll() {
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(LST_ITEMS))).setEnable(true);
    
    if (xControlContainer.getControl(COMBO_PATH) != null) {
      ((XWindow)UnoRuntime.queryInterface(
          XWindow.class, xControlContainer.getControl(COMBO_PATH))).setEnable(true);      
    }
    
    if (xControlContainer.getControl(BTN_PREV) != null) {
      if (!"/".equals(currentPath)) {
        ((XWindow)UnoRuntime.queryInterface(
            XWindow.class, xControlContainer.getControl(BTN_PREV))).setEnable(true);    
      }      
    }
    
    ((XWindow)UnoRuntime.queryInterface(
        XWindow.class, xControlContainer.getControl(BTN_CANCEL))).setEnable(true);      
  }    
  
  protected boolean isCollection(ResponseDoc response) {
    ResourceTypeProp resourceTypeProperty = 
      (ResourceTypeProp)response.getProperty(Const.DavProp.RESOURCETYPE);
    if (resourceTypeProperty == null) {
      return false;
    }
    return resourceTypeProperty.isCollection();
  }  
  
  protected String formatResponseLine(ResponseDoc response) {    
    String fileItem = "";
    
    VersionNameProp versionNameProperty = 
        (VersionNameProp)response.getProperty(Const.DavProp.VERSIONNAME);
    if ((versionNameProperty != null) &&
        (versionNameProperty.getStatus() == Const.HttpStatus.OK)) {
      fileItem += "*";
    }
    
    while (fileItem.length() < VNAME_LEN) {
     fileItem += " ";
    }
    
    DisplayNameProp displayNameProperty = 
      (DisplayNameProp)response.getProperty(Const.DavProp.DISPLAYNAME);
    ResourceTypeProp resourceTypeProperty =
      (ResourceTypeProp)response.getProperty(Const.DavProp.RESOURCETYPE);
    
    if (resourceTypeProperty != null && resourceTypeProperty.isCollection()) {
      fileItem += "[ ";
      fileItem += displayNameProperty.getDisplayName();
      fileItem += " ]";
    } else {
      fileItem += displayNameProperty.getDisplayName();      
    }
    while (fileItem.length() < NAME_LEN) {
      fileItem += " ";
    }
    
    ContentLengthProp contentLengthProperty =
      (ContentLengthProp)response.getProperty(Const.DavProp.GETCONTENTLENGTH);
    if (contentLengthProperty != null) {
      fileItem += contentLengthProperty.getContentLength();
    }
    while (fileItem.length() < SIZE_LEN) {
      fileItem += " ";
    }
    
    LastModifiedProp lastModifiedProperty =
      (LastModifiedProp)response.getProperty(Const.DavProp.GETLASTMODIFIED);
    if (lastModifiedProperty != null) {
      fileItem += lastModifiedProperty.getLastModified();
    }
    
    while (fileItem.length() < LASTMODOFIED_SIZE) {
      fileItem += " ";
    }
    
    CommonProp mimeTypeProperty = (CommonProp)response.getProperty(JCR_MIMETYPE);
    
    if (mimeTypeProperty != null) {
      fileItem += mimeTypeProperty.getValue();
    }
    
    while (fileItem.length() < MIMETYPE_SIZE) {
      fileItem += " ";
    }
    
    CommonProp commentProperty =
      (CommonProp)response.getProperty(Const.DavProp.COMMENT);
    if (commentProperty != null) {
      fileItem += commentProperty.getValue();
    }
  
    return fileItem;
  }
  
  protected void fillItemsList() {
    XListBox xListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xControlContainer.getControl(LST_ITEMS));    
    xListBox.removeItems((short)0, xListBox.getItemCount());    
    
    for (int i = responses.size() - 1; i >= 0; i--) {
      ResponseDoc response = responses.get(i);
      xListBox.addItem(formatResponseLine(response), (short)0);
    }    
  }
    
  protected void doPropFind() {
    disableAll();
    openThread = new OpenThread();
    openThread.start();    
  }  
  
  protected class OpenThread extends Thread {    
    
    public void run() {
      try {    	  
        DavPropFind davPropFind = new DavPropFind(config.getContext());
        davPropFind.setResourcePath(currentPath);
        
        davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
        davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
        
        davPropFind.setRequiredProperty(Const.DavProp.GETLASTMODIFIED);
        
        davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
        davPropFind.setRequiredProperty(Const.DavProp.VERSIONNAME);
        davPropFind.setRequiredProperty(Const.DavProp.COMMENT);
        
        davPropFind.setRequiredProperty(JCR_MIMETYPE, JCR_NAMESPACE);
        
        int status = davPropFind.execute();
        
        String serverPrefix = config.getContext().getServerPrefix();
        String currentHref = serverPrefix + currentPath;
        
        if (status != Const.HttpStatus.MULTISTATUS) {
          showMessageBox("Can't open remote directory. ErrorCode: " + status);
          return;
        }
        
        Multistatus multistatus = davPropFind.getMultistatus();

        responses.clear();
        
        ArrayList<ResponseDoc> tmpResponses = multistatus.getResponses();
        for (int i = 0; i < tmpResponses.size(); i++) {
          ResponseDoc response = tmpResponses.get(i);          
          String responseHref = TextUtils.UnEscape(response.getHref(), '%');
          
          if (i == 0 && responseHref.equals(currentHref)) {
            continue;
          }              
          
          responses.add(response);
        }

        Collections.sort(responses, new Comparer());
        fillItemsList();
        
        XComboBox xComboBox = (XComboBox)UnoRuntime.queryInterface(
            XComboBox.class, xControlContainer.getControl(COMBO_PATH));
        xComboBox.addItem(currentHref, (short) 0);

        XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
            XTextComponent.class, xControlContainer.getControl(COMBO_PATH));
        xComboText.setText(currentHref);
        
        enableAll();
      } catch (Throwable exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
    }
    
  }
  
  class Comparer implements Comparator<ResponseDoc> {
    
    public int compare(ResponseDoc resp1, ResponseDoc resp2) {
      
      ResourceTypeProp rt1 = (ResourceTypeProp)resp1.getProperty(Const.DavProp.RESOURCETYPE);
      ResourceTypeProp rt2 = (ResourceTypeProp)resp2.getProperty(Const.DavProp.RESOURCETYPE);
      
      if (rt1.isCollection() && !rt2.isCollection()) {
        return 0;
      }
      
      if (!rt1.isCollection() && rt2.isCollection()) {
        return 1;
      }
      
      DisplayNameProp dn1 = (DisplayNameProp)resp1.getProperty(Const.DavProp.DISPLAYNAME);
      DisplayNameProp dn2 = (DisplayNameProp)resp2.getProperty(Const.DavProp.DISPLAYNAME);
      
      return dn1.getDisplayName().compareToIgnoreCase(dn2.getDisplayName());
    }
  }
  
  protected int getSelectedItemPos() {
    XListBox xListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xControlContainer.getControl(LST_ITEMS));      
    return xListBox.getSelectedItemPos();    
  }
  
  protected void doSelectItem() {
    int selectedPos = getSelectedItemPos();
    
    if (selectedPos < 0) {
      return;
    }
    
    ResponseDoc response = responses.get(selectedPos);
    
    if (isCollection(response)) {
      doPropFindResponse(response);
      return;
    }
    
    try {
      doOpenRemoteFile(TextUtils.UnEscape(response.getHref(), '%'));
      Thread.sleep(100);
      xDialog.endExecute();
    } catch (Exception exc) {
      showMessageBox("Can't open remote file!");
      
      Log.info("Can't open remote file... " + exc.getMessage(), exc);
    }
    
  }
  
  protected void doPropFindResponse(ResponseDoc response) {
    String href = TextUtils.UnEscape(response.getHref(), '%');
    String serverPrefix = config.getContext().getServerPrefix();

    if (!href.startsWith(serverPrefix)) {
      return;
    }
    
    currentPath = href.substring(serverPrefix.length());
    doPropFind();    
  }
    
  private class ListItemsClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      doSelectItem();
    }
  }
  
  private class PrevClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {
      XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
          XTextComponent.class, xControlContainer.getControl(COMBO_PATH));
      
      String currentHref = xComboText.getText();
      String serverPrefix = config.getContext().getServerPrefix();
      
      if (!currentHref.startsWith(serverPrefix)) {
        return;
      }
      
      String curPath = currentHref.substring(serverPrefix.length());
      
      String cuttedPath = curPath.substring(0, curPath.lastIndexOf("/"));
      if (!cuttedPath.startsWith("/")) {
        cuttedPath = "/" + cuttedPath;
      }
      
      currentPath = cuttedPath;
      doPropFind();
    }
  }
  
}
