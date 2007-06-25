/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropManager {
  
  protected static String [][]availableProperties = {
    { Const.DavProp.DISPLAYNAME, DisplayNameProp.class.getCanonicalName() },
    { Const.DavProp.LOCKDISCOVERY, LockDiscoveryProp.class.getCanonicalName() },
    { Const.DavProp.RESOURCETYPE, ResourceTypeProp.class.getCanonicalName() },
    { Const.DavProp.GETCONTENTTYPE, ContentTypeProp.class.getCanonicalName() },
    { Const.DavProp.GETCONTENTLENGTH, ContentLengthProp.class.getCanonicalName() },
    { Const.DavProp.GETLASTMODIFIED, LastModifiedProp.class.getCanonicalName() },
    { Const.DavProp.CHECKEDIN, CheckedInProp.class.getCanonicalName() },
    { Const.DavProp.CHECKEDOUT, CheckedOutProp.class.getCanonicalName() },
    { Const.DavProp.VERSIONNAME, VersionNameProp.class.getCanonicalName() },
    { Const.DavProp.CREATORDISPLAYNAME, CreatorDisplayNameProp.class.getCanonicalName() },
    { Const.DavProp.SUPPORTEDLOCK, SupportedLockProp.class.getCanonicalName() },
    { Const.DavProp.SUPPORTEDQUERYGRAMMARSET, SupportedQueryGrammarSetProp.class.getCanonicalName() },
    { Const.DavProp.SUPPORTEDMETHODSET, SupportedMethodSetProp.class.getCanonicalName() },
    { Const.DavProp.CURRENT_USER_PRIVILEGE_SET, CurrentUserPrivilegeSet.class.getCanonicalName() },
    { Const.DavProp.CREATIONDATE, CreationDateProp.class.getCanonicalName() }
  };
  
  
  public static PropApi getPropertyByNode(Node propertyNode, String httpStatus) {
    try {
      String nodeName = propertyNode.getLocalName();
      if (!propertyNode.getNamespaceURI().equals(Const.Dav.NAMESPACE)) {
        nodeName = propertyNode.getNodeName();
      }
      
      Log.info("LOCAL NAME: " + nodeName);
      
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
      Log.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
    return null;
  }
  
}
