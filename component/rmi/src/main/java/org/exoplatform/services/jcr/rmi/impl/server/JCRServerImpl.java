/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
public class JCRServerImpl implements JCRSerever, Startable {

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
