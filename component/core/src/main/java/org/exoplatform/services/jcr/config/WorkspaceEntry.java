/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.config;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov
 *         </a>
 * @version $Id: WorkspaceEntry.java 12843 2007-02-16 09:11:18Z peterit $
 */
public class WorkspaceEntry {

  private String name;

  private String autoInitializedRootNt;

  private ContainerEntry container;

  private QueryHandlerEntry queryHandler;
  
  private CacheEntry cache;
  
  private String uniqueName;
  
  private AccessManagerEntry accessManager;
  
  public WorkspaceEntry() {

  }

  public WorkspaceEntry(String name, String rootNt) {
    this.name = name;
    this.autoInitializedRootNt = rootNt;

  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the autoInitializedRootNt.
   */
  public String getAutoInitializedRootNt() {
    return autoInitializedRootNt;
  }

  /**
   * @param autoInitializedRootNt
   *          The autoInitializedRootNt to set.
   */
  public void setAutoInitializedRootNt(String autoInitializedRootNt) {
    this.autoInitializedRootNt = autoInitializedRootNt;
  }

  /**
   * @return Returns the container.
   */
  public ContainerEntry getContainer() {
    return container;
  }

  /**
   * @param container
   *          The container to set.
   */
  public void setContainer(ContainerEntry container) {
    this.container = container;
  }

  /**
   * @return Returns the queryManager.
   */
  public QueryHandlerEntry getQueryHandler() {
    return queryHandler;
  }

  /**
   * @param queryManager
   *          The queryManager to set.
   */
  public void setQueryHandler(QueryHandlerEntry queryHandler) {
    this.queryHandler = queryHandler;
  }
  /**
   * @return Returns the cache.
   */
  public CacheEntry getCache() {
    return cache;
  }
  /**
   * @param cache The cache to set.
   */
  public void setCache(CacheEntry cache) {
    this.cache = cache;
  }
  /**
   * @return Returns the uniqueName.
   */
  public String getUniqueName() {
    return uniqueName;
  }
  /**
   * @param uniqueName The uniqueName to set.
   */
  public void setUniqueName(String uniqueName) {
    this.uniqueName = uniqueName;
  }

  public AccessManagerEntry getAccessManager() {
    return accessManager;
  }

  public void setAccessManager(AccessManagerEntry accessManager) {
    this.accessManager = accessManager;
  }

}