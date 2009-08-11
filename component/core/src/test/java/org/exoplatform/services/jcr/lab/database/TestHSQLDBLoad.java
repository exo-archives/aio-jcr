/**
 * 
 */
/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.lab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jcr.PropertyType;

import junit.framework.TestCase;

import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCStorageConnection;
import org.exoplatform.services.jcr.impl.storage.jdbc.init.DBInitializer;
import org.exoplatform.services.jcr.util.SIDGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 11.08.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestHSQLDBLoad extends TestCase {

  public static final int    TEST_EXECUTIONS_COUNT    = 1000;

  public static final int    NODES_COUNT              = 1000;

  public static final int    TREE_SIZE                = 1;

  public static final int    PROPERTIES_COUNT_ENDNODE = 10;

  public static final String TEST_ROOT_ID             = "__hsqldb_test_root_000000";

  public static final String TEST_CONTAINER_NAME      = "HSQLDB-Test";

  // public static final String TEST_NODE_ID_PREFIX = "__hsqldb_test_node";

  // public static final String TEST_PROPERTY_ID_PREFIX = "__hsqldb_test_property";

  public static final String INSERT_NODE              = "insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, N_ORDER_NUM) VALUES(?,?,?,?,?,"
                                                          + JDBCStorageConnection.I_CLASS_NODE
                                                          + ",?,?)";

  public static final String INSERT_PROPERTY          = "insert into JCR_SITEM(ID, PARENT_ID, NAME, CONTAINER_NAME, VERSION, I_CLASS, I_INDEX, P_TYPE, P_MULTIVALUED) VALUES(?,?,?,?,?,"
                                                          + JDBCStorageConnection.I_CLASS_PROPERTY
                                                          + ",?,?,?)";

  private Connection         connection;

  private PreparedStatement  insertNode;

  private PreparedStatement  insertProperty;
  
  private static List<String> parents;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // JDBC Connection
    Class.forName("org.hsqldb.jdbcDriver");

    // JCR tables init (SingleDB)
    DBInitializer dbinit = new DBInitializer(TEST_CONTAINER_NAME,
                                             openConnection(),
                                             "/conf/storage/jcr-sjdbc.sql",
                                             false);
    dbinit.init();

    // New Connection
    connection = openConnection();

    // Load data
    if (!connection.createStatement().executeQuery("select * from JCR_SITEM where ID='"
        + TEST_ROOT_ID + "'").next()) {

      connection.setAutoCommit(false);

      parents = new ArrayList<String>();
      
      // load data... root
      addNode(connection,
              SIDGenerator.generate(),
              Constants.ROOT_PARENT_UUID,
              "[]dummy1",
              "dummy_container1",
              1,
              1,
              1);
      addNode(connection,
              SIDGenerator.generate(),
              Constants.ROOT_PARENT_UUID,
              "[]dummy1",
              "dummy_container2",
              1,
              1,
              1);
      
      addNode(connection,
              TEST_ROOT_ID,
              Constants.ROOT_PARENT_UUID,
              "[]hsqldb_test_root",
              TEST_CONTAINER_NAME,
              1,
              1,
              1);

      // ...and tree, parents
      int parentsCnt = NODES_COUNT / TREE_SIZE;
      for (int i = 0; i < parentsCnt; i++) {
        String parentId = SIDGenerator.generate();
        addNode(connection, parentId, TEST_ROOT_ID, "[]parent" + i, TEST_CONTAINER_NAME, 1, 1, i);
        
        parents.add(parentId);

        // subnodes
        for (int si = 0; si < TREE_SIZE; si++) {
          String nodeId = SIDGenerator.generate();
          addNode(connection, nodeId, parentId, "[]node" + si, TEST_CONTAINER_NAME, 1, 1, si);

          // few properties
          for (int pi = 0; pi < PROPERTIES_COUNT_ENDNODE; pi++) {
            addProperty(connection, SIDGenerator.generate(), nodeId, "[]node" + si + "_property"
                + pi, TEST_CONTAINER_NAME, 1, 1, PropertyType.STRING, false);
          }

          // commit the part
          connection.commit();
        }
      }

      connection.commit();
    }
  }

  @Override
  protected void tearDown() throws Exception {

    if (insertNode != null)
      insertNode.close();

    if (insertProperty != null)
      insertProperty.close();

    connection.close();
    connection = null;

    super.tearDown();
  }

  private Connection openConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:hsqldb:file:target/hsqldb-load/data0");
  }

  private int addNode(Connection conn,
                      String id,
                      String parentId,
                      String path,
                      String containerName,
                      int version,
                      int index,
                      int orderNumber) throws SQLException {
    if (insertNode == null)
      insertNode = conn.prepareStatement(INSERT_NODE);
    else
      insertNode.clearParameters();

    insertNode.setString(1, id);
    // if root then parent identifier equals space string
    insertNode.setString(2, parentId);
    insertNode.setString(3, path);
    insertNode.setString(4, containerName);
    insertNode.setInt(5, version);
    insertNode.setInt(6, index);
    insertNode.setInt(7, orderNumber);
    return insertNode.executeUpdate();
  }

  protected int addProperty(Connection conn,
                            String id,
                            String parentId,
                            String path,
                            String containerName,
                            int version,
                            int index,
                            int type,
                            boolean multi) throws SQLException {
    if (insertProperty == null)
      insertProperty = conn.prepareStatement(INSERT_PROPERTY);
    else
      insertProperty.clearParameters();

    insertProperty.setString(1, id);
    insertProperty.setString(2, parentId);
    insertProperty.setString(3, path);
    insertProperty.setString(4, containerName);
    insertProperty.setInt(5, version);
    insertProperty.setInt(6, index);
    insertProperty.setInt(7, type);
    insertProperty.setBoolean(8, multi);

    return insertProperty.executeUpdate();
  }

  public void testSelectByContainerParentId() throws SQLException {

    int parentsCnt = (NODES_COUNT / TREE_SIZE) - 1;
    Random parentsRnd = new Random();
    
    Random nodesRnd = new Random();
    
    // 1. avg. time = 93.109ms, 527.281
    // 2. avg. time = 93.766ms
    final String FIND_ITEM_BY_NAME = "select * from JCR_SITEM"
      + " where CONTAINER_NAME=? and PARENT_ID=? and NAME=? and I_INDEX=? order by I_CLASS, VERSION DESC";
    
    long start = System.currentTimeMillis();
    
    for (int i=0; i<TEST_EXECUTIONS_COUNT; i++) {
      PreparedStatement pstmt = connection.prepareStatement(FIND_ITEM_BY_NAME);
      
      pstmt.setString(1, TEST_CONTAINER_NAME);
      pstmt.setString(2, parents.get(parentsRnd.nextInt(parentsCnt)));
      
      pstmt.setString(3, "[]node" + nodesRnd.nextInt(TREE_SIZE));
      
      pstmt.setInt(4, 1);
      
      // execute query & fetch it
      pstmt.executeQuery().next();
      
      // close stmt
      pstmt.close();
    }
    
    long end = System.currentTimeMillis();
    
    System.out.println("Container+ParentID avg. time = " + ((end - start) * 1d/TEST_EXECUTIONS_COUNT));
  }
  
  public void testSelectByParentIdContainer() throws SQLException {

    int parentsCnt = (NODES_COUNT / TREE_SIZE) - 1;
    Random parentsRnd = new Random();
    
    Random nodesRnd = new Random();
    
    // 1. avg. time = 0.453ms, 0.391
    // 2. avg. time = 0.438ms
    final String FIND_ITEM_BY_NAME = "select * from JCR_SITEM"
      + " where PARENT_ID=? and CONTAINER_NAME=? and NAME=? and I_INDEX=? order by I_CLASS, VERSION DESC";
    
    long start = System.currentTimeMillis();
    
    for (int i=0; i<TEST_EXECUTIONS_COUNT; i++) {
      PreparedStatement pstmt = connection.prepareStatement(FIND_ITEM_BY_NAME);
      
      pstmt.setString(1, parents.get(parentsRnd.nextInt(parentsCnt)));
      
      pstmt.setString(2, TEST_CONTAINER_NAME);
      
      pstmt.setString(3, "[]node" + nodesRnd.nextInt(TREE_SIZE));
      
      pstmt.setInt(4, 1);
      
      // execute query & fetch it
      pstmt.executeQuery().next();
      
      // close stmt
      pstmt.close();
    }
    
    long end = System.currentTimeMillis();
    
    System.out.println("ParentID+Container avg. time = " + ((end - start) * 1d/TEST_EXECUTIONS_COUNT));
  }
  
  public void _testSelectByItemIndex() throws SQLException {

    int parentsCnt = (NODES_COUNT / TREE_SIZE) - 1;
    Random parentsRnd = new Random();
    
    Random nodesRnd = new Random();
    
    // 1. avg. time = 92.765ms
    // 2. avg. time = 0.438ms
    final String FIND_ITEM_BY_NAME = "select * from JCR_SITEM"
      + " where I_INDEX=? and CONTAINER_NAME=? and PARENT_ID=? and NAME=? order by I_CLASS, VERSION DESC";
    
    long start = System.currentTimeMillis();
    
    for (int i=0; i<TEST_EXECUTIONS_COUNT; i++) {
      PreparedStatement pstmt = connection.prepareStatement(FIND_ITEM_BY_NAME);

      pstmt.setInt(1, 1);
      
      pstmt.setString(2, TEST_CONTAINER_NAME);
      
      pstmt.setString(3, parents.get(parentsRnd.nextInt(parentsCnt)));
      
      pstmt.setString(4, "[]node" + nodesRnd.nextInt(TREE_SIZE));
      
      // execute query & fetch it
      pstmt.executeQuery().next();
      
      // close stmt
      pstmt.close();
    }
    
    long end = System.currentTimeMillis();
    
    System.out.println("ItemIndex+Container avg. time = " + ((end - start) * 1d/TEST_EXECUTIONS_COUNT));
  }
}
