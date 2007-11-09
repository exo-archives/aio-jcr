/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.tools.tree;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.impl.tools.tree.generator.NodeGenerator;
import org.exoplatform.services.jcr.impl.tools.tree.generator.PropertyGenerator;
import org.exoplatform.services.jcr.impl.tools.tree.generator.ValueGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TreeGenerator {
  protected static Log        log = ExoLogger.getLogger(TreeGenerator.class);

  private final Node          root;

  private final NodeGenerator nodegenerator;

  public TreeGenerator(Node root, NodeGenerator nodegenerator) {
    this.root = root;
    this.nodegenerator = nodegenerator;
  }

  public void genereteTree() throws RepositoryException {
    long startTime = System.currentTimeMillis();
    nodegenerator.genereteTree(root);
    root.save();
    log.info("Tree generete by " + (System.currentTimeMillis() - startTime) / 1000 + " sec");
  }

}
