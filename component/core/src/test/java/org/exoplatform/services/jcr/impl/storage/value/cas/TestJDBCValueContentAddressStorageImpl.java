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
package org.exoplatform.services.jcr.impl.storage.value.cas;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 19.07.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: TestJDBCValueContentAddressStorageImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class TestJDBCValueContentAddressStorageImpl extends JcrImplBaseTest {

  private JDBCValueContentAddressStorageImpl vcas;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    Properties props = new Properties();
    
    // find jdbc-source-name
    String jdbcSourceName = null;
    for (WorkspaceEntry wse: repository.getConfiguration().getWorkspaceEntries()) {
      if (wse.getName().equals(session.getWorkspace().getName()))
        jdbcSourceName = wse.getContainer().getParameterValue(JDBCWorkspaceDataContainer.SOURCE_NAME);
    }
    
    if (jdbcSourceName == null)
      fail(JDBCWorkspaceDataContainer.SOURCE_NAME + " required in workspace container config");
    
    props.put(JDBCValueContentAddressStorageImpl.JDBC_SOURCE_NAME_PARAM, jdbcSourceName);
    props.put(JDBCValueContentAddressStorageImpl.TABLE_NAME_PARAM, JDBCValueContentAddressStorageImpl.DEFAULT_TABLE_NAME + "_TEST");
    
    vcas = new JDBCValueContentAddressStorageImpl();
    vcas.init(props);
  }

  
  public void testAddRecord() throws Exception {
    String propertyId, hashId;
    vcas.add(propertyId = IdGenerator.generate(), 0, hashId = IdGenerator.generate());
    
    assertEquals("id should be same but ", hashId, vcas.getIdentifier(propertyId, 0));
  }
  
  public void testAddRecords() throws Exception {
    List<String> testSet = new ArrayList<String>();
    String propertyId = IdGenerator.generate();
    
    for (int i=0; i<100; i++) {      
      String hashId = IdGenerator.generate();
      vcas.add(propertyId, i, hashId);
      testSet.add(hashId);
    }
    
    List<String> ids = vcas.getIdentifiers(propertyId);
    for (int i=0; i<testSet.size(); i++) {
      assertEquals("id should be same but ", testSet.get(i), ids.get(i));  
    }
  }
  
  public void testDeleteRecord() throws Exception {
    String propertyId;
    vcas.add(propertyId = IdGenerator.generate(), 0, IdGenerator.generate());
    
    vcas.delete(propertyId);
    
    try {
      vcas.getIdentifier(propertyId, 0);
      fail("Record was deleted " + propertyId);
    } catch(RecordNotFoundException e) {
      // ok 
    }
  }
  
  
}
