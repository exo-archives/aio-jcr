/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

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
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.observation.ObservationManagerImpl;
import org.exoplatform.services.jcr.impl.core.observation.ObservationManagerRegistry;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataMoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionInterceptor;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.impl.xml.ExportImportFactory;
import org.exoplatform.services.jcr.impl.xml.ImportRespectingSemantics;
import org.exoplatform.services.jcr.impl.xml.XmlConstants;
import org.exoplatform.services.jcr.impl.xml.XmlMapping;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.impl.xml.exporting.ExportXmlBase;
import org.exoplatform.services.jcr.impl.xml.importing.StreamImporter;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: SessionImpl.java 13866 2007-03-28 13:39:28Z ksm $ The
 *          implementation supported CredentialsImpl
 */
public class SessionImpl implements Session, NamespaceAccessor {

  private final Log log = ExoLogger.getLogger("jcr.SessionImpl");

  private final RepositoryImpl repository;

  private final CredentialsImpl credentials;

  private final WorkspaceImpl workspace;

  protected final SessionDataManager nodesManager;

  private final Map<String, String> namespaces;
  private final Map<String, String> prefixes;

  private final AccessManager accessManager;

  private final LocationFactory locationFactory;

  private final ValueFactoryImpl valueFactory;

  private final ExoContainer container;

  private final LocationFactory systemLocationFactory;

  private final LockManagerImpl lockManager;

  private final String workspaceName;

  private boolean live;

  private final List<SessionLifecycleListener> lifecycleListeners;

  private final SessionFactory sessionFactory;

  private final String id;

  private final SessionActionInterceptor actionHandler;

  private long lastAccessTime;

  private final SessionRegistry sessionRegistry;

  public SessionImpl(String workspaceName, Credentials credentials, ExoContainer container) throws RepositoryException {

    this.workspaceName = workspaceName;
    this.container = container;
    this.live = true;
    this.id = IdGenerator.generate();

    this.repository = (RepositoryImpl) container.getComponentInstanceOfType(RepositoryImpl.class);
    this.systemLocationFactory = (LocationFactory) container.getComponentInstanceOfType(LocationFactory.class);

    this.accessManager = (AccessManager) container.getComponentInstanceOfType(AccessManager.class);
    this.lockManager = (LockManagerImpl) container
        .getComponentInstanceOfType(LockManagerImpl.class);
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
    this.prefixes =  new LinkedHashMap<String, String>();

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

    SessionActionCatalog catalog = (SessionActionCatalog) container.getComponentInstanceOfType(SessionActionCatalog.class);
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
    this.sessionRegistry.unregisterSession(getId());
    this.live = false;
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
  public RepositoryImpl getRepository() {
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
    else if (credentials instanceof SimpleCredentials) {
      String name = ((SimpleCredentials) credentials).getUserID();
      char[] pswd = ((SimpleCredentials) credentials).getPassword();
      CredentialsImpl thisCredentials = new CredentialsImpl(name, pswd);
      return sessionFactory.createSession(thisCredentials);
    } else
      throw new LoginException("Credentials for the authentication should be CredentialsImpl or SimpleCredentials type");
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
    Item item = nodesManager.getItemByIdentifier(Constants.ROOT_UUID, true);
    if (item != null && item.isNode()) {
      return (NodeImpl) item;
    }

    throw new ItemNotFoundException("Node not found " + JCRPath.ROOT_PATH + " at " + workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getNodeByUUID(java.lang.String)
   */
  public NodeImpl getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getNodeByUUID(" + uuid + ") >>>>>");

    try {
      Item item = nodesManager.getItemByIdentifier(uuid, true);
  
      if (item != null && item.isNode()) {
        NodeImpl node = (NodeImpl) item;
        node.getUUID(); // throws exception
        return node;
      }
  
      throw new ItemNotFoundException("Node not found " + uuid + " at " + workspaceName);
    } finally {
      if (log.isDebugEnabled())
        log.debug("getNodeByUUID(" + uuid + ") <<<<< " + ((System.currentTimeMillis() - start)/1000d) + "sec");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getItem(java.lang.String)
   */
  public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getItem(" + absPath + ") >>>>>");
    
    try {
      JCRPath loc = locationFactory.parseAbsPath(absPath);
  
      ItemImpl item = nodesManager.getItem(loc.getInternalPath(), true);
      if (item != null)
        return item;
  
      throw new PathNotFoundException("Item not found " + absPath + " in workspace " + workspaceName);
    } finally {
      if (log.isDebugEnabled())
        log.debug("getItem(" + absPath + ") <<<<< " + ((System.currentTimeMillis() - start)/1000d) + "sec");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#itemExists(java.lang.String)
   */
  public boolean itemExists(String absPath) {
    try {
      if (getItem(absPath) != null)
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
  public void save() throws AccessDeniedException,
      LockException,
      ConstraintViolationException,
      InvalidItemStateException,
      RepositoryException {
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
  public ValueFactoryImpl getValueFactory() throws UnsupportedRepositoryOperationException,
      RepositoryException {
    return valueFactory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getNamespacePrefix(java.lang.String)
   */
  public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
    if (prefixes.containsKey(uri)) {
      return prefixes.get(uri);
    }
    return workspace.getNamespaceRegistry().getPrefix(uri);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#setNamespacePrefix(java.lang.String,
   *      java.lang.String)
   */
  public void setNamespacePrefix(String prefix, String uri) throws NamespaceException,
      RepositoryException {
    NamespaceRegistryImpl nrg = (NamespaceRegistryImpl) workspace.getNamespaceRegistry();
    if (!nrg.isUriRegistered(uri))
      throw new NamespaceException("The specified uri:" + uri + " is not among "
          + "those registered in the NamespaceRegistry");
    if (nrg.isPrefixMaped(prefix))
      throw new NamespaceException("A prefix '" + prefix + "' is currently already mapped to "
          + nrg.getURI(prefix) + " URI persistently in the repository NamespaceRegistry "
          + "and cannot be remapped to a new URI using this method, since this would make any "
          + "content stored using the old URI unreadable.");
    if (namespaces.containsKey(prefix))
      throw new NamespaceException("A prefix '" + prefix + "' is currently already mapped to "
          + namespaces.get(prefix) + " URI transiently within this Session and cannot be "
          + "remapped to a new URI using this method, since this would make any "
          + "content stored using the old URI unreadable.");
    nrg.validateNamespace(prefix, uri);
    namespaces.put(prefix, uri);
    prefixes.put(uri, prefix);
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
        if (!prefixes.containsKey(workspace.getNamespaceRegistry().getURI(permanentPrefixes[i]))) {
          allPrefixes.add(permanentPrefixes[i]);
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
    if (namespaces.size() > 0) {
      uri = namespaces.get(prefix);
      if (uri != null)
        return uri;
    }
//    uri = ;
//    if (namespaces.values().contains(uri))
//      return null;
    return workspace.getNamespaceRegistry().getURI(prefix);
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
  public void exportSystemView(String absPath,
      ContentHandler contentHandler,
      boolean skipBinary,
      boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {

    ExportXmlBase exporter = new ExportImportFactory(this).getExportVisitor(XmlMapping.SYSVIEW,
        contentHandler,
        skipBinary,
        noRecurse);

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
    ItemData srcItemData = nodesManager.getItemData(srcNodePath.getInternalPath());
    if (srcItemData == null) {
      throw new PathNotFoundException("No node exists at " + absPath);
    }

    try {
      exporter.export((NodeData) srcItemData);
    } catch (Exception e) {
      if (e instanceof RepositoryException)
        throw (RepositoryException) e;
      else if (e instanceof SAXException)
        throw (SAXException) e;
      else
        throw new RepositoryException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportSystemView(java.lang.String,
   *      java.io.OutputStream, boolean, boolean)
   */
  public void exportSystemView(String absPath,
      OutputStream out,
      boolean skipBinary,
      boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException {

    ExportXmlBase exporter = new ExportImportFactory(this).getExportVisitor(XmlMapping.SYSVIEW,
        out,
        skipBinary,
        noRecurse);

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
    ItemData srcItemData = nodesManager.getItemData(srcNodePath.getInternalPath());

    if (srcItemData == null) {
      throw new PathNotFoundException("No node exists at " + absPath);
    }

    try {
      exporter.export((NodeData) srcItemData);
    } catch (Exception e) {
      if (e instanceof RepositoryException)
        throw (RepositoryException) e;
      else if (e instanceof IOException)
        throw (IOException) e;
      else
        throw new RepositoryException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportDocumentView(java.lang.String,
   *      org.xml.sax.ContentHandler, boolean, boolean)
   */
  public void exportDocumentView(String absPath,
      ContentHandler contentHandler,
      boolean skipBinary,
      boolean noRecurse) throws InvalidSerializedDataException,
      PathNotFoundException,
      SAXException,
      RepositoryException {

    ExportXmlBase exporter = new ExportImportFactory(this).getExportVisitor(XmlMapping.DOCVIEW,
        contentHandler,
        skipBinary,
        noRecurse);

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
    ItemData srcItemData = nodesManager.getItemData(srcNodePath.getInternalPath());

    if (srcItemData == null) {
      throw new PathNotFoundException("No node exists at " + absPath);
    }

    try {
      exporter.export((NodeData) srcItemData);
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof RepositoryException)
        throw (RepositoryException) e;
      else if (e instanceof SAXException)
        throw (SAXException) e;
      else
        throw new RepositoryException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#exportDocumentView(java.lang.String,
   *      java.io.OutputStream, boolean, boolean)
   */
  public void exportDocumentView(String absPath,
      OutputStream out,
      boolean skipBinary,
      boolean noRecurse) throws InvalidSerializedDataException,
      IOException,
      PathNotFoundException,
      RepositoryException {

    ExportXmlBase exporter = new ExportImportFactory(this).getExportVisitor(XmlMapping.DOCVIEW,
        out,
        skipBinary,
        noRecurse);

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
    ItemData srcItemData = nodesManager.getItemData(srcNodePath.getInternalPath());

    if (srcItemData == null) {
      throw new PathNotFoundException("No node exists at " + absPath);
    }

    try {
      exporter.export((NodeData) srcItemData);
    } catch (Exception e) {
      if (e instanceof RepositoryException) {
        e.printStackTrace();
        throw (RepositoryException) e;
      } else if (e instanceof IOException) {
        e.printStackTrace();
        throw (IOException) e;
      } else {
        e.printStackTrace();
        throw new RepositoryException(e);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#importXML(java.lang.String, java.io.InputStream,
   *      int)
   */
  public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException,
      PathNotFoundException,
      ItemExistsException,
      ConstraintViolationException,
      InvalidSerializedDataException,
      RepositoryException {

    NodeImpl node = (NodeImpl) getItem(parentAbsPath);
    // TODO it's not a place for this, checked-in check
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

    ImportRespectingSemantics respectingSemantics = (ImportRespectingSemantics) getAttribute(XmlConstants.PARAMETER_IMPORT_RESPECTING);
    
    if (respectingSemantics == null) {
      respectingSemantics = ImportRespectingSemantics.IMPORT_SEMANTICS_RESPECT;
    }
    
    StreamImporter importer = new ExportImportFactory(this).getStreamImporter(XmlSaveType.SESSION,
                                                                              node,
                                                                              uuidBehavior,
                                                                              respectingSemantics);
    importer.importStream(in);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getImportContentHandler(java.lang.String, int)
   */
  public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException,
      ConstraintViolationException,
      VersionException,
      RepositoryException {
    NodeImpl node = (NodeImpl) getItem(parentAbsPath);
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

    ImportRespectingSemantics respectingSemantics = (ImportRespectingSemantics) getAttribute(XmlConstants.PARAMETER_IMPORT_RESPECTING);

    if (respectingSemantics == null) {
      respectingSemantics = ImportRespectingSemantics.IMPORT_SEMANTICS_RESPECT;
    }

    return new ExportImportFactory(this).getImportHandler(XmlSaveType.SESSION,
                                                          node,
                                                          uuidBehavior,
                                                          respectingSemantics);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#move(java.lang.String, java.lang.String)
   */
  public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException,
      PathNotFoundException,
      VersionException,
      LockException,
      RepositoryException {

    JCRPath srcNodePath = getLocationFactory().parseAbsPath(srcAbsPath);
    NodeImpl srcNode = (NodeImpl) nodesManager.getItem(srcNodePath.getInternalPath(), true);
    JCRPath destNodePath = getLocationFactory().parseAbsPath(destAbsPath);
    if (destNodePath.isIndexSetExplicitly())
      throw new RepositoryException("The relPath provided must not have an index on its final element. "
          + destNodePath.getAsString(false));

    NodeImpl destParentNode = (NodeImpl) nodesManager.getItem(destNodePath.makeParentPath().getInternalPath(), true);

    if (srcNode == null || destParentNode == null) {
      throw new PathNotFoundException("No node exists at " + srcAbsPath
          + " or no node exists one level above " + destAbsPath);
    }

    destParentNode.validateChildNode(destNodePath.getName().getInternalName(),
        ((ExtendedNodeType) srcNode.getPrimaryNodeType()).getQName());

    // Check for node with destAbsPath name in session
    NodeImpl destNode = (NodeImpl) nodesManager.getItem((NodeData) destParentNode.getData(),
        new QPathEntry(destNodePath.getInternalPath().getName(), 0),
        true);

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
        destNodePath.getName().getInternalName(),
        getWorkspace().getNodeTypeManager(),
        getTransientNodesManager(),
        true);

    srcNode.getData().accept(initializer);

    // deleting nodes
    getTransientNodesManager().getChangesLog().addAll(initializer.getItemDeletedStates(true));
    // [PN] 06.01.07 Reindex same-name siblings after deletion
    getTransientNodesManager().getChangesLog().addAll(getTransientNodesManager()
        .reindexSameNameSiblings(srcNode.nodeData(), getTransientNodesManager()));

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
    getLockManager().addLockToken(getId(), lt);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#getLockTokens()
   */
  public String[] getLockTokens() {
    return getLockManager().getLockTokens(getId());
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Session#removeLockToken(java.lang.String)
   */
  public void removeLockToken(String lt) {
    getLockManager().removeLockToken(getId(), lt);
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

  public LocationFactory getLocationFactory() {
    return locationFactory;
  }

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
   * For debug purpose! 
   * Can accessed by admin only, otherwise null will be returned
   */
  public ExoContainer getContainer() {
    return container;
  }

  public SessionActionInterceptor getActionHandler() {
    return actionHandler;
  }

  public long getLastAccessTime() {
    return lastAccessTime;

  }

  public void updateLastAccessTime() {
    lastAccessTime = System.currentTimeMillis();
  }
}