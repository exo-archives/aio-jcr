/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jcr.PropertyType;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer;
import org.exoplatform.services.jcr.impl.storage.value.StandaloneStoragePluginProvider;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.LogConfigurationInitializer;
/**
 * Created by The eXo Platform SARL        .
 * 
 * Prerequisites: there should be "jdbcjcr" DataSource configured
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JDBCWDCTest.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class JDBCWDCTest extends TestCase {

  protected static Log log = ExoLogger.getLogger("jcr.JDBCWorkspaceDataContainer");

  
  protected WorkspaceEntry config;
  protected String sourceName = "jdbcjcr";
  JDBCWorkspaceDataContainer container;
  
  @Override
  protected void setUp() throws Exception {
    
    config = new WorkspaceEntry();
    config.setName("test");
    ContainerEntry containerEntry = new ContainerEntry();
    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", sourceName));
    params.add(new SimpleParameterEntry("multi-db", "true"));
    containerEntry.setParameters(params);
    config.setContainer(containerEntry);
    
    // Construct BasicDataSource reference
    Reference ref = new Reference("javax.sql.DataSource",
        "org.apache.commons.dbcp.BasicDataSourceFactory", null);

    //Reference ref = new Reference("org.hsqldb.jdbc.jdbcDataSource",
    //    "org.hsqldb.jdbc.jdbcDataSourceFactory", null);
       
    ref.add(new StringRefAddr("driverClassName",
        "org.hsqldb.jdbcDriver"));
    
    ref.add(new StringRefAddr("url", "jdbc:hsqldb:file:target/data/test"));
    //ref.add(new StringRefAddr("url", "jdbc:hsqldb:mem:aname"));
    
    ref.add(new StringRefAddr("username", "sa"));
    ref.add(new StringRefAddr("password", ""));
    //ref.add(new StringRefAddr("maxActive", "10"));
    //ref.add(new StringRefAddr("maxWait", "10"));
    //ref.add(new StringRefAddr("database", "jdbc:hsqldb:file:data/test"));


//    SimpleJNDIContextInitializer.initialize(sourceName, ref);
//    
    container = new JDBCWorkspaceDataContainer(config, null,null, new StandaloneStoragePluginProvider(config));
    
    Properties logProps = new Properties();
    logProps.put("org.apache.commons.logging.simplelog.defaultlog", "debug");
    
    new LogConfigurationInitializer("org.exoplatform.services.log.impl.BufferedSimpleLog", 
        "org.exoplatform.services.log.impl.SimpleLogConfigurator", 
        logProps);
    
    
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testContainerStartUp() throws Exception {
    //log.info("Container "+container);
    InitialContext context = new InitialContext();
    DataSource ds = (DataSource)context.lookup(sourceName);
    assertNotNull(sourceName);
    
    
    Connection conn = ds.getConnection();
    assertNotNull(conn);
//    conn = ds.getConnection();
//    conn = ds.getConnection();
//    conn = ds.getConnection();
//    conn = ds.getConnection();
//    conn = ds.getConnection();
//    conn = ds.getConnection();
    
    //// COMMONS-DBCP /////// 
    //BasicDataSource bds = (BasicDataSource)ds; 
    //System.out.println("getMaxActive: "+bds.getMaxActive());
    //System.out.println("getInitialSize: "+bds.getInitialSize());
    //System.out.println("getNumActive: "+bds.getNumActive());
    //System.out.println("getNumIdle: "+bds.getNumIdle());
    
    //System.out.println("getMaxWait: "+bds.getMaxWait());

    ////////////////

    
    // (conn instanceof PooledConnection)
    //System.out.println("CONN: "+conn);
    //System.out.println("Container "+container);
  
  }
  
  public void testCreateDB() throws Exception {
    String script = 
    "CREATE TABLE JCR_MITEM(ID VARCHAR(255) NOT NULL PRIMARY KEY,VERSION INTEGER NOT NULL,PATH VARCHAR(255) NOT NULL);"+
    "CREATE TABLE JCR_MNODE(ID VARCHAR(255) NOT NULL PRIMARY KEY,ORDER_NUM INTEGER,PARENT_ID VARCHAR(255),CONSTRAINT FK_NODEITEM FOREIGN KEY(PARENT_ID) REFERENCES JCR_MNODE(ID),CONSTRAINT FKA3F3EA499B521C0A FOREIGN KEY(ID) REFERENCES JCR_MITEM(ID))"+
    "CREATE TABLE JCR_MPROPERTY(ID VARCHAR(255) NOT NULL PRIMARY KEY,TYPE INTEGER NOT NULL,PARENT_ID VARCHAR(255) NOT NULL,MULTIVALUED BOOLEAN NOT NULL,CONSTRAINT FKD9E9969CE7BEC36E FOREIGN KEY(PARENT_ID) REFERENCES JCR_MNODE(ID),CONSTRAINT FKD9E9969C9B521C0A FOREIGN KEY(ID) REFERENCES JCR_MITEM(ID))"+
    "CREATE TABLE JCR_MVALUE(ID BIGINT generated by default as identity (START WITH 2, INCREMENT BY 1) NOT NULL PRIMARY KEY, DATA VARBINARY(65535) NOT NULL,ORDER_NUM INTEGER,PROPERTY_ID VARCHAR(255) NOT NULL,CONSTRAINT FKDAF3DDEA8AD85276 FOREIGN KEY(PROPERTY_ID) REFERENCES JCR_MPROPERTY(ID))";
    
    //DBSchemaCreator.initialize(sourceName, script);
  }

  public void testAddRoot() throws Exception {

    InternalQName nt = Constants.NT_UNSTRUCTURED;
    QPath rootPath = QPath.parse(Constants.ROOT_URI);
    WorkspaceStorageConnection conn = container.openConnection();
    NodeData node = new TransientNodeData(rootPath, 
        Constants.ROOT_UUID, 1, nt, new InternalQName[0],
        0, null, new AccessControlList());
    TransientPropertyData ntProp = new TransientPropertyData(
        QPath.makeChildPath(rootPath, Constants.JCR_PRIMARYTYPE), 
        "1", 1, PropertyType.NAME,  Constants.ROOT_UUID, false);
    ValueData vd = new TransientValueData(Constants.NT_UNSTRUCTURED.getAsString()); 
    ntProp.setValue(vd);
    conn.add(node);
    conn.add(ntProp);
    conn.commit();
    //assertNotNull(root);
    //assertEquals(Constants.ROOT_URI, root.getQPath().getAsString());
    //assertEquals("nt:unstructured", locationFactory.createJCRName(root.getPrimaryTypeName()).getAsString());
  }

}
