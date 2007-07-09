/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.tools.tree.generator;

import java.util.HashSet;
import java.util.Random;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.tools.tree.NameTraversingVisitor;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class WeightNodeGenerator implements NodeGenerator {
  private int maxDepth;

  private int maxWidth;

  public WeightNodeGenerator(int maxDepth, int maxWidth) {
    super();
    this.maxDepth = maxDepth;
    this.maxWidth = maxWidth;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public int getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  private void addNodes(Node parentNode, int level) throws ItemExistsException,
      PathNotFoundException,
      VersionException,
      ConstraintViolationException,
      LockException,
      RepositoryException {
    if (level >= maxDepth)
      return;
    int maxNodesOnLevel = (int) Math.round((new Double(maxWidth) / new Double(maxDepth)) * level);
    maxNodesOnLevel = maxNodesOnLevel != 0 ? maxNodesOnLevel : 1;
    Random random = new Random();
    for (int i = 0; i < maxNodesOnLevel; i++) {
      Node node = parentNode.addNode("node_level_" + level + "_number_" + i + "_"
          + random.nextInt(Integer.MAX_VALUE));
      addNodes(node, level + 1);
    }
  }

  public void genereteTree(Node root) throws ItemExistsException,
      PathNotFoundException,
      VersionException,
      ConstraintViolationException,
      LockException,
      RepositoryException {
    addNodes(root, 1);
  }

}
