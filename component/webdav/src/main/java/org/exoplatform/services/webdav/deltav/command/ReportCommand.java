/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.jcr.Item;
import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.XmlResponseWriter;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class ReportCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.ReportCommand");
  
  public ReportCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.REPORT)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response report(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document document,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,      
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      @HeaderParam(WebDavHeaders.DEPTH) String depthHeader
      ) {
    
    try {
      
      log.info("//////////////////////////////////////////////////////////////////////////");
      log.info("/ REPORTING //////////////////////////////////////////////////////////////");
      log.info("//////////////////////////////////////////////////////////////////////////");
      
      
      String serverPrefix = getServerPrefix(repoName, repoPath);
      
      log.info("ServerPrefix: " + serverPrefix);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      RequestRepresentation requestRepresentation = webDavService.getRequestDispatcher().getRequestRepresentation(document);
      
      log.info("RequestRepresentation: " + requestRepresentation);
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);
      
      log.info("Item: " + item);
      
      tuneSession(item.getSession(), lockTokens);

      ResponseRepresentation responseRepresentation = requestRepresentation.getResponseRepresentation();
      
      log.info("ResponseRepresentation: " + responseRepresentation);
      
      responseRepresentation.init(serverPrefix, (Node)item, new Integer(depthHeader));
      
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      
      XmlResponseWriter writer = new XmlResponseWriter(responseRepresentation);
      
      //writer.write(outStream);
      
      byte []bytes = outStream.toByteArray();
      log.info("REPLY:\r\n" + new String(bytes));
      
      InputStream inputStream = new ByteArrayInputStream(bytes);

      log.info("//////////////////////////////////////////////////////////////////////////");
      log.info("/ END REPORTING //////////////////////////////////////////////////////////");
      log.info("//////////////////////////////////////////////////////////////////////////");      
      
      return Response.Builder.withStatus(WebDavStatus.MULTISTATUS).header(DavConst.Headers.CONTENTLENGTH, ""+bytes.length).entity(inputStream, "text/xml").build();
      
    } catch (Exception exc) {
      return responseByException(exc);
    }    
  }    

}
