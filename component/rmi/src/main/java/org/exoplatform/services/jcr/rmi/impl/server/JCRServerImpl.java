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
package org.exoplatform.services.jcr.rmi.impl.server;

import java.net.InetAddress;
import java.rmi.Naming;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;
import org.picocontainer.Startable;

/**
 * Default implementation of the
 * {@link RemoteAdapterFactory RemoteAdapterFactory} interface. This factory
 * uses the server adapters defined in this package as the default adapter
 * implementations. Subclasses can override or extend the default adapters by
 * implementing the corresponding factory methods.
 * <p>
 */
public class JCRServerImpl implements JCRServer, Startable {

  private final int                  rmiPort;

  private final boolean              autoBind;

  private boolean                    bound;

  private final RemoteAdapterFactory factory;

  private final RepositoryService    service;

  public JCRServerImpl(InitParams params, RepositoryService service) {
    bound = false;
    rmiPort = Integer.valueOf(params.getValueParam("rmiPort").getValue()).intValue();
    autoBind = Boolean.valueOf(params.getValueParam("autoBind").getValue()).booleanValue();
    factory = new RemoteAdapterFactoryImpl();
    this.service = service;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.rmi2.api.server.JCRSerever#bind()
   */
  public void bind() {
    if (bound) {
      return;
    }
    String bindName = "";
    try {
      List config = service.getConfig().getRepositoryConfigurations();
      for (int i = 1; i <= config.size(); i++) {
        RepositoryEntry conf = (RepositoryEntry) config.get(i - 1);
        RemoteRepository remote = factory
            .getRemoteRepository(service.getRepository(conf.getName()));

        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();

        bindName = "//" + hostname;
        if (rmiPort > 0) {
          bindName = bindName + ":" + String.valueOf(rmiPort);
        }
        bindName = bindName + "/" + conf.getName();
        System.out.println(bindName);
        Naming.bind(bindName, remote);
      }
      bound = true;
    } catch (Exception e) {
      throw new RuntimeException(bindName + ": " + e.toString(), e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.rmi2.api.server.JCRSerever#start()
   */
  public void start() {
    if (autoBind) {
      bind();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.rmi2.api.server.JCRSerever#stop()
   */
  public void stop() {
    if (autoBind) {
      unbind();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.rmi2.api.server.JCRSerever#unbind()
   */
  public void unbind() {
    String bindName = "";
    try {
      List config = service.getConfig().getRepositoryConfigurations();
      for (int i = 1; i <= config.size(); i++) {
        RepositoryEntry conf = (RepositoryEntry) config.get(i - 1);

        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();

        bindName = "//" + hostname;
        if (rmiPort > 0) {
          bindName = bindName + ":" + String.valueOf(rmiPort);
        }
        bindName = bindName + "/" + conf.getName();
        Naming.unbind(bindName);
      }
      bound = false;
    } catch (Exception e) {
      throw new RuntimeException(bindName + ": " + e.toString(), e);
    }

  }

}
