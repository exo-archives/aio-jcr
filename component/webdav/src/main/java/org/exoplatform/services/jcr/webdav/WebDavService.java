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
   * WedDAV "GET" method. See <a href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP
   * methods for distributed authoring sec. 8.4 "GET, HEAD for Collections"</a>.
   * 
   * @param repoName repository name
   * @param repoPath path in repository
   * @param range Range HTTP header
   * @param version version name
   * @param baseURI base URI info
   * @return the instance of javax.ws.rs.core.Response
   */
  Response get(String repoName, String repoPath, String range, String version, UriInfo baseURI);

  /**
   * WedDAV "HEAD" method. see <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP methods for distributed
   * authoring sec. 8.4 "GET, HEAD for Collections"</a>.
   * 
   * @param repoName repository name
   * @param repoPath path in repository
   * @param baseURI base URI info
   * @return the instance of javax.ws.rs.core.Response
   */
  Response head(String repoName, String repoPath, UriInfo baseURI);

  /**
   * WedDAV "HEAD" method. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>HTTP methods for distributed
   * authoring sec. 8.7 "PUT"</a>.
   * 
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If HTTP Header
   * @param fileNodeTypeHeader JCR NodeType header
   * @param contentNodeTypeHeader JCR Content-NodeType header
   * @param mixinTypes JCR Mixin types header
   * @param mimeType Content-Type HTTP header
   * @param inputStream stream that contain incoming data
   * @return the instance of javax.ws.rs.core.Response
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
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If HTTP Header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response delete(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param destinationHeader Destination HTTP Header
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @param depthHeader Depth HTTP header
   * @param overwriteHeader Overwrite HTTP header
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response
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
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @param nodeTypeHeader JCR Node-Type header
   * @param mixinTypesHeader JCR Mixin-Types header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response mkcol(String repoName,
                 String repoPath,
                 String lockTokenHeader,
                 String ifHeader,
                 String nodeTypeHeader,
                 List<String> mixinTypesHeader);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param destinationHeader Destination HTTP header
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @param depthHeader Depth HTTP header
   * @param overwriteHeader Overwrite HTTP header
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response
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
   * @param repoName repository name
   * @return the instance of javax.ws.rs.core.Response
   */
  Response options(String repoName);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param depthHeader Depth HTTP header
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response HTTP response
   */
  Response propfind(String repoName,
                    String repoPath,
                    String depthHeader,
                    UriInfo baseURI,
                    HierarchicalProperty body);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response HTTP response
   */
  Response proppatch(String repoName,
                     String repoPath,
                     String lockTokenHeader,
                     String ifHeader,
                     UriInfo baseURI,
                     HierarchicalProperty body);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @param depth Depth HTTP header
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response
   */
  Response lock(String repoName,
                String repoPath,
                String lockTokenHeader,
                String ifHeader,
                String depth,
                HierarchicalProperty body);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response unlock(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  // DeltaV: RFC-3253 Versioning Extensions to WebDAV
  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response checkin(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response checkout(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param depth Depth HTTP header
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response
   */
  Response report(String repoName,
                  String repoPath,
                  String depth,
                  UriInfo baseURI,
                  HierarchicalProperty body);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response uncheckout(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @return the instance of javax.ws.rs.core.Response
   */
  Response versionControl(String repoName, String repoPath, String lockTokenHeader, String ifHeader);

  // Order: RFC-3648: Web Distributed Authoring and Versioning (WebDAV)
  // Ordered Collections Protocol

  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param lockTokenHeader Lock-Token HTTP header
   * @param ifHeader If- HTTP Header
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response
   */
  Response order(String repoName,
                 String repoPath,
                 String lockTokenHeader,
                 String ifHeader,
                 UriInfo baseURI,
                 HierarchicalProperty body);

  // Search
  /**
   * @param repoName repository name
   * @param repoPath path in repository
   * @param baseURI base URI info
   * @param body Request body
   * @return the instance of javax.ws.rs.core.Response
   */
  Response search(String repoName, String repoPath, UriInfo baseURI, HierarchicalProperty body);
}
