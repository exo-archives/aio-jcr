/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: RepositoryEntry.java 13730 2007-03-23 16:25:55Z ksm $
 */

public class RepositoryEntry {

  private String                    name;

  private String                    systemWorkspaceName;

  private String                    defaultWorkspaceName;

  private String                    accessControl;

  private ReplicationEntry          replication;

  private String                    securityDomain;

  private ArrayList<WorkspaceEntry> workspaces;

  private String                    authenticationPolicy;

  private BinarySwapEntry           binaryTemp;

  private long                      sessionTimeOut = -1;

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

  public ReplicationEntry getReplication() {
    return replication;
  }

  public void setReplication(ReplicationEntry replication) {
    this.replication = replication;
  }

  public long getSessionTimeOut() {
    return sessionTimeOut;
  }

  public void setSessionTimeOut(long sessionTimeOut) {
    this.sessionTimeOut = sessionTimeOut;
  }

}