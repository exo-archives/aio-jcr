/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.ext.action;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: ActionConfiguration.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class ActionConfiguration {

  private String       actionClassName;

  private String       eventTypes;

  private String       path;

  private boolean      isDeep;

  private String       nodeTypes;

  private final String parentNodeType;

  private final String nodeType;

  private String       workspace;

  public ActionConfiguration() {
    this.actionClassName = null;
    this.eventTypes = null;
    this.path = null;
    this.isDeep = true;
    this.parentNodeType = null;
    this.nodeType = null;
    this.workspace = null;
    this.nodeTypes = null;
  }

  public ActionConfiguration(String actionClassName,
                             String eventTypes,
                             String path,
                             boolean isDeep,
                             String parentNodeType,
                             String nodeType,
                             String workspace,
                             String nodeTypes) {
    this.actionClassName = actionClassName;
    this.eventTypes = eventTypes;
    this.path = path;
    this.isDeep = isDeep;
    this.parentNodeType = parentNodeType;
    this.nodeType = nodeType;
    this.workspace = workspace;
    this.nodeTypes = nodeTypes;
  }

  public String getActionClassName() {
    return actionClassName;
  }

  public String getEventTypes() {
    return eventTypes;
  }

  public String getNodeTypes() {
    return nodeTypes;
  }

  public String getPath() {
    return path;
  }

  public String getWorkspace() {
    return workspace;
  }

  public boolean isDeep() {
    return isDeep;
  }

  public void setActionClassName(String actionClassName) {
    this.actionClassName = actionClassName;
  }

  public void setDeep(boolean isDeep) {
    this.isDeep = isDeep;
  }

  public void setEventTypes(String eventTypes) {
    this.eventTypes = eventTypes;
  }

  public void setNodeTypes(String nodeTypes) {
    this.nodeTypes = nodeTypes;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }
}
