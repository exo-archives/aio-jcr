/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases.common;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.OnParentVersionAction;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: TestMultiValueOfReferenceProperty.java 12841 2007-02-16 08:58:38Z peterit $
 * 
 * Items under /jcr:system
 */

public class TestMultiValueOfReferenceProperty extends BaseUsecasesTest {
  
   
  public void testValuesOfReferenceProperty() throws Exception {   
    registerExoCategoriedType() ;
    
    String[] wss = repository.getWorkspaceNames();    
    Session session = repository.getSystemSession(wss[0]);           
    Node refNode1 = session.getRootNode().addNode("refNode1");    
    refNode1.addMixin("mix:referenceable");    
    
    Node refNode2 = session.getRootNode().addNode("refNode2");
    refNode2.addMixin("mix:referenceable");    
    session.save();
    String node1UUID = refNode1.getUUID();
    String node2UUID = refNode2.getUUID();    
    Node testNode = session.getRootNode().addNode("testNode1");    
    session.save();
    session.refresh(true);
    // testNode refecenced to refNode1
    addReferenceProperty(testNode,refNode1);    
    // testNode refecenced to refNode2
    addReferenceProperty(testNode,refNode2);
    
    try {
      refNode1 = session.getNodeByUUID(node1UUID) ;      
      PropertyIterator iter1 = refNode1.getReferences() ;      
      assertTrue(iter1.getSize()==1) ;
    }catch (Exception e) {
      fail("\n\n ====>Exception when use Node.getRefencences medthod:" + e.getMessage()) ;
    }
    
    try {
      refNode2 = session.getNodeByUUID(node2UUID) ;      
      PropertyIterator iter2 = refNode2.getReferences() ;      
      assertTrue(iter2.getSize()==1) ;
    }catch (Exception e) {
      fail("\n\n ===>Exception when use Node.getRefencences medthod:" + e.getMessage()) ;
    }    
    refNode1.remove() ;
    refNode2.remove() ;
    testNode.remove() ;
    session.save() ;
  }
  
  private void addReferenceProperty(Node srcNode, Node refNode) throws Exception{
    String CATEGORY_MIXIN = "exo:categorized" ;
    String CATEGORY_PROP = "exo:category" ;
    Session session = srcNode.getSession() ;
    Value ref1Value = session.getValueFactory().createValue(refNode);
    
    if (!srcNode.isNodeType(CATEGORY_MIXIN)) {     
      srcNode.addMixin(CATEGORY_MIXIN);    
      srcNode.setProperty(CATEGORY_PROP, new Value[] {ref1Value});
    } else {
      List<Value> vals = new ArrayList<Value>();
      Value[] values = srcNode.getProperty(CATEGORY_PROP).getValues();
      for (int i = 0; i < values.length; i++) {
        Value value = values[i];       
        vals.add(value);
      }
      vals.add(ref1Value);
      srcNode.setProperty(CATEGORY_PROP, vals.toArray(new Value[vals.size()]));
    }
    session.save() ;
    session.refresh(true) ;
  }
  
  private void registerExoCategoriedType() throws Exception {    
    
//  <nodeType name="exo:categorized" isMixin="true" hasOrderableChildNodes="false" primaryItemName="">
//  <propertyDefinitions>
//    <propertyDefinition name="exo:category" requiredType="Reference" autoCreated="false" mandatory="true"
//      onParentVersion="COPY" protected="false" multiple="true">
//      <valueConstraints/>
//    </propertyDefinition>        
//  </propertyDefinitions>
//  </nodeType>
    
      ExtendedNodeTypeManager manager = repository.getNodeTypeManager() ;
      NodeTypeValue exoCategoried = new NodeTypeValue() ;
      
      exoCategoried.setName("exo:categorized") ;
      exoCategoried.setMixin(true) ;
      exoCategoried.setOrderableChild(false) ;
      exoCategoried.setPrimaryItemName("") ;
      
      PropertyDefinitionValue exoCategory = new PropertyDefinitionValue() ;
      exoCategory.setName("exo:category") ;
      exoCategory.setRequiredType(PropertyType.REFERENCE) ;
      exoCategory.setAutoCreate(false) ;
      exoCategory.setMandatory(true) ;
      exoCategory.setOnVersion(OnParentVersionAction.COPY) ;
      exoCategory.setMultiple(true) ;
      
      List<PropertyDefinitionValue> propetiesDefValues = new ArrayList<PropertyDefinitionValue>() ;
      propetiesDefValues.add(exoCategory) ;
      exoCategoried.setDeclaredPropertyDefinitionValues(propetiesDefValues) ;
      
      manager.registerNodeType(exoCategoried,ExtendedNodeTypeManager.IGNORE_IF_EXISTS) ; 
    }
}
