/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.util;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;

/**
 * Created by The eXo Platform SARL
 * 15.05.2006
 * 
 * ItemData bulk reader (base class).
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ItemDataReader.java 12841 2007-02-16 08:58:38Z peterit $
 */
public abstract class ItemDataReader {

  protected DataManager dataManager;
  protected ValueFactoryImpl valueFactory = null;
  protected NodeData parent;
  
  public ItemDataReader(NodeData parent, DataManager dataManager, ValueFactoryImpl valueFactory) {
    this.dataManager = dataManager;
    this.parent = parent;
    this.valueFactory = valueFactory;
  }

  public NodeData getParentNode() {
    return parent;
  }
}
