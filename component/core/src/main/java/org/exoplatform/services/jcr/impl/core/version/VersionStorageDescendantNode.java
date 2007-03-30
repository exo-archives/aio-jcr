/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.version;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy;

/**
 * Created by The eXo Platform SARL        .
 * 
 * @author Gennady Azarenkov
 * @version $Id: VersionStorageDescendantNode.java 12841 2007-02-16 08:58:38Z peterit $
 */

public abstract class VersionStorageDescendantNode extends NodeImpl {

  // new impl
  public VersionStorageDescendantNode(NodeData data,
      SessionImpl session) throws PathNotFoundException,
      RepositoryException {

    super(data, session);
    
  }
  
  public Node addNode(String relPath, String nodeTypeName) throws ConstraintViolationException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public Node addNode(String relPath) throws ConstraintViolationException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public Property setProperty(String name, boolean value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, Calendar value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, double value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, InputStream value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, long value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, Node value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, String value, int type)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public Property setProperty(String name, String value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, String[] values, int type)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public Property setProperty(String name, String[] values)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, Value value, int type)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, Value value)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public Property setProperty(String name, Value[] values, int type)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public Property setProperty(String name, Value[] values)
      throws ValueFormatException, VersionException, LockException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  public void remove() throws RepositoryException,
      ConstraintViolationException, VersionException, LockException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public void addMixin(String mixinName) throws ConstraintViolationException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public boolean canAddMixin(String mixinName) throws RepositoryException {
    return false;
  }
  
  public void removeMixin(String mixinName) throws NoSuchNodeTypeException,
      ConstraintViolationException, RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public void restore(Version version, boolean removeExisting)
      throws VersionException, ItemExistsException,
      UnsupportedRepositoryOperationException, LockException,
      RepositoryException, InvalidItemStateException {
    throw new UnsupportedRepositoryOperationException("jcr:versionStorage is protected");
  }
  
  public void restore(String versionName, boolean removeExisting)
      throws VersionException, ItemExistsException,
      UnsupportedRepositoryOperationException, LockException,
      RepositoryException, InvalidItemStateException {
    throw new UnsupportedRepositoryOperationException("jcr:versionStorage is protected");
  }

  public void restore(Version version, String relPath, boolean removeExisting)
      throws VersionException, ItemExistsException,
      UnsupportedRepositoryOperationException, LockException,
      RepositoryException, InvalidItemStateException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }

  
  public void update(String srcWorkspaceName) throws NoSuchWorkspaceException,
      AccessDeniedException, InvalidItemStateException, LockException,
      RepositoryException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  
  public NodeIterator merge(String srcWorkspace, boolean bestEffort)
      throws UnsupportedRepositoryOperationException, NoSuchWorkspaceException,
      AccessDeniedException, MergeException, RepositoryException,
      InvalidItemStateException {
    throw new ConstraintViolationException("jcr:versionStorage is protected");
  }
  
  public void cancelMerge(Version version) throws VersionException,
      InvalidItemStateException, UnsupportedRepositoryOperationException,
      RepositoryException {
    throw new UnsupportedRepositoryOperationException(
        "jcr:versionStorage is protected");
  }

  public void doneMerge(Version version) throws VersionException,
      InvalidItemStateException, UnsupportedRepositoryOperationException,
      RepositoryException {
    throw new UnsupportedRepositoryOperationException(
        "jcr:versionStorage is protected");
  }
  
/*  
  protected PropertyImpl doSetProperty(String name, Value[] values, int type,
      boolean multiValued, boolean registerInDataManager, boolean doExternalValidation) 
      throws ConstraintViolationException, VersionException, RepositoryException {
    if(doExternalValidation)
      throw new ConstraintViolationException("jcr:versionStorage is protected");
    else
      return super.doSetProperty(name, values, type, multiValued, 
          registerInDataManager, doExternalValidation);
  }
*/

}
