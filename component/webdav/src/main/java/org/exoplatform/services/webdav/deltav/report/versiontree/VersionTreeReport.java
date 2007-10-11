/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report.versiontree;

import java.util.ArrayList;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.deltav.report.DeltaVReport;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeReport extends MultiStatus implements DeltaVReport {
  
  public int getStatus() {
    return WebDavStatus.MULTISTATUS;
  }
  
  public VersionTreeReport(ArrayList<MultiStatusResponse> responses) {
    super(responses);
  }
  
}
