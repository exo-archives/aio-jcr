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

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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

