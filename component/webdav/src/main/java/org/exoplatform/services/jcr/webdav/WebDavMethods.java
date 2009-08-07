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

import org.exoplatform.common.http.HTTPMethods;

/**
 * Created by The eXo Platform SARL Author : <a href="mailto:gavrik-vetal@gmail.com">Vitaly
 * Guly</a>.
 * 
 * @version $Id$
 */

public interface WebDavMethods extends HTTPMethods {

  /**
   * WebDav "PROPFIND" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.1 for more information.
   */
  String PROPFIND       = "PROPFIND";

  /**
   * WebDav "REPORT" method.
   */
  String REPORT         = "REPORT";

  /**
   * WebDav "COPY" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.8 for more information.
   */
  String COPY           = "COPY";

  /**
   * HTTP/1.1 "OPTIONS" method. See <a ftp://ftp.isi.edu/in-notes/rfc2616.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9.2 for more information.
   */
  String OPTIONS        = "OPTIONS";

  /**
   * WebDav "MKCOL" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.3 for more information.
   */
  String MKCOL          = "MKCOL";

  /**
   * WebDav "DELETE" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.6 for more information.
   */
  String DELETE         = "DELETE";

  /**
   * WebDav "HEAD" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.4 for more information.
   */
  String HEAD           = "HEAD";

  /**
   * WebDav "MOVE" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.9 for more information.
   */
  String MOVE           = "MOVE";

  /**
   * WebDav "PUT" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.7 for more information.
   */
  String PUT            = "PUT";

  /**
   * WebDav "GET" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.4 for more information.
   */
  String GET            = "GET";

  /**
   * WebDav "LOCK" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.10 for more information.
   */
  String LOCK           = "LOCK";

  /**
   * WebDav "UNLOCK" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.11 for more information.
   */
  String UNLOCK         = "UNLOCK";

  /**
   * WebDav "PROPPATCH" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'> HTTP Headers for
   * Distributed Authoring</a> section 8.2 for more information.
   */
  String PROPPATCH      = "PROPPATCH";

  /**
   * HTTP/1.1 "POST" method. See <a ftp://ftp.isi.edu/in-notes/rfc2616.txt'> HTTP Headers for
   * Distributed Authoring</a> section 9.5 for more information.
   */
  String POST           = "POST";

  /**
   * WebDav "ORDERPATCH" method.
   */
  String ORDERPATCH     = "ORDERPATCH";

  /**
   * WebDav "VERSION-CONTROL" method.
   */
  String VERSIONCONTROL = "VERSION-CONTROL";

  /**
   * WebDav "CHECKIN" method.
   */
  String CHECKIN        = "CHECKIN";

  /**
   * WebDav "CHECKOUT" method.
   */
  String CHECKOUT       = "CHECKOUT";

  /**
   * WebDav "UNCHECKOUT" method.
   */
  String UNCHECKOUT     = "UNCHECKOUT";

  /**
   * WebDav "RESTORE" method.
   */
  String RESTORE        = "RESTORE";

  /**
   * WebDav "SEARCH" method.
   */
  String SEARCH         = "SEARCH";

}
