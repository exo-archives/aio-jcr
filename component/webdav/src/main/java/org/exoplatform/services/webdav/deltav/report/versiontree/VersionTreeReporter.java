/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report.versiontree;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.deltav.report.DeltaVReport;
import org.exoplatform.services.webdav.deltav.report.DeltaVReporter;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeReporter implements DeltaVReporter {
  
  private VersionTreeReportDocument reportDocument;
  
  public VersionTreeReporter(VersionTreeReportDocument reportDocument) {
    this.reportDocument = reportDocument;
  }
  
  public DeltaVReport doReport(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof DeltaVResource)) {
      throw new AccessDeniedException("Can't report not versionned node");
    }
    
    VersionTreeResponseBuilder responseBuilder = new VersionTreeResponseBuilder(resource, reportDocument);

    ArrayList<MultiStatusResponse> responses = responseBuilder.getVersionResponses();

    return new VersionTreeReport(responses);
  }

}
