/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.log.ExoLogger;
/**
 * Created by The eXo Platform SARL        . <br/>
 * Prerequisites:
              <value-storages>
              <value-storage class="org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage">
                <properties>
                  <property name="path" value="target/temp/values"/>
                </properties>
                <filters>
                  <filter property-type="Binary"/>
                </filters>
              </value-storage>
            </value-storages>
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueStoragePluginTest.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class ValueStoragePluginTest extends BaseStandaloneTest {

  protected static Log log = ExoLogger.getLogger("jcr.ValueStoragePluginTest");
  
  
//  protected String sourceName = "jdbc/basic";
//  JDBCWorkspaceDataContainer container;
  
  public String getRepositoryName() {
    return repository.getName();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    List <WorkspaceEntry> wsList = repository.getConfiguration().
    getWorkspaceEntries();
    for(WorkspaceEntry ws:wsList) {
      log.info("---- Value STORAGE of "+ws.getName() +" = "+ws.getContainer().getValueStorages());
      if(ws.getName().equals(workspace.getName())) {
       if(ws.getContainer().getValueStorages() == null)
        throw new Exception("No value storage plugins configured for workspace "+ws.getName());
       List <ValueStorageEntry> wssEntries = ws.getContainer().getValueStorages();
       for(ValueStorageEntry we : wssEntries) {
         for(ValueStorageFilterEntry vef : (List <ValueStorageFilterEntry>)we.getFilters()) {
           if(PropertyType.valueFromName(vef.getPropertyType()) == PropertyType.BINARY)
             return;
         }
       }
       throw new Exception("No BINARY type filter found for workspace "+ws.getName());
      }
    }
  }
  
//  public void testConfig() throws Exception {
//  }

  public void testShortBinary() throws Exception {
    Node n = root.addNode("binaryTestNode", "nt:unstructured");
    // add property
    n.setProperty("binaryTestProp", "Binary content", PropertyType.BINARY);
    root.save();
    //log.info("CONTENT '"+n.getProperty("binaryTestProp").getString()+"'");
    assertEquals("Binary content", n.getProperty("binaryTestProp").getString());
    // update property
    n.setProperty("binaryTestProp", "NEW Binary content", PropertyType.BINARY);
    root.save();
    assertEquals("NEW Binary content", n.getProperty("binaryTestProp").getString());
    // delete property
    n.getProperty("binaryTestProp").remove();
    root.save();
  }

  public void testLongBinary() throws Exception {
//    Node n = root.getNode("binaryTestNode");
    Node n = root.addNode("binaryTestNode1", "nt:unstructured");

    // add property
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<90000; i++) {
      sb.append("Binary content");
    }
    String s = new String(sb);
    n.setProperty("binaryTestProp", s, PropertyType.BINARY);
    root.save();
    //System.out.println("CONTENT '"+n.getProperty("binaryTestProp").getString().length()+" "+s.length());
    assertEquals(s, n.getProperty("binaryTestProp").getString());
    // update property
    s+="NEW";
    n.setProperty("binaryTestProp", s, PropertyType.BINARY);
    root.save();
    assertEquals(s, n.getProperty("binaryTestProp").getString());
    // delete property
    n.getProperty("binaryTestProp").remove();
    root.save();
  }

//  public void testMultiBinary() throws Exception {
//    Node n = root.addNode("binaryTestNode2", "nt:unstructured");
//    // add property
//    Value[] vals = new Value[2];
//    vals[0] = new BinaryValue("Binary content1");
//    vals[1] = new BinaryValue("Binary content2");
//    n.setProperty("binaryTestProp", vals);
//    
////    n.setProperty("binaryTestProp", new String[]{"Binary content1","Binary content2"}, PropertyType.BINARY);
//    System.out.println("CONTENT '"+n.getProperties("binaryTestProp").getSize());
//    root.save();
//    //log.info("CONTENT '"+n.getProperty("binaryTestProp").getString()+"'");
//    assertEquals(2, n.getProperties("binaryTestProp").getSize());
//    // update property
////    n.setProperty("binaryTestProp", "NEW Binary content", PropertyType.BINARY);
////    root.save();
////    assertEquals("NEW Binary content", n.getProperty("binaryTestProp").getString());
////    // delete property
////    n.getProperty("binaryTestProp").remove();
////    root.save();
//  }

}
