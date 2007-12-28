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

package org.exoplatform.services.webdav.common.command;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.version.Version;
import javax.xml.transform.stream.StreamSource;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.rest.transformer.XSLT4SourceOutputTransformer;
import org.exoplatform.services.rest.transformer.XSLTConstants;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrFileResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrPropertyData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.XmlItemData;
import org.exoplatform.services.webdav.common.response.RangedInputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class GetCommand extends WebDavCommand {
  
  public GetCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.GET)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  // XSLT4SourceOutputTransformer defined here but used only once
  // because this type of transformer needs component from ExoContainer.
  // It will be created by EntityTransformerFactory from REST engine.
  @OutputTransformer(XSLT4SourceOutputTransformer.class)
  public Response get(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.RANGE) String rangeHeader,
      @QueryParam("VERSIONID") String versionId
      ) {
    
    try {
      String serverPrefix = getPrefix(repoPath);
      
      long rangeStart = -1;
      
      long rangeEnd = -1;
      
      if (rangeHeader != null) {
        if (rangeHeader.startsWith("bytes=")) {
          String rangeString = rangeHeader.substring(rangeHeader.indexOf("=") + 1);
          String[] curRanges = rangeString.split("-");

          if (curRanges.length > 0) {
            rangeStart = new Long(curRanges[0]);
            if (curRanges.length > 1) {
              rangeEnd = new Long(curRanges[1]);
            }
          }
        }
      }

      SessionProvider sessionProvider = getSessionProvider(authorization);      
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);
      
      ResourceData resourceData = null;
      
      if (item instanceof Node) {
        Node node = (Node)item;
        
        if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
          Node fileNode = node;
          
          if (versionId != null) {
            Version version = node.getVersionHistory().getVersion(versionId);
            fileNode = version.getNode(DavConst.NodeTypes.JCR_FROZENNODE);            
          }
          
          resourceData = new JcrFileResourceData(fileNode);
        } else {          
          resourceData = new XmlItemData(serverPrefix + "/" + repoPath.split("/")[0], node);
        }
      } else {
        resourceData = new JcrPropertyData((Property)item);
      }
      
      return doGet(resourceData, rangeStart, rangeEnd, rangeHeader != null);      
    } catch (Exception exc) {
      return responseByException(exc);
    }
  }
  
  private Response doGet(ResourceData resourceData, long startRange, long endRange, boolean isPartial) throws Exception {
    String contentType = resourceData.getContentType();
    
    long contentLength = resourceData.getContentLength();
    
    if (resourceData.getContentLength() == 0) {
      return Response.Builder.ok().header(DavConst.Headers.CONTENTLENGTH,
      "0").header(DavConst.Headers.ACCEPT_RANGES, "bytes").entity(resourceData.getContentStream(), contentType)
      .transformer(new PassthroughOutputTransformer()).build();
    }
    
    if ((startRange > contentLength - 1) || (endRange > contentLength - 1)) {
      return Response.Builder.withStatus(WebDavStatus.REQUESTED_RANGE_NOT_SATISFIABLE).
          header(DavConst.Headers.ACCEPT_RANGES, "bytes").entity(resourceData.getContentStream(), contentType)
          .transformer(new PassthroughOutputTransformer()).build();
    }

    if (startRange < 0) {
      startRange = 0;
    }    
    
    if (endRange < 0) {
      endRange = contentLength - 1;
    }
    
    InputStream rangedInputStream = new RangedInputStream(resourceData.getContentStream(),
        startRange, endRange);
    
    if (isPartial) {      
      long returnedContentLength = (endRange - startRange + 1);
      return Response.Builder.withStatus(WebDavStatus.PARTIAL_CONTENT).
          header(DavConst.Headers.CONTENTLENGTH, "" + returnedContentLength).
          header(DavConst.Headers.ACCEPT_RANGES, "bytes").
          header(DavConst.Headers.CONTENTRANGE, "bytes " + startRange + "-" + endRange + "/" + contentLength).
          entity(rangedInputStream, contentType).transformer(new PassthroughOutputTransformer()).build();
    }
    // Collection view
    if (resourceData.isCollection()) {
      Map<String, String> tp = new HashMap<String, String>();
      tp.put(XSLTConstants.XSLT_TEMPLATE, "get.method.template");
      return Response.Builder.ok().entity(new StreamSource(resourceData.getContentStream()),
          "text/html").setTransformerParameters(tp).build();
    }
    
    return Response.Builder.ok().header(DavConst.Headers.CONTENTLENGTH,
        "" + resourceData.getContentLength()).header(DavConst.Headers.ACCEPT_RANGES, "bytes")
        .entity(resourceData.getContentStream(), contentType).transformer(new PassthroughOutputTransformer())
        .build();

  }
  
}
