/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.command;

import javax.jcr.AccessDeniedException;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.deltav.report.DeltaVReport;
import org.exoplatform.services.webdav.deltav.report.DeltaVReportDocument;
import org.exoplatform.services.webdav.deltav.report.DeltaVReporter;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class ReportCommand extends WebDavCommand {
  
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
      Document requestDocument      
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);

      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, serverPrefix, repoPath);      
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);
      
      RequestDocument reportDocument = documentDispatcher.getRequestDocument();
      
      if (!(reportDocument instanceof DeltaVReportDocument)) {
        throw new BadRequestException();
      }
      
      DeltaVReporter reporter = ((DeltaVReportDocument)reportDocument).getReporter();
      
      WebDavResource resource = resourceLocator.getSrcResource(false);
      
      if (!(resource instanceof AbstractNodeResource)) {
        throw new AccessDeniedException("Can't reports not JCR resource.");
      }
      
      DeltaVReport report = reporter.doReport(resource);

      return xmlResponse(report, report.getStatus());
    } catch (Exception exc) {
      return responseByException(exc);
    }    
  }    

}
