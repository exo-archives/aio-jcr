/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.registry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL        .<br/>
 * JCR based Services Registry abstraction
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class Registry {
  
  /**
   * Returns existed or newly created RegistryEntry which wraps Node of "exo:registryEntry" type  
   * @param sessionProvider
   * @param entryType
   * @param entryName
   * @param repository
   * @return
   * @throws RepositoryException
   */
  public abstract RegistryEntryNode getRegistryEntry(SessionProvider sessionProvider, String entryType,
      String entryName, ManageableRepository repository) throws RepositoryException;

  /**
   * Returns Registry object which wraps Node of "exo:registry" type
   * (the whole registry tree)  
   * @param sessionProvider
   * @param repository
   * @return
   * @throws RepositoryException
   */
  public abstract RegistryNode getRegistry(SessionProvider sessionProvider, ManageableRepository repository) 
      throws RepositoryException;

  /**
   * Registers entry (saves the node)  
   * @param entry
   * @throws RepositoryException
   */
  public abstract void register(RegistryEntryNode entry) throws RepositoryException;
  
  /**
   * Unregisters entry
   * @param entry
   * @throws RepositoryException
   */
  public abstract void unregister(RegistryEntryNode entry) throws RepositoryException;
  
 
  /**
   * Internal Node wrapper which ensures the node of "exo:registryEntry" type inside
   */
  public final class RegistryEntryNode extends NodeWrapper {
    protected RegistryEntryNode(final Node node) throws RepositoryException {
      super(node);
    }
  }

  /**
   * Internal Node wrapper which ensures the node of "exo:registry" type inside
   */
  public final class RegistryNode extends NodeWrapper {
    protected RegistryNode(final Node node) throws RepositoryException {
      super(node);
    }
  }
  
  private class NodeWrapper {
    
    private final Node node;
    
    private NodeWrapper(final Node node) {
      this.node = node;
    }

    public final Node getNode() {
      return node;
    }
  }

}
