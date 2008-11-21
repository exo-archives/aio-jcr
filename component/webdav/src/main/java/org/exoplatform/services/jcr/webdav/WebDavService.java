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

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.common.util.HierarchicalProperty;

/**
 * Created by The eXo Platform SARL .<br/>
 * JCR WebDAV entry point. Defines WebDav protocol methods: RFC-2518 HTTP Extensions for Distributed
 * Authoring -- WEBDAV RFC-3253 Versioning Extensions to WebDAV RFC-3648: Web Distributed Authoring
 * and Versioning (WebDAV) Ordered Collections Protocol
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface WebDavService {

  // WebDav: RFC-2518 HTTP Extensions for Distributed Authoring -- WEBDAV
  /**
   * the GET method
   * 
   * @param repoName
   * @param repoPath
   * @param auth
   * @param range
   * @param version
   * @return HTTP response
   */
  Response get(String repoName, String repoPath, String range, String version, UriInfo baseURI);

  /**
   * the HEAD method
   * 
   * @param repoName
   * @param repoPath
   * @param auth
   * @return HTTP response
   */
  Response head(String repoName, String repoPath, UriInfo baseURI);

  /**
   * @param repoName
   * @param repoPath
   * @param inputStream
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param nodeTypeHeader
   * @param mixinTypesHeader
   * @param mimeType
   * @return HTTP response
   */
  Response put(String repoName,
               String repoPath,
               String lockTokenHeader,
               String ifHeader,
               String nodeTypeHeader,
               String mimeType,
               InputStream inputStream);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @return HTTP response
   */
  Response delete(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param destinationHeader
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response copy(String repoName,
                String repoPath,
                String destinationHeader,
                String lockTokenHeader,
                String ifHeader,
                String depthHeader,
                String overwriteHeader,
                UriInfo baseURI,
                HierarchicalProperty body);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param nodeTypeHeader
   * @param mixinTypesHeader
   * @return HTTP response
   */
  Response mkcol(String repoName,
                 String repoPath,
                 String lockTokenHeader,
                 String ifHeader,
                 String nodeTypeHeader,
                 String mixinTypesHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param destinationHeader
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response move(String repoName,
                String repoPath,
                String destinationHeader,
                String lockTokenHeader,
                String ifHeader,
                String depthHeader,
                String overwriteHeader,
                UriInfo baseURI,
                HierarchicalProperty body);

  /**
   * @return HTTP response
   */
  Response options(String repoName);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param depth
   * @param body
   * @return HTTP response
   */
  Response propfind(String repoName,
                    String repoPath,
                    String depthHeader,
                    UriInfo baseURI,
                    HierarchicalProperty body);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response proppatch(String repoName,
                     String repoPath,
                     String lockTokenHeader,
                     String ifHeader,
                     UriInfo baseURI,
                     HierarchicalProperty body);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response lock(String repoName,
                String repoPath,
                String lockTokenHeader,
                String ifHeader,
                String depth,
                HierarchicalProperty body);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response unlock(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  // DeltaV: RFC-3253 Versioning Extensions to WebDAV
  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response checkin(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response checkout(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param body
   * @return HTTP response
   */
  Response report(String repoName,
                  String repoPath,
                  String depth,
                  UriInfo baseURI,
                  HierarchicalProperty body);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response uncheckout(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @return HTTP response
   */
  Response versionControl(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  // Order: RFC-3648: Web Distributed Authoring and Versioning (WebDAV)
  // Ordered Collections Protocol

  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response order(String repoName,
                 String repoPath,
                 String lockTokenHeader,
                 String ifHeader,
                 UriInfo baseURI,
                 HierarchicalProperty body);

  // Search
  /**
   * @param repoName
   * @param repoPath
   * @param auth
   * @param lockTokenHeader
   * @param ifHeader
   * @param body
   * @return HTTP response
   */
  Response search(String repoName, String repoPath, UriInfo baseURI, HierarchicalProperty body);
}
