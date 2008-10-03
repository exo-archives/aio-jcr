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
 * @version $Id: GroupImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class GroupImpl implements Group {

  // not used
  private String id          = null;

  private String parentId;

  // not used
  private String groupName   = null;

  private String label;

  private String description = "";

  GroupImpl() {
  }

  GroupImpl(String label, String parendId) {
    this.label = label;
    this.parentId = parendId;
  }

  /**
   * @see org.exoplatform.services.organization.Group#getDescription()
   */
  public String getDescription() {
    return description;
  }

  /**
   * @see org.exoplatform.services.organization.Group#getGroupName()
   */
  public String getGroupName() {
    return groupName;
  }

  /**
   * @see org.exoplatform.services.organization.Group#getId()
   */
  public String getId() {
    return id;
  }

  /**
   * @see org.exoplatform.services.organization.Group#getLabel()
   */
  public String getLabel() {
    return label;
  }

  /**
   * @see org.exoplatform.services.organization.Group#getParentId()
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @see org.exoplatform.services.organization.Group#setDescription(java.lang.String )
   */
  public void setDescription(String desc) {
    description = desc;
  }

  /**
   * @see org.exoplatform.services.organization.Group#setGroupName(java.lang.String)
   */
  public void setGroupName(String name) {
  }

  /**
   * @see org.exoplatform.services.organization.Group#setLabel(java.lang.String)
   */
  public void setLabel(String name) {
    label = name;
  }

}
