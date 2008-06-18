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
package org.exoplatform.services.jcr.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: RepositoryEntry.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class RepositoryEntry {

  private String                    name;

  private String                    systemWorkspaceName;

  private String                    defaultWorkspaceName;

  private String                    accessControl;

  private String                    securityDomain;

  private ArrayList<WorkspaceEntry> workspaces;

  private String                    authenticationPolicy;

  private BinarySwapEntry           binaryTemp;

  private long                    sessionTimeOut;

  public RepositoryEntry() {
    workspaces = new ArrayList<WorkspaceEntry>();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the systemWorkspace.
   */
  public String getSystemWorkspaceName() {
    return systemWorkspaceName;
  }

  /**
   * @param systemWorkspace The systemWorkspace to set.
   */
  public void setSystemWorkspaceName(String systemWorkspace) {
    this.systemWorkspaceName = systemWorkspace;
  }

  /**
   * @return Returns the workspaces.
   */
  public List<WorkspaceEntry> getWorkspaceEntries() {
    return workspaces;
  }

  public void addWorkspace(WorkspaceEntry ws) {
    workspaces.add(ws);
  }

  /**
   * @return Returns the accessControl.
   */
  public String getAccessControl() {
    return accessControl;
  }

  /**
   * @param accessControl The accessControl to set.
   */
  public void setAccessControl(String accessControl) {
    this.accessControl = accessControl;
  }

  /**
   * @return Returns the securityDomain.
   */
  public String getSecurityDomain() {
    return securityDomain;
  }

  /**
   * @param securityDomain The securityDomain to set.
   */
  public void setSecurityDomain(String securityDomain) {
    this.securityDomain = securityDomain;
  }

  /**
   * @return Returns the authenticationPolicy.
   */
  public String getAuthenticationPolicy() {
    return authenticationPolicy;
  }

  /**
   * @param authenticationPolicy The authenticationPolicy to set.
   */
  public void setAuthenticationPolicy(String authenticationPolicy) {
    this.authenticationPolicy = authenticationPolicy;
  }

  /**
   * @return Returns the defaultWorkspaceName.
   */
  public String getDefaultWorkspaceName() {
    return defaultWorkspaceName;
  }

  /**
   * @param defaultWorkspaceName The defaultWorkspaceName to set.
   */
  public void setDefaultWorkspaceName(String defaultWorkspaceName) {
    this.defaultWorkspaceName = defaultWorkspaceName;
  }

  public BinarySwapEntry getBinaryTemp() {
    return binaryTemp;
  }

  public void setBinaryTemp(BinarySwapEntry binaryTemp) {
    this.binaryTemp = binaryTemp;
  }

  public long getSessionTimeOut() {
    return sessionTimeOut;
  }

  public void setSessionTimeOut(long sessionTimeOut) {
    this.sessionTimeOut = sessionTimeOut;
  }

}