/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report.locatebyhistory;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.deltav.report.DeltaVReportDocument;
import org.exoplatform.services.webdav.deltav.report.DeltaVReporter;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LocateByHistoryReportDocument implements DeltaVReportDocument {
  
  private static Log log = ExoLogger.getLogger("jcr.LocateByHistoryReportDocument");
  
  public LocateByHistoryReportDocument() {
    log.info("Construct............");
  }
  
  public DeltaVReporter getReporter() {
    log.info("returning reporter.......");
    return new LocateByHistoryReporter(this);
  }  

}

