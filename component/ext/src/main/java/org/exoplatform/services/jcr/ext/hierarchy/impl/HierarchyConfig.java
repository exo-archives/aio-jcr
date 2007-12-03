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
package org.exoplatform.services.jcr.ext.hierarchy.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 2:06:42 PM
 */
public class HierarchyConfig {

  private String repository ;
  private List<String> workspaces = new ArrayList<String>(5) ;
  private List<JcrPath> jcrPaths = new ArrayList<JcrPath>(5);

  public String getRepository() { return repository ; }
  public void setRepository(String rp) { repository = rp ; }

  public List<JcrPath> getJcrPaths() {   return jcrPaths; }
  public void setJcrPaths(List<JcrPath> s) {  this.jcrPaths = s; }

  public List<String> getWorkspaces() { return this.workspaces ; }
  public void setWorksapces(List<String> list) { this.workspaces = list ; }

  static public class JcrPath {
    private String alias ;
    private String path ;
    private String nodeType ;
    private List mixinTypes ;
    
    private List<Permission> permissions = new ArrayList<Permission>(4);

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<Permission> getPermissions() { return this.permissions ; }
    public void setPermissions(List<Permission> list) { this.permissions = list ; }
    
    public String getNodeType() { return this.nodeType ; }
    public void setNodeType(String nodetype) { this.nodeType = nodetype ; }
    
    public List getMixinTypes() { return mixinTypes ; }
    public void setMixinTypes(List mixinTypes) { this.mixinTypes = mixinTypes ; }
  }   

  static public class Permission {
    private String identity;
    private String read;
    private String addNode;
    private String setProperty;
    private String remove;

    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }

    public String getAddNode() { return addNode; }
    public void setAddNode(String addNode) { this.addNode = addNode; }

    public String getRead() { return read; }
    public void setRead(String read) { this.read = read; }

    public String getRemove() { return remove; }
    public void setRemove(String remove) { this.remove = remove; }

    public String getSetProperty() { return setProperty; }
    public void setSetProperty(String setProperty) { this.setProperty = setProperty; }
  }
}