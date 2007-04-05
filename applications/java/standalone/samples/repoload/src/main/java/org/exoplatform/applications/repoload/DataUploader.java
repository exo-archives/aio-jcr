/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.applications.repoload;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.WorkspaceImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.util.UUIDGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.org.ua
 *          reshetnyak.alex@gmail.com		
 * 05.04.2007 17:14:45 
 * @version $Id: DataUploader.java 05.04.2007 17:14:45 rainfox 
 */
public class DataUploader{

  private static class DCPropertyQName {
    public static InternalQName dcElementSet;

    public static InternalQName dcTitle;

    public static InternalQName dcCreator;

    public static InternalQName dcSubject;

    public static InternalQName dcDescription;

    public static InternalQName dcPublisher;
  }

  private HashMap<String, String>    mapConfig;

  private String                     tree        = "10-5-5-5";

  private String                     sVdfile     = "/image.tif";

  private String                     sConf       = "/exo-configuration.xml";

  private String                     sRoot       = "/testroot";

  private String                     sWorkspace  = "ws";

  private String                     sRepository = "db1";

  private String                     sReadTree   = "false";

  private SessionImpl                session;

  private SessionDataManager         dataManager;

  private RepositoryImpl             repository;

  private CredentialsImpl            credentials;

  private WorkspaceImpl              workspace;

  private RepositoryService          repositoryService;

  private NodeImpl                   root;

  private NodeImpl                   rootTestNode;

  private StandaloneContainer        container;

  private WorkspaceStorageConnection connection;

  private WorkspaceDataContainerBase workspaceDataContainer;

  private LocationFactory            locationFactory;

  private TransientValueData         fileData;

  private Calendar                   date;

  private String                     sName;

  private String                     sFile;

  public int                         countNodes;

  public DataUploader(String[] args) {
    this.mapConfig = parceCommandLine(args);
  }

  public void initRepository() throws Exception {
    sConf = mapConfig.get("-conf");
    sRepository = mapConfig.get("-repo");
    sWorkspace = mapConfig.get("-ws");
    sRoot = mapConfig.get("-root");
    sVdfile = mapConfig.get("-vdfile");
    sReadTree = mapConfig.get("-readtree");

    fileData = new TransientValueData(new FileInputStream(sVdfile));

    try {
      StandaloneContainer.setConfigurationPath(sConf);

      container = StandaloneContainer.getInstance();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource("login.conf").toString());

      credentials = new CredentialsImpl("admin", "admin".toCharArray());

      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);

      repository = (RepositoryImpl) repositoryService.getRepository(sRepository);
      if (repository != null)
        System.out.println("--->>> perository");

      session = repository.login(credentials, sWorkspace);
      if (session != null)
        System.out.println("--->>> session");

      locationFactory = session.getLocationFactory();
      if (locationFactory != null)
        System.out.println("--->>> locationFactory");

      dataManager = session.getTransientNodesManager();
      if (dataManager != null)
        System.out.println("--->>> dataManager");

      workspaceDataContainer = (WorkspaceDataContainerBase) (session.getContainer()
          .getComponentInstanceOfType(WorkspaceDataContainerBase.class));
      connection = workspaceDataContainer.openConnection();
      if (connection != null)
        System.out.println("--->>> connection");

      workspace = session.getWorkspace();
      if (workspace != null)
        System.out.println("--->>> workspace");

      root = (NodeImpl) session.getRootNode();
      if (root != null)
        System.out.println("--->>> root");
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }

    try {
      rootTestNode = (NodeImpl) root.getNode(sRoot);
      System.out.println("--->>> Node " + sRoot + " exist");
    } catch (Exception e) {
      try {
        root.addNode(sRoot);
        session.save();
        System.out.println("--->>> Node " + sRoot + " create");
        rootTestNode = (NodeImpl) root.getNode(sRoot);
        if (rootTestNode != null)
          System.out.println("--->>> rootTestNode");
      } catch (Exception ee) {
        ee.printStackTrace();
      }
    }

    DCPropertyQName.dcElementSet = locationFactory.parseJCRName("dc:elementSet").getInternalName();
    DCPropertyQName.dcTitle = locationFactory.parseJCRName("dc:title").getInternalName();
    DCPropertyQName.dcCreator = locationFactory.parseJCRName("dc:creator").getInternalName();
    DCPropertyQName.dcSubject = locationFactory.parseJCRName("dc:subject").getInternalName();
    DCPropertyQName.dcDescription = locationFactory.parseJCRName("dc:description")
        .getInternalName();
    DCPropertyQName.dcPublisher = locationFactory.parseJCRName("dc:publisher").getInternalName();
  }

  public void uploadData() throws Exception {
    int tree[] = getTree(mapConfig.get("-tree"));

    System.out.println(">>>>>>>>>>>---------- Upload data ----------<<<<<<<<<<<<");

    sName = "node";

    sFile = "file";

    date = Calendar.getInstance();

    countNodes = tree[0] * tree[1] * tree[2] * tree[3];

    for (int i = 1; i <= tree[0]; i++) {
      try {
        TransientNodeData nodeData_L1 = addNode(connection, sName + i, i, rootTestNode, date);

        for (int j = 1; j <= tree[1]; j++) {
          TransientNodeData nodeData_L2 = addNode(connection, sName + j, j, nodeData_L1, date);

          for (int k = 1; k <= tree[2]; k++) {
            TransientNodeData nodeData_L3 = addNode(connection, sName + k, k, nodeData_L2, date);

            for (int index = 1; index <= tree[3]; index++)
              addNode_file(connection, sFile + index, index, nodeData_L3, date, fileData);

            connection.commit();
            connection = getConnection();

            System.out.println("Node " + i + " - " + j + " - " + k + " - " + "[1..." + tree[3]
                + "] add");
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        connection.rollback();
        System.out.println(">>>>>>>>>>>---------- Upload data Exception ----------<<<<<<<<<<<<");
      }
    }
  }

  public void readData() {
    if (sReadTree.compareTo("true") == 0) {

      int tree[] = getTree(mapConfig.get("-tree"));

      for (int i = 1; i <= tree[0]; i++) {
        try {
          NodeImpl n_1 = getNode(rootTestNode, sName + i);

          for (int j = 1; j <= tree[1]; j++) {
            NodeImpl n_2 = getNode(n_1, sName + j);

            for (int k = 1; k <= tree[2]; k++) {
              NodeImpl n_3 = getNode(n_2, sName + k);

              for (int s = 1; s <= tree[3]; s++) {
                NodeImpl n_4 = getNode(n_3, sFile + s);

                NodeImpl sourceNode = getNode( n_4, "jcr:content");
                InputStream fis = getProperty( sourceNode, "jcr:data").getStream();

                Value[] t = getProperty( n_4, "dc:title").getValues();
                String tit = t[0].getString();

                Value[] d = getProperty( n_4, "dc:description").getValues();
                String des = d[0].getString();

                System.out.println("--->>> Node " + mapConfig.get("-root") + "/" + (sName + i)
                    + "/" + (sName + j) + "/" + (sName + k) + "/" + (sFile + s) + " exist : "
                    + fis.available() + " b   |   dc:title --> " + tit
                    + "   |   dc:description --> " + des);

              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println(">>>>>>>>>>>---------- Read data Exception ----------<<<<<<<<<<<<");
        }
      }
    }
  }

  private TransientNodeData creteNodeData_nt_folder(String name, int orderNum, NodeImpl parentNode) {
    InternalQName[] mixinTypeNames = new InternalQName[0];

    InternalQName iQName = new InternalQName(Constants.NS_DEFAULT_URI, name);

    QPath path = QPath.makeChildPath(parentNode.getInternalPath(), iQName);

    AccessControlList acl = new AccessControlList();

    String uuid = UUIDGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, Constants.NT_FOLDER,
        mixinTypeNames, orderNum, parentNode.getInternalUUID(), acl);

    return nodeData;
  }

  private TransientNodeData addNode(WorkspaceStorageConnection con, String name, int orderNum,
      NodeImpl parentNode, Calendar date) throws Exception {
    TransientNodeData nodeData = creteNodeData_nt_folder(name, orderNum, parentNode);
    con.add(nodeData);

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), UUIDGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getUUID(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FOLDER));
    con.add(primaryTypeData);

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), UUIDGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getUUID(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);

    return nodeData;
  }

  private TransientNodeData creteNodeData(InternalQName iQName, int orderNum,
      TransientNodeData parentNode, InternalQName primaryType, InternalQName mixinName)
      throws Exception {

    InternalQName[] mixinTypeNames = null;

    if (mixinName == null)
      mixinTypeNames = new InternalQName[0];
    else {
      mixinTypeNames = new InternalQName[1];
      mixinTypeNames[0] = mixinName;
    }

    QPath path = QPath.makeChildPath(parentNode.getQPath(), iQName);

    AccessControlList acl = new AccessControlList();

    String uuid = UUIDGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, primaryType, mixinTypeNames,
        orderNum, parentNode.getUUID(), acl);

    return nodeData;
  }

  private TransientNodeData addNode(WorkspaceStorageConnection con, String name, int orderNum,
      TransientNodeData parentNode, Calendar date) throws Exception {
    TransientNodeData nodeData = creteNodeData(new InternalQName(Constants.NS_DEFAULT_URI, name),
        orderNum, parentNode, Constants.NT_FOLDER, null);
    con.add(nodeData);

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), UUIDGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getUUID(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FOLDER));
    con.add(primaryTypeData);

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), UUIDGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getUUID(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);

    return nodeData;
  }

  private void addNode_file(WorkspaceStorageConnection con, String name, int orderNum,
      TransientNodeData parentNode, Calendar date, TransientValueData fData) throws Exception {

    TransientNodeData nodeData = creteNodeData(new InternalQName(Constants.NS_DEFAULT_URI, name),
        orderNum, parentNode, Constants.NT_FILE, DCPropertyQName.dcElementSet);
    con.add(nodeData);

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), UUIDGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getUUID(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FILE));
    con.add(primaryTypeData);

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), UUIDGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getUUID(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);

    TransientPropertyData mixinTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_MIXINTYPES), UUIDGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getUUID(), true);
    mixinTypeData.setValue(new TransientValueData(DCPropertyQName.dcElementSet));
    con.add(mixinTypeData);

    TransientNodeData contentNode = creteNodeData(Constants.JCR_CONTENT, 0, nodeData,
        Constants.NT_RESOURCE, null);
    con.add(contentNode);

    TransientPropertyData primaryTypeContenNode = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_PRIMARYTYPE), UUIDGenerator.generate(), -1,
        PropertyType.NAME, contentNode.getUUID(), false);
    primaryTypeContenNode.setValue(new TransientValueData(Constants.NT_RESOURCE));
    con.add(primaryTypeContenNode);

    TransientPropertyData uuidPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_UUID), UUIDGenerator.generate(), -1,
        PropertyType.STRING, contentNode.getUUID(), false);
    uuidPropertyData.setValue(new TransientValueData(UUIDGenerator.generate()));
    con.add(uuidPropertyData);

    TransientPropertyData mimeTypePropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_MIMETYPE), UUIDGenerator.generate(), -1,
        PropertyType.STRING, contentNode.getUUID(), false);
    mimeTypePropertyData.setValue(new TransientValueData("image/tiff"));
    con.add(mimeTypePropertyData);

    TransientPropertyData lastModifiedPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_LASTMODIFIED), UUIDGenerator.generate(), -1,
        PropertyType.DATE, contentNode.getUUID(), false);
    lastModifiedPropertyData.setValue(new TransientValueData(date));
    con.add(lastModifiedPropertyData);

    TransientPropertyData dataPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_DATA), UUIDGenerator.generate(), -1,
        PropertyType.BINARY, contentNode.getUUID(), false);
    dataPropertyData.setValue(fData);
    con.add(dataPropertyData);

    addDcElementSet(con, nodeData);
  }

  private void addDcElementSet(WorkspaceStorageConnection con, TransientNodeData nodeData)
      throws Exception {

    addDCProperty(con, nodeData, DCPropertyQName.dcTitle, "Title");
    addDCProperty(con, nodeData, DCPropertyQName.dcCreator, "Creator");
    addDCProperty(con, nodeData, DCPropertyQName.dcSubject, "Subject");
    addDCProperty(con, nodeData, DCPropertyQName.dcDescription, "Description");
    addDCProperty(con, nodeData, DCPropertyQName.dcPublisher, "Publisher");
  }

  private void addDCProperty(WorkspaceStorageConnection con, TransientNodeData dcNode,
      InternalQName propertyQName, String propertyContent) throws Exception {

    TransientPropertyData dcPropertyData = new TransientPropertyData(QPath.makeChildPath(dcNode
        .getQPath(), propertyQName), UUIDGenerator.generate(), -1, PropertyType.STRING, dcNode
        .getUUID(), true);
    dcPropertyData.setValue(new TransientValueData(propertyContent));
    con.add(dcPropertyData);
  }

  public NodeImpl getNode(NodeImpl parentNode, String relPath) throws PathNotFoundException,
      RepositoryException {
    JCRPath itemPath = session.getLocationFactory()
        .createJCRPath(parentNode.getLocation(), relPath);
    NodeImpl node = (NodeImpl) dataManager.getItem(itemPath.getInternalPath(), true);
    if (node == null)
      throw new PathNotFoundException("Node not found " + itemPath.getAsString(true));
    return node;
  }
  
  public Property getProperty(NodeImpl node,String relPath) throws PathNotFoundException, RepositoryException {
    JCRPath itemPath = locationFactory.createJCRPath(node.getLocation(), relPath);
    
    Item prop = dataManager.getItem(itemPath.getInternalPath(), true);
    if(prop == null || prop.isNode())
      throw new PathNotFoundException("Property not found " + itemPath.getAsString(false));
    
    return (Property)prop;
  }

  private int[] getTree(String sTree) {
    String[] masTree = sTree.split("-");
    int iTree[] = new int[4];
    for (int i = 0; i < 4; i++)
      iTree[i] = Integer.valueOf(masTree[i]).intValue();

    return iTree;
  }

  private WorkspaceStorageConnection getConnection() throws Exception {
    return workspaceDataContainer.openConnection();
  }

  private HashMap<String, String> parceCommandLine(String[] args) {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("-conf", "");
    map.put("-root", "");
    map.put("-tree", "");
    map.put("-vdfile", "");
    map.put("-repo", "");
    map.put("-ws", "");
    map.put("-readtree", "");

    for (int i = 0; i < args.length; i++) {
      String[] params = args[i].split("=");
      if (!map.containsKey(params[0]))
        System.out.println("Error: " + params[0] + " unknown parameter");

      map.remove(params[0]);
      map.put(params[0], params[1]);

      System.out.println(params[0] + " = " + params[1]);
    }

    return map;
  }
  
}
