/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.webdav.common.representation.property.ChildCountRepresentation;
import org.exoplatform.services.webdav.common.representation.property.CreationDateRepresentation;
import org.exoplatform.services.webdav.common.representation.property.CreatorDisplayNameRepresentation;
import org.exoplatform.services.webdav.common.representation.property.DisplayNameRepresentation;
import org.exoplatform.services.webdav.common.representation.property.GetContentLengthRepresentation;
import org.exoplatform.services.webdav.common.representation.property.GetContentTypeRepresentation;
import org.exoplatform.services.webdav.common.representation.property.GetLastModifiedRepresentation;
import org.exoplatform.services.webdav.common.representation.property.HasChildrenRepresentation;
import org.exoplatform.services.webdav.common.representation.property.IsCollectionRepresentation;
import org.exoplatform.services.webdav.common.representation.property.IsFolderRepresentation;
import org.exoplatform.services.webdav.common.representation.property.IsRootRepresentation;
import org.exoplatform.services.webdav.common.representation.property.JcrPropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.property.ParentNameRepresentation;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.property.ResourceTypeRepresentation;
import org.exoplatform.services.webdav.common.representation.property.SupportedMethodSetRepresentation;
import org.exoplatform.services.webdav.common.representation.read.PropFindRequestRepresentation;
import org.exoplatform.services.webdav.common.representation.write.PropertyUpdateRepresentation;
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.config.WebDavConfigImpl;
import org.exoplatform.services.webdav.deltav.representation.property.CheckedInRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.CheckedOutRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.IsVersionedRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.PredecessorSetRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.SuccessorSetRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.VersionHistoryRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.VersionNameRepresentation;
import org.exoplatform.services.webdav.deltav.representation.versiontree.VersionTreeRepresentation;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.exoplatform.services.webdav.lock.representation.LockInfoRepresentation;
import org.exoplatform.services.webdav.lock.representation.property.LockDiscoveryRepresentation;
import org.exoplatform.services.webdav.lock.representation.property.SupportedLockRepresentation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavServiceImpl.java 12853 2007-02-16 16:24:30Z gavrikvetal $
 */

public class WebDavServiceImpl implements WebDavService {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavServiceImpl");
  
  public static final String WEBDAVCONFIG_PATH = "/conf/exo-webdav-config.xml";
  
  protected RepositoryService  repositoryService;
  
  protected ResourceBinder resourceBinder;
  
  protected WebDavConfig config;

  protected FakeLockTable lockTable;
  
  private RequestRepresentationDispatcher requestDispatcher;
  
  private HashMap<String, HashMap<String, String>> documents = new HashMap<String, HashMap<String, String>>();
  
  private HashMap<String, HashMap<String, String>> properties = new HashMap<String, HashMap<String, String>>();  
  
  public WebDavServiceImpl (
      InitParams params, 
      RepositoryService repositoryService,
      ResourceBinder resourceBinder
    ) throws Exception {

    this.repositoryService = repositoryService;
    this.resourceBinder = resourceBinder;
    
    config = new WebDavConfigImpl(params, WEBDAVCONFIG_PATH);

    lockTable = new FakeLockTable();

    /*
     * here registers common properties
     * 
     */
    registerProperty(ChildCountRepresentation.NAMESPACE, ChildCountRepresentation.TAGNAME, ChildCountRepresentation.class.getCanonicalName());    
    registerProperty(GetContentLengthRepresentation.NAMESPACE, GetContentLengthRepresentation.TAGNAME, GetContentLengthRepresentation.class.getCanonicalName());        
    registerProperty(GetContentTypeRepresentation.NAMESPACE, GetContentTypeRepresentation.TAGNAME, GetContentTypeRepresentation.class.getCanonicalName());        
    registerProperty(CreationDateRepresentation.NAMESPACE, CreationDateRepresentation.TAGNAME, CreationDateRepresentation.class.getCanonicalName());    
    registerProperty(CreatorDisplayNameRepresentation.NAMESPACE, CreatorDisplayNameRepresentation.TAGNAME, CreatorDisplayNameRepresentation.class.getCanonicalName());   
    registerProperty(DisplayNameRepresentation.NAMESPACE, DisplayNameRepresentation.TAGNAME, DisplayNameRepresentation.class.getCanonicalName());    
    registerProperty(HasChildrenRepresentation.NAMESPACE, HasChildrenRepresentation.TAGNAME, HasChildrenRepresentation.class.getCanonicalName());    
    registerProperty(IsCollectionRepresentation.NAMESPACE, IsCollectionRepresentation.TAGNAME, IsCollectionRepresentation.class.getCanonicalName());    
    registerProperty(IsFolderRepresentation.NAMESPACE, IsFolderRepresentation.TAGNAME, IsFolderRepresentation.class.getCanonicalName());    
    registerProperty(IsRootRepresentation.NAMESPACE, IsRootRepresentation.TAGNAME, IsRootRepresentation.class.getCanonicalName());    
    registerProperty(GetLastModifiedRepresentation.NAMESPACE, GetLastModifiedRepresentation.TAGNAME, GetLastModifiedRepresentation.class.getCanonicalName());    
    registerProperty(ParentNameRepresentation.NAMESPACE, ParentNameRepresentation.TAGNAME, ParentNameRepresentation.class.getCanonicalName());    
    registerProperty(ResourceTypeRepresentation.NAMESPACE, ResourceTypeRepresentation.TAGNAME, ResourceTypeRepresentation.class.getCanonicalName());    
    registerProperty(SupportedMethodSetRepresentation.NAMESPACE, SupportedMethodSetRepresentation.TAGNAME, SupportedMethodSetRepresentation.class.getCanonicalName());

    /*
     * registering DeltaV properties
     * 
     */    
    registerProperty(CheckedInRepresentation.NAMESPACE, CheckedInRepresentation.TAGNAME, CheckedInRepresentation.class.getCanonicalName());    
    registerProperty(CheckedOutRepresentation.NAMESPACE, CheckedOutRepresentation.TAGNAME, CheckedOutRepresentation.class.getCanonicalName());    
    registerProperty(IsVersionedRepresentation.NAMESPACE, IsVersionedRepresentation.TAGNAME, IsVersionedRepresentation.class.getCanonicalName());    
    registerProperty(PredecessorSetRepresentation.NAMESPACE, PredecessorSetRepresentation.TAGNAME, PredecessorSetRepresentation.class.getCanonicalName());    
    registerProperty(SuccessorSetRepresentation.NAMESPACE, SuccessorSetRepresentation.TAGNAME, SuccessorSetRepresentation.class.getCanonicalName());    
    registerProperty(VersionHistoryRepresentation.NAMESPACE, VersionHistoryRepresentation.TAGNAME, VersionHistoryRepresentation.class.getCanonicalName());    
    registerProperty(VersionNameRepresentation.NAMESPACE, VersionNameRepresentation.TAGNAME, VersionNameRepresentation.class.getCanonicalName());
    
    /*
     * registering LOCK properties 
     * 
     */
    
    registerProperty(SupportedLockRepresentation.NAMESPACE, SupportedLockRepresentation.TAGNAME, SupportedLockRepresentation.class.getCanonicalName());
    registerProperty(LockDiscoveryRepresentation.NAMESPACE, LockDiscoveryRepresentation.TAGNAME, LockDiscoveryRepresentation.class.getCanonicalName());    
    
    /*
     * registers request document representations
     * 
     */    
    registerDocument(DAV_NAMESPACE, PropFindRequestRepresentation.TAGNAME, PropFindRequestRepresentation.class.getCanonicalName());
    registerDocument(DAV_NAMESPACE, PropertyUpdateRepresentation.TAGNAME, PropertyUpdateRepresentation.class.getCanonicalName());
    registerDocument(DAV_NAMESPACE, LockInfoRepresentation.TAGNAME, LockInfoRepresentation.class.getCanonicalName());
    
    /*
     * reports
     */
    registerDocument(DAV_NAMESPACE, VersionTreeRepresentation.TAGNAME, VersionTreeRepresentation.class.getCanonicalName());
    
    requestDispatcher = new RequestRepresentationDispatcher(this);
    
  }
  
  public WebDavConfig getConfig() {
    return config;
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#getAvailableCommands()
   * 
   * uses for retrieve list of all registered webdav commands, whitch uses
   * by OPTIONS command and SupportedMethodSet property 
   * 
   */
  public ArrayList<String> getAvailableCommands() {
    
    ArrayList<String> commands = new ArrayList<String>();
    
    List<ResourceDescriptor> descriptors = resourceBinder.getAllDescriptors();
    for (int i = 0; i < descriptors.size(); i++) {
      ResourceDescriptor descriptor = descriptors.get(i);
      
      String acceptableMethod = descriptor.getAcceptableMethod();
      log.info(">>>>>> METHOD: " + acceptableMethod);
      
      String uriPattern = descriptor.getURIPattern().getString();
      log.info(">>>>>> URI: " + uriPattern);
      
      if (uriPattern.startsWith("/jcr/")) {
        commands.add(acceptableMethod);
      }
    }
    
    return commands;
  }


  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#getRepository(java.lang.String)
   * returns the repository by name
   */
  public ManageableRepository getRepository(String repositoryName) throws RepositoryException {
    try {
      return repositoryService.getRepository(repositoryName);
    } catch (RepositoryConfigurationException exc) {
      log.info("Repository configuration error!");
      throw new RepositoryException(exc.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#getLockTable()
   * 
   * returns the FakeLockTable.
   * this table used for locking not existed resource
   * 
   */
  public FakeLockTable getLockTable() {
    return lockTable;
  }
  
  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#getRequestDispatcher()
   * returns the Request Representation Dispatcher
   * 
   */
  public RequestRepresentationDispatcher getRequestDispatcher() {    
    log.info("try to get request dispatcher...");    
    return requestDispatcher;
  }
  
  public PropertyRepresentation getPropertyRepresentation(String nameSpaceURI, String propertyName) {
    
    if (nameSpaceURI == null || propertyName == null) {
      
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      
      try {
        throw new Exception("NULL!!!!!!!!!!");
      } catch (Exception exc) {
        log.info("NULL!!!!! ", exc);
        log.info("NAMESPACE: " + nameSpaceURI);
        log.info("PROPERTY NAME: " + propertyName);
      }
      
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      
    }
    
    
    HashMap<String, String> nameSpacedMap = properties.get(nameSpaceURI);
    if (nameSpacedMap == null) {
      
      log.info("NAMESPACED MAP NOT FOUND!!!!!!!!!");
      
      //return new NotFoundPropertyRepresentation(nameSpaceURI, propertyName);
      return new JcrPropertyRepresentation(nameSpaceURI, propertyName);
    }
    
    String className = nameSpacedMap.get(propertyName);
    if (className == null) {
      
      log.info(" >>>> NAMESPACE [" + nameSpaceURI + "] NAME [" + propertyName + "]");
      
      log.info("CLASS NAME ABSENT!!!!!!!!!");
      
      //return new NotFoundPropertyRepresentation(nameSpaceURI, propertyName);
      return new JcrPropertyRepresentation(nameSpaceURI, propertyName);
    }
    
    try {
      return (PropertyRepresentation)Class.forName(className).newInstance();
    } catch (Exception cexc) {
      
      log.info("CLASS NOT FOUND!!!!!!!!!!!");
      
      //return new NotFoundPropertyRepresentation(nameSpaceURI, propertyName);
      return new JcrPropertyRepresentation(nameSpaceURI, propertyName);
    }    
  }
  
  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#registerDocument(java.lang.String, java.lang.String, java.lang.String)
   * adding request document description for next using by RequestRepresentationDispatcher 
   */
  public void registerDocument(String nameSpace, String name, String className) {
    log.info("Registering document [" + nameSpace + "][" + name + "]");
    
    HashMap<String, String> nameSpacedList = documents.get(nameSpace);
    if (nameSpacedList == null) {
      nameSpacedList = new HashMap<String, String>();
      documents.put(nameSpace, nameSpacedList);
    }
    
    if (nameSpacedList.get(name) != null) {
      log.info("Document [" + nameSpace + "][" + name + "] already registered!");
    } else {
      nameSpacedList.put(name, className);
    }
    
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#registerProperty(java.lang.String, java.lang.String, java.lang.String)
   * adding property description for next using by PROPFIND, PROPPATCH, ect
   */
  public void registerProperty(String nameSpace, String name, String className) {    
    log.info("Registering property [" + nameSpace + "][" + name + "]...");    
    
    HashMap<String, String> nameSpacedList = properties.get(nameSpace);
    if (nameSpacedList == null) {
      nameSpacedList = new HashMap<String, String>();
      properties.put(nameSpace, nameSpacedList);
    }
    
    if (nameSpacedList.get(name) != null) {
      log.info("Property [" + nameSpace + "][" + name + "] already registered!");
    } else {
      nameSpacedList.put(name, className);
      log.info("added [" + nameSpace + "] [" + name + "] [" + className + "]...");
    }    
  }  

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#getDocuments()
   * returns all registered documents
   */
  public HashMap<String, HashMap<String, String>> getDocuments() {
    return documents;
  }
  
  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#getProperies()
   * returns all registered properties
   */
  public HashMap<String, HashMap<String, String>> getProperies() {
    return properties;
  }
  
}
