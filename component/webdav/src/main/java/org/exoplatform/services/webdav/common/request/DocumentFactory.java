/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.request;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.acl.request.AclPrincipalPropSetDocument;
import org.exoplatform.services.webdav.acl.request.AclPrincipalPropsDocument;
import org.exoplatform.services.webdav.common.request.documents.PropFindDocument;
import org.exoplatform.services.webdav.common.request.documents.PropertyBehaviorDocument;
import org.exoplatform.services.webdav.common.request.documents.PropertyUpdateDocument;
import org.exoplatform.services.webdav.deltav.request.ExpandPropertyDocument;
import org.exoplatform.services.webdav.deltav.request.VersionTreeDocument;
import org.exoplatform.services.webdav.lock.request.LockInfoDocument;
import org.exoplatform.services.webdav.order.request.OrderPatchDocument;
import org.exoplatform.services.webdav.search.request.SearchRequestDocument;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DocumentFactory {
  
  public static final String [][]AVAILABLE_DOCUMENTS = {
    {DavConst.DavDocument.ACLPRINCIPALPROPS, AclPrincipalPropsDocument.class.getCanonicalName()},
    {DavConst.DavDocument.ACLPRINCIPALPROPSET, AclPrincipalPropSetDocument.class.getCanonicalName()},
    {DavConst.DavDocument.EXPANDPROPERTY, ExpandPropertyDocument.class.getCanonicalName()},
    {DavConst.DavDocument.LOCKINFO, LockInfoDocument.class.getCanonicalName()},
    {DavConst.DavDocument.PROPERTYBEHAVIOR, PropertyBehaviorDocument.class.getCanonicalName()},
    {DavConst.DavDocument.PROPERTYUPDATE, PropertyUpdateDocument.class.getCanonicalName()},
    {DavConst.DavDocument.PROPFIND, PropFindDocument.class.getCanonicalName()},
    {DavConst.DavDocument.VERSIONTREE, VersionTreeDocument.class.getCanonicalName()},
    {DavConst.DavDocument.ORDERPATCH, OrderPatchDocument.class.getCanonicalName()},
    {DavConst.DavDocument.SEARCHREQUEST, SearchRequestDocument.class.getCanonicalName()}
  };    

}
