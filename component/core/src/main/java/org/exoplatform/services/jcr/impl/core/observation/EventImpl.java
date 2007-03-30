/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.impl.core.observation;

import javax.jcr.observation.Event;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: EventImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class EventImpl implements Event {

  private int type;

  private String path;

  private String userId;

//  public EventImpl(int type, ItemData item, SessionImpl session)
//      throws RepositoryException {
//
//    this.type = type;
//    this.userId = session.getUserID();
//    this.path = session.getLocationFactory().createJCRPath(item.getQPath())
//        .getAsString(false);
//  }

  public EventImpl(int type, String path, String userId) {

    this.type = type;
    this.userId = userId;
    this.path = path;
  }
  
  /**
   * @see javax.jcr.observation.Event#getType
   */
  public int getType() {
    return this.type;
  }

  /**
   * @see javax.jcr.observation.Event#getPath
   */
  public String getPath() { //throws RepositoryException {
    return this.path;
  }

  /**
   * @see javax.jcr.observation.Event#getUserId
   */
  public String getUserID() {
    return this.userId;
  }

  //public String dump() {
  //  return "EventImpl: " + type + " " + path + " " + userId;
  //}

}
