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
 * Created by The eXo Platform SAS.
 * 
 * Date: 24.07.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class UserImpl implements User {

  /**
   * The user name
   */
  private String           userName;

  /**
   * The password of the user
   */
  private transient String password;

  /**
   * The first name of the user
   */
  private String           firstName;

  /**
   * The last name of the user
   */
  private String           lastName;

  /**
   * The email of the user
   */
  private String           email;

  /**
   * The user's created date
   */
  private Date             createdDate;

  /**
   * The last login time of the user
   */
  private Date             lastLoginTime;

  /**
   * The UUId of the user in the storage
   */
  private final String     UUId;

  UserImpl() {
    this.UUId = null;
  }

  UserImpl(String name) {
    this.userName = name;
    this.UUId = null;
  }

  UserImpl(String name, String UUId) {
    this.userName = name;
    this.UUId = UUId;
  }

  /**
   * @return The date that the user register or create the account
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @return The email address of the user
   */
  public String getEmail() {
    return email;
  }

  /**
   * @return This method return the first name of the user
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @return return the full name of the user. The full name shoul have the format: first name, last
   *         name by default
   */
  public String getFullName() {
    return getFirstName() + " " + getLastName();
  }

  /**
   * @return Return the last time that the user access the account
   */
  public Date getLastLoginTime() {
    return lastLoginTime;
  }

  /**
   * @return The last name of the user
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @return the id of organization the user belongs to or null if not applicable
   */
  public String getOrganizationId() {
    return null;
  }

  /**
   * @return This method return the password of the user account
   */
  public String getPassword() {
    return password;
  }

  /**
   * This method should return the username of the user. The username should be unique and the user
   * database should not have 2 user record with the same username
   * 
   * @return
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param t
   * @deprecated The third party should not used this method.
   */
  public void setCreatedDate(Date t) {
    createdDate = t;
  }

  /**
   * @param s
   *          The new user email address
   */
  public void setEmail(String s) {
    email = s;
  }

  /**
   * @param s
   *          the new first name
   */
  public void setFirstName(String s) {
    firstName = s;
  }

  /**
   * @param s
   *          The name that should show in the full name
   */
  public void setFullName(String s) {
  }

  /**
   * @param t
   * @deprecated The third party developer should not aware of this method
   */
  public void setLastLoginTime(Date t) {
    lastLoginTime = t;
  }

  /**
   * @param s
   *          The new last name of the user
   */
  public void setLastName(String s) {
    lastName = s;
  }

  /**
   * sets the prganizationId
   */
  public void setOrganizationId(String s) {
  }

  /**
   * This method is used to change the user account password.
   * 
   * @param s
   */
  public void setPassword(String s) {
    password = s;
  }

  /**
   * This method is used to change the username
   * 
   * @param s
   * @deprecated The third party developer should not used this method TODO: I think we should not
   *             have this method. the username should be set only for the first time. So we can
   *             pass the username to the @see UserHandler createUserInstance() method.
   */
  public void setUserName(String s) {
    userName = s;
  }

  /**
   * @return UUId of the user in the storage
   */
  String getUUId() {
    return UUId;
  }

}
