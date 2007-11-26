/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.lang.reflect.Constructor;
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
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.config.WebDavConfigImpl;
import org.exoplatform.services.webdav.deltav.representation.property.CheckedInRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.CheckedOutRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.IsVersionedRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.LabelNameSetRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.PredecessorSetRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.SuccessorSetRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.VersionHistoryRepresentation;
import org.exoplatform.services.webdav.deltav.representation.property.VersionNameRepresentation;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.exoplatform.services.webdav.lock.representation.property.LockDiscoveryRepresentation;
import org.exoplatform.services.webdav.lock.representation.property.SupportedLockRepresentation;
import org.exoplatform.services.webdav.order.representation.property.OrderingTypeRepresentation;
import org.exoplatform.services.webdav.search.representation.property.SupportedQueryGrammarSetRepresentation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavServiceImpl implements WebDavService {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavServiceImpl");
  
  public static final String WEBDAVCONFIG_PATH = "/conf/exo-webdav-config.xml";
  
  protected RepositoryService  repositoryService;
  
  protected ResourceBinder resourceBinder;
  
  protected WebDavConfig config;

  protected FakeLockTable lockTable;
  
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
    registerProperty(LabelNameSetRepresentation.NAMESPACE, LabelNameSetRepresentation.TAGNAME, LabelNameSetRepresentation.class.getCanonicalName());
    
    /*
     * registering LOCK properties 
     * 
     */
    
    registerProperty(SupportedLockRepresentation.NAMESPACE, SupportedLockRepresentation.TAGNAME, SupportedLockRepresentation.class.getCanonicalName());
    registerProperty(LockDiscoveryRepresentation.NAMESPACE, LockDiscoveryRepresentation.TAGNAME, LockDiscoveryRepresentation.class.getCanonicalName());
    
    /*
     * ORDERING
     */
    registerProperty(OrderingTypeRepresentation.NAMESPACE, OrderingTypeRepresentation.TAGNAME, OrderingTypeRepresentation.class.getCanonicalName());
    
    /*
     * SEARCHING
     */
    
    registerProperty(SupportedQueryGrammarSetRepresentation.NAMESPACE, SupportedQueryGrammarSetRepresentation.TAGNAME, SupportedQueryGrammarSetRepresentation.class.getCanonicalName());
  }
  
  public WebDavConfig getConfig() {
    return config;
  }
  
  private HashMap<String, String> ownerTable = new HashMap<String, String>(); 
  
  public HashMap<String, String> getOwnerTable() {
    return ownerTable;
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
      String uriPattern = descriptor.getURIPattern().getString();
      
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
  
  public PropertyRepresentation getPropertyRepresentation(String nameSpaceURI, String propertyName, String href) {
    HashMap<String, String> nameSpacedMap = properties.get(nameSpaceURI);
    if (nameSpacedMap == null) {
      return new JcrPropertyRepresentation(nameSpaceURI, propertyName);
    }
    
    String className = nameSpacedMap.get(propertyName);
    if (className == null) {
      return new JcrPropertyRepresentation(nameSpaceURI, propertyName);
    }
    
    try {      
      Class propertyClass = Class.forName(className);
      
      /*
       * find property with constructor like PropertyRepresentation(WebDavService)
       */
      try {
        Constructor constructor = propertyClass.getConstructor(WebDavService.class);
        return (PropertyRepresentation)constructor.newInstance(this);
      } catch (NoSuchMethodException mexc) {
      }
      
      /*
       * find proeprty with constructor like PropertyRepresentation(String href)
       */
      try {
        Constructor constructor = propertyClass.getConstructor(String.class);
        return (PropertyRepresentation)constructor.newInstance(href);
      } catch (NoSuchMethodException mexc) {
      }
      
      return (PropertyRepresentation)propertyClass.newInstance();
    } catch (Exception exc) {
      return new JcrPropertyRepresentation(nameSpaceURI, propertyName);
    }    
  }
  
  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.webdav.WebDavService#registerProperty(java.lang.String, java.lang.String, java.lang.String)
   * adding property description for next using by PROPFIND, PROPPATCH, ect
   */
  public void registerProperty(String nameSpace, String name, String className) {    
    //log.info("Registering property [" + nameSpace + "][" + name + "]...");    
    
    HashMap<String, String> nameSpacedList = properties.get(nameSpace);
    if (nameSpacedList == null) {
      nameSpacedList = new HashMap<String, String>();
      properties.put(nameSpace, nameSpacedList);
    }
    
    if (nameSpacedList.get(name) != null) {
      //log.info("Property [" + nameSpace + "][" + name + "] already registered!");
    } else {
      nameSpacedList.put(name, className);
      //log.info("added [" + nameSpace + "] [" + name + "] [" + className + "]...");
    }    
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
