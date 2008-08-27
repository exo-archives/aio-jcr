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

package org.exoplatform.applications.ooplugin.props;

import org.exoplatform.applications.ooplugin.dav.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropManager {
  
  protected static String [][]availableProperties = {
    { Const.DavProp.DISPLAYNAME, "org.exoplatform.frameworks.webdavclient.properties.DisplayNameProp" },
    { Const.DavProp.LOCKDISCOVERY, "org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp" },
    { Const.DavProp.RESOURCETYPE, "org.exoplatform.frameworks.webdavclient.properties.ResourceTypeProp" },
    { Const.DavProp.GETCONTENTTYPE, "org.exoplatform.frameworks.webdavclient.properties.ContentTypeProp" },
    { Const.DavProp.GETCONTENTLENGTH, "org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp" },
    { Const.DavProp.GETLASTMODIFIED, "org.exoplatform.frameworks.webdavclient.properties.LastModifiedProp" },
    { Const.DavProp.CHECKEDIN, "org.exoplatform.frameworks.webdavclient.properties.CheckedInProp" },
    { Const.DavProp.CHECKEDOUT, "org.exoplatform.frameworks.webdavclient.properties.CheckedOutProp" },
    { Const.DavProp.VERSIONNAME, "org.exoplatform.frameworks.webdavclient.properties.VersionNameProp" },
    { Const.DavProp.CREATORDISPLAYNAME, "org.exoplatform.frameworks.webdavclient.properties.CreatorDisplayNameProp" },
    { Const.DavProp.SUPPORTEDLOCK, "org.exoplatform.frameworks.webdavclient.properties.SupportedLockProp" },
    { Const.DavProp.SUPPORTEDQUERYGRAMMARSET, "org.exoplatform.frameworks.webdavclient.properties.SupportedQueryGrammarSetProp" },
    { Const.DavProp.SUPPORTEDMETHODSET, "org.exoplatform.frameworks.webdavclient.properties.SupportedMethodSetProp" },
    { Const.DavProp.CURRENT_USER_PRIVILEGE_SET, "org.exoplatform.frameworks.webdavclient.properties.CurrentUserPrivilegeSet" },
    { Const.DavProp.CREATIONDATE, "org.exoplatform.frameworks.webdavclient.properties.CreationDateProp" }    
  };
  
  
  public static PropApi getPropertyByNode(Node propertyNode, String httpStatus) {
    try {
      String nodeName = propertyNode.getLocalName();
      if (!propertyNode.getNamespaceURI().equals(Const.Dav.NAMESPACE)) {
        nodeName = propertyNode.getNodeName();
      }
      
      PropApi curProp = null;

      for (int i = 0; i < availableProperties.length; i++) {
        if (nodeName.equals(availableProperties[i][0])) {
          curProp = (PropApi)Class.forName(availableProperties[i][1]).newInstance();
          break;
        }
      }
      
      if (curProp == null) {
        curProp = new CommonProp(nodeName);
      }
      curProp.setStatus(httpStatus);
      curProp.init(propertyNode);      
      return curProp;
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    return null;
  }
  
}
