/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.jcr.AccessDeniedException;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.Decoder;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.JCRPath.PathElement;
import org.exoplatform.services.jcr.impl.core.itemfilters.ItemFilter;
import org.exoplatform.services.jcr.impl.core.itemfilters.NamePatternFilter;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: SysNodeImporter.java 13463 2007-03-16 09:17:29Z geaz $
 */

class SysNodeImporter extends ImporterBase {

  protected Log                    log = ExoLogger.getLogger("jcr.SysNodeImporter");

  private Stack<NodeInfo>          tree;

  private String                   curPropName;

  private int                      curPropType;

  private List<DecodedValue>       curPropValues;

  private List<NodeInfo>           nodeInfos;

  private NodeData                 parent;

  private InternalQName            primaryTypeName;

  private List<ParsedPropertyInfo> propsParsed;

  private List<ExtendedNodeType>   nodeTypes;

  private InternalQName[]          mixinTypeNames;
  

  SysNodeImporter(NodeImpl parent, int uuidBehavior) {
    super(parent, uuidBehavior);
    this.parent = (NodeData) parent.getData();
    this.tree = new Stack<NodeInfo>();
    this.nodeInfos = new ArrayList<NodeInfo>();
    this.curPropValues = new ArrayList<DecodedValue>();
  }

  private void buildNode() throws RepositoryException {

    // build node from parsed infos
    NodeData node = null;

    Map<String, NodeData> parents = new LinkedHashMap<String, NodeData>();

    for (int i = 0; i < nodeInfos.size(); i++) {
      NodeInfo info = nodeInfos.get(i);

      mixinTypeNames = null;
      nodeTypes = new ArrayList<ExtendedNodeType>();
      propsParsed = new ArrayList<ParsedPropertyInfo>();
      primaryTypeName = null;

      NodeData parentNode = null;
      String relPathStr = info.getRelPath();
      String uuid = null;
      uuid = traverseNodeInfo(relPathStr, info);

      // check UUID Behavior of the import

      boolean hasMixReferenceable = isReferenceable(nodeTypes);
      if (hasMixReferenceable) {
        uuid = validateUuidCollision(uuid);
        if (uuid == null) {
          throw new RepositoryException("Ipossible state");
        }
      }

      // serach for parent of this rel path
      int lastPathElem = relPathStr.lastIndexOf("/");
      if (lastPathElem > 0) {
        String relPathParentStr = relPathStr.substring(0, lastPathElem);
        // in case of empty last elem of rel path - JCR will throw an exception
        // below
        relPathStr = lastPathElem < relPathStr.length() - 1 ? relPathStr
            .substring(lastPathElem + 1) : "";
        NodeData pathParent = parents.get(relPathParentStr);
        parentNode = pathParent != null ? pathParent : parent;
      } else {
        parentNode = parent;
      }

      // build current path of the imported node
      JCRPath path = locationFactory.createJCRPath(locationFactory.createJCRPath(parentNode
          .getQPath()), relPathStr);

      validatePath(path, parentNode, relPathStr);

      if (mixinTypeNames == null)
        mixinTypeNames = new InternalQName[0];

      InternalQName jcrName = path.getInternalPath().getName();
      int nodeIndex = getNodeIndex(parentNode, jcrName);

      NodeData newNodeData = TransientNodeData.createNodeData(parentNode,
          jcrName,
          primaryTypeName,
          nodeIndex);

      ((TransientNodeData) newNodeData).setMixinTypeNames(mixinTypeNames);

      if (hasMixReferenceable)
        ((TransientNodeData) newNodeData).setIdentifier(uuid);

      itemStatesList.add(new ItemState(newNodeData, ItemState.ADDED, true, parentNode.getQPath()));

      if (log.isDebugEnabled())
        log.debug("node: " + newNodeData.getQPath().getAsString() + ", " + path.getIndex() + ", "
            + newNodeData.getIdentifier() + ", " + primaryTypeName.getAsString() + ", "
            + (mixinTypeNames.length > 0 ? mixinTypeNames[0].getAsString() + "..." : ""));

      for (ParsedPropertyInfo prop : propsParsed) {
        List<ValueData> vDataList = prop.getValues();
        if (log.isDebugEnabled()) {

          String vals = "";

          for (ValueData vdata : vDataList) {
            try {
              vals += new String(vdata.getAsByteArray())
                  + (vDataList.lastIndexOf(vdata) != vDataList.size() - 1 ? "," : "");

            } catch (IOException e) {
              log.error("Debug eror: ", e);
            }
            log.debug("prop(2): " + prop.getName().getAsString() + ", [" + vals + "], "
                + ExtendedPropertyType.nameFromValue(prop.getType()));

          }
        }

        PropertyData newProperty = null;
        if (prop.getName().equals(Constants.JCR_UUID) && hasMixReferenceable) {
          newProperty = TransientPropertyData.createPropertyData(newNodeData, prop.getName(), prop
              .getType(), false, new TransientValueData(newNodeData.getIdentifier()));

        } else {
          // determinating is property multivalue;
          boolean isMultivalue = true;
          PropertyDefinitions defs = ntManager.findPropertyDefinitions(prop.getName(),
              primaryTypeName,
              mixinTypeNames);

          if (vDataList.size() == 1) {
            // there is single-value defeniton
            if (defs.getDefinition(false) != null)
              isMultivalue = false;
          } else {
            if (defs.getDefinition(true) == null && defs.getDefinition(false) != null) {
              throw new ValueFormatException("Can not assign multiple-values Value to a single-valued property "
                  + prop.getName());
            }
          }
          log.debug("Import " + prop.getName() + " size=" + vDataList.size() + " isMultivalue="
              + isMultivalue);
          newProperty = TransientPropertyData.createPropertyData(newNodeData, prop.getName(), prop
              .getType(), isMultivalue, vDataList);

        }

        itemStatesList
            .add(new ItemState(newProperty, ItemState.ADDED, true, newNodeData.getQPath()));

      }

      parents.put(info.getRelPath(), newNodeData); // add one new parent

      if (i == 0) {
        node = newNodeData;
      }
    }
  }

  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
    if (qName.equals("sv:node")) {
      try {
        String name = "";
        try {
          name = atts.getValue("sv:name");
        } catch (IncompatibleClassChangeError e) {

          e.printStackTrace();
        }
        String relPath;
        if (!tree.isEmpty())
          relPath = (tree.peek()).getRelPath() + "/" + name;
        else
          relPath = name;

        NodeInfo info = new NodeInfo(relPath);
        tree.push(info);
        nodeInfos.add(info);
      } catch (Exception e) {

        throw new SAXException(e.getMessage(), e);
      }
    } else if (qName.equals("sv:property")) {
      // TODO manage UUID
      try {
        curPropType = ExtendedPropertyType.valueFromName(atts.getValue("sv:type"));
        curPropName = atts.getValue("sv:name");
        curPropValues = new ArrayList<DecodedValue>();
      } catch (Exception e) {
        throw new SAXException(e.getMessage(), e);
      }
    } else if (qName.equals("sv:value")) {
      DecodedValue curPropValue = new DecodedValue();
      curPropValues.add(curPropValue);
    } else {
      throw new SAXException("'" + qName
          + "' is not allowed. Only sv:node, sv:property and sv:value are allowed");
    }
  }

  public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    if (qName.equals("sv:node")) {
      tree.pop();
    } else if (qName.equals("sv:property")) {
      tree.peek().addProperty(curPropName, curPropValues);
    } else if (qName.equals("sv:value")) {
      // no thing to do
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    // property values
    if (curPropValues.size() > 0) {
      DecodedValue curPropValue = curPropValues.get(curPropValues.size() - 1);
      if (curPropType == PropertyType.BINARY) {

        try {

          curPropValue.getBinaryDecoder().write(ch, start, length);
        } catch (IOException e) {
          throw new SAXException(e);
        }
      } else {
        curPropValue.getStringBuffer().append(ch, start, length);
      }
    } else {
      // wrong XML, no sv:value visited before
      log
          .warn("Wrong XML content. Element 'sv:value' expected, but SAX event 'characters' occured. characters:["
              + new String(ch) + "]");
    }
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startDocument() throws SAXException {
    nodeInfos.clear();
  }

  public void endDocument() throws SAXException {
    // so, build the node parsed before
    // and put it in the session
    try {
      buildNode();
    } catch (Exception e) {
      throw new SAXException("Error create node from System View document. Exception: "
          + e.getMessage(), e);
    }
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

  public NodeIterator getNodes(NodeData parent, String namePattern) throws RepositoryException {
    List<NodeImpl> childNodes = session.getTransientNodesManager().getChildNodes(parent, true);
    ItemFilter filter = new NamePatternFilter(namePattern);
    ArrayList<NodeData> list = new ArrayList<NodeData>();

    for (NodeImpl item : childNodes) {
      if (filter.accept(item))
        list.add((NodeData) item.getData());
    }
    return new EntityCollection(list);
  }

  private String traverseNodeInfo(String path, NodeInfo info) throws PathNotFoundException,
      RepositoryException {

      String uuid = null;
      List<PropertyInfo> props = info.getProperties();

      for (PropertyInfo prop : props) {
        InternalQName propName = locationFactory.parseJCRName(prop.getName()).getInternalName();
        List<DecodedValue> valueList = prop.getValues();

        if (propName.equals(Constants.JCR_PRIMARYTYPE)) {
          if (valueList.size() > 0) {
            primaryTypeName = locationFactory.parseJCRName(new String(valueList.get(0).toString()))
                .getInternalName();
            nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(primaryTypeName));
          } else
            log.warn("Imported property " + path + "/jcr:primaryType has empty value");
        } else if (propName.equals(Constants.JCR_MIXINTYPES)) {
          if (valueList.size() > 0) {
            mixinTypeNames = new InternalQName[valueList.size()];
          } else
            log.warn("Imported property " + path + "/jcr:mixinTypes has empty value(s)");
        } else if (propName.equals(Constants.JCR_UUID)) {
          if (valueList.size() > 0)
            uuid = new String(valueList.get(0).toString());
          else
            log.warn("Imported property " + path + "/jcr:uuid has empty value");
        }

        List<ValueData> values = new ArrayList<ValueData>(valueList.size());// new
        String valStr = "";
        for (int k = 0; k < valueList.size(); k++) {

          if (prop.getType() == PropertyType.BINARY) {
            try {
              InputStream vStream = valueList.get(k).getInputStream();

              TransientValueData binaryValue = new TransientValueData(0,
                  null, // bytes
                  vStream,
                  null, // spoolFile
                  session.getValueFactory().getFileCleaner(),
                  session.getValueFactory().getMaxBufferSize(),
                  session.getValueFactory().getTempDirectory(),
                  true);
              
              // Call to spool file into tmp
              binaryValue.getAsStream();
              vStream.close();
              valueList.get(k).remove();
              values.add(binaryValue);

            } catch (IOException e) {
              throw new RepositoryException(e);
            }

          } else {
            String val = new String(valueList.get(k).toString());
            values.add(((BaseValue) session.getValueFactory().createValue(val, prop.getType()))
                .getInternalData());
            if (propName.equals(Constants.JCR_MIXINTYPES)) {
              mixinTypeNames[k] = locationFactory.parseJCRName(val).getInternalName();
              nodeTypes.add((ExtendedNodeType) ntManager.getNodeType(mixinTypeNames[k]));
            }
            valStr += val + " ";
          }
        }

        if (log.isDebugEnabled())
          log.debug("prop(1): " + prop.getName() + ", [" + valStr.trim() + "], "
              + ExtendedPropertyType.nameFromValue(prop.getType()));

        propsParsed.add(new ParsedPropertyInfo(propName, prop.getType(), values));
      }
      return uuid;
  }

  private void validatePath(JCRPath path, NodeData parentNode, String relPathStr) {
    int depthIndex = path.getDepth() - parentNode.getQPath().getDepth();
    PathElement[] relPathElems = path.getRelPath(depthIndex);
    if (log.isDebugEnabled())
      log.debug("BUILD NODE, PREPARE: '" + parentNode.getQPath().getAsString() + "' + '"
          + relPathStr + "', check from depth: " + depthIndex);
    for (int depth = 0; depth < relPathElems.length; depth++) {
      PathElement pathElement = relPathElems[depth];
      if (log.isDebugEnabled())
        log.debug("BUILD NODE, NODE: '" + pathElement.getAsString(true) + "', depth: " + depth);
      try {

        NodeIterator snsNodes = getNodes(parentNode, pathElement.getAsString());
        if (depth < relPathElems.length - 1) {
          relPathElems[depth] = pathElement = pathElement.clone((int) snsNodes.getSize());

          try {
            parentNode = (NodeData) session.getTransientNodesManager().getItemData(parentNode,
                new QPathEntry(pathElement.getNamespace(), pathElement.getName(), pathElement
                    .getIndex()));

            if (log.isDebugEnabled())
              log.debug("BUILD NODE, <<< NEW ANCESTOR for RELATIVE path >>> : '"
                  + parentNode.getQPath().getAsString() + "', depth: " + depth + ", path: '"
                  + path.getAsString(true) + "'");
          } catch (PathNotFoundException e) {
            log.warn("Next parent not found: '" + parentNode.getQPath().getAsString()
                + "', depth: " + depth + ", relPath: '" + path.getAsString(true) + "'");
          }
        } else if (depth == relPathElems.length - 1) {
          relPathElems[depth] = pathElement = pathElement.clone((int) snsNodes.getSize() + 1);
        }
        if (log.isDebugEnabled())
          log.debug("BUILD NODE, FIX ANCESTOR: '" + parentNode.getQPath().getAsString()
              + "', depth: " + depth + ", path: '" + path.getAsString(true) + "'");
      } catch (PathNotFoundException e) {
        log.warn("Node not found: '" + parentNode.getQPath().getAsString() + "', depth: " + depth
            + ", relPath: '" + path.getAsString(true) + "'");
      } catch (RepositoryException e) {
        log.warn("Error of same-name-sibling nodes processing: " + e.getMessage() + ". Node: "
            + parentNode.getQPath().getAsString());
        // if exception will be thrown a node will be created at relPath
      }
    }
  }

  private String validateUuidCollision(String uuid) throws AccessDeniedException,
      RepositoryException {
    String retUuid = uuid != null ? new String(uuid) : null;
    try {

      ItemDataRemoveVisitor visitor = null;
      NodeImpl sameUuidNode = (NodeImpl) session.getNodeByUUID(uuid);
      List<ItemState> removedStates = null;
      switch (uuidBehavior) {
      case ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW:
        // Incoming referenceable nodes are assigned newly created UUIDs
        // upon addition to the workspace. As a result UUID collisions
        // never occur.

        // reset UUID and it will be autocreated in session
        retUuid = IdGenerator.generate();
        break;
      case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING:
        // If an incoming referenceable node has the same UUID as a node
        // already existing in the workspace then the already existing
        // node (and its subtree) is removed from wherever it may be in
        // the workspace before the incoming node is added. Note that this
        // can result in nodes �disappearing� from locations in the
        // workspace that are remote from the location to which the
        // incoming subtree is being written.

        visitor = new ItemDataRemoveVisitor(session.getTransientNodesManager());
        sameUuidNode.getData().accept(visitor);
        removedStates = visitor.getRemovedStates();
        itemStatesList.addAll(removedStates);

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
        parent = (NodeData) ((NodeImpl) sameUuidNode.getParent()).getData();

        visitor = new ItemDataRemoveVisitor(session.getTransientNodesManager());
        sameUuidNode.getData().accept(visitor);
        removedStates = visitor.getRemovedStates();
        itemStatesList.addAll(removedStates);

        sameUuidNode = null;

        break;
      case ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW:
        // If an incoming referenceable node has the same UUID as a node
        // already existing in the workspace then a SAXException is thrown
        // by the ContentHandler during deserialization.
        throw new ItemExistsException("An incoming referenceable node has the same UUID as a node already existing in the workspace!");
      default:
      }
    } catch (ItemNotFoundException e) {
      // node not found, it's ok - willing create one new
    }
    return retUuid;
  }

  private class NodeInfo {
    private String             relPath;

    private List<PropertyInfo> properties = new ArrayList<PropertyInfo>();

    public NodeInfo(String relPath) {
      this.relPath = relPath;
    }

    public void addProperty(String name, List<DecodedValue> strValues) {
      PropertyInfo prop = new PropertyInfo(name, curPropType, strValues);
      properties.add(prop);
    }

    public List<PropertyInfo> getProperties() {
      return properties;
    }

    public String getRelPath() {
      return relPath;
    }

  }

  private class ParsedPropertyInfo {

    private final InternalQName   name;

    private final int             type;

    private final List<ValueData> values;

    public ParsedPropertyInfo(InternalQName name, int type, List<ValueData> values) {
      super();
      this.name = name;
      this.type = type;
      this.values = values;
    }

    public InternalQName getName() {
      return name;
    }

    public int getType() {
      return type;
    }

    public List<ValueData> getValues() {
      return values;
    }
  }

  private class PropertyInfo {

    private String             name;

    private int                type;

    private List<DecodedValue> values;

    public PropertyInfo(String name, int type, List<DecodedValue> values) {
      this.name = name;
      this.type = type;
      this.values = values;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
      return name;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
      return type;
    }

    /**
     * @return Returns the values.
     */
    public List<DecodedValue> getValues() {
      return values;
    }
  }

  /**
   * Temporary class for swapping values and decode binary values during import.
   * 
   * @author ksm
   */
  private class DecodedValue {
    private final static int DEFAULT_BUFFER_SIZE = 4096;

    private StringBuffer     stringBuffer;

    private OutputStream     bos;

    private BufferedDecoder  decoder;

    public DecodedValue() {
      super();
    }

    /**
     * @return Base64 decoder. It is write decoded incoming data into the
     *         temporary file
     * @throws IOException
     */
    public Decoder getBinaryDecoder() throws IOException {
      if (decoder == null) {
        decoder = new BufferedDecoder();

      }
      return decoder;
    }

    @Override
    public String toString() {
      if (stringBuffer == null)
        return decoder.toString();
      return stringBuffer.toString();
    }

    /**
     * @return InputStream from decoded file
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
      if (decoder == null)
        return new ByteArrayInputStream(new byte[0]);
      return decoder.getInputStream();
    }

    /**
     * Removes all temporary variables and files
     * 
     * @throws IOException
     */
    public void remove() throws IOException {

      if (decoder != null) {
        decoder.remove();
        decoder = null;
      }
    }

    public StringBuffer getStringBuffer() {
      if (stringBuffer == null)
        stringBuffer = new StringBuffer();

      return stringBuffer;
    }
  }
  
  private class BufferedDecoder extends Base64.Decoder {
    private final static int DEFAULT_BUFFER_SIZE = 4096;

    //
    private final int        BUFFER_SIZE;

    private OutputStream     out;

    private File             fileBuffer;

    public BufferedDecoder(int bufferSize) {
      super(bufferSize);
      BUFFER_SIZE = bufferSize;
      out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
    }

    public BufferedDecoder() {
      super(DEFAULT_BUFFER_SIZE);
      BUFFER_SIZE = DEFAULT_BUFFER_SIZE;
      out = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
    }

    @Override
    protected void writeBuffer(byte[] buffer, int start, int length) throws IOException {
      if (out instanceof ByteArrayOutputStream) {
        if (((ByteArrayOutputStream) out).size() + length > BUFFER_SIZE)
          swapBuffers();
      }
      out.write(buffer, start, length);
    }

    private void swapBuffers() throws IOException {
      byte[] data = ((ByteArrayOutputStream) out).toByteArray();
      fileBuffer = File.createTempFile("decoderBuffer", ".tmp");
      fileBuffer.deleteOnExit();
      out = new BufferedOutputStream(new FileOutputStream(fileBuffer), BUFFER_SIZE);
      out.write(data);
    }
    
    public String toString() {
      if (out instanceof ByteArrayOutputStream)
        return ((ByteArrayOutputStream) out).toString();
      else if (out instanceof BufferedOutputStream) {
        try {
          out.close();
          BufferedInputStream is = new BufferedInputStream(new FileInputStream(fileBuffer));
          StringBuffer stringBuffer = new StringBuffer((int) fileBuffer.length());
          StringBuffer fileData = new StringBuffer(1000);

          byte[] buf = new byte[BUFFER_SIZE];
          int numRead = 0;
          while ((numRead = is.read(buf)) != -1) {

            fileData.append(new String(buf, 0, numRead));

          }
          is.close();
          return fileData.toString();
        } catch (IOException e) {
          return null;
        }

      } else {
        return null;
      }
    }

    public InputStream getInputStream() throws IOException {
      flush();
      if (out instanceof ByteArrayOutputStream){
        return new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
      }
      else if (out instanceof BufferedOutputStream) {
        
        out.close();
        return new BufferedInputStream(new FileInputStream(fileBuffer));
      } else {
        throw new IOException("unexpected change of buffer");
      }
    }

    public void remove() throws IOException {
      if (fileBuffer != null && fileBuffer.exists()) {
        if (!fileBuffer.delete())
          throw new IOException("Cannot remove file " + fileBuffer.getAbsolutePath()
              + " Close all streams.");
      }
    }

  }
}
