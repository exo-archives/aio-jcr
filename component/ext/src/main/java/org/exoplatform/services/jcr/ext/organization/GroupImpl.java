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
package org.exoplatform.services.jcr.ext.organization;

import org.exoplatform.services.organization.Group;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class GroupImpl implements Group {

  private String parentId;

  private String label;

  private String description;

  private String groupName;

  GroupImpl() {
  }

  GroupImpl(String name, String parendId) {
    this.groupName = name;
    this.parentId = parendId;
  }

  /**
   * @return The group description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return The local name of the group
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * @deprecated This method not used
   * @return the id of the group. The id should have the form /ancestor/parent/groupname
   */
  public String getId() {
    return null;
  }

  /**
   * @return The display label of the group.
   */
  public String getLabel() {
    return label;
  }

  /**
   * @return the id of the parent group. if the parent id is null , it mean that the group is at the
   *         first level. the child of root group.
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @param desc
   *          The new description of the group
   */
  public void setDescription(String desc) {
    description = desc;
  }

  /**
   * @deprecated This method not used
   * @param name
   *          The local name for the group
   */
  public void setGroupName(String name) {
    groupName = name;
  }

  /**
   * @param name
   *          The new label of the group
   */
  public void setLabel(String name) {
    label = name;
  }

}
