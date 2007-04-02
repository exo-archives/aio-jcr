/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.VersionException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.observation.ObservationManagerImpl;
import org.exoplatform.services.jcr.impl.core.observation.ObservationManagerRegistry;
import org.exoplatform.services.jcr.impl.core.value.BinaryValue;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataMoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionInterceptor;
import org.exoplatform.services.jcr.impl.util.StringConverter;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.impl.xml.NodeImporter;
import org.exoplatform.services.jcr.impl.xml.SysExportXmlVisior;
import org.exoplatform.services.jcr.impl.xml.XMLWriter;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: SessionImpl.java 13866 2007-03-28 13:39:28Z ksm $ The
 *          implementation supported CredentialsImpl
 */
public class SessionImpl implements Session, NamespaceAccessor {

  protected static Log log = ExoLogger.getLogger("jcr.SessionImpl");

  private RepositoryImpl repository;

  private CredentialsImpl credentials;

  private WorkspaceImpl workspace;

  protected SessionDataManager nodesManager;

  private Map<String, String> namespaces;

  private AccessManager accessManager;

  private LocationFactory locationFactory;

  private ValueFactoryImpl valueFactory;

  private ExoContainer container;

  private LocationFactory systemLocationFactory;

  private LockManagerImpl lockManager;

  private String workspaceName;

  private boolean live;

  //private HashSet<String> lockTokens;

  private List<SessionLifecycleListener> lifecycleListeners;

  private SessionFactory sessionFactory;

  private final String id;

  private SessionActionInterceptor actionHandler;

  private long lastAccessTime;

  private SessionRegistry sessionRegistry;
  private static long count = 0;
  SessionImpl(String workspaceName, Credentials credentials, ExoContainer container)
      throws RepositoryException {

    this.workspaceName = workspaceName;
    this.container = container;
    this.live = true;
    this.id = UUIDGenerator.generate();
    //this.lockTokens = new HashSet<String>();

    this.repository = (RepositoryImpl) container.getComponentInstanceOfType(RepositoryImpl.class);
    this.systemLocationFactory = (LocationFactory) container
        .getComponentInstanceOfType(LocationFactory.class);
//    this.uuidGenerator = (UUIDGenerator) container.getComponentInstanceOfType(UUIDGenerator.class);
    this.accessManager = (AccessManager) container.getComponentInstanceOfType(AccessManager.class);
    this.lockManager = (LockManagerImpl) container.getComponentInstanceOfType(LockManagerImpl.class);
    this.sessionFactory = (SessionFactory) container
        .getComponentInstanceOfType(SessionFactory.class);
    RepositoryEntry repositoryConfig = (RepositoryEntry) container
        .getComponentInstanceOfType(RepositoryEntry.class);
    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container
        .getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);
    
    this.credentials = (CredentialsImpl) credentials;
    this.locationFactory = new LocationFactory(this);
    this.valueFactory = new ValueFactoryImpl(locationFactory, repositoryConfig, cleanerHolder);
    this.namespaces = new LinkedHashMap<String, String>();

    // Observation manager per session
    ObservationManagerRegistry observationManagerRegistry = (ObservationManagerRegistry) container
        .getComponentInstanceOfType(ObservationManagerRegistry.class);
    ObservationManager observationManager = observationManagerRegistry
        .createObservationManager(this);

    LocalWorkspaceDataManagerStub workspaceDataManager = (LocalWorkspaceDataManagerStub) container
        .getComponentInstanceOfType(LocalWorkspaceDataManagerStub.class);

    this.nodesManager = new SessionDataManager(this, workspaceDataManager);

    this.workspace = new WorkspaceImpl(workspaceName, container, this, observationManager);

    this.lifecycleListeners = new ArrayList<SessionLifecycleListener>();
    this.registerLifecycleListener((ObservationManagerImpl) observationManager);
    this.registerLifecycleListener(lockManager);

    SessionActionCatalog catalog = (SessionActionCatalog) container
      .getComponentInstanceOfType(SessionActionCatalog.class); 
    actionHandler = new SessionActionInterceptor(catalog, container);
    
        
    sessionRegistry = (SessionRegistry) container.getComponentInstanceOfType(SessionRegistry.class);
    
    sessionRegistry.registerSession(this);
    
    this.lastAccessTime = System.currentTimeMillis();
  }
  

  public String getSessionInfo() {
    return getUserID() + "@" + workspaceName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getUserID()
   */
  public String getUserID() {
    return credentials.getUserID();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) {
    return credentials.getAttribute(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getAttributeNames()
   */
  public String[] getAttributeNames() {
    return credentials.getAttributeNames();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#logout()
   */
  public void logout() {
    for (int i = 0; i < lifecycleListeners.size(); i++) {
      lifecycleListeners.get(i).onCloseSession(this);
    }
    sessionRegistry.unregisterSession(getId());
    live = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#isLive()
   */
  public boolean isLive() {
    return live;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getRepository()
   */
  public Repository getRepository() {
    return repository;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#impersonate(javax.jcr.Credentials)
   */
  public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
    if (credentials instanceof CredentialsImpl)
      return sessionFactory.createSession((CredentialsImpl) credentials);
    // return new SessionImpl(workspaceName, credentials, container);
    else if (credentials instanceof SimpleCredentials) {
      String name = ((SimpleCredentials) credentials).getUserID();
      char[] pswd = ((SimpleCredentials) credentials).getPassword();
      CredentialsImpl thisCredentials = new CredentialsImpl(name, pswd);
      return sessionFactory.createSession(thisCredentials);
      // return new SessionImpl(workspaceName, thisCredentials, container);
    } else
      throw new LoginException(
          "Credentials for the authentication should be CredentialsImpl or SimpleCredentials type");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getWorkspace()
   */
  public WorkspaceImpl getWorkspace() {
    return workspace;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getRootNode()
   */
  public Node getRootNode() throws RepositoryException {
    return (Node) getItem(JCRPath.ROOT_PATH);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getNodeByUUID(java.lang.String)
   */
  public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
    Item item = nodesManager.getItemByUUID(uuid, true);
    // .getAccessibleItemByUUID(uuid);

    if (item != null && item.isNode()) {
      Node node = (Node) item;
      node.getUUID(); // throws exception
      return node;
    }

    throw new ItemNotFoundException("Node not found " + uuid + " at " + workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getItem(java.lang.String)
   */
  public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
    JCRPath loc = locationFactory.parseAbsPath(absPath);
    ItemImpl item = nodesManager.getItem(loc.getInternalPath(), true);
    // .getAccessibleItem(loc);
    if (item != null)
      return item;

    throw new PathNotFoundException("Item not found " + absPath + " in workspace " + workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#itemExists(java.lang.String)
   */
  public boolean itemExists(String absPath) {
    try {
      if (nodesManager.getItem(locationFactory.parseAbsPath(absPath).getInternalPath(), true) != null)
        // getAccessibleItem(locationFactory.parseAbsPath(absPath)) != null)
        return true;
    } catch (RepositoryException e) {
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#save()
   */
  public void save() throws AccessDeniedException, LockException, ConstraintViolationException,
      InvalidItemStateException, RepositoryException {
    getRootNode().save();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#refresh(boolean)
   */
  public void refresh(boolean keepChanges) throws RepositoryException {
    getRootNode().refresh(keepChanges);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#hasPendingChanges()
   */
  public boolean hasPendingChanges() throws RepositoryException {
    return nodesManager.hasPendingChanges(Constants.ROOT_PATH);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getValueFactory()
   */
  public ValueFactoryImpl getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
    return valueFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getNamespacePrefix(java.lang.String)
   */
  public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {

    if (namespaces.values().contains(uri)) {
      // if so, then we return last setted prefix for this uri
      String[] keys = namespaces.keySet().toArray(new String[namespaces.size()]);
      for (int i = keys.length - 1; i >= 0; i--) {
        String key = keys[i];
        String value = namespaces.get(key);
        if (value.equals(uri)) {
          return key;
        }
      }
    }
    // String[] prefixes = getNamespacePrefixes();
    String[] prefixes = workspace.getNamespaceRegistry().getPrefixes();
    for (int i = 0; i < prefixes.length; i++) {
      try {
        String prefixUri = workspace.getNamespaceRegistry().getURI(prefixes[i]);
        if (prefixUri != null && prefixUri.equals(uri)) {
          return prefixes[i];
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    throw new NamespaceException("Prefix for " + uri + " not found");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#setNamespacePrefix(java.lang.String,
   *      java.lang.String)
   */
  public void setNamespacePrefix(String prefix, String uri) throws NamespaceException,
      RepositoryException {
    ((NamespaceRegistryImpl) workspace.getNamespaceRegistry()).validateNamespace(prefix, uri);
    String testPrefix = workspace.getNamespaceRegistry().getPrefix(uri);
    if (testPrefix.equals(prefix)) // no needs to remap
      return;
    try {
      workspace.getNamespaceRegistry().getURI(prefix);
    } catch (NamespaceException e) {
      namespaces.put(prefix, uri);
      return;
    }
    throw new NamespaceException("Prefix " + prefix + " is already mapped to other uri");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getNamespacePrefixes()
   */
  public String[] getNamespacePrefixes() throws RepositoryException {
    Collection<String> allPrefixes = new LinkedList<String>();
    allPrefixes.addAll(namespaces.keySet());
    String[] permanentPrefixes = workspace.getNamespaceRegistry().getPrefixes();
    for (int i = 0; i < permanentPrefixes.length; i++) {
      // log.info("PREFIX Session.getnamespaceprefixes "+permanentPrefixes[i]);
      String permanentPrefix = permanentPrefixes[i];
      String uri = null;
      try {
        uri = workspace.getNamespaceRegistry().getURI(permanentPrefix);
        if (!allPrefixes.contains(permanentPrefix) && !(namespaces.values().contains(uri))) {
          allPrefixes.add(permanentPrefix);
        }
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
    return allPrefixes.toArray(new String[allPrefixes.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getNamespaceURI(java.lang.String)
   */
  public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
    String uri = null;
    // look in session first
    uri = namespaces.get(prefix);
    if (uri != null)
      return uri;
    uri = workspace.getNamespaceRegistry().getURI(prefix);
    if (namespaces.values().contains(uri))
      return null;
    if (uri == null)
      throw new NamespaceException("No namespace '" + uri + "' found");
    return uri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespaceURIByPrefix(java.lang.String)
   */
  public String getNamespaceURIByPrefix(String prefix) throws NamespaceException,
      RepositoryException {
    return getNamespaceURI(prefix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespacePrefixByURI(java.lang.String)
   */
  public String getNamespacePrefixByURI(String uri) throws NamespaceException, RepositoryException {
    return getNamespacePrefix(uri);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getAllNamespacePrefixes()
   */
  public String[] getAllNamespacePrefixes() throws RepositoryException {
    return getNamespacePrefixes();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportSystemView(java.lang.String,
   *      org.xml.sax.ContentHandler, boolean, boolean)
   */
  public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary,
      boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {
    SysExportXmlVisior exporter = new SysExportXmlVisior(contentHandler, this,
        getTransientNodesManager(), skipBinary, noRecurse);

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
    ItemData srcItemData = nodesManager.getItemData(srcNodePath.getInternalPath());

    if (srcItemData == null) {
      throw new PathNotFoundException("No node exists at " + absPath);
    }
    exporter.export((NodeData) srcItemData);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportSystemView(java.lang.String,
   *      java.io.OutputStream, boolean, boolean)
   */
  public void exportSystemView(String absPath, OutputStream out, boolean skipBinary,
      boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException {
    TransformerHandler handler;
    try {
      SAXTransformerFactory saxFact = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
      handler = saxFact.newTransformerHandler();
      handler.setResult(new StreamResult(out));
      // need to solve trouble with XMLQuery
      // handler.getTransformer().setOutputProperty(
      // OutputKeys.OMIT_XML_DECLARATION, "yes");
    } catch (javax.xml.transform.TransformerFactoryConfigurationError ex) {
      throw new IOException(ex.getMessage());
    } catch (javax.xml.transform.TransformerConfigurationException ex) {
      throw new IOException(ex.getMessage());
    }

    try {
      exportSystemView(absPath, handler, skipBinary, noRecurse);
    } catch (SAXException ex) {
      throw new RepositoryException("Error on export", ex);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportDocumentView(java.lang.String,
   *      org.xml.sax.ContentHandler, boolean, boolean)
   */
  public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary,
      boolean noRecurse) throws InvalidSerializedDataException, PathNotFoundException,
      SAXException, RepositoryException {

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
    NodeImpl srcNode = (NodeImpl) nodesManager.getItem(srcNodePath.getInternalPath(), true);


    XMLWriter writer = new XMLWriter(this);
    //    
    initNodeAsDocView(srcNode, writer, skipBinary, noRecurse);
    //    
    invokeHandler(writer.getBytes(), contentHandler);
    

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportDocumentView(java.lang.String,
   *      java.io.OutputStream, boolean, boolean)
   */
  public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary,
      boolean noRecurse) throws InvalidSerializedDataException, IOException, PathNotFoundException,
      RepositoryException {
// TODO should we save it to pool?
//    NodeImpl node = (NodeImpl) nodesManager.getItem(locationFactory.parseAbsPath(absPath)
//        .getInternalPath(), true);
//
//    XMLWriter writer = new XMLWriter(this);
//    initNodeAsDocView(node, writer, skipBinary, noRecurse);
//    out.write(writer.getBytes());
//    out.flush();
    
    SAXTransformerFactory stf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

    try {
        TransformerHandler th = stf.newTransformerHandler();
        th.setResult(new StreamResult(out));
        th.getTransformer().setParameter(OutputKeys.METHOD, "xml");
        th.getTransformer().setParameter(OutputKeys.ENCODING, "UTF-8");
        th.getTransformer().setParameter(OutputKeys.INDENT, "no");

        exportDocumentView(absPath, th, skipBinary, noRecurse);
    } catch (TransformerException te) {
        throw new RepositoryException(te);
    } catch (SAXException se) {
        throw new RepositoryException(se);
    }
    
    
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#importXML(java.lang.String, java.io.InputStream,
   *      int)
   */
  public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException,
      PathNotFoundException, ItemExistsException, ConstraintViolationException,
      InvalidSerializedDataException, RepositoryException {

    try {
      NodeImporter importer = (NodeImporter) getImportContentHandler(parentAbsPath, uuidBehavior);
      
      importer.parse(in);
      // update changes cache in manager
      // with question for SysView import???
      // node.setState(ItemChangeState.ADDED); // ADDED has been
      // nodesManager.update(node);
      // nodesManager.update(ItemState.createUpdatedState(node.getData()),
      // true);
    } catch (IOException e) {
      // e.printStackTrace();
      throw new InvalidSerializedDataException("importXML failed", e);
    } catch (SAXException e) {
      // e.printStackTrace();
      // throw new InvalidSerializedDataException("importXML failed", e);
      Throwable rootCause = e.getException();
      if (rootCause == null) {
        rootCause = getRootCauseException(e);
      }
      if (rootCause == null) {
        rootCause = e;
      }
      // rootCause.printStackTrace();
      if (rootCause instanceof ItemExistsException) {
        throw new ItemExistsException("importXML failed", rootCause);
      } else if (rootCause instanceof ConstraintViolationException) {
        throw new ConstraintViolationException("importXML failed", rootCause);
      } else {
        throw new InvalidSerializedDataException("importXML failed", e);
      }
      /*
       * if (uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW) {
       * throw new ItemExistsException("importXML failed", e); } else { throw
       * new InvalidSerializedDataException("importXML failed", e); }
       */
    } catch (ParserConfigurationException e) {
      throw new InvalidSerializedDataException("importXML failed", e);
    }
  }

  public Throwable getRootCauseException(Throwable e) {
    Throwable ex = e;
    while (ex.getCause() != null) {
      ex = ex.getCause();
    }
    return ex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getImportContentHandler(java.lang.String, int)
   */
  public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior)
      throws PathNotFoundException, ConstraintViolationException, VersionException,
      RepositoryException {

    NodeImpl node = (NodeImpl) getItem(parentAbsPath);
    checkNodeImport(node);

    NodeImporter handler = new NodeImporter(node);
    handler.setUuidBehavior(uuidBehavior);
    handler.setSaveType(NodeImporter.SAVETYPE_UPDATE);
    return handler;
  }

  private void checkNodeImport(NodeImpl node) throws VersionException,
      ConstraintViolationException, LockException, RepositoryException {
    // checked-in check
    if (!node.isCheckedOut()) {
      throw new VersionException("Node " + node.getPath()
          + " or its nearest ancestor is checked-in");
    }

    // Check if node is not protected
    if (node.getDefinition().isProtected()) {
      throw new ConstraintViolationException("Can't add protected node " + node.getName() + " to "
          + node.getParent().getPath());
    }

    // Check locking
    if (!node.checkLocking()) {
      throw new LockException("Node " + node.getPath() + " is locked ");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#move(java.lang.String, java.lang.String)
   */
  public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException,
      PathNotFoundException, VersionException, LockException, RepositoryException {

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(srcAbsPath);
    NodeImpl srcNode = (NodeImpl) nodesManager.getItem(srcNodePath.getInternalPath(), true);

    JCRPath destNodePath = getLocationFactory().parseAbsPath(destAbsPath);
    if (destNodePath.isIndexSetExplicitly())
      throw new RepositoryException(
          "The relPath provided must not have an index on its final element. "
              + destNodePath.getAsString(false));

    NodeImpl destParentNode = (NodeImpl) nodesManager.getItem(
        destNodePath.makeParentPath().getInternalPath(), true);

    if (srcNode == null || destParentNode == null) {
      throw new PathNotFoundException("No node exists at " + srcAbsPath
          + " or no node exists one level above " + destAbsPath);
    }

    destParentNode.validateChildNode(destNodePath.getName().getInternalName(),
        ((ExtendedNodeType) srcNode.getPrimaryNodeType()).getQName());

    // Check for node with destAbsPath name in session
    NodeImpl destNode = (NodeImpl) nodesManager.getItem(destNodePath.getInternalPath(), true);

    if (destNode != null) {
      if (!destNode.getDefinition().allowsSameNameSiblings()) {
        // Throw exception
        String msg = "A node with this name (" + destAbsPath + ") is already exists. ";
        throw new ItemExistsException(msg);
      }
    }

    // Check if versionable ancestor is not checked-in
    if (!srcNode.getParent().isCheckedOut())
      throw new VersionException("Parent or source Node or its nearest ancestor is checked-in");

    if (!srcNode.checkLocking())
      throw new LockException("Source parent node " + srcNode.getPath() + " is locked ");

    ItemDataMoveVisitor initializer = new ItemDataMoveVisitor((NodeData) destParentNode.getData(),
        destNodePath.getName().getInternalName(), getWorkspace().getNodeTypeManager(),
        getTransientNodesManager(), true);
        //getTransientNodesManager(), srcNode.isNodeType(Constants.MIX_REFERENCEABLE));

    srcNode.getData().accept(initializer);
    
    // TODO [PN] 06.01.07 Don't use SDM.getChangesLog()
    
    // deleting nodes
    getTransientNodesManager().getChangesLog().addAll(initializer.getItemDeletedStates(true));
    // getTransientNodesManager().getChangesLog().dump()
    // [PN] 06.01.07 Reindex same-name siblings after deletion
    getTransientNodesManager().getChangesLog().addAll(
        getTransientNodesManager().reindexSameNameSiblings(srcNode.nodeData(), getTransientNodesManager()));
    
    List<ItemState> itemStates = initializer.getItemAddStates();
    for (ItemState itemState : itemStates) {
      getTransientNodesManager().update(itemState, true);
    }
  }

  // //////////////////// OPTIONAL

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#checkPermission(java.lang.String, java.lang.String)
   */
  public void checkPermission(String absPath, String actions) throws AccessControlException {

    try {
      JCRPath jcrPath = locationFactory.parseAbsPath(absPath);
      AccessControlList acl = nodesManager.getACL(jcrPath.getInternalPath());
      if (!accessManager.hasPermission(acl, actions, getUserID()))
        throw new AccessControlException("Permission denied " + absPath + " : " + actions);
    } catch (RepositoryException e) {
      throw new AccessControlException("Could not check permission for " + absPath + " " + e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#addLockToken(java.lang.String)
   */
  public void addLockToken(String lt) {
    //lockTokens.add(lt);
    getLockManager().addLockToken(getId(),lt );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getLockTokens()
   */
  public String[] getLockTokens() {
//    String[] tokens = new String[lockTokens.size()];
//    lockTokens.toArray(tokens);
    return getLockManager().getLockTokens(getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#removeLockToken(java.lang.String)
   */
  public void removeLockToken(String lt) {
    //lockTokens.remove(lt);
    getLockManager().removeLockToken(getId(),lt);
  }

  // //////////////////// IMPL

  public SessionDataManager getTransientNodesManager() {
    return this.nodesManager;
  }

  public Credentials getCredentials() {
    return this.credentials;
  }

  LocationFactory getSystemLocationFactory() {
    return systemLocationFactory;
  }

  public LockManagerImpl getLockManager() {
    return lockManager;
  }

  public void registerLifecycleListener(SessionLifecycleListener listener) {
    this.lifecycleListeners.add(listener);
  }
//  @Deprecated
//  boolean hasLockToken(String lt) {
//    return false;
//
//    //return lockTokens.contains(lt);
//  }
//  @Deprecated
//  public String getLockToken(String lt) {
////    for (Iterator<String> it = lockTokens.iterator(); it.hasNext();) {
////      String token = it.next();
////      if (token.equals(lt))
////        return token;
////    }
//    return null;
//  }

  // Helper for for node export
  private void initNodeAsDocView(NodeImpl node, XMLWriter writer, boolean skipBinary,
      boolean noRecurse) throws RepositoryException {

    String name = node.getName();
    if (name.length() == 0) // root node
      name = "jcr:root";

    Properties attrs = new Properties();

    for (PropertyImpl prop: node.childProperties()) {

      String[] strPropValues = getStrPropValues(prop, skipBinary);
      if (strPropValues == null || strPropValues.length == 0) // skip
        continue;
      String strValues = "";
      for (int i = 0; i < strPropValues.length; i++) {
        strValues += strPropValues[i];
        if (i < strPropValues.length - 1 && strPropValues[i].length() > 0)
          // space as delimiter for multi-valued (not applied for skipBinary)
          strValues += " ";
        // System.out.println(">>> PROP >>>> "+prop.getPath()+" "+strValues);
      }
      attrs.setProperty(prop.getName(), strValues);
    }
    writer.startElement(node.getLocation().getName(), attrs);

    NodeIterator nodes = node.getNodes();
    while (nodes.hasNext()) {
      NodeImpl child = (NodeImpl) nodes.nextNode();
      if (!noRecurse) {
        if (child.getLocation().getName().getInternalName().equals(Constants.JCR_XMLTEXT)) {
          try {
            // TODO jcr:xmlcharacters
            String val = StringConverter.normalizeString(child.getProperty("jcr:xmlcharacters")
                .getString(), false);
            // System.out.println(">>> TEXT >>>> "+child.getPath()+" "+val);
            writer.writeText(val);
            continue;
          } catch (ValueFormatException e) {
            // will init as element
          } catch (PathNotFoundException e) {
            // will init as element
          }
        }
        initNodeAsDocView(child, writer, skipBinary, noRecurse);
      }

    }

    writer.endElement();
  }

  private void invokeHandler(byte[] input, ContentHandler contentHandler) throws SAXException,
      RepositoryException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      //factory.setNamespaceAware(true);
      // System.out.println(" factory.isNamespaceAware() "
      // + factory.isNamespaceAware());
      SAXParser parser = factory.newSAXParser();

      XMLReader reader = parser.getXMLReader();

      reader.setContentHandler(contentHandler);
      reader.setFeature("http://apache.org/xml/features/allow-java-encodings",true);

      // System.out.println(" Session.invokeHandler: " + new String(input));
      // saveAsFile(input, "d:/tmp/export_test_sys.xml");

      reader.parse(new InputSource(new ByteArrayInputStream(input)));

    } catch (SAXException e) {
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RepositoryException("Can not invoke content handler", e);
    }
  }

  private String[] getStrPropValues(PropertyImpl prop, boolean skipBinary)
      throws ValueFormatException, RepositoryException {

    String[] values = new String[prop.getValueArray().length];
    if (values.length == 0)
      return null;

    if (prop.getType() == PropertyType.BINARY) {
      if (skipBinary) {
        for (int i = 0; i < values.length; i++)
          values[i] = "";
      } else {
        for (int i = 0; i < values.length; i++) {
          try {
            ValueData data = ((BinaryValue) prop.getValueArray()[i]).getInternalData();
//            String b64s = new String(Base64.encodeBase64(BLOBUtil.readValue(data)));
//            values[i] = b64s;
            values[i] = new String(Base64.encodeBase64(data.getAsByteArray()));
          } catch (IOException e) {
            throw new RepositoryException("Can't export value data to string: " + e.getMessage(), e);
          }
        }
      }
    } else {
      for (int i = 0; i < values.length; i++) {
        Value val = prop.getValueArray()[i];
        try {
          if (val != null)
            values[i] = StringConverter.normalizeString(val.getString(), true);
        } catch (ValueFormatException e) {
          if (!e.getMessage().equals("empty value")) {
            throw e;
          }
        }
      }
    }
    return values;
  }

  /**
   * @return Returns the locationFactory.
   */
  public LocationFactory getLocationFactory() {
    return locationFactory;
  }

//  /**
//   * @return Returns the uuidGenerator.
//   */
//  public UUIDGenerator getUuidGenerator() {
//    return uuidGenerator;
//  }

  /**
   * @return Returns the accessManager.
   */
  public AccessManager getAccessManager() {
    return accessManager;
  }

  public String getId() {
    return id;
  }

  /**
   * For debug purpose Can accessed by admin only, otherwise null will be
   * returned [PN] 26.09.06
   */
  public ExoContainer getContainer() throws RepositoryException {
    boolean hasPerm = getAccessManager().hasPermission(((NodeImpl) getRootNode()).getACL(),
        PermissionType.ALL, getUserID());
    return hasPerm ? container : null;
  }

  public SessionActionInterceptor getActionHandler() {
    return actionHandler;
  }
  public long getLastAccessTime(){
    return  lastAccessTime;

  }

  public void updateLastAccessTime(){
    lastAccessTime = System.currentTimeMillis();
  }
}