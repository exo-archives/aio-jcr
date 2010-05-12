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

package org.exoplatform.applications.ooplugin.client;

/**
 * Created by The eXo Platform SARL Author : <a href="mailto:gavrik-vetal@gmail.com">Vitaly
 * Guly</a>.
 * 
 * @version $Id: WebDavHeaders.java 35184 2009-08-07 14:14:37Z pnedonosko $
 */

public class WebDavHeaders {

  /**
   * WebDav "Depth" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9 for more information.
   */
  public static final String DEPTH                = "depth";

  /**
   * HTTP 1.1 "Accept-Ranges" header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
   * "Header Field Definitions"</a> for more information.
   */
  public static final String ACCEPT_RANGES        = "Accept-Ranges";

  /**
   * HTTP 1.1 "Allow" header. See <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'>
   * HTTP/1.1 section 14 "Header Field Definitions"</a> for more information.
   */
  public static final String ALLOW                = "Allow";

  /**
   * HTTP 1.1 "Authorization" header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
   * "Header Field Definitions"</a> for more information.
   */
  public static final String AUTHORIZATION        = "Authorization";

  /**
   * HTTP 1.1 "Content-Length" header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
   * "Header Field Definitions"</a> for more information.
   */
  public static final String CONTENTLENGTH        = "Content-Length";

  /**
   * HTTP 1.1 "Content-Range" header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
   * "Header Field Definitions"</a> for more information.
   */
  public static final String CONTENTRANGE         = "Content-Range";

  /**
   * HTTP 1.1 "Content-type" header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP/1.1 section 14
   * "Header Field Definitions"</a> for more information.
   */
  public static final String CONTENTTYPE          = "Content-type";

  /**
   * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9 for more information.
   */
  public static final String DAV                  = "DAV";

  /**
   * HTTP 1.1 "Allow" header. See <a href='http://msdn.microsoft.com/en-us/library/ms965954.aspx'>
   * WebDAV/DASL Request and Response Syntax</a> for more information.
   */
  public static final String DASL                 = "DASL";

  /**
   * MS-Author-Via Response Header. See <a
   * href='http://msdn.microsoft.com/en-us/library/cc250217.aspx'> MS-Author-Via Response Header</a>
   * for more information.
   */
  public static final String MSAUTHORVIA          = "MS-Author-Via";

  /**
   * JCR "Nodetype" header.
   */
  public static final String NODETYPE             = "NodeType";

  /**
   * HTTP 1.1 "Range" header. See <a href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'>
   * HTTP/1.1 section 14 "Header Field Definitions"</a> for more information.
   */
  public static final String RANGE                = "Range";

  /**
   * WebDav "Destination" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers
   * for Distributed Authoring</a> section 9 for more information.
   */
  public static final String DESTINATION          = "Destination";

  /**
   * JCR "MixType" header.
   */
  public static final String MIXTYPE              = "MixType";

  /**
   * WebDav "DAV" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9 for more information.
   */
  public static final String LOCKTOKEN            = "lock-token";

  /**
   * WebDav "If" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9 for more information.
   */
  public static final String IF                   = "If";

  /**
   * WebDav "Timeout" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9 for more information.
   */
  public static final String TIMEOUT              = "Timeout";

  /**
   * WebDav multipart/byteranges header.
   */
  public static final String MULTIPART_BYTERANGES = "multipart/byteranges; boundary=";

  /**
   * WebDav "Overwrite" header. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9 for more information.
   */
  public static final String OVERWRITE            = "Overwrite";
  
  /**
   * User-Agent header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP Header
   * Field Definitions sec. 14.43 Transfer-Encoding</a>.
   */
  public static final String USERAGENT              = "User-Agent";
  
  /**
   * Transfer-Encoding header. See <a
   * href='http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html'> HTTP Header
   * Field Definitions sec. 14.41 Transfer-Encoding</a>.
   */
  public static final String TRANSFER_ENCODING      = "Transfer-Encoding";
  
  public static final String HOST      =              "Host";

}
