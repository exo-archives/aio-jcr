/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.audit;

import java.util.Collections;
import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class AuditHistory {
  
  private final Node auditableNode;
  private final List <AuditRecord> auditRecords;
  
  public AuditHistory(final Node auditableNode, final List<AuditRecord> auditRecords) {
    this.auditableNode = auditableNode;
    this.auditRecords = auditRecords;
    Collections.sort(auditRecords);
  }

  public Node getAuditableNode() {
    return auditableNode;
  }

  public List<AuditRecord> getAuditRecords() {
    return auditRecords;
  }

}
