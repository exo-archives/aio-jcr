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

package org.exoplatform.services.jcr.webdav;

/**
 * Constants used for webdav service implemetation.
 * 
 * @author <a href="mailto:gavrik-vetal@gmail.com">Vitaly Guly</a>
 */
public class WebDavConst {

  /**
   * 
   */
  public static final String WDBDAV_COMMAND_CATALOG = "WEBDAV";

  /**
   * HTTP-protocol version.
   */
  public static final String HTTPVER                = "HTTP/1.1";

  /**
   * Some boundary const.
   */
  public static final String BOUNDARY               = "1234567890";

  /**
   * WebDav "MS-Author-Via" header value.
   */
  public static final String DAV_MSAUTHORVIA        = "DAV";

  /**
   * WebDav "DAV" namespace value.
   */
  public static final String DAV_NAMESPACE          = "DAV:";

  /**
   * WebDav "eXo" namespace value.
   */
  public static final String EXO_NAMESPACE          = "http://exoplatform.com/jcr";

  /**
   * WebDav "DAV" prefix.
   */
  public static final String DAV_PREFIX             = "D:";

  /**
   * WebDav server version.
   */
  public static final String DAV_SERVER             = "eXo-Webdav Server /1.0";

  /**
   * WebDav default header value.
   */
  public static final String DAV_HEADER             = "1, 2, ordered-collections";

  /**
   * DAV Searching And Locating request value.
   */
  public static final String DASL_VALUE             = "<DAV:basicsearch>"
                                                        + "<exo:sql xmlns:exo=\"http://exoplatform.com/jcr\"/>"
                                                        + "<exo:xpath xmlns:exo=\"http://exoplatform.com/jcr\"/>";

  /**
   * WebDav version identifier.
   */
  public static final String DAV_VERSIONIDENTIFIER  = "VERSIONID";

  /**
   * WebDav version prefix.
   */
  public static final String DAV_VERSIONPREFIX      = "?" + DAV_VERSIONIDENTIFIER + "=";

  /**
   * WebDav default mime-type.
   */
  public static final String DAV_DEFAULT_MIME_TYPE  = "text/plain";

  /**
   * Http headers used for webdav service implemetation.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class Headers {

    /**
     * HTTP 1.1 "Allow" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String ALLOW           = "Allow";

    /**
     * HTTP 1.1 "Allow" header. See <a href='http://msdn.microsoft.com/en-us/library/ms965954.aspx'>
     * WebDAV/DASL Request and Response Syntax</a> for more information.
     */
    public static final String DASL            = "DASL";

    /**
     * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
     * Distributed Authoring</a> section 9 for more information.
     */
    public static final String DAV             = "DAV";

    /**
     * HTTP 1.1 "Accept-Encoding" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String ACCEPTENCODING  = "Accept-Encoding";

    /**
     * HTTP 1.1 "Connection" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String CONNECTION      = "Connection";

    /**
     * HTTP 1.1 "Content-Length" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String CONTENTLENGTH   = "Content-Length";

    /**
     * HTTP 1.1 "Content-Range" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String CONTENTRANGE    = "Content-Range";

    /**
     * HTTP 1.1 "Content-type" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String CONTENTTYPE     = "Content-type";

    /**
     * HTTP 1.1 "Host" header. See <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'>
     * HTTP/1.1 section 14 "Header Field Definitions"</a> for more information.
     */
    public static final String HOST            = "Host";

    /**
     * HTTP 1.1 "User-Agent" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String USERAGENT       = "User-Agent";

    /**
     * HTTP 1.1 "WWW-Authenticate" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String WWWAUTHENTICATE = "WWW-Authenticate";

    /**
     * HTTP 1.1 "Accept-Ranges" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String ACCEPT_RANGES   = "Accept-Ranges";

    /**
     * HTTP 1.1 "Authorization" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String AUTHORIZATION   = "Authorization";

    /**
     * HTTP 1.1 "Cache-Control" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String CACHECONTROL    = "Cache-Control";

    /**
     * HTTP 1.1 "Server" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String SERVER          = "Server";

    /**
     * WebDav "Depth" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
     * Distributed Authoring</a> section 9 for more information.
     */
    public static final String DEPTH           = "Depth";

    /**
     * WebDav "Destination" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers
     * for Distributed Authoring</a> section 9 for more information.
     */
    public static final String DESTINATION     = "Destination";

    /**
     * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
     * Distributed Authoring</a> section 9 for more information.
     */
    public static final String LOCKTOKEN       = "Lock-Token";

    /**
     * HTTP 1.1 "Last-Modified" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String LASTMODIFIED    = "Last-Modified";

    /**
     * MS-Author-Via Response Header. See <a
     * href='http://msdn.microsoft.com/en-us/library/cc250217.aspx'> MS-Author-Via Response
     * Header</a> for more information.
     */
    public static final String MSAUTHORVIA     = "MS-Author-Via";

    /**
     * WebDav "Overwrite" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers
     * for Distributed Authoring</a> section 9 for more information.
     */
    public static final String OVERWRITE       = "Overwrite";

    /**
     * HTTP 1.1 "TE" header. See <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'>
     * HTTP/1.1 section 14 "Header Field Definitions"</a> for more information.
     */
    public static final String TE              = "TE";

    /**
     * WebDav "Timeout" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
     * Distributed Authoring</a> section 9 for more information.
     */
    public static final String TIMEOUT         = "Timeout";

    /**
     * HTTP 1.1 "Translate" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String TRANSLATE       = "Translate";

    /**
     * Some HTTP header.
     */
    public static final String XFEATURES       = "X-Features";

    /**
     * HTTP 1.1 "Date" header. See <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'>
     * HTTP/1.1 section 14 "Header Field Definitions"</a> for more information.
     */
    public static final String DATE            = "Date";

    /**
     * WebDav "If" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
     * Distributed Authoring</a> section 9 for more information.
     */
    public static final String IF              = "If";

    /**
     * HTTP 1.1 "Range" header. See <a
     * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
     * "Header Field Definitions"</a> for more information.
     */
    public static final String RANGE           = "Range";
    
    /**
     * See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25">HTTP/1.1 documentation</a>}.
     */
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

    /**
     * JCR "Nodetype" header.
     */
    public static final String NODETYPE        = "NodeType";

    /**
     * JCR "MixType" header.
     */
    public static final String MIXTYPE         = "MixType";
  }

  /**
   * Webdav document properties.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class DavDocument {

    /**
     * Webdav document "acl-principal-props" property. See <a
     * href='http://tools.ietf.org/html/draft-ietf-webdav-acl-06'>WebDAV Access Control Protocol</a>
     * for more information.
     */
    public static final String ACLPRINCIPALPROPS   = "acl-principal-props";

    /**
     * Webdav document "acl-principal-prop-set" property. See <a
     * href='http://tools.ietf.org/html/draft-ietf-webdav-acl-06'>WebDAV Access Control Protocol</a>
     * for more information.
     */
    public static final String ACLPRINCIPALPROPSET = "acl-principal-prop-set";

    /**
     * Webdav document "expand property" property. See <a
     * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to WebDAV</a> for more
     * information.
     */
    public static final String EXPANDPROPERTY      = "";

    /**
     * Webdav document "lockinfo" property.
     */
    public static final String LOCKINFO            = "lockinfo";

    /**
     * Webdav document "property behavior" property. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions for Distributed Authoring </a> for
     * more information.
     */
    public static final String PROPERTYBEHAVIOR    = "";

    /**
     * Webdav document "propertyupdate" property. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions for Distributed Authoring </a> for
     * more information.
     */
    public static final String PROPERTYUPDATE      = "propertyupdate";

    /**
     * Webdav document "propfind" property. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP
     * Extensions for Distributed Authoring </a> for more information.
     */
    public static final String PROPFIND            = "propfind";

    /**
     * Webdav document "version-tree" property. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions for Distributed Authoring </a> for
     * more information.
     */
    public static final String VERSIONTREE         = "version-tree";

    /**
     * Webdav document "orderpatch" property. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP
     * Extensions for Distributed Authoring </a> for more information.
     */
    public static final String ORDERPATCH          = "orderpatch";

    /**
     * Webdav document "searchrequest" property. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions for Distributed Authoring </a> for
     * more information.
     */
    public static final String SEARCHREQUEST       = "searchrequest";
  }

  /**
   * Webdav custom properties.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class DavProperty {

    /**
     * Webdav "depth" property. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions
     * for Distributed Authoring </a> for more information.
     */
    public static final String DEPTH              = "depth";

    /**
     * Webdav "multistatus" property. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP
     * Extensions for Distributed Authoring </a> for more information.
     */
    public static final String MULTISTATUS        = "multistatus";

    /**
     * Webdav "propfind" property. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions
     * for Distributed Authoring </a> for more information.
     */
    public static final String PROPFIND           = "propfind";

    /**
     * Webdav "supported-method-set" property. See <a
     * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP Extensions for Distributed Authoring </a> for
     * more information.
     */
    public static final String SUPPORDEDMETHODSET = "supported-method-set";
  }

  /**
   * Webdav resource types.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class ResourceType {

    /**
     * Webdav "collection" resource type.
     */
    public static final String COLLECTION = "collection";

    /**
     * Webdav "resource" resource type.
     */
    public static final String RESOURCE   = "resource";

  }

  /**
   * Webdav locks types.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class Lock {

    /**
     * Webdav "shared" lock type.
     */
    public static final String SCOPE_SHARED    = "shared";

    /**
     * Webdav "exclusive" lock type.
     */
    public static final String SCOPE_EXCLUSIVE = "exclusive";

    /**
     * Webdav "write" lock type.
     */
    public static final String TYPE_WRITE      = "write";
  }

  /**
   * Jcr node types used by webdav.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class NodeTypes {
    /**
     * JCR "jcr:content" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_CONTENT      = "jcr:content";

    /**
     * JCR "jcr:data" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170: Content
     * Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_DATA         = "jcr:data";

    /**
     * JCR "jcr:frozenNode" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_FROZENNODE   = "jcr:frozenNode";

    /**
     * JCR "jcr:lockOwner" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_LOCKOWNER    = "jcr:lockOwner";

    /**
     * JCR "nt:version" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String NT_VERSION       = "nt:version";

    /**
     * JCR "jcr:created" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_CREATED      = "jcr:created";

    /**
     * JCR "nt:file" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170: Content
     * Repository for JavaTM technology API</a> for more information.
     */
    public static final String NT_FILE          = "nt:file";

    /**
     * JCR "jcr:rootVersion" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_ROOTVERSION  = "jcr:rootVersion";

    /**
     * JCR "jcr:lastModified" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_LASTMODIFIED = "jcr:lastModified";

    /**
     * JCR "jcr:mimeType" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String JCR_MIMETYPE     = "jcr:mimeType";

    /**
     * JCR "nt:resource" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String NT_RESOURCE      = "nt:resource";

    /**
     * JCR "mix:lockable" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String MIX_LOCKABLE     = "mix:lockable";

    /**
     * JCR "mix:versionable" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170:
     * Content Repository for JavaTM technology API</a> for more information.
     */
    public static final String MIX_VERSIONABLE  = "mix:versionable";

    /**
     * JCR "nt:folder" NodeType. See <a href='http://jcp.org/en/jsr/detail?id=170'> JSR 170: Content
     * Repository for JavaTM technology API</a> for more information.
     */
    public static final String NT_FOLDER        = "nt:folder";
  }

  /**
   * Date format patterns used by webdav.
   * 
   * @author <a href="mailto:dkatayev@gmail.com">Dmytro Katayev</a>
   */
  public class DateFormat {

    /**
     * Creation date pattern.
     */
    public static final String CREATION     = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * Last modification date psttern.
     */
    public static final String MODIFICATION = "EEE, dd MMM yyyy HH:mm:ss z";
    
    /**
     * If-Modified-Since date psttern.
     */
    public static final String IF_MODIFIED_SINCE_PATTERN = "EEE, d MMM yyyy HH:mm:ss z";
    
    
  }

}
