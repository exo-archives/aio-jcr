/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: DavConst.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class DavConst {

    public static final String WDBDAV_COMMAND_CATALOG = "WEBDAV";
  
    public static final String HTTPVER = "HTTP/1.1";

    public static final String DAV_MSAUTHORVIA = "DAV";
    
    public static final String DAV_NAMESPACE = "DAV:";
    
    public static final String EXO_NAMESPACE = "http://exoplatform.com/jcr";
    
    public static final String DAV_PREFIX = "D:";
    public static final String DAV_SERVER = "eXo-Webdav Server /1.0";
    
    public static final String DAV_HEADER = "1, 2, ordered-collections";
    
    public static final String DASL_VALUE = "<DAV:basicsearch>" + 
               "<exo:sql xmlns:exo=\"http://exoplatform.com/jcr\"/>" +
               "<exo:xpath xmlns:exo=\"http://exoplatform.com/jcr\"/>";
    
    public static final String DAV_VERSIONIDENTIFIER = "VERSIONID";
    public static final String DAV_VERSIONPREFIX = "?" + DAV_VERSIONIDENTIFIER + "=";
    
    public static final String DAV_DEFAULT_MIME_TYPE = "text/plain";
    
    public class Headers {
      public static final String ALLOW = "Allow";
      public static final String DASL = "DASL";
      public static final String DAV = "DAV";
      
      public static final String ACCEPTENCODING = "Accept-Encoding";
      public static final String CONNECTION = "Connection";      
      public static final String CONTENTLENGTH = "Content-Length";
      public static final String CONTENTRANGE = "Content-Range";      
      public static final String CONTENTTYPE = "Content-type";      
      public static final String DEPTH = "Depth";
      public static final String DESTINATION = "Destination";
      public static final String HOST = "Host";
      public static final String LOCKTOKEN = "Lock-Token";
      public static final String LASTMODIFIED = "Last-Modified";
      public static final String MSAUTHORVIA = "MS-Author-Via";
      public static final String OVERWRITE = "Overwrite";
      public static final String TE = "TE";
      public static final String TIMEOUT = "Timeout";
      public static final String TRANSLATE = "Translate";
      public static final String USERAGENT = "User-Agent";
      public static final String XFEATURES = "X-Features";
      public static final String DATE = "Date";
      public static final String SERVER = "Server";
      public static final String WWWAUTHENTICATE = "WWW-Authenticate";
      public static final String AUTHORIZATION = "Authorization";
      public static final String CACHECONTROL = "Cache-Control";
      public static final String IF = "If";
      public static final String RANGE = "Range";
      public static final String ACCEPT_RANGES = "Accept-Ranges";      
      public static final String NODETYPE = "NodeType";
      public static final String MIXTYPE = "MixType";
    }
    
    public class DavDocument {
      public static final String ACLPRINCIPALPROPS = "acl-principal-props";
      public static final String ACLPRINCIPALPROPSET = "acl-principal-prop-set";
      
      public static final String EXPANDPROPERTY = "";
      
      public static final String LOCKINFO = "lockinfo";
      public static final String PROPERTYBEHAVIOR = "";
      public static final String PROPERTYUPDATE = "propertyupdate";
      public static final String PROPFIND = "propfind";
      public static final String VERSIONTREE = "version-tree";
      public static final String ORDERPATCH = "orderpatch";      
      public static final String SEARCHREQUEST = "searchrequest";  
    }
    
    public class DavProperty {
      public static final String DEPTH = "depth";
      public static final String MULTISTATUS = "multistatus";
      public static final String PROPFIND = "propfind";      
      public static final String SUPPORDEDMETHODSET = "supported-method-set";
    }
     
    public class ResourceType {
      public static final String COLLECTION = "collection";
      public static final String RESOURCE = "resource";
      
    }

    public class Lock {
      public static final String SCOPE_SHARED = "shared";
      public static final String SCOPE_EXCLUSIVE = "exclusive";
      public static final String TYPE_WRITE = "write";      
    }

    public class NodeTypes {
      public static final String JCR_CONTENT = "jcr:content";
      public static final String JCR_DATA = "jcr:data";
      public static final String JCR_FROZENNODE = "jcr:frozenNode";
      public static final String JCR_LOCKOWNER = "jcr:lockOwner";
      public static final String NT_VERSION = "nt:version";
      public static final String JCR_CREATED = "jcr:created";
      public static final String NT_FILE = "nt:file";
      public static final String JCR_ROOTVERSION = "jcr:rootVersion";
      public static final String JCR_LASTMODIFIED = "jcr:lastModified";
      public static final String JCR_MIMETYPE = "jcr:mimeType";
      public static final String NT_RESOURCE = "nt:resource";
      public static final String MIX_LOCKABLE = "mix:lockable";
      public static final String MIX_VERSIONABLE = "mix:versionable";
      public static final String NT_FOLDER = "nt:folder";
    }
    
    public class DateFormat {
      public static final String CREATION = "yyyy-MM-dd'T'HH:mm:ss'Z'";
      public static final String MODIFICATION = "EEE, dd MMM yyyy HH:mm:ss z";
    }
    
}
