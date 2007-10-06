/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report.versiontree;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.request.documents.PropertyRequiredDocument;
import org.exoplatform.services.webdav.deltav.report.DeltaVReportDocument;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeReportDocument extends PropertyRequiredDocument implements DeltaVReportDocument {
  
  public String getDocumentName() {
    return DavConst.DavDocument.VERSIONTREE;
  }
  
  public VersionTreeReporter getReporter() {
    return new VersionTreeReporter(this);
  }
  
}
