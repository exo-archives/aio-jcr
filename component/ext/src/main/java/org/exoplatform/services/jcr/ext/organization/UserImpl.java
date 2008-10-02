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

import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: UserImpl.java 111 2008-11-11 11:11:11Z peterit $
 */
public class UserImpl implements User {

  private String           id             = null;

  private String           userName       = null;

  private transient String password       = null;

  private String           firstName      = null;

  private String           lastName       = null;

  private String           email          = null;

  private Date             createdDate    = null;

  private Date             lastLoginTime  = null;

  private String           organizationId = null;

  /**
   * @see org.exoplatform.services.organization.User#getCreatedDate()
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @see org.exoplatform.services.organization.User#getEmail()
   */
  public String getEmail() {
    return email;
  }

  /**
   * @see org.exoplatform.services.organization.User#getFirstName()
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @see org.exoplatform.services.organization.User#getFullName()
   */
  public String getFullName() {
    return getFirstName() + " " + getLastName();
  }

  /**
   * @see org.exoplatform.services.organization.User#getLastLoginTime()
   */
  public Date getLastLoginTime() {
    return lastLoginTime;
  }

  /**
   * @see org.exoplatform.services.organization.User#getLastName()
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @see org.exoplatform.services.organization.User#getOrganizationId()
   */
  public String getOrganizationId() {
    return organizationId;
  }

  /**
   * @see org.exoplatform.services.organization.User#getPassword()
   */
  public String getPassword() {
    return password;
  }

  /**
   * @see org.exoplatform.services.organization.User#getUserName()
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @see org.exoplatform.services.organization.User#setCreatedDate(java.util.Date)
   */
  public void setCreatedDate(Date t) {
    createdDate = t;
  }

  /**
   * @see org.exoplatform.services.organization.User#setEmail(java.lang.String)
   */
  public void setEmail(String s) {
    email = s;
  }

  /**
   * @see org.exoplatform.services.organization.User#setFirstName(java.lang.String)
   */
  public void setFirstName(String s) {
    firstName = s;
  }

  /**
   * @see org.exoplatform.services.organization.User#setFullName(java.lang.String)
   */
  public void setFullName(String s) {
  }

  /**
   * @see org.exoplatform.services.organization.User#setLastLoginTime(java.util.Date)
   */
  public void setLastLoginTime(Date t) {
    lastLoginTime = t;
  }

  /**
   * @see org.exoplatform.services.organization.User#setLastName(java.lang.String)
   */
  public void setLastName(String s) {
    lastName = s;
  }

  /**
   * @see org.exoplatform.services.organization.User#setOrganizationId(java.lang.String)
   */
  public void setOrganizationId(String s) {
  }

  /**
   * @see org.exoplatform.services.organization.User#setPassword(java.lang.String)
   */
  public void setPassword(String s) {
    password = s;
  }

  /**
   * @see org.exoplatform.services.organization.User#setUserName(java.lang.String)
   */
  public void setUserName(String s) {
  }

}
