/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.svn.report;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.report.DeltaVReport;
import org.exoplatform.services.webdav.deltav.report.DeltaVReporter;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SVNUpdateReporter implements DeltaVReporter {
  
  private static Log log = ExoLogger.getLogger("jcr.SVNUpdateReporter");
  
  private SVNUpdateReportDocument reportDocument;
  
  public SVNUpdateReporter(SVNUpdateReportDocument reportDocument) {
    log.info("Construct........");
    
    this.reportDocument = reportDocument;
  }
  
  public DeltaVReport doReport(WebDavResource resource) {
    log.info("Reporting............");
    
    log.info("ReportDocument: " + reportDocument);
    
    return null;
  }

}
