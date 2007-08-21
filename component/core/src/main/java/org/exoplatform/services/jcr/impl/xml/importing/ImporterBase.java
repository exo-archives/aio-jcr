/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeDefinitionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.PropertyDefinitionImpl;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;

/**
 * Created by The eXo Platform SARL Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 22.09.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ImporterBase.java 13421 2007-03-15 10:46:47Z geaz $
 */
abstract public class ImporterBase implements Importer {

  private final XmlSaveType           saveType;

  /**
   * The list of added item states
   */
  protected List<ItemState>           itemStatesList;

  protected LocationFactory           locationFactory;

  /**
   * The NodeTypeManager
   */
  protected final NodeTypeManagerImpl ntManager;

  protected final NodeImpl            parent;

  protected final SessionImpl         session;

  protected final int                 uuidBehavior;

  public ImporterBase(NodeImpl parent, int uuidBehavior, XmlSaveType saveType) {

    this.parent = parent;
    this.saveType = saveType;
    this.session = parent.getSession();
    this.ntManager = (NodeTypeManagerImpl) this.session.getRepository().getNodeTypeManager();
    this.locationFactory = session.getLocationFactory();
    this.uuidBehavior = uuidBehavior;
    this.itemStatesList = new ArrayList<ItemState>();
  }

  @Deprecated
  public List<ItemState> getItemStatesList() {
    return itemStatesList;
  }

  public int getNodeIndex(NodeData parent, InternalQName name) throws PathNotFoundException,
      IllegalPathException,
      RepositoryException {

    int newIndex = 1;

    NodeImpl parentNode = ((NodeImpl) session.getTransientNodesManager().getItemByIdentifier(parent
        .getIdentifier(),
        true));

    NodeDefinitionImpl nodedef = session.getWorkspace().getNodeTypeManager()
        .findNodeDefinition(name, parent.getPrimaryTypeName(), parent.getMixinTypeNames());

    NodeImpl sameNameNode = null;
    try {
      sameNameNode = (NodeImpl) session.getTransientNodesManager().getItem(parent,
          new QPathEntry(name, 0),
          true);
    } catch (PathNotFoundException e) {
      // Ok no same name node;
      return newIndex;
    }

    List<ItemState> transientAddChilds = getItemStatesList(parent,
        new QPathEntry(name, 0),
        ItemState.ADDED);
    List<ItemState> transientDeletedChilds = getItemStatesList(parent,
        new QPathEntry(name, 0),
        ItemState.DELETED);

    if (!nodedef.allowsSameNameSiblings()
        && ((sameNameNode != null) || (transientAddChilds.size() > 0))) {
      if ((sameNameNode != null) && (transientDeletedChilds.size() < 1)) {
        throw new ItemExistsException("The node  already exists in " + sameNameNode.getPath()
            + " and same name sibling is not allowed ");
      }
      if (transientAddChilds.size() > 0) {
        throw new ItemExistsException("The node  already exists in add state "
            + "  and same name sibling is not allowed ");

      }
    }

    newIndex += transientAddChilds.size();

    List<NodeData> existedChilds = session.getTransientNodesManager().getChildNodesData(parent);

    // Calculate SNS index for dest root
    for (NodeData child : existedChilds) {
      // skeep deleted items
      if (transientDeletedChilds.size() != 0) {
        continue;
      }

      if (child.getQPath().getName().equals(name)) {
        newIndex++; // next sibling index
      }

    }

    // searching
    return newIndex;
  }

  public XmlSaveType getSaveType() {
    return saveType;
  }

  public void registerNamespace(String prefix, String uri) {
    try {
      session.getWorkspace().getNamespaceRegistry().getPrefix(uri);
    } catch (NamespaceException e) {
      try {
        session.getWorkspace().getNamespaceRegistry().registerNamespace(prefix, uri);
      } catch (NamespaceException e1) {
        throw new RuntimeException(e1);
      } catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  public void save() throws RepositoryException {
    switch (saveType) {
    case SESSION:
      for (ItemState itemState : itemStatesList) {
        switch (itemState.getState()) {
        case ItemState.ADDED:
          session.getTransientNodesManager().update(itemState, true);
          break;
        case ItemState.DELETED:
          session.getTransientNodesManager().delete(itemState.getData());
        default:
          break;
        }

      }
      break;
    case WORKSPACE:
      PlainChangesLogImpl changesLog = new PlainChangesLogImpl(itemStatesList, session.getId());
      session.getTransientNodesManager().getTransactManager().save(changesLog);
      break;
    default:
      throw new IllegalStateException("Save type undefined");

    }

  }

  public void setItemStatesList(List<ItemState> itemStatesList) {
    this.itemStatesList = itemStatesList;
  }

  private List<ItemState> getItemStatesList(NodeData parentData, QPathEntry name, int state) {
    List<ItemState> states = new ArrayList<ItemState>();
    for (ItemState itemState : itemStatesList) {
      if (itemState.getData().getParentIdentifier().equals(parentData.getIdentifier())
          && itemState.getData().getQPath().getEntries()[itemState.getData().getQPath()
              .getEntries().length - 1].isSame(name)) {
        if ((state != 0) && (state != itemState.getState())) {
          continue;
        }
        states.add(itemState);
      }
    }
    return states;
  }

  protected ItemData getLocalItemData(NodeData parent, QPathEntry name) {
    ItemData item = null;
    List<ItemState> states = getItemStatesList(parent, name, 0);
    // get last state
    ItemState state = states.get(states.size() - 1);
    if (!state.isDeleted()) {
      item = state.getData();
    }
    return item;
  }

  protected PropertyDefinition getPropertyDefinition(InternalQName propertyName,
      List<ExtendedNodeType> nodeTypes) {
    PropertyDefinition pdResidual = null;
    for (ExtendedNodeType nt : nodeTypes) {
      PropertyDefinitions pds = nt.getPropertyDefinitions(propertyName);
      PropertyDefinition pd = pds.getAnyDefinition();
      if (pd != null) {
        if (((PropertyDefinitionImpl) pd).isResidualSet()) {
          pdResidual = pd;
        } else {
          return pd;
        }
      }
    }
    return pdResidual;
  }

  protected boolean isReferenceable(List<ExtendedNodeType> nodeTypes) {

    for (ExtendedNodeType nt : nodeTypes) {
      if (nt.isNodeType(Constants.MIX_REFERENCEABLE)) {
        return true;
      }
    }
    return false;
  }
}