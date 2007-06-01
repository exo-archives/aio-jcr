/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.session;

import java.util.Calendar;

import org.exoplatform.services.jcr.dataflow.DataManager;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface WorkspaceStorageDataManagerProxy extends DataManager {

  Calendar getCurrentTime();

}