/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.tools.tree;

import java.util.HashSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NameTraversingVisitor extends ItemDataTraversingVisitor {
  private HashSet<QPath>   validNames       = new HashSet<QPath>();

  private HashSet<String>  validUuids       = new HashSet<String>();

  public  final static int SCOPE_NODES      = 1;

  public final static int SCOPE_PROPERTYES = 2;

  public final static int SCOPE_ALL        = SCOPE_NODES | SCOPE_PROPERTYES;

  private final int        scope;

  public NameTraversingVisitor(ItemDataConsumer dataManager, int scope) {
    super(dataManager);
    this.scope = scope;
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    if ((scope & SCOPE_PROPERTYES) != 0) {
      validNames.add(property.getQPath());
      validUuids.add(property.getIdentifier());
    }

  }

  private HashSet<QPath> getValidNames() {
    return validNames;
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    if ((scope & SCOPE_PROPERTYES) != 0) {
      validNames.add(node.getQPath());
      validUuids.add(node.getIdentifier());
    }

  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
    // TODO Auto-generated method stub

  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    // TODO Auto-generated method stub

  }

  public static QPath[] getValidNames(Node rootNode, int scope) throws RepositoryException {
    NameTraversingVisitor visitor = new NameTraversingVisitor(((SessionImpl) rootNode.getSession())
        .getTransientNodesManager(),scope);
    (((NodeImpl) rootNode).getData()).accept(visitor);
    HashSet<QPath> valNames = visitor.getValidNames();
    return valNames.toArray(new QPath[valNames.size()]);
  }

  public static String[] getValidUuids(Node rootNode, int scope) throws RepositoryException {
    NameTraversingVisitor visitor = new NameTraversingVisitor(((SessionImpl) rootNode.getSession())
        .getTransientNodesManager(),scope);
    (((NodeImpl) rootNode).getData()).accept(visitor);
    HashSet<String> valUuids = visitor.getValidUuids();
    return valUuids.toArray(new String[valUuids.size()]);

  }

  private HashSet<String> getValidUuids() {
    return validUuids;
  }

}
