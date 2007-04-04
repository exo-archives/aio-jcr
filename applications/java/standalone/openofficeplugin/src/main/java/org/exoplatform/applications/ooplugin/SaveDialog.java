/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;

import org.exoplatform.applications.ooplugin.dialog.Component;
import org.exoplatform.applications.ooplugin.events.ActionListener;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInfo;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SaveDialog extends BrowseDialog {
  
  public static final String DIALOG_NAME = "_SaveDialog";
  
  private Thread launchThread;
  
  private XStorable xStorable;
  private XDocumentInfo xDocumentInfo;
  private XModel xModel;
  private String localFilePath = "";
  
  private static final String EDT_NAME = "edtName"; 
  private static final String BTN_SAVE = "btnSave";
  
  private boolean isSaveAs = false;
  
  public SaveDialog(WebDavConfig config, XComponentContext xComponentContext, XFrame xFrame, XToolkit xToolkit, boolean isSaveAs) {
    super(config, xComponentContext, xFrame, xToolkit);
    dialogName = DIALOG_NAME;
    
    this.isSaveAs = isSaveAs;
    
    addHandler(BTN_SAVE, Component.XTYPE_XBUTTON, new SaveClick());
    
    launchThread = new LaunchThread();
    launchThread.start();
  }
  
  @Override
  protected void disableAll() {
    super.disableAll();
  }
  
  @Override
  protected void enableAll() {
    super.enableAll();
  }  
  
  private class SaveClick extends ActionListener {
    
    public void actionPerformed(ActionEvent arg0) {      
      try {
        doSaveFile();
      } catch (Exception exc) {
        Log.info("Unhandled exception", exc);
      }      
    }

  }
  
  protected String getFileName() {
    XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
        XTextComponent.class, xControlContainer.getControl(EDT_NAME));
    return xComboText.getText();
  }
  
  protected void setFileName(String fileName) {
    XTextComponent xComboText = (XTextComponent)UnoRuntime.queryInterface(
        XTextComponent.class, xControlContainer.getControl(EDT_NAME));
    xComboText.setText(fileName);    
  }
    
  protected void doSaveFile() throws Exception {
    String fileName = getFileName();
    
    if ("".equals(fileName)) {
      showMessageBox("File name required!");
      return;
    }
    
    String repoFilePath = currentPath;
    if (repoFilePath.endsWith("/")) {
      repoFilePath += fileName; 
    } else {
      repoFilePath += "/" + fileName;
    }
    
    if ("".equals(localFilePath)) {              
      prepareTmpPath(currentPath);
      
      String storeToPath = LocalFileSystem.getDocumentsPath() + File.separatorChar + "repository" + currentPath;
      if ("/".equals(currentPath)) {
        storeToPath += fileName;
      } else {
        storeToPath += "/" + fileName;
      }
      storeToPath = storeToPath.replace("\\", "/");
      
      storeLocal(storeToPath);
      if (doSave(storeToPath, repoFilePath)) {
        setSessionPath(repoFilePath);
        xDialog.endExecute();        
      }
      
      return;
    }
    
    xStorable.store();
    if (doSave(localFilePath, repoFilePath)) {
      setSessionPath(repoFilePath);
      xDialog.endExecute();
    }
    
  }
  
  protected void setSessionPath(String path) throws IndexOutOfBoundsException {
    xDocumentInfo.setUserFieldName((short) 0, "eXoRemoteFileName");
    xDocumentInfo.setUserFieldValue((short) 0, path);    
  }
  
  protected String getSessionPath() throws IndexOutOfBoundsException {
    if (xDocumentInfo.getUserFieldName((short) 0).compareTo("eXoRemoteFileName") == 0 &&
        xDocumentInfo.getUserFieldValue((short) 0).length() > 0) {
      
      return xDocumentInfo.getUserFieldValue((short) 0);
    }
    return null; 
  }
  
  public void storeLocal(String url)
  {
    try {
      String path = com.sun.star.uri.ExternalUriReferenceTranslator.
      create(xComponentContext).translateToInternal("file:///" + url.replace("\\", "/"));
      if (path.length() == 0) {
        throw new RuntimeException();
      }
      
      PropertyValue[] loadProps = new PropertyValue[1];
      PropertyValue asTemplate = new PropertyValue();
      loadProps[0] = asTemplate;
      
      xStorable.storeAsURL(path, loadProps);
    } catch (com.sun.star.io.IOException e) {
      Log.info("Exception.", e);
    }
  }  
  
  @Override
  public boolean launchBeforeOpen() {    
    try {      
      initDefaults();
   
      if ("".equals(localFilePath)) {
        return true;
      }
      
      if (isSaveAs) {         
        if (getSessionPath() != null) {
          String sessPath = getSessionPath();
          String onlyPath = sessPath.substring(0, sessPath.lastIndexOf("/"));
          currentPath = onlyPath;
          String onlyName = sessPath.substring(sessPath.lastIndexOf("/") + 1);
          setFileName(onlyName);
        } else {
          String onlyName = localFilePath.substring(localFilePath.lastIndexOf("/") + 1);
          setFileName(onlyName);
        }
        return true;
      } else {
    	  String onlyName = localFilePath.substring(localFilePath.lastIndexOf("/") + 1);
    	  setFileName(onlyName);
      }
      
      String repositoryFolderName = LocalFileSystem.getDocumentsPath() + File.separatorChar + "repository";
      repositoryFolderName = repositoryFolderName.replace("\\", "/");

      if (getSessionPath() != null) {
        String remotePath = getSessionPath();
        xStorable.store();
        doSave(localFilePath, remotePath);
        return false;
      }      
      
      if (localFilePath.startsWith(repositoryFolderName)) {
        xStorable.store();
        String remotePath = localFilePath.substring(repositoryFolderName.length());
        doSave(localFilePath, remotePath);
        return false;
      }

      return true;
    } catch (Exception exc) {
      Log.info("Unhandled ecxeption. " + exc.getMessage(), exc);
    }
    
    return false;
  }
  
  
  protected void initDefaults() throws Exception {
    XMultiComponentFactory xMultiComponentFactory = xComponentContext.getServiceManager();

    Object descTop = xMultiComponentFactory.createInstanceWithContext(
        "com.sun.star.frame.Desktop", xComponentContext);

    XDesktop xDescTop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, descTop);
    XComponent xComponent = xDescTop.getCurrentComponent();
    
    xStorable = (XStorable)UnoRuntime.queryInterface(XStorable.class, xComponent);
    
    XDocumentInfoSupplier xDocumentInfoSupplier =
      (XDocumentInfoSupplier)UnoRuntime.queryInterface(
          XDocumentInfoSupplier.class, xComponent);
    
    xDocumentInfo = xDocumentInfoSupplier.getDocumentInfo();    
    xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xComponent);
    
    localFilePath = URLDecoder.decode(xModel.getURL().replaceFirst("file:///", ""));
  }
  
  
  private class LaunchThread extends Thread {
    public void run() {
      try {
        while (!enabled) {
          Thread.sleep(100);
        }
        Thread.sleep(100);
        
        doPropFind();
                
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
    }
  }
  
  protected boolean doSave(String localPath, String remotePath) {
    try {      
      File inFile = new File(localPath);
      FileInputStream inStream = new FileInputStream(inFile);
      
      DavPut davPut = new DavPut(config.getContext());
      davPut.setResourcePath(remotePath);
      davPut.setRequestInputStream(inStream, inStream.available());
      
      int status = davPut.execute();
      if (status == Const.HttpStatus.CREATED) {
        showMessageBox("File saved!");
        return true;
      }
      
      showMessageBox("Can't store file. Error code: " + status);
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    return false;
  }  
  
}
