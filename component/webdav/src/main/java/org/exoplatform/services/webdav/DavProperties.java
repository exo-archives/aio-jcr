/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import org.exoplatform.services.webdav.acl.property.CurrentUserPrivilegeSetProp;
import org.exoplatform.services.webdav.acl.property.SupportedPrivilegeSetProp;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.ChildCountProp;
import org.exoplatform.services.webdav.common.property.dav.ContentLengthProp;
import org.exoplatform.services.webdav.common.property.dav.CreationDateProp;
import org.exoplatform.services.webdav.common.property.dav.DisplayNameProp;
import org.exoplatform.services.webdav.common.property.dav.HasChildrenProp;
import org.exoplatform.services.webdav.common.property.dav.IsCollectionProp;
import org.exoplatform.services.webdav.common.property.dav.IsFolderProp;
import org.exoplatform.services.webdav.common.property.dav.IsRootProp;
import org.exoplatform.services.webdav.common.property.dav.LastModifiedProp;
import org.exoplatform.services.webdav.common.property.dav.ParentNameProp;
import org.exoplatform.services.webdav.common.property.dav.ResourceTypeProp;
import org.exoplatform.services.webdav.common.property.dav.SupportedMethodSetProp;
import org.exoplatform.services.webdav.deltav.property.CheckedInProp;
import org.exoplatform.services.webdav.deltav.property.CheckedOutProp;
import org.exoplatform.services.webdav.deltav.property.IsVersioned;
import org.exoplatform.services.webdav.deltav.property.PredecessorSet;
import org.exoplatform.services.webdav.deltav.property.SuccessorSetProp;
import org.exoplatform.services.webdav.deltav.property.VersionHistoryProp;
import org.exoplatform.services.webdav.deltav.property.VersionNameProp;
import org.exoplatform.services.webdav.lock.property.LockDiscoveryProp;
import org.exoplatform.services.webdav.lock.property.SupportedLockProp;
import org.exoplatform.services.webdav.search.property.SupportedQueryGrammarSetProp;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavProperties {

  public static final String [][]PROPERTIES = {
    {DavProperty.CHECKEDIN,
          CheckedInProp.class.getCanonicalName()},    
    {DavProperty.CHECKEDOUT,
          CheckedOutProp.class.getCanonicalName()},    
    {DavProperty.CREATIONDATE,
          CreationDateProp.class.getCanonicalName()},    
//    {DavProperty.CREATORDISPLAYNAME,
//          CreatorDisplayNameProp.class.getCanonicalName()},    
    {DavProperty.DISPLAYNAME,
          DisplayNameProp.class.getCanonicalName()},    
    {DavProperty.GETCONTENTLENGTH, 
          ContentLengthProp.class.getCanonicalName()},    
//    {DavProperty.GETCONTENTTYPE,
//          ContentTypeProp.class.getCanonicalName()},    
    {DavProperty.GETLASTMODIFIED, 
          LastModifiedProp.class.getCanonicalName()},
    {DavProperty.ISCOLLECTION,
          IsCollectionProp.class.getCanonicalName()},
    {DavProperty.LOCKDISCOVERY,
          LockDiscoveryProp.class.getCanonicalName()},
    {DavProperty.PREDECESSORSET,
          PredecessorSet.class.getCanonicalName()},
    {DavProperty.RESOURCETYPE,
          ResourceTypeProp.class.getCanonicalName()},
    {DavProperty.SUCCESSORSET,
          SuccessorSetProp.class.getCanonicalName()},
    {DavProperty.SUPPORTEDLOCK,
          SupportedLockProp.class.getCanonicalName()},
    {DavProperty.VERSIONHISTORY,
          VersionHistoryProp.class.getCanonicalName()},
    {DavProperty.VERSIONNAME,
          VersionNameProp.class.getCanonicalName()},
    {DavProperty.Search.SUPPORTEDQUERYGRAMMARSET,
          SupportedQueryGrammarSetProp.class.getCanonicalName()},
    {DavProperty.SUPPORTEDMETHODSET,
          SupportedMethodSetProp.class.getCanonicalName()},
    {DavProperty.PARENTNAME,
          ParentNameProp.class.getCanonicalName()},
    {DavProperty.ISROOT,
          IsRootProp.class.getCanonicalName()},
    {DavProperty.ISVERSIONED,
          IsVersioned.class.getCanonicalName()},
    {DavProperty.HASCHILDREN,
          HasChildrenProp.class.getCanonicalName()},
    {DavProperty.CHILDCOUNT,
          ChildCountProp.class.getCanonicalName()},
    {DavProperty.ISFOLDER,
          IsFolderProp.class.getCanonicalName()},
    {DavProperty.SUPPORTED_PRIVILEGE_SET,
          SupportedPrivilegeSetProp.class.getCanonicalName()},
    {DavProperty.CURRENT_USER_PRIVILEGE_SET,
          CurrentUserPrivilegeSetProp.class.getCanonicalName()}
  };  
  
}
