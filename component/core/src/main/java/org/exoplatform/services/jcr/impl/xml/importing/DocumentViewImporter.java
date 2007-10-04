/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
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
import org.exoplatform.services.jcr.impl.xml.ImportRespectingSemantics;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class DocumentViewImporter extends ImporterBase {
  private static Log            log = ExoLogger.getLogger("jcr.DocNodeImporter");

  private String                primaryNodeType;

  private Stack<NodeData>       tree;

  private TransientPropertyData xmlCharactersProperty;

  private String                XmlCharactersPropertyValue;

  public DocumentViewImporter(NodeImpl parent,
                              int uuidBehavior,
                              XmlSaveType saveType,
                              boolean respectPropertyDefinitionsConstraints) {
    super(parent, uuidBehavior, saveType, respectPropertyDefinitionsConstraints);
    this.tree = new Stack<NodeData>();
    this.xmlCharactersProperty = null;
    this.XmlCharactersPropertyValue = null;
    tree.push((NodeData) parent.getData());
  }

  public void characters(char[] ch, int start, int length) throws RepositoryException {
    StringBuilder text = new StringBuilder();
    text.append(ch, start, length);
    if (log.isDebugEnabled())
      log.debug("Property:xmltext=" + text + " Parent=" + parent().getQPath().getAsString());
    
    if (xmlCharactersProperty != null) {
      XmlCharactersPropertyValue += text.toString();
      xmlCharactersProperty.setValue(new TransientValueData(XmlCharactersPropertyValue));
    } else {
      TransientNodeData nodeData = TransientNodeData.createNodeData(parent(),
          Constants.JCR_XMLTEXT,
          Constants.NT_UNSTRUCTURED,
          getNodeIndex(parent(), Constants.JCR_XMLTEXT));
      nodeData.setOrderNumber(getNextChildOrderNum(parent()));

      itemStatesList.add(new ItemState(nodeData, ItemState.ADDED, true, parent().getQPath()));
      if (log.isDebugEnabled())
        log.debug("New node " + nodeData.getQPath().getAsString());

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
      xmlCharactersProperty = newProperty;
      XmlCharactersPropertyValue = text.toString();
    }

  }

  public void endElement(String uri, String localName, String qName) throws RepositoryException {
    tree.pop();
    xmlCharactersProperty = null;
  }

  public void startElement(String namespaceURI,
      String localName,
      String qName,
      Map<String, String> atts) throws RepositoryException {

    String nodeName = ISO9075.decode(qName);
    primaryNodeType = "nt:unstructured";
    if ("jcr:root".equals(nodeName)) {
      nodeName = "";
    }
    xmlCharactersProperty = null;
    List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();

    HashMap<InternalQName, String> props = new HashMap<InternalQName, String>();
    props.put(Constants.JCR_PRIMARYTYPE, Constants.NT_UNSTRUCTURED.getAsString());

    nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(primaryNodeType));

    List<InternalQName> mixinNodeTypes = new ArrayList<InternalQName>();

    parseAttr(nodeName, atts, nodeTypes, mixinNodeTypes, props);

    // NodeData nodeData = null;

    // try {
    boolean isMixReferenceable = isReferenceable(nodeTypes);
    String identifier = null;
    if (isMixReferenceable) {
      identifier = validateUuidCollision(props.get(Constants.JCR_UUID));
    }
    if (identifier == null) {
      identifier = IdGenerator.generate();
    }

    InternalQName jcrName = locationFactory.parseJCRName(nodeName).getInternalName();

    InternalQName primaryTypeName = locationFactory.parseJCRName(primaryNodeType).getInternalName();
    TransientNodeData nodeData = TransientNodeData.createNodeData(parent(),
        jcrName,
        primaryTypeName,
        getNodeIndex(parent(), jcrName));
    nodeData.setOrderNumber(getNextChildOrderNum(parent()));
    nodeData.setMixinTypeNames(mixinNodeTypes.toArray(new InternalQName[mixinNodeTypes.size()]));
    nodeData.setIdentifier(identifier);

    itemStatesList.add(new ItemState(nodeData, ItemState.ADDED, true, parent().getQPath()));
    tree.push(nodeData);

    if (log.isDebugEnabled()) {
      log.debug("Node " + ": " + nodeData.getQPath().getAsString());
    }

    Iterator<InternalQName> keys = props.keySet().iterator();

    PropertyData newProperty;
    while (keys.hasNext()) {
      newProperty = null;

      InternalQName key = keys.next();

      if (key.equals(Constants.JCR_PRIMARYTYPE)) {
        if (log.isDebugEnabled()) {
          log.debug("Property NAME: " + key + "=" + props.get(key));
        }
        newProperty = TransientPropertyData.createPropertyData(parent(),
            Constants.JCR_PRIMARYTYPE,
            PropertyType.NAME,
            false,
            new TransientValueData(props.get(key)));

      } else if (key.equals(Constants.JCR_MIXINTYPES)) {

        List<ValueData> valuesData = new ArrayList<ValueData>(mixinNodeTypes.size());

        for (InternalQName mixinQname : mixinNodeTypes) {
          valuesData.add(new TransientValueData(mixinQname));
        }

        newProperty = TransientPropertyData.createPropertyData(parent(),
            key,
            PropertyType.NAME,
            false,
            valuesData);

      } else if (isMixReferenceable && key.equals(Constants.JCR_UUID)) {
        Value value = session.getValueFactory().createValue(nodeData.getIdentifier(),
            PropertyType.STRING);
        if (log.isDebugEnabled()) {
          log.debug("Property STRING: " + key + "=" + value.getString());
        }

        newProperty = TransientPropertyData.createPropertyData(parent(),
            Constants.JCR_UUID,
            PropertyType.STRING,
            false,
            new TransientValueData(identifier));

      } else {
        PropertyDefinition pDef = getPropertyDefinition(key, nodeTypes);
        String pb = props.get(key);
        if (pDef.getRequiredType() == PropertyType.BINARY) {
          byte[] decoded;
          try {
            decoded = Base64.decode(pb);
          } catch (DecodingException e) {
            throw new RepositoryException(e);
          }
          if (log.isDebugEnabled()) {

            if (pb.length() > 512) {
              pb = pb.substring(0, 512);
            }
            log.debug("Property BINARY: " + key + "="
                + (pb.length() > 0 ? new String(decoded) : "[empty data]"));
          }

          newProperty = TransientPropertyData.createPropertyData(parent(),
              key,
              PropertyType.BINARY,
              false,
              new TransientValueData(new ByteArrayInputStream(decoded)));

        } else {
          StringTokenizer spaceTokenizer = new StringTokenizer(props.get(key));
          // Value[] values = new Value[spaceTokenizer.countTokens()];
          List<ValueData> values = new ArrayList<ValueData>();
          int pType = pDef.getRequiredType() > 0 ? pDef.getRequiredType() : PropertyType.STRING;
          InternalQName propName = key;
          if ("".equals(props.get(key))) {
            // Skip empty non string values
            if (pType != PropertyType.STRING) {
              continue;
            }
            Value value = session.getValueFactory().createValue(StringConverter
                .denormalizeString(props.get(key)),
                pType);
            values.add(((BaseValue) value).getInternalData());
          } else {
            while (spaceTokenizer.hasMoreTokens()) {
              String elem = spaceTokenizer.nextToken();

              Value value = session.getValueFactory().createValue(StringConverter
                  .denormalizeString(elem),
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
              values.add(((BaseValue) value).getInternalData());
            }
          }
          
          PropertyDefinitions defs;
          try {
            defs = ntManager.findPropertyDefinitions(propName,
                                                     primaryTypeName,
                                                     mixinNodeTypes.toArray(new InternalQName[mixinNodeTypes.size()]));
          } catch (RepositoryException e) {
            if (!respectPropertyDefinitionsConstraints) {
              log.warn(e.getLocalizedMessage());
              continue;
            }
            throw e;
          }
          boolean isMultivalue = true;
          
          // determinating is property multivalue;

          if (values.size() == 1) {
            // there is single-value defeniton
            if (defs.getDefinition(false) != null) {
              isMultivalue = false;
            }
          } else {
            if ((defs.getDefinition(true) == null) && (defs.getDefinition(false) != null)) {
              throw new ValueFormatException("Can not assign multiple-values Value to a single-valued property "
                  + propName.getAsString() + " node " + jcrName.getName());
            }
          }

          newProperty = TransientPropertyData.createPropertyData(parent(),
              propName,
              pType,
              isMultivalue,
              values);
        }
      }
      itemStatesList.add(new ItemState(newProperty, ItemState.ADDED, true, parent().getQPath()));
    }
    // } catch (Exception e) {
    // log.error("Error in import: " + e.getMessage());
    // e.printStackTrace();
    // // throw new SAXException(e.getMessage(), e);
    // }
  }

  private NodeData parent() {
    return tree.peek();
  }

  private void parseAttr(String nodeName,
      Map<String, String> atts,
      List<ExtendedNodeType> nodeTypes,
      List<InternalQName> mixinNodeTypes,
      HashMap<InternalQName, String> props) throws PathNotFoundException, RepositoryException {
    if (atts != null) {
      for (String key : atts.keySet()) {

        String attValue = atts.get(key);

        String propName = ISO9075.decode(key);
        InternalQName propInternalQName = locationFactory.parseJCRName(propName).getInternalName();

        if (Constants.JCR_PRIMARYTYPE.equals(propInternalQName)) {
          primaryNodeType = StringConverter.denormalizeString(attValue);
          InternalQName ntName = locationFactory.parseJCRName(primaryNodeType).getInternalName();
          nodeTypes.add(ntManager.getNodeType(ntName));
          props.put(propInternalQName, ntName.getAsString());
        } else if (Constants.JCR_MIXINTYPES.equals(propInternalQName)) {
          String[] amTypes = attValue.split(" ");
          for (int mi = 0; mi < amTypes.length; mi++) {
            amTypes[mi] = StringConverter.denormalizeString(amTypes[mi]);
            mixinNodeTypes.add(locationFactory.parseJCRName(amTypes[mi]).getInternalName());
            nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(amTypes[mi]));
          }
          // value will not be used anywhere; for key only
          props.put(propInternalQName, null);
        } else {
          props.put(propInternalQName, attValue);
        }
      }

    }

  }

  private String validateUuidCollision(String identifier) throws RepositoryException {

    // String identifier = props.get("jcr:uuid");
    NodeData parentNodeData = parent();
    ItemDataRemoveVisitor visitor = null;
    List<ItemState> removedStates = null;
    if (identifier != null) {
      try {
        NodeImpl sameIdentifierNode = session.getNodeByUUID(identifier);
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
