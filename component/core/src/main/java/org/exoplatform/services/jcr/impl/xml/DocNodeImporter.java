/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.xml;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.ISO9075;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: DocNodeImporter.java 12849 2007-02-16 14:20:47Z ksm $
 */

class DocNodeImporter extends ImporterBase {

  private static Log            log       = ExoLogger.getLogger("jcr.DocNodeImporter");

  private Stack<NodeData>       tree;

  private TransientPropertyData XmlCharactersProperty;

  private String                XmlCharactersPropertyValue;

  private boolean               saveOnEnd = false;

  private String                primaryNodeType;

  public DocNodeImporter(NodeImpl parent, int uuidBehavior) throws RepositoryException {
    super(parent, uuidBehavior);
    this.tree = new Stack<NodeData>();
    this.XmlCharactersProperty = null;
    this.XmlCharactersPropertyValue = null;
    tree.push((NodeData) parent.getData());

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.NodeImporter#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

    String nodeName = ISO9075.decode(qName);
    primaryNodeType = "nt:unstructured";
    if ("jcr:root".equals(nodeName))
      nodeName = "";

    List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();

    HashMap<String, String> props = new HashMap<String, String>();
    props.put("jcr:primaryType", Constants.NT_UNSTRUCTURED.getAsString());
    try {
      nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(primaryNodeType));
    } catch (RepositoryException e) {
      throw new SAXException("impossible state");

    }

    List<InternalQName> mixinNodeTypes = new ArrayList<InternalQName>();

    parseAttr(nodeName, atts, nodeTypes, mixinNodeTypes, props);

    NodeData nodeData = null;

    try {
      boolean isMixReferenceable = isReferenceable(nodeTypes);
      String identifier = validateIdentifierCollision(nodeName,
          isMixReferenceable,
          nodeTypes,
          props);
      if (identifier == null) {
        identifier = IdGenerator.generate();
      }

      InternalQName jcrName = locationFactory.parseJCRName(nodeName).getInternalName();
      int nodeIndex = getNodeIndex(parent(), jcrName);

      nodeData = TransientNodeData.createNodeData(parent(), jcrName, locationFactory
          .parseJCRName(primaryNodeType).getInternalName(), nodeIndex);

      ((TransientNodeData) nodeData).setMixinTypeNames(mixinNodeTypes
          .toArray(new InternalQName[mixinNodeTypes.size()]));
      // newNode.setACL(node.getACL());
      ((TransientNodeData) nodeData).setIdentifier(identifier);

      itemStatesList.add(new ItemState(nodeData, ItemState.ADDED, true, parent().getQPath()));
      tree.push(nodeData);

      if (log.isDebugEnabled())
        log.debug("Node " + ": " + nodeData.getQPath().getAsString());

      Iterator<String> keys = props.keySet().iterator();

      PropertyData newProperty;
      while (keys.hasNext()) {
        newProperty = null;

        String key = keys.next();

        if (key.equals("jcr:primaryType")) {
          if (log.isDebugEnabled())
            log.debug("Property NAME: " + key + "=" + props.get(key));
          newProperty = TransientPropertyData.createPropertyData(parent(),
              Constants.JCR_PRIMARYTYPE,
              PropertyType.NAME,
              false,
              new TransientValueData(props.get(key)));

        } else if (key.equals("jcr:mixinTypes")) {

          List<ValueData> valuesData = new ArrayList<ValueData>(mixinNodeTypes.size());

          for (InternalQName mixinQname : mixinNodeTypes) {
            valuesData.add(new TransientValueData(mixinQname));
          }

          newProperty = TransientPropertyData.createPropertyData(parent(), locationFactory
              .parseJCRName(key).getInternalName(), PropertyType.NAME, false, valuesData);

        } else if (isMixReferenceable && key.equals("jcr:uuid")) {
          Value value = session.getValueFactory().createValue(nodeData.getIdentifier(),
              PropertyType.STRING);
          if (log.isDebugEnabled())
            log.debug("Property STRING: " + key + "=" + value.getString());

          newProperty = TransientPropertyData.createPropertyData(parent(),
              Constants.JCR_UUID,
              PropertyType.STRING,
              false,
              new TransientValueData(identifier));

        } else {
          PropertyDefinition pDef = getPropertyDefinition(key, nodeTypes);
          String pb = props.get(key);
          if (pDef.getRequiredType() == PropertyType.BINARY) {
            byte[] decoded = Base64.decode(pb);
            if (log.isDebugEnabled()) {

              if (pb.length() > 512)
                pb = pb.substring(0, 512);
              log.debug("Property BINARY: " + key + "="
                  + (pb.length() > 0 ? new String(decoded) : "[empty data]"));
            }

            newProperty = TransientPropertyData.createPropertyData(parent(),
                locationFactory.parseJCRName(key).getInternalName(),
                PropertyType.BINARY,
                false,
                new TransientValueData(new ByteArrayInputStream(decoded)));

          } else {
            int pType = pDef.getRequiredType() > 0 ? pDef.getRequiredType() : PropertyType.STRING;
            Value value = session.getValueFactory().createValue(StringConverter
                .denormalizeString(props.get(key)),
                pType);
            if (log.isDebugEnabled()) {
              String valueAsString = null;
              try {
                valueAsString = value.getString();
              } catch (Exception e) {
                log.error("Can't present value as string. " + e.getMessage());
                valueAsString = "[Can't present value as string]";
              }
              log.debug("Property " + PropertyType.nameFromValue(pType) + ": " + key + "="
                  + valueAsString);
            }
            newProperty = TransientPropertyData.createPropertyData(parent(), locationFactory
                .parseJCRName(key).getInternalName(), pType, false, ((BaseValue) value)
                .getInternalData());

          }
        }
        itemStatesList.add(new ItemState(newProperty, ItemState.ADDED, true, parent().getQPath()));
      }
    } catch (Exception e) {
      log.error("Error in import: " + e.getMessage());
      throw new SAXException(e.getMessage(), e);
    }
  }

  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    tree.pop();
    XmlCharactersProperty = null;
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    StringBuilder text = new StringBuilder();
    text.append(ch, start, length);
    if (log.isDebugEnabled()) {
      log.debug("Property:xmltext=" + text + " Parent=" + parent().getQPath().getAsString());
    }
    if (XmlCharactersProperty != null) {
      XmlCharactersPropertyValue += text.toString();
      XmlCharactersProperty.setValue(new TransientValueData(XmlCharactersPropertyValue));
    } else {
      NodeData nodeData = TransientNodeData.createNodeData(parent(),
          Constants.JCR_XMLTEXT,
          Constants.NT_UNSTRUCTURED);
      itemStatesList.add(new ItemState(nodeData, ItemState.ADDED, true, parent().getQPath()));

      TransientPropertyData newProperty = TransientPropertyData.createPropertyData(nodeData,
          Constants.JCR_PRIMARYTYPE,
          PropertyType.NAME,
          false,
          new TransientValueData(Constants.NT_UNSTRUCTURED));
      itemStatesList.add(new ItemState(newProperty, ItemState.ADDED, true, nodeData.getQPath()));

      newProperty = TransientPropertyData.createPropertyData(nodeData,
          Constants.JCR_XMLCHARACTERS,
          PropertyType.STRING,
          false,
          new TransientValueData(text.toString()));
      itemStatesList.add(new ItemState(newProperty, ItemState.ADDED, true, nodeData.getQPath()));
      XmlCharactersProperty = newProperty;
      XmlCharactersPropertyValue = text.toString();
    }
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  private NodeData parent() {
    return tree.peek();
  }

  public boolean isSaveOnEnd() {
    return saveOnEnd;
  }

  public void setSaveOnEnd(boolean saveOnEnd) {
    this.saveOnEnd = saveOnEnd;
  }

  private void parseAttr(String nodeName,
      Attributes atts,
      List<ExtendedNodeType> nodeTypes,
      List<InternalQName> mixinNodeTypes,
      HashMap<String, String> props) throws SAXException {
    if (atts != null) {
      try {
        for (int i = 0; i < atts.getLength(); i++) {
          String propName;
          String attrQName = atts.getQName(i);
          String attValue = atts.getValue(i);
          if (!"".equals(attrQName))
            propName = ISO9075.decode(attrQName);
          else
            propName = atts.getLocalName(i);

          if (propName.equals("jcr:primaryType")) {
            primaryNodeType = StringConverter.denormalizeString(attValue);
            InternalQName ntName = locationFactory.parseJCRName(primaryNodeType).getInternalName();
            nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(ntName));
            props.put(propName, ntName.getAsString());
          } else if (propName.equals("jcr:mixinTypes")) {
            String[] amTypes = attValue.split(" ");
            for (int mi = 0; mi < amTypes.length; mi++) {
              amTypes[mi] = StringConverter.denormalizeString(amTypes[mi]);
              mixinNodeTypes.add(locationFactory.parseJCRName(amTypes[mi]).getInternalName());
              nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(amTypes[mi]));
            }
            // value will not be used anywhere; for key only
            props.put(propName, null);
          } else if (propName.equals("jcr:uuid")) {
            props.put(propName, attValue);
          } else {
            props.put(propName, attValue);
          }
        }
      } catch (RepositoryException e) {
        log.error("Error in node properties import, " + nodeName + ": " + e.getMessage(), e);
        throw new SAXException("Error in node properties import, " + nodeName + ": "
            + e.getMessage(), e);
      }
    }

  }

  private String validateIdentifierCollision(String nodeName,
      boolean hasMixReferenceable,
      List<ExtendedNodeType> nodeTypes,
      HashMap<String, String> props) throws SAXException, RepositoryException {

    String identifier = props.get("jcr:uuid");
    NodeData parentNodeData = parent();
    ItemDataRemoveVisitor visitor = null;
    List<ItemState> removedStates = null;
    if (hasMixReferenceable && identifier != null) {
      try {
        NodeImpl sameIdentifierNode = (NodeImpl) session.getNodeByUUID(identifier);
        switch (uuidBehavior) {
        case ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW:
          // Incoming referenceable nodes are assigned newly created UUIDs
          // upon addition to the workspace. As a result UUID collisions
          // never occur.

          // reset UUID and it will be autocreated in session
          identifier = null;
          break;
        case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING:
          // If an incoming referenceable node has the same UUID as a node
          // already existing in the workspace then the already existing
          // node (and its subtree) is removed from wherever it may be in
          // the workspace before the incoming node is added. Note that this
          // can result in nodes �disappearing� from locations in the
          // workspace that are remote from the location to which the
          // incoming subtree is being written.

          NodeIterator samePatterns = sameIdentifierNode.getNodes(parentNodeData.getQPath()
              .getName().getName());
          if (samePatterns.hasNext()) {
            throw new ConstraintViolationException("A uuidBehavior is set to "
                + "IMPORT_UUID_COLLISION_REMOVE_EXISTING and an incoming node has the same "
                + "UUID as the node at parentAbsPath or one of its ancestors");
          }
          visitor = new ItemDataRemoveVisitor(session, true);
          sameIdentifierNode.getData().accept(visitor);
          removedStates = visitor.getRemovedStates();
          itemStatesList.addAll(removedStates);

          // sameUuidNode = null;
          break;
        case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING:
          // If an incoming referenceable node has the same UUID as a node
          // already existing in the workspace, then the already existing
          // node is replaced by the incoming node in the same position as
          // the existing node. Note that this may result in the incoming
          // subtree being disaggregated and �spread around� to different
          // locations in the workspace. In the most extreme case this
          // behavior may result in no node at all being added as child of
          // parentAbsPath. This will occur if the topmost element of the
          // incoming XML has the same UUID as an existing node elsewhere in
          // the workspace.

          // replace in same location
          parentNodeData = (NodeData) ((NodeImpl) sameIdentifierNode.getParent()).getData();
          visitor = new ItemDataRemoveVisitor(session, true);
          sameIdentifierNode.getData().accept(visitor);
          removedStates = visitor.getRemovedStates();
          itemStatesList.addAll(removedStates);
          // itemStatesList.add(ItemState.createDeletedState(sameUuidNode.getData()));
          tree.push(parentNodeData);
          // sameUuidNode = null;
          break;
        case ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW:
          // If an incoming referenceable node has the same UUID as a node
          // already existing in the workspace then a SAXException is thrown
          // by the ContentHandler during deserialization.
          throw new ItemExistsException("An incoming referenceable node has the same "
              + "UUID as a node already existing in the workspace!");
        default:
        }
      } catch (ItemNotFoundException e) {
        // node not found, it's ok - willing create one new
      }
    }
    return identifier;
  }

}
