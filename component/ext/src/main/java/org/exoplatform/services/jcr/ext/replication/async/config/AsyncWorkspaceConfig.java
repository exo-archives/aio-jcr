/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.ext.replication.async.AsyncReplication;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 18.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncWorkspaceConfig.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncWorkspaceConfig extends BaseComponentPlugin {

  protected final int           priority;

  protected final List<Integer> otherParticipantsPriority;

  protected final String        bindIPAddress;

  protected final String        channelConfig;

  protected final String        channelName;

  protected final int           waitAllMembersTimeout;

  protected final String        mergeTempDir;

  protected final String        storageDir;

  protected final String        localStorageDir;

  protected final String        incomeStorageDir;

  protected final String        repositoryName;

  protected final String        workspaceName;

  public AsyncWorkspaceConfig(InitParams params) {
    PropertiesParam pps = params.getPropertiesParam("async-workspca-config");

    if (pps == null)
      throw new RuntimeException("replication-properties not specified");

    // initialize replication parameters;
    repositoryName = pps.getProperty("repository-name");
    if (repositoryName == null)
      throw new RuntimeException("repository-name not specified");
    
    workspaceName = pps.getProperty("workspace-name");
    if (workspaceName == null)
      throw new RuntimeException("workspace-name not specified");
    
    if (pps.getProperty("priority") == null)
      throw new RuntimeException("priority not specified");

    priority = Integer.parseInt(pps.getProperty("priority"));

    bindIPAddress = pps.getProperty("bind-ip-address");

    String chConfig = pps.getProperty("channel-config");
    if (chConfig == null)
      throw new RuntimeException("channel-config not specified");
    channelConfig = chConfig.replaceAll(AsyncReplication.IP_ADRESS_TEMPLATE, bindIPAddress);

    channelName = pps.getProperty("channel-name");
    if (channelName == null)
      throw new RuntimeException("channel-config not specified");

    if (pps.getProperty("wait-all-members") == null)
      throw new RuntimeException("wait-all-members timeout not specified");
    waitAllMembersTimeout = Integer.parseInt(pps.getProperty("wait-all-members")) * 1000;

    this.storageDir = pps.getProperty("storage-dir");
    if (storageDir == null)
      throw new RuntimeException("storage-dir not specified");

    String sOtherParticipantsPriority = pps.getProperty("other-participants-priority");
    if (sOtherParticipantsPriority == null)
      throw new RuntimeException("other-participants-priority not specified");

    String saOtherParticipantsPriority[] = sOtherParticipantsPriority.split(",");

    // Ready to begin...

    this.otherParticipantsPriority = new ArrayList<Integer>();

    for (String sPriority : saOtherParticipantsPriority)
      otherParticipantsPriority.add(Integer.valueOf(sPriority));

    if (hasDuplicatePriority(this.otherParticipantsPriority, this.priority))
      throw new RuntimeException("The value of priority is duplicated : " + "Priority = "
          + this.priority + " ; " + "Other participants priority = " + otherParticipantsPriority);

    // create IncomlStorages
    File incomeDir = new File(storageDir + "/income");
    incomeDir.mkdirs();
    this.incomeStorageDir = incomeDir.getAbsolutePath();

    // create LocalStorages
    File localDir = new File(storageDir + "/local");
    localDir.mkdirs();
    this.localStorageDir = localDir.getAbsolutePath();

    File mergeTempDir = new File(storageDir + "/merge-temp");
    mergeTempDir.mkdirs();
    this.mergeTempDir = mergeTempDir.getAbsolutePath();

  }
  
  private boolean hasDuplicatePriority(List<Integer> other, int ownPriority) {
    if (other.contains(ownPriority))
      return true;

    for (int i = 0; i < other.size(); i++) {
      int pri = other.get(i);
      List<Integer> oth = new ArrayList<Integer>(other);
      oth.remove(i);

      if (oth.contains(pri))
        return true;
    }

    return false;
  }

  public int getPriority() {
    return priority;
  }

  public List<Integer> getOtherParticipantsPriority() {
    return otherParticipantsPriority;
  }

  public String getBindIPAddress() {
    return bindIPAddress;
  }

  public String getChannelConfig() {
    return channelConfig;
  }

  public String getChannelName() {
    return channelName;
  }

  public int getWaitAllMembersTimeout() {
    return waitAllMembersTimeout;
  }

  public String getMergeTempDir() {
    return mergeTempDir;
  }

  public String getStorageDir() {
    return storageDir;
  }

  public String getLocalStorageDir() {
    return localStorageDir;
  }

  public String getIncomeStorageDir() {
    return incomeStorageDir;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  public String getWorkspaceName() {
    return workspaceName;
  }
}
