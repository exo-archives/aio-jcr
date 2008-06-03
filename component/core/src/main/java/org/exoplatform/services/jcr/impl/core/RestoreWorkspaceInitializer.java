/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.jcr.NamespaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataPersister;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.JCRDateFormat;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Restores workspace from ready backupset. <br/> Should be configured with restore-path parameter. The path to a backup result
 * file.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: RestoreWorkspaceInitializer.java 15035 2008-06-02 08:44:24Z rainf0x $
 */

public class RestoreWorkspaceInitializer implements WorkspaceInitializer {

  public static final String          RESTORE_PATH_PARAMETER = "restore-path";

  protected static final Log          log                    = ExoLogger.getLogger("jcr.WorkspaceInitializer");

  protected final String              workspaceName;

  protected final DataManager         dataManager;

  private final NamespaceRegistryImpl namespaceRegistry;

  private final LocationFactory       locationFactory;

  private final NodeTypeManagerImpl   nodeTypeManager;

  private final ValueFactoryImpl      valueFactory;

  private final AccessManager         accessManager;

  protected String                    restorePath;

  protected class SVNodeData extends TransientNodeData {

    List<InternalQName> childNodes = new LinkedList<InternalQName>();

    SVNodeData(QPath path, String identifier, String parentIdentifier, int version, int orderNum) {
      super(path, identifier, version, null, null, orderNum, parentIdentifier, null);
    }

    void setPrimartTypeName(InternalQName primaryTypeName) {
      this.primaryTypeName = primaryTypeName;
    }

    /**
     * Add name of child node.
     * 
     * @return array of added node orderNumber and index
     */
    int[] addChildNode(InternalQName childName) {
      int orderNumber = childNodes.size();
      int index = 1;
      for (int i = 0; i < childNodes.size(); i++) {
        if (childName.equals(childNodes.get(i)))
          index++;
      }
      childNodes.add(childName);
      return new int[] { orderNumber, index };
    }
  }

  protected class SVPropertyData extends TransientPropertyData {

    SVPropertyData(QPath path, String identifier, int version, int type, String parentIdentifier, boolean multivalued) {
      super(path, identifier, version, type, parentIdentifier, multivalued);
      this.values = new ArrayList<ValueData>();
    }

    //    public boolean isMultiValued() {
    //      return values.size() > 1;
    //    }

    public void setMultiValued(boolean multiValued) {
      this.multiValued = multiValued;
    }

  }

  public RestoreWorkspaceInitializer(WorkspaceEntry config,
                            RepositoryEntry repConfig,
                            CacheableWorkspaceDataManager dataManager,
                            NamespaceRegistryImpl namespaceRegistry,
                            LocationFactory locationFactory,
                            NodeTypeManagerImpl nodeTypeManager,
                            ValueFactoryImpl valueFactory,
                            AccessManager accessManager) throws RepositoryConfigurationException,
                                                        PathNotFoundException,
                                                        RepositoryException {

    this.workspaceName = config.getName();

    this.dataManager = dataManager;

    this.namespaceRegistry = namespaceRegistry;
    this.locationFactory = locationFactory;
    this.nodeTypeManager = nodeTypeManager;
    this.valueFactory = valueFactory;
    this.accessManager = accessManager;

    this.restorePath = config.getInitializer().getParameterValue(RestoreWorkspaceInitializer.RESTORE_PATH_PARAMETER, null);
    if (this.restorePath == null)
      throw new RepositoryConfigurationException("Workspace (" + workspaceName
          + ") RestoreIntializer should have mandatory parameter " + RestoreWorkspaceInitializer.RESTORE_PATH_PARAMETER);
  }

  @Deprecated
  public NodeData initWorkspace(InternalQName rootNodeType) throws RepositoryException {

    log.warn("NOT IMPLEMENTED! initWorkspace(InternalQName rootNodeType) is deprecated");
    return null;
  }

  public NodeData initWorkspace() throws RepositoryException {

    if (isWorkspaceInitialized()) {
      return (NodeData) dataManager.getItemData(Constants.ROOT_UUID);
    }

    try {
      long start = System.currentTimeMillis();
      
      PlainChangesLog changes = read();
      
      //log.info(changes.dump());

      dataManager.save(changes);

      final NodeData root = (NodeData) dataManager.getItemData(Constants.ROOT_UUID);

      log.info("Workspace " + workspaceName + " restored from file " + restorePath + " in "
          + (System.currentTimeMillis() - start) * 1d / 1000 + "sec");

      return root;
    } catch (XMLStreamException e) {
      throw new RepositoryException(e);
    } catch (FactoryConfigurationError e) {
      throw new RepositoryException(e);
    } catch (IOException e) {
      throw new RepositoryException(e);
    } catch (IllegalNameException e) {
      throw new RepositoryException(e);
    }
  }

  /**
   * Parse of SysView export content and fill changes log within it.
   * 
   * @throws XMLStreamException
   * @throws FactoryConfigurationError
   * @throws IOException
   * @throws RepositoryException
   * @throws NamespaceException
   * @throws IllegalNameException
   */
  protected PlainChangesLog read() throws XMLStreamException,
                                FactoryConfigurationError,
                                IOException,
                                NamespaceException,
                                RepositoryException,
                                IllegalNameException {

    InputStream input = new FileInputStream(restorePath);
    try {
      XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);

      // SV prefix URIs
      String svURI = null;
      String exoURI = null;

      PlainChangesLog changes = new PlainChangesLogImpl();

      //SVNodeData currentNode = null;
      Stack<SVNodeData> parents = new Stack<SVNodeData>();

      SVPropertyData currentProperty = null;

      // TODO use TransientValueData with FileCleaner etc,
      // to have BLOBs in files
      // or use EditableValueData
      StringBuilder propertyValue = null;
      int propertyType = -1;

      while (reader.hasNext()) {
        int eventCode = reader.next();

        switch (eventCode) {

        case StartElement.START_ELEMENT: {

          String lname = reader.getLocalName();
          String prefix = reader.getPrefix();
          if (Constants.NS_SV_PREFIX.equals(prefix)) {
            // read prefixes URIes from source SV XML
            if (svURI == null) {
              svURI = reader.getNamespaceURI(Constants.NS_SV_PREFIX);
              exoURI = reader.getNamespaceURI(Constants.NS_EXO_PREFIX);
            }

            if (Constants.SV_NODE.equals(lname)) {
              String svName = reader.getAttributeValue(svURI, Constants.SV_NAME);
              String exoId = reader.getAttributeValue(exoURI, Constants.EXO_ID);
              if (svName != null && exoId != null) {
                // create subnode
                QPath currentPath;
                String parentId;
                int orderNumber;
                if (parents.size() > 0) {
                  // path to a new node
                  SVNodeData parent = parents.peek();

                  InternalQName name = locationFactory.parseJCRName(svName).getInternalName();

                  int[] chi = parent.addChildNode(name);
                  orderNumber = chi[0];
                  int index = chi[1];
                  currentPath = QPath.makeChildPath(parent.getQPath(), name, index);

                  parentId = parent.getIdentifier();
                } else {
                  // root
                  currentPath = Constants.ROOT_PATH;
                  parentId = null;
                  orderNumber = 0;

                  // register namespaces from jcr:root node
                  for (int i = 0; i < reader.getNamespaceCount(); i++) {
                    String nsp = reader.getNamespacePrefix(i);
                    try {
                      namespaceRegistry.getURI(nsp);
                    } catch (NamespaceException e) {
                      namespaceRegistry.registerNamespace(nsp, reader.getNamespaceURI(i));
                    }
                  }
                }

                SVNodeData currentNode = new SVNodeData(currentPath, exoId, parentId, 0, orderNumber);

                // push current node as parent
                parents.push(currentNode);

                // add current node to changes log.
                // add node, no event fire, persisted, internally created, root is ancestor to save
                changes.add(new ItemState(currentNode, ItemState.ADDED, false, Constants.ROOT_PATH, true, true));
              } else
                log.warn("Node skipped name=" + svName + " id=" + exoId + ". Context node "
                    + (parents.size() > 0 ? parents.peek().getQPath().getAsString() : "/"));
            } else if (Constants.SV_PROPERTY.equals(lname)) {
              String svName = reader.getAttributeValue(svURI, Constants.SV_NAME);
              String exoId = reader.getAttributeValue(exoURI, Constants.EXO_ID);
              String svType = reader.getAttributeValue(svURI, Constants.SV_TYPE);
              if (svName != null && svType != null && exoId != null) {
                if (parents.size() > 0) {
                  SVNodeData parent = parents.peek();
                  QPath currentPath =
                      QPath.makeChildPath(parent.getQPath(), locationFactory.parseJCRName(svName).getInternalName());
                  try {
                    propertyType = PropertyType.valueFromName(svType);
                  } catch (IllegalArgumentException e) {
                    propertyType = ExtendedPropertyType.valueFromName(svType);
                  }
                  
                  // exo:multivalued optional, assigned for multivalued properties only
                  String exoMultivalued = reader.getAttributeValue(exoURI, Constants.EXO_MULTIVALUED);
                  
                  currentProperty = new SVPropertyData(currentPath, exoId, 0, propertyType, parent.getIdentifier(),
                                                       ("true".equals(exoMultivalued) ? true : false));
                  propertyValue = new StringBuilder();
                } else
                  log.warn("Property can'b be first name=" + svName + " type=" + svType + " id=" + exoId
                      + ". Node should be prior. Context node "
                      + (parents.size() > 0 ? parents.peek().getQPath().getAsString() : "/"));
              } else
                log.warn("Property skipped name=" + svName + " type=" + svType + " id=" + exoId + ". Context node "
                    + (parents.size() > 0 ? parents.peek().getQPath().getAsString() : "/"));
            }
          }
          break;
        }

        case StartElement.CHARACTERS: {
          if (propertyValue != null)
            // read property value text
            propertyValue.append(reader.getText()); // TODO String in memory, EOfM problem for BLOBs!!!

          break;
        }

        case StartElement.END_ELEMENT: {
          String lname = reader.getLocalName();
          String prefix = reader.getPrefix();
          if (Constants.NS_SV_PREFIX.equals(prefix)) {
            if (Constants.SV_NODE.equals(lname)) {
              // change current context
              // - pop parent from the stack
              parents.pop();
            } else if (Constants.SV_PROPERTY.equals(lname)) {
              // apply property to the current node and changes log
              if (currentProperty != null) {
                SVNodeData parent = parents.peek();

                // check NodeData specific properties
                // TODO reuse parsed Names in value event here to have performance good
                if (currentProperty.getQPath().getName().equals(Constants.JCR_PRIMARYTYPE)) {
                  parent.setPrimartTypeName(InternalQName.parse(new String(currentProperty.getValues().get(0).getAsByteArray())));
                } else if (currentProperty.getQPath().getName().equals(Constants.JCR_MIXINTYPES)) {
                  InternalQName[] mixins = new InternalQName[currentProperty.getValues().size()];
                  for (int i = 0; i < currentProperty.getValues().size(); i++) {
                    mixins[i] = InternalQName.parse(new String(currentProperty.getValues().get(i).getAsByteArray()));
                  }
                  parent.setMixinTypeNames(mixins);
                }

                // TODO multivalued option, handled by exo:multivalued attr in SV file
//                PropertyDefinitions pdefs;
//                try {
//                  pdefs =
//                      nodeTypeManager.findPropertyDefinitions(currentProperty.getQPath().getName(),
//                                                              parent.getPrimaryTypeName(),
//                                                              parent.getMixinTypeNames());
//                } catch (RepositoryException e) {
//                  log.warn(e.getMessage() + ". Target property " + currentProperty.getQPath().getAsString());
//                  pdefs = null;
//                }
//                if (pdefs != null) {
//                  PropertyDefinition pdef = pdefs.getAnyDefinition();
//                  if (pdef != null)
//                    currentProperty.setMultiValued(pdef.isMultiple());
//                  else
//                    log.warn("There is no definition for property " + currentProperty.getQPath().getAsString());  
//                }

                // add property, no event fire, persisted, internally created, root is ancestor to save
                changes.add(new ItemState(currentProperty, ItemState.ADDED, false, Constants.ROOT_PATH, true, true));

                // reset property context
                propertyValue = null;
                propertyType = -1;
                currentProperty = null;
              }
            } else if (Constants.SV_VALUE.equals(lname)) {
              // apply property value to the current property
              TransientValueData vdata;
              if (propertyType == PropertyType.NAME) {
                vdata = new TransientValueData(locationFactory.parseJCRName(propertyValue.toString()).getInternalName());
              } else if (propertyType == PropertyType.PATH) {
                vdata = new TransientValueData(locationFactory.parseJCRPath(propertyValue.toString()).getInternalPath());
              } else if (propertyType == PropertyType.DATE) {
                vdata = new TransientValueData(JCRDateFormat.parse(propertyValue.toString()));
              } else {
                vdata = new TransientValueData(propertyValue.toString()); // other like String  
              }

              vdata.setOrderNumber(currentProperty.getValues().size());
              currentProperty.getValues().add(vdata);
              propertyValue = new StringBuilder();
            }
          }
          break;
        }
        }
      }

      return changes;
    } finally {
      input.close();
    }
  }

  public void start() {
  }

  public void stop() {
  }

  public boolean isWorkspaceInitialized() {
    try {
      return dataManager.getItemData(Constants.ROOT_UUID) == null ? false : true;
    } catch (RepositoryException e) {
      return false;
    }
  }
}
