/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.common;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Wrapper for jcr node. The idea is to force application to use the node of particular NodeType
 * so the object's client could not change its type in modified method.
 * For example
 *   
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class NodeWrapper {
    
  private final Node node;

  protected NodeWrapper(final Node node) {
    this.node = node;
  }

  public final Node getNode() {
    return node;
  }
  
}
