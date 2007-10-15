/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.audit;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: AuditService.java 12164 2007-01-22 08:39:22Z geaz $
 */

public interface AuditService {
  public static final String AUDIT_STORAGE_ID = "00exo0jcr0audit0storage0id000000";
  public static final InternalQName EXO_AUDIT = new InternalQName(Constants.NS_EXO_URI, "audit");
  public static final InternalQName EXO_AUDITABLE = new InternalQName(Constants.NS_EXO_URI, "auditable");
  public static final InternalQName EXO_AUDITSTORAGE = new InternalQName(Constants.NS_EXO_URI, "auditStorage");
  public static final InternalQName EXO_AUDITRECORD = new InternalQName(Constants.NS_EXO_URI, "auditRecord");
  public static final InternalQName EXO_AUDITRECORD_USER = new InternalQName(Constants.NS_EXO_URI, "user");
  public static final InternalQName EXO_AUDITRECORD_CREATED = new InternalQName(Constants.NS_EXO_URI, "created");
  public static final InternalQName EXO_AUDITRECORD_EVENTTYPE = new InternalQName(Constants.NS_EXO_URI, "eventType");
  public static final InternalQName EXO_AUDITRECORD_PROPERTYNAME = new InternalQName(Constants.NS_EXO_URI, "propertyName");
  public static final InternalQName EXO_AUDITHISTORY = new InternalQName(Constants.NS_EXO_URI, "auditHistory");
  public static final InternalQName EXO_AUDITHISTORY_TARGETNODE = new InternalQName(Constants.NS_EXO_URI, "targetNode");
  public static final InternalQName EXO_AUDITHISTORY_LASTRECORD = new InternalQName(Constants.NS_EXO_URI, "lastRecord");
  
  
  /**
   * creates audit history
   * @param item
   * @throws RepositoryException
   */
  void createHistory(Node node) throws RepositoryException;

  /**
   * deletes audit history
   * @param item
   * @throws RepositoryException
   */
  void removeHistory(Node node) throws RepositoryException;

  /**
   * adds new audit record
   * @param item
   * @param eventType
   * @throws RepositoryException
   */
  void addRecord(Item item, int eventType) throws RepositoryException;
  
  
  /**
   * @param item
   * @return audit history of this item
   * @throws RepositoryException
   * @throws UnsupportedOperationException if item(parent) is not auditable
   */
  AuditHistory getHistory(Node node) throws RepositoryException, UnsupportedOperationException;
  
  /**
   * @param item
   * @return true if audit history for this item exists
   */
  boolean hasHistory(Node node);
}
