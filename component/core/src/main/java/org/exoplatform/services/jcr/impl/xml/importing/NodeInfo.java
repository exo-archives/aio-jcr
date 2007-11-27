/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NodeInfo {

  private static Log                   log       = ExoLogger.getLogger("jcr.NodeInfo");

  private final InternalQName          nodeName;

  private final QPath                  parentPath;

  private InternalQName                primaryTypeName;

  private InternalQName[]              mixinNames;

  // private Map<InternalQName, PropertyData> currentProperties;

  private final List<ExtendedNodeType> nodeTypes = new ArrayList<ExtendedNodeType>();

  // private List<DecodedValue> currentNodeMixinTypeNames;

  // private int currentPropType;

  private boolean                      isMixReferenceable;

  private boolean                      isMixVersionable;

  private String                       versionHistoryIdentifier;

  private boolean                      isContainsVersionhistory;

  private String                       baseVersionIdentifier;

  public NodeInfo(QPath parentPath, InternalQName nodeName) {
    super();
    this.parentPath = parentPath;
    this.nodeName = nodeName;
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

  /**
   * @return the mixinNames
   */
  public InternalQName[] getMixinNames() {

    if (mixinNames == null)
      return new InternalQName[0];
    return mixinNames;
  }

  /**
   * @return the nodeName
   */
  public InternalQName getNodeName() {
    return nodeName;
  }

  /**
   * @return the primaryTypeName
   */
  public InternalQName getPrimaryTypeName() {
    return primaryTypeName;
  }

  public QPath getQPath() {
    return QPath.makeChildPath(parentPath, nodeName);
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
   * @param mixinNames the mixinNames to set
   */
  public void setMixinNames(InternalQName[] mixinNames) {
    this.mixinNames = mixinNames;
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

  /**
   * @param primaryTypeName the primaryTypeName to set
   */
  public void setPrimaryTypeName(InternalQName primaryTypeName) {
    this.primaryTypeName = primaryTypeName;
  }

  /**
   * @param versionHistoryIdentifier the versionHistoryIdentifier to set
   */
  public void setVersionHistoryIdentifier(String versionHistoryIdentifier) {
    this.versionHistoryIdentifier = versionHistoryIdentifier;
  }

}
