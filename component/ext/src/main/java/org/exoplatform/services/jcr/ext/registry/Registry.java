/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.common.NodeWrapper;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL        .<br/>
 * JCR based Services Registry abstraction.
 * As interchange object all the methods use Nodes' wrappers to not
 * to let using an arbitrary Type of Node.
 * There is 2 phase modification of RegistryEntry
 * (1) get or create RegistryEntry retrieves or creates new object in memory
 * and 
 * (2) register/unregister stores the object permanently  
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class Registry {
  
  /**
   * Returns Registry object which wraps Node of "exo:registry" type
   * (the whole registry tree)  
   * @param sessionProvider
   * @param repository
   * @return
   * @throws RepositoryException
   */
  public abstract RegistryNode getRegistry(SessionProvider sessionProvider) 
      throws RepositoryConfigurationException, RepositoryException;

  
  /**
   * Returns existed RegistryEntry which wraps Node of "exo:registryEntry" type  
   * @param sessionProvider
   * @param entryType
   * @param entryName
   * @param repository
   * @return
   * @throws RepositoryException
   */
  public abstract RegistryEntry getEntry(SessionProvider sessionProvider, String groupName,
      String entryName) throws RepositoryConfigurationException, RepositoryException;

  /**
   * @param sessionProvider
   * @param groupName
   * @param entry
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public abstract void createEntry(SessionProvider sessionProvider,
  		String groupName, RegistryEntry entry) throws RepositoryConfigurationException,
  		RepositoryException;

  /**
   * @param sessionProvider
   * @param groupName
   * @param entryName
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public abstract void recreateEntry(SessionProvider sessionProvider,
  		String groupName, RegistryEntry entry) throws RepositoryConfigurationException,
  		RepositoryException;

  /**
   * @param sessionProvider
   * @param groupName
   * @param entryName
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public abstract void removeEntry(SessionProvider sessionProvider,
  		String groupName, String entryName) throws RepositoryConfigurationException,
  		RepositoryException;
  
  /**
   * Internal Node wrapper which ensures the node of "exo:registry" type inside
   */
  public final class RegistryNode extends NodeWrapper {
    protected RegistryNode(final Node node) throws RepositoryException {
      super(node);
    }
  }

}
