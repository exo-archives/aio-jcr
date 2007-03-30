/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.dataflow;

import java.util.Calendar;

/**
 * Created by The eXo Platform SARL
 *
 * 11.12.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SharedDataManager.java 12843 2007-02-16 09:11:18Z peterit $
 */
public interface SharedDataManager extends DataManager {
  
  /**
   * Return current time of a persistent data manager
   */
  Calendar getCurrentTime();

}
