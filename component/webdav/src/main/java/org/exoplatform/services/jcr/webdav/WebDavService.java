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
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.common.util.HierarchicalProperty;

/**
 * Created by The eXo Platform SARL .<br/>
 * JCR WebDAV entry point. Defines WebDav protocol methods: RFC-2518 HTTP
 * Extensions for Distributed Authoring -- WEBDAV RFC-3253 Versioning Extensions
 * to WebDAV RFC-3648: Web Distributed Authoring and Versioning (WebDAV) Ordered
 * Collections Protocol
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface WebDavService {

  /**
   * WedDAV "GET" method see<ahref='http://www.ietf.org/rfc/rfc2518.txt'>HTTP
   * methods for distributed authoring sec. 8.4 "GET, HEAD for Collections"</a>.
   * 
   * @param repoName
   * @param repoPath
   * @param range
   * @param version
   * @param baseURI
   * @return
   */
  Response get(String repoName, String repoPath, String range, String version, UriInfo baseURI);

  /**
   * WedDAV "HEAD" method see<ahref='http://www.ietf.org/rfc/rfc2518.txt'>HTTP
   * methods for distributed authoring sec. 8.4 "GET, HEAD for Collections"</a>.
   * 
   * @param repoName
   * @param repoPath
   * @param baseURI
   * @return
   */
  Response head(String repoName, String repoPath, UriInfo baseURI);

  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @param nodeTypeHeader
   * @param mixinTypes
   * @param mimeType
   * @param inputStream
   * @return
   */
  Response put(String repoName,
               String repoPath,
               String lockTokenHeader,
               String ifHeader,
               String fileNodeTypeHeader,
               String contentNodeTypeHeader,
               List<String> mixinTypes,
               String mimeType,
               InputStream inputStream);

  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @return
   */
  Response delete(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param destinationHeader
   * @param lockTokenHeader
   * @param ifHeader
   * @param depthHeader
   * @param overwriteHeader
   * @param baseURI
   * @param body
   * @return
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
   * @param lockTokenHeader
   * @param ifHeader
   * @param nodeTypeHeader
   * @param mixinTypesHeader
   * @return
   */
  Response mkcol(String repoName,
                 String repoPath,
                 String lockTokenHeader,
                 String ifHeader,
                 String nodeTypeHeader,
                 List<String> mixinTypesHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param destinationHeader
   * @param lockTokenHeader
   * @param ifHeader
   * @param depthHeader
   * @param overwriteHeader
   * @param baseURI
   * @param body
   * @return
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
   * @param repoName
   * @return
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
   * @param lockTokenHeader
   * @param ifHeader
   * @param depth
   * @param body
   * @return
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
   * @param lockTokenHeader
   * @param ifHeader
   * @return
   */
  Response unlock(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  // DeltaV: RFC-3253 Versioning Extensions to WebDAV
  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @return
   */
  Response checkin(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @return
   */
  Response checkout(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param depth
   * @param baseURI
   * @param body
   * @return
   */
  Response report(String repoName,
                  String repoPath,
                  String depth,
                  UriInfo baseURI,
                  HierarchicalProperty body);

  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @return
   */
  Response uncheckout(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @return
   */
  Response versionControl(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  // Order: RFC-3648: Web Distributed Authoring and Versioning (WebDAV)
  // Ordered Collections Protocol

  /**
   * @param repoName
   * @param repoPath
   * @param lockTokenHeader
   * @param ifHeader
   * @param baseURI
   * @param body
   * @return
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
   * @param baseURI
   * @param body
   * @return
   */
  Response search(String repoName, String repoPath, UriInfo baseURI, HierarchicalProperty body);
}
