/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.connectors.jcr.adapters.local;

import java.net.MalformedURLException;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 *
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: JcrResourceAdapter.java 7176 2006-07-19 07:59:47Z peterit $
 */

public class JcrResourceAdapter implements ResourceAdapter {

  private static Log log = ExoLogger.getLogger("jcr.JcrResourceAdapter");

  String containerConfig;

  /* (non-Javadoc)
   * @see javax.resource.spi.ResourceAdapter#start(javax.resource.spi.BootstrapContext)
   */
  public synchronized void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException {

    System.out.println("<<<<<<<<<<<<<<<<<< JcrResourceAdapter.start(), " + containerConfig + " >>>>>>>>>>>>>>>>>>>");

    log.info("Container config: " + containerConfig);
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
	  String url = Thread.currentThread().getContextClassLoader().getResource(
          containerConfig).toString();
      StandaloneContainer.setConfigurationURL(url);
    } catch (MalformedURLException e) {
      log.warn("Invalid containerConfig URL, ignored: "+containerConfig);
      e.printStackTrace();
    }

    try {
      StandaloneContainer sc = StandaloneContainer.getInstance();
    } catch (Exception e) {
      log.error("Standalone container start error: " + e);
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see javax.resource.spi.ResourceAdapter#stop()
   */
  public void stop() {
    System.out.println("<<<<<<<<<<<<<<<<<< JcrResourceAdapter.stop(), " + containerConfig + " >>>>>>>>>>>>>>>>>>>");
    try {
      StandaloneContainer sc = StandaloneContainer.getInstance();
      sc.stop();
    } catch (Exception e) {
      log.error("Standalone container stop error: " + e);
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see javax.resource.spi.ResourceAdapter#getXAResources(javax.resource.spi.ActivationSpec[])
   */
  public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException {
    return null;
  }

  /* (non-Javadoc)
   * @see javax.resource.spi.ResourceAdapter#endpointActivation(javax.resource.spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
   */
  public void endpointActivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec) throws ResourceException {
  }

  /* (non-Javadoc)
   * @see javax.resource.spi.ResourceAdapter#endpointDeactivation(javax.resource.spi.endpoint.MessageEndpointFactory, javax.resource.spi.ActivationSpec)
   */
  public void endpointDeactivation(MessageEndpointFactory endpointFactory,
      ActivationSpec spec) {
  }

  public void setContainerConfig(String prop) {
    this.containerConfig = prop;
  }

}
