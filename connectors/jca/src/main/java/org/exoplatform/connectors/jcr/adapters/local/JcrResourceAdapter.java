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
 * Created by The eXo Platform SAS .
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

	  if (containerConfig != null) {
	  String url = Thread.currentThread().getContextClassLoader().getResource(
          containerConfig).toString();
      StandaloneContainer.addConfigurationURL(url);
     }
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
