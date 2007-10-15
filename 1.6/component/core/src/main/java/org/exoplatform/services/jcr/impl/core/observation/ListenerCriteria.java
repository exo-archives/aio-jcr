/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.impl.core.observation;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.core.SessionImpl;


/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ListenerCriteria.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class ListenerCriteria {

  private int eventTypes;

  private String absPath;
  
  private boolean deep;

  private String[] identifier;

  private String[] nodeTypeName;

  private boolean noLocal;
  
  private String sessionId;

  public ListenerCriteria(int eventTypes, String absPath, boolean isDeep,
      String[] identifier, String[] nodeTypeName, boolean noLocal, String sessionId) throws RepositoryException {
    this.eventTypes = eventTypes;
    this.absPath = absPath;
    this.deep = isDeep;
    this.identifier = identifier;
    this.nodeTypeName = nodeTypeName;
    this.noLocal = noLocal;
    this.sessionId = sessionId;
  }

  public int getEventTypes() {
    return this.eventTypes;
  }

  public String getAbsPath() {
    return this.absPath;
  }

  public boolean isDeep() {
    return deep;
  }

  public String[] getIdentifier() {
    return this.identifier;
  }

  public String[] getNodeTypeName() {
    return this.nodeTypeName;
  }

  public boolean getNoLocal() {
    return this.noLocal;
  }

  public String getSessionId() {
    return this.sessionId;
  }
  
}