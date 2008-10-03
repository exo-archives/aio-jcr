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

import java.util.Date;

import org.exoplatform.services.organization.MembershipType;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: MembershipTypeImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class MembershipTypeImpl implements MembershipType {

  private Date   createdDate  = null;

  private String description  = null;

  private Date   modifiedDate = null;

  private String name         = null;

  private String owner        = null;

  /**
   * @return The date that the membership type is saved to the database
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @return The description of the membership type
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return The last time that an user modify the data of the membership type.
   */
  public Date getModifiedDate() {
    return modifiedDate;
  }

  /**
   * @return The name of the membership type. The name of the membership type should be unique in
   *         the membership type database.
   */
  public String getName() {
    return name;
  }

  /**
   * @return The owner of the membership type
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Set created date for membership type.
   * 
   * @param d
   *          the created date
   */
  public void setCreatedDate(Date d) {
  }

  /**
   * Set description for membership type.
   * 
   * @param s
   *          The new description of the membership type
   */
  public void setDescription(String s) {
    description = s;
  }

  /**
   * Set modified date for membership type.
   * 
   * @param d
   *          the modified date
   */
  public void setModifiedDate(Date d) {
  }

  /**
   * Set name for membership type.
   * 
   * @param s
   *          The name of the membership type
   */
  public void setName(String s) {
    name = s;
  }

  /**
   * Set owner for membership type.
   * 
   * @param s
   *          The new owner of the membership type
   */
  public void setOwner(String s) {
  }

}
