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
package org.exoplatform.services.jcr.impl.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.VersionException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.NamespaceAccessor;
import org.exoplatform.services.jcr.core.SessionLifecycleListener;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
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
import org.exoplatform.services.jcr.impl.xml.ItemDataKeeperAdapter;
import org.exoplatform.services.jcr.impl.xml.XmlMapping;
import org.exoplatform.services.jcr.impl.xml.exporting.BaseXmlExporter;
import org.exoplatform.services.jcr.impl.xml.importing.ContentImporter;
import org.exoplatform.services.jcr.impl.xml.importing.StreamImporter;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: SessionImpl.java 14244 2008-05-14 11:44:54Z ksm $ The implementation supported
 *          CredentialsImpl
 */
public class SessionImpl implements ExtendedSession, NamespaceAccessor {

  private final RepositoryImpl                 repository;

  private final ConversationState              userState;

  private final WorkspaceImpl                  workspace;

  private final Map<String, String>            namespaces;

  private final Map<String, String>            prefixes;

  private final AccessManager                  accessManager;

  private final LocationFactory                locationFactory;

  private final ValueFactoryImpl               valueFactory;

  private final ExoContainer                   container;

  private final LocationFactory                systemLocationFactory;

  private final LockManagerImpl                lockManager;

  protected final String                       workspaceName;

  private boolean                              live;

  private final List<SessionLifecycleListener> lifecycleListeners;

  // private final SessionFactory sessionFactory;

  private final String                         id;

  private final SessionActionInterceptor       actionHandler;

  private long                                 lastAccessTime;

  private final SessionRegistry                sessionRegistry;

  protected final SessionDataManager           dataManager;

  public SessionImpl(String workspaceName, ConversationState userState, ExoContainer container) throws RepositoryException {

    this.workspaceName = workspaceName;
    this.container = container;
    this.live = true;
    this.id = IdGenerator.generate();
    this.userState = userState;

    this.repository = (RepositoryImpl) container.getComponentInstanceOfType(RepositoryImpl.class);
    this.systemLocationFactory = (LocationFactory) container.getComponentInstanceOfType(LocationFactory.class);

    this.accessManager = (AccessManager) container.getComponentInstanceOfType(AccessManager.class);
    this.lockManager = (LockManagerImpl) container.getComponentInstanceOfType(LockManagerImpl.class);
    WorkspaceEntry wsConfig = (WorkspaceEntry) container.getComponentInstanceOfType(WorkspaceEntry.class);
    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container.getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);

    this.locationFactory = new LocationFactory(this);
    this.valueFactory = new ValueFactoryImpl(locationFactory, wsConfig, cleanerHolder);

    this.namespaces = new LinkedHashMap<String, String>();
    this.prefixes = new LinkedHashMap<String, String>();

    // Observation manager per session
    ObservationManagerRegistry observationManagerRegistry = (ObservationManagerRegistry) container.getComponentInstanceOfType(ObservationManagerRegistry.class);
    ObservationManager observationManager = observationManagerRegistry.createObservationManager(this);

    LocalWorkspaceDataManagerStub workspaceDataManager = (LocalWorkspaceDataManagerStub) container.getComponentInstanceOfType(LocalWorkspaceDataManagerStub.class);

    this.dataManager = new SessionDataManager(this, workspaceDataManager);

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

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#addLockToken(java.lang.String)
   */
  public void addLockToken(String lt) {
    getLockManager().addLockToken(getId(), lt);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#checkPermission(java.lang.String, java.lang.String)
   */
  public void checkPermission(String absPath, String actions) throws AccessControlException {

    try {
      JCRPath jcrPath = locationFactory.parseAbsPath(absPath);
      AccessControlList acl = dataManager.getACL(jcrPath.getInternalPath());
      if (!accessManager.hasPermission(acl, actions, getUserState().getIdentity()))
        throw new AccessControlException("Permission denied " + absPath + " : " + actions);
    } catch (RepositoryException e) {
      throw new AccessControlException("Could not check permission for " + absPath + " " + e);
    }
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#exportDocumentView(java.lang.String, org.xml.sax.ContentHandler,
   * boolean, boolean)
   */
  public void exportDocumentView(String absPath,
                                 ContentHandler contentHandler,
                                 boolean skipBinary,
                                 boolean noRecurse) throws InvalidSerializedDataException,
                                                   PathNotFoundException,
                                                   SAXException,
                                                   RepositoryException {

    LocationFactory factory = new LocationFactory(((NamespaceRegistryImpl) repository.getNamespaceRegistry()));

    WorkspaceEntry wsConfig = (WorkspaceEntry) container.getComponentInstanceOfType(WorkspaceEntry.class);

    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container.getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);

    ValueFactoryImpl valueFactoryImpl = new ValueFactoryImpl(factory, wsConfig, cleanerHolder);

    try {
      BaseXmlExporter exporter = new ExportImportFactory().getExportVisitor(XmlMapping.DOCVIEW,
                                                                            contentHandler,
                                                                            skipBinary,
                                                                            noRecurse,
                                                                            getTransientNodesManager(),
                                                                            repository.getNamespaceRegistry(),
                                                                            valueFactoryImpl);

      JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
      ItemData srcItemData = dataManager.getItemData(srcNodePath.getInternalPath());

      if (srcItemData == null) {
        throw new PathNotFoundException("No node exists at " + absPath);
      }

      exporter.export((NodeData) srcItemData);

    } catch (XMLStreamException e) {
      throw new SAXException(e);
    }
  }

  public void exportDocumentView(String absPath,
                                 OutputStream out,
                                 boolean skipBinary,
                                 boolean noRecurse) throws InvalidSerializedDataException,
                                                   IOException,
                                                   PathNotFoundException,
                                                   RepositoryException {

    LocationFactory factory = new LocationFactory(((NamespaceRegistryImpl) repository.getNamespaceRegistry()));

    WorkspaceEntry wsConfig = (WorkspaceEntry) container.getComponentInstanceOfType(WorkspaceEntry.class);

    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container.getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);

    ValueFactoryImpl valueFactoryImpl = new ValueFactoryImpl(factory, wsConfig, cleanerHolder);

    try {
      BaseXmlExporter exporter = new ExportImportFactory().getExportVisitor(XmlMapping.DOCVIEW,
                                                                            out,
                                                                            skipBinary,
                                                                            noRecurse,
                                                                            getTransientNodesManager(),
                                                                            repository.getNamespaceRegistry(),
                                                                            valueFactoryImpl);

      JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
      ItemData srcItemData = dataManager.getItemData(srcNodePath.getInternalPath());

      if (srcItemData == null) {
        throw new PathNotFoundException("No node exists at " + absPath);
      }

      exporter.export((NodeData) srcItemData);
    } catch (XMLStreamException e) {
      throw new IOException(e.getLocalizedMessage());
    } catch (SAXException e) {
      throw new IOException(e.getLocalizedMessage());
    }
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#exportSystemView(java.lang.String, org.xml.sax.ContentHandler, boolean,
   * boolean)
   */
  public void exportWorkspaceSystemView(OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException,
                                                                                                PathNotFoundException,
                                                                                                RepositoryException {
    LocationFactory factory = new LocationFactory(((NamespaceRegistryImpl) repository.getNamespaceRegistry()));

    WorkspaceEntry wsConfig = (WorkspaceEntry) container.getComponentInstanceOfType(WorkspaceEntry.class);

    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container.getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);

    ValueFactoryImpl valueFactoryImpl = new ValueFactoryImpl(factory, wsConfig, cleanerHolder);

    try {
      BaseXmlExporter exporter = new ExportImportFactory().getExportVisitor(XmlMapping.BACKUP,
                                                                            out,
                                                                            skipBinary,
                                                                            noRecurse,
                                                                            getTransientNodesManager(),
                                                                            repository.getNamespaceRegistry(),
                                                                            valueFactoryImpl);

      ItemData srcItemData = dataManager.getItemData(Constants.ROOT_UUID);
      if (srcItemData == null) {
        throw new PathNotFoundException("Root node not found");
      }

      exporter.export((NodeData) srcItemData);
    } catch (XMLStreamException e) {
      throw new IOException(e.getLocalizedMessage());
    } catch (SAXException e) {
      throw new IOException(e.getLocalizedMessage());
    }
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#exportSystemView(java.lang.String, org.xml.sax.ContentHandler, boolean,
   * boolean)
   */
  public void exportSystemView(String absPath,
                               ContentHandler contentHandler,
                               boolean skipBinary,
                               boolean noRecurse) throws PathNotFoundException,
                                                 SAXException,
                                                 RepositoryException {
    LocationFactory factory = new LocationFactory(((NamespaceRegistryImpl) repository.getNamespaceRegistry()));

    WorkspaceEntry wsConfig = (WorkspaceEntry) container.getComponentInstanceOfType(WorkspaceEntry.class);

    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container.getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);

    ValueFactoryImpl valueFactoryImpl = new ValueFactoryImpl(factory, wsConfig, cleanerHolder);
    try {
      BaseXmlExporter exporter = new ExportImportFactory().getExportVisitor(XmlMapping.SYSVIEW,
                                                                            contentHandler,
                                                                            skipBinary,
                                                                            noRecurse,
                                                                            getTransientNodesManager(),
                                                                            repository.getNamespaceRegistry(),
                                                                            valueFactoryImpl);

      JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
      ItemData srcItemData = dataManager.getItemData(srcNodePath.getInternalPath());
      if (srcItemData == null) {
        throw new PathNotFoundException("No node exists at " + absPath);
      }

      exporter.export((NodeData) srcItemData);

    } catch (XMLStreamException e) {
      throw new SAXException(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#exportSystemView(java.lang.String, java.io.OutputStream, boolean,
   * boolean)
   */
  public void exportSystemView(String absPath,
                               OutputStream out,
                               boolean skipBinary,
                               boolean noRecurse) throws IOException,
                                                 PathNotFoundException,
                                                 RepositoryException {
    LocationFactory factory = new LocationFactory(((NamespaceRegistryImpl) repository.getNamespaceRegistry()));

    WorkspaceEntry wsConfig = (WorkspaceEntry) container.getComponentInstanceOfType(WorkspaceEntry.class);

    WorkspaceFileCleanerHolder cleanerHolder = (WorkspaceFileCleanerHolder) container.getComponentInstanceOfType(WorkspaceFileCleanerHolder.class);

    ValueFactoryImpl valueFactoryImpl = new ValueFactoryImpl(factory, wsConfig, cleanerHolder);
    try {
      BaseXmlExporter exporter = new ExportImportFactory().getExportVisitor(XmlMapping.SYSVIEW,
                                                                            out,
                                                                            skipBinary,
                                                                            noRecurse,
                                                                            getTransientNodesManager(),
                                                                            repository.getNamespaceRegistry(),
                                                                            valueFactoryImpl);

      JCRPath srcNodePath = getLocationFactory().parseAbsPath(absPath);
      ItemData srcItemData = dataManager.getItemData(srcNodePath.getInternalPath());

      if (srcItemData == null) {
        throw new PathNotFoundException("No node exists at " + absPath);
      }

      exporter.export((NodeData) srcItemData);
    } catch (XMLStreamException e) {
      throw new IOException(e.getLocalizedMessage());
    } catch (SAXException e) {
      throw new IOException(e.getLocalizedMessage());
    }
  }

  /**
   * @return Returns the accessManager.
   */
  public AccessManager getAccessManager() {
    return accessManager;
  }

  public SessionActionInterceptor getActionHandler() {
    return actionHandler;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.NamespaceAccessor#getAllNamespacePrefixes()
   */
  public String[] getAllNamespacePrefixes() throws RepositoryException {
    return getNamespacePrefixes();
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) {
    return userState.getAttribute(name);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getAttributeNames()
   */
  public String[] getAttributeNames() {

    Set<String> attributes = userState.getAttributeNames();

    String[] names = new String[attributes.size()];
    int i = 0;
    for (String name : attributes)
      names[i++] = name;
    return names;
  }

  /**
   * For debug purpose! Can accessed by admin only, otherwise null will be returned
   * 
   * @deprecated use WorkspaceContainerFacade instead of using container directly
   */
  public ExoContainer getContainer() {
    return container;
  }

  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getImportContentHandler(java.lang.String, int)
   */
  public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws

  PathNotFoundException, ConstraintViolationException, VersionException, RepositoryException {
    NodeImpl node = (NodeImpl) getItem(parentAbsPath);
    // checked-in check
    if (!node.checkedOut()) {
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

    Map<String, Object> context = new HashMap<String, Object>();
    context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, true);

    return new ExportImportFactory().getImportHandler(((NodeData) node.getData()),
                                                      uuidBehavior,
                                                      new ItemDataKeeperAdapter(getTransientNodesManager()),
                                                      getTransientNodesManager(),
                                                      getWorkspace().getNodeTypeManager(),
                                                      getLocationFactory(),
                                                      getValueFactory(),
                                                      getWorkspace().getNamespaceRegistry(),
                                                      getAccessManager(),
                                                      userState,
                                                      context,
                                                      (RepositoryImpl) getRepository(),
                                                      getWorkspace().getName());
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getItem(java.lang.String)
   */
  public ItemImpl getItem(String absPath) throws PathNotFoundException, RepositoryException {

    JCRPath loc = locationFactory.parseAbsPath(absPath);

    ItemImpl item = dataManager.getItem(loc.getInternalPath(), true);
    if (item != null)
      return item;

    throw new PathNotFoundException("Item not found " + absPath + " in workspace " + workspaceName);
  }

  public long getLastAccessTime() {
    return lastAccessTime;

  }

  public LocationFactory getLocationFactory() {
    return locationFactory;
  }

  public LockManagerImpl getLockManager() {
    return lockManager;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getLockTokens()
   */
  public String[] getLockTokens() {
    return getLockManager().getLockTokens(getId());
  }

  /*
   * (non-Javadoc)
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
   * @see
   * org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespacePrefixByURI(java.lang.String)
   */
  public String getNamespacePrefixByURI(String uri) throws NamespaceException, RepositoryException {
    return getNamespacePrefix(uri);
  }

  /*
   * (non-Javadoc)
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
    // uri = ;
    // if (namespaces.values().contains(uri))
    // return null;
    return workspace.getNamespaceRegistry().getURI(prefix);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.NamespaceAccessor#getNamespaceURIByPrefix(java.lang.String)
   */
  public String getNamespaceURIByPrefix(String prefix) throws NamespaceException,
                                                      RepositoryException {
    return getNamespaceURI(prefix);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getNodeByUUID(java.lang.String)
   */
  public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
    Item item = dataManager.getItemByIdentifier(uuid, true);

    if (item != null && item.isNode()) {
      NodeImpl node = (NodeImpl) item;
      node.getUUID(); // throws exception
      return node;
    }

    throw new ItemNotFoundException("Node not found " + uuid + " at " + workspaceName);

  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getRepository()
   */
  public Repository getRepository() {
    return repository;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getRootNode()
   */
  public Node getRootNode() throws RepositoryException {
    Item item = dataManager.getItemByIdentifier(Constants.ROOT_UUID, true);
    if (item != null && item.isNode()) {
      return (NodeImpl) item;
    }

    throw new ItemNotFoundException("Node not found " + JCRPath.ROOT_PATH + " at " + workspaceName);
  }

  public SessionDataManager getTransientNodesManager() {
    return this.dataManager;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getUserID()
   */
  public String getUserID() {
    return userState.getIdentity().getUserId();
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getValueFactory()
   */
  public ValueFactoryImpl getValueFactory() throws UnsupportedRepositoryOperationException,
                                           RepositoryException {
    return valueFactory;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#getWorkspace()
   */
  public WorkspaceImpl getWorkspace() {
    return workspace;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#hasPendingChanges()
   */
  public boolean hasPendingChanges() throws RepositoryException {
    return dataManager.hasPendingChanges(Constants.ROOT_PATH);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#impersonate(javax.jcr.Credentials)
   */
  public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {

    String name;
    if (credentials instanceof CredentialsImpl) {
      name = ((CredentialsImpl) credentials).getUserID();
    } else if (credentials instanceof SimpleCredentials) {
      name = ((SimpleCredentials) credentials).getUserID();
    } else
      throw new LoginException("Credentials for the authentication should be CredentialsImpl or SimpleCredentials type");

    SessionFactory sessionFactory = (SessionFactory) container.getComponentInstanceOfType(SessionFactory.class);

    ConversationState newState = new ConversationState(new Identity(name,
                                                                    userState.getIdentity()
                                                                             .getMemberships(),
                                                                    userState.getIdentity()
                                                                             .getRoles()));
    return (Session) sessionFactory.createSession(newState);

  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#importXML(java.lang.String, java.io.InputStream, int)
   */
  public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException,
                                                                               PathNotFoundException,
                                                                               ItemExistsException,
                                                                               ConstraintViolationException,
                                                                               InvalidSerializedDataException,
                                                                               RepositoryException {
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, true);

    importXML(parentAbsPath, in, uuidBehavior, context);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ExtendedSession#importXML(java.lang.String,
   * java.io.InputStream, int, boolean)
   */
  public void importXML(String parentAbsPath,
                        InputStream in,
                        int uuidBehavior,
                        boolean respectPropertyDefinitionsConstraints) throws IOException,
                                                                      PathNotFoundException,
                                                                      ItemExistsException,
                                                                      ConstraintViolationException,
                                                                      InvalidSerializedDataException,
                                                                      RepositoryException {
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS,
                respectPropertyDefinitionsConstraints);
    importXML(parentAbsPath, in, uuidBehavior, context);

  }

  public void importXML(String parentAbsPath,
                        InputStream in,
                        int uuidBehavior,
                        Map<String, Object> context) throws IOException,
                                                    PathNotFoundException,
                                                    ItemExistsException,
                                                    ConstraintViolationException,
                                                    InvalidSerializedDataException,
                                                    RepositoryException {
    NodeImpl node = (NodeImpl) getItem(parentAbsPath);
    if (!node.checkedOut()) {
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

    StreamImporter importer = new ExportImportFactory().getStreamImporter(((NodeData) node.getData()),
                                                                          uuidBehavior,
                                                                          new ItemDataKeeperAdapter(getTransientNodesManager()),
                                                                          getTransientNodesManager(),
                                                                          getWorkspace().getNodeTypeManager(),
                                                                          getLocationFactory(),
                                                                          getValueFactory(),
                                                                          getWorkspace().getNamespaceRegistry(),
                                                                          getAccessManager(),
                                                                          userState,
                                                                          context,
                                                                          (RepositoryImpl) getRepository(),
                                                                          getWorkspace().getName());
    importer.importStream(in);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#isLive()
   */
  public boolean isLive() {
    return live;
  }

  /*
   * (non-Javadoc)
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
   * @see javax.jcr.Session#logout()
   */
  public void logout() {
    for (int i = 0; i < lifecycleListeners.size(); i++) {
      lifecycleListeners.get(i).onCloseSession(this);
    }
    this.sessionRegistry.unregisterSession(getId());
    this.live = false;
  }

  public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException,
                                                         PathNotFoundException,
                                                         VersionException,
                                                         LockException,
                                                         RepositoryException {
    JCRPath srcNodePath = getLocationFactory().parseAbsPath(srcAbsPath);

    NodeImpl srcNode = (NodeImpl) dataManager.getItem(srcNodePath.getInternalPath(), false);
    JCRPath destNodePath = getLocationFactory().parseAbsPath(destAbsPath);
    if (destNodePath.isIndexSetExplicitly())
      throw new RepositoryException("The relPath provided must not have an index on its final element. "
          + destNodePath.getAsString(false));

    NodeImpl destParentNode = (NodeImpl) dataManager.getItem(destNodePath.makeParentPath()
                                                                         .getInternalPath(), true);

    if (srcNode == null || destParentNode == null) {
      throw new PathNotFoundException("No node exists at " + srcAbsPath
          + " or no node exists one level above " + destAbsPath);
    }

    destParentNode.validateChildNode(destNodePath.getName().getInternalName(),
                                     ((ExtendedNodeType) srcNode.getPrimaryNodeType()).getQName());

    // Check for node with destAbsPath name in session
    NodeImpl destNode = (NodeImpl) dataManager.getItem((NodeData) destParentNode.getData(),
                                                       new QPathEntry(destNodePath.getInternalPath()
                                                                                  .getName(),
                                                                      0),
                                                       false);

    if (destNode != null) {
      if (!destNode.getDefinition().allowsSameNameSiblings()) {
        throw new ItemExistsException("A node with this name (" + destAbsPath
            + ") is already exists. ");
      }
    }

    // Check if versionable ancestor is not checked-in
    if (!srcNode.parent().checkedOut())
      throw new VersionException("Parent or source Node or its nearest ancestor is checked-in");

    if (!srcNode.checkLocking())
      throw new LockException("Source parent node " + srcNode.getPath() + " is locked ");

    ItemDataMoveVisitor initializer = new ItemDataMoveVisitor((NodeData) destParentNode.getData(),
                                                              destNodePath.getName()
                                                                          .getInternalName(),
                                                              getWorkspace().getNodeTypeManager(),
                                                              getTransientNodesManager(),
                                                              true);

    getTransientNodesManager().rename((NodeData) srcNode.getData(), initializer);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#refresh(boolean)
   */
  public void refresh(boolean keepChanges) throws RepositoryException {
    getRootNode().refresh(keepChanges);
  }

  public void registerLifecycleListener(SessionLifecycleListener listener) {
    this.lifecycleListeners.add(listener);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Session#removeLockToken(java.lang.String)
   */
  public void removeLockToken(String lt) {
    getLockManager().removeLockToken(getId(), lt);
  }

  /*
   * (non-Javadoc)
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
   * @see javax.jcr.Session#setNamespacePrefix(java.lang.String, java.lang.String)
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

  public void updateLastAccessTime() {
    lastAccessTime = System.currentTimeMillis();
  }

  LocationFactory getSystemLocationFactory() {
    return systemLocationFactory;
  }

  public ConversationState getUserState() {
    return this.userState;
  }

}
