/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.Comparator;

import org.exoplatform.services.jcr.datamodel.NodeData;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version
 */
public class NodeDataOrderComparator implements Comparator<NodeData> {

  public int compare(NodeData n1, NodeData n2) {
    return n1.getOrderNumber() - n2.getOrderNumber();
  }

}
