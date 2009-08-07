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
package org.exoplatform.connectors.jcr.adapters.rmi;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.exoplatform.services.jcr.rmi.api.client.ClientRepositoryFactory;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id$
 */

public class JcrRmiResourceAdapter implements javax.resource.spi.ResourceAdapter
{
  protected String bindName;
  protected String remoteName;
  protected Repository repository; 

  public synchronized void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
System.out.println("bindName: " + bindName);
System.out.println("remoteName: " + remoteName);
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      ClientRepositoryFactory factory = new ClientRepositoryFactory();
      repository = factory.getRepository(remoteName);
      
      InitialContext jndiContext = new InitialContext();
      jndiContext.bind(bindName, repository);
System.out.println("bound repository " + repository);
    } catch (Exception e) {
System.out.println("!!! BINDING ERROR: " + e);
//e.printStackTrace();
    }
  }
 
  public void stop() {
    try {
      InitialContext jndiContext = new InitialContext();
      jndiContext.unbind(bindName);
    } catch (Exception e) {
    }
  }

  public void setBindName(String prop) {
    this.bindName = prop;
  }
 
  public void setRemoteName(String prop) {
    this.remoteName = prop;
  }
 
  void bind(String name, Object obj) {
    try {
      Context ctx = new InitialContext();
      ctx.rebind(name, obj);
    } catch (Exception e) {
    }
  }

  public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
    return null;
  }

  public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
  }

  public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
  }

}
