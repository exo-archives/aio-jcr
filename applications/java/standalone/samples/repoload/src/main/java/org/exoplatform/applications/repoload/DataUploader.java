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
package org.exoplatform.applications.repoload;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
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
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 05.04.2007
 * 17:14:45
 * 
 * @version $Id: DataUploader.java 05.04.2007 17:14:45 rainfox
 */
public class DataUploader {

  protected Log log = ExoLogger.getLogger("repload.DataUploader");

  protected static class DCPropertyQName {
    public static InternalQName dcElementSet;

    public static InternalQName dcTitle;

    public static InternalQName dcCreator;

    public static InternalQName dcSubject;

    public static InternalQName dcDescription;

    public static InternalQName dcPublisher;
  }

  protected String[]                   args;

  protected HashMap<String, String>    mapConfig;

  protected String                     tree        = "10-5-5-5";

  protected String                     sVdfile     = "/image.tif";

  protected String                     sConf       = "/exo-configuration.xml";

  protected String                     sRoot       = "/testroot";

  protected String                     sWorkspace  = "ws";

  protected String                     sRepository = "db1";

  protected String                     sReadTree   = "false";

  protected SessionImpl                session;

  protected SessionDataManager         dataManager;

  protected RepositoryImpl             repository;

  protected CredentialsImpl            credentials;

  protected WorkspaceImpl              workspace;

  protected RepositoryService          repositoryService;

  protected NodeImpl                   root;

  protected NodeImpl                   rootTestNode;

  protected StandaloneContainer        container;

  protected WorkspaceStorageConnection connection;

  protected WorkspaceDataContainerBase workspaceDataContainer;

  protected LocationFactory            locationFactory;

  protected TransientValueData         fileData;

  protected Calendar                   date;

  protected String                     sName;

  protected String                     sFile;

  public int                           countNodes;

  protected String                     sMimeType;

  protected FileCleaner                fileCleaner;

  public DataUploader(String[] args) {
    this.mapConfig = parceCommandLine(args);
    this.args = args;
  }

  public void initRepository() throws Exception {
    sConf = mapConfig.get("-conf");
    sRepository = mapConfig.get("-repo");
    sWorkspace = mapConfig.get("-ws");
    sRoot = mapConfig.get("-root");
    sVdfile = mapConfig.get("-vdfile");
    sReadTree = mapConfig.get("-readtree");
    sMimeType = mapConfig.get("-mimeType");
    
    fileCleaner = new FileCleaner();

    if (!sVdfile.equals("")) {
      fileData = new TransientValueData(new FileInputStream(sVdfile));
      fileData.setFileCleaner(fileCleaner);
    }

    try {
      StandaloneContainer.addConfigurationPath(sConf);

      container = StandaloneContainer.getInstance();

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource("login.conf").toString());

      credentials = new CredentialsImpl("admin", "admin".toCharArray());

      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);

      repository = (RepositoryImpl) repositoryService.getRepository(sRepository);
      if (repository != null)
        log.info("--->>> perository");

      session = (SessionImpl) repository.login(credentials, sWorkspace);
      if (session != null)
        log.info("--->>> session");

      locationFactory = session.getLocationFactory();
      if (locationFactory != null)
        log.info("--->>> locationFactory");

      dataManager = session.getTransientNodesManager();
      if (dataManager != null)
        log.info("--->>> dataManager");

      workspaceDataContainer = (WorkspaceDataContainerBase) (session.getContainer()
          .getComponentInstanceOfType(WorkspaceDataContainerBase.class));
      connection = workspaceDataContainer.openConnection();
      if (connection != null)
        log.info("--->>> connection");

      workspace = session.getWorkspace();
      if (workspace != null)
        log.info("--->>> workspace");

      root = (NodeImpl) session.getRootNode();
      if (root != null)
        log.info("--->>> root");
    } catch (Exception e) {
      log.error("Can not initialize repository", e);
    }

    try {
      if (sRoot.startsWith("/")) {
        rootTestNode = (NodeImpl) session.getItem(sRoot);
        log.info("--->>> Node " + sRoot + " exist");
      } else
        new Exception("Test root is not absolute path: " + sRoot);

    } catch (PathNotFoundException e) {
      try {

        // root.addNode(sRoot);
        rootTestNode = addNodes(sRoot, root);
        session.save();
        log.info("--->>> Node " + sRoot + " create");
        // rootTestNode = (NodeImpl) root.getNode(sRoot);
        if (rootTestNode != null)
          log.info("--->>> rootTestNode");
      } catch (Exception ee) {
        log.error("Can not create roottest node: " + sRoot, ee);
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

  private NodeImpl addNodes(String sRoot, NodeImpl parentNode) throws Exception {
    String mas[] = sRoot.split("/");

    NodeImpl temp = parentNode;

    for (int i = 1; i < mas.length; i++) {
      if (temp.hasNode(mas[i]))
        temp = (NodeImpl) temp.getNode(mas[i]);
      else {
        temp = (NodeImpl) temp.addNode(mas[i]);
        session.save();
      }
    }

    return temp;
  }

  public void uploadData() throws Exception {
    long start, end, temp, localStart, localEnd;

    int tree[] = getTree(mapConfig.get("-tree"));

    log.info(">>>>>>>>>>>---------- Upload data ----------<<<<<<<<<<<<");

    sName = "node";

    sFile = "file";

    date = Calendar.getInstance();

    countNodes = 0/* tree[0] * tree[1] * tree[2] * tree[3] */;

    start = System.currentTimeMillis();

    for (int i = 1; i <= tree[0]; i++) {
      try {
        TransientNodeData nodeData_L1 = addNode(connection, sName + i, i, rootTestNode, date);

        for (int j = 1; j <= tree[1]; j++) {
          TransientNodeData nodeData_L2 = addNode(connection, sName + j, j, nodeData_L1, date);

          localStart = System.currentTimeMillis();

          for (int k = 1; k <= tree[2]; k++) {
            TransientNodeData nodeData_L3 = addNode(connection, sName + k, k, nodeData_L2, date);

            for (int index = 1; index <= tree[3]; index++) {
              addNode_file(connection, sFile + index, index, nodeData_L3, date, fileData);
              countNodes++;
            }

            connection.commit();
            connection = getConnection();

            log.info("Node " + i + " - " + j + " - " + k + " - " + "[1..." + tree[3] + "] add");
          }

          localEnd = System.currentTimeMillis();

          log.info("\tThe time of adding of " + tree[2] * tree[3] + " nodes: "
              + ((localEnd - localStart) / 1000.0) + " sec");
          log.info("\tTotal adding time " + countNodes + " nodes: " + ((localEnd - start) / 1000.0)
              + " sec");
        }
      } catch (Exception e) {
        connection.rollback();
        log.error(">>>>>>>>>>>---------- Upload data Exception ----------<<<<<<<<<<<<", e);
      }
    }

    end = System.currentTimeMillis();
    log.info("The time of the adding of " + countNodes + " nodes: " + ((end - start) / 1000.0)
        + " sec");
  }

  public void uploadDataTh() {
    int tree[] = getTree(mapConfig.get("-tree"));

    Thread[] threads = new Thread[tree[0]];
    DataUploaderTh[] uploaderThs = new DataUploaderTh[tree[0]];

    for (int i = 0; i < threads.length; i++) {
      uploaderThs[i] = new DataUploaderTh(args, workspaceDataContainer, rootTestNode, i + 1);
      threads[i] = new Thread(uploaderThs[i]);
    }

    for (int i = 0; i < threads.length; i++)
      threads[i].start();
    // threads[0].start();
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

                NodeImpl sourceNode = getNode(n_4, "jcr:content");
                InputStream fis = getProperty(sourceNode, "jcr:data").getStream();

                Value[] t = getProperty(n_4, "dc:title").getValues();
                String tit = t[0].getString();

                Value[] d = getProperty(n_4, "dc:description").getValues();
                String des = d[0].getString();

                log.info("--->>> Node " + mapConfig.get("-root") + "/" + (sName + i) + "/"
                    + (sName + j) + "/" + (sName + k) + "/" + (sFile + s) + " exist : "
                    + fis.available() + " b   |   dc:title --> " + tit
                    + "   |   dc:description --> " + des);

              }
            }
          }
        } catch (Exception e) {
          log.error(">>>>>>>>>>>---------- Read data Exception ----------<<<<<<<<<<<<", e);
        }
      }
    }
  }

  private TransientNodeData creteNodeData_nt_folder(String name, int orderNum, NodeImpl parentNode) {
    InternalQName[] mixinTypeNames = new InternalQName[0];

    InternalQName iQName = new InternalQName(Constants.NS_DEFAULT_URI, name);

    QPath path = QPath.makeChildPath(parentNode.getInternalPath(), iQName);

    AccessControlList acl = new AccessControlList();

    String uuid = IdGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, Constants.NT_FOLDER,
        mixinTypeNames, orderNum, parentNode.getInternalIdentifier(), acl);

    return nodeData;
  }

  protected TransientNodeData addNode(WorkspaceStorageConnection con, String name, int orderNum,
      NodeImpl parentNode, Calendar date) throws Exception {
    TransientNodeData nodeData = creteNodeData_nt_folder(name, orderNum, parentNode);
    con.add(nodeData);

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FOLDER));
    con.add(primaryTypeData);

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), IdGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getIdentifier(), false);
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

    String uuid = IdGenerator.generate();

    TransientNodeData nodeData = new TransientNodeData(path, uuid, -1, primaryType, mixinTypeNames,
        orderNum, parentNode.getIdentifier(), acl);

    return nodeData;
  }

  protected TransientNodeData addNode(WorkspaceStorageConnection con, String name, int orderNum,
      TransientNodeData parentNode, Calendar date) throws Exception {
    TransientNodeData nodeData = creteNodeData(new InternalQName(Constants.NS_DEFAULT_URI, name),
        orderNum, parentNode, Constants.NT_FOLDER, null);
    con.add(nodeData);

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FOLDER));
    con.add(primaryTypeData);

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), IdGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getIdentifier(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);

    return nodeData;
  }

  protected void addNode_file(WorkspaceStorageConnection con, String name, int orderNum,
      TransientNodeData parentNode, Calendar date, TransientValueData fData) throws Exception {

    TransientNodeData nodeData = creteNodeData(new InternalQName(Constants.NS_DEFAULT_URI, name),
        orderNum, parentNode, Constants.NT_FILE, DCPropertyQName.dcElementSet);
    con.add(nodeData);

    TransientPropertyData primaryTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), false);
    primaryTypeData.setValue(new TransientValueData(Constants.NT_FILE));
    con.add(primaryTypeData);

    TransientPropertyData createdData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_CREATED), IdGenerator.generate(), -1, PropertyType.DATE,
        nodeData.getIdentifier(), false);
    createdData.setValue(new TransientValueData(date));
    con.add(createdData);

    TransientPropertyData mixinTypeData = new TransientPropertyData(QPath.makeChildPath(nodeData
        .getQPath(), Constants.JCR_MIXINTYPES), IdGenerator.generate(), -1, PropertyType.NAME,
        nodeData.getIdentifier(), true);
    mixinTypeData.setValue(new TransientValueData(DCPropertyQName.dcElementSet));
    con.add(mixinTypeData);

    TransientNodeData contentNode = creteNodeData(Constants.JCR_CONTENT, 0, nodeData,
        Constants.NT_RESOURCE, null);
    con.add(contentNode);

    TransientPropertyData primaryTypeContenNode = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_PRIMARYTYPE), IdGenerator.generate(), -1,
        PropertyType.NAME, contentNode.getIdentifier(), false);
    primaryTypeContenNode.setValue(new TransientValueData(Constants.NT_RESOURCE));
    con.add(primaryTypeContenNode);

    TransientPropertyData uuidPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_UUID), IdGenerator.generate(), -1,
        PropertyType.STRING, contentNode.getIdentifier(), false);
    uuidPropertyData.setValue(new TransientValueData(IdGenerator.generate()));
    con.add(uuidPropertyData);

    TransientPropertyData mimeTypePropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_MIMETYPE), IdGenerator.generate(), -1,
        PropertyType.STRING, contentNode.getIdentifier(), false);
    mimeTypePropertyData.setValue(new TransientValueData(sMimeType/* "image/tiff" */));
    con.add(mimeTypePropertyData);

    TransientPropertyData lastModifiedPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_LASTMODIFIED), IdGenerator.generate(), -1,
        PropertyType.DATE, contentNode.getIdentifier(), false);
    lastModifiedPropertyData.setValue(new TransientValueData(date));
    con.add(lastModifiedPropertyData);

    TransientPropertyData dataPropertyData = new TransientPropertyData(QPath.makeChildPath(
        contentNode.getQPath(), Constants.JCR_DATA), IdGenerator.generate(), -1,
        PropertyType.BINARY, contentNode.getIdentifier(), false);
    dataPropertyData.setValue(fData);
    con.add(dataPropertyData);

    addDcElementSet(con, nodeData);
  }

  private void addDcElementSet(WorkspaceStorageConnection con, TransientNodeData nodeData)
      throws Exception {

    addDCProperty(con, nodeData, DCPropertyQName.dcTitle, "T123456789");
    addDCProperty(con, nodeData, DCPropertyQName.dcCreator, "C123456789");
    addDCProperty(con, nodeData, DCPropertyQName.dcSubject, "S123456789");
    addDCProperty(con, nodeData, DCPropertyQName.dcDescription, "D123456789");
    addDCProperty(con, nodeData, DCPropertyQName.dcPublisher, "P123456789");
  }

  private void addDCProperty(WorkspaceStorageConnection con, TransientNodeData dcNode,
      InternalQName propertyQName, String propertyContent) throws Exception {

    TransientPropertyData dcPropertyData = new TransientPropertyData(QPath.makeChildPath(dcNode
        .getQPath(), propertyQName), IdGenerator.generate(), -1, PropertyType.STRING, dcNode
        .getIdentifier(), true);
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

  public Property getProperty(NodeImpl node, String relPath) throws PathNotFoundException,
      RepositoryException {
    JCRPath itemPath = locationFactory.createJCRPath(node.getLocation(), relPath);

    Item prop = dataManager.getItem(itemPath.getInternalPath(), true);
    if (prop == null || prop.isNode())
      throw new PathNotFoundException("Property not found " + itemPath.getAsString(false));

    return (Property) prop;
  }

  protected int[] getTree(String sTree) {
    String[] masTree = sTree.split("-");
    int iTree[] = new int[4];
    for (int i = 0; i < 4; i++)
      iTree[i] = Integer.valueOf(masTree[i]).intValue();

    return iTree;
  }

  protected WorkspaceStorageConnection getConnection() throws Exception {
    return workspaceDataContainer.openConnection();
  }

  public WorkspaceDataContainerBase getWorkspaceDataContainer() {
    return workspaceDataContainer;
  }

  private HashMap<String, String> parceCommandLine(String[] args) {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("-conf", "");
    map.put("-root", "");
    map.put("-tree", "");
    map.put("-vdfile", "");
    map.put("-repo", "");
    map.put("-ws", "");
    map.put("-readtree", "false");
    map.put("-read", "");
    map.put("-readdc", "false");
    map.put("-threads", "1");
    map.put("-iteration", "1");
    map.put("-concurrent", "false");
    map.put("-mimeType", "image/tiff");
    map.put("-api", "false");

    for (int i = 0; i < args.length; i++) {
      String[] params = args[i].split("=");
      if (!map.containsKey(params[0]))
        log.error("Error: " + params[0] + " unknown parameter");

      map.remove(params[0]);
      map.put(params[0], params[1]);

      log.info(params[0] + " = " + params[1]);
    }

    return map;
  }

}
