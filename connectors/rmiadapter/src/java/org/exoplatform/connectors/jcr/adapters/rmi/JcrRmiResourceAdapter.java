/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: JcrRmiResourceAdapter.java 11123 2006-12-12 15:52:22Z ksm $
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
