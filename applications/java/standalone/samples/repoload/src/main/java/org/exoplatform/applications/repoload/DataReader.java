/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.applications.repoload;

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.WorkspaceImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 12.04.2007
 * 11:42:21
 * 
 * @version $Id: DataReader.java 12.04.2007 11:42:21 rainfox
 */
public class DataReader {
  private Log log = ExoLogger.getLogger("repload.DataReader");

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

  protected String                     sReadOnly;

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

  private int                          ntFolderCount;

  private int                          ntFileCount;

  public DataReader(String[] args) {

    this.mapConfig = parceCommandLine(args);
    this.args = args;
    try {
      this.initRepository();
    } catch (Exception e) {
      log.error("Error: Can not initialize repository", e);
    }

    try {
      this.readDataAPI();
      // this.readData();
    } catch (/* Repository */Exception e) {
      log.error("Read error", e);
    }
  }

  public void initRepository() throws Exception {
    sConf = mapConfig.get("-conf");
    sRepository = mapConfig.get("-repo");
    sWorkspace = mapConfig.get("-ws");
    sRoot = mapConfig.get("-root");
    sVdfile = mapConfig.get("-vdfile");
    sReadTree = mapConfig.get("-readtree");
    sReadOnly = mapConfig.get("-read");

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
        log.info("--->>> perository");

      session = repository.login(credentials, sWorkspace);
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
      log.error("Testroot is not found : " + sRoot, e);
    }

    DCPropertyQName.dcElementSet = locationFactory.parseJCRName("dc:elementSet").getInternalName();
    DCPropertyQName.dcTitle = locationFactory.parseJCRName("dc:title").getInternalName();
    DCPropertyQName.dcCreator = locationFactory.parseJCRName("dc:creator").getInternalName();
    DCPropertyQName.dcSubject = locationFactory.parseJCRName("dc:subject").getInternalName();
    DCPropertyQName.dcDescription = locationFactory.parseJCRName("dc:description")
        .getInternalName();
    DCPropertyQName.dcPublisher = locationFactory.parseJCRName("dc:publisher").getInternalName();
  }

  @Deprecated
  public void readData() {

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
                  + fis.available() + " b   |   dc:title --> " + tit + "   |   dc:description --> "
                  + des);

            }
          }
        }
      } catch (Exception e) {
        log.error(">>>>>>>>>>>---------- Read data Exception ----------<<<<<<<<<<<<", e);
      }
    }
  }

  public void readDataAPI() throws RepositoryException {
    readChilds(rootTestNode);
  }

  public void readChilds(Node parent) throws RepositoryException {
    NodeIterator ni = parent.getNodes();
    
    String primaryType = parent.getPrimaryNodeType().getName();
    
    if (primaryType.equals("nt:folder")) {
      ntFolderCount++;
      log.info("\t" + ntFolderCount + " nt:folder has been raed");
    } else if (primaryType.equals("nt:file")) {
      ntFileCount++;
      log.info("\t" + ntFileCount + " nt:file has been raed");
      showDCProperty(parent);
    }

    if (ni.hasNext()) {
      while (ni.hasNext()) {
        Node n1 = ni.nextNode();
        readChilds(n1);
      }

    } else {
      showProperty(parent);
    }
  }

  public void showDCProperty(Node parent) throws RepositoryException {
    PropertyIterator pi = parent.getProperties();

    String sMix = parent.getPrimaryNodeType().getName();

    if (sMix.equals("nt:file")) {

      for (NodeType mt : parent.getMixinNodeTypes()) 
        sMix += " " + mt.getName();
      
      log.info(sMix + " " + parent.getPath());
      
      if(mapConfig.get("-readdc").equals("true")){
        String[] dcprop = {"dc:title", "dc:creator", "dc:subject", "dc:description", "dc:publisher"}; 
        
        for (int i = 0; i < dcprop.length; i++) {
          Property propdc = parent.getProperty(dcprop[i]);
          
          String s = propdc.getValues()[0].getString();
          
          log.info("\t\t" + propdc.getName() + " " + PropertyType.nameFromValue(propdc.getType()) + " "
              + s);
        }
      }
    }
      
  }
  
  public void showProperty(Node parent) throws RepositoryException {
    PropertyIterator pi = parent.getProperties();

    String sMix = parent.getPrimaryNodeType().getName();

    if (sMix.equals("nt:resource")) {

      for (NodeType mt : parent.getMixinNodeTypes()) {
        sMix += " " + mt.getName();
      }

      log.info(sMix + " " + parent.getPath());

      while (pi.hasNext()) {
        Property prop = pi.nextProperty();
        if (prop.getType() == PropertyType.BINARY) {
          log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()));
        } else {
          String s = prop.getString();
          if (s.length() > 64)
            s = s.substring(0, 64);
          log.info("\t\t" + prop.getName() + " " + PropertyType.nameFromValue(prop.getType()) + " "
              + s);
        }
      }
    }
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

  private HashMap<String, String> parceCommandLine(String[] args) {
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("-conf", "");
    map.put("-root", "");
    map.put("-tree", "");
    map.put("-vdfile", "");
    map.put("-repo", "");
    map.put("-ws", "");
    map.put("-readtree", "");
    map.put("-read", "");
    map.put("-readdc", "false");

    for (int i = 0; i < args.length; i++) {
      String[] params = args[i].split("=");
      if (!map.containsKey(params[0]))
        log.info("Error: " + params[0] + " unknown parameter");

      map.remove(params[0]);
      if (params.length > 1)
        map.put(params[0], params[1]);
      else if (params[0].equals("-readdc"))
             map.put(params[0], "true");
           else
             map.put(params[0], "");
      
      
        
      

      if (params.length > 1)
        log.info(params[0] + " = " + params[1]);
    }

    return map;
  }

}
