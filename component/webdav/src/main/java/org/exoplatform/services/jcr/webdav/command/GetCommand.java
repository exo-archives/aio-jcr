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

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.transform.stream.StreamSource;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.webdav.Range;
import org.exoplatform.services.jcr.webdav.WebDavConst;
import org.exoplatform.services.jcr.webdav.WebDavHeaders;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.FileResource;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.resource.ResourceUtil;
import org.exoplatform.services.jcr.webdav.resource.VersionResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedFileResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedResource;
import org.exoplatform.services.jcr.webdav.util.MultipartByterangesEntity;
import org.exoplatform.services.jcr.webdav.util.RangedInputStream;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.rest.transformer.XSLT4SourceOutputTransformer;
import org.exoplatform.services.rest.transformer.XSLTConstants;
import org.exoplatform.services.xml.transform.impl.trax.TRAXTemplatesServiceImpl;
import org.exoplatform.services.xml.transform.trax.TRAXTemplatesService;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class GetCommand {

  /**
   * GET content of the resource.
   * Can be return content of the file.
   * The content returns in the XML type.
   * If version parameter is present, returns the content of the version of the resource. 
   *  
   * @param session
   * @param path
   * @param version
   * @param baseURI
   * @param range
   * @return
   */
  public Response get(Session session, String path, String version, String baseURI,
      List<Range> ranges) {

    if (null == version) {
      if (path.indexOf("?version=") > 0) {
        version = path.substring(path.indexOf("?version=") + "?version=".length());
        path = path.substring(0, path.indexOf("?version="));
      }
    }

    try {
      /*
       * Path can be encoded in LinkGenerator.
       * Problem with spaces, they can be encoded in two ways:
       * 1. %20
       * 2. +
       */
      path = URLDecoder.decode(path, "UTF-8");
      Node node = (Node) session.getItem(path);

      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(session);
      URI uri = new URI(TextUtil.escape(baseURI + node.getPath(), '%', true));
      
      Resource resource;
      InputStream istream;

      if (ResourceUtil.isFile(node)) {

        if (version != null) {
          VersionedResource versionedFile = new VersionedFileResource(uri, node, nsContext);
          resource = versionedFile.getVersionHistory().getVersion(version);
          istream = ((VersionResource) resource).getContentAsStream();
        } else {
          resource = new FileResource(uri, node, nsContext);
          istream = ((FileResource) resource).getContentAsStream();
        }

        HierarchicalProperty contentLengthProperty = resource
            .getProperty(FileResource.GETCONTENTLENGTH);
        long contentLength = new Long(contentLengthProperty.getValue());

        HierarchicalProperty mimeTypeProperty = resource.getProperty(FileResource.GETCONTENTTYPE);
        String contentType = mimeTypeProperty.getValue();

        // content length is not present
        if (contentLength == 0) {
          return Response.Builder.ok().header(WebDavHeaders.ACCEPT_RANGES, "bytes").entity(istream,
              contentType).build();
        }

        // no ranges request 
        if (ranges.size() == 0) {
          return Response.Builder.ok().header(WebDavHeaders.CONTENTLENGTH,
              Long.toString(contentLength)).header(WebDavHeaders.ACCEPT_RANGES, "bytes").entity(
              istream, contentType).build();
        }

        // one range
        if (ranges.size() == 1) {
          Range range = ranges.get(0);
          if (!validateRange(range, contentLength))
            return Response.Builder.withStatus(WebDavStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(WebDavHeaders.CONTENTRANGE, "bytes */" + contentLength).build();

          long start = range.getStart();
          long end = range.getEnd();
          long returnedContentLength = (end - start + 1);

          RangedInputStream rangedInputStream = new RangedInputStream(istream, start, end);

          return Response.Builder.withStatus(WebDavStatus.PARTIAL_CONTENT).header(
              WebDavHeaders.CONTENTLENGTH, Long.toString(returnedContentLength)).header(
              WebDavHeaders.ACCEPT_RANGES, "bytes").header(WebDavHeaders.CONTENTRANGE,
              "bytes " + start + "-" + end + "/" + contentLength).entity(rangedInputStream,
              contentType).build();
        }

        // multipart byte ranges as byte:0-100,80-150,210-300
        for (int i = 0; i < ranges.size(); i++) {
          Range range = ranges.get(i);
          if (!validateRange(range, contentLength))
            return Response.Builder.withStatus(WebDavStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(WebDavHeaders.CONTENTRANGE, "bytes */" + contentLength).build();
          ranges.set(i, range);
        }

        MultipartByterangesEntity mByterangesEntity = new MultipartByterangesEntity(resource,
            ranges, contentType, contentLength);

        return Response.Builder.withStatus(WebDavStatus.PARTIAL_CONTENT).header(
            WebDavHeaders.ACCEPT_RANGES, "bytes").entity(mByterangesEntity,
            WebDavHeaders.MULTIPART_BYTERANGES + WebDavConst.BOUNDARY).transformer(
            new SerializableTransformer()).build();
      } else {
        //Collection processing;
        resource = new CollectionResource(uri, node, nsContext);
        istream = ((CollectionResource) resource).getContentAsStream(baseURI);
        
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        TRAXTemplatesService templateService = (TRAXTemplatesService) container
            .getComponentInstanceOfType(TRAXTemplatesServiceImpl.class);
        
        Map<String, String> tp = new HashMap<String, String>();
        tp.put(XSLTConstants.XSLT_TEMPLATE, "get.method.template");
        
        XSLT4SourceOutputTransformer transformer = new XSLT4SourceOutputTransformer(templateService);
        
        return Response.Builder.ok().entity(new StreamSource(istream), "text/html").transformer(
            transformer).setTransformerParameters(tp).build();

      }

    } catch (PathNotFoundException exc) {
      exc.printStackTrace();
      return Response.Builder.notFound().build();
    } catch (RepositoryException exc) {
      exc.printStackTrace();
      return Response.Builder.serverError().build();
    } catch (Exception exc) {
      exc.printStackTrace();
      return Response.Builder.serverError().build();
    }
  }

  private boolean validateRange(Range range, long contentLength) {
    long start = range.getStart();
    long end = range.getEnd();

    // range set as bytes:-100
    // take 100 bytes from end
    if (start < 0 && end == -1) {
      if ((-1 * start) >= contentLength) {
        start = 0;
        end = contentLength - 1;
      } else {
        start = contentLength + start;
        end = contentLength - 1;
      }
    }

    // range set as bytes:100-
    // take from 100 to the end
    if (start >= 0 && end == -1)
      end = contentLength - 1;

    // normal range set as bytes:100-200
    // end can be greater then content-length
    if (end >= contentLength)
      end = contentLength - 1;

    if (start >= 0 && end >= 0 && start <= end) {
      range.setStart(start);
      range.setEnd(end);
      return true;
    }
    return false;
  }

}
