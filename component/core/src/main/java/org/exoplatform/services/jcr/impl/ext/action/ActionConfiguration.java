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
package org.exoplatform.services.jcr.impl.ext.action;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class ActionConfiguration {

  private String  actionClassName;

  private String  eventTypes;

  private String  path;

  private boolean isDeep;

  private String  nodeTypes;

  private String  workspace;

  public ActionConfiguration() {
    this.actionClassName = null;
    this.eventTypes = null;
    this.path = null;
    this.isDeep = true;
    this.workspace = null;
    this.nodeTypes = null;
  }

  public ActionConfiguration(String actionClassName,
                             String eventTypes,
                             String path,
                             boolean isDeep,
                             String workspace,
                             String nodeTypes) {
    this.actionClassName = actionClassName;
    this.eventTypes = eventTypes;
    this.path = path;
    this.isDeep = isDeep;
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
