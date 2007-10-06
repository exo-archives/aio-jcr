/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report.locatebyhistory;

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

public class LocateByHistoryReporter implements DeltaVReporter {
  
  private static Log log = ExoLogger.getLogger("jcr.LocateByHistoryReporter");
  
  private LocateByHistoryReportDocument reportDocument;
  
  public LocateByHistoryReporter(LocateByHistoryReportDocument reportDocument) {
    log.info("Construct.............");
    
    this.reportDocument = reportDocument;
  }
  
  public DeltaVReport doReport(WebDavResource resource) {
    log.info("DoReport........");
    
    log.info("ReportDocument: " + reportDocument);
    
    return null;
  }

}

