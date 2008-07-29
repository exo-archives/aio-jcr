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

/*
 * 
 * DAV:parentname
 * DAV:isroot
 * DAV:isversioned
 * DAV:getetag
 * DAV:haschildren
 * DAV:childcount
 * DAV:isfolder
 * 
 */

public class AdditionalPropertiesTest extends TestCase {

  public void testAdditionalProperties() throws Exception {
    Log.info("testAdditionalProperties...");
        
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath("/production");
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.PARENTNAME);
      davPropFind.setRequiredProperty(Const.DavProp.ISROOT);
      davPropFind.setRequiredProperty(Const.DavProp.ISVERSIONED);
      
      //davPropFind.setRequiredProperty(Const.DavProp.GETETAG);
      
      davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
      davPropFind.setRequiredProperty(Const.DavProp.HASCHILDREN);
      davPropFind.setRequiredProperty(Const.DavProp.CHILDCOUNT);
      davPropFind.setRequiredProperty(Const.DavProp.ISFOLDER);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());      
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc response = responses.get(i);
        
        PropApi parentNameProperty = response.getProperty(Const.DavProp.PARENTNAME); 
        assertNotNull(parentNameProperty);
        
        if (i == 0) {
          assertEquals(Const.HttpStatus.NOTFOUND, parentNameProperty.getStatus());
        } else {
          assertEquals(Const.HttpStatus.OK, parentNameProperty.getStatus());
        }        
        
        PropApi isRootProperty = response.getProperty(Const.DavProp.ISROOT);
        assertNotNull(isRootProperty);
        assertEquals(Const.HttpStatus.OK, isRootProperty.getStatus());
        
        PropApi isVersionedProperty = response.getProperty(Const.DavProp.ISVERSIONED);
        assertNotNull(isVersionedProperty);
        assertEquals(Const.HttpStatus.OK, isVersionedProperty.getStatus());
        
        PropApi resourceTypePrpoperty = response.getProperty(Const.DavProp.RESOURCETYPE);
        assertNotNull(resourceTypePrpoperty);
        assertEquals(Const.HttpStatus.OK, resourceTypePrpoperty.getStatus());
        
//        PropApi getETagProperty = response.getProperty(Const.DavProp.GETETAG);
//        assertNotNull(getETagProperty);
//        
//        boolean isCollection = ((ResourceTypeProp)resourceTypePrpoperty).isCollection();
//        if (!isCollection) {          
//          assertEquals(Const.HttpStatus.OK, getETagProperty.getStatus());
//        } else {
//          assertEquals(Const.HttpStatus.NOTFOUND, getETagProperty.getStatus());
//        }       
        
        PropApi hasChildrenProp = response.getProperty(Const.DavProp.HASCHILDREN);
        assertNotNull(hasChildrenProp);
        assertEquals(Const.HttpStatus.OK, hasChildrenProp.getStatus());
        
        PropApi childCountProp = response.getProperty(Const.DavProp.CHILDCOUNT);
        assertNotNull(childCountProp);
        assertEquals(Const.HttpStatus.OK, childCountProp.getStatus());
        
        PropApi isFolderProp = response.getProperty(Const.DavProp.ISFOLDER);
        assertNotNull(isFolderProp);
        assertEquals(Const.HttpStatus.OK, isFolderProp.getStatus());        
      }

    }

    Log.info("done.");
  }
  
}
