/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.JChannel;
import org.jgroups.blocks.MessageDispatcher;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 31.01.2008
 */
public class ReplicationService implements Startable {

  protected static Log        log                = ExoLogger.getLogger("ext.ReplicationService");

  private final static String IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  private final static String PERSISTENT_MODE    = "persistent";

  private final static String PROXY_MODE         = "proxy";

  private RepositoryService   repoService;

  private String              testMode;

  private String              enabled;

  private String              mode;

  private String              bindIPAdaress;

  private String              channelConfig;

  private List<String>        repoNamesList;

  public ReplicationService(RepositoryService repoService, InitParams params)
      throws RepositoryConfigurationException {
    this.repoService = repoService;

    PropertiesParam pps = params.getPropertiesParam("replication-properties");
    
    testMode = pps.getProperty("test-mode");

    enabled = pps.getProperty("enabled");
    if (enabled == null)
      throw new RepositoryConfigurationException("enabled not specified");

    mode = pps.getProperty("mode");
    if (mode == null)
      throw new RepositoryConfigurationException("mode not specified");
    else if (!mode.equals(PERSISTENT_MODE) && !mode.equals(PROXY_MODE))
      throw new RepositoryConfigurationException(
          "Parameter 'mode' (persistent|proxy) required for replication configuration");

    bindIPAdaress = pps.getProperty("bind-ip-address");
    if (bindIPAdaress == null)
      throw new RepositoryConfigurationException("bind-ip-address not specified");

    channelConfig = pps.getProperty("channel-config");
    if (channelConfig == null)
      throw new RepositoryConfigurationException("channel-config not specified");

     ValuesParam vp = params.getValuesParam("repositories");
     
     if (vp == null || vp.getValues().size() == 0)
       throw new RepositoryConfigurationException("repositories not specified");
     
     repoNamesList = vp.getValues();
  }

  public void start() {
    if (enabled.equals("true"))
      try {

        for (int rIndex = 0; rIndex < repoNamesList.size(); rIndex++) {
          RepositoryImpl jcrRepository = (RepositoryImpl) repoService.getRepository(repoNamesList
              .get(rIndex));

          String[] workspaces = jcrRepository.getWorkspaceNames();

          for (int wIndex = 0; wIndex < workspaces.length; wIndex++) {
            try {
              String systemId = IdGenerator.generate();
              String props = channelConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAdaress);
              JChannel channel = new JChannel(props);
              MessageDispatcher disp = new MessageDispatcher(channel, null, null, null);

              // get workspace container
              WorkspaceContainer wContainer = (WorkspaceContainer) jcrRepository.getSystemSession(
                  workspaces[wIndex]).getContainer();

              // add data transmitter
              wContainer.registerComponentImplementation(WorkspaceDataTransmitter.class);
              WorkspaceDataTransmitter dataTransmitter = (WorkspaceDataTransmitter) wContainer
                  .getComponentInstanceOfType(WorkspaceDataTransmitter.class);
              dataTransmitter.init(disp, systemId);

              String uniqueNoame = getUniqueName(jcrRepository.getConfiguration(),
                  workspaces[wIndex]);
              if (testMode != null && "true".equals(testMode))
                uniqueNoame = "Test_Channel";

              // add data receiver
              if (mode.equals(PROXY_MODE)) {
                wContainer.registerComponentImplementation(WorkspaceDataManagerProxy.class);

                wContainer.registerComponentImplementation(ProxyWorkspaceDataReceiver.class);

                ProxyWorkspaceDataReceiver proxyDataReceiver = (ProxyWorkspaceDataReceiver) wContainer
                    .getComponentInstanceOfType(ProxyWorkspaceDataReceiver.class);

                proxyDataReceiver.init(disp, systemId);

              } else if (mode.equals(PERSISTENT_MODE)) {

                wContainer.registerComponentImplementation(PersistentWorkspaceDataReceiver.class);

                PersistentWorkspaceDataReceiver persistentDataReceiver = (PersistentWorkspaceDataReceiver) wContainer
                    .getComponentInstanceOfType(PersistentWorkspaceDataReceiver.class);

                persistentDataReceiver.init(disp, systemId);
              }

              channel.connect(uniqueNoame);

            } catch (Exception e) {
              log.error("Can not start replication on " + repoNamesList.get(rIndex) + "_"
                  + workspaces[wIndex] + " \n" + e, e);
            }
          }
        }
      } catch (RepositoryException re) {
        log.error("Can not start ReplicationService \n" + re, re);
      } catch (RepositoryConfigurationException e) {
        log.error("Can not start ReplicationService \n" + e, e);
      }

  }

  private String getUniqueName(RepositoryEntry configuration, String workspaceName) {
    List<WorkspaceEntry> wEntrys = configuration.getWorkspaceEntries();

    for (Iterator iterator = wEntrys.iterator(); iterator.hasNext();) {
      WorkspaceEntry wEntry = (WorkspaceEntry) iterator.next();

      if (workspaceName.equals(wEntry.getName()))
        return wEntry.getUniqueName();
    }
    return null;
  }

  public void stop() {

  }

}
