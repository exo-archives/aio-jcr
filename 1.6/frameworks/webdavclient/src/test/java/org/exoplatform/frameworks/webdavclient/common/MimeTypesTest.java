/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;


import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MimeTypesTest extends TestCase {
  
  public void testWordMimeTypes() {
    Log.info("MimeTypesTest:testWordMimeTypes");
    
    try {
      // retrieve mimetype of file
      {
        String srcPath = "/production";
        
        DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
        davPropFind.setResourcePath(srcPath);
        
        davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
        davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
        davPropFind.setRequiredProperty(Const.DavProp.CREATIONDATE);
        davPropFind.setRequiredProperty(Const.DavProp.ISFOLDER);
        davPropFind.setRequiredProperty("jcr:mimeType");
        
        int status = davPropFind.execute();
        Log.info("STATUS: " + status);
        Log.info("REPLY: \r\n" + new String(davPropFind.getResponseDataBuffer()));
        //TestUtils.logXML(davPropFind);
        
        Multistatus multistatus = davPropFind.getMultistatus();
        ArrayList<ResponseDoc> responses = multistatus.getResponses();
        for (int i = 0; i < responses.size(); i++) {
          ResponseDoc curResponse = responses.get(i);
          
          Log.info("RESPONSE: " + curResponse);
          Log.info("HREF: " + curResponse.getHref());
          
          PropApi property = curResponse.getProperty("jcr:mimeType");
          Log.info("PROPERTY: " + property);
          if (property != null) {
            Log.info("NAME: " + property.getName());
            Log.info("VALUE: " + property.getValue());
            
          }
          
          
//          ArrayList<PropApi> properties = curResponse.getProperties();
//          for (int j = 0; j < properties.size(); j++) {
//            PropApi curProperty = properties.get(j);
//            
//            Log.info("PROPAPI: " + curProperty);
//            
//            Log.info("NAME: " + curProperty.getName());
//            Log.info("VALUE: " + curProperty.getValue());
//            Log.info("STATUS: " + curProperty.getStatus());
//            
//          }
        }
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception. ", exc);
    }    
    
    Log.info("MimeTypesTest:done");
  }

//  public void testWebDavGetXML() {
//    Log.info("Test...");
//    
//    try {
//      
//      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
//      davGet.setResourcePath("/production/1");
//      
//      int status = davGet.execute();
//      Log.info("STATUS: " + status);
//      
//      Log.info("REPLY: \r\n" + new String(davGet.getResponseDataBuffer()));
//      TestUtils.logXML(davGet);
//    } catch (Exception exc) {
//      Log.info("Unhandled exception. ", exc);
//    }
//    
//    Log.info("done.");
//  }
  
}

