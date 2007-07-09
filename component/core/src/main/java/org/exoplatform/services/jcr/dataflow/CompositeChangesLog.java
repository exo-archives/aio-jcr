/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;
/**
 * Created by The eXo Platform SARL        .<br/>
 * Changes log containined other changes logs inside
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public interface CompositeChangesLog extends ItemStateChangesLog {

  /**
   * creates new ChangesLogIterator
   * @return
   */
  ChangesLogIterator getLogIterator();
  
  /**
   * adds new PlainChangesLog
   * @param log
   */
  void addLog(PlainChangesLog log);
  
  /**
   * @return systemId
   */
  String getSystemId();
}
