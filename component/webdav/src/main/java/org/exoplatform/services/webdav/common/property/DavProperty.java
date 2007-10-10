/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavProperty.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class DavProperty {

  public class Search {
    public static final String BASICSEARCH = "basicsearch";
    public static final String SELECT = "select";
    public static final String FROM = "from";
    public static final String WHERE = "where";    

    public static final String SUPPORTEDQUERYGRAMMARSET = "supported-query-grammar-set";
    public static final String SUPPORTEDQUERYGRAMMAR = "supported-query-grammar";
    public static final String GRAMMAR = "grammar";  
  }
  
  public static final String SUPPORTEDMETHODSET = "supported-method-set";
  public static final String SUPPORTEDMETHOD = "supported-method";
  public static final String NAME = "name";
  
  // LOCK

//  public static final String LOCKENTRY = "lockentry";
//  public static final String SHARED = "shared";
//  public static final String LOCKDISCOVERY = "lockdiscovery";
//  public static final String SUPPORTEDLOCK = "supportedlock";  

  
  //PROPERTYUPDATE  
  
//  public static final String REMOVE = "remove";            
//  public static final String SET = "set";      
  
  //DELTAV
  public static final String CHECKEDIN = "checked-in";
  public static final String CHECKEDOUT = "checked-out";
  public static final String PREDECESSORSET = "predecessor-set";
  public static final String SUCCESSORSET = "successor-set";
  
  
  public static final String GETCONTENTLENGTH = "getcontentlength";
  public static final String GETCONTENTTYPE = "getcontenttype";
  public static final String GETLASTMODIFIED = "getlastmodified";
  public static final String DISPLAYNAME = "displayname";
  public static final String CREATIONDATE = "creationdate";
  public static final String CREATORDISPLAYNAME = "creator-displayname";
  public static final String ISCOLLECTION = "iscollection";
  public static final String RESOURCETYPE = "resourcetype";
  public static final String VERSIONHISTORY = "version-history";
  public static final String VERSIONNAME = "version-name";  
  public static final String STATUS = "status";      
  public static final String TIMEOUT = "timeout";      
  public static final String WRITE = "write";            
  public static final String ORDERMEMBER = "order-member";
  public static final String SEGMENT = "segment";
  public static final String POSITION = "position";      
  public static final String FIRST = "first";      
  public static final String LAST = "last";      
  public static final String BEFORE = "before";      
  public static final String AFTER = "after";      
  public static final String PROPSTAT = "propstat";
  public static final String PROP = "prop";
  public static final String RESPONSEDESCRIPTION = "responsedescription";      
  public static final String ACTIVELOCK = "activelock";      
  public static final String COLLECTION = "collection";      
  public static final String ALLPROP = "allprop";      
  public static final String EXCLUSIVE = "exclusive";      
  public static final String HREF = "href";          
  public static final String INCLUDE = "include";      
  public static final String LOCKINFO = "lockinfo";      
  public static final String LOCKSCOPE = "lockscope";
  public static final String LOCKTOKEN = "locktoken";
  public static final String LOCKTYPE = "locktype";
  public static final String OWNER = "owner";
  public static final String RESPONSE = "response";
  public static final String PARENTNAME = "parentname";
  public static final String ISROOT = "isroot";
  public static final String ISFOLDER = "isfolder";
  public static final String ISVERSIONED = "isversioned";
  public static final String HASCHILDREN = "haschildren";
  public static final String CHILDCOUNT = "childcount";
  
  public static final String SELECT = "select";
  
  public static final String SUPPORTED_PRIVILEGE_SET = "supported-privilege-set";
  public static final String CURRENT_USER_PRIVILEGE_SET = "current-user-privilege-set";
  public static final String ACL = "acl";
  public static final String ACL_RESTRICTIONS = "acl-restrictions";
  public static final String PRINCIPAL_COLLECTION_SET = "principal-collection-set";   
}
