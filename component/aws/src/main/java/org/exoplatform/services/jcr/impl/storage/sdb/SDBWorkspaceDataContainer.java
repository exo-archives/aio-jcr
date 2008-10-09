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

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.storage.WorkspaceDataContainerBase;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;

import sun.nio.cs.SingleByteDecoder;

import com.amazonaws.sdb.AmazonSimpleDB;
import com.amazonaws.sdb.AmazonSimpleDBClient;
import com.amazonaws.sdb.AmazonSimpleDBConfig;
import com.amazonaws.sdb.AmazonSimpleDBException;
import com.amazonaws.sdb.model.CreateDomainRequest;
import com.amazonaws.sdb.model.CreateDomainResponse;
import com.amazonaws.sdb.model.DeleteDomainRequest;
import com.amazonaws.sdb.model.DeleteDomainResponse;
import com.amazonaws.sdb.model.GetAttributesRequest;
import com.amazonaws.sdb.model.GetAttributesResponse;
import com.amazonaws.sdb.model.ListDomainsRequest;
import com.amazonaws.sdb.model.ListDomainsResponse;
import com.amazonaws.sdb.model.ListDomainsResult;
import com.amazonaws.sdb.model.PutAttributesRequest;
import com.amazonaws.sdb.model.PutAttributesResponse;
import com.amazonaws.sdb.model.ReplaceableAttribute;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 30.09.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SDBWorkspaceDataContainer.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class SDBWorkspaceDataContainer extends WorkspaceDataContainerBase {

  /**
   * AWS access key parameter name.
   */
  public static final String SDB_ACCESSKEY  = "aws-access-key";

  /**
   * AWS secret key parameter name.
   */
  public static final String SDB_SECRETKEY  = "aws-secret-key";

  /**
   * AWS SimpleDB domain name will be used. If name doesn't exist it will be created.
   */
  public static final String SDB_DOMAINNAME = "sdb-domain-name";

  /**
   * AWS access key.
   */
  protected final String     accessKey;

  /**
   * AWS secret key.
   */
  protected final String     secretKey;

  /**
   * SDB domain name.
   */
  protected final String     domainName;

  /**
   * Create container using repository and workspace configuration.
   * 
   * @param wsConfig
   *          Workspace configuration
   * @param repConfig
   *          Repositiry configuration
   * @throws RepositoryConfigurationException
   */
  public SDBWorkspaceDataContainer(WorkspaceEntry wsConfig, RepositoryEntry repConfig) throws RepositoryConfigurationException,
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
  }

  // Interfaces implementaion.

  /**
   * {@inheritDoc}
   */
  public WorkspaceStorageConnection openConnection() throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public WorkspaceStorageConnection reuseConnection(WorkspaceStorageConnection original) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String getInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String getStorageVersion() {
    // TODO Auto-generated method stub
    return null;
  }

}
