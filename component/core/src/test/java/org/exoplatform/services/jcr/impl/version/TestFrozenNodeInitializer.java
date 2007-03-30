/**
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.version;

import java.util.List;

import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestFrozenNodeInitializer.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestFrozenNodeInitializer extends BaseVersionImplTest {

  public void testFrozenCreated() throws Exception {

    createVersionable(TESTCASE_ONPARENT_COPY);
    
    // going to test
    versionable.accept(visitor);
    
    // ask for nt:frozenNode 
    List<ItemState> versionableChanges = versionableLog.getChildrenChanges(versionable.getUUID());
    List<ItemState> testChanges = changesLog.getChildrenChanges(frozenRoot.getUUID());
    
    next: for (ItemState state: versionableChanges) {
      if (versionable.equals(state.getData()))
        continue next; // we have no interest for this item
      
      log.info("versionable change " + state.getData().getQPath().getAsString() + ", " + state.getData().getUUID() + "... ");
      for (ItemState result: testChanges) {
        ItemData resultData = result.getData();
        //log.info("frozen change found " + resultData.getQPath().getAsString() + ", " + resultData.getUUID() + "... ");
        if (resultData.getQPath().getName().equals(state.getData().getQPath().getName())) {
          //if (resultData.isNode() && resultData.getUUID().equals(state.getData().getUUID()))
          log.info("...found in frozen changes " + result.getData().getQPath().getAsString());
          continue next;
        }
      }
      fail("Change is not stored in frozen state: " + state.getData().getQPath().getAsString());
    }
  }
  
  public void testFrozenInitialized_OnParentVersion_COPY() throws Exception {

    createVersionable(TESTCASE_ONPARENT_COPY);
    
    // going to test
    versionable.accept(visitor);
    
    List<ItemState> versionableChanges = versionableLog.getDescendantsChanges(versionable.getQPath());
    List<ItemState> testChanges = changesLog.getDescendantsChanges(frozenRoot.getQPath());
    
    next: for (ItemState state: versionableChanges) {
      if (versionable.equals(state.getData()))
        continue next; // we have no interest for this item
      
      log.info("versionable change " + state.getData().getQPath().getAsString() + ", " + state.getData().getUUID() + "... ");
      for (ItemState result: testChanges) {
        ItemData resultData = result.getData();
        //log.info("frozen change found " + resultData.getQPath().getAsString() + ", " + resultData.getUUID() + "... ");
        if (resultData.getQPath().getName().equals(state.getData().getQPath().getName())) {
          //if (resultData.isNode() && resultData.getUUID().equals(state.getData().getUUID()))
          log.info("...found in frozen changes " + result.getData().getQPath().getAsString());
          continue next;
        }
      }
      fail("Change is not stored in frozen state: " + state.getData().getQPath().getAsString());
    }
  }
  
  public void testFrozenInitialized_OnParentVersion_ABORT() throws Exception {
    
    createVersionable(TESTCASE_ONPARENT_ABORT);
    
    try {
      versionable.accept(visitor);
      fail("A VersionException must be throwed on OnParentVersion=ABORT");
    } catch(VersionException e) {
      // ok
    }
  }
  
  public void testFrozenInitialized_OnParentVersion_IGNORE() throws Exception {
    
    createVersionable(TESTCASE_ONPARENT_IGNORE);
    
    versionable.accept(visitor);
    
    List<ItemState> versionableChanges = versionableLog.getDescendantsChanges(versionable.getQPath());
    List<ItemState> testChanges = changesLog.getDescendantsChanges(frozenRoot.getQPath());
    
    next: for (ItemState state: versionableChanges) {
      if (versionable.equals(state.getData()))
        continue next; // we have no interest for this item
      
      log.info("versionable change " + state.getData().getQPath().getAsString() + ", " + state.getData().getUUID() + "... ");
      for (ItemState result: testChanges) {
        ItemData resultData = result.getData();
        //log.info("frozen change found " + resultData.getQPath().getAsString() + ", " + resultData.getUUID() + "... ");
        if (resultData.getQPath().getName().equals(state.getData().getQPath().getName())) {
          if (resultData.getQPath().getName().equals(PROPERTY_IGNORED))
            fail("Ignored property can't be stored in frozen state: " + resultData.getQPath().getAsString());
          if (resultData.getQPath().getName().equals(NODE_IGNORED))
            fail("Ignored node can't be stored in frozen state: " + resultData.getQPath().getAsString());
          log.info("...found in frozen changes " + resultData.getQPath().getAsString());
          continue next;
        }
      }
      if (!(state.getData().getQPath().getName().equals(PROPERTY_IGNORED) || state.getData().getQPath().getName().equals(NODE_IGNORED)))
        fail("Change is not stored in frozen state: " + state.getData().getQPath().getAsString());
    }
  }
  
  public void testFrozenInitialized_OnParentVersion_VERSION() throws Exception {
    
    createVersionable(TESTCASE_ONPARENT_VERSION);
    
    versionable.accept(visitor);
    
    List<ItemState> versionableChanges = versionableLog.getDescendantsChanges(versionable.getQPath());
    List<ItemState> testChanges = changesLog.getDescendantsChanges(frozenRoot.getQPath());
    
    next: for (ItemState state: versionableChanges) {
      if (versionable.equals(state.getData()))
        continue next; // we have no interest for this item
      
      if (state.getData().getQPath().getName().equals(Constants.JCR_VERSIONHISTORY))
        continue next; // we have no interest for jcr:versionHistory on versionable (as it will be saved as jcr:childVersionHistory)
      
      log.info("versionable change " + state.getData().getQPath().getAsString() + ", " + state.getData().getUUID() + "... ");
      for (ItemState result: testChanges) {
        ItemData resultData = result.getData();
        if (resultData.getQPath().getName().equals(state.getData().getQPath().getName())) {
          if (resultData.getQPath().getName().equals(NODE_VERSIONED)) {
            InternalQName ntName = null; 
            if (resultData instanceof TransientNodeData) {
              ntName = ((TransientNodeData) resultData).getPrimaryTypeName();
            } else if (resultData instanceof PersistedNodeData) {
              ntName = ((PersistedNodeData) resultData).getPrimaryTypeName();
            } else {
              fail("Unknown (for test) node data instance type: " + resultData);
            }
            
            assertEquals("Versioned node must be stored in frozen state as node of type nt:versionedChild: " 
                + resultData.getQPath().getAsString(), Constants.NT_VERSIONEDCHILD, ntName);

            InternalQPath versionHistoryPropertyPath = InternalQPath.makeChildPath(state.getData().getQPath(), Constants.JCR_VERSIONHISTORY);
            PropertyData vh = (PropertyData) session.getTransientNodesManager().getItemData(versionHistoryPropertyPath);
            String vhUuid = new String(vh.getValues().get(0).getAsByteArray());
            
            InternalQPath childVersionHistoryPropertyPath = InternalQPath.makeChildPath(resultData.getQPath(), Constants.JCR_CHILDVERSIONHISTORY);
            ItemState cvhState = changesLog.getItemState(childVersionHistoryPropertyPath);
            assertNotNull("Frozen state of node of type nt:versionedChild hasn't jcr:childVersionHistory property: " + resultData.getQPath().getAsString(), 
                cvhState);
            
            String cvhUuid = new String(((PropertyData) cvhState.getData()).getValues().get(0).getAsByteArray());
            assertEquals("jcr:childVersionHistory property in frozen state contains wrong uuid", vhUuid, cvhUuid);            
            
          } else if (resultData.getQPath().getName().equals(PROPERTY_VERSIONED)) {
            // behaviour of COPY...
          }
          log.info("...found in frozen changes " + resultData.getQPath().getAsString());
          continue next;
        }
      }
      fail("Change is not stored in frozen state: " + state.getData().getQPath().getAsString());
    }
  }  
}