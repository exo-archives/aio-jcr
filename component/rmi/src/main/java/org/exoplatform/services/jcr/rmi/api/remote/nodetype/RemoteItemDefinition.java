/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote.nodetype;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;

/**
 * Remote version of the JCR {@link javax.jcr.nodetype.ItemDefinition ItemDef}
 * interface. Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerItemDefinition ServerItemDefinition}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientItemDefinition ClientItemDefinition}
 * adapter base classes to provide transparent RMI access to remote item
 * definitions.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding ItemDef method. The remote object will simply forward the
 * method call to the underlying ItemDef instance. Argument and return values,
 * as well as possible exceptions, are copied over the network. Compex
 * {@link javax.jcr.nodetype.NodeType NodeType} return values are returned as
 * remote references to the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeType RemoteNodeType}
 * interface. RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.nodetype.ItemDefinition
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientItemDefinition
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerItemDefinition
 */
public interface RemoteItemDefinition extends Remote {

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.ItemDefinition#getDeclaringNodeType() ItemDef.getDeclaringNodeType()}
   * method.
   * 
   * @return declaring node type
   * @throws RemoteException on RMI errors
   */
  RemoteNodeType getDeclaringNodeType() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.ItemDefinition#getName() ItemDef.getName()}
   * method.
   * 
   * @return item name
   * @throws RemoteException on RMI errors
   */
  String getName() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.ItemDefinition#isAutoCreated() ItemDef.isAutoCreate()}
   * method.
   * 
   * @return <code>true</code> if the item is automatically created,
   *         <code>false</code> otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isAutoCreated() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.ItemDefinition#isMandatory() ItemDef.isMandatory()}
   * method.
   * 
   * @return <code>true</code> if the item is mandatory, <code>false</code>
   *         otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isMandatory() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.ItemDefinition#getOnParentVersion() ItemDef.getOnParentVersion()}
   * method.
   * 
   * @return parent version behaviour
   * @throws RemoteException on RMI errors
   */
  int getOnParentVersion() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.nodetype.ItemDefinition#isProtected() ItemDef.isProtected()}
   * method.
   * 
   * @return <code>true</code> if the item is protected, <code>false</code>
   *         otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isProtected() throws RemoteException;

}
