package org.exoplatform.services.jcr.impl.dataflow;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;

public class ItemDataCopyVisitor1 extends DefaultItemDataCopyVisitor {

  private Log      log = ExoLogger.getLogger("jcr.ItemDataCopyVisitor");
  
  public ItemDataCopyVisitor1(NodeData parent, InternalQName destNodeName,
      NodeTypeManagerImpl nodeTypeManager, SessionDataManager dataManager, boolean keepUUIDs) {
    super(parent, destNodeName, nodeTypeManager, dataManager, keepUUIDs);
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    
    if (log.isDebugEnabled())
      log.debug("entering " + property.getQPath().getAsString());
    
    // don't using super
    InternalQName qname = property.getQPath().getName();
    
    List<ValueData> values;
    
    if (ntManager.isNodeType(Constants.MIX_VERSIONABLE,
        curParent().getPrimaryTypeName(),
        curParent().getMixinTypeNames())) {
      // versionable node copy
//    [mix:versionable] > mix:referenceable
//        mixin
//        - jcr:versionHistory (reference) mandatory protected
//        < 'nt:versionHistory'
//        - jcr:baseVersion (reference) mandatory protected
//        ignore
//        < 'nt:version'
//        - jcr:isCheckedOut (boolean) = 'true' mandatory
//        autocreated protected ignore
//        - jcr:predecessors (reference) mandatory protected
//        multiple
//        < 'nt:version'
//        - jcr:mergeFailed (reference) protected multiple abort
//        < 'nt:version'      
      
      // before manipulate with version stuuf we have create a one new VH right here!
      QPath vhpPath = QPath.makeChildPath(curParent().getQPath(), Constants.JCR_VERSIONHISTORY);
      ItemState vhpState = findLastItemState(vhpPath);
      if (vhpState == null) {
        // need create a new VH
        // NodeData versionable, PlainChangesLogImpl changes, ItemDataConsumer dataManager, NodeTypeManagerImpl ntManager
        //PropertyData vhp = (PropertyData) findLastItemState(vhpPath).getData();
        
        PlainChangesLogImpl changes = new PlainChangesLogImpl();
        VersionHistoryDataHelper vh = new VersionHistoryDataHelper(curParent(), changes, dataManager, ntManager);
        itemAddStates.addAll(changes.getAllStates());
        
        //vhp = (PropertyData) findLastItemState(vhpPath).getData();
      }
      
      values = new ArrayList<ValueData>(1);
      
      if (qname.equals(Constants.JCR_VERSIONHISTORY)) {
        return; // added in VH create
      } else if (qname.equals(Constants.JCR_PREDECESSORS)) {
//        QPath rvPath = QPath.makeChildPath(vhp.getQPath(), Constants.JCR_ROOTVERSION);  
//        PropertyData rvp = (PropertyData) findLastItemState(rvPath).getData();
//        if (rvp != null) {
//          values.add(new TransientValueData(rvp.getUUID())); // UUID of jcr:rootVersion  
//        } else {
//          throw new VersionException("jcr:predecessors: jcr:rootVersion is not found. " + curParent().getQPath().getAsString());
//        }
        return; // added in VH create
      } else if (qname.equals(Constants.JCR_BASEVERSION)) {
//        QPath rvPath = QPath.makeChildPath(vhp.getQPath(), Constants.JCR_ROOTVERSION);  
//        PropertyData rvp = (PropertyData) findLastItemState(rvPath).getData();
//        if (rvp != null) {
//          values.add(new TransientValueData(rvp.getUUID())); // UUID of jcr:rootVersion
//        } else {
//          throw new VersionException("jcr:baseVersion: jcr:rootVersion is not found. " + curParent().getQPath().getAsString());
//        }
        return; // added in VH create
      } else if (qname.equals(Constants.JCR_ISCHECKEDOUT)) {
        values.add(new TransientValueData(true));
      } else if (qname.equals(Constants.JCR_MERGEFAILED)) {
        return; // skip it
      } else if (qname.equals(Constants.JCR_UUID)) {
        values.add(new TransientValueData(curParent().getUUID())); // uuid of the parent
      } else {
        values = property.getValues(); // copy the property
      }
    } else if (ntManager.isNodeType(Constants.MIX_REFERENCEABLE,
        curParent().getPrimaryTypeName(),
        curParent().getMixinTypeNames())
        && qname.equals(Constants.JCR_UUID)) {

      values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData(curParent().getUUID()));
    } else {
      values = property.getValues();
    }
    
    TransientPropertyData newProperty = new TransientPropertyData(
        QPath.makeChildPath(curParent().getQPath(), qname),
        keepUUIDs ? property.getUUID() : UUIDGenerator.generate(),
        -1,
        property.getType(),
        curParent().getUUID(),
        property.isMultiValued());
    
    newProperty.setValues(values);
    
    if (log.isDebugEnabled())
      log.debug("entering COPY " + newProperty.getQPath().getAsString() + "; puuid: " + newProperty.getParentUUID() + "; uuid: " + newProperty.getUUID());
    
    itemAddStates.add(
        new ItemState(newProperty, ItemState.ADDED, true, ancestorToSave, level != 0));
  }
  
  
  
}
