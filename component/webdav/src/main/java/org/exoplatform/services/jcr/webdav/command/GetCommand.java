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

package org.exoplatform.services.jcr.webdav.command;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.webdav.WebDavHeaders;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.resource.FileResource;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.resource.ResourceUtil;
import org.exoplatform.services.jcr.webdav.resource.VersionResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedFileResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedResource;
import org.exoplatform.services.jcr.webdav.util.RangedInputStream;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class GetCommand {
  
  private InputStream inputStream;
  
  private long contentLength;
  
  private String contentType;
  
  /**
   * GET content of the resource.
   * Can be return content of the file.
   * The content returns in the XML type.
   * If version parameter is present, returns the content of the version of the resource. 
   *  
   * 
   * @param session
   * @param path
   * @param version
   * @param baseURI
   * @param startRange
   * @param endRange
   * @return
   */
  public Response get(Session session, String path, String version, String baseURI, long startRange, long endRange) {
    if (null == version) {
      if (path.indexOf("?version=") > 0) {
        version = path.substring(path.indexOf("?version=") + "?version=".length());
        path = path.substring(0, path.indexOf("?version="));
      }      
    }
    
    try {      
      Node node = (Node)session.getItem(path);
      
      if (ResourceUtil.isFile(node)) {
        WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
        URI uri = new URI(TextUtil.escape(baseURI + node.getPath(), '%', true));        

        Resource resource;
        
        if (version != null) {
          VersionedResource versionedFile = new VersionedFileResource(uri, node, nsContext);
          resource = versionedFile.getVersionHistory().getVersion(version);
          inputStream = ((VersionResource)resource).getContentAsStream();
        } else {
          resource = new FileResource(uri, node, nsContext);
          inputStream = ((FileResource)resource).getContentAsStream();
        }
        
        HierarchicalProperty contentLengthProperty = resource.getProperty(FileResource.GETCONTENTLENGTH); 
        contentLength = new Long(contentLengthProperty.getValue());
        
        HierarchicalProperty mimeTypeProperty = resource.getProperty(FileResource.GETCONTENTTYPE);
        contentType = mimeTypeProperty.getValue();        
        
        return doGet(startRange, endRange);
      }

      /*
       *  will be implemented later 
       */
      return Response.Builder.ok().build();      
      
    } catch (PathNotFoundException exc) {
      return Response.Builder.notFound().build();
      
    } catch (RepositoryException exc) {
      return Response.Builder.serverError().build();
      
    } catch (Exception exc) {
      return Response.Builder.serverError().build();
    }
    
  }
  
  private Response doGet(long startRange, long endRange) throws IOException {
    
    if (contentLength == 0) {
      return Response.Builder.ok().header(WebDavHeaders.ACCEPT_RANGES, "bytes").
        entity(inputStream, contentType).build();
    }

    if ((startRange > contentLength - 1) || (endRange > contentLength - 1)) {
      return Response.Builder.withStatus(WebDavStatus.REQUESTED_RANGE_NOT_SATISFIABLE).
          header(WebDavHeaders.ACCEPT_RANGES, "bytes").
          entity(inputStream, contentType).build();
    }
    
    boolean isPartialContent = (startRange < 0) ? false : true;

    if (startRange < 0) {
      startRange = 0;
    }    
    
    if (endRange < 0) {
      endRange = contentLength - 1;
    }
    
    RangedInputStream rangedInputStream = new RangedInputStream(inputStream, startRange, endRange);
    
    if (isPartialContent) {      
      long returnedContentLength = (endRange - startRange + 1);
      
      return Response.Builder.withStatus(WebDavStatus.PARTIAL_CONTENT).
          header(WebDavHeaders.CONTENTLENGTH, "" + returnedContentLength).
          header(WebDavHeaders.ACCEPT_RANGES, "bytes").
          header(WebDavHeaders.CONTENTRANGE, "bytes " + startRange + "-" + endRange + "/" + contentLength).
          entity(rangedInputStream, contentType).build();
    }
    
    return Response.Builder.ok().header(WebDavHeaders.CONTENTLENGTH,
        "" + contentLength).header(WebDavHeaders.ACCEPT_RANGES, "bytes")
        .entity(inputStream, contentType).build();    
  }  

}
