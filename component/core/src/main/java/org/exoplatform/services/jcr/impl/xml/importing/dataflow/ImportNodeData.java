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
package org.exoplatform.services.jcr.impl.xml.importing.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ImportNodeData extends TransientNodeData implements ImportItemData {
  /**
   * 
   */
  private static Log                   log       = ExoLogger.getLogger("jcr.ImportedNodeData");

  /**
   * 
   */
  private boolean                      isMixReferenceable;

  /**
   * 
   */
  private boolean                      isMixVersionable;

  /**
   * 
   */
  private String                       versionHistoryIdentifier;

  /**
   * 
   */
  private boolean                      isContainsVersionhistory;

  /**
   * 
   */
  private String                       baseVersionIdentifier;

  /**
   * 
   */
  private final List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();

  /**
   * @param parent
   * @param name
   * @param index
   */
  public ImportNodeData(ItemData parent, InternalQName name, int index) {
    super();
    this.qpath = QPath.makeChildPath(parent.getQPath(), name, index);
    this.parentIdentifier = parent.getIdentifier();
  }
  /**
   * 
   * @param path
   * @param identifier
   * @param version
   * @param primaryTypeName
   * @param mixinTypeNames
   * @param orderNum
   * @param parentIdentifier
   * @param acl
   */
  public ImportNodeData(QPath path,
                          String identifier,
                          int version,
                          InternalQName primaryTypeName,
                          InternalQName[] mixinTypeNames,
                          int orderNum,
                          String parentIdentifier,
                          AccessControlList acl) {
    super(path,
          identifier,
          version,
          primaryTypeName,
          mixinTypeNames,
          orderNum,
          parentIdentifier,
          acl);
  }

  /**
   * @return the currentNodeTypes
   */
  public void addNodeType(ExtendedNodeType nt) {
    nodeTypes.add(nt);
  }

  /**
   * @return the baseVersionIdentifier
   */
  public String getBaseVersionIdentifier() {
    return baseVersionIdentifier;
  }

  /**
   * @return the currentNodeTypes
   */
  public List<ExtendedNodeType> getCurrentNodeTypes() {
    return nodeTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.dataflow.TransientNodeData#getMixinTypeNames()
   */
  @Override
  public InternalQName[] getMixinTypeNames() {
    if (mixinTypeNames == null)
      return new InternalQName[0];
    return mixinTypeNames;

  }

  /**
   * @return the nodeTypes
   */
  public List<ExtendedNodeType> getNodeTypes() {
    return nodeTypes;
  }

  /**
   * @return the versionHistoryIdentifier
   */
  public String getVersionHistoryIdentifier() {
    return versionHistoryIdentifier;
  }

  /**
   * @return the isContainsVersionhistory
   */
  public boolean isContainsVersionhistory() {
    return isContainsVersionhistory;
  }

  /**
   * @return the isMixReferenceable
   */
  public boolean isMixReferenceable() {
    return isMixReferenceable;
  }

  /**
   * @return the isMixVersionable
   */
  public boolean isMixVersionable() {
    return isMixVersionable;
  }

  /**
   * @param baseVersionIdentifier the baseVersionIdentifier to set
   */
  public void setBaseVersionIdentifier(String baseVersionIdentifier) {
    this.baseVersionIdentifier = baseVersionIdentifier;
  }

  /**
   * @param isContainsVersionhistory the isContainsVersionhistory to set
   */
  public void setContainsVersionhistory(boolean isContainsVersionhistory) {
    this.isContainsVersionhistory = isContainsVersionhistory;
  }

  /**
   * @param isMixReferenceable the isMixReferenceable to set
   */
  public void setMixReferenceable(boolean isMixReferenceable) {
    this.isMixReferenceable = isMixReferenceable;
  }

  /**
   * @param isMixVersionable the isMixVersionable to set
   */
  public void setMixVersionable(boolean isMixVersionable) {
    this.isMixVersionable = isMixVersionable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.ImportedItemData#setParentIdentifer(java.lang.String)
   */
  public void setParentIdentifer(String identifer) {
    this.parentIdentifier = identifer;
  }

  public void setPrimaryTypeName(InternalQName name) {
    primaryTypeName = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.ImportedItemData#setQPath(org.exoplatform.services.jcr.datamodel.QPath)
   */
  public void setQPath(QPath path) {
    this.qpath = path;
  }

  /**
   * @param versionHistoryIdentifier the versionHistoryIdentifier to set
   */
  public void setVersionHistoryIdentifier(String versionHistoryIdentifier) {
    this.versionHistoryIdentifier = versionHistoryIdentifier;
  }

  public static ImportNodeData createCopy(TransientNodeData source) {
    return new ImportNodeData(source.getQPath(),
                                source.getIdentifier(),
                                source.getPersistedVersion(),
                                source.getPrimaryTypeName(),
                                source.getMixinTypeNames(),
                                source.getOrderNumber(),
                                source.getParentIdentifier(),
                                source.getACL());

  }
}
