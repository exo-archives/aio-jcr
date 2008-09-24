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
package org.exoplatform.services.jcr.config;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov </a>
 * @version $Id: WorkspaceEntry.java 13991 2008-05-08 11:07:05Z pnedonosko $
 */
public class WorkspaceEntry {

  private String                    name;

  private String                    autoInitializedRootNt;

  private ContainerEntry            container;

  private QueryHandlerEntry         queryHandler;

  private CacheEntry                cache;

  private String                    uniqueName;

  private AccessManagerEntry        accessManager;

  private LockManagerEntry          lockManager;

  private WorkspaceInitializerEntry initializer;

  private String                    autoInitPermissions;

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
  @Deprecated
  public String getAutoInitializedRootNt() {
    return autoInitializedRootNt;
  }

  /**
   * @param autoInitializedRootNt
   *          The autoInitializedRootNt to set.
   */
  @Deprecated
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
   * @return Returns the cache.
   */
  public CacheEntry getCache() {
    return cache;
  }

  /**
   * @param cache
   *          The cache to set.
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
   * @param uniqueName
   *          The uniqueName to set.
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

  @Deprecated
  public String getAutoInitPermissions() {
    return autoInitPermissions;
  }

  @Deprecated
  public void setAutoInitPermissions(String autoInitPermissions) {
    this.autoInitPermissions = autoInitPermissions;
  }

  public LockManagerEntry getLockManager() {
    return lockManager;
  }

  public void setLockManager(LockManagerEntry lockManager) {
    this.lockManager = lockManager;
  }

  public QueryHandlerEntry getQueryHandler() {
    return queryHandler;
  }

  public void setQueryHandler(QueryHandlerEntry queryHandlerEntry) {
    this.queryHandler = queryHandlerEntry;
  }

  public WorkspaceInitializerEntry getInitializer() {
    return initializer;
  }

  public void setInitializer(WorkspaceInitializerEntry initializer) {
    this.initializer = initializer;
  }

}
