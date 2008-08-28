/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.applications.ooplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.exoplatform.applications.ooplugin.Const.Dav;
import org.exoplatform.applications.ooplugin.Const.DavProp;
import org.exoplatform.applications.ooplugin.dav.DavPropFind;
import org.exoplatform.applications.ooplugin.dav.WebDavConfig;
import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;
import org.exoplatform.applications.ooplugin.utils.TextUtils;
import org.exoplatform.applications.ooplugin.utils.XmlUtil;
import org.exoplatform.common.http.HTTPStatus;

//___________________________________________________________________________
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.TimeOutException;
import org.exoplatform.frameworks.webdavclient.properties.CommonProp;
import org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp;
import org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp;
import org.exoplatform.frameworks.webdavclient.properties.LastModifiedProp;
import org.exoplatform.frameworks.webdavclient.properties.ResourceTypeProp;
import org.exoplatform.frameworks.webdavclient.properties.VersionNameProp;
//___________________________________________________________________________

import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
  
  private static final Log log = ExoLogger.getLogger("jcr.ooplugin.PlugInDialog");
  
  public static final int VNAME_LEN = 3;
  
  public static final int NAME_LEN = 30;
  
  public static final int SIZE_LEN = 36;
  
  public static final int MIMETYPE_SIZE = 63;
  
  public static final int LASTMODOFIED_SIZE = 92;
  
  public static final String BTN_PREV = "btnPrev";
  public static final String LST_ITEMS = "lstItems";
  public static final String COMBO_PATH = "comboPath";
  public static final String BTN_CANCEL = "btnCancel";
  
  public static final String LBL_TABLEHEAD = "lblTableHead";
  
  public static final String JCR_PREFIX = "jcr:";
  
  public static final String JCR_MIMETYPE = "jcr:mimeType";
  
  public static final String JCR_NAMESPACE = "http://www.jcp.org/jcr/1.0";
  
  protected String currentPath = "/";
  
  protected Thread openThread;  
  
  protected ArrayList<ResponseDoc> responses = new ArrayList<ResponseDoc>();
  
  protected ArrayList<Node> responsesNodes = new ArrayList<Node>();
  
  
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
      
      headerValue += "Mime-Type";
      while (headerValue.length() < MIMETYPE_SIZE) {
        headerValue += " ";
      }
      
      headerValue += "Last Modified";
      while (headerValue.length() < LASTMODOFIED_SIZE) {
        headerValue += " ";
      }
      
      headerValue += "Comments";
      
      xLabelHead.setText(headerValue);
      
    } catch (Exception exc) {
      log.info("Unhandled exception: " + exc.getMessage(), exc);
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
      (ResourceTypeProp)response.getProperty(DavProp.RESOURCETYPE);
    if (resourceTypeProperty == null) {
      return false;
    }
    return resourceTypeProperty.isCollection();
  }  
  
  protected String formatResponseLine(ResponseDoc response) {    
    String fileItem = "";
    
    VersionNameProp versionNameProperty = 
        (VersionNameProp)response.getProperty(DavProp.VERSIONNAME);
    if ((versionNameProperty != null) &&
        (versionNameProperty.getStatus() == HTTPStatus.OK)) {
      fileItem += "*";
    }
    
    while (fileItem.length() < VNAME_LEN) {
     fileItem += " ";
    }
    
    DisplayNameProp displayNameProperty = 
      (DisplayNameProp)response.getProperty(DavProp.DISPLAYNAME);
    ResourceTypeProp resourceTypeProperty =
      (ResourceTypeProp)response.getProperty(DavProp.RESOURCETYPE);
    
    String displayName = displayNameProperty.getDisplayName();
    
    
    if (resourceTypeProperty != null && resourceTypeProperty.isCollection()) {      
      if (displayName.length() > NAME_LEN - VNAME_LEN - 5) {
        displayName = displayName.substring(0, NAME_LEN - VNAME_LEN - 5);
      }
      
      fileItem += "[ ";
      fileItem += displayName;
      fileItem += " ]";
    } else {
      if (displayName.length() > NAME_LEN) {      
//        if (displayName.indexOf(".") > 0) {        
//          String name = displayName.substring(0, displayName.lastIndexOf("."));
//          Log.info("NAME: " + name);
//          String extension = displayName.substring(displayName.lastIndexOf("."));        
//          name = name.substring(0, NAME_LEN - extension.length() - 8);        
//          fileItem += (name + "... " + extension); 
//        } else {
//          
//        }
        fileItem += (displayName.substring(0, NAME_LEN - 7) + "...");
      } else {
        fileItem += displayName;
      }
    }
    
    while (fileItem.length() < NAME_LEN) {
      fileItem += " ";
    }
    
    ContentLengthProp contentLengthProperty =
      (ContentLengthProp)response.getProperty(DavProp.GETCONTENTLENGTH);
    if (contentLengthProperty != null) {
      
      long contentLength = contentLengthProperty.getContentLength();

      if (contentLength < 1024) {
        fileItem += contentLength;
      } else if (contentLength < (1024*1024)) {        
        contentLength = contentLength >> 10;
        fileItem += contentLength;
        fileItem += "K";
      } else {
        String kb = "" + (contentLength >> 10) % 1024;
        while (kb.length() < 3) {
          kb = "0" + kb;
        }        
        contentLength = contentLength >> 20;
        fileItem += contentLength;
        fileItem += ".";
        fileItem += kb.toCharArray()[0];
        fileItem += "M";        
      }
    }
    while (fileItem.length() < SIZE_LEN) {
      fileItem += " ";
    }
    
    CommonProp mimeTypeProperty = (CommonProp)response.getProperty(JCR_MIMETYPE);
    
    if (mimeTypeProperty != null) {
      fileItem += mimeTypeProperty.getValue();
    }
    
    if (fileItem.length() > MIMETYPE_SIZE - 1) {
      fileItem = fileItem.substring(0, MIMETYPE_SIZE - 4);
      fileItem += "...";
    }
    
    while (fileItem.length() < MIMETYPE_SIZE) {
      fileItem += " ";
    }    
    
    LastModifiedProp lastModifiedProperty =
      (LastModifiedProp)response.getProperty(DavProp.GETLASTMODIFIED);
    if (lastModifiedProperty != null) {
      fileItem += lastModifiedProperty.getLastModified();
    }
    
    while (fileItem.length() < LASTMODOFIED_SIZE) {
      fileItem += " ";
    }    
    
    CommonProp commentProperty =
      (CommonProp)response.getProperty(DavProp.COMMENT);
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
        davPropFind.setRequiredProperty(DavProp.DISPLAYNAME);
        davPropFind.setRequiredProperty(DavProp.RESOURCETYPE);        
        davPropFind.setRequiredProperty(DavProp.GETLASTMODIFIED);        
        davPropFind.setRequiredProperty(DavProp.GETCONTENTLENGTH);
        davPropFind.setRequiredProperty(DavProp.VERSIONNAME);
        davPropFind.setRequiredProperty(DavProp.COMMENT);        
        davPropFind.setRequiredProperty(JCR_MIMETYPE, JCR_NAMESPACE);        
        davPropFind.setDepth(1);        
        
        int status;
      
        try {
          status = davPropFind.execute();
        } catch (TimeOutException exc) {
          davPropFind = new DavPropFind(config.getContext());
          davPropFind.setResourcePath(currentPath);        
          davPropFind.setRequiredProperty(DavProp.DISPLAYNAME);
          davPropFind.setRequiredProperty(DavProp.RESOURCETYPE);        
          davPropFind.setRequiredProperty(DavProp.GETLASTMODIFIED);        
          davPropFind.setRequiredProperty(DavProp.GETCONTENTLENGTH);
          davPropFind.setRequiredProperty(DavProp.VERSIONNAME);
          davPropFind.setRequiredProperty(DavProp.COMMENT);        
          davPropFind.setRequiredProperty(JCR_MIMETYPE, JCR_NAMESPACE);        
          davPropFind.setDepth(1);        
          status = davPropFind.execute();
        }
        
        String serverPrefix = config.getServerPrefix();
        String currentHref = serverPrefix + currentPath;
        
        if (status != HTTPStatus.MULTISTATUS) {
          showMessageBox("Can't open remote directory. ErrorCode: " + status);
          return;
        }

//_        
        Document responseDoc = TextUtils.getXmlFromBytes(davPropFind.getResponseDataBuffer());
        Element el = responseDoc.getDocumentElement();
        NodeList nodeList = el.getChildNodes();
        int za = nodeList.getLength();
        
        

        for (int i = 0; i < nodeList.getLength(); i++) {
          responsesNodes.add(nodeList.item(i));
          
        }
          
//_        
        
        Multistatus multistatus = davPropFind.getMultistatus();                

        responses.clear();
        
        ArrayList<ResponseDoc> tmpResponses = multistatus.getResponses();
        for (int i = 0; i < tmpResponses.size(); i++) {
          ResponseDoc response = tmpResponses.get(i);          
          String responseHref = TextUtils.UnEscape(response.getHref(), '%');
          
          if (responseHref.equals(currentHref)) {
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
        log.info("Unhandled exception: " + exc.getMessage(), exc);
      }
    }
    
  }
  
  class NodeComparer implements Comparator<Node>{

    public int compare(Node o1, Node o2) {
      // TODO Auto-generated method stub
      return 0;
    }
    
  }
  
  class Comparer implements Comparator<ResponseDoc> {
    
    public int compare(ResponseDoc resp1, ResponseDoc resp2) {
      
      ResourceTypeProp rt1 = (ResourceTypeProp)resp1.getProperty(DavProp.RESOURCETYPE);
      ResourceTypeProp rt2 = (ResourceTypeProp)resp2.getProperty(DavProp.RESOURCETYPE);
      
      if (rt1.isCollection() && !rt2.isCollection()) {
        return 0;
      }
      
      if (!rt1.isCollection() && rt2.isCollection()) {
        return 1;
      }
      
      DisplayNameProp dn1 = (DisplayNameProp)resp1.getProperty(DavProp.DISPLAYNAME);
      DisplayNameProp dn2 = (DisplayNameProp)resp2.getProperty(DavProp.DISPLAYNAME);
      
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
      
      log.info("Can't open remote file... " + exc.getMessage(), exc);
    }
    
  }
  
  protected void doPropFindResponse(ResponseDoc response) {
    String href = TextUtils.UnEscape(response.getHref(), '%');
    String serverPrefix = config.getServerPrefix();

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
      String serverPrefix = config.getServerPrefix();
      
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
