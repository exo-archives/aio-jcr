/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Const {
  
  public static final String DAV_PREFIX = "exo.davclient.";  

  public class Dav {
    public static final String NAMESPACE = "DAV:";
    public static final String NAMESPACE_XPATH = "XPATH:";
    public static final String NAMESPACE_SQL = "SQL";
    
    public static final String PREFIX = "A:";
    public static final String NAMESPACEATTR = "xmlns:A";
  }
  
  public class DavDepth {
    public static final int INFINITY = Integer.MAX_VALUE;
  }
  
  public class Http {
    public static final String CLIENTDESCR = "Exo-Http Client v.1.0.beta.";
    public static final String HEADER_DELIM = ": ";
    public static final String VERSION = "HTTP/1.1";    
  }
  
  public class DavProp {
    public static final String ACTIVELOCK = "activelock";
    public static final String COMMENT = "comment";
    public static final String CHECKEDIN = "checked-in";
    public static final String CHECKEDOUT = "checked-out";
    public static final String DEPTH = "depth";
    public static final String DISPLAYNAME = "displayname";
    public static final String EXCLUSIVE = "exclusive";
    public static final String GETCONTENTLENGTH = "getcontentlength";
    public static final String GETCONTENTTYPE = "getcontenttype";
    public static final String GETLASTMODIFIED = "getlastmodified";
    public static final String HREF = "href";
        
    public static final String MULTISTATUS = "multistatus";
    public static final String OWNER = "owner";
    public static final String PROP = "prop";
    public static final String PROPSTAT = "propstat";
    public static final String RESOURCETYPE = "resourcetype";
    public static final String RESPONSE = "response";
    public static final String STATUS = "status";
    public static final String TIMEOUT = "timeout";
    public static final String VERSIONNAME = "version-name";
    
    public static final String LOCKDISCOVERY = "lockdiscovery";
    public static final String LOCKENTRY = "lockentry";
    public static final String LOCKSCOPE = "lockscope";
    public static final String LOCKTOKEN = "locktoken";    
    public static final String LOCKTYPE = "locktype";
    
    public static final String WRITE = "write";
    public static final String ALLPROP = "allprop";
    public static final String COLLECTION = "collection";
    public static final String SHARED = "shared";
    public static final String REMOVE = "remove";
    public static final String SET = "set";    
    public static final String CREATORDISPLAYNAME = "creator-displayname";

    public static final String SUCCESSORSET = "successor-set";
    public static final String PREDECESSORSET = "predecessor-set";
    
    public static final String SUPPORTEDLOCK = "supportedlock";    
    public static final String ISCOLLECTION = "iscollection";
    
    
    public static final String CREATIONDATE = "creationdate";
    public static final String VERSIONHISTORY = "version-history";
    
    public static final String ORDERMEMBER = "order-member";
    public static final String SEGMENT = "segment";
    public static final String POSITION = "position";

    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    
    public static final String BASICSEARCH = "basicsearch";
    public static final String SELECT = "select";
    public static final String FROM = "from";
    public static final String WHERE = "where";
    public static final String SCOPE = "scope";
    
    public static final String SUPPORTEDQUERYGRAMMARSET = "supported-query-grammar-set";
    public static final String SUPPORTEDQUERYGRAMMAR = "supported-query-grammar";
    public static final String GRAMMAR = "grammar";
    
    public static final String SUPPORTEDMETHODSET = "supported-method-set";
    public static final String SUPPORTEDMETHOD = "supported-method";
    public static final String NAME = "name";
    public static final String PARENTNAME = "parentname";
    public static final String ISROOT = "isroot";
    public static final String ISVERSIONED = "isversioned";
    public static final String GETETAG = "getetag";
    public static final String HASCHILDREN = "haschildren";
    public static final String CHILDCOUNT = "childcount";
    public static final String ISFOLDER = "isfolder";
  }

  public class StreamDocs {
    public static final String OPTIONS = "options";
    public static final String PROPFIND = "propfind";
    public static final String MULTISTATUS = "multistatus";
    public static final String LOCKINFO = "lockinfo";
    public static final String PROP = "prop";
    public static final String PROPERTYUPDATE = "propertyupdate";
    public static final String VERSION_TREE = "version-tree";
    public static final String ORDERPATCH = "orderpatch";    
    public static final String SEARCHREQUEST = "searchrequest";
  }

  public class DavCommand {
    public static final String CHECKIN = "CHECKIN";
    public static final String CHECKOUT = "CHECKOUT";
    public static final String COPY = "COPY";
    public static final String DELETE = "DELETE";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String LABEL = "LABEL";
    public static final String LOCK = "LOCK";
    public static final String MERGE = "MERGE";
    public static final String MKCOL = "MKCOL";
    public static final String MKWORKSPACE = "MKWORKSPACE";
    public static final String MOVE = "MOVE";
    public static final String OPTIONS = "OPTIONS";
    public static final String ORDERPATCH = "ORDERPATCH";
    public static final String PROPFIND = "PROPFIND";
    public static final String PROPPATCH = "PROPPATCH";
    public static final String PUT = "PUT";
    public static final String REPORT = "REPORT";
    public static final String RESTORE = "RESTORE";
    public static final String UNCHECKOUT = "UNCHECKOUT";
    public static final String UNLOCK = "UNLOCK";
    public static final String UPDATE = "UPDATE";
    public static final String VERSIONCONTROL = "VERSION-CONTROL";
    public static final String SEARCH = "SEARCH";
  }
  
  public class Lock {
    public static final String SCOPE_SHARED = "shared";
    public static final String SCOPE_EXCLUSIVE = "exclusive";
    public static final String TYPE_WRITE = "write";      
  }
  
  public class HttpStatus {
    public static final String OK_DESCR = "HTTP/1.1 200 OK";
    
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NOCONTENT = 204;
    public static final int PARTIAL_CONTENT = 206;
    public static final int MULTISTATUS = 207;
    
    public static final int AUTHNEEDED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOTFOUND = 404;
    public static final int METHODNOTALLOWED = 405;         
    public static final int CONFLICT = 409;
    public static final int PRECONDITIONFAILED = 412; 
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    
    public static final int UNSUPPORTEDMEDIATYPE = 415;
    public static final int HTTPNOTRECOGNIZED = 505;
    public static final int INSUFFICIENTSTORAGE = 507;
  }
  
  public class HttpHeaders {
    public static final String ALLOW = "Allow";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONNECTION = "Connection";
    public static final String CONTENTLENGTH = "Content-Length";
    public static final String DESTINATION = "Destination";
    public static final String HOST = "Host";
    public static final String USERAGENT = "User-Agent";
    public static final String TRANSLATE = "Translate";
    public static final String ACCEPTENCODING = "Accept-Encoding";
    public static final String TE = "TE";
    public static final String DEPTH = "Depth";
    public static final String CONTENTTYPE = "Content-type";
    public static final String TIMEOUT = "Timeout";
    public static final String LOCKTOKEN = "Lock-Token";
    public static final String OVERWRITE = "Overwrite";
    public static final String RANGE = "Range";
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final String NODETYPE = "NodeType";
    public static final String MIXTYPE = "MixType";
  }

}
