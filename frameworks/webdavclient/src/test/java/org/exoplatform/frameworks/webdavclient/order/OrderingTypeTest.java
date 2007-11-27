/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.order;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
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

public class OrderingTypeTest extends TestCase {  

  public void testOrderingTypeNotFound() throws Exception {
    Log.info("OrderingTypeTest:testOrderingType");
    
    String srcName = "/production/order test folder " + System.currentTimeMillis();

    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(srcName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());      
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcName);
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
      
      davPropFind.setRequiredProperty(Const.DavProp.ORDERING_TYPE);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      ResponseDoc response = responses.get(0);
      
      PropApi property = response.getProperty(Const.DavProp.ORDERING_TYPE);

      assertNotNull(property);      
      assertEquals(Const.HttpStatus.NOTFOUND, property.getStatus());      
    }

    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(srcName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("Done.");
  }
  
  public void testOrderingTypeSuccess() throws Exception {
    Log.info("OrderingTypeTest:testOrderingTypeSuccess");
    
    String srcName = "/production/order test folder " + System.currentTimeMillis();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(srcName);
      davMkCol.setNodeType("webdav:folder");
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(srcName);
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
      davPropFind.setRequiredProperty(Const.DavProp.ORDERING_TYPE);

      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      ResponseDoc response = responses.get(0);      
      PropApi property = response.getProperty(Const.DavProp.ORDERING_TYPE);      
      assertEquals(Const.HttpStatus.OK, property.getStatus());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(srcName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("Done.");
  }

}
