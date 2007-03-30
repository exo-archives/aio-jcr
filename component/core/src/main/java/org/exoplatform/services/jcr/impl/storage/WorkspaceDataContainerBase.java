/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.storage;


import java.util.Calendar;
import java.util.Date;

import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;


/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceDataContainerBase.java 12841 2007-02-16 08:58:38Z peterit $
 */

abstract public class WorkspaceDataContainerBase implements WorkspaceDataContainer {

  //private static long id = System.currentTimeMillis();

  public Calendar getCurrentTime() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    return cal;
  }

}
