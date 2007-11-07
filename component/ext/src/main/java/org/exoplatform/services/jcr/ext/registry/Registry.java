/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.common.NodeWrapper;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

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
   * Returns Registry node object which wraps Node of "exo:registry" type
   * (the whole registry tree)  
   * @param sessionProvider
   * @param repository
   * @return egistry node object
   * @throws RepositoryException
   */
  public abstract RegistryNode getRegistry(SessionProvider sessionProvider) 
      throws RepositoryConfigurationException, RepositoryException;

  
  /**
   * Returns existed RegistryEntry which wraps Node of "exo:registryEntry" type  
   * @param sessionProvider
   * @param groupPath
   * @param entryName
   * @return existed RegistryEntry
   * @throws ItemNotFoundException if entry not found
   * @throws RepositoryException
   */
  public abstract RegistryEntry getEntry(SessionProvider sessionProvider, String groupPath,
      String entryName) throws ItemNotFoundException, RepositoryException;

  /**
   * creates an entry in  the group. In a case if the group does not exist it will be 
   * silently created as well
   * @param sessionProvider
   * @param groupPath related path (w/o leading slash) to group 
   * @param entry
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public abstract void createEntry(SessionProvider sessionProvider,
  		String groupPath, RegistryEntry entry) throws RepositoryException;

  /**
   * updates an entry in the group
   * @param sessionProvider
   * @param groupPath related path (w/o leading slash) to group 
   * @param entry
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public abstract void recreateEntry(SessionProvider sessionProvider,
  		String groupPath, RegistryEntry entry) throws RepositoryException;

  /**
   * removes entry located on entryPath (concatenation of group path / entry name)
   * @param sessionProvider
   * @param entryPath related path (w/o leading slash) to entry 
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public abstract void removeEntry(SessionProvider sessionProvider,
  		String entryPath) throws RepositoryException;
  
  /**
   * Internal Node wrapper which ensures the node of "exo:registry" type inside
   */
  public final class RegistryNode extends NodeWrapper {
    protected RegistryNode(final Node node) throws RepositoryException {
      super(node);
    }
  }

}
