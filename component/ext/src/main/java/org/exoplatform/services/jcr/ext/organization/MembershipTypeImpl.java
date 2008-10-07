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
 * @version $Id$
 */
public class MembershipTypeImpl implements MembershipType {

  /**
   * The description of the membership type
   */
  private String       description;

  /**
   * The name of the membership type
   */
  private String       name;

  /**
   * The UUID of the membership type in the storage
   */
  private final String UUId;

  /**
   * MembershipTypeImpl constructor.
   * 
   */
  MembershipTypeImpl() {
    this.UUId = null;
  }

  /**
   * MembershipTypeImpl constructor.
   * 
   * @param UUId
   *          - membership node id
   */
  MembershipTypeImpl(String UUId) {
    this.UUId = UUId;
  }

  /**
   * MembershipTypeImpl constructor.
   * 
   * @param name
   *          memebership name
   * @param description
   *          memebership description
   * @param UUId
   *          memebership node id
   */
  MembershipTypeImpl(String name, String description, String UUId) {
    this.name = name;
    this.UUId = UUId;
    this.description = description;
  }

  /**
   * @return The date that the membership type is saved to the database
   */
  public Date getCreatedDate() {
    return null;
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
    return null;
  }

  /**
   * @return The name of the membership type
   */
  public String getName() {
    return name;
  }

  /**
   * @return The owner of the membership type
   */
  public String getOwner() {
    return null;
  }

  /**
   * @return The UUID of the membership type in the storage
   */
  public String getUUId() {
    return UUId;
  }

  /**
   * Set created date of the membership type.
   * 
   * @deprecated This method is not used.
   * @param d
   *          The created date
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
   *          The modified date
   */
  public void setModifiedDate(Date d) {
  }

  /**
   * Set name for membership type.
   * 
   * @param s
   *          The new name of the membership type
   */
  public void setName(String s) {
    name = s;
  }

  /**
   * Set owner for membership type.
   * 
   * @deprecated This method is not used.
   * @param s
   *          The new owner of the membership type
   */
  public void setOwner(String s) {
  }

}
