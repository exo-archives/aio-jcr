/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import org.exoplatform.frameworks.webdavclient.FileLogger;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInfo;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class OOUtils {
    
  public static XComponent loadFromFile(XComponentContext xComponentContext, String url, String remoteUrl) throws Exception {    
    PropertyValue[] loadProps = null;

    loadProps = new PropertyValue[1];
    PropertyValue asTemplate = new PropertyValue();
    loadProps[0] = asTemplate;      

    // Create a blank writer document
    XMultiComponentFactory xMultiComponentFactory = xComponentContext.getServiceManager();
    Object oDesktop;

    oDesktop = xMultiComponentFactory.createInstanceWithContext(
        "com.sun.star.frame.Desktop", xComponentContext);
    XComponentLoader oComponentLoader =
      (com.sun.star.frame.XComponentLoader)
      com.sun.star.uno.UnoRuntime.queryInterface(
          com.sun.star.frame.XComponentLoader.class,
          oDesktop);
    
    String path = com.sun.star.uri.ExternalUriReferenceTranslator.create(xComponentContext).translateToInternal("file:///" + url.replace("\\", "/"));
    
    if (path.length() == 0 && url.length() != 0) {
      throw new RuntimeException();
    }
    
    XComponent xComponent =
      oComponentLoader.loadComponentFromURL(
          path,
          "_default",
          0,
          loadProps);

    try {
      XDocumentInfoSupplier xDocumentInfoSupplier =
        (XDocumentInfoSupplier)UnoRuntime.queryInterface(
            XDocumentInfoSupplier.class, xComponent);
      XDocumentInfo xDocumentInfo = xDocumentInfoSupplier.getDocumentInfo();
      
      xDocumentInfo.setUserFieldName((short) 0, "eXoRemoteFileName");
      xDocumentInfo.setUserFieldValue((short) 0, remoteUrl);    
      
//      XStorable xStorable = (XStorable)UnoRuntime.queryInterface(
//                XStorable.class, xComponent);
//      xStorable.store();
      
    } catch (Exception exc) {
      FileLogger.info("Can't store info to opened file...");
    }    
    
    return (xComponent);
  }   

}
