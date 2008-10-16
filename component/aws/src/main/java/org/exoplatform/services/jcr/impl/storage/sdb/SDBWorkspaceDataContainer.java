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
package org.exoplatform.services.jcr.impl.storage.sdb;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.jcr.storage.value.ValueStoragePluginProvider;
import org.exoplatform.services.log.ExoLogger;

import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.Item;
import com.amazonaws.sdb.model.QueryWithAttributesResponse;
import com.amazonaws.sdb.model.QueryWithAttributesResult;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 30.09.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class SDBWorkspaceDataContainer extends WorkspaceDataContainerBase {

  /**
   * Container storage version of the implementation .
   */
  public static final String                 CURRENT_STORAGE_VERSION = "1.0-b";

  /**
   * AWS access key parameter name.
   */
  public static final String                 SDB_ACCESSKEY           = "aws-access-key";

  /**
   * AWS secret key parameter name.
   */
  public static final String                 SDB_SECRETKEY           = "aws-secret-access-key";

  /**
   * AWS SimpleDB domain name will be used. If name doesn't exist it will be created.
   */
  public static final String                 SDB_DOMAINNAME          = "domain-name";
  
  /**
   * Storage cleaner timeout 30min.
   */
  protected static final int                 CLEANER_TIMEOUT           = 20 * 60 * 1000; 

  /**
   * Container logger.
   */
  protected static final Log                 LOG                     = ExoLogger.getLogger("jcr.SDBWorkspaceDataContainer");

  /**
   * Container name.
   */
  protected final String                     containerName;

  /**
   * Actual container storage version.
   */
  protected final String                     storageVersion;

  /**
   * AWS access key.
   */
  protected final String                     accessKey;

  /**
   * AWS secret key.
   */
  protected final String                     secretKey;

  /**
   * SDB domain name.
   */
  protected final String                     domainName;

  /**
   * External Value Storages provider to save Properties using configured filters.
   */
  protected final ValueStoragePluginProvider valueStorageProvider;

  /**
   * Max buffer size used by External Value Storages provider to match storage per Property.
   */
  protected final int                        maxBufferSize;
  
  /**
   * Storage cleaner.
   */
  protected final StorageCleaner                       storageCleaner;

  /**
   * Create container using repository and workspace configuration.
   * 
   * @param wsConfig
   *          Workspace configuration
   * @param repConfig
   *          Repositiry configuration
   * @param valueStorageProvider
   *          - External Value Storages provider component
   * @throws RepositoryException
   *           - if init procedure fails
   * @throws RepositoryConfigurationException
   *           - if Workspace configuration is wrong
   */
  public SDBWorkspaceDataContainer(WorkspaceEntry wsConfig,
                                   RepositoryEntry repConfig,
                                   ValueStoragePluginProvider valueStorageProvider) throws RepositoryConfigurationException,
      RepositoryException {

    String myAccessKey = wsConfig.getContainer().getParameterValue(SDB_ACCESSKEY);
    if (myAccessKey == null || myAccessKey.length() <= 0)
      throw new RepositoryConfigurationException("AWS (SimpleDB) Access key required");

    String mySecretKey = wsConfig.getContainer().getParameterValue(SDB_SECRETKEY);
    if (mySecretKey == null || mySecretKey.length() <= 0)
      throw new RepositoryConfigurationException("AWS (SimpleDB) Secret key required");

    String myDomainName = wsConfig.getContainer().getParameterValue(SDB_DOMAINNAME);
    if (myDomainName == null || myDomainName.length() <= 0)
      throw new RepositoryConfigurationException("AWS (SimpleDB) domain name required");

    accessKey = myAccessKey;

    secretKey = mySecretKey;

    domainName = myDomainName;

    // External storage
    this.valueStorageProvider = valueStorageProvider;

    int maxbs;
    try {
      maxbs = wsConfig.getContainer().getParameterInteger(MAXBUFFERSIZE);
    } catch (RepositoryConfigurationException e) {
      maxbs = DEF_MAXBUFFERSIZE;
    }
    this.maxBufferSize = maxbs;

    this.containerName = wsConfig.getName();

    SDBWorkspaceStorageConnection conn = new SDBWorkspaceStorageConnection(accessKey,
                                                                           secretKey,
                                                                           domainName,
                                                                           maxBufferSize,
                                                                           valueStorageProvider);
    this.storageVersion = conn.initStorage(containerName, CURRENT_STORAGE_VERSION);
    
    this.storageCleaner = new StorageCleaner(containerName, conn, CLEANER_TIMEOUT);
    this.storageCleaner.start();

    LOG.info(getInfo());
  }

  // Interfaces implementaion.

  /**
   * {@inheritDoc}
   */
  public WorkspaceStorageConnection openConnection() throws RepositoryException {
    return new SDBWorkspaceStorageConnection(accessKey,
                                             secretKey,
                                             domainName,
                                             maxBufferSize,
                                             valueStorageProvider);
  }

  /**
   * {@inheritDoc}
   */
  public WorkspaceStorageConnection reuseConnection(WorkspaceStorageConnection original) throws RepositoryException {
    // There are no actual difference for the SimpleDB impl.
    return openConnection();
  }

  /**
   * {@inheritDoc}
   */
  public String getInfo() {
    String str = "SimpleDB based JCR Workspace Data container \n" + "container name: "
        + containerName + " \n" + "domain name: " + domainName + "\n" + "\n" + "storage version: "
        + storageVersion + "\n" + "value storage provider: " + valueStorageProvider + "\n"
        + "max buffer size (bytes): " + maxBufferSize;
    return str;
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    return containerName;
  }

  /**
   * {@inheritDoc}
   */
  public String getStorageVersion() {
    return storageVersion;
  }

}
